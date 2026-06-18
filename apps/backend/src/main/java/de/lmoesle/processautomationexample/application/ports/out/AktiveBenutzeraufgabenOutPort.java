package de.lmoesle.processautomationexample.application.ports.out;

import de.lmoesle.processautomationexample.domain.tasklist.UserTaskId;

public interface AktiveBenutzeraufgabenOutPort {

    boolean speichereWennNeu(UserTaskId taskId);

    void entferne(UserTaskId taskId);
}
