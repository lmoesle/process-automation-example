package de.lmoesle.processautomationexample.application.ports.in;

import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.processautomationexample.domain.tasklist.UserTaskId;

public interface GenehmigungVomVorgesetztenInPort {

    void entscheideGenehmigungVomVorgesetzten(GenehmigungVomVorgesetztenCommand command);

    record GenehmigungVomVorgesetztenCommand(
        UserTaskId taskId,
        BenutzerId benutzerId,
        boolean genehmigt,
        String kommentar
    ) {
    }
}
