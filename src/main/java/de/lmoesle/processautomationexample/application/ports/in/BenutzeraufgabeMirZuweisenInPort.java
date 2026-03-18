package de.lmoesle.processautomationexample.application.ports.in;

import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.processautomationexample.domain.tasklist.UserTaskId;

public interface BenutzeraufgabeMirZuweisenInPort {

    void weiseBenutzeraufgabeMirZu(WeiseBenutzeraufgabeMirZuCommand command);

    record WeiseBenutzeraufgabeMirZuCommand(UserTaskId taskId, BenutzerId benutzerId) {
    }
}
