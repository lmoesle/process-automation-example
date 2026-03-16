package de.lmoesle.processautomationexample.adapter.out.persistence;

import de.lmoesle.processautomationexample.domain.user.TeamRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

    @Override
    @EntityGraph(attributePaths = {"teamMemberships", "teamMemberships.team"})
    Optional<UserEntity> findById(UUID userId);

    List<UserEntity> findDistinctByTeamMembershipsIdTeamIdAndTeamMembershipsRole(UUID teamId, TeamRole role);

    @EntityGraph(attributePaths = {"teamMemberships", "teamMemberships.team"})
    List<UserEntity> findDistinctByIdIn(Collection<UUID> userIds);
}
