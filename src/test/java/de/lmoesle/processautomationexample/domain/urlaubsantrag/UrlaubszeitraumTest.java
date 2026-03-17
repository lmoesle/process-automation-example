package de.lmoesle.processautomationexample.domain.urlaubsantrag;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrlaubszeitraumTest {

    @Test
    void createsUrlaubszeitraum() {
        Urlaubszeitraum urlaubszeitraum = Urlaubszeitraum.of(
            UrlaubsantragTestData.FROM,
            UrlaubsantragTestData.TO
        );

        assertThat(urlaubszeitraum.von()).isEqualTo(UrlaubsantragTestData.FROM);
        assertThat(urlaubszeitraum.bis()).isEqualTo(UrlaubsantragTestData.TO);
    }

    @Test
    void rejectsNullFromDate() {
        assertThatThrownBy(() -> Urlaubszeitraum.of(null, UrlaubsantragTestData.TO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("von darf nicht null sein");
    }

    @Test
    void rejectsNullToDate() {
        assertThatThrownBy(() -> Urlaubszeitraum.of(UrlaubsantragTestData.FROM, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("bis darf nicht null sein");
    }

    @Test
    void rejectsFromAfterTo() {
        assertThatThrownBy(() -> Urlaubszeitraum.of(
            LocalDate.parse("2026-07-10"),
            LocalDate.parse("2026-07-01")
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("'von' muss vor oder gleich 'bis' liegen.");
    }
}
