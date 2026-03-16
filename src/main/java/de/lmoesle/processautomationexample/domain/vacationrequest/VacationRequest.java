package de.lmoesle.processautomationexample.domain.vacationrequest;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.util.Assert;

import java.time.LocalDate;

@Getter
@Accessors(fluent = true)
public final class VacationRequest {

    private final VacationRequestId id;
    private final VacationPeriod period;
    private final UserId applicantUserId;
    private final UserId substituteUserId;
    private ProcessInstanceId processInstanceId;

    private VacationRequest(
        VacationRequestId id,
        VacationPeriod period,
        UserId applicantUserId,
        UserId substituteUserId,
        ProcessInstanceId processInstanceId
    ) {
        Assert.notNull(id, "id must not be null");
        Assert.notNull(period, "period must not be null");
        Assert.notNull(applicantUserId, "applicantUserId must not be null");
        this.id = id;
        this.period = period;
        this.applicantUserId = applicantUserId;
        this.substituteUserId = substituteUserId;
        this.processInstanceId = processInstanceId;
    }

    public static VacationRequest submit(LocalDate from, LocalDate to, UserId applicantUserId, UserId substituteUserId) {
        return new VacationRequest(VacationRequestId.newId(), VacationPeriod.of(from, to), applicantUserId, substituteUserId, null);
    }

    public static VacationRequest reconstitute(
        VacationRequestId id,
        VacationPeriod period,
        UserId applicantUserId,
        UserId substituteUserId,
        ProcessInstanceId processInstanceId
    ) {
        return new VacationRequest(id, period, applicantUserId, substituteUserId, processInstanceId);
    }

    public void markApprovalProcessStarted(ProcessInstanceId processInstanceId) {
        Assert.notNull(processInstanceId, "processInstanceId must not be null");
        Assert.state(this.processInstanceId == null, "Approval process already started.");
        this.processInstanceId = processInstanceId;
    }
}
