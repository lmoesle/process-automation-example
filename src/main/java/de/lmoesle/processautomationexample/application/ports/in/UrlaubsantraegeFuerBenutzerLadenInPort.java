package de.lmoesle.processautomationexample.application.ports.in;

import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;

import java.util.List;

public interface UrlaubsantraegeFuerBenutzerLadenInPort {

    List<Urlaubsantrag> ladeUrlaubsantraegeFuerBenutzer(UrlaubsantraegeFuerBenutzerLadenCommand command);

    record UrlaubsantraegeFuerBenutzerLadenCommand(BenutzerId benutzerId) {
    }
}
