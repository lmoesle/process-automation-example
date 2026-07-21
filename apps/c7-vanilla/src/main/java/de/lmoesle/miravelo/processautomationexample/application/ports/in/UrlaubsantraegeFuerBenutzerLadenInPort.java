package de.lmoesle.miravelo.processautomationexample.application.ports.in;

import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import org.springframework.util.Assert;

import java.util.List;

public interface UrlaubsantraegeFuerBenutzerLadenInPort {

    List<Urlaubsantrag> ladeUrlaubsantraegeFuerBenutzer(UrlaubsantraegeFuerBenutzerLadenCommand command);

    record UrlaubsantraegeFuerBenutzerLadenCommand(BenutzerId benutzerId) {
        public UrlaubsantraegeFuerBenutzerLadenCommand {
            Assert.notNull(benutzerId, "benutzerId darf nicht null sein");
        }
    }
}
