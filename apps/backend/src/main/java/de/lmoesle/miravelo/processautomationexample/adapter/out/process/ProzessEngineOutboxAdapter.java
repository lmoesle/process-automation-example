package de.lmoesle.miravelo.processautomationexample.adapter.out.process;

import de.lmoesle.miravelo.processautomationexample.adapter.out.persistence.ProzessEngineOutboxAuftragJpaRepository;
import de.lmoesle.miravelo.processautomationexample.adapter.out.persistence.entities.ProzessEngineOutboxAuftragEntity;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.AssignTaskOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.CompleteTaskOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.StarteGenehmigungsprozessDirektOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.TaskBearbeitenDirektOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.UrlaubsantragProzessinstanzSpeichernOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.UrlaubsantraegeLadenOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.UrlaubsantragGenehmigungsprozessStartenOutPort;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.ProzessinstanzId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProzessEngineOutboxAdapter implements
    UrlaubsantragGenehmigungsprozessStartenOutPort,
    AssignTaskOutPort,
    CompleteTaskOutPort {

    private static final int MAXIMALE_VERSUCHE = 10;
    private static final int BATCH_SIZE = 20;
    private static final List<ProzessEngineOutboxAuftragStatus> WIEDERHOLBARE_STATUS = List.of(
        ProzessEngineOutboxAuftragStatus.OFFEN,
        ProzessEngineOutboxAuftragStatus.FEHLGESCHLAGEN
    );

    private final ProzessEngineOutboxAuftragJpaRepository outboxAuftragJpaRepository;
    private final StarteGenehmigungsprozessDirektOutPort starteGenehmigungsprozessDirektOutPort;
    private final TaskBearbeitenDirektOutPort taskBearbeitenDirektOutPort;
    private final UrlaubsantraegeLadenOutPort urlaubsantraegeLadenOutPort;
    private final UrlaubsantragProzessinstanzSpeichernOutPort urlaubsantragProzessinstanzSpeichernOutPort;

    @Override
    public void starteGenehmigungsprozessFuer(Urlaubsantrag urlaubsantrag, List<BenutzerId> teamLeadIds) {
        outboxAuftragJpaRepository.save(ProzessEngineOutboxMapper.starteGenehmigungsprozess(
            urlaubsantrag,
            teamLeadIds,
            Instant.now()
        ));
    }

    @Override
    public void assignTaskToUser(UserTaskId taskId, BenutzerId benutzerId) {
        outboxAuftragJpaRepository.save(ProzessEngineOutboxMapper.weiseTaskZu(taskId, benutzerId, Instant.now()));
    }

    @Override
    public void completeTask(UserTaskId taskId, BenutzerId benutzerId, boolean genehmigt) {
        outboxAuftragJpaRepository.save(ProzessEngineOutboxMapper.schliesseTaskAb(
            taskId,
            benutzerId,
            genehmigt,
            Instant.now()
        ));
    }

    @Scheduled(fixedDelayString = "${de.lmoesle.miravelo.processautomationexample.process-engine-outbox.fixed-delay-ms:5000}")
    @Transactional
    public void verarbeiteFaelligeAuftraege() {
        final List<ProzessEngineOutboxAuftragEntity> auftraege = outboxAuftragJpaRepository.findeFaelligeAuftraege(
            WIEDERHOLBARE_STATUS,
            Instant.now(),
            MAXIMALE_VERSUCHE,
            PageRequest.of(0, BATCH_SIZE)
        );

        auftraege.forEach(this::verarbeiteAuftrag);
    }

    private void verarbeiteAuftrag(ProzessEngineOutboxAuftragEntity auftrag) {
        final Instant versuchsZeitpunkt = Instant.now();
        auftrag.registriereVersuch(versuchsZeitpunkt);

        try {
            switch (auftrag.getTyp()) {
                case STARTE_GENEHMIGUNGSPROZESS -> starteGenehmigungsprozess(auftrag);
                case WEISE_TASK_ZU -> taskBearbeitenDirektOutPort.assignTaskToUser(
                    UserTaskId.of(auftrag.getTaskId()),
                    BenutzerId.of(auftrag.getBenutzerId())
                );
                case SCHLIESSE_TASK_AB -> taskBearbeitenDirektOutPort.completeTask(
                    UserTaskId.of(auftrag.getTaskId()),
                    BenutzerId.of(auftrag.getBenutzerId()),
                    Boolean.TRUE.equals(auftrag.getGenehmigt())
                );
            }
            auftrag.markiereErfolgreich(Instant.now());
            log.info("Prozess-Engine-Outbox-Auftrag erfolgreich verarbeitet: auftragId={}, typ={}", auftrag.getId(), auftrag.getTyp());
        } catch (ProzessEngineAuftragUnklarException exception) {
            final Instant fehlgeschlagenAm = Instant.now();
            if (istUnklareProzessStartAntwortWiederholbar(auftrag)) {
                auftrag.markiereFehlgeschlagen(
                    exception.getMessage(),
                    fehlgeschlagenAm,
                    berechneNaechstenVersuch(fehlgeschlagenAm, auftrag.getVersuche())
                );
                log.warn(
                    "Prozess-Engine-Outbox-Auftrag mit unklarem Prozessstart wird wiederholt: auftragId={}, typ={}, versuch={}",
                    auftrag.getId(),
                    auftrag.getTyp(),
                    auftrag.getVersuche(),
                    exception
                );
            } else {
                auftrag.markiereEndgueltigFehlgeschlagen(exception.getMessage(), fehlgeschlagenAm);
                log.error(
                    "Prozess-Engine-Outbox-Auftrag mit unklarem Engine-Zustand endgueltig fehlgeschlagen: auftragId={}, typ={}, versuch={}",
                    auftrag.getId(),
                    auftrag.getTyp(),
                    auftrag.getVersuche(),
                    exception
                );
            }
        } catch (RuntimeException exception) {
            final Instant fehlgeschlagenAm = Instant.now();
            if (auftrag.getVersuche() >= MAXIMALE_VERSUCHE) {
                auftrag.markiereEndgueltigFehlgeschlagen(exception.getMessage(), fehlgeschlagenAm);
            } else {
                auftrag.markiereFehlgeschlagen(
                    exception.getMessage(),
                    fehlgeschlagenAm,
                    berechneNaechstenVersuch(fehlgeschlagenAm, auftrag.getVersuche())
                );
            }
            log.warn(
                "Prozess-Engine-Outbox-Auftrag fehlgeschlagen: auftragId={}, typ={}, versuch={}",
                auftrag.getId(),
                auftrag.getTyp(),
                auftrag.getVersuche(),
                exception
            );
        }
    }

    private boolean istUnklareProzessStartAntwortWiederholbar(ProzessEngineOutboxAuftragEntity auftrag) {
        return auftrag.getTyp() == ProzessEngineOutboxAuftragTyp.STARTE_GENEHMIGUNGSPROZESS
            && auftrag.getVersuche() < MAXIMALE_VERSUCHE;
    }

    private void starteGenehmigungsprozess(ProzessEngineOutboxAuftragEntity auftrag) {
        final Urlaubsantrag urlaubsantrag = urlaubsantraegeLadenOutPort.findeNachId(
            UrlaubsantragId.of(auftrag.getUrlaubsantragId())
        ).orElseThrow(() -> new IllegalStateException(
            "Urlaubsantrag " + auftrag.getUrlaubsantragId() + " fuer Prozess-Engine-Outbox-Auftrag nicht gefunden"
        ));

        if (urlaubsantrag.prozessinstanzId() != null) {
            auftrag.setProzessinstanzId(urlaubsantrag.prozessinstanzId().value());
            return;
        }

        if (auftrag.getProzessinstanzId() == null) {
            final var prozessinstanzId = starteGenehmigungsprozessDirektOutPort.starteGenehmigungsprozess(
                urlaubsantrag.id(),
                ProzessEngineOutboxMapper.teamLeadIds(auftrag)
            );
            auftrag.setProzessinstanzId(prozessinstanzId.value());
        }

        final Urlaubsantrag aktualisierterUrlaubsantrag = urlaubsantraegeLadenOutPort.findeNachId(urlaubsantrag.id())
            .orElseThrow(() -> new IllegalStateException(
                "Urlaubsantrag " + auftrag.getUrlaubsantragId() + " fuer Prozess-Engine-Outbox-Auftrag nicht gefunden"
            ));
        if (aktualisierterUrlaubsantrag.prozessinstanzId() != null) {
            return;
        }

        final var prozessinstanzId = ProzessinstanzId.of(auftrag.getProzessinstanzId());
        aktualisierterUrlaubsantrag.markiereGenehmigungsprozessAlsGestartet(prozessinstanzId);
        urlaubsantragProzessinstanzSpeichernOutPort.speichereProzessinstanzId(
            aktualisierterUrlaubsantrag.id(),
            aktualisierterUrlaubsantrag.prozessinstanzId()
        );
    }

    private Instant berechneNaechstenVersuch(Instant fehlgeschlagenAm, int versuche) {
        final long verzogerungInSekunden = Math.min(300, 5L * (1L << Math.min(versuche - 1, 6)));
        return fehlgeschlagenAm.plusSeconds(verzogerungInSekunden);
    }
}
