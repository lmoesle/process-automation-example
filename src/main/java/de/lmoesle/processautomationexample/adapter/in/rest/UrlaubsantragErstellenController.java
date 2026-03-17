package de.lmoesle.processautomationexample.adapter.in.rest;

import de.lmoesle.processautomationexample.adapter.in.rest.dto.UrlaubsantragErstellenDto;
import de.lmoesle.processautomationexample.adapter.in.rest.dto.UrlaubsantragDto;
import de.lmoesle.processautomationexample.application.ports.in.UrlaubsantragErstellenInPort;
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
@RequestMapping("/api/urlaubsantraege")
@RequiredArgsConstructor
@Tag(name = "Urlaubsanträge")
public class UrlaubsantragErstellenController {

    private final UrlaubsantragErstellenInPort erstelleUrlaubsantragInPort;

    @PostMapping
    @Operation(
        summary = "Urlaubsantrag erstellen",
        description = "Erstellt einen neuen Urlaubsantrag, speichert ihn und startet den BPMN-Genehmigungsprozess."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Urlaubsantrag erfolgreich erstellt.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UrlaubsantragDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Ungueltige Anfrage, zum Beispiel wenn `von` nach `bis` liegt.",
            content = @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class)
            )
        )
    })
    public ResponseEntity<UrlaubsantragDto> erstelleUrlaubsantrag(
        @Valid @RequestBody UrlaubsantragErstellenDto request
    ) {
        var ergebnis = erstelleUrlaubsantragInPort.erstelleUrlaubsantrag(request.alsCommand());
        var response = UrlaubsantragDto.ausErstellenErgebnis(ergebnis, request.von(), request.bis());

        return ResponseEntity
            .created(URI.create("/api/urlaubsantraege/" + ergebnis.urlaubsantragId().value()))
            .body(response);
    }
}
