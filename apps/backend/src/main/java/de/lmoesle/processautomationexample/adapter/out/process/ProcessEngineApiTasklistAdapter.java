package de.lmoesle.processautomationexample.adapter.out.process;

import de.lmoesle.processautomationexample.application.ports.out.AssignTaskOutPort;
import de.lmoesle.processautomationexample.application.ports.out.CompleteTaskOutPort;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.processautomationexample.domain.tasklist.UserTaskId;
import de.lmoesle.processautomationexample.shared.bpmn.VacationApprovalBpmnApi;
import dev.bpmcrafters.processengineapi.task.ChangeAssignmentModifyTaskCmd.AssignTaskCmd;
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd;
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi;
import dev.bpmcrafters.processengineapi.task.UserTaskModificationApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class ProcessEngineApiTasklistAdapter implements CompleteTaskOutPort, AssignTaskOutPort {

    private static final long TASK_MODIFICATION_TIMEOUT_SECONDS = 10;
    private static final long TASK_COMPLETION_TIMEOUT_SECONDS = 10;

    private final UserTaskModificationApi userTaskModificationApi;
    private final UserTaskCompletionApi userTaskCompletionApi;

    @Override
    public void assignTaskToUser(UserTaskId taskId, BenutzerId benutzerId) {
        Assert.notNull(taskId, "taskId darf nicht null sein");
        Assert.notNull(benutzerId, "benutzerId darf nicht null sein");

        try {
            userTaskModificationApi.update(new AssignTaskCmd(taskId.value(), benutzerId.value().toString()))
                .get(TASK_MODIFICATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | IllegalStateException | ExecutionException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException(
                "Aufgabe " + taskId.value() + " konnte Benutzer " + benutzerId.value() + " nicht zugewiesen werden",
                exception
            );
        }
    }

    @Override
    public void completeTask(UserTaskId taskId, boolean genehmigt) {
        Assert.notNull(taskId, "taskId darf nicht null sein");

        try {
            userTaskCompletionApi.completeTask(new CompleteTaskCmd(
                taskId.value(),
                Map.of(VacationApprovalBpmnApi.PROCESS_VARIABLE_APPROVED, genehmigt)
            ))
                .get(TASK_COMPLETION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | IllegalStateException | ExecutionException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException(
                "Aufgabe " + taskId.value() + " konnte nicht abgeschlossen werden",
                exception
            );
        }
    }

}
