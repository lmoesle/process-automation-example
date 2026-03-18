package de.lmoesle.processautomationexample.adapter.out.persistence;

import de.lmoesle.processautomationexample.adapter.out.persistence.entities.BenutzerEntity;
import de.lmoesle.processautomationexample.adapter.out.persistence.entities.TeamEntity;
import de.lmoesle.processautomationexample.adapter.out.persistence.entities.TeamMitgliedschaftEntity;
import de.lmoesle.processautomationexample.adapter.out.persistence.entities.TeamMitgliedschaftId;
import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.processautomationexample.domain.benutzer.TeamRolle;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;

import static org.assertj.core.api.Assertions.assertThat;

class BenutzerPersistenceMapperTest {

    @Test
    void mapsBenutzerEntityWithTeamMembershipsToDomain() {
        TeamEntity engineeringTeam = new TeamEntity(
            BenutzerTestdaten.ENGINEERING_TEAM_UUID,
            BenutzerTestdaten.ENGINEERING_TEAM
        );
        TeamEntity platformTeam = new TeamEntity(
            BenutzerTestdaten.PLATFORM_TEAM_UUID,
            BenutzerTestdaten.PLATFORM_TEAM
        );
        BenutzerEntity benutzerEntity = new BenutzerEntity(
            BenutzerTestdaten.ADA_UUID,
            "Ada Lovelace",
            "ada.lovelace@example.com",
            new LinkedHashSet<>()
        );

        benutzerEntity.getTeamMitgliedschaften().add(new TeamMitgliedschaftEntity(
            new TeamMitgliedschaftId(engineeringTeam.getId(), benutzerEntity.getId()),
            engineeringTeam,
            benutzerEntity,
            TeamRolle.LEITUNG
        ));
        benutzerEntity.getTeamMitgliedschaften().add(new TeamMitgliedschaftEntity(
            new TeamMitgliedschaftId(platformTeam.getId(), benutzerEntity.getId()),
            platformTeam,
            benutzerEntity,
            TeamRolle.MITGLIED
        ));

        Benutzer benutzer = BenutzerPersistenceMapper.toDomain(benutzerEntity);

        assertThat(benutzer.id()).isEqualTo(BenutzerTestdaten.adaId());
        assertThat(benutzer.name()).isEqualTo("Ada Lovelace");
        assertThat(benutzer.email()).isEqualTo("ada.lovelace@example.com");
        assertThat(benutzer.teams()).extracting(team -> team.id().value()).containsExactly(
            BenutzerTestdaten.ENGINEERING_TEAM_UUID,
            BenutzerTestdaten.PLATFORM_TEAM_UUID
        );
        assertThat(benutzer.teams()).containsExactly(
            BenutzerTestdaten.engineeringLeadTeam(),
            BenutzerTestdaten.platformUserTeam()
        );
    }
}
