package de.lmoesle.miravelo.processautomationexample.application.ports.out;

import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskId;

public interface AktiveBenutzeraufgabenOutPort {

    boolean speichereWennNeu(UserTaskId taskId);

    void entferne(UserTaskId taskId);
}
