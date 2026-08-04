package de.lmoesle.miravelo.processautomationexample.application.usecases;

import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort.AktiveBenutzeraufgabeCommand;
import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort.EntfernteBenutzeraufgabeCommand;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.SendeBenutzeraufgabenBenachrichtigungOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.TasklistRepositoryOutPort;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.TaskNichtGefundenException;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTask;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskTestdaten;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
    void storesTaskAndNotifiesAllDistinctCandidateUsers() {
        final UserTask userTask = new UserTask(
            UserTaskTestdaten.taskId(),
            UserTaskTestdaten.userTask().urlaubsantrag(),
            List.of(BenutzerTestdaten.ada(), BenutzerTestdaten.carla(), BenutzerTestdaten.ada()),
            BenutzerTestdaten.ada()
        );
        when(tasklistRepositoryOutPort.speichere(UserTaskTestdaten.offeneBenutzeraufgabe())).thenReturn(true);
        when(tasklistRepositoryOutPort.getTaskById(UserTaskTestdaten.taskId())).thenReturn(Optional.of(userTask));

        useCase.verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.offeneBenutzeraufgabe())
        );

        final InOrder inOrder = inOrder(tasklistRepositoryOutPort, sendeBenutzeraufgabenBenachrichtigungOutPort);
        inOrder.verify(tasklistRepositoryOutPort).speichere(UserTaskTestdaten.offeneBenutzeraufgabe());
        inOrder.verify(tasklistRepositoryOutPort).getTaskById(UserTaskTestdaten.taskId());
        inOrder.verify(sendeBenutzeraufgabenBenachrichtigungOutPort)
            .sendeBenutzeraufgabenBenachrichtigung(userTask, List.of(BenutzerTestdaten.ada(), BenutzerTestdaten.carla()));
        verifyNoMoreInteractions(tasklistRepositoryOutPort, sendeBenutzeraufgabenBenachrichtigungOutPort);
    }

    @Test
    void updatesExistingTaskWithoutSendingAnotherNotification() {
        when(tasklistRepositoryOutPort.speichere(UserTaskTestdaten.offeneBenutzeraufgabe())).thenReturn(false);

        useCase.verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.offeneBenutzeraufgabe())
        );

        verify(tasklistRepositoryOutPort).speichere(UserTaskTestdaten.offeneBenutzeraufgabe());
        verifyNoMoreInteractions(tasklistRepositoryOutPort);
        verifyNoInteractions(sendeBenutzeraufgabenBenachrichtigungOutPort);
    }

    @Test
    void removesTaskWhenItIsCompletedOrDeleted() {
        useCase.verarbeiteEntfernteBenutzeraufgabe(
            new EntfernteBenutzeraufgabeCommand(UserTaskTestdaten.taskId())
        );

        verify(tasklistRepositoryOutPort).entferne(UserTaskTestdaten.taskId());
        verifyNoInteractions(sendeBenutzeraufgabenBenachrichtigungOutPort);
    }

    @Test
    void throwsWhenStoredTaskCannotBeLoaded() {
        when(tasklistRepositoryOutPort.speichere(UserTaskTestdaten.offeneBenutzeraufgabe())).thenReturn(true);
        when(tasklistRepositoryOutPort.getTaskById(UserTaskTestdaten.taskId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.offeneBenutzeraufgabe())
        ))
            .isInstanceOf(TaskNichtGefundenException.class)
            .hasMessage("taskId verweist auf keine vorhandene Aufgabe: " + UserTaskTestdaten.TASK_ID);

        verifyNoInteractions(sendeBenutzeraufgabenBenachrichtigungOutPort);
    }

    @Test
    void propagatesNotificationFailureForTransactionRollback() {
        when(tasklistRepositoryOutPort.speichere(UserTaskTestdaten.offeneBenutzeraufgabe())).thenReturn(true);
        when(tasklistRepositoryOutPort.getTaskById(UserTaskTestdaten.taskId()))
            .thenReturn(Optional.of(UserTaskTestdaten.userTask()));
        doThrow(new IllegalStateException("mail down"))
            .when(sendeBenutzeraufgabenBenachrichtigungOutPort)
            .sendeBenutzeraufgabenBenachrichtigung(any(), any());

        assertThatThrownBy(() -> useCase.verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.offeneBenutzeraufgabe())
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("mail down");
    }

    @Test
    void rejectsNullActiveTaskCommand() {
        assertThatThrownBy(() -> useCase.verarbeiteAktiveBenutzeraufgabe(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("command darf nicht null sein");

        verifyNoInteractions(tasklistRepositoryOutPort, sendeBenutzeraufgabenBenachrichtigungOutPort);
    }

    @Test
    void rejectsNullTaskSnapshot() {
        assertThatThrownBy(() -> useCase.verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(null)
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("aufgabe darf nicht null sein");

        verifyNoInteractions(tasklistRepositoryOutPort, sendeBenutzeraufgabenBenachrichtigungOutPort);
    }

    @Test
    void rejectsNullRemoveCommand() {
        assertThatThrownBy(() -> useCase.verarbeiteEntfernteBenutzeraufgabe(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("command darf nicht null sein");

        verifyNoInteractions(tasklistRepositoryOutPort, sendeBenutzeraufgabenBenachrichtigungOutPort);
    }

    @Test
    void rejectsNullTaskIdForRemoveCommand() {
        assertThatThrownBy(() -> useCase.verarbeiteEntfernteBenutzeraufgabe(
            new EntfernteBenutzeraufgabeCommand(null)
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("taskId darf nicht null sein");

        verifyNoInteractions(tasklistRepositoryOutPort, sendeBenutzeraufgabenBenachrichtigungOutPort);
    }
}
