package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.GenehmigungVomVorgesetztenInPort;
import de.lmoesle.processautomationexample.application.ports.out.CompleteTaskOutPort;
import de.lmoesle.processautomationexample.application.ports.out.SendeBenachrichtigungOutPort;
import de.lmoesle.processautomationexample.application.ports.out.TasklistRepositoryOutPort;
import de.lmoesle.processautomationexample.application.ports.out.UrlaubsantragSpeichernOutPort;
import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.processautomationexample.domain.tasklist.TaskNichtGefundenException;
import de.lmoesle.processautomationexample.domain.tasklist.TaskZugriffVerweigertException;
import de.lmoesle.processautomationexample.domain.tasklist.UserTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class GenehmigungVomVorgesetztenUseCase implements GenehmigungVomVorgesetztenInPort {

    private final TasklistRepositoryOutPort tasklistRepositoryOutPort;
    private final CompleteTaskOutPort completeTaskOutPort;
    private final UrlaubsantragSpeichernOutPort urlaubsantragSpeichernOutPort;
    private final SendeBenachrichtigungOutPort sendeBenachrichtigungOutPort;

    @Override
    public void entscheideGenehmigungVomVorgesetzten(GenehmigungVomVorgesetztenCommand command) {
        Assert.notNull(command, "command darf nicht null sein");
        Assert.notNull(command.taskId(), "taskId darf nicht null sein");
        Assert.notNull(command.benutzerId(), "benutzerId darf nicht null sein");

        var task = tasklistRepositoryOutPort.getTaskById(command.taskId())
            .orElseThrow(() -> new TaskNichtGefundenException(command.taskId()));

        if (!task.istSichtbarFuer(command.benutzerId())) {
            throw new TaskZugriffVerweigertException(command.taskId());
        }

        var urlaubsantrag = task.urlaubsantrag();
        if (urlaubsantrag == null) {
            throw new IllegalStateException("taskId verweist auf keinen zugeordneten Urlaubsantrag");
        }

        urlaubsantrag.weiseVorgesetztenZu(ermittleEntscheidendenVorgesetzten(task, command.benutzerId()));

        if (command.genehmigt()) {
            urlaubsantrag.genehmigeDurchVorgesetzten(command.kommentar());
        } else {
            urlaubsantrag.lehneDurchVorgesetztenAb(command.kommentar());
        }

        urlaubsantragSpeichernOutPort.speichere(urlaubsantrag);
        completeTaskOutPort.completeTask(command.taskId(), command.benutzerId(), command.genehmigt());
        sendeBenachrichtigungOutPort.sendeBenachrichtigung(urlaubsantrag);
        log.info(
            "Vorgesetztenentscheidung erfolgreich abgeschlossen: urlaubsantragId={}, genehmigt={}, status={}",
            urlaubsantrag.id().value(),
            command.genehmigt(),
            urlaubsantrag.status()
        );
    }

    private Benutzer ermittleEntscheidendenVorgesetzten(UserTask task, BenutzerId benutzerId) {
        if (task.bearbeiter() != null && task.bearbeiter().id().equals(benutzerId)) {
            return task.bearbeiter();
        }

        return task.candidateUsers().stream()
            .filter(benutzer -> benutzer.id().equals(benutzerId))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "Aufgabe " + task.id().value() + " enthaelt keinen Kandidaten fuer Benutzer " + benutzerId.value()
            ));
    }
}
