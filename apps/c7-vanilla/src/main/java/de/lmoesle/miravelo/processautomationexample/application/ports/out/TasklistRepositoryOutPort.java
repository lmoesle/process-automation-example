package de.lmoesle.miravelo.processautomationexample.application.ports.out;

import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.OffeneBenutzeraufgabe;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTask;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskId;

import java.util.List;
import java.util.Optional;

public interface TasklistRepositoryOutPort {

    boolean speichere(OffeneBenutzeraufgabe aufgabe);

    void entferne(UserTaskId taskId);

    List<UserTask> getAllTasks(BenutzerId benutzerId);

    Optional<UserTask> getTaskById(UserTaskId taskId);

    Optional<UserTask> getTaskById(UserTaskId taskId, BenutzerId benutzerId);

}
