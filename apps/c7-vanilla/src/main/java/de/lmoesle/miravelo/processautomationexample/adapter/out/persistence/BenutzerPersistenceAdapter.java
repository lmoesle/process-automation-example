package de.lmoesle.miravelo.processautomationexample.adapter.out.persistence;

import de.lmoesle.miravelo.processautomationexample.adapter.out.persistence.entities.BenutzerEntity;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.BenutzerRepositoryOutPort;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.TeamId;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.TeamRolle;
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

    // Demo-only allowlist: the UI must only offer users that can log in via the sample Basic Auth setup.
    private static final List<String> DEMO_BENUTZERNAMEN = List.of("john", "jane", "max");

    private final BenutzerJpaRepository benutzerJpaRepository;

    @Override
    public List<Benutzer> findeAlleAuswaehlbaren() {
        return benutzerJpaRepository.findDistinctByBenutzernameIn(DEMO_BENUTZERNAMEN).stream()
            .map(BenutzerPersistenceMapper::toDomain)
            .sorted(Comparator.comparing(Benutzer::name))
            .toList();
    }

    @Override
    public List<Benutzer> sucheAuswaehlbareNachNameOderEmail(String suchbegriff) {
        Assert.hasText(suchbegriff, "suchbegriff darf nicht leer sein");
        return benutzerJpaRepository.findAuswaehlbareByBenutzernamenAndNameOrEmailContainingIgnoreCase(
                DEMO_BENUTZERNAMEN,
                suchbegriff
            )
            .stream()
            .map(BenutzerPersistenceMapper::toDomain)
            .sorted(Comparator.comparing(Benutzer::name))
            .toList();
    }

    @Override
    public Optional<Benutzer> findeNachId(BenutzerId benutzerId) {
        Assert.notNull(benutzerId, "benutzerId darf nicht null sein");
        return benutzerJpaRepository.findById(benutzerId.value())
            .map(BenutzerPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Benutzer> findeNachBenutzername(String benutzername) {
        Assert.hasText(benutzername, "benutzername darf nicht leer sein");
        return benutzerJpaRepository.findByBenutzername(benutzername)
            .map(BenutzerPersistenceMapper::toDomain);
    }

    @Override
    public List<Benutzer> findeAlleLeitendenNachTeamId(TeamId teamId) {
        Assert.notNull(teamId, "teamId darf nicht null sein");

        final List<UUID> leitendenIds = benutzerJpaRepository.findDistinctByTeamMitgliedschaftenIdTeamIdAndTeamMitgliedschaftenRolle(
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
