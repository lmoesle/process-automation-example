package de.lmoesle.processautomationexample.domain.vacationrequest;

import de.lmoesle.processautomationexample.domain.user.UserId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdentityObjectsTest {

    @Test
    void createsVacationRequestIdFromUuid() {
        assertThat(VacationRequestTestData.vacationRequestId().value())
            .isEqualTo(VacationRequestTestData.VACATION_REQUEST_UUID);
    }

    @Test
    void createsGeneratedVacationRequestId() {
        assertThat(VacationRequestId.newId().value()).isNotNull();
    }

    @Test
    void rejectsNullVacationRequestIdValue() {
        assertThatThrownBy(() -> VacationRequestId.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("value must not be null");
    }

    @Test
    void createsUserIdFromUuid() {
        assertThat(VacationRequestTestData.applicantUserId().value())
            .isEqualTo(VacationRequestTestData.APPLICANT_USER_UUID);
        assertThat(VacationRequestTestData.substituteUserId().value())
            .isEqualTo(VacationRequestTestData.SUBSTITUTE_USER_UUID);
    }

    @Test
    void rejectsNullUserIdValue() {
        assertThatThrownBy(() -> UserId.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("value must not be null");
    }

    @Test
    void createsProcessInstanceIdFromText() {
        assertThat(VacationRequestTestData.processInstanceId().value())
            .isEqualTo(VacationRequestTestData.PROCESS_INSTANCE_ID_VALUE);
    }

    @Test
    void rejectsBlankProcessInstanceIdValue() {
        assertThatThrownBy(() -> ProcessInstanceId.of("  "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("value must not be blank");
    }
}
