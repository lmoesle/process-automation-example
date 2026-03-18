package de.lmoesle.processautomationexample.domain.tasklist;

import de.lmoesle.processautomationexample.domain.benutzer.BenutzerTestdaten;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTaskTest {

    @Test
    void identifiesCandidateUsers() {
        assertThat(UserTaskTestdaten.userTask().istCandidateUser(BenutzerTestdaten.adaId())).isTrue();
        assertThat(UserTaskTestdaten.userTask().istCandidateUser(BenutzerTestdaten.carlaId())).isTrue();
    }

    @Test
    void returnsFalseForNonCandidateUsers() {
        assertThat(UserTaskTestdaten.secondUserTask().istCandidateUser(BenutzerTestdaten.adaId())).isFalse();
    }

    @Test
    void rejectsNullCandidateUserCheck() {
        assertThatThrownBy(() -> UserTaskTestdaten.userTask().istCandidateUser(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("benutzerId darf nicht null sein");
    }
}
