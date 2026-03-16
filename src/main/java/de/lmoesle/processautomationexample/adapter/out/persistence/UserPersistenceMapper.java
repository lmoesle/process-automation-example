package de.lmoesle.processautomationexample.adapter.out.persistence;

import de.lmoesle.processautomationexample.domain.user.User;
import de.lmoesle.processautomationexample.domain.user.UserId;
import de.lmoesle.processautomationexample.domain.user.Team;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class UserPersistenceMapper {

    private UserPersistenceMapper() {
    }

    public static User toDomain(UserEntity userEntity) {
        Set<TeamMembershipEntity> teamMemberships = userEntity.getTeamMemberships() == null
            ? Set.of()
            : userEntity.getTeamMemberships();

        List<Team> teams = teamMemberships.stream()
            .map(membership -> new Team(membership.getTeam().getName(), membership.getRole()))
            .sorted(Comparator.comparing(Team::name))
            .toList();

        return User.reconstitute(
            UserId.of(userEntity.getId()),
            userEntity.getName(),
            userEntity.getEmail(),
            teams
        );
    }
}
