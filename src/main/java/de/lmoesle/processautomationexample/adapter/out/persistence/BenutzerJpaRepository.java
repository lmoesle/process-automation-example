package de.lmoesle.processautomationexample.adapter.out.persistence;

import de.lmoesle.processautomationexample.domain.benutzer.TeamRolle;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BenutzerJpaRepository extends JpaRepository<BenutzerEntity, UUID> {

    @Override
    @EntityGraph(attributePaths = {"teamMitgliedschaften", "teamMitgliedschaften.team"})
    Optional<BenutzerEntity> findById(UUID benutzerId);

    List<BenutzerEntity> findDistinctByTeamMitgliedschaftenIdTeamIdAndTeamMitgliedschaftenRolle(UUID teamId, TeamRolle rolle);

    @EntityGraph(attributePaths = {"teamMitgliedschaften", "teamMitgliedschaften.team"})
    List<BenutzerEntity> findDistinctByIdIn(Collection<UUID> benutzerIds);
}
