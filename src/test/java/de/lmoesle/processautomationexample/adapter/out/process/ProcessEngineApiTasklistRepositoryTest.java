package de.lmoesle.processautomationexample.adapter.out.process;

import de.lmoesle.processautomationexample.application.ports.out.BenutzerRepositoryOutPort;
import de.lmoesle.processautomationexample.application.ports.out.UrlaubsantraegeLadenOutPort;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.processautomationexample.domain.tasklist.UserTask;
import de.lmoesle.processautomationexample.domain.tasklist.UserTaskTestdaten;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import dev.bpmcrafters.processengineapi.task.TaskInformation;
import dev.bpmcrafters.processengineapi.task.support.UserTaskSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProcessEngineApiTasklistRepositoryTest {

    private ProcessEngineApiTasklistRepository processEngineApiTasklistRepository;
    private UserTaskSupport userTaskSupport;
    private UrlaubsantraegeLadenOutPort urlaubsantraegeLadenOutPort;
    private BenutzerRepositoryOutPort benutzerRepositoryOutPort;

    @BeforeEach
    void setUp() {
        userTaskSupport = new UserTaskSupport();
        urlaubsantraegeLadenOutPort = mock(UrlaubsantraegeLadenOutPort.class);
        benutzerRepositoryOutPort = mock(BenutzerRepositoryOutPort.class);
        processEngineApiTasklistRepository = new ProcessEngineApiTasklistRepository(
            userTaskSupport,
            urlaubsantraegeLadenOutPort,
            benutzerRepositoryOutPort
        );
    }

    @Test
    void loadsAllTasksWithEnrichedDomainData() {
        stubReferenzen();
        liefereTask(UserTaskTestdaten.taskId(), UserTaskTestdaten.meta(), UserTaskTestdaten.payload());
        liefereTask(UserTaskTestdaten.secondTaskId(), UserTaskTestdaten.secondMeta(), UserTaskTestdaten.secondPayload());

        var tasks = processEngineApiTasklistRepository.getAllTasks();

        assertThat(tasks)
            .extracting(task -> task.id().value())
            .containsExactlyInAnyOrder(UserTaskTestdaten.TASK_ID, UserTaskTestdaten.SECOND_TASK_ID);
        assertThat(tasks)
            .extracting(task -> task.urlaubsantrag().id().value())
            .containsExactlyInAnyOrder(
                UrlaubsantragTestData.VACATION_REQUEST_UUID,
                UrlaubsantragTestData.SECOND_VACATION_REQUEST_UUID
            );
        assertThat(tasks)
            .extracting(UserTask::candidateUsers)
            .containsExactlyInAnyOrder(
                java.util.List.of(BenutzerTestdaten.ada(), BenutzerTestdaten.carla()),
                java.util.List.of(BenutzerTestdaten.carla())
            );
        assertThat(tasks)
            .extracting(UserTask::bearbeiter)
            .containsExactlyInAnyOrder(BenutzerTestdaten.ada(), BenutzerTestdaten.carla());
    }

    @Test
    void loadsTaskByIdWithEnrichedDomainData() {
        stubReferenzen();
        liefereTask(UserTaskTestdaten.taskId(), UserTaskTestdaten.meta(), UserTaskTestdaten.payload());

        var task = processEngineApiTasklistRepository.getTaskById(UserTaskTestdaten.taskId()).orElseThrow();

        assertThat(task.id().value()).isEqualTo(UserTaskTestdaten.TASK_ID);
        assertThat(task.urlaubsantrag().id().value()).isEqualTo(UrlaubsantragTestData.VACATION_REQUEST_UUID);
        assertThat(task.urlaubsantrag().antragsteller()).isEqualTo(UrlaubsantragTestData.antragsteller());
        assertThat(task.candidateUsers()).containsExactly(BenutzerTestdaten.ada(), BenutzerTestdaten.carla());
        assertThat(task.bearbeiter()).isEqualTo(BenutzerTestdaten.ada());
    }

    @Test
    void returnsEmptyOptionalWhenTaskDoesNotExist() {
        assertThat(processEngineApiTasklistRepository.getTaskById(UserTaskTestdaten.taskId())).isEmpty();
    }

    @Test
    void rejectsNullTaskId() {
        assertThatThrownBy(() -> processEngineApiTasklistRepository.getTaskById(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("taskId darf nicht null sein");
    }

    private void liefereTask(
        de.lmoesle.processautomationexample.domain.tasklist.UserTaskId taskId,
        java.util.Map<String, String> meta,
        java.util.Map<String, Object> payload
    ) {
        userTaskSupport.accept(new TaskInformation(taskId.value(), meta), payload);
    }

    private void stubReferenzen() {
        when(urlaubsantraegeLadenOutPort.findeNachId(UrlaubsantragTestData.urlaubsantragId()))
            .thenReturn(Optional.of(UrlaubsantragTestData.urlaubsantragWithStartedProcess()));
        when(urlaubsantraegeLadenOutPort.findeNachId(UrlaubsantragTestData.secondUrlaubsantragId()))
            .thenReturn(Optional.of(UrlaubsantragTestData.secondUrlaubsantrag(BenutzerTestdaten.ada(), BenutzerTestdaten.carla())));
        when(benutzerRepositoryOutPort.findeNachId(BenutzerTestdaten.adaId()))
            .thenReturn(Optional.of(BenutzerTestdaten.ada()));
        when(benutzerRepositoryOutPort.findeNachId(BenutzerTestdaten.carlaId()))
            .thenReturn(Optional.of(BenutzerTestdaten.carla()));
    }
}
