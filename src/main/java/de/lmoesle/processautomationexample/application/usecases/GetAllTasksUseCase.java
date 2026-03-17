package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.GetAllTasksInPort;
import de.lmoesle.processautomationexample.application.ports.out.TasklistRepositoryOutPort;
import de.lmoesle.processautomationexample.domain.tasklist.UserTask;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "dev.bpm-crafters.process-api.adapter.c7embedded", name = "enabled", havingValue = "true")
public class GetAllTasksUseCase implements GetAllTasksInPort {

    private final TasklistRepositoryOutPort tasklistRepositoryOutPort;

    @Override
    public List<UserTask> getAllTasks() {
        return tasklistRepositoryOutPort.getAllTasks();
    }
}
