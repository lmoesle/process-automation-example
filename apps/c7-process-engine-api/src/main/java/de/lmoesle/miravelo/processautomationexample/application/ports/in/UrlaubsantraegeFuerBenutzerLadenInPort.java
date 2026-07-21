package de.lmoesle.miravelo.processautomationexample.application.ports.in;

import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;

import java.util.List;

public interface UrlaubsantraegeFuerBenutzerLadenInPort {

    List<Urlaubsantrag> ladeUrlaubsantraegeFuerBenutzer(UrlaubsantraegeFuerBenutzerLadenCommand command);

    record UrlaubsantraegeFuerBenutzerLadenCommand(BenutzerId benutzerId) {
    }
}
