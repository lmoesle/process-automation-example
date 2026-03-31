package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.BenutzeraufgabeMirZuweisenInPort;
import de.lmoesle.processautomationexample.application.ports.out.AssignTaskOutPort;
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
public class BenutzeraufgabeMirZuweisenUseCase implements BenutzeraufgabeMirZuweisenInPort {

    private final TasklistRepositoryOutPort tasklistRepositoryOutPort;
    private final AssignTaskOutPort assignTaskOutPort;
    private final UrlaubsantragSpeichernOutPort urlaubsantragSpeichernOutPort;

    @Override
    public void weiseBenutzeraufgabeMirZu(WeiseBenutzeraufgabeMirZuCommand command) {
        Assert.notNull(command, "command darf nicht null sein");
        Assert.notNull(command.taskId(), "taskId darf nicht null sein");
        Assert.notNull(command.benutzerId(), "benutzerId darf nicht null sein");

        var task = tasklistRepositoryOutPort.getTaskById(command.taskId())
            .orElseThrow(() -> new TaskNichtGefundenException(command.taskId()));

        if (!task.istCandidateUser(command.benutzerId())) {
            throw new TaskZugriffVerweigertException(command.taskId());
        }

        assignTaskOutPort.assignTaskToUser(command.taskId(), command.benutzerId());

        if (task.urlaubsantrag() != null) {
            task.urlaubsantrag().weiseVorgesetztenZu(ermittleVorgesetzten(task, command.benutzerId()));
            urlaubsantragSpeichernOutPort.speichere(task.urlaubsantrag());
        }

        log.info(
            "Benutzeraufgabe erfolgreich zugewiesen: taskId={}, benutzerId={}, urlaubsantragId={}",
            command.taskId().value(),
            command.benutzerId().value(),
            task.urlaubsantrag() == null ? null : task.urlaubsantrag().id().value()
        );
    }

    private Benutzer ermittleVorgesetzten(UserTask task, BenutzerId vorgesetztenId) {
        return task.candidateUsers().stream()
            .filter(benutzer -> benutzer.id().equals(vorgesetztenId))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "Aufgabe " + task.id().value() + " enthaelt keinen Kandidaten fuer Benutzer " + vorgesetztenId.value()
            ));
    }
}
