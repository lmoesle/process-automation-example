package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.SendeBenutzeraufgabenBenachrichtigungInPort.SendeBenutzeraufgabenBenachrichtigungCommand;
import de.lmoesle.processautomationexample.application.ports.out.SendeBenutzeraufgabenBenachrichtigungOutPort;
import de.lmoesle.processautomationexample.application.ports.out.TasklistRepositoryOutPort;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.processautomationexample.domain.tasklist.TaskNichtGefundenException;
import de.lmoesle.processautomationexample.domain.tasklist.UserTask;
import de.lmoesle.processautomationexample.domain.tasklist.UserTaskTestdaten;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SendeBenutzeraufgabenBenachrichtigungUseCaseTest {

    private TasklistRepositoryOutPort tasklistRepositoryOutPort;
    private SendeBenutzeraufgabenBenachrichtigungOutPort sendeBenutzeraufgabenBenachrichtigungOutPort;
    private SendeBenutzeraufgabenBenachrichtigungUseCase useCase;

    @BeforeEach
    void setUp() {
        tasklistRepositoryOutPort = mock(TasklistRepositoryOutPort.class);
        sendeBenutzeraufgabenBenachrichtigungOutPort = mock(SendeBenutzeraufgabenBenachrichtigungOutPort.class);
        useCase = new SendeBenutzeraufgabenBenachrichtigungUseCase(
            tasklistRepositoryOutPort,
            sendeBenutzeraufgabenBenachrichtigungOutPort
        );
    }

    @Test
    void sendsNotificationToAllDistinctCandidateUsers() {
        UserTask userTask = new UserTask(
            UserTaskTestdaten.taskId(),
            UserTaskTestdaten.userTask().urlaubsantrag(),
            List.of(BenutzerTestdaten.ada(), BenutzerTestdaten.carla(), BenutzerTestdaten.ada()),
            BenutzerTestdaten.ada()
        );
        when(tasklistRepositoryOutPort.getTaskById(UserTaskTestdaten.taskId())).thenReturn(Optional.of(userTask));

        useCase.sendeBenutzeraufgabenBenachrichtigung(
            new SendeBenutzeraufgabenBenachrichtigungCommand(UserTaskTestdaten.taskId())
        );

        InOrder inOrder = inOrder(tasklistRepositoryOutPort, sendeBenutzeraufgabenBenachrichtigungOutPort);
        inOrder.verify(tasklistRepositoryOutPort).getTaskById(UserTaskTestdaten.taskId());
        inOrder.verify(sendeBenutzeraufgabenBenachrichtigungOutPort)
            .sendeBenutzeraufgabenBenachrichtigung(userTask, List.of(BenutzerTestdaten.ada(), BenutzerTestdaten.carla()));
        verifyNoMoreInteractions(tasklistRepositoryOutPort, sendeBenutzeraufgabenBenachrichtigungOutPort);
    }

    @Test
    void doesNothingWhenTaskHasNoCandidateUsers() {
        UserTask userTask = new UserTask(
            UserTaskTestdaten.taskId(),
            UserTaskTestdaten.userTask().urlaubsantrag(),
            List.of(),
            BenutzerTestdaten.ada()
        );
        when(tasklistRepositoryOutPort.getTaskById(UserTaskTestdaten.taskId())).thenReturn(Optional.of(userTask));

        useCase.sendeBenutzeraufgabenBenachrichtigung(
            new SendeBenutzeraufgabenBenachrichtigungCommand(UserTaskTestdaten.taskId())
        );

        verify(tasklistRepositoryOutPort).getTaskById(UserTaskTestdaten.taskId());
        verify(sendeBenutzeraufgabenBenachrichtigungOutPort).sendeBenutzeraufgabenBenachrichtigung(userTask, List.of());
    }

    @Test
    void throwsWhenTaskDoesNotExist() {
        when(tasklistRepositoryOutPort.getTaskById(UserTaskTestdaten.taskId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.sendeBenutzeraufgabenBenachrichtigung(
            new SendeBenutzeraufgabenBenachrichtigungCommand(UserTaskTestdaten.taskId())
        ))
            .isInstanceOf(TaskNichtGefundenException.class)
            .hasMessage("taskId verweist auf keine vorhandene Aufgabe: " + UserTaskTestdaten.TASK_ID);

        verifyNoInteractions(sendeBenutzeraufgabenBenachrichtigungOutPort);
    }

    @Test
    void rejectsNullCommand() {
        assertThatThrownBy(() -> useCase.sendeBenutzeraufgabenBenachrichtigung(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("command darf nicht null sein");

        verifyNoInteractions(tasklistRepositoryOutPort, sendeBenutzeraufgabenBenachrichtigungOutPort);
    }

    @Test
    void rejectsNullTaskId() {
        assertThatThrownBy(() -> useCase.sendeBenutzeraufgabenBenachrichtigung(
            new SendeBenutzeraufgabenBenachrichtigungCommand(null)
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("taskId darf nicht null sein");

        verifyNoInteractions(tasklistRepositoryOutPort, sendeBenutzeraufgabenBenachrichtigungOutPort);
    }
}
