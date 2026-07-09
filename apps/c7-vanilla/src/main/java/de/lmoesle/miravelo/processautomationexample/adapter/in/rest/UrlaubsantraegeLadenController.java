package de.lmoesle.miravelo.processautomationexample.adapter.in.rest;

import de.lmoesle.miravelo.processautomationexample.adapter.in.rest.dto.UrlaubsantragDto;
import de.lmoesle.miravelo.processautomationexample.application.ports.in.UrlaubsantraegeFuerBenutzerLadenInPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.in.UrlaubsantraegeFuerBenutzerLadenInPort.UrlaubsantraegeFuerBenutzerLadenCommand;
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

@RestController
@RequestMapping("/api/urlaubsantraege")
@RequiredArgsConstructor
@Tag(name = "Urlaubsanträge")
public class UrlaubsantraegeLadenController {

    private final UrlaubsantraegeFuerBenutzerLadenInPort urlaubsantraegeFuerBenutzerLadenInPort;
    private final AktuellerBenutzerProvider aktuellerBenutzerProvider;

    @GetMapping
    @Operation(
        summary = "Urlaubsanträge laden",
        description = "Laedt alle Urlaubsantraege fuer den aktuell angemeldeten Benutzer."
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
        final var aktuellerBenutzerId = aktuellerBenutzerProvider.benutzerId();
        return urlaubsantraegeFuerBenutzerLadenInPort.ladeUrlaubsantraegeFuerBenutzer(
                new UrlaubsantraegeFuerBenutzerLadenCommand(aktuellerBenutzerId)
            ).stream()
            .map(UrlaubsantragDto::ausDomain)
            .toList();
    }
}
