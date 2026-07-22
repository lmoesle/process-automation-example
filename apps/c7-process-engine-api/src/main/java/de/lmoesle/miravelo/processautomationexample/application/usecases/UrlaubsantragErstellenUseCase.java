package de.lmoesle.miravelo.processautomationexample.application.usecases;

import de.lmoesle.miravelo.processautomationexample.application.ports.in.UrlaubsantragErstellenInPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.UrlaubsantragSpeichernOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.UrlaubsantragGenehmigungsprozessStartenOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.BenutzerRepositoryOutPort;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.Team;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UrlaubsantragErstellenUseCase implements UrlaubsantragErstellenInPort {

    private final BenutzerRepositoryOutPort benutzerRepositoryOutPort;
    private final UrlaubsantragSpeichernOutPort urlaubsantragSpeichernOutPort;
    private final UrlaubsantragGenehmigungsprozessStartenOutPort genehmigungsprozessStartenOutPort;

    @Override
    public UrlaubsantragErstellenErgebnis erstelleUrlaubsantrag(UrlaubsantragErstellenCommand command) {
        final Benutzer antragsteller = ladeBenutzer(command.antragstellerId(), "antragstellerId");
        final Benutzer vertretung = command.vertretungId() == null
            ? null
            : ladeBenutzer(command.vertretungId(), "vertretungId");

        final Urlaubsantrag urlaubsantrag = Urlaubsantrag.stelle(
            command.von(),
            command.bis(),
            antragsteller,
            vertretung
        );

        urlaubsantragSpeichernOutPort.speichere(urlaubsantrag);

        genehmigungsprozessStartenOutPort.starteGenehmigungsprozessFuer(
            urlaubsantrag,
            ermittleTeamLeadIds(antragsteller)
        );

        log.info(
            "Urlaubsantrag erfolgreich erstellt und Genehmigungsprozess-Start beauftragt: urlaubsantragId={}, status={}",
            urlaubsantrag.id().value(),
            urlaubsantrag.status()
        );

        return new UrlaubsantragErstellenErgebnis(
            urlaubsantrag.id(),
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

    private List<BenutzerId> ermittleTeamLeadIds(Benutzer antragsteller) {
        return antragsteller.teams().stream()
            .map(Team::id)
            .map(benutzerRepositoryOutPort::findeAlleLeitendenNachTeamId)
            .flatMap(List::stream)
            .filter(benutzer -> !benutzer.id().equals(antragsteller.id()))
            .map(Benutzer::id)
            .distinct()
            .toList();
    }
}
