package de.lmoesle.miravelo.processautomationexample.application.ports.out;

import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskId;

public interface TaskBearbeitenDirektOutPort {

    void assignTaskToUser(UserTaskId taskId, BenutzerId benutzerId);

    void completeTask(UserTaskId taskId, BenutzerId benutzerId, boolean genehmigt);
}
