package de.lmoesle.processautomationexample.application.ports.in;

import de.lmoesle.processautomationexample.domain.tasklist.UserTask;
import de.lmoesle.processautomationexample.domain.tasklist.UserTaskId;

public interface GetTaskByIdInPort {

    UserTask getTaskById(GetTaskByIdCommand command);

    record GetTaskByIdCommand(UserTaskId taskId) {
    }
}
