package de.lmoesle.processautomationexample.adapter.in.rest.dto;

import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import io.swagger.v3.oas.annotations.media.Schema;

public record BenutzerDto(
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

    public static BenutzerDto ausDomain(Benutzer benutzer) {
        return new BenutzerDto(
            benutzer.name(),
            benutzer.email()
        );
    }

    public static BenutzerDto ausOptionalerDomain(Benutzer benutzer) {
        return benutzer == null ? null : ausDomain(benutzer);
    }
}
