package de.lmoesle.processautomationexample.application.ports.in;

import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.processautomationexample.domain.tasklist.UserTask;
import de.lmoesle.processautomationexample.domain.tasklist.UserTaskId;

import java.util.List;

public interface TaskAbfragenInPort {

    List<UserTask> getAllTasks(GetAllTasksCommand command);

    UserTask getTaskById(GetTaskByIdCommand command);

    record GetAllTasksCommand(BenutzerId benutzerId) {
    }

    record GetTaskByIdCommand(UserTaskId taskId, BenutzerId benutzerId) {
    }
}
