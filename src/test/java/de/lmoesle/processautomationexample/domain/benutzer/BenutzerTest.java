package de.lmoesle.processautomationexample.domain.benutzer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BenutzerTest {

    @Test
    void reconstitutesUserWithTeams() {
        Benutzer benutzer = Benutzer.rekonstituiere(
            BenutzerTestdaten.adaId(),
            "Ada Lovelace",
            "ada.lovelace@example.com",
            List.of(BenutzerTestdaten.engineeringLeadTeam(), BenutzerTestdaten.platformUserTeam())
        );

        assertThat(benutzer.id()).isEqualTo(BenutzerTestdaten.adaId());
        assertThat(benutzer.name()).isEqualTo("Ada Lovelace");
        assertThat(benutzer.email()).isEqualTo("ada.lovelace@example.com");
        assertThat(benutzer.teams()).containsExactly(
            BenutzerTestdaten.engineeringLeadTeam(),
            BenutzerTestdaten.platformUserTeam()
        );
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> Benutzer.rekonstituiere(
            BenutzerTestdaten.adaId(),
            " ",
            "ada.lovelace@example.com",
            List.of(BenutzerTestdaten.engineeringLeadTeam())
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("name darf nicht leer sein");
    }

    @Test
    void rejectsBlankTeamName() {
        assertThatThrownBy(() -> new Team(" ", TeamRolle.LEITUNG))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("name darf nicht leer sein");
    }

    @Test
    void createsTeamIdFromUuid() {
        assertThat(BenutzerTestdaten.engineeringTeamId().value()).isEqualTo(BenutzerTestdaten.ENGINEERING_TEAM_UUID);
    }
}
