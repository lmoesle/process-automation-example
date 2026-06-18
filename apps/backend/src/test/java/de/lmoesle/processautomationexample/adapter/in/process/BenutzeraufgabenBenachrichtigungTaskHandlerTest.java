package de.lmoesle.processautomationexample.adapter.in.process;

import de.lmoesle.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort;
import de.lmoesle.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort.AktiveBenutzeraufgabeCommand;
import de.lmoesle.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort.EntfernteBenutzeraufgabeCommand;
import de.lmoesle.processautomationexample.domain.tasklist.UserTaskTestdaten;
import dev.bpmcrafters.processengineapi.task.TaskInformation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class BenutzeraufgabenBenachrichtigungTaskHandlerTest {

    private BenutzeraufgabenLifecycleInPort inPort;
    private ObjectProvider<BenutzeraufgabenLifecycleInPort> inPortProvider;
    private BenutzeraufgabenBenachrichtigungTaskHandler taskHandler;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        inPort = mock(BenutzeraufgabenLifecycleInPort.class);
        inPortProvider = mock(ObjectProvider.class);
        when(inPortProvider.getObject()).thenReturn(inPort);
        taskHandler = new BenutzeraufgabenBenachrichtigungTaskHandler(inPortProvider);
    }

    @Test
    void forwardsTaskIdToUseCase() {
        taskHandler.accept(new TaskInformation(UserTaskTestdaten.TASK_ID, UserTaskTestdaten.meta()), UserTaskTestdaten.payload());

        verify(inPort).verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.taskId())
        );
    }

    @Test
    void forwardsExplicitCreateReasonToUseCase() {
        taskHandler.accept(
            new TaskInformation(UserTaskTestdaten.TASK_ID, UserTaskTestdaten.meta()).withReason(TaskInformation.CREATE),
            UserTaskTestdaten.payload()
        );

        verify(inPort).verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.taskId())
        );
    }

    @Test
    void forwardsTaskUpdatesForIdempotentProcessing() {
        taskHandler.accept(
            new TaskInformation(UserTaskTestdaten.TASK_ID, UserTaskTestdaten.meta()).withReason(TaskInformation.UPDATE),
            UserTaskTestdaten.payload()
        );

        verify(inPort).verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.taskId())
        );
    }

    @Test
    void forwardsTaskAssignmentsForIdempotentProcessing() {
        taskHandler.accept(
            new TaskInformation(UserTaskTestdaten.TASK_ID, UserTaskTestdaten.meta()).withReason(TaskInformation.ASSIGN),
            UserTaskTestdaten.payload()
        );

        verify(inPort).verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.taskId())
        );
    }

    @Test
    void forwardsCompleteReasonAsRemovedTask() {
        taskHandler.accept(
            new TaskInformation(UserTaskTestdaten.TASK_ID, UserTaskTestdaten.meta()).withReason(TaskInformation.COMPLETE),
            UserTaskTestdaten.payload()
        );

        verify(inPort).verarbeiteEntfernteBenutzeraufgabe(
            new EntfernteBenutzeraufgabeCommand(UserTaskTestdaten.taskId())
        );
    }

    @Test
    void forwardsDeleteReasonAsRemovedTask() {
        taskHandler.accept(
            new TaskInformation(UserTaskTestdaten.TASK_ID, UserTaskTestdaten.meta()).withReason(TaskInformation.DELETE),
            UserTaskTestdaten.payload()
        );

        verify(inPort).verarbeiteEntfernteBenutzeraufgabe(
            new EntfernteBenutzeraufgabeCommand(UserTaskTestdaten.taskId())
        );
    }

    @Test
    void forwardsTerminatedTaskAsRemovedTask() {
        taskHandler.accept(new TaskInformation(UserTaskTestdaten.TASK_ID, UserTaskTestdaten.meta()));

        verify(inPort).verarbeiteEntfernteBenutzeraufgabe(
            new EntfernteBenutzeraufgabeCommand(UserTaskTestdaten.taskId())
        );
    }

    @Test
    void propagatesRuntimeExceptionsFromUseCase() {
        doThrow(new IllegalStateException("boom"))
            .when(inPort)
            .verarbeiteAktiveBenutzeraufgabe(new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.taskId()));

        assertThatThrownBy(() ->
            taskHandler.accept(new TaskInformation(UserTaskTestdaten.TASK_ID, Map.of()), Map.of())
        )
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("boom");
    }

    @Test
    void propagatesRuntimeExceptionsFromRemovedTaskUseCase() {
        doThrow(new IllegalStateException("boom"))
            .when(inPort)
            .verarbeiteEntfernteBenutzeraufgabe(new EntfernteBenutzeraufgabeCommand(UserTaskTestdaten.taskId()));

        assertThatThrownBy(() -> taskHandler.accept(new TaskInformation(UserTaskTestdaten.TASK_ID, Map.of())))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("boom");
    }

    @Test
    void rejectsNullTaskInformation() {
        assertThatThrownBy(() -> taskHandler.accept(null, Map.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("taskInformation darf nicht null sein");
    }

    @Test
    void rejectsBlankTaskId() {
        assertThatThrownBy(() -> taskHandler.accept(new TaskInformation(" ", Map.of()), Map.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("taskInformation.taskId darf nicht leer sein");
    }
}
