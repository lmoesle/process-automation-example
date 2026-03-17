package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.UrlaubsantragErstellenInPort;
import de.lmoesle.processautomationexample.application.ports.out.UrlaubsantragSpeichernOutPort;
import de.lmoesle.processautomationexample.application.ports.out.UrlaubsantragGenehmigungsprozessStartenOutPort;
import de.lmoesle.processautomationexample.application.ports.out.BenutzerRepositoryOutPort;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.ProzessinstanzId;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UrlaubsantragErstellenUseCase implements UrlaubsantragErstellenInPort {

    private final BenutzerRepositoryOutPort benutzerRepositoryOutPort;
    private final UrlaubsantragSpeichernOutPort urlaubsantragSpeichernOutPort;
    private final UrlaubsantragGenehmigungsprozessStartenOutPort genehmigungsprozessStartenOutPort;

    @Override
    public UrlaubsantragErstellenErgebnis erstelleUrlaubsantrag(UrlaubsantragErstellenCommand command) {
        Benutzer antragsteller = ladeBenutzer(command.antragstellerId(), "antragstellerId");
        Benutzer vertretung = command.vertretungId() == null
            ? null
            : ladeBenutzer(command.vertretungId(), "vertretungId");

        Urlaubsantrag urlaubsantrag = Urlaubsantrag.stelle(
            command.von(),
            command.bis(),
            antragsteller,
            vertretung
        );

        urlaubsantragSpeichernOutPort.speichere(urlaubsantrag);

        ProzessinstanzId prozessinstanzId = genehmigungsprozessStartenOutPort.starteGenehmigungsprozessFuer(urlaubsantrag);
        urlaubsantrag.markiereGenehmigungsprozessAlsGestartet(prozessinstanzId);
        urlaubsantrag = urlaubsantragSpeichernOutPort.speichere(urlaubsantrag);

        return new UrlaubsantragErstellenErgebnis(
            urlaubsantrag.id(),
            prozessinstanzId,
            urlaubsantrag.status(),
            urlaubsantrag.statusHistorie(),
            antragsteller,
            vertretung
        );
    }

    private Benutzer ladeBenutzer(BenutzerId benutzerId, String feldname) {
        return benutzerRepositoryOutPort.findeNachId(benutzerId)
            .orElseThrow(() -> new IllegalArgumentException(feldname + " verweist auf keinen vorhandenen Benutzer"));
    }
}
