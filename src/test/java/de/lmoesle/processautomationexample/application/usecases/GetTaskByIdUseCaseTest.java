package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.GetTaskByIdInPort.GetTaskByIdCommand;
import de.lmoesle.processautomationexample.application.ports.out.TasklistRepositoryOutPort;
import de.lmoesle.processautomationexample.domain.tasklist.TaskNichtGefundenException;
import de.lmoesle.processautomationexample.domain.tasklist.UserTaskTestdaten;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetTaskByIdUseCaseTest {

    private TasklistRepositoryOutPort tasklistRepositoryOutPort;
    private GetTaskByIdUseCase getTaskByIdUseCase;

    @BeforeEach
    void setUp() {
        tasklistRepositoryOutPort = mock(TasklistRepositoryOutPort.class);
        getTaskByIdUseCase = new GetTaskByIdUseCase(tasklistRepositoryOutPort);
    }

    @Test
    void loadsTaskById() {
        var expectedTask = UserTaskTestdaten.userTask();
        when(tasklistRepositoryOutPort.getTaskById(UserTaskTestdaten.taskId())).thenReturn(Optional.of(expectedTask));

        var task = getTaskByIdUseCase.getTaskById(new GetTaskByIdCommand(UserTaskTestdaten.taskId()));

        verify(tasklistRepositoryOutPort).getTaskById(UserTaskTestdaten.taskId());
        assertThat(task).isEqualTo(expectedTask);
    }

    @Test
    void rejectsNullCommand() {
        assertThatThrownBy(() -> getTaskByIdUseCase.getTaskById(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("command darf nicht null sein");
    }

    @Test
    void rejectsNullTaskId() {
        assertThatThrownBy(() -> getTaskByIdUseCase.getTaskById(new GetTaskByIdCommand(null)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("taskId darf nicht null sein");
    }

    @Test
    void throwsWhenTaskDoesNotExist() {
        when(tasklistRepositoryOutPort.getTaskById(UserTaskTestdaten.taskId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getTaskByIdUseCase.getTaskById(new GetTaskByIdCommand(UserTaskTestdaten.taskId())))
            .isInstanceOf(TaskNichtGefundenException.class)
            .hasMessage("taskId verweist auf keine vorhandene Aufgabe: " + UserTaskTestdaten.TASK_ID);
    }
}
