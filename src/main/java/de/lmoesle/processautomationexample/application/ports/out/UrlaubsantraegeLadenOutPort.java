package de.lmoesle.processautomationexample.application.ports.out;

import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragId;

import java.util.List;
import java.util.Optional;

public interface UrlaubsantraegeLadenOutPort {

    Optional<Urlaubsantrag> findeNachId(UrlaubsantragId urlaubsantragId);

    List<Urlaubsantrag> findeAlleNachAntragstellerId(BenutzerId antragstellerId);
}
