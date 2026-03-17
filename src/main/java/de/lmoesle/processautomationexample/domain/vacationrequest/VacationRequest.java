package de.lmoesle.processautomationexample.domain.vacationrequest;

import de.lmoesle.processautomationexample.domain.user.User;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(fluent = true)
public final class VacationRequest {

    private final VacationRequestId id;
    private final VacationPeriod period;
    private final User applicantUser;
    private final User substituteUser;
    private VacationRequestStatus status;
    private final List<VacationRequestStatusHistoryEntry> statusHistory;
    private ProcessInstanceId processInstanceId;

    private VacationRequest(
        VacationRequestId id,
        VacationPeriod period,
        User applicantUser,
        User substituteUser,
        VacationRequestStatus status,
        List<VacationRequestStatusHistoryEntry> statusHistory,
        ProcessInstanceId processInstanceId
    ) {
        Assert.notNull(id, "id must not be null");
        Assert.notNull(period, "period must not be null");
        Assert.notNull(applicantUser, "applicantUser must not be null");
        Assert.notNull(status, "status must not be null");
        Assert.notEmpty(statusHistory, "statusHistory must not be empty");
        Assert.isTrue(
            statusHistory.getLast().status() == status,
            "Last status history entry must match current status."
        );
        this.id = id;
        this.period = period;
        this.applicantUser = applicantUser;
        this.substituteUser = substituteUser;
        this.status = status;
        this.statusHistory = new ArrayList<>(statusHistory);
        this.processInstanceId = processInstanceId;
    }

    public static VacationRequest submit(LocalDate from, LocalDate to, User applicantUser, User substituteUser) {
        return new VacationRequest(
            VacationRequestId.newId(),
            VacationPeriod.of(from, to),
            applicantUser,
            substituteUser,
            VacationRequestStatus.ANTRAG_GESTELLT,
            List.of(VacationRequestStatusHistoryEntry.withoutComment(VacationRequestStatus.ANTRAG_GESTELLT)),
            null
        );
    }

    public static VacationRequest reconstitute(
        VacationRequestId id,
        VacationPeriod period,
        User applicantUser,
        User substituteUser,
        VacationRequestStatus status,
        List<VacationRequestStatusHistoryEntry> statusHistory,
        ProcessInstanceId processInstanceId
    ) {
        return new VacationRequest(id, period, applicantUser, substituteUser, status, statusHistory, processInstanceId);
    }

    public void markApprovalProcessStarted(ProcessInstanceId processInstanceId) {
        Assert.notNull(processInstanceId, "processInstanceId must not be null");
        Assert.state(this.processInstanceId == null, "Approval process already started.");
        this.processInstanceId = processInstanceId;
    }

    public void startAutomaticCheck() {
        if (status == VacationRequestStatus.AUTOMATISCHE_PRUEFUNG) {
            return;
        }
        Assert.state(
            status == VacationRequestStatus.ANTRAG_GESTELLT,
            "Automatic check can only be started for submitted vacation requests."
        );
        transitionTo(VacationRequestStatus.AUTOMATISCHE_PRUEFUNG, null);
    }

    public List<VacationRequestStatusHistoryEntry> statusHistory() {
        return List.copyOf(statusHistory);
    }

    public boolean isAutomaticallyValidAgainst(List<VacationRequest> substituteVacationRequests) {
        if (substituteUser == null) {
            return true;

        }
        Assert.notNull(substituteVacationRequests, "substituteVacationRequests must not be null");
        return substituteVacationRequests.stream()
            .noneMatch(vacationRequest -> period.overlaps(vacationRequest.period()));
    }

    private void transitionTo(VacationRequestStatus newStatus, String comment) {
        status = newStatus;
        statusHistory.add(new VacationRequestStatusHistoryEntry(newStatus, comment));
    }
}
