package de.lmoesle.processautomationexample.adapter.in.rest;

import de.lmoesle.processautomationexample.adapter.in.rest.dto.UrlaubsantragDto;
import de.lmoesle.processautomationexample.application.ports.in.UrlaubsantraegeFuerBenutzerLadenInPort;
import de.lmoesle.processautomationexample.application.ports.in.UrlaubsantraegeFuerBenutzerLadenInPort.UrlaubsantraegeFuerBenutzerLadenCommand;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
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
@RequestMapping("/api/urlaubsantraege")
@RequiredArgsConstructor
@Tag(name = "Urlaubsanträge")
public class UrlaubsantraegeLadenController {

    private static final BenutzerId AKTUELLER_BENUTZER_ID = BenutzerId.of(UUID.fromString("2d88b39b-e7b0-4a3f-b9c6-b3d8e6fbe100"));

    private final UrlaubsantraegeFuerBenutzerLadenInPort urlaubsantraegeFuerBenutzerLadenInPort;

    @GetMapping
    @Operation(
        summary = "Urlaubsanträge laden",
        description = "Laedt alle Urlaubsantraege fuer den aktuell angemeldeten Benutzer. Solange keine Authentifizierung existiert, ist der Benutzer im Controller fest verdrahtet."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Urlaubsantraege erfolgreich geladen.",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = UrlaubsantragDto.class))
            )
        )
    })
    public List<UrlaubsantragDto> ladeUrlaubsantraege() {
        return urlaubsantraegeFuerBenutzerLadenInPort.ladeUrlaubsantraegeFuerBenutzer(
                new UrlaubsantraegeFuerBenutzerLadenCommand(AKTUELLER_BENUTZER_ID)
            ).stream()
            .map(UrlaubsantragDto::ausDomain)
            .toList();
    }
}
