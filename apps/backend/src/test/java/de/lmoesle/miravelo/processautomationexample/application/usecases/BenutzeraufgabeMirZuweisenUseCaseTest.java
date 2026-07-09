package de.lmoesle.miravelo.processautomationexample.application.usecases;

import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzeraufgabeMirZuweisenInPort.WeiseBenutzeraufgabeMirZuCommand;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.AssignTaskOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.TasklistRepositoryOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.UrlaubsantragSpeichernOutPort;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.TaskNichtGefundenException;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.TaskZugriffVerweigertException;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskTestdaten;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class BenutzeraufgabeMirZuweisenUseCaseTest {

    private TasklistRepositoryOutPort tasklistRepositoryOutPort;
    private AssignTaskOutPort assignTaskOutPort;
    private UrlaubsantragSpeichernOutPort urlaubsantragSpeichernOutPort;
    private BenutzeraufgabeMirZuweisenUseCase benutzeraufgabeMirZuweisenUseCase;

    @BeforeEach
    void setUp() {
        tasklistRepositoryOutPort = mock(TasklistRepositoryOutPort.class);
        assignTaskOutPort = mock(AssignTaskOutPort.class);
        urlaubsantragSpeichernOutPort = mock(UrlaubsantragSpeichernOutPort.class);
        benutzeraufgabeMirZuweisenUseCase = new BenutzeraufgabeMirZuweisenUseCase(
            tasklistRepositoryOutPort,
            assignTaskOutPort,
            urlaubsantragSpeichernOutPort
        );
    }

    @Test
    void assignsTaskWhenCurrentUserIsCandidateUser() {
        when(tasklistRepositoryOutPort.getTaskById(UserTaskTestdaten.taskId()))
            .thenReturn(Optional.of(UserTaskTestdaten.userTask()));

        benutzeraufgabeMirZuweisenUseCase.weiseBenutzeraufgabeMirZu(
            new WeiseBenutzeraufgabeMirZuCommand(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId())
        );

        verify(tasklistRepositoryOutPort).getTaskById(UserTaskTestdaten.taskId());
        verify(assignTaskOutPort).assignTaskToUser(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId());
        final ArgumentCaptor<de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.Urlaubsantrag> urlaubsantragCaptor = ArgumentCaptor.forClass(
            de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.Urlaubsantrag.class
        );
        verify(urlaubsantragSpeichernOutPort).speichere(urlaubsantragCaptor.capture());
        assertThat(urlaubsantragCaptor.getValue().vorgesetzter()).isEqualTo(BenutzerTestdaten.ada());
        verifyNoMoreInteractions(tasklistRepositoryOutPort, urlaubsantragSpeichernOutPort);
    }

    @Test
    void throwsWhenTaskDoesNotExist() {
        when(tasklistRepositoryOutPort.getTaskById(UserTaskTestdaten.taskId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> benutzeraufgabeMirZuweisenUseCase.weiseBenutzeraufgabeMirZu(
            new WeiseBenutzeraufgabeMirZuCommand(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId())
        ))
            .isInstanceOf(TaskNichtGefundenException.class)
            .hasMessage("taskId verweist auf keine vorhandene Aufgabe: " + UserTaskTestdaten.TASK_ID);

        verifyNoInteractions(urlaubsantragSpeichernOutPort);
    }

    @Test
    void throwsWhenCurrentUserIsNotCandidateUser() {
        when(tasklistRepositoryOutPort.getTaskById(UserTaskTestdaten.secondTaskId()))
            .thenReturn(Optional.of(UserTaskTestdaten.secondUserTask()));

        assertThatThrownBy(() -> benutzeraufgabeMirZuweisenUseCase.weiseBenutzeraufgabeMirZu(
            new WeiseBenutzeraufgabeMirZuCommand(UserTaskTestdaten.secondTaskId(), BenutzerTestdaten.adaId())
        ))
            .isInstanceOf(TaskZugriffVerweigertException.class)
            .hasMessage("Aktueller Benutzer hat keinen Zugriff auf Aufgabe: " + UserTaskTestdaten.SECOND_TASK_ID);

        verify(tasklistRepositoryOutPort).getTaskById(UserTaskTestdaten.secondTaskId());
        verifyNoMoreInteractions(tasklistRepositoryOutPort);
        verifyNoInteractions(urlaubsantragSpeichernOutPort);
    }

    @Test
    void rejectsNullCommand() {
        assertThatThrownBy(() -> benutzeraufgabeMirZuweisenUseCase.weiseBenutzeraufgabeMirZu(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("command darf nicht null sein");
    }

    @Test
    void rejectsNullTaskId() {
        assertThatThrownBy(() -> benutzeraufgabeMirZuweisenUseCase.weiseBenutzeraufgabeMirZu(
            new WeiseBenutzeraufgabeMirZuCommand(null, BenutzerTestdaten.adaId())
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("taskId darf nicht null sein");
    }

    @Test
    void rejectsNullUserId() {
        assertThatThrownBy(() -> benutzeraufgabeMirZuweisenUseCase.weiseBenutzeraufgabeMirZu(
            new WeiseBenutzeraufgabeMirZuCommand(UserTaskTestdaten.taskId(), null)
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("benutzerId darf nicht null sein");

        verifyNoInteractions(tasklistRepositoryOutPort, urlaubsantragSpeichernOutPort);
    }
}
