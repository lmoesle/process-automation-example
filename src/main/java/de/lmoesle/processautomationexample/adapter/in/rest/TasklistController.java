package de.lmoesle.processautomationexample.adapter.in.rest;

import de.lmoesle.processautomationexample.adapter.in.rest.dto.UserTaskDto;
import de.lmoesle.processautomationexample.application.ports.in.GetAllTasksInPort;
import de.lmoesle.processautomationexample.application.ports.in.GetTaskByIdInPort;
import de.lmoesle.processautomationexample.application.ports.in.GetTaskByIdInPort.GetTaskByIdCommand;
import de.lmoesle.processautomationexample.domain.tasklist.UserTaskId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "dev.bpm-crafters.process-api.adapter.c7embedded", name = "enabled", havingValue = "true")
@Tag(name = "Tasklist")
public class TasklistController {

    private final GetAllTasksInPort getAllTasksInPort;
    private final GetTaskByIdInPort getTaskByIdInPort;

    @GetMapping
    @Operation(
        summary = "Alle User Tasks laden",
        description = "Liefert alle aktuell bekannten User Tasks aus dem registrierten UserTaskSupport."
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
        return getAllTasksInPort.getAllTasks().stream()
            .map(UserTaskDto::ausDomain)
            .toList();
    }

    @GetMapping("/{taskId}")
    @Operation(
        summary = "User Task per ID laden",
        description = "Liefert einen einzelnen User Task inklusive Payload anhand seiner technischen ID."
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
        return UserTaskDto.ausDomain(getTaskByIdInPort.getTaskById(new GetTaskByIdCommand(UserTaskId.of(taskId))));
    }
}
