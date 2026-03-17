package de.lmoesle.processautomationexample.application.ports.in;

import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragId;

public interface UrlaubsantragAutomatischPruefenInPort {

    boolean pruefeUrlaubsantragAutomatisch(UrlaubsantragAutomatischPruefenCommand command);

    record UrlaubsantragAutomatischPruefenCommand(
        UrlaubsantragId urlaubsantragId
    ) {
    }
}
