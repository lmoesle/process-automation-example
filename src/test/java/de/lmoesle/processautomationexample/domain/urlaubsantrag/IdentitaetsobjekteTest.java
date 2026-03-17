package de.lmoesle.processautomationexample.domain.urlaubsantrag;

import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdentitaetsobjekteTest {

    @Test
    void createsUrlaubsantragIdFromUuid() {
        assertThat(UrlaubsantragTestData.urlaubsantragId().value())
            .isEqualTo(UrlaubsantragTestData.VACATION_REQUEST_UUID);
    }

    @Test
    void createsGeneratedUrlaubsantragId() {
        assertThat(UrlaubsantragId.newId().value()).isNotNull();
    }

    @Test
    void createsUrlaubsantragIdFromText() {
        assertThat(UrlaubsantragId.of(UrlaubsantragTestData.VACATION_REQUEST_UUID.toString()).value())
            .isEqualTo(UrlaubsantragTestData.VACATION_REQUEST_UUID);
    }

    @Test
    void rejectsNullUrlaubsantragIdValue() {
        assertThatThrownBy(() -> UrlaubsantragId.of((UUID) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("wert darf nicht null sein");
    }

    @Test
    void createsBenutzerIdFromUuid() {
        assertThat(UrlaubsantragTestData.antragstellerId().value())
            .isEqualTo(UrlaubsantragTestData.APPLICANT_USER_UUID);
        assertThat(UrlaubsantragTestData.vertretungId().value())
            .isEqualTo(UrlaubsantragTestData.SUBSTITUTE_USER_UUID);
    }

    @Test
    void rejectsNullBenutzerIdValue() {
        assertThatThrownBy(() -> BenutzerId.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("wert darf nicht null sein");
    }

    @Test
    void createsProzessinstanzIdFromText() {
        assertThat(UrlaubsantragTestData.prozessinstanzId().value())
            .isEqualTo(UrlaubsantragTestData.PROCESS_INSTANCE_ID_VALUE);
    }

    @Test
    void rejectsBlankProzessinstanzIdValue() {
        assertThatThrownBy(() -> ProzessinstanzId.of("  "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("wert darf nicht leer sein");
    }
}
