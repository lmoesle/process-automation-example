package de.lmoesle.processautomationexample.adapter.out.persistence;

import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.processautomationexample.domain.benutzer.Team;
import de.lmoesle.processautomationexample.domain.benutzer.TeamId;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class BenutzerPersistenceMapper {

    private BenutzerPersistenceMapper() {
    }

    public static Benutzer toDomain(BenutzerEntity benutzerEntity) {
        Set<TeamMitgliedschaftEntity> teamMitgliedschaften = benutzerEntity.getTeamMitgliedschaften() == null
            ? Set.of()
            : benutzerEntity.getTeamMitgliedschaften();

        List<Team> teams = teamMitgliedschaften.stream()
            .map(mitgliedschaft -> new Team(
                TeamId.of(mitgliedschaft.getTeam().getId()),
                mitgliedschaft.getTeam().getName(),
                mitgliedschaft.getRolle()
            ))
            .sorted(Comparator.comparing(Team::name))
            .toList();

        return Benutzer.rekonstituiere(
            BenutzerId.of(benutzerEntity.getId()),
            benutzerEntity.getName(),
            benutzerEntity.getEmail(),
            teams
        );
    }
}
