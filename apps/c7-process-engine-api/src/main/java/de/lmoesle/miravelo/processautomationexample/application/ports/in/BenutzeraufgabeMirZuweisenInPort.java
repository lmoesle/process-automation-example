package de.lmoesle.miravelo.processautomationexample.application.ports.in;

import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskId;

public interface BenutzeraufgabeMirZuweisenInPort {

    void weiseBenutzeraufgabeMirZu(WeiseBenutzeraufgabeMirZuCommand command);

    record WeiseBenutzeraufgabeMirZuCommand(UserTaskId taskId, BenutzerId benutzerId) {
    }
}
