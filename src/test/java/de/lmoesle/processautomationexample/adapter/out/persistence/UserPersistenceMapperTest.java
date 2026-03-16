package de.lmoesle.processautomationexample.adapter.out.persistence;

import de.lmoesle.processautomationexample.domain.user.TeamRole;
import de.lmoesle.processautomationexample.domain.user.User;
import de.lmoesle.processautomationexample.domain.user.UserTestData;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import static org.assertj.core.api.Assertions.assertThat;

class UserPersistenceMapperTest {

    @Test
    void mapsUserEntityWithTeamMembershipsToDomain() {
        TeamEntity engineeringTeam = new TeamEntity(
            UserTestData.ENGINEERING_TEAM_UUID,
            UserTestData.ENGINEERING_TEAM
        );
        TeamEntity platformTeam = new TeamEntity(
            UserTestData.PLATFORM_TEAM_UUID,
            UserTestData.PLATFORM_TEAM
        );
        UserEntity userEntity = new UserEntity(
            UserTestData.ADA_UUID,
            "Ada Lovelace",
            "ada.lovelace@example.com",
            new LinkedHashSet<>()
        );

        userEntity.getTeamMemberships().add(new TeamMembershipEntity(
            new TeamMembershipId(engineeringTeam.getId(), userEntity.getId()),
            engineeringTeam,
            userEntity,
            TeamRole.LEAD
        ));
        userEntity.getTeamMemberships().add(new TeamMembershipEntity(
            new TeamMembershipId(platformTeam.getId(), userEntity.getId()),
            platformTeam,
            userEntity,
            TeamRole.USER
        ));

        User user = UserPersistenceMapper.toDomain(userEntity);

        assertThat(user.id()).isEqualTo(UserTestData.adaId());
        assertThat(user.name()).isEqualTo("Ada Lovelace");
        assertThat(user.email()).isEqualTo("ada.lovelace@example.com");
        assertThat(user.teams()).containsExactly(
            UserTestData.engineeringLeadTeam(),
            UserTestData.platformUserTeam()
        );
    }
}
