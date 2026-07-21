package de.lmoesle.miravelo.processautomationexample.application.ports.in;

import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTask;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskId;
import org.springframework.util.Assert;

import java.util.List;

public interface TaskAbfragenInPort {

    List<UserTask> getAllTasks(GetAllTasksCommand command);

    UserTask getTaskById(GetTaskByIdCommand command);

    record GetAllTasksCommand(BenutzerId benutzerId) {
        public GetAllTasksCommand {
            Assert.notNull(benutzerId, "benutzerId darf nicht null sein");
        }
    }

    record GetTaskByIdCommand(UserTaskId taskId, BenutzerId benutzerId) {
        public GetTaskByIdCommand {
            Assert.notNull(taskId, "taskId darf nicht null sein");
            Assert.notNull(benutzerId, "benutzerId darf nicht null sein");
        }
    }
}
