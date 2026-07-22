package de.lmoesle.miravelo.processautomationexample.application.ports.out;

import de.lmoesle.miravelo.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTask;

import java.util.List;

public interface SendeBenutzeraufgabenBenachrichtigungOutPort {

    void sendeBenutzeraufgabenBenachrichtigung(UserTask userTask, List<Benutzer> empfaenger);
}
