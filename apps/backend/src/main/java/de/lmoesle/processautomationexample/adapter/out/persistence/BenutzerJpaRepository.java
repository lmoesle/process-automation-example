package de.lmoesle.processautomationexample.adapter.out.persistence;

import de.lmoesle.processautomationexample.adapter.out.persistence.entities.BenutzerEntity;
import de.lmoesle.processautomationexample.domain.benutzer.TeamRolle;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BenutzerJpaRepository extends JpaRepository<BenutzerEntity, UUID> {

    @EntityGraph(attributePaths = {"teamMitgliedschaften", "teamMitgliedschaften.team"})
    List<BenutzerEntity> findDistinctByBenutzernameIn(Collection<String> benutzernamen);

    @EntityGraph(attributePaths = {"teamMitgliedschaften", "teamMitgliedschaften.team"})
    @Query("""
        select distinct benutzer
        from BenutzerEntity benutzer
        where benutzer.benutzername in :benutzernamen
          and (
            lower(benutzer.name) like lower(concat('%', :suchbegriff, '%'))
            or lower(benutzer.email) like lower(concat('%', :suchbegriff, '%'))
          )
        """)
    List<BenutzerEntity> findAuswaehlbareByBenutzernamenAndNameOrEmailContainingIgnoreCase(
        @Param("benutzernamen") Collection<String> benutzernamen,
        @Param("suchbegriff") String suchbegriff
    );

    @Override
    @EntityGraph(attributePaths = {"teamMitgliedschaften", "teamMitgliedschaften.team"})
    Optional<BenutzerEntity> findById(UUID benutzerId);

    @EntityGraph(attributePaths = {"teamMitgliedschaften", "teamMitgliedschaften.team"})
    Optional<BenutzerEntity> findByBenutzername(String benutzername);

    List<BenutzerEntity> findDistinctByTeamMitgliedschaftenIdTeamIdAndTeamMitgliedschaftenRolle(UUID teamId, TeamRolle rolle);

    @EntityGraph(attributePaths = {"teamMitgliedschaften", "teamMitgliedschaften.team"})
    List<BenutzerEntity> findDistinctByIdIn(Collection<UUID> benutzerIds);
}
