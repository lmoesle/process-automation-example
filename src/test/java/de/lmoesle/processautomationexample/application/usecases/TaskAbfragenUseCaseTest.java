package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.TaskAbfragenInPort.GetAllTasksCommand;
import de.lmoesle.processautomationexample.application.ports.in.TaskAbfragenInPort.GetTaskByIdCommand;
import de.lmoesle.processautomationexample.application.ports.out.TasklistRepositoryOutPort;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.processautomationexample.domain.tasklist.TaskNichtGefundenException;
import de.lmoesle.processautomationexample.domain.tasklist.UserTaskTestdaten;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskAbfragenUseCaseTest {

    private TasklistRepositoryOutPort tasklistRepositoryOutPort;
    private TaskAbfragenUseCase taskAbfragenUseCase;

    @BeforeEach
    void setUp() {
        tasklistRepositoryOutPort = mock(TasklistRepositoryOutPort.class);
        taskAbfragenUseCase = new TaskAbfragenUseCase(tasklistRepositoryOutPort);
    }

    @Test
    void loadsAllTasksVisibleForUser() {
        var expectedTasks = List.of(
            UserTaskTestdaten.userTaskWithoutPayload(),
            UserTaskTestdaten.secondUserTaskWithoutPayload()
        );
        when(tasklistRepositoryOutPort.getAllTasks(BenutzerTestdaten.carlaId())).thenReturn(expectedTasks);

        var tasks = taskAbfragenUseCase.getAllTasks(new GetAllTasksCommand(BenutzerTestdaten.carlaId()));

        verify(tasklistRepositoryOutPort).getAllTasks(BenutzerTestdaten.carlaId());
        assertThat(tasks).containsExactlyElementsOf(expectedTasks);
    }

    @Test
    void rejectsNullGetAllTasksCommand() {
        assertThatThrownBy(() -> taskAbfragenUseCase.getAllTasks(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("command darf nicht null sein");
    }

    @Test
    void rejectsNullUserInGetAllTasksCommand() {
        assertThatThrownBy(() -> taskAbfragenUseCase.getAllTasks(new GetAllTasksCommand(null)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("benutzerId darf nicht null sein");
    }

    @Test
    void loadsTaskById() {
        var expectedTask = UserTaskTestdaten.userTask();
        when(tasklistRepositoryOutPort.getTaskById(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId()))
            .thenReturn(Optional.of(expectedTask));

        var task = taskAbfragenUseCase.getTaskById(new GetTaskByIdCommand(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId()));

        verify(tasklistRepositoryOutPort).getTaskById(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId());
        assertThat(task).isEqualTo(expectedTask);
    }

    @Test
    void rejectsNullGetTaskByIdCommand() {
        assertThatThrownBy(() -> taskAbfragenUseCase.getTaskById(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("command darf nicht null sein");
    }

    @Test
    void rejectsNullTaskId() {
        assertThatThrownBy(() -> taskAbfragenUseCase.getTaskById(new GetTaskByIdCommand(null, BenutzerTestdaten.adaId())))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("taskId darf nicht null sein");
    }

    @Test
    void rejectsNullUserInGetTaskByIdCommand() {
        assertThatThrownBy(() -> taskAbfragenUseCase.getTaskById(new GetTaskByIdCommand(UserTaskTestdaten.taskId(), null)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("benutzerId darf nicht null sein");
    }

    @Test
    void throwsWhenTaskDoesNotExistOrIsNotVisible() {
        when(tasklistRepositoryOutPort.getTaskById(UserTaskTestdaten.secondTaskId(), BenutzerTestdaten.adaId()))
            .thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> taskAbfragenUseCase.getTaskById(new GetTaskByIdCommand(UserTaskTestdaten.secondTaskId(), BenutzerTestdaten.adaId()))
        )
            .isInstanceOf(TaskNichtGefundenException.class)
            .hasMessage("taskId verweist auf keine vorhandene Aufgabe: " + UserTaskTestdaten.SECOND_TASK_ID);
    }
}
