package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.out.TasklistRepositoryOutPort;
import de.lmoesle.processautomationexample.domain.tasklist.UserTaskTestdaten;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GetAllTasksUseCaseTest {

    private TasklistRepositoryOutPort tasklistRepositoryOutPort;
    private GetAllTasksUseCase getAllTasksUseCase;

    @BeforeEach
    void setUp() {
        tasklistRepositoryOutPort = mock(TasklistRepositoryOutPort.class);
        getAllTasksUseCase = new GetAllTasksUseCase(tasklistRepositoryOutPort);
    }

    @Test
    void loadsAllTasks() {
        var expectedTasks = List.of(
            UserTaskTestdaten.userTaskWithoutPayload(),
            UserTaskTestdaten.secondUserTaskWithoutPayload()
        );
        when(tasklistRepositoryOutPort.getAllTasks()).thenReturn(expectedTasks);

        var tasks = getAllTasksUseCase.getAllTasks();

        verify(tasklistRepositoryOutPort).getAllTasks();
        assertThat(tasks).containsExactlyElementsOf(expectedTasks);
    }
}
