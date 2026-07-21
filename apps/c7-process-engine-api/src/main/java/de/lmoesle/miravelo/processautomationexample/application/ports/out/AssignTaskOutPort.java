package de.lmoesle.miravelo.processautomationexample.application.ports.out;

import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskId;

public interface AssignTaskOutPort {

    void assignTaskToUser(UserTaskId taskId, BenutzerId benutzerId);
}
