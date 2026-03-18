package de.lmoesle.processautomationexample.domain.tasklist;

public class TaskZugriffVerweigertException extends SecurityException {

    public TaskZugriffVerweigertException(UserTaskId taskId) {
        super("Aktueller Benutzer hat keinen Zugriff auf Aufgabe: " + taskId.value());
    }
}
