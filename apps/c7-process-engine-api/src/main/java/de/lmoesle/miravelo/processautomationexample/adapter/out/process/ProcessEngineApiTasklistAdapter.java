package de.lmoesle.miravelo.processautomationexample.adapter.out.process;

import de.lmoesle.miravelo.processautomationexample.application.ports.out.TaskBearbeitenDirektOutPort;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskId;
import dev.bpmcrafters.processengineapi.task.ChangeAssignmentModifyTaskCmd.AssignTaskCmd;
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd;
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi;
import dev.bpmcrafters.processengineapi.task.UserTaskModificationApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class ProcessEngineApiTasklistAdapter implements TaskBearbeitenDirektOutPort {

    private static final long TASK_MODIFICATION_TIMEOUT_SECONDS = 10;
    private static final long TASK_COMPLETION_TIMEOUT_SECONDS = 10;

    private final UserTaskModificationApi userTaskModificationApi;
    private final UserTaskCompletionApi userTaskCompletionApi;

    @Override
    public void assignTaskToUser(UserTaskId taskId, BenutzerId benutzerId) {
        try {
            userTaskModificationApi.update(new AssignTaskCmd(taskId.value(), benutzerId.value().toString()))
                .get(TASK_MODIFICATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException exception) {
            throw new ProzessEngineAuftragUnklarException(
                "Aufgabe " + taskId.value() + " konnte Benutzer " + benutzerId.value()
                    + " innerhalb des Timeouts nicht eindeutig zugewiesen werden",
                exception
            );
        } catch (InterruptedException | IllegalStateException | ExecutionException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            if (istDurchTimeoutVerursacht(exception)) {
                throw new ProzessEngineAuftragUnklarException(
                    "Aufgabe " + taskId.value() + " konnte Benutzer " + benutzerId.value()
                        + " nicht eindeutig zugewiesen werden",
                    exception
                );
            }
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
            userTaskCompletionApi.completeTask(new CompleteTaskCmd(taskId.value(), Map.of("genehmigt", genehmigt)))
                .get(TASK_COMPLETION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException exception) {
            throw new ProzessEngineAuftragUnklarException(
                "Aufgabe " + taskId.value() + " konnte innerhalb des Timeouts nicht eindeutig abgeschlossen werden",
                exception
            );
        } catch (InterruptedException | IllegalStateException | ExecutionException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            if (istDurchTimeoutVerursacht(exception)) {
                throw new ProzessEngineAuftragUnklarException(
                    "Aufgabe " + taskId.value() + " konnte nicht eindeutig abgeschlossen werden",
                    exception
                );
            }
            throw new IllegalStateException(
                "Aufgabe " + taskId.value() + " konnte nicht abgeschlossen werden",
                exception
            );
        }
    }

    private boolean istDurchTimeoutVerursacht(Throwable exception) {
        Throwable currentException = exception;
        while (currentException != null) {
            if (currentException instanceof TimeoutException) {
                return true;
            }
            currentException = currentException.getCause();
        }
        return false;
    }

}
