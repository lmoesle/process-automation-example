package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.GetAllTasksInPort;
import de.lmoesle.processautomationexample.application.ports.out.TasklistRepositoryOutPort;
import de.lmoesle.processautomationexample.domain.tasklist.UserTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAllTasksUseCase implements GetAllTasksInPort {

    private final TasklistRepositoryOutPort tasklistRepositoryOutPort;

    @Override
    public List<UserTask> getAllTasks() {
        return tasklistRepositoryOutPort.getAllTasks();
    }
}
