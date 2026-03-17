package de.lmoesle.processautomationexample.adapter.in.rest.dto;

import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragStatusHistorieneintrag;
import io.swagger.v3.oas.annotations.media.Schema;

public record UrlaubsantragStatusHistorieneintragDto(
    @Schema(
        description = "Status, der fuer den Urlaubsantrag erfasst wurde.",
        example = "ANTRAG_GESTELLT"
    )
    UrlaubsantragStatusDto status,
    @Schema(
        description = "Optionaler Kommentar zur Statusaenderung.",
        nullable = true
    )
    String kommentar
) {

    public static UrlaubsantragStatusHistorieneintragDto ausDomain(UrlaubsantragStatusHistorieneintrag statusHistorienEintrag) {
        return new UrlaubsantragStatusHistorieneintragDto(
            UrlaubsantragStatusDto.ausDomain(statusHistorienEintrag.status()),
            statusHistorienEintrag.kommentar()
        );
    }
}
