package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.GetTaskByIdInPort;
import de.lmoesle.processautomationexample.application.ports.out.TasklistRepositoryOutPort;
import de.lmoesle.processautomationexample.domain.tasklist.TaskNichtGefundenException;
import de.lmoesle.processautomationexample.domain.tasklist.UserTask;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "dev.bpm-crafters.process-api.adapter.c7embedded", name = "enabled", havingValue = "true")
public class GetTaskByIdUseCase implements GetTaskByIdInPort {

    private final TasklistRepositoryOutPort tasklistRepositoryOutPort;

    @Override
    public UserTask getTaskById(GetTaskByIdCommand command) {
        Assert.notNull(command, "command darf nicht null sein");
        Assert.notNull(command.taskId(), "taskId darf nicht null sein");
        return tasklistRepositoryOutPort.getTaskById(command.taskId())
            .orElseThrow(() -> new TaskNichtGefundenException(command.taskId()));
    }
}
