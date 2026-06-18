package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort.AktiveBenutzeraufgabeCommand;
import de.lmoesle.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort.EntfernteBenutzeraufgabeCommand;
import de.lmoesle.processautomationexample.application.ports.out.AktiveBenutzeraufgabenOutPort;
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

    private AktiveBenutzeraufgabenOutPort aktiveBenutzeraufgabenOutPort;
    private TasklistRepositoryOutPort tasklistRepositoryOutPort;
    private SendeBenutzeraufgabenBenachrichtigungOutPort sendeBenutzeraufgabenBenachrichtigungOutPort;
    private SendeBenutzeraufgabenBenachrichtigungUseCase useCase;

    @BeforeEach
    void setUp() {
        aktiveBenutzeraufgabenOutPort = mock(AktiveBenutzeraufgabenOutPort.class);
        tasklistRepositoryOutPort = mock(TasklistRepositoryOutPort.class);
        sendeBenutzeraufgabenBenachrichtigungOutPort = mock(SendeBenutzeraufgabenBenachrichtigungOutPort.class);
        useCase = new SendeBenutzeraufgabenBenachrichtigungUseCase(
            aktiveBenutzeraufgabenOutPort,
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
        when(aktiveBenutzeraufgabenOutPort.speichereWennNeu(UserTaskTestdaten.taskId())).thenReturn(true);
        when(tasklistRepositoryOutPort.getTaskById(UserTaskTestdaten.taskId())).thenReturn(Optional.of(userTask));

        useCase.verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.taskId())
        );

        InOrder inOrder = inOrder(aktiveBenutzeraufgabenOutPort, tasklistRepositoryOutPort, sendeBenutzeraufgabenBenachrichtigungOutPort);
        inOrder.verify(aktiveBenutzeraufgabenOutPort).speichereWennNeu(UserTaskTestdaten.taskId());
        inOrder.verify(tasklistRepositoryOutPort).getTaskById(UserTaskTestdaten.taskId());
        inOrder.verify(sendeBenutzeraufgabenBenachrichtigungOutPort)
            .sendeBenutzeraufgabenBenachrichtigung(userTask, List.of(BenutzerTestdaten.ada(), BenutzerTestdaten.carla()));
        verifyNoMoreInteractions(aktiveBenutzeraufgabenOutPort, tasklistRepositoryOutPort, sendeBenutzeraufgabenBenachrichtigungOutPort);
    }

    @Test
    void doesNothingWhenTaskHasNoCandidateUsers() {
        UserTask userTask = new UserTask(
            UserTaskTestdaten.taskId(),
            UserTaskTestdaten.userTask().urlaubsantrag(),
            List.of(),
            BenutzerTestdaten.ada()
        );
        when(aktiveBenutzeraufgabenOutPort.speichereWennNeu(UserTaskTestdaten.taskId())).thenReturn(true);
        when(tasklistRepositoryOutPort.getTaskById(UserTaskTestdaten.taskId())).thenReturn(Optional.of(userTask));

        useCase.verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.taskId())
        );

        verify(aktiveBenutzeraufgabenOutPort).speichereWennNeu(UserTaskTestdaten.taskId());
        verify(tasklistRepositoryOutPort).getTaskById(UserTaskTestdaten.taskId());
        verify(sendeBenutzeraufgabenBenachrichtigungOutPort).sendeBenutzeraufgabenBenachrichtigung(userTask, List.of());
    }

    @Test
    void ignoresAlreadyRegisteredTask() {
        when(aktiveBenutzeraufgabenOutPort.speichereWennNeu(UserTaskTestdaten.taskId())).thenReturn(false);

        useCase.verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.taskId())
        );

        verify(aktiveBenutzeraufgabenOutPort).speichereWennNeu(UserTaskTestdaten.taskId());
        verifyNoInteractions(tasklistRepositoryOutPort, sendeBenutzeraufgabenBenachrichtigungOutPort);
    }

    @Test
    void removesActiveTaskWhenTaskIsRemoved() {
        useCase.verarbeiteEntfernteBenutzeraufgabe(
            new EntfernteBenutzeraufgabeCommand(UserTaskTestdaten.taskId())
        );

        verify(aktiveBenutzeraufgabenOutPort).entferne(UserTaskTestdaten.taskId());
        verifyNoInteractions(tasklistRepositoryOutPort, sendeBenutzeraufgabenBenachrichtigungOutPort);
    }

    @Test
    void throwsWhenTaskDoesNotExist() {
        when(aktiveBenutzeraufgabenOutPort.speichereWennNeu(UserTaskTestdaten.taskId())).thenReturn(true);
        when(tasklistRepositoryOutPort.getTaskById(UserTaskTestdaten.taskId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.taskId())
        ))
            .isInstanceOf(TaskNichtGefundenException.class)
            .hasMessage("taskId verweist auf keine vorhandene Aufgabe: " + UserTaskTestdaten.TASK_ID);

        verify(aktiveBenutzeraufgabenOutPort).entferne(UserTaskTestdaten.taskId());
        verifyNoInteractions(sendeBenutzeraufgabenBenachrichtigungOutPort);
    }

    @Test
    void removesActiveTaskMarkerWhenNotificationFails() {
        when(aktiveBenutzeraufgabenOutPort.speichereWennNeu(UserTaskTestdaten.taskId())).thenReturn(true);
        when(tasklistRepositoryOutPort.getTaskById(UserTaskTestdaten.taskId()))
            .thenReturn(Optional.of(UserTaskTestdaten.userTask()));
        doThrow(new IllegalStateException("mail down"))
            .when(sendeBenutzeraufgabenBenachrichtigungOutPort)
            .sendeBenutzeraufgabenBenachrichtigung(any(), any());

        assertThatThrownBy(() -> useCase.verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(UserTaskTestdaten.taskId())
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("mail down");

        verify(aktiveBenutzeraufgabenOutPort).entferne(UserTaskTestdaten.taskId());
    }

    @Test
    void rejectsNullCommand() {
        assertThatThrownBy(() -> useCase.verarbeiteAktiveBenutzeraufgabe(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("command darf nicht null sein");

        verifyNoInteractions(aktiveBenutzeraufgabenOutPort, tasklistRepositoryOutPort, sendeBenutzeraufgabenBenachrichtigungOutPort);
    }

    @Test
    void rejectsNullTaskId() {
        assertThatThrownBy(() -> useCase.verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(null)
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("taskId darf nicht null sein");

        verifyNoInteractions(aktiveBenutzeraufgabenOutPort, tasklistRepositoryOutPort, sendeBenutzeraufgabenBenachrichtigungOutPort);
    }

    @Test
    void rejectsNullRemoveCommand() {
        assertThatThrownBy(() -> useCase.verarbeiteEntfernteBenutzeraufgabe(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("command darf nicht null sein");

        verifyNoInteractions(aktiveBenutzeraufgabenOutPort, tasklistRepositoryOutPort, sendeBenutzeraufgabenBenachrichtigungOutPort);
    }

    @Test
    void rejectsNullTaskIdForRemoveCommand() {
        assertThatThrownBy(() -> useCase.verarbeiteEntfernteBenutzeraufgabe(
            new EntfernteBenutzeraufgabeCommand(null)
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("taskId darf nicht null sein");

        verifyNoInteractions(aktiveBenutzeraufgabenOutPort, tasklistRepositoryOutPort, sendeBenutzeraufgabenBenachrichtigungOutPort);
    }
}
