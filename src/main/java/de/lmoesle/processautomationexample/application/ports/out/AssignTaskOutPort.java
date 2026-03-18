package de.lmoesle.processautomationexample.application.ports.out;

import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.processautomationexample.domain.tasklist.UserTaskId;

public interface AssignTaskOutPort {

    void assignTaskToUser(UserTaskId taskId, BenutzerId benutzerId);
}
