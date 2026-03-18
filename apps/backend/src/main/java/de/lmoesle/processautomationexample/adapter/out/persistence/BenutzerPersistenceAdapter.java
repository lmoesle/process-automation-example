package de.lmoesle.processautomationexample.adapter.out.persistence;

import de.lmoesle.processautomationexample.adapter.out.persistence.entities.BenutzerEntity;
import de.lmoesle.processautomationexample.application.ports.out.BenutzerRepositoryOutPort;
import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.processautomationexample.domain.benutzer.TeamId;
import de.lmoesle.processautomationexample.domain.benutzer.TeamRolle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BenutzerPersistenceAdapter implements BenutzerRepositoryOutPort {

    private final BenutzerJpaRepository benutzerJpaRepository;

    @Override
    public Optional<Benutzer> findeNachId(BenutzerId benutzerId) {
        Assert.notNull(benutzerId, "benutzerId darf nicht null sein");
        return benutzerJpaRepository.findById(benutzerId.value())
            .map(BenutzerPersistenceMapper::toDomain);
    }

    @Override
    public List<Benutzer> findeAlleLeitendenNachTeamId(TeamId teamId) {
        Assert.notNull(teamId, "teamId darf nicht null sein");

        List<UUID> leitendenIds = benutzerJpaRepository.findDistinctByTeamMitgliedschaftenIdTeamIdAndTeamMitgliedschaftenRolle(
            teamId.value(),
            TeamRolle.LEITUNG
        ).stream()
            .map(BenutzerEntity::getId)
            .distinct()
            .toList();

        if (leitendenIds.isEmpty()) {
            return List.of();
        }

        return benutzerJpaRepository.findDistinctByIdIn(leitendenIds).stream()
            .map(BenutzerPersistenceMapper::toDomain)
            .sorted(Comparator.comparing(Benutzer::name))
            .toList();
    }
}
