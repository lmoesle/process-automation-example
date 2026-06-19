package de.lmoesle.processautomationexample.application.ports.out;

import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.processautomationexample.domain.benutzer.TeamId;

import java.util.List;
import java.util.Optional;

public interface BenutzerRepositoryOutPort {

    List<Benutzer> findeAlleAuswaehlbaren();

    List<Benutzer> sucheAuswaehlbareNachNameOderEmail(String suchbegriff);

    Optional<Benutzer> findeNachId(BenutzerId benutzerId);

    Optional<Benutzer> findeNachBenutzername(String benutzername);

    List<Benutzer> findeAlleLeitendenNachTeamId(TeamId teamId);
}
