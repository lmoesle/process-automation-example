package de.lmoesle.processautomationexample.adapter.in.process;

import de.lmoesle.processautomationexample.application.ports.in.SendeBenutzeraufgabenBenachrichtigungInPort;
import de.lmoesle.processautomationexample.application.ports.in.SendeBenutzeraufgabenBenachrichtigungInPort.SendeBenutzeraufgabenBenachrichtigungCommand;
import de.lmoesle.processautomationexample.domain.tasklist.UserTaskTestdaten;
import dev.bpmcrafters.processengineapi.task.TaskInformation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class BenutzeraufgabenBenachrichtigungTaskHandlerTest {

    private SendeBenutzeraufgabenBenachrichtigungInPort inPort;
    private ObjectProvider<SendeBenutzeraufgabenBenachrichtigungInPort> inPortProvider;
    private BenutzeraufgabenBenachrichtigungTaskHandler taskHandler;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        inPort = mock(SendeBenutzeraufgabenBenachrichtigungInPort.class);
        inPortProvider = mock(ObjectProvider.class);
        when(inPortProvider.getObject()).thenReturn(inPort);
        taskHandler = new BenutzeraufgabenBenachrichtigungTaskHandler(inPortProvider);
    }

    @Test
    void forwardsTaskIdToUseCase() {
        taskHandler.accept(new TaskInformation(UserTaskTestdaten.TASK_ID, UserTaskTestdaten.meta()), UserTaskTestdaten.payload());

        verify(inPort).sendeBenutzeraufgabenBenachrichtigung(
            new SendeBenutzeraufgabenBenachrichtigungCommand(UserTaskTestdaten.taskId())
        );
    }

    @Test
    void swallowsRuntimeExceptionsFromUseCase() {
        doThrow(new IllegalStateException("boom"))
            .when(inPort)
            .sendeBenutzeraufgabenBenachrichtigung(new SendeBenutzeraufgabenBenachrichtigungCommand(UserTaskTestdaten.taskId()));

        assertThatCode(() ->
            taskHandler.accept(new TaskInformation(UserTaskTestdaten.TASK_ID, Map.of()), Map.of())
        ).doesNotThrowAnyException();
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
