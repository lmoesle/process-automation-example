package de.lmoesle.processautomationexample.adapter.in.rest;

import de.lmoesle.processautomationexample.adapter.in.rest.dto.CreateVacationRequestDto;
import de.lmoesle.processautomationexample.adapter.in.rest.dto.VacationRequestDto;
import de.lmoesle.processautomationexample.application.ports.in.CreateVacationRequestInPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/vacation-requests")
@RequiredArgsConstructor
@Tag(name = "Vacation Requests")
public class CreateVacationRequestController {

    private final CreateVacationRequestInPort createVacationRequestInPort;

    @PostMapping
    @Operation(
        summary = "Create vacation request",
        description = "Creates a new vacation request, stores it in the database, and starts the vacation approval BPMN process."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Vacation request created successfully.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = VacationRequestDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request payload, for example when `from` is after `to`.",
            content = @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class)
            )
        )
    })
    public ResponseEntity<VacationRequestDto> createVacationRequest(
        @Valid @RequestBody CreateVacationRequestDto request
    ) {
        var result = createVacationRequestInPort.createVacationRequest(request.toCommand());
        var response = VacationRequestDto.from(result, request.from(), request.to());

        return ResponseEntity
            .created(URI.create("/api/vacation-requests/" + result.vacationRequestId().value()))
            .body(response);
    }
}
