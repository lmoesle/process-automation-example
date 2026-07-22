package de.lmoesle.miravelo.processautomationexample.adapter.out.process;

import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskTestdaten;
import org.camunda.bpm.engine.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CamundaTasklistAdapterTest {

    private TaskService taskService;
    private CamundaTasklistAdapter camundaTasklistAdapter;

    @BeforeEach
    void setUp() {
        taskService = mock(TaskService.class);
        camundaTasklistAdapter = new CamundaTasklistAdapter(taskService);
    }

    @Test
    void assignsTaskToUserViaTaskService() {
        camundaTasklistAdapter.assignTaskToUser(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId());

        verify(taskService).setAssignee(UserTaskTestdaten.TASK_ID, BenutzerTestdaten.ADA_UUID.toString());
    }

    @Test
    void wrapsTaskServiceErrorsWhenAssigningTaskToUser() {
        doThrow(new RuntimeException("boom"))
            .when(taskService)
            .setAssignee(UserTaskTestdaten.TASK_ID, BenutzerTestdaten.ADA_UUID.toString());

        assertThatThrownBy(() -> camundaTasklistAdapter.assignTaskToUser(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Aufgabe " + UserTaskTestdaten.TASK_ID + " konnte Benutzer " + BenutzerTestdaten.ADA_UUID + " nicht zugewiesen werden")
            .hasRootCauseMessage("boom");
    }

    @Test
    void assignsTaskBeforeCompletingIt() {
        camundaTasklistAdapter.completeTask(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId(), true);

        final InOrder inOrder = inOrder(taskService);
        inOrder.verify(taskService).setAssignee(UserTaskTestdaten.TASK_ID, BenutzerTestdaten.ADA_UUID.toString());
        inOrder.verify(taskService).complete(UserTaskTestdaten.TASK_ID, Map.of("genehmigt", true));
    }

    @Test
    void wrapsTaskServiceErrorsWhenCompletingTask() {
        doThrow(new RuntimeException("boom"))
            .when(taskService)
            .complete(UserTaskTestdaten.TASK_ID, Map.of("genehmigt", false));

        assertThatThrownBy(() -> camundaTasklistAdapter.completeTask(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId(), false))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Aufgabe " + UserTaskTestdaten.TASK_ID + " konnte nicht abgeschlossen werden")
            .hasRootCauseMessage("boom");
    }
}
