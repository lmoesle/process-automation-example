package de.lmoesle.miravelo.processautomationexample.adapter.in.rest;

import de.lmoesle.miravelo.processautomationexample.adapter.in.rest.dto.BenutzerAuswahlDto;
import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzerSuchenInPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzerSuchenInPort.BenutzerSuchenCommand;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/benutzer")
@RequiredArgsConstructor
@Tag(name = "Benutzer")
public class BenutzerSuchenController {

    private final BenutzerSuchenInPort benutzerSuchenInPort;

    @GetMapping
    @Operation(
        summary = "Benutzer suchen",
        description = "Liefert die aktuell verfuegbaren Benutzer fuer Auswahlfelder wie die Vertretung."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Benutzer erfolgreich geladen.",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = BenutzerAuswahlDto.class))
            )
        )
    })
    public List<BenutzerAuswahlDto> sucheBenutzer(
        @RequestParam(name = "suchbegriff", required = false) String suchbegriff
    ) {
        return benutzerSuchenInPort.sucheBenutzer(new BenutzerSuchenCommand(suchbegriff)).stream()
            .map(BenutzerAuswahlDto::ausDomain)
            .toList();
    }
}
