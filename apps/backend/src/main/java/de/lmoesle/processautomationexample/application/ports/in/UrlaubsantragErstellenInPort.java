package de.lmoesle.processautomationexample.application.ports.in;

import de.lmoesle.processautomationexample.domain.urlaubsantrag.ProzessinstanzId;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragId;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragStatus;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragStatusHistorieneintrag;
import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;

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
        ProzessinstanzId prozessinstanzId,
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
