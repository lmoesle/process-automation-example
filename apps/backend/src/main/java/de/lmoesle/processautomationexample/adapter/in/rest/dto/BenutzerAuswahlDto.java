package de.lmoesle.processautomationexample.adapter.in.rest.dto;

import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record BenutzerAuswahlDto(
    @Schema(
        description = "Technische Benutzer-ID fuer Auswahlfelder.",
        example = "2d88b39b-e7b0-4a3f-b9c6-b3d8e6fbe100"
    )
    UUID id,
    @Schema(
        description = "Anzeigename des Benutzers.",
        example = "Ada Lovelace"
    )
    String name,
    @Schema(
        description = "E-Mail-Adresse des Benutzers.",
        example = "ada.lovelace@example.com"
    )
    String email
) {

    public static BenutzerAuswahlDto ausDomain(Benutzer benutzer) {
        return new BenutzerAuswahlDto(
            benutzer.id().value(),
            benutzer.name(),
            benutzer.email()
        );
    }
}
