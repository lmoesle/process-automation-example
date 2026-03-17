package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.TaskAbfragenInPort;
import de.lmoesle.processautomationexample.application.ports.out.TasklistRepositoryOutPort;
import de.lmoesle.processautomationexample.domain.tasklist.TaskNichtGefundenException;
import de.lmoesle.processautomationexample.domain.tasklist.UserTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskAbfragenUseCase implements TaskAbfragenInPort {

    private final TasklistRepositoryOutPort tasklistRepositoryOutPort;

    @Override
    public List<UserTask> getAllTasks(GetAllTasksCommand command) {
        Assert.notNull(command, "command darf nicht null sein");
        Assert.notNull(command.benutzerId(), "benutzerId darf nicht null sein");
        return tasklistRepositoryOutPort.getAllTasks(command.benutzerId());
    }

    @Override
    public UserTask getTaskById(GetTaskByIdCommand command) {
        Assert.notNull(command, "command darf nicht null sein");
        Assert.notNull(command.taskId(), "taskId darf nicht null sein");
        Assert.notNull(command.benutzerId(), "benutzerId darf nicht null sein");
        return tasklistRepositoryOutPort.getTaskById(command.taskId(), command.benutzerId())
            .orElseThrow(() -> new TaskNichtGefundenException(command.taskId()));
    }
}
