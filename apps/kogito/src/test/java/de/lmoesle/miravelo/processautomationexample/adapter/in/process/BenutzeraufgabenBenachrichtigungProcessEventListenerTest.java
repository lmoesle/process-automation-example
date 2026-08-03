package de.lmoesle.miravelo.processautomationexample.adapter.in.process;

import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort.AktiveBenutzeraufgabeCommand;
import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort.EntfernteBenutzeraufgabeCommand;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.internal.process.event.ProcessWorkItemTransitionEvent;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.kie.kogito.internal.process.workitem.WorkItemTerminationType;
import org.kie.kogito.internal.process.workitem.WorkItemTransition;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BenutzeraufgabenBenachrichtigungProcessEventListenerTest {

    private BenutzeraufgabenLifecycleInPort inPort;
    private BenutzeraufgabenBenachrichtigungProcessEventListener listener;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        inPort = mock(BenutzeraufgabenLifecycleInPort.class);
        final ObjectProvider<BenutzeraufgabenLifecycleInPort> inPortProvider = mock(ObjectProvider.class);
        when(inPortProvider.getObject()).thenReturn(inPort);
        listener = new BenutzeraufgabenBenachrichtigungProcessEventListener(inPortProvider);
    }

    @Test
    void forwardsActiveUserTaskSnapshotToUseCase() {
        listener.afterWorkItemTransition(activeTaskEvent());

        verify(inPort).verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.offeneBenutzeraufgabe())
        );
    }

    @Test
    void usesVacationRequestVariableWhenBusinessKeyIsMissing() {
        final var event = activeTaskEvent();
        final var processInstance = event.getWorkItem().getProcessInstance();
        when(processInstance.getBusinessKey()).thenReturn(null);
        when(processInstance.getVariables()).thenReturn(Map.of(
            "urlaubsantragId",
            UserTaskTestdaten.BUSINESS_KEY
        ));

        listener.afterWorkItemTransition(event);

        verify(inPort).verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.offeneBenutzeraufgabe())
        );
    }

    @Test
    void forwardsTerminatedUserTaskAsRemovedTask() {
        listener.afterWorkItemTransition(terminatedTaskEvent());

        verify(inPort).verarbeiteEntfernteBenutzeraufgabe(
            new EntfernteBenutzeraufgabeCommand(UserTaskTestdaten.taskId())
        );
    }

    @Test
    void ignoresEventBeforeTransition() {
        final var event = mock(ProcessWorkItemTransitionEvent.class);

        listener.afterWorkItemTransition(event);

        verifyNoInteractions(inPort);
    }

    @Test
    void ignoresWorkItemsWithoutTaskName() {
        final var event = mock(ProcessWorkItemTransitionEvent.class);
        when(event.isTransitioned()).thenReturn(true);
        when(event.getWorkItem()).thenReturn(mock(KogitoWorkItem.class));

        listener.afterWorkItemTransition(event);

        verifyNoInteractions(inPort);
    }

    @Test
    void propagatesRuntimeExceptionsFromUseCase() {
        doThrow(new IllegalStateException("boom"))
            .when(inPort)
            .verarbeiteAktiveBenutzeraufgabe(
                new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.offeneBenutzeraufgabe())
            );

        assertThatThrownBy(() -> listener.afterWorkItemTransition(activeTaskEvent()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("boom");
    }

    @Test
    void rejectsNullEvent() {
        assertThatThrownBy(() -> listener.afterWorkItemTransition(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("event darf nicht null sein");
    }

    private ProcessWorkItemTransitionEvent activeTaskEvent() {
        final var event = taskEvent(Optional.empty());
        final var workItem = event.getWorkItem();
        final var processInstance = mock(KogitoProcessInstance.class);
        when(workItem.getProcessInstanceStringId()).thenReturn(UrlaubsantragTestData.PROCESS_INSTANCE_ID_VALUE);
        when(workItem.getParameter("ActorId")).thenReturn(
            BenutzerTestdaten.ADA_UUID + "," + BenutzerTestdaten.CARLA_UUID
        );
        when(workItem.getActualOwner()).thenReturn(BenutzerTestdaten.ADA_UUID.toString());
        when(workItem.getProcessInstance()).thenReturn(processInstance);
        when(processInstance.getBusinessKey()).thenReturn(UserTaskTestdaten.BUSINESS_KEY);
        return event;
    }

    private ProcessWorkItemTransitionEvent terminatedTaskEvent() {
        return taskEvent(Optional.of(WorkItemTerminationType.COMPLETE));
    }

    private ProcessWorkItemTransitionEvent taskEvent(Optional<WorkItemTerminationType> termination) {
        final var event = mock(ProcessWorkItemTransitionEvent.class);
        final var transition = mock(WorkItemTransition.class);
        final var workItem = mock(KogitoWorkItem.class);
        when(event.isTransitioned()).thenReturn(true);
        when(event.getWorkItem()).thenReturn(workItem);
        when(event.getTransition()).thenReturn(transition);
        when(transition.termination()).thenReturn(termination);
        when(workItem.getStringId()).thenReturn(UserTaskTestdaten.TASK_ID);
        when(workItem.getParameter("TaskName")).thenReturn(UserTaskTestdaten.TASK_NAME);
        return event;
    }
}
