package de.lmoesle.processautomationexample.domain.vacationrequest;

import de.lmoesle.processautomationexample.domain.user.User;
import de.lmoesle.processautomationexample.domain.user.UserId;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class VacationRequestTestData {

    public static final UUID VACATION_REQUEST_UUID = UUID.fromString("c7a6939b-a97b-4445-bd66-4a0f98781899");
    public static final UUID SECOND_VACATION_REQUEST_UUID = UUID.fromString("a91e8877-f17a-40d4-a9ee-1b0350f27b52");
    public static final UUID APPLICANT_USER_UUID = UUID.fromString("772699cf-4ebd-4eb1-bf18-0f6d7569d9bb");
    public static final UUID SUBSTITUTE_USER_UUID = UUID.fromString("1deec1f5-2f8d-456d-bef5-b3fb75f6f028");
    public static final String PROCESS_INSTANCE_ID_VALUE = "process-instance-42";
    public static final String SECOND_PROCESS_INSTANCE_ID_VALUE = "process-instance-84";
    public static final LocalDate FROM = LocalDate.parse("2026-07-01");
    public static final LocalDate TO = LocalDate.parse("2026-07-10");
    public static final LocalDate SECOND_FROM = LocalDate.parse("2026-08-15");
    public static final LocalDate SECOND_TO = LocalDate.parse("2026-08-22");

    private VacationRequestTestData() {
    }

    public static VacationRequestId vacationRequestId() {
        return VacationRequestId.of(VACATION_REQUEST_UUID);
    }

    public static UserId applicantUserId() {
        return UserId.of(APPLICANT_USER_UUID);
    }

    public static User applicantUser() {
        return User.reconstitute(
            applicantUserId(),
            "Applicant User",
            "applicant.user@example.com",
            List.of()
        );
    }

    public static UserId substituteUserId() {
        return UserId.of(SUBSTITUTE_USER_UUID);
    }

    public static User substituteUser() {
        return User.reconstitute(
            substituteUserId(),
            "Substitute User",
            "substitute.user@example.com",
            List.of()
        );
    }

    public static ProcessInstanceId processInstanceId() {
        return ProcessInstanceId.of(PROCESS_INSTANCE_ID_VALUE);
    }

    public static VacationPeriod vacationPeriod() {
        return VacationPeriod.of(FROM, TO);
    }

    public static VacationRequest vacationRequest() {
        return vacationRequest(vacationRequestId(), vacationPeriod(), applicantUser(), substituteUser(), null);
    }

    public static VacationRequest vacationRequestWithStartedProcess() {
        return vacationRequest(
            vacationRequestId(),
            vacationPeriod(),
            applicantUser(),
            substituteUser(),
            processInstanceId()
        );
    }

    public static VacationRequestId secondVacationRequestId() {
        return VacationRequestId.of(SECOND_VACATION_REQUEST_UUID);
    }

    public static VacationPeriod secondVacationPeriod() {
        return VacationPeriod.of(SECOND_FROM, SECOND_TO);
    }

    public static ProcessInstanceId secondProcessInstanceId() {
        return ProcessInstanceId.of(SECOND_PROCESS_INSTANCE_ID_VALUE);
    }

    public static VacationRequest secondVacationRequest(User applicantUser, User substituteUser) {
        return vacationRequest(
            secondVacationRequestId(),
            secondVacationPeriod(),
            applicantUser,
            substituteUser,
            secondProcessInstanceId()
        );
    }

    public static VacationRequest vacationRequest(
        VacationRequestId vacationRequestId,
        VacationPeriod vacationPeriod,
        User applicantUser,
        User substituteUser,
        ProcessInstanceId processInstanceId
    ) {
        return VacationRequest.reconstitute(
            vacationRequestId,
            vacationPeriod,
            applicantUser,
            substituteUser,
            processInstanceId
        );
    }
}
