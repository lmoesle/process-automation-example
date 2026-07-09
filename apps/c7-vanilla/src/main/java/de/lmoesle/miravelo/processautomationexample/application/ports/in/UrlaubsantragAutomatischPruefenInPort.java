package de.lmoesle.miravelo.processautomationexample.application.ports.in;

import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragId;

public interface UrlaubsantragAutomatischPruefenInPort {

    boolean pruefeUrlaubsantragAutomatisch(UrlaubsantragAutomatischPruefenCommand command);

    record UrlaubsantragAutomatischPruefenCommand(
        UrlaubsantragId urlaubsantragId
    ) {
    }
}
