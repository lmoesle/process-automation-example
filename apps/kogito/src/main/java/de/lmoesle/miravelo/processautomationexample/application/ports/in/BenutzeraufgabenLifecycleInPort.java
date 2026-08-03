package de.lmoesle.miravelo.processautomationexample.application.ports.in;

import de.lmoesle.miravelo.processautomationexample.domain.tasklist.OffeneBenutzeraufgabe;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskId;
import org.springframework.util.Assert;

public interface BenutzeraufgabenLifecycleInPort {

    void verarbeiteAktiveBenutzeraufgabe(AktiveBenutzeraufgabeCommand command);

    void verarbeiteEntfernteBenutzeraufgabe(EntfernteBenutzeraufgabeCommand command);

    record AktiveBenutzeraufgabeCommand(OffeneBenutzeraufgabe aufgabe) {
        public AktiveBenutzeraufgabeCommand {
            Assert.notNull(aufgabe, "aufgabe darf nicht null sein");
        }
    }

    record EntfernteBenutzeraufgabeCommand(UserTaskId taskId) {
        public EntfernteBenutzeraufgabeCommand {
            Assert.notNull(taskId, "taskId darf nicht null sein");
        }
    }
}
