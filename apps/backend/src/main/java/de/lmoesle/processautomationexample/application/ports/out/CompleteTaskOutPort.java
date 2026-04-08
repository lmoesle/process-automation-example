package de.lmoesle.processautomationexample.application.ports.out;

import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.processautomationexample.domain.tasklist.UserTaskId;

public interface CompleteTaskOutPort {

    void completeTask(UserTaskId taskId, BenutzerId benutzerId, boolean genehmigt);

}
