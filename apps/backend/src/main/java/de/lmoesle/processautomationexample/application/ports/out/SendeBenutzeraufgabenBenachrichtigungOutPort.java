package de.lmoesle.processautomationexample.application.ports.out;

import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.processautomationexample.domain.tasklist.UserTask;

import java.util.List;

public interface SendeBenutzeraufgabenBenachrichtigungOutPort {

    void sendeBenutzeraufgabenBenachrichtigung(UserTask userTask, List<Benutzer> empfaenger);
}
