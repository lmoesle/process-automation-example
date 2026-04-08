package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.adapter.out.process.ProcessEngineApiTasklistAdapter;
import de.lmoesle.processautomationexample.application.ports.in.GenehmigungVomVorgesetztenInPort.GenehmigungVomVorgesetztenCommand;
import de.lmoesle.processautomationexample.application.ports.out.SendeBenachrichtigungOutPort;
import de.lmoesle.processautomationexample.application.ports.out.TasklistRepositoryOutPort;
import de.lmoesle.processautomationexample.application.ports.out.UrlaubsantragSpeichernOutPort;
import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.processautomationexample.domain.tasklist.TaskNichtGefundenException;
import de.lmoesle.processautomationexample.domain.tasklist.TaskZugriffVerweigertException;
import de.lmoesle.processautomationexample.domain.tasklist.UserTask;
import de.lmoesle.processautomationexample.domain.tasklist.UserTaskTestdaten;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragStatus;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class GenehmigungVomVorgesetztenUseCaseTest {

    private TasklistRepositoryOutPort tasklistRepositoryOutPort;
    private ProcessEngineApiTasklistAdapter processEngineApiTasklistAdapter;
    private UrlaubsantragSpeichernOutPort urlaubsantragSpeichernOutPort;
    private SendeBenachrichtigungOutPort sendeBenachrichtigungOutPort;
    private GenehmigungVomVorgesetztenUseCase genehmigungVomVorgesetztenUseCase;

    @BeforeEach
    void setUp() {
        tasklistRepositoryOutPort = mock(TasklistRepositoryOutPort.class);
        processEngineApiTasklistAdapter = mock(ProcessEngineApiTasklistAdapter.class);
        urlaubsantragSpeichernOutPort = mock(UrlaubsantragSpeichernOutPort.class);
        sendeBenachrichtigungOutPort = mock(SendeBenachrichtigungOutPort.class);
        genehmigungVomVorgesetztenUseCase = new GenehmigungVomVorgesetztenUseCase(
            tasklistRepositoryOutPort,
            processEngineApiTasklistAdapter,
            urlaubsantragSpeichernOutPort,
            sendeBenachrichtigungOutPort
        );
    }

    @Test
    void approvesVacationRequestWhenCurrentUserIsCandidateUserWithoutManualAssignment() {
        UserTask task = taskMitUrlaubsantragInVorgesetztenpruefung(UserTaskTestdaten.taskId(), null);
        when(tasklistRepositoryOutPort.getTaskById(UserTaskTestdaten.taskId())).thenReturn(Optional.of(task));

        genehmigungVomVorgesetztenUseCase.entscheideGenehmigungVomVorgesetzten(
            new GenehmigungVomVorgesetztenCommand(
                UserTaskTestdaten.taskId(),
                BenutzerTestdaten.adaId(),
                true,
                "Vertretung ist organisiert."
            )
        );

        InOrder inOrder = inOrder(tasklistRepositoryOutPort, urlaubsantragSpeichernOutPort, processEngineApiTasklistAdapter, sendeBenachrichtigungOutPort);
        inOrder.verify(tasklistRepositoryOutPort).getTaskById(UserTaskTestdaten.taskId());
        ArgumentCaptor<Urlaubsantrag> savedCaptor = ArgumentCaptor.forClass(Urlaubsantrag.class);
        inOrder.verify(urlaubsantragSpeichernOutPort).speichere(savedCaptor.capture());
        inOrder.verify(processEngineApiTasklistAdapter).completeTask(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId(), true);
        inOrder.verify(sendeBenachrichtigungOutPort).sendeBenachrichtigung(savedCaptor.getValue());

        assertThat(savedCaptor.getValue().status()).isEqualTo(UrlaubsantragStatus.GENEHMIGT);
        assertThat(savedCaptor.getValue().vorgesetzter()).isEqualTo(BenutzerTestdaten.ada());
        assertThat(savedCaptor.getValue().statusHistorie()).last().satisfies(entry -> {
            assertThat(entry.status()).isEqualTo(UrlaubsantragStatus.GENEHMIGT);
            assertThat(entry.kommentar()).isEqualTo("Vertretung ist organisiert.");
        });
    }

    @Test
    void rejectsVacationRequestWhenCurrentUserIsBearbeiter() {
        UserTask task = taskMitUrlaubsantragInVorgesetztenpruefung(UserTaskTestdaten.taskId(), BenutzerTestdaten.ada());
        when(tasklistRepositoryOutPort.getTaskById(UserTaskTestdaten.taskId())).thenReturn(Optional.of(task));

        genehmigungVomVorgesetztenUseCase.entscheideGenehmigungVomVorgesetzten(
            new GenehmigungVomVorgesetztenCommand(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId(), false, null)
        );

        verify(urlaubsantragSpeichernOutPort).speichere(task.urlaubsantrag());
        verify(processEngineApiTasklistAdapter).completeTask(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId(), false);
        verify(sendeBenachrichtigungOutPort).sendeBenachrichtigung(task.urlaubsantrag());
        assertThat(task.urlaubsantrag().status()).isEqualTo(UrlaubsantragStatus.ABGELEHNT);
    }

    @Test
    void throwsWhenCurrentUserCannotSeeTask() {
        when(tasklistRepositoryOutPort.getTaskById(UserTaskTestdaten.secondTaskId()))
            .thenReturn(Optional.of(taskMitUrlaubsantragInVorgesetztenpruefung(
                UserTaskTestdaten.secondTaskId(),
                List.of(BenutzerTestdaten.carla()),
                BenutzerTestdaten.carla()
            )));

        assertThatThrownBy(() -> genehmigungVomVorgesetztenUseCase.entscheideGenehmigungVomVorgesetzten(
            new GenehmigungVomVorgesetztenCommand(UserTaskTestdaten.secondTaskId(), BenutzerTestdaten.adaId(), true, "ok")
        ))
            .isInstanceOf(TaskZugriffVerweigertException.class)
            .hasMessage("Aktueller Benutzer hat keinen Zugriff auf Aufgabe: " + UserTaskTestdaten.SECOND_TASK_ID);

        verify(tasklistRepositoryOutPort).getTaskById(UserTaskTestdaten.secondTaskId());
        verifyNoInteractions(urlaubsantragSpeichernOutPort);
        verifyNoInteractions(sendeBenachrichtigungOutPort);
        verify(processEngineApiTasklistAdapter, never()).completeTask(any(), any(), anyBoolean());
    }

    @Test
    void throwsWhenTaskDoesNotExist() {
        when(tasklistRepositoryOutPort.getTaskById(UserTaskTestdaten.taskId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> genehmigungVomVorgesetztenUseCase.entscheideGenehmigungVomVorgesetzten(
            new GenehmigungVomVorgesetztenCommand(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId(), true, null)
        ))
            .isInstanceOf(TaskNichtGefundenException.class)
            .hasMessage("taskId verweist auf keine vorhandene Aufgabe: " + UserTaskTestdaten.TASK_ID);

        verifyNoInteractions(urlaubsantragSpeichernOutPort, sendeBenachrichtigungOutPort);
    }

    @Test
    void throwsWhenTaskHasNoUrlaubsantrag() {
        when(tasklistRepositoryOutPort.getTaskById(UserTaskTestdaten.taskId()))
            .thenReturn(Optional.of(new UserTask(
                UserTaskTestdaten.taskId(),
                null,
                List.of(BenutzerTestdaten.ada(), BenutzerTestdaten.carla()),
                BenutzerTestdaten.ada()
            )));

        assertThatThrownBy(() -> genehmigungVomVorgesetztenUseCase.entscheideGenehmigungVomVorgesetzten(
            new GenehmigungVomVorgesetztenCommand(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId(), true, null)
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("taskId verweist auf keinen zugeordneten Urlaubsantrag");

        verifyNoInteractions(urlaubsantragSpeichernOutPort, sendeBenachrichtigungOutPort);
    }

    @Test
    void rejectsNullCommand() {
        assertThatThrownBy(() -> genehmigungVomVorgesetztenUseCase.entscheideGenehmigungVomVorgesetzten(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("command darf nicht null sein");

        verifyNoInteractions(tasklistRepositoryOutPort, urlaubsantragSpeichernOutPort, sendeBenachrichtigungOutPort);
    }

    @Test
    void rejectsNullTaskId() {
        assertThatThrownBy(() -> genehmigungVomVorgesetztenUseCase.entscheideGenehmigungVomVorgesetzten(
            new GenehmigungVomVorgesetztenCommand(null, BenutzerTestdaten.adaId(), true, null)
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("taskId darf nicht null sein");

        verifyNoInteractions(tasklistRepositoryOutPort, urlaubsantragSpeichernOutPort, sendeBenachrichtigungOutPort);
    }

    @Test
    void rejectsNullUserId() {
        assertThatThrownBy(() -> genehmigungVomVorgesetztenUseCase.entscheideGenehmigungVomVorgesetzten(
            new GenehmigungVomVorgesetztenCommand(UserTaskTestdaten.taskId(), null, true, null)
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("benutzerId darf nicht null sein");

        verifyNoInteractions(tasklistRepositoryOutPort, urlaubsantragSpeichernOutPort, sendeBenachrichtigungOutPort);
    }

    @Test
    void rejectsBlankCommentFromDomainValidation() {
        when(tasklistRepositoryOutPort.getTaskById(UserTaskTestdaten.taskId()))
            .thenReturn(Optional.of(taskMitUrlaubsantragInVorgesetztenpruefung(UserTaskTestdaten.taskId(), BenutzerTestdaten.ada())));

        assertThatThrownBy(() -> genehmigungVomVorgesetztenUseCase.entscheideGenehmigungVomVorgesetzten(
            new GenehmigungVomVorgesetztenCommand(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId(), true, " ")
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("kommentar darf nicht leer sein");

        verifyNoInteractions(urlaubsantragSpeichernOutPort);
        verifyNoInteractions(sendeBenachrichtigungOutPort);
        verify(processEngineApiTasklistAdapter, never()).completeTask(any(), any(), anyBoolean());
    }

    private static UserTask taskMitUrlaubsantragInVorgesetztenpruefung(
        de.lmoesle.processautomationexample.domain.tasklist.UserTaskId taskId,
        Benutzer bearbeiter
    ) {
        return taskMitUrlaubsantragInVorgesetztenpruefung(
            taskId,
            List.of(BenutzerTestdaten.ada(), BenutzerTestdaten.carla()),
            bearbeiter
        );
    }

    private static UserTask taskMitUrlaubsantragInVorgesetztenpruefung(
        de.lmoesle.processautomationexample.domain.tasklist.UserTaskId taskId,
        List<Benutzer> candidateUsers,
        Benutzer bearbeiter
    ) {
        Urlaubsantrag urlaubsantrag = UrlaubsantragTestData.urlaubsantragWithStartedProcess();
        urlaubsantrag.starteAutomatischePruefung();
        urlaubsantrag.schliesseAutomatischePruefungAb(true);
        return new UserTask(
            taskId,
            urlaubsantrag,
            candidateUsers,
            bearbeiter
        );
    }
}
