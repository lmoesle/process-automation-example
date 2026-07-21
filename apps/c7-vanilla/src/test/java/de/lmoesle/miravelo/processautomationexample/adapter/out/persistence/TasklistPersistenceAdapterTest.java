package de.lmoesle.miravelo.processautomationexample.adapter.out.persistence;

import de.lmoesle.miravelo.processautomationexample.adapter.out.persistence.entities.OffeneBenutzeraufgabeEntity;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.BenutzerRepositoryOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.UrlaubsantraegeLadenOutPort;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TasklistPersistenceAdapterTest {

    private OffeneBenutzeraufgabeJpaRepository offeneBenutzeraufgabeJpaRepository;
    private UrlaubsantraegeLadenOutPort urlaubsantraegeLadenOutPort;
    private BenutzerRepositoryOutPort benutzerRepositoryOutPort;
    private TasklistPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        offeneBenutzeraufgabeJpaRepository = mock(OffeneBenutzeraufgabeJpaRepository.class);
        urlaubsantraegeLadenOutPort = mock(UrlaubsantraegeLadenOutPort.class);
        benutzerRepositoryOutPort = mock(BenutzerRepositoryOutPort.class);
        adapter = new TasklistPersistenceAdapter(
            offeneBenutzeraufgabeJpaRepository,
            urlaubsantraegeLadenOutPort,
            benutzerRepositoryOutPort
        );
    }

    @Test
    void storesCompleteTaskProjectionAndReportsNewTask() {
        whenInsertReturns(1);

        final boolean neueAufgabe = adapter.speichere(UserTaskTestdaten.offeneBenutzeraufgabe());

        verifyAtomicInsert();
        final ArgumentCaptor<OffeneBenutzeraufgabeEntity> entityCaptor = ArgumentCaptor.captor();
        verify(offeneBenutzeraufgabeJpaRepository).save(entityCaptor.capture());
        assertThat(neueAufgabe).isTrue();
        assertThat(entityCaptor.getValue())
            .returns(UserTaskTestdaten.TASK_ID, OffeneBenutzeraufgabeEntity::getTaskId)
            .returns(UserTaskTestdaten.TASK_NAME, OffeneBenutzeraufgabeEntity::getTaskName)
            .returns(UrlaubsantragTestData.PROCESS_INSTANCE_ID_VALUE, OffeneBenutzeraufgabeEntity::getProzessinstanzId)
            .returns(UserTaskTestdaten.BUSINESS_KEY, OffeneBenutzeraufgabeEntity::getBusinessKey)
            .returns(BenutzerTestdaten.ADA_UUID, OffeneBenutzeraufgabeEntity::getAssignee);
        assertThat(entityCaptor.getValue().getCandidateUserIds())
            .containsExactly(BenutzerTestdaten.ADA_UUID, BenutzerTestdaten.CARLA_UUID);
    }

    @Test
    void updatesExistingTaskProjectionWithoutReportingNewTask() {
        whenInsertReturns(0);

        final boolean neueAufgabe = adapter.speichere(UserTaskTestdaten.offeneBenutzeraufgabe());

        assertThat(neueAufgabe).isFalse();
        verifyAtomicInsert();
        verify(offeneBenutzeraufgabeJpaRepository).save(org.mockito.ArgumentMatchers.any(OffeneBenutzeraufgabeEntity.class));
    }

    @Test
    void removesCompletedTaskProjection() {
        adapter.entferne(UserTaskTestdaten.taskId());

        verify(offeneBenutzeraufgabeJpaRepository).deleteById(UserTaskTestdaten.TASK_ID);
    }

    @Test
    void loadsVisibleTasksFromOwnTaskTable() {
        when(offeneBenutzeraufgabeJpaRepository.findAllVisibleFor(BenutzerTestdaten.ADA_UUID))
            .thenReturn(List.of(entity()));
        stubDomainReferences();

        final var tasks = adapter.getAllTasks(BenutzerTestdaten.adaId());

        assertThat(tasks).singleElement().satisfies(task -> {
            assertThat(task.id()).isEqualTo(UserTaskTestdaten.taskId());
            assertThat(task.urlaubsantrag().id()).isEqualTo(UrlaubsantragTestData.urlaubsantragId());
            assertThat(task.candidateUsers()).containsExactly(BenutzerTestdaten.ada(), BenutzerTestdaten.carla());
            assertThat(task.bearbeiter()).isEqualTo(BenutzerTestdaten.ada());
        });
    }

    @Test
    void loadsTaskByIdWithoutCamundaTaskService() {
        when(offeneBenutzeraufgabeJpaRepository.findById(UserTaskTestdaten.TASK_ID))
            .thenReturn(Optional.of(entity()));
        stubDomainReferences();

        final var task = adapter.getTaskById(UserTaskTestdaten.taskId()).orElseThrow();

        assertThat(task.id()).isEqualTo(UserTaskTestdaten.taskId());
        assertThat(task.urlaubsantrag().id()).isEqualTo(UrlaubsantragTestData.urlaubsantragId());
        assertThat(task.candidateUsers()).containsExactly(BenutzerTestdaten.ada(), BenutzerTestdaten.carla());
        assertThat(task.bearbeiter()).isEqualTo(BenutzerTestdaten.ada());
    }

    @Test
    void loadsVisibleTaskByIdFromOwnTaskTable() {
        when(offeneBenutzeraufgabeJpaRepository.findVisibleById(UserTaskTestdaten.TASK_ID, BenutzerTestdaten.CARLA_UUID))
            .thenReturn(Optional.of(entity()));
        stubDomainReferences();

        final var task = adapter.getTaskById(UserTaskTestdaten.taskId(), BenutzerTestdaten.carlaId());

        assertThat(task).isPresent();
    }

    @Test
    void returnsEmptyWhenTaskIsNotVisibleForUser() {
        when(offeneBenutzeraufgabeJpaRepository.findVisibleById(UserTaskTestdaten.TASK_ID, BenutzerTestdaten.CARLA_UUID))
            .thenReturn(Optional.empty());

        final var task = adapter.getTaskById(UserTaskTestdaten.taskId(), BenutzerTestdaten.carlaId());

        assertThat(task).isEmpty();
        verifyNoInteractions(urlaubsantraegeLadenOutPort, benutzerRepositoryOutPort);
    }

    @Test
    void rejectsNullArguments() {
        assertThatThrownBy(() -> adapter.speichere(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("aufgabe darf nicht null sein");
        assertThatThrownBy(() -> adapter.entferne(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("taskId darf nicht null sein");
        assertThatThrownBy(() -> adapter.getAllTasks(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("benutzerId darf nicht null sein");
        assertThatThrownBy(() -> adapter.getTaskById(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("taskId darf nicht null sein");
        assertThatThrownBy(() -> adapter.getTaskById(UserTaskTestdaten.taskId(), null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("benutzerId darf nicht null sein");
    }

    private OffeneBenutzeraufgabeEntity entity() {
        return new OffeneBenutzeraufgabeEntity(
            UserTaskTestdaten.TASK_ID,
            BenutzerTestdaten.ADA_UUID,
            UserTaskTestdaten.TASK_NAME,
            UrlaubsantragTestData.PROCESS_INSTANCE_ID_VALUE,
            UserTaskTestdaten.BUSINESS_KEY,
            new LinkedHashSet<>(List.of(BenutzerTestdaten.ADA_UUID, BenutzerTestdaten.CARLA_UUID))
        );
    }

    private void stubDomainReferences() {
        when(urlaubsantraegeLadenOutPort.findeNachId(UrlaubsantragTestData.urlaubsantragId()))
            .thenReturn(Optional.of(UrlaubsantragTestData.urlaubsantragWithStartedProcess()));
        when(benutzerRepositoryOutPort.findeNachId(BenutzerTestdaten.adaId()))
            .thenReturn(Optional.of(BenutzerTestdaten.ada()));
        when(benutzerRepositoryOutPort.findeNachId(BenutzerTestdaten.carlaId()))
            .thenReturn(Optional.of(BenutzerTestdaten.carla()));
    }

    private void whenInsertReturns(int insertedRows) {
        when(offeneBenutzeraufgabeJpaRepository.insertiereWennNichtVorhanden(
            UserTaskTestdaten.TASK_ID,
            BenutzerTestdaten.ADA_UUID,
            UserTaskTestdaten.TASK_NAME,
            UrlaubsantragTestData.PROCESS_INSTANCE_ID_VALUE,
            UserTaskTestdaten.BUSINESS_KEY
        )).thenReturn(insertedRows);
    }

    private void verifyAtomicInsert() {
        verify(offeneBenutzeraufgabeJpaRepository).insertiereWennNichtVorhanden(
            UserTaskTestdaten.TASK_ID,
            BenutzerTestdaten.ADA_UUID,
            UserTaskTestdaten.TASK_NAME,
            UrlaubsantragTestData.PROCESS_INSTANCE_ID_VALUE,
            UserTaskTestdaten.BUSINESS_KEY
        );
    }
}
