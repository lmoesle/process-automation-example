package de.lmoesle.miravelo.processautomationexample.application.ports.in;

import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskId;
import org.springframework.util.Assert;

public interface GenehmigungVomVorgesetztenInPort {

    void entscheideGenehmigungVomVorgesetzten(GenehmigungVomVorgesetztenCommand command);

    record GenehmigungVomVorgesetztenCommand(
        UserTaskId taskId,
        BenutzerId benutzerId,
        boolean genehmigt,
        String kommentar
    ) {
        public GenehmigungVomVorgesetztenCommand {
            Assert.notNull(taskId, "taskId darf nicht null sein");
            Assert.notNull(benutzerId, "benutzerId darf nicht null sein");
        }
    }
}
