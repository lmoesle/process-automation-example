package de.lmoesle.processautomationexample.domain.user;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    void reconstitutesUserWithTeams() {
        User user = User.reconstitute(
            UserTestData.adaId(),
            "Ada Lovelace",
            "ada.lovelace@example.com",
            List.of(UserTestData.engineeringLeadTeam(), UserTestData.platformUserTeam())
        );

        assertThat(user.id()).isEqualTo(UserTestData.adaId());
        assertThat(user.name()).isEqualTo("Ada Lovelace");
        assertThat(user.email()).isEqualTo("ada.lovelace@example.com");
        assertThat(user.teams()).containsExactly(
            UserTestData.engineeringLeadTeam(),
            UserTestData.platformUserTeam()
        );
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> User.reconstitute(
            UserTestData.adaId(),
            " ",
            "ada.lovelace@example.com",
            List.of(UserTestData.engineeringLeadTeam())
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("name must not be blank");
    }

    @Test
    void rejectsBlankTeamName() {
        assertThatThrownBy(() -> new Team(" ", TeamRole.LEAD))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("name must not be blank");
    }

    @Test
    void createsTeamIdFromUuid() {
        assertThat(UserTestData.engineeringTeamId().value()).isEqualTo(UserTestData.ENGINEERING_TEAM_UUID);
    }
}
