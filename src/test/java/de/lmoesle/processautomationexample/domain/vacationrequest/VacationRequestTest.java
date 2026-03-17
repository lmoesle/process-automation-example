package de.lmoesle.processautomationexample.domain.vacationrequest;

import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestStatus;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestStatusHistoryEntry;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestTestData.PROCESS_INSTANCE_ID_VALUE;
import static de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestTestData.statusHistory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VacationRequestTest {

    @Test
    void submitsVacationRequestWithGeneratedId() {
        VacationRequest vacationRequest = VacationRequest.submit(
            VacationRequestTestData.FROM,
            VacationRequestTestData.TO,
            VacationRequestTestData.applicantUser(),
            VacationRequestTestData.substituteUser()
        );

        assertThat(vacationRequest.id()).isNotNull();
        assertThat(vacationRequest.period().from()).isEqualTo(VacationRequestTestData.FROM);
        assertThat(vacationRequest.period().to()).isEqualTo(VacationRequestTestData.TO);
        assertThat(vacationRequest.applicantUser()).isEqualTo(VacationRequestTestData.applicantUser());
        assertThat(vacationRequest.substituteUser()).isEqualTo(VacationRequestTestData.substituteUser());
        assertThat(vacationRequest.processInstanceId()).isNull();
        assertThat(vacationRequest.status()).isEqualTo(VacationRequestStatus.ANTRAG_GESTELLT);
        assertThat(vacationRequest.statusHistory()).hasSize(1)
            .first()
            .satisfies(entry -> {
                assertThat(entry.status()).isEqualTo(VacationRequestStatus.ANTRAG_GESTELLT);
                assertThat(entry.comment()).isNull();
            });
    }

    @Test
    void reconstitutesVacationRequestWithExistingState() {
        List<VacationRequestStatusHistoryEntry> history = statusHistory(
            VacationRequestStatus.ANTRAG_GESTELLT,
            VacationRequestStatus.LEAD_PRUEFUNG
        );
        VacationRequest vacationRequest = VacationRequest.reconstitute(
            VacationRequestTestData.vacationRequestId(),
            VacationRequestTestData.vacationPeriod(),
            VacationRequestTestData.applicantUser(),
            VacationRequestTestData.substituteUser(),
            VacationRequestStatus.LEAD_PRUEFUNG,
            history,
            VacationRequestTestData.processInstanceId()
        );

        assertThat(vacationRequest.id()).isEqualTo(VacationRequestTestData.vacationRequestId());
        assertThat(vacationRequest.period().from()).isEqualTo(VacationRequestTestData.FROM);
        assertThat(vacationRequest.period().to()).isEqualTo(VacationRequestTestData.TO);
        assertThat(vacationRequest.applicantUser()).isEqualTo(VacationRequestTestData.applicantUser());
        assertThat(vacationRequest.substituteUser()).isEqualTo(VacationRequestTestData.substituteUser());
        assertThat(vacationRequest.processInstanceId()).isEqualTo(VacationRequestTestData.processInstanceId());
        assertThat(vacationRequest.status()).isEqualTo(VacationRequestStatus.LEAD_PRUEFUNG);
        assertThat(vacationRequest.statusHistory()).isEqualTo(history);
    }

    @Test
    void rejectsVacationPeriodWhenFromIsAfterTo() {
        assertThatThrownBy(() -> VacationRequest.submit(
            LocalDate.parse("2026-07-10"),
            LocalDate.parse("2026-07-01"),
            VacationRequestTestData.applicantUser(),
            null
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("'from' must be on or before 'to'.");
    }

    @Test
    void rejectsMissingApplicantUser() {
        assertThatThrownBy(() -> VacationRequest.submit(
            VacationRequestTestData.FROM,
            VacationRequestTestData.TO,
            null,
            VacationRequestTestData.substituteUser()
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("applicantUser must not be null");
    }

    @Test
    void automaticCheckIsValidWhenNoSubstituteUserIsConfigured() {
        VacationRequest vacationRequest = VacationRequest.submit(
            VacationRequestTestData.FROM,
            VacationRequestTestData.TO,
            VacationRequestTestData.applicantUser(),
            null
        );

        assertThat(vacationRequest.isAutomaticallyValidAgainst(
            java.util.List.of(VacationRequestTestData.secondVacationRequest(VacationRequestTestData.substituteUser(), null))
        )).isTrue();
    }

    @Test
    void automaticCheckIsValidWhenSubstituteUserHasNoOverlappingVacationRequest() {
        VacationRequest vacationRequest = VacationRequestTestData.vacationRequest();

        assertThat(vacationRequest.isAutomaticallyValidAgainst(
            java.util.List.of(VacationRequestTestData.secondVacationRequest(VacationRequestTestData.substituteUser(), null))
        )).isTrue();
    }

    @Test
    void automaticCheckIsInvalidWhenSubstituteUserHasOverlappingVacationRequest() {
        VacationRequest vacationRequest = VacationRequestTestData.vacationRequest();
        VacationRequest overlappingVacationRequest = VacationRequestTestData.vacationRequest(
            VacationRequestTestData.secondVacationRequestId(),
            VacationRequestTestData.vacationPeriod(),
            VacationRequestTestData.substituteUser(),
            null,
            VacationRequestTestData.secondProcessInstanceId()
        );

        assertThat(vacationRequest.isAutomaticallyValidAgainst(java.util.List.of(overlappingVacationRequest))).isFalse();
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

    @Test
    void rejectsNullVacationRequestsWhenRunningAutomaticCheck() {
        VacationRequest vacationRequest = VacationRequestTestData.vacationRequest();

        assertThatThrownBy(() -> vacationRequest.isAutomaticallyValidAgainst(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("substituteVacationRequests must not be null");
    }

    @Test
    void startsAutomaticCheckAddsStatusHistory() {
        VacationRequest vacationRequest = VacationRequestTestData.vacationRequest();

        vacationRequest.startAutomaticCheck();

        assertThat(vacationRequest.status()).isEqualTo(VacationRequestStatus.AUTOMATISCHE_PRUEFUNG);
        assertThat(vacationRequest.statusHistory()).hasSize(2)
            .last()
            .satisfies(entry -> {
                assertThat(entry.status()).isEqualTo(VacationRequestStatus.AUTOMATISCHE_PRUEFUNG);
                assertThat(entry.comment()).isNull();
            });
    }

    @Test
    void startAutomaticCheckIsIdempotent() {
        VacationRequest vacationRequest = VacationRequestTestData.vacationRequest();

        vacationRequest.startAutomaticCheck();
        int firstEntryCount = vacationRequest.statusHistory().size();

        vacationRequest.startAutomaticCheck();

        assertThat(vacationRequest.statusHistory()).hasSize(firstEntryCount);
    }
}
