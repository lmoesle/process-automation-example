package de.lmoesle.miravelo.processautomationexample.application.ports.in;

import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskId;

public interface BenutzeraufgabenLifecycleInPort {

    void verarbeiteAktiveBenutzeraufgabe(AktiveBenutzeraufgabeCommand command);

    void verarbeiteEntfernteBenutzeraufgabe(EntfernteBenutzeraufgabeCommand command);

    record AktiveBenutzeraufgabeCommand(UserTaskId taskId) {
    }

    record EntfernteBenutzeraufgabeCommand(UserTaskId taskId) {
    }
}
