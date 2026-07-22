package de.lmoesle.miravelo.processautomationexample.application.ports.in;

import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskId;
import org.springframework.util.Assert;

public interface BenutzeraufgabeMirZuweisenInPort {

    void weiseBenutzeraufgabeMirZu(WeiseBenutzeraufgabeMirZuCommand command);

    record WeiseBenutzeraufgabeMirZuCommand(UserTaskId taskId, BenutzerId benutzerId) {
        public WeiseBenutzeraufgabeMirZuCommand {
            Assert.notNull(taskId, "taskId darf nicht null sein");
            Assert.notNull(benutzerId, "benutzerId darf nicht null sein");
        }
    }
}
