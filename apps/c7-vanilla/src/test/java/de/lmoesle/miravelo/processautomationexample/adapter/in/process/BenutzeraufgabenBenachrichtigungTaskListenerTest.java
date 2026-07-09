package de.lmoesle.miravelo.processautomationexample.adapter.in.process;

import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort.AktiveBenutzeraufgabeCommand;
import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort.EntfernteBenutzeraufgabeCommand;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskTestdaten;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BenutzeraufgabenBenachrichtigungTaskListenerTest {

    private BenutzeraufgabenLifecycleInPort inPort;
    private ObjectProvider<BenutzeraufgabenLifecycleInPort> inPortProvider;
    private BenutzeraufgabenBenachrichtigungTaskListener taskListener;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        inPort = mock(BenutzeraufgabenLifecycleInPort.class);
        inPortProvider = mock(ObjectProvider.class);
        when(inPortProvider.getObject()).thenReturn(inPort);
        taskListener = new BenutzeraufgabenBenachrichtigungTaskListener(inPortProvider);
    }

    @Test
    void forwardsCreatedTaskToUseCase() {
        taskListener.notify(taskWithEvent(TaskListener.EVENTNAME_CREATE));

        verify(inPort).verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.taskId())
        );
    }

    @Test
    void forwardsAssignedTaskToUseCaseForIdempotentProcessing() {
        taskListener.notify(taskWithEvent(TaskListener.EVENTNAME_ASSIGNMENT));

        verify(inPort).verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.taskId())
        );
    }

    @Test
    void forwardsCompletedTaskAsRemovedTask() {
        taskListener.notify(taskWithEvent(TaskListener.EVENTNAME_COMPLETE));

        verify(inPort).verarbeiteEntfernteBenutzeraufgabe(
            new EntfernteBenutzeraufgabeCommand(UserTaskTestdaten.taskId())
        );
    }

    @Test
    void forwardsDeletedTaskAsRemovedTask() {
        taskListener.notify(taskWithEvent(TaskListener.EVENTNAME_DELETE));

        verify(inPort).verarbeiteEntfernteBenutzeraufgabe(
            new EntfernteBenutzeraufgabeCommand(UserTaskTestdaten.taskId())
        );
    }

    @Test
    void propagatesRuntimeExceptionsFromUseCase() {
        doThrow(new IllegalStateException("boom"))
            .when(inPort)
            .verarbeiteAktiveBenutzeraufgabe(new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.taskId()));

        assertThatThrownBy(() -> taskListener.notify(taskWithEvent(TaskListener.EVENTNAME_CREATE)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("boom");
    }

    @Test
    void rejectsNullDelegateTask() {
        assertThatThrownBy(() -> taskListener.notify(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("delegateTask darf nicht null sein");
    }

    @Test
    void rejectsBlankTaskId() {
        final DelegateTask delegateTask = mock(DelegateTask.class);
        when(delegateTask.getId()).thenReturn(" ");
        when(delegateTask.getEventName()).thenReturn(TaskListener.EVENTNAME_CREATE);

        assertThatThrownBy(() -> taskListener.notify(delegateTask))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("delegateTask.id darf nicht leer sein");
    }

    @Test
    void rejectsUnknownTaskEvent() {
        assertThatThrownBy(() -> taskListener.notify(taskWithEvent(TaskListener.EVENTNAME_UPDATE)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unbekanntes Benutzeraufgaben-Event: update");
    }

    private DelegateTask taskWithEvent(String eventName) {
        final DelegateTask delegateTask = mock(DelegateTask.class);
        when(delegateTask.getId()).thenReturn(UserTaskTestdaten.TASK_ID);
        when(delegateTask.getEventName()).thenReturn(eventName);
        return delegateTask;
    }
}
