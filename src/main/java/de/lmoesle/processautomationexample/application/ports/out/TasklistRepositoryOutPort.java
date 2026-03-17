package de.lmoesle.processautomationexample.application.ports.out;

import de.lmoesle.processautomationexample.domain.tasklist.UserTask;
import de.lmoesle.processautomationexample.domain.tasklist.UserTaskId;

import java.util.List;
import java.util.Optional;

public interface TasklistRepositoryOutPort {

    List<UserTask> getAllTasks();

    Optional<UserTask> getTaskById(UserTaskId taskId);
}
