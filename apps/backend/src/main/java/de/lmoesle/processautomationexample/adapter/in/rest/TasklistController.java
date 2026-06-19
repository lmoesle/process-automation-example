package de.lmoesle.processautomationexample.adapter.in.rest;

import de.lmoesle.processautomationexample.adapter.in.rest.dto.VorgesetztenentscheidungDto;
import de.lmoesle.processautomationexample.adapter.in.rest.dto.UserTaskDto;
import de.lmoesle.processautomationexample.application.ports.in.GenehmigungVomVorgesetztenInPort;
import de.lmoesle.processautomationexample.application.ports.in.TaskAbfragenInPort;
import de.lmoesle.processautomationexample.application.ports.in.TaskAbfragenInPort.GetAllTasksCommand;
import de.lmoesle.processautomationexample.application.ports.in.TaskAbfragenInPort.GetTaskByIdCommand;
import de.lmoesle.processautomationexample.domain.tasklist.UserTaskId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasklist")
public class TasklistController {

    private final TaskAbfragenInPort taskAbfragenInPort;
    private final GenehmigungVomVorgesetztenInPort genehmigungVomVorgesetztenInPort;
    private final AktuellerBenutzerProvider aktuellerBenutzerProvider;

    @GetMapping
    @Operation(
        summary = "Alle User Tasks laden",
        description = "Liefert alle fuer den aktuell angemeldeten Benutzer sichtbaren User Tasks."
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
        final var aktuellerBenutzerId = aktuellerBenutzerProvider.benutzerId();
        return taskAbfragenInPort.getAllTasks(new GetAllTasksCommand(aktuellerBenutzerId)).stream()
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
        final var aktuellerBenutzerId = aktuellerBenutzerProvider.benutzerId();
        return UserTaskDto.ausDomain(
            taskAbfragenInPort.getTaskById(new GetTaskByIdCommand(
                UserTaskId.of(taskId),
                aktuellerBenutzerId
            ))
        );
    }

    @PostMapping("/{taskId}/vorgesetztenentscheidung")
    @Operation(
        summary = "Genehmigung vom Vorgesetzten entscheiden",
        description = "Der aktuelle Benutzer entscheidet ueber den sichtbaren User Task und kann optional einen Kommentar fuer die Statushistorie hinterlegen. Beim Abschluss wird die Aufgabe automatisch dem aktuellen Benutzer zugewiesen und danach abgeschlossen."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Vorgesetztenentscheidung erfolgreich verarbeitet."),
        @ApiResponse(
            responseCode = "400",
            description = "Ungueltige Anfrage, zum Beispiel bei fehlender Entscheidung oder ungueltigem Kommentar.",
            content = @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Der aktuelle Benutzer darf die Aufgabe nicht abschliessen.",
            content = @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Kein User Task mit der angegebenen ID gefunden.",
            content = @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class)
            )
        )
    })
    public ResponseEntity<Void> entscheideGenehmigungVomVorgesetzten(
        @PathVariable("taskId") String taskId,
        @Valid @RequestBody VorgesetztenentscheidungDto request
    ) {
        final var aktuellerBenutzerId = aktuellerBenutzerProvider.benutzerId();
        genehmigungVomVorgesetztenInPort.entscheideGenehmigungVomVorgesetzten(
            request.alsCommand(UserTaskId.of(taskId), aktuellerBenutzerId)
        );
        return ResponseEntity.noContent().build();
    }
}
