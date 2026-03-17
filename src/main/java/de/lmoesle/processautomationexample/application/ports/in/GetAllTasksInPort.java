package de.lmoesle.processautomationexample.application.ports.in;

import de.lmoesle.processautomationexample.domain.tasklist.UserTask;

import java.util.List;

public interface GetAllTasksInPort {

    List<UserTask> getAllTasks();

}
