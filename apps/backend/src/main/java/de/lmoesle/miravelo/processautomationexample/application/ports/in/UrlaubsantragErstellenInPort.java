package de.lmoesle.miravelo.processautomationexample.application.ports.in;

import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragStatus;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragStatusHistorieneintrag;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;

import java.time.LocalDate;
import java.util.List;

public interface UrlaubsantragErstellenInPort {

    UrlaubsantragErstellenErgebnis erstelleUrlaubsantrag(UrlaubsantragErstellenCommand command);

    record UrlaubsantragErstellenCommand(
        LocalDate von,
        LocalDate bis,
        BenutzerId antragstellerId,
        BenutzerId vertretungId
    ) {
    }

    record UrlaubsantragErstellenErgebnis(
        UrlaubsantragId urlaubsantragId,
        UrlaubsantragStatus status,
        List<UrlaubsantragStatusHistorieneintrag> statusHistorie,
        Benutzer antragsteller,
        Benutzer vertretung
    ) {
        public List<UrlaubsantragStatusHistorieneintrag> statushistorie() {
            return statusHistorie;
        }
    }
}
