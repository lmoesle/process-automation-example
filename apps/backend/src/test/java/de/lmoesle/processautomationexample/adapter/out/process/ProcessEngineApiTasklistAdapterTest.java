package de.lmoesle.processautomationexample.adapter.out.process;

import de.lmoesle.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.processautomationexample.domain.tasklist.UserTaskTestdaten;
import dev.bpmcrafters.processengineapi.task.ChangeAssignmentModifyTaskCmd.AssignTaskCmd;
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd;
import dev.bpmcrafters.processengineapi.task.ModifyTaskCmd;
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi;
import dev.bpmcrafters.processengineapi.task.UserTaskModificationApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class ProcessEngineApiTasklistAdapterTest {

    private UserTaskModificationApi userTaskModificationApi;
    private UserTaskCompletionApi userTaskCompletionApi;
    private ProcessEngineApiTasklistAdapter processEngineApiTasklistAdapter;

    @BeforeEach
    void setUp() {
        userTaskModificationApi = mock(UserTaskModificationApi.class);
        userTaskCompletionApi = mock(UserTaskCompletionApi.class);
        processEngineApiTasklistAdapter = new ProcessEngineApiTasklistAdapter(
            userTaskModificationApi,
            userTaskCompletionApi
        );
    }

    @Test
    void assignsTaskToUserViaModificationApi() {
        when(userTaskModificationApi.update(argThat(command ->
            command instanceof AssignTaskCmd assignTaskCmd
                && assignTaskCmd.getTaskId().equals(UserTaskTestdaten.TASK_ID)
                && assignTaskCmd.getAssignee().equals(BenutzerTestdaten.ADA_UUID.toString())
        ))).thenReturn(CompletableFuture.completedFuture(null));

        processEngineApiTasklistAdapter.assignTaskToUser(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId());

        verify(userTaskModificationApi).update(argThat(command ->
            command instanceof AssignTaskCmd assignTaskCmd
                && assignTaskCmd.getTaskId().equals(UserTaskTestdaten.TASK_ID)
                && assignTaskCmd.getAssignee().equals(BenutzerTestdaten.ADA_UUID.toString())
        ));
    }

    @Test
    void wrapsModificationErrorsWhenAssigningTaskToUser() {
        when(userTaskModificationApi.update(org.mockito.ArgumentMatchers.any(ModifyTaskCmd.class)))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("boom")));

        assertThatThrownBy(() -> processEngineApiTasklistAdapter.assignTaskToUser(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Aufgabe " + UserTaskTestdaten.TASK_ID + " konnte Benutzer " + BenutzerTestdaten.ADA_UUID + " nicht zugewiesen werden")
            .hasRootCauseMessage("boom");
    }

    @Test
    void assignsTaskBeforeCompletingIt() {
        when(userTaskModificationApi.update(argThat(command ->
            command instanceof AssignTaskCmd assignTaskCmd
                && assignTaskCmd.getTaskId().equals(UserTaskTestdaten.TASK_ID)
                && assignTaskCmd.getAssignee().equals(BenutzerTestdaten.ADA_UUID.toString())
        ))).thenReturn(CompletableFuture.completedFuture(null));
        when(userTaskCompletionApi.completeTask(argThat(command ->
            command.getTaskId().equals(UserTaskTestdaten.TASK_ID)
                && command.get().get("genehmigt").equals(true)
        ))).thenReturn(CompletableFuture.completedFuture(null));

        processEngineApiTasklistAdapter.completeTask(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId(), true);

        InOrder inOrder = inOrder(userTaskModificationApi, userTaskCompletionApi);
        inOrder.verify(userTaskModificationApi).update(argThat(command ->
            command instanceof AssignTaskCmd assignTaskCmd
                && assignTaskCmd.getTaskId().equals(UserTaskTestdaten.TASK_ID)
                && assignTaskCmd.getAssignee().equals(BenutzerTestdaten.ADA_UUID.toString())
        ));
        inOrder.verify(userTaskCompletionApi).completeTask(argThat(command ->
            command.getTaskId().equals(UserTaskTestdaten.TASK_ID)
                && command.get().get("genehmigt").equals(true)
        ));
    }

    @Test
    void wrapsCompletionErrorsWhenCompletingTask() {
        when(userTaskModificationApi.update(org.mockito.ArgumentMatchers.any(ModifyTaskCmd.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        when(userTaskCompletionApi.completeTask(org.mockito.ArgumentMatchers.any(CompleteTaskCmd.class)))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("boom")));

        assertThatThrownBy(() -> processEngineApiTasklistAdapter.completeTask(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId(), false))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Aufgabe " + UserTaskTestdaten.TASK_ID + " konnte nicht abgeschlossen werden")
            .hasRootCauseMessage("boom");
    }
}
