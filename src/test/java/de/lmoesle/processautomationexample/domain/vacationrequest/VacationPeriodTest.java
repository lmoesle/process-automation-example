package de.lmoesle.processautomationexample.domain.vacationrequest;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VacationPeriodTest {

    @Test
    void createsVacationPeriod() {
        VacationPeriod vacationPeriod = VacationPeriod.of(
            VacationRequestTestData.FROM,
            VacationRequestTestData.TO
        );

        assertThat(vacationPeriod.from()).isEqualTo(VacationRequestTestData.FROM);
        assertThat(vacationPeriod.to()).isEqualTo(VacationRequestTestData.TO);
    }

    @Test
    void rejectsNullFromDate() {
        assertThatThrownBy(() -> VacationPeriod.of(null, VacationRequestTestData.TO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("from must not be null");
    }

    @Test
    void rejectsNullToDate() {
        assertThatThrownBy(() -> VacationPeriod.of(VacationRequestTestData.FROM, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("to must not be null");
    }

    @Test
    void rejectsFromAfterTo() {
        assertThatThrownBy(() -> VacationPeriod.of(
            LocalDate.parse("2026-07-10"),
            LocalDate.parse("2026-07-01")
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("'from' must be on or before 'to'.");
    }
}
