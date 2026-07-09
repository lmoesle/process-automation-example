package de.lmoesle.miravelo.processautomationexample.adapter.out.process;

import de.lmoesle.miravelo.processautomationexample.application.ports.out.AssignTaskOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.CompleteTaskOutPort;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskId;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.TaskService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CamundaTasklistAdapter implements CompleteTaskOutPort, AssignTaskOutPort {

    private final TaskService taskService;

    @Override
    public void assignTaskToUser(UserTaskId taskId, BenutzerId benutzerId) {
        try {
            taskService.setAssignee(taskId.value(), benutzerId.value().toString());
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                "Aufgabe " + taskId.value() + " konnte Benutzer " + benutzerId.value() + " nicht zugewiesen werden",
                exception
            );
        }
    }

    @Override
    public void completeTask(UserTaskId taskId, BenutzerId benutzerId, boolean genehmigt) {
        assignTaskToUser(taskId, benutzerId);
        try {
            taskService.complete(taskId.value(), Map.of("genehmigt", genehmigt));
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                "Aufgabe " + taskId.value() + " konnte nicht abgeschlossen werden",
                exception
            );
        }
    }

}
