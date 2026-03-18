package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.UrlaubsantragAutomatischPruefenInPort;
import de.lmoesle.processautomationexample.application.ports.out.UrlaubsantraegeLadenOutPort;
import de.lmoesle.processautomationexample.application.ports.out.UrlaubsantragSpeichernOutPort;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UrlaubsantragAutomatischPruefenUseCase implements UrlaubsantragAutomatischPruefenInPort {

    private final UrlaubsantraegeLadenOutPort urlaubsantraegeLadenOutPort;
    private final UrlaubsantragSpeichernOutPort urlaubsantragSpeichernOutPort;

    @Override
    public boolean pruefeUrlaubsantragAutomatisch(UrlaubsantragAutomatischPruefenCommand command) {
        Assert.notNull(command, "command darf nicht null sein");
        Assert.notNull(command.urlaubsantragId(), "urlaubsantragId darf nicht null sein");

        Urlaubsantrag urlaubsantrag = urlaubsantraegeLadenOutPort.findeNachId(command.urlaubsantragId())
            .orElseThrow(() -> new IllegalArgumentException("urlaubsantragId verweist auf keinen vorhandenen Urlaubsantrag"));

        if (urlaubsantrag.status() == UrlaubsantragStatus.VORGESETZTEN_PRUEFUNG) {
            return true;
        }
        if (urlaubsantrag.status() == UrlaubsantragStatus.ABGELEHNT) {
            return false;
        }

        urlaubsantrag.starteAutomatischePruefung();

        List<Urlaubsantrag> vertretungsUrlaubsantraege = urlaubsantrag.vertretung() == null
            ? List.of()
            : urlaubsantraegeLadenOutPort.findeAlleNachAntragstellerId(urlaubsantrag.vertretung().id());

        boolean gueltig = urlaubsantrag.istAutomatischGueltigGegen(vertretungsUrlaubsantraege);
        urlaubsantrag.schliesseAutomatischePruefungAb(gueltig);
        urlaubsantragSpeichernOutPort.speichere(urlaubsantrag);

        return gueltig;
    }
}
