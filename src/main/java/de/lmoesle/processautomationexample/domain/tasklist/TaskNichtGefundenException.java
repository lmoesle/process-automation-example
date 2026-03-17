package de.lmoesle.processautomationexample.domain.tasklist;

import java.util.NoSuchElementException;

public class TaskNichtGefundenException extends NoSuchElementException {

    public TaskNichtGefundenException(UserTaskId taskId) {
        super("taskId verweist auf keine vorhandene Aufgabe: " + taskId.value());
    }
}
