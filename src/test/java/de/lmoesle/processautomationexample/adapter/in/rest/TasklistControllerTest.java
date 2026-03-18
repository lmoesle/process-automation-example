package de.lmoesle.processautomationexample.adapter.in.rest;

import de.lmoesle.processautomationexample.application.ports.in.BenutzeraufgabeMirZuweisenInPort;
import de.lmoesle.processautomationexample.application.ports.in.BenutzeraufgabeMirZuweisenInPort.WeiseBenutzeraufgabeMirZuCommand;
import de.lmoesle.processautomationexample.application.ports.in.TaskAbfragenInPort;
import de.lmoesle.processautomationexample.application.ports.in.TaskAbfragenInPort.GetAllTasksCommand;
import de.lmoesle.processautomationexample.application.ports.in.TaskAbfragenInPort.GetTaskByIdCommand;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.processautomationexample.domain.tasklist.TaskNichtGefundenException;
import de.lmoesle.processautomationexample.domain.tasklist.TaskZugriffVerweigertException;
import de.lmoesle.processautomationexample.domain.tasklist.UserTaskTestdaten;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TasklistController.class)
class TasklistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskAbfragenInPort taskAbfragenInPort;

    @MockitoBean
    private BenutzeraufgabeMirZuweisenInPort benutzeraufgabeMirZuweisenInPort;

    @Test
    void loadsAllTasks() throws Exception {
        when(taskAbfragenInPort.getAllTasks(new GetAllTasksCommand(BenutzerTestdaten.adaId()))).thenReturn(List.of(
            UserTaskTestdaten.userTaskWithoutPayload(),
            UserTaskTestdaten.secondUserTaskWithoutPayload()
        ));

        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].taskId").value(UserTaskTestdaten.TASK_ID))
            .andExpect(jsonPath("$[0].bearbeiter.name").value("Ada Lovelace"))
            .andExpect(jsonPath("$[0].candidateUsers[0].name").value("Ada Lovelace"))
            .andExpect(jsonPath("$[0].candidateUsers[1].name").value("Carla Gomez"))
            .andExpect(jsonPath("$[0].urlaubsantrag.id").value("c7a6939b-a97b-4445-bd66-4a0f98781899"))
            .andExpect(jsonPath("$[1].taskId").value(UserTaskTestdaten.SECOND_TASK_ID))
            .andExpect(jsonPath("$[1].bearbeiter.name").value("Carla Gomez"))
            .andExpect(jsonPath("$[1].candidateUsers[0].name").value("Carla Gomez"))
            .andExpect(jsonPath("$[1].urlaubsantrag.id").value("a91e8877-f17a-40d4-a9ee-1b0350f27b52"));

        verify(taskAbfragenInPort).getAllTasks(new GetAllTasksCommand(BenutzerTestdaten.adaId()));
    }

    @Test
    void loadsTaskById() throws Exception {
        when(taskAbfragenInPort.getTaskById(new GetTaskByIdCommand(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId())))
            .thenReturn(UserTaskTestdaten.userTask());

        mockMvc.perform(get("/api/tasks/{taskId}", UserTaskTestdaten.TASK_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.taskId").value(UserTaskTestdaten.TASK_ID))
            .andExpect(jsonPath("$.urlaubsantrag.id").value("c7a6939b-a97b-4445-bd66-4a0f98781899"))
            .andExpect(jsonPath("$.candidateUsers[0].name").value("Ada Lovelace"))
            .andExpect(jsonPath("$.candidateUsers[1].name").value("Carla Gomez"))
            .andExpect(jsonPath("$.bearbeiter.name").value("Ada Lovelace"));

        verify(taskAbfragenInPort).getTaskById(new GetTaskByIdCommand(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId()));
    }

    @Test
    void returnsNotFoundForUnknownTaskId() throws Exception {
        when(taskAbfragenInPort.getTaskById(new GetTaskByIdCommand(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId())))
            .thenThrow(new TaskNichtGefundenException(UserTaskTestdaten.taskId()));

        mockMvc.perform(get("/api/tasks/{taskId}", UserTaskTestdaten.TASK_ID))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Aufgabe nicht gefunden"))
            .andExpect(jsonPath("$.detail").value("taskId verweist auf keine vorhandene Aufgabe: " + UserTaskTestdaten.TASK_ID));
    }

    @Test
    void assignsTaskToCurrentUser() throws Exception {
        mockMvc.perform(post("/api/tasks/{taskId}/assign-to-me", UserTaskTestdaten.TASK_ID))
            .andExpect(status().isNoContent());

        verify(benutzeraufgabeMirZuweisenInPort).weiseBenutzeraufgabeMirZu(
            new WeiseBenutzeraufgabeMirZuCommand(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId())
        );
    }

    @Test
    void returnsForbiddenWhenCurrentUserCannotAssignTask() throws Exception {
        doThrow(new TaskZugriffVerweigertException(UserTaskTestdaten.taskId()))
            .when(benutzeraufgabeMirZuweisenInPort)
            .weiseBenutzeraufgabeMirZu(new WeiseBenutzeraufgabeMirZuCommand(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId()));

        mockMvc.perform(post("/api/tasks/{taskId}/assign-to-me", UserTaskTestdaten.TASK_ID))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.title").value("Zugriff auf Aufgabe verweigert"))
            .andExpect(jsonPath("$.detail").value("Aktueller Benutzer hat keinen Zugriff auf Aufgabe: " + UserTaskTestdaten.TASK_ID));
    }
}
