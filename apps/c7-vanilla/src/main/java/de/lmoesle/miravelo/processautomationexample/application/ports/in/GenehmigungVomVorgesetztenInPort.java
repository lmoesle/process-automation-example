package de.lmoesle.miravelo.processautomationexample.application.ports.in;

import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskId;

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
