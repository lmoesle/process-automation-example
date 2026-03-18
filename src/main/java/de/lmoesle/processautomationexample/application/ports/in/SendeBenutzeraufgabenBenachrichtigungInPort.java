package de.lmoesle.processautomationexample.application.ports.in;

import de.lmoesle.processautomationexample.domain.tasklist.UserTaskId;

public interface SendeBenutzeraufgabenBenachrichtigungInPort {

    void sendeBenutzeraufgabenBenachrichtigung(SendeBenutzeraufgabenBenachrichtigungCommand command);

    record SendeBenutzeraufgabenBenachrichtigungCommand(UserTaskId taskId) {
    }
}
