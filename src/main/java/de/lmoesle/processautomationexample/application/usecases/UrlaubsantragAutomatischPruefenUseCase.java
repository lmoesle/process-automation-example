package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.UrlaubsantragAutomatischPruefenInPort;
import de.lmoesle.processautomationexample.application.ports.out.UrlaubsantraegeLadenOutPort;
import de.lmoesle.processautomationexample.application.ports.out.UrlaubsantragSpeichernOutPort;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
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

        urlaubsantrag.starteAutomatischePruefung();
        urlaubsantragSpeichernOutPort.speichere(urlaubsantrag);

        List<Urlaubsantrag> vertretungsUrlaubsantraege = urlaubsantrag.vertretung() == null
            ? List.of()
            : urlaubsantraegeLadenOutPort.findeAlleNachAntragstellerId(urlaubsantrag.vertretung().id());

        return urlaubsantrag.istAutomatischGueltigGegen(vertretungsUrlaubsantraege);
    }
}
