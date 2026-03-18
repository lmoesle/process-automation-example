package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.BenutzeraufgabeMirZuweisenInPort;
import de.lmoesle.processautomationexample.application.ports.out.TasklistRepositoryOutPort;
import de.lmoesle.processautomationexample.domain.tasklist.TaskNichtGefundenException;
import de.lmoesle.processautomationexample.domain.tasklist.TaskZugriffVerweigertException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@RequiredArgsConstructor
public class BenutzeraufgabeMirZuweisenUseCase implements BenutzeraufgabeMirZuweisenInPort {

    private final TasklistRepositoryOutPort tasklistRepositoryOutPort;

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

        tasklistRepositoryOutPort.assignTaskToUser(command.taskId(), command.benutzerId());
    }
}
