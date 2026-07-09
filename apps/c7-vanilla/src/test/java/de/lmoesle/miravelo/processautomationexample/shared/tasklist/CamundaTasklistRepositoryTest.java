package de.lmoesle.miravelo.processautomationexample.shared.tasklist;

import de.lmoesle.miravelo.processautomationexample.application.ports.out.BenutzerRepositoryOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.UrlaubsantraegeLadenOutPort;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTask;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskId;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CamundaTasklistRepositoryTest {

    private TasklistRepository tasklistRepository;
    private TaskService taskService;
    private UrlaubsantraegeLadenOutPort urlaubsantraegeLadenOutPort;
    private BenutzerRepositoryOutPort benutzerRepositoryOutPort;

    @BeforeEach
    void setUp() {
        taskService = mock(TaskService.class);
        urlaubsantraegeLadenOutPort = mock(UrlaubsantraegeLadenOutPort.class);
        benutzerRepositoryOutPort = mock(BenutzerRepositoryOutPort.class);
        tasklistRepository = new TasklistRepository(
            taskService,
            urlaubsantraegeLadenOutPort,
            benutzerRepositoryOutPort
        );
    }

    @Test
    void loadsAllTasksWithEnrichedDomainData() {
        stubReferenzen();
        final Task firstTask = task(UserTaskTestdaten.taskId(), BenutzerTestdaten.ADA_UUID.toString());
        final Task secondTask = task(UserTaskTestdaten.secondTaskId(), BenutzerTestdaten.CARLA_UUID.toString());
        stubAllTasks(firstTask, secondTask);
        stubTaskDetails(
            UserTaskTestdaten.taskId(),
            UserTaskTestdaten.payload(),
            List.of(BenutzerTestdaten.ADA_UUID.toString(), BenutzerTestdaten.CARLA_UUID.toString())
        );
        stubTaskDetails(
            UserTaskTestdaten.secondTaskId(),
            UserTaskTestdaten.secondPayload(),
            List.of(BenutzerTestdaten.CARLA_UUID.toString())
        );

        final var tasks = tasklistRepository.getAllTasks(BenutzerTestdaten.carlaId());

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
                List.of(BenutzerTestdaten.ada(), BenutzerTestdaten.carla()),
                List.of(BenutzerTestdaten.carla())
            );
        assertThat(tasks)
            .extracting(UserTask::bearbeiter)
            .containsExactlyInAnyOrder(BenutzerTestdaten.ada(), BenutzerTestdaten.carla());
    }

    @Test
    void filtersTasksByCurrentUserVisibility() {
        stubReferenzen();
        final Task firstTask = task(UserTaskTestdaten.taskId(), BenutzerTestdaten.ADA_UUID.toString());
        final Task secondTask = task(UserTaskTestdaten.secondTaskId(), BenutzerTestdaten.CARLA_UUID.toString());
        stubAllTasks(firstTask, secondTask);
        stubTaskDetails(
            UserTaskTestdaten.taskId(),
            UserTaskTestdaten.payload(),
            List.of(BenutzerTestdaten.ADA_UUID.toString(), BenutzerTestdaten.CARLA_UUID.toString())
        );
        stubTaskDetails(
            UserTaskTestdaten.secondTaskId(),
            UserTaskTestdaten.secondPayload(),
            List.of(BenutzerTestdaten.CARLA_UUID.toString())
        );

        final var tasks = tasklistRepository.getAllTasks(BenutzerTestdaten.adaId());

        assertThat(tasks)
            .extracting(task -> task.id().value())
            .containsExactly(UserTaskTestdaten.TASK_ID);
    }

    @Test
    void loadsTaskByIdWithEnrichedDomainData() {
        stubReferenzen();
        final Task task = task(UserTaskTestdaten.taskId(), BenutzerTestdaten.ADA_UUID.toString());
        stubTaskById(UserTaskTestdaten.taskId(), task);
        stubTaskDetails(
            UserTaskTestdaten.taskId(),
            UserTaskTestdaten.payload(),
            List.of(BenutzerTestdaten.ADA_UUID.toString(), BenutzerTestdaten.CARLA_UUID.toString())
        );

        final var loadedTask = tasklistRepository.getTaskById(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId()).orElseThrow();

        assertThat(loadedTask.id().value()).isEqualTo(UserTaskTestdaten.TASK_ID);
        assertThat(loadedTask.urlaubsantrag().id().value()).isEqualTo(UrlaubsantragTestData.VACATION_REQUEST_UUID);
        assertThat(loadedTask.urlaubsantrag().antragsteller()).isEqualTo(UrlaubsantragTestData.antragsteller());
        assertThat(loadedTask.candidateUsers()).containsExactly(BenutzerTestdaten.ada(), BenutzerTestdaten.carla());
        assertThat(loadedTask.bearbeiter()).isEqualTo(BenutzerTestdaten.ada());
    }

    @Test
    void loadsTaskByIdWithoutVisibilityFilter() {
        stubReferenzen();
        final Task task = task(UserTaskTestdaten.secondTaskId(), BenutzerTestdaten.CARLA_UUID.toString());
        stubTaskById(UserTaskTestdaten.secondTaskId(), task);
        stubTaskDetails(
            UserTaskTestdaten.secondTaskId(),
            UserTaskTestdaten.secondPayload(),
            List.of(BenutzerTestdaten.CARLA_UUID.toString())
        );

        final var loadedTask = tasklistRepository.getTaskById(UserTaskTestdaten.secondTaskId()).orElseThrow();

        assertThat(loadedTask.id()).isEqualTo(UserTaskTestdaten.secondTaskId());
        assertThat(loadedTask.candidateUsers()).containsExactly(BenutzerTestdaten.carla());
        assertThat(loadedTask.bearbeiter()).isEqualTo(BenutzerTestdaten.carla());
    }

    @Test
    void returnsEmptyOptionalWhenTaskExistsButUserIsNotAuthorized() {
        stubReferenzen();
        final Task task = task(UserTaskTestdaten.secondTaskId(), BenutzerTestdaten.CARLA_UUID.toString());
        stubTaskById(UserTaskTestdaten.secondTaskId(), task);
        stubTaskDetails(
            UserTaskTestdaten.secondTaskId(),
            UserTaskTestdaten.secondPayload(),
            List.of(BenutzerTestdaten.CARLA_UUID.toString())
        );

        assertThat(tasklistRepository.getTaskById(UserTaskTestdaten.secondTaskId(), BenutzerTestdaten.adaId())).isEmpty();
    }

    @Test
    void returnsEmptyOptionalWhenTaskDoesNotExist() {
        stubTaskById(UserTaskTestdaten.taskId(), null);

        assertThat(tasklistRepository.getTaskById(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId())).isEmpty();
    }

    @Test
    void rejectsNullTaskId() {
        assertThatThrownBy(() -> tasklistRepository.getTaskById(null, BenutzerTestdaten.adaId()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("taskId darf nicht null sein");
    }

    @Test
    void rejectsNullTaskIdForUnfilteredLookup() {
        assertThatThrownBy(() -> tasklistRepository.getTaskById(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("taskId darf nicht null sein");
    }

    @Test
    void rejectsNullUserForGetTaskById() {
        assertThatThrownBy(() -> tasklistRepository.getTaskById(UserTaskTestdaten.taskId(), null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("benutzerId darf nicht null sein");
    }

    @Test
    void rejectsNullUserForGetAllTasks() {
        assertThatThrownBy(() -> tasklistRepository.getAllTasks(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("benutzerId darf nicht null sein");
    }

    private void stubAllTasks(Task... tasks) {
        final TaskQuery taskQuery = mock(TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.or()).thenReturn(taskQuery);
        when(taskQuery.taskCandidateUser(anyString())).thenReturn(taskQuery);
        when(taskQuery.includeAssignedTasks()).thenReturn(taskQuery);
        when(taskQuery.taskAssignee(anyString())).thenReturn(taskQuery);
        when(taskQuery.endOr()).thenReturn(taskQuery);
        when(taskQuery.active()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(List.of(tasks));
    }

    private void stubTaskById(UserTaskId taskId, Task task) {
        final TaskQuery taskQuery = mock(TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId(taskId.value())).thenReturn(taskQuery);
        when(taskQuery.or()).thenReturn(taskQuery);
        when(taskQuery.taskCandidateUser(anyString())).thenReturn(taskQuery);
        when(taskQuery.includeAssignedTasks()).thenReturn(taskQuery);
        when(taskQuery.taskAssignee(anyString())).thenReturn(taskQuery);
        when(taskQuery.endOr()).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(task);
    }

    private void stubTaskDetails(UserTaskId taskId, Map<String, Object> variables, List<String> candidateUserIds) {
        final List<IdentityLink> identityLinks = candidateUserIds.stream()
            .map(this::candidateLink)
            .toList();
        when(taskService.getVariables(taskId.value())).thenReturn(variables);
        when(taskService.getIdentityLinksForTask(taskId.value())).thenReturn(identityLinks);
    }

    private Task task(UserTaskId taskId, String assignee) {
        final Task task = mock(Task.class);
        when(task.getId()).thenReturn(taskId.value());
        when(task.getAssignee()).thenReturn(assignee);
        return task;
    }

    private IdentityLink candidateLink(String userId) {
        final IdentityLink identityLink = mock(IdentityLink.class);
        when(identityLink.getType()).thenReturn(IdentityLinkType.CANDIDATE);
        when(identityLink.getUserId()).thenReturn(userId);
        return identityLink;
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
