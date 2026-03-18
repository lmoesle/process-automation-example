package de.lmoesle.processautomationexample.application.ports.out;

import de.lmoesle.processautomationexample.domain.tasklist.UserTaskId;

public interface CompleteTaskOutPort {

    void completeTask(UserTaskId taskId, boolean genehmigt);

}
