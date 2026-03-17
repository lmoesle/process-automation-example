package de.lmoesle.processautomationexample.adapter.in.rest;

import de.lmoesle.processautomationexample.adapter.in.rest.dto.UserTaskDto;
import de.lmoesle.processautomationexample.application.ports.in.TaskAbfragenInPort;
import de.lmoesle.processautomationexample.application.ports.in.TaskAbfragenInPort.GetAllTasksCommand;
import de.lmoesle.processautomationexample.application.ports.in.TaskAbfragenInPort.GetTaskByIdCommand;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.processautomationexample.domain.tasklist.UserTaskId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasklist")
public class TasklistController {

    private static final BenutzerId AKTUELLER_BENUTZER_ID = BenutzerId.of(UUID.fromString("2d88b39b-e7b0-4a3f-b9c6-b3d8e6fbe100"));

    private final TaskAbfragenInPort taskAbfragenInPort;

    @GetMapping
    @Operation(
        summary = "Alle User Tasks laden",
        description = "Liefert alle fuer den aktuell angemeldeten Benutzer sichtbaren User Tasks. Solange keine Authentifizierung existiert, ist der Benutzer im Controller fest verdrahtet."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "User Tasks erfolgreich geladen.",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = UserTaskDto.class))
            )
        )
    })
    public List<UserTaskDto> getAllTasks() {
        return taskAbfragenInPort.getAllTasks(new GetAllTasksCommand(AKTUELLER_BENUTZER_ID)).stream()
            .map(UserTaskDto::ausDomain)
            .toList();
    }

    @GetMapping("/{taskId}")
    @Operation(
        summary = "User Task per ID laden",
        description = "Liefert einen einzelnen User Task fuer den aktuell angemeldeten Benutzer inklusive Payload anhand seiner technischen ID."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "User Task erfolgreich geladen.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserTaskDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Kein User Task mit der angegebenen ID gefunden.")
    })
    public UserTaskDto getTaskById(@PathVariable("taskId") String taskId) {
        return UserTaskDto.ausDomain(
            taskAbfragenInPort.getTaskById(new GetTaskByIdCommand(UserTaskId.of(taskId), AKTUELLER_BENUTZER_ID))
        );
    }
}
