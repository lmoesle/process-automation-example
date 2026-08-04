package de.lmoesle.miravelo.processautomationexample.application.ports.in;

import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragId;
import org.springframework.util.Assert;

public interface UrlaubsantragAutomatischPruefenInPort {

    boolean pruefeUrlaubsantragAutomatisch(UrlaubsantragAutomatischPruefenCommand command);

    record UrlaubsantragAutomatischPruefenCommand(
        UrlaubsantragId urlaubsantragId
    ) {
        public UrlaubsantragAutomatischPruefenCommand {
            Assert.notNull(urlaubsantragId, "urlaubsantragId darf nicht null sein");
        }
    }
}
