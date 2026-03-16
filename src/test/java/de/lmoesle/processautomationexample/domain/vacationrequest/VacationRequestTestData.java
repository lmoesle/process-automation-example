package de.lmoesle.processautomationexample.domain.vacationrequest;

import java.time.LocalDate;
import java.util.UUID;

public final class VacationRequestTestData {

    public static final UUID VACATION_REQUEST_UUID = UUID.fromString("c7a6939b-a97b-4445-bd66-4a0f98781899");
    public static final UUID APPLICANT_USER_UUID = UUID.fromString("772699cf-4ebd-4eb1-bf18-0f6d7569d9bb");
    public static final UUID SUBSTITUTE_USER_UUID = UUID.fromString("1deec1f5-2f8d-456d-bef5-b3fb75f6f028");
    public static final String PROCESS_INSTANCE_ID_VALUE = "process-instance-42";
    public static final LocalDate FROM = LocalDate.parse("2026-07-01");
    public static final LocalDate TO = LocalDate.parse("2026-07-10");

    private VacationRequestTestData() {
    }

    public static VacationRequestId vacationRequestId() {
        return VacationRequestId.of(VACATION_REQUEST_UUID);
    }

    public static UserId applicantUserId() {
        return UserId.of(APPLICANT_USER_UUID);
    }

    public static UserId substituteUserId() {
        return UserId.of(SUBSTITUTE_USER_UUID);
    }

    public static ProcessInstanceId processInstanceId() {
        return ProcessInstanceId.of(PROCESS_INSTANCE_ID_VALUE);
    }

    public static VacationPeriod vacationPeriod() {
        return VacationPeriod.of(FROM, TO);
    }

    public static VacationRequest vacationRequest() {
        return VacationRequest.reconstitute(
            vacationRequestId(),
            vacationPeriod(),
            applicantUserId(),
            substituteUserId(),
            null
        );
    }

    public static VacationRequest vacationRequestWithStartedProcess() {
        return VacationRequest.reconstitute(
            vacationRequestId(),
            vacationPeriod(),
            applicantUserId(),
            substituteUserId(),
            processInstanceId()
        );
    }
}
