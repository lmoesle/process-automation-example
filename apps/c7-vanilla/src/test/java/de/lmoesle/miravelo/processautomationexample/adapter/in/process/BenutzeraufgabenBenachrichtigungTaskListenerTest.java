package de.lmoesle.miravelo.processautomationexample.adapter.in.process;

import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort.AktiveBenutzeraufgabeCommand;
import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort.EntfernteBenutzeraufgabeCommand;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.ObjectProvider;

import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
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
    void forwardsCreatedTaskSnapshotToUseCase() {
        taskListener.notify(taskWithEvent(TaskListener.EVENTNAME_CREATE));

        verify(inPort).verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.offeneBenutzeraufgabe())
        );
    }

    @Test
    void forwardsAssignedTaskSnapshotToUseCaseForIdempotentProcessing() {
        taskListener.notify(taskWithEvent(TaskListener.EVENTNAME_ASSIGNMENT));

        verify(inPort).verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.offeneBenutzeraufgabe())
        );
    }

    @Test
    void forwardsUpdatedTaskSnapshotToUseCaseForIdempotentProcessing() {
        taskListener.notify(taskWithEvent(TaskListener.EVENTNAME_UPDATE));

        verify(inPort).verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.offeneBenutzeraufgabe())
        );
    }

    @Test
    void usesVacationRequestVariableForTaskWithoutBusinessKey() {
        final DelegateTask delegateTask = taskWithEvent(TaskListener.EVENTNAME_UPDATE);
        when(delegateTask.getExecution().getProcessBusinessKey()).thenReturn(null);
        when(delegateTask.getVariable("urlaubsantragId")).thenReturn(UserTaskTestdaten.BUSINESS_KEY);

        taskListener.notify(delegateTask);

        verify(inPort).verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.offeneBenutzeraufgabe())
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
    void processesAssignmentBeforeCompletionInSameTransaction() {
        taskListener.notify(taskWithEvent(TaskListener.EVENTNAME_ASSIGNMENT));
        taskListener.notify(taskWithEvent(TaskListener.EVENTNAME_COMPLETE));

        final InOrder inOrder = inOrder(inPort);
        inOrder.verify(inPort).verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.offeneBenutzeraufgabe())
        );
        inOrder.verify(inPort).verarbeiteEntfernteBenutzeraufgabe(
            new EntfernteBenutzeraufgabeCommand(UserTaskTestdaten.taskId())
        );
    }

    @Test
    void propagatesRuntimeExceptionsFromUseCase() {
        doThrow(new IllegalStateException("boom"))
            .when(inPort)
            .verarbeiteAktiveBenutzeraufgabe(new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.offeneBenutzeraufgabe()));

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
        assertThatThrownBy(() -> taskListener.notify(taskWithEvent("unknown")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unbekanntes Benutzeraufgaben-Event: unknown");
    }

    private DelegateTask taskWithEvent(String eventName) {
        final DelegateTask delegateTask = mock(DelegateTask.class);
        final DelegateExecution delegateExecution = mock(DelegateExecution.class);
        when(delegateTask.getId()).thenReturn(UserTaskTestdaten.TASK_ID);
        when(delegateTask.getEventName()).thenReturn(eventName);
        when(delegateTask.getName()).thenReturn(UserTaskTestdaten.TASK_NAME);
        when(delegateTask.getProcessInstanceId()).thenReturn(UrlaubsantragTestData.PROCESS_INSTANCE_ID_VALUE);
        when(delegateTask.getExecution()).thenReturn(delegateExecution);
        when(delegateExecution.getProcessBusinessKey()).thenReturn(UserTaskTestdaten.BUSINESS_KEY);
        final var candidateUsers = new LinkedHashSet<>(List.of(
            candidate(BenutzerTestdaten.ADA_UUID.toString()),
            candidate(BenutzerTestdaten.CARLA_UUID.toString())
        ));
        when(delegateTask.getCandidates()).thenReturn(candidateUsers);
        when(delegateTask.getAssignee()).thenReturn(BenutzerTestdaten.ADA_UUID.toString());
        return delegateTask;
    }

    private IdentityLink candidate(String benutzerId) {
        final IdentityLink identityLink = mock(IdentityLink.class);
        when(identityLink.getType()).thenReturn(IdentityLinkType.CANDIDATE);
        when(identityLink.getUserId()).thenReturn(benutzerId);
        return identityLink;
    }
}
