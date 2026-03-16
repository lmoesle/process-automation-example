package de.lmoesle.processautomationexample.adapter.out.persistence;

import de.lmoesle.processautomationexample.application.ports.out.UserRepositoryOutPort;
import de.lmoesle.processautomationexample.domain.user.TeamId;
import de.lmoesle.processautomationexample.domain.user.TeamRole;
import de.lmoesle.processautomationexample.domain.user.User;
import de.lmoesle.processautomationexample.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryOutPort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> findById(UserId userId) {
        Assert.notNull(userId, "userId must not be null");
        return userJpaRepository.findById(userId.value())
            .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public List<User> findAllLeadsByTeamId(TeamId teamId) {
        Assert.notNull(teamId, "teamId must not be null");

        List<UUID> leadUserIds = userJpaRepository.findDistinctByTeamMembershipsIdTeamIdAndTeamMembershipsRole(
            teamId.value(),
            TeamRole.LEAD
        ).stream()
            .map(UserEntity::getId)
            .distinct()
            .toList();

        if (leadUserIds.isEmpty()) {
            return List.of();
        }

        return userJpaRepository.findDistinctByIdIn(leadUserIds).stream()
            .map(UserPersistenceMapper::toDomain)
            .sorted(Comparator.comparing(User::name))
            .toList();
    }
}
