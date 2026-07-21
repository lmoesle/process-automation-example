package de.lmoesle.miravelo.processautomationexample.application.ports.in;

import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskId;
import org.springframework.util.Assert;

public interface BenutzeraufgabenLifecycleInPort {

    void verarbeiteAktiveBenutzeraufgabe(AktiveBenutzeraufgabeCommand command);

    void verarbeiteEntfernteBenutzeraufgabe(EntfernteBenutzeraufgabeCommand command);

    record AktiveBenutzeraufgabeCommand(UserTaskId taskId) {
        public AktiveBenutzeraufgabeCommand {
            Assert.notNull(taskId, "taskId darf nicht null sein");
        }
    }

    record EntfernteBenutzeraufgabeCommand(UserTaskId taskId) {
        public EntfernteBenutzeraufgabeCommand {
            Assert.notNull(taskId, "taskId darf nicht null sein");
        }
    }
}
