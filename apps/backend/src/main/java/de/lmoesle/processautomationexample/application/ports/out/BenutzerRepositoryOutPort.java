package de.lmoesle.processautomationexample.application.ports.out;

import de.lmoesle.processautomationexample.domain.benutzer.TeamId;
import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;

import java.util.List;
import java.util.Optional;

public interface BenutzerRepositoryOutPort {

    Optional<Benutzer> findeNachId(BenutzerId benutzerId);

    List<Benutzer> findeAlleLeitendenNachTeamId(TeamId teamId);
}
