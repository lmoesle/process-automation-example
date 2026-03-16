package de.lmoesle.processautomationexample.domain.vacationrequest;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestTestData.PROCESS_INSTANCE_ID_VALUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VacationRequestTest {

    @Test
    void submitsVacationRequestWithGeneratedId() {
        VacationRequest vacationRequest = VacationRequest.submit(
            VacationRequestTestData.FROM,
            VacationRequestTestData.TO,
            VacationRequestTestData.applicantUserId(),
            VacationRequestTestData.substituteUserId()
        );

        assertThat(vacationRequest.id()).isNotNull();
        assertThat(vacationRequest.period().from()).isEqualTo(VacationRequestTestData.FROM);
        assertThat(vacationRequest.period().to()).isEqualTo(VacationRequestTestData.TO);
        assertThat(vacationRequest.applicantUserId()).isEqualTo(VacationRequestTestData.applicantUserId());
        assertThat(vacationRequest.substituteUserId()).isEqualTo(VacationRequestTestData.substituteUserId());
        assertThat(vacationRequest.processInstanceId()).isNull();
    }

    @Test
    void reconstitutesVacationRequestWithExistingState() {
        VacationRequest vacationRequest = VacationRequest.reconstitute(
            VacationRequestTestData.vacationRequestId(),
            VacationRequestTestData.vacationPeriod(),
            VacationRequestTestData.applicantUserId(),
            VacationRequestTestData.substituteUserId(),
            VacationRequestTestData.processInstanceId()
        );

        assertThat(vacationRequest.id()).isEqualTo(VacationRequestTestData.vacationRequestId());
        assertThat(vacationRequest.period().from()).isEqualTo(VacationRequestTestData.FROM);
        assertThat(vacationRequest.period().to()).isEqualTo(VacationRequestTestData.TO);
        assertThat(vacationRequest.applicantUserId()).isEqualTo(VacationRequestTestData.applicantUserId());
        assertThat(vacationRequest.substituteUserId()).isEqualTo(VacationRequestTestData.substituteUserId());
        assertThat(vacationRequest.processInstanceId()).isEqualTo(VacationRequestTestData.processInstanceId());
    }

    @Test
    void rejectsVacationPeriodWhenFromIsAfterTo() {
        assertThatThrownBy(() -> VacationRequest.submit(
            LocalDate.parse("2026-07-10"),
            LocalDate.parse("2026-07-01"),
            VacationRequestTestData.applicantUserId(),
            null
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("'from' must be on or before 'to'.");
    }

    @Test
    void rejectsMissingApplicantUserId() {
        assertThatThrownBy(() -> VacationRequest.submit(
            VacationRequestTestData.FROM,
            VacationRequestTestData.TO,
            null,
            VacationRequestTestData.substituteUserId()
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("applicantUserId must not be null");
    }

    @Test
    void marksApprovalProcessAsStarted() {
        VacationRequest vacationRequest = VacationRequestTestData.vacationRequest();

        vacationRequest.markApprovalProcessStarted(ProcessInstanceId.of(PROCESS_INSTANCE_ID_VALUE));

        assertThat(vacationRequest.processInstanceId()).isEqualTo(VacationRequestTestData.processInstanceId());
    }

    @Test
    void rejectsNullProcessInstanceIdWhenStartingApprovalProcess() {
        VacationRequest vacationRequest = VacationRequestTestData.vacationRequest();

        assertThatThrownBy(() -> vacationRequest.markApprovalProcessStarted(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("processInstanceId must not be null");
    }

    @Test
    void rejectsStartingApprovalProcessTwice() {
        VacationRequest vacationRequest = VacationRequestTestData.vacationRequestWithStartedProcess();

        assertThatThrownBy(() -> vacationRequest.markApprovalProcessStarted(ProcessInstanceId.of("process-instance-99")))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Approval process already started.");
    }
}
