package de.lmoesle.processautomationexample.domain.vacationrequest;

import de.lmoesle.processautomationexample.domain.user.User;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.List;

@Getter
@Accessors(fluent = true)
public final class VacationRequest {

    private final VacationRequestId id;
    private final VacationPeriod period;
    private final User applicantUser;
    private final User substituteUser;
    private ProcessInstanceId processInstanceId;

    private VacationRequest(
        VacationRequestId id,
        VacationPeriod period,
        User applicantUser,
        User substituteUser,
        ProcessInstanceId processInstanceId
    ) {
        Assert.notNull(id, "id must not be null");
        Assert.notNull(period, "period must not be null");
        Assert.notNull(applicantUser, "applicantUser must not be null");
        this.id = id;
        this.period = period;
        this.applicantUser = applicantUser;
        this.substituteUser = substituteUser;
        this.processInstanceId = processInstanceId;
    }

    public static VacationRequest submit(LocalDate from, LocalDate to, User applicantUser, User substituteUser) {
        return new VacationRequest(VacationRequestId.newId(), VacationPeriod.of(from, to), applicantUser, substituteUser, null);
    }

    public static VacationRequest reconstitute(
        VacationRequestId id,
        VacationPeriod period,
        User applicantUser,
        User substituteUser,
        ProcessInstanceId processInstanceId
    ) {
        return new VacationRequest(id, period, applicantUser, substituteUser, processInstanceId);
    }

    public void markApprovalProcessStarted(ProcessInstanceId processInstanceId) {
        Assert.notNull(processInstanceId, "processInstanceId must not be null");
        Assert.state(this.processInstanceId == null, "Approval process already started.");
        this.processInstanceId = processInstanceId;
    }

    public boolean isAutomaticallyValidAgainst(List<VacationRequest> substituteVacationRequests) {
        if (substituteUser == null) {
            return true;

        }
        Assert.notNull(substituteVacationRequests, "substituteVacationRequests must not be null");
        return substituteVacationRequests.stream()
            .noneMatch(vacationRequest -> period.overlaps(vacationRequest.period()));
    }
}
