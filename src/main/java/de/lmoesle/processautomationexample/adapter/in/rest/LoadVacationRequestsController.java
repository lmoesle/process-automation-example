package de.lmoesle.processautomationexample.adapter.in.rest;

import de.lmoesle.processautomationexample.adapter.in.rest.dto.VacationRequestDto;
import de.lmoesle.processautomationexample.application.ports.in.LoadVacationRequestsForUserInPort;
import de.lmoesle.processautomationexample.application.ports.in.LoadVacationRequestsForUserInPort.LoadVacationRequestsForUserCommand;
import de.lmoesle.processautomationexample.domain.user.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vacation-requests")
@RequiredArgsConstructor
@Tag(name = "Vacation Requests")
public class LoadVacationRequestsController {

    private static final UserId CURRENT_USER_ID = UserId.of(UUID.fromString("2d88b39b-e7b0-4a3f-b9c6-b3d8e6fbe100"));

    private final LoadVacationRequestsForUserInPort loadVacationRequestsForUserInPort;

    @GetMapping
    @Operation(
        summary = "Load vacation requests",
        description = "Loads all vacation requests for the currently logged-in user. Until authentication exists, the user is hardcoded in the controller."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Vacation requests loaded successfully.",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = VacationRequestDto.class))
            )
        )
    })
    public List<VacationRequestDto> loadVacationRequests() {
        return loadVacationRequestsForUserInPort.loadVacationRequestsForUser(
                new LoadVacationRequestsForUserCommand(CURRENT_USER_ID)
            ).stream()
            .map(VacationRequestDto::fromDomain)
            .toList();
    }
}
