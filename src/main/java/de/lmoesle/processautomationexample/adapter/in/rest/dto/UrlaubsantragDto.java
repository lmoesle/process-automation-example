package de.lmoesle.processautomationexample.adapter.in.rest.dto;

import de.lmoesle.processautomationexample.application.ports.in.UrlaubsantragErstellenInPort.UrlaubsantragErstellenErgebnis;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UrlaubsantragDto(
    @Schema(
        description = "Technische ID des Urlaubsantrags.",
        example = "c7a6939b-a97b-4445-bd66-4a0f98781899"
    )
    UUID id,
    @Schema(
        description = "Startdatum des beantragten Urlaubszeitraums.",
        example = "2026-07-01"
    )
    LocalDate von,
    @Schema(
        description = "Endedatum des beantragten Urlaubszeitraums.",
        example = "2026-07-10"
    )
    LocalDate bis,
    @Schema(
        description = "Aufgeloester Antragsteller."
    )
    BenutzerDto antragsteller,
    @Schema(
        description = "Aufgeloeste Vertretung waehrend des Urlaubszeitraums.",
        nullable = true
    )
    BenutzerDto vertretung,
    @Schema(
        description = "Aktueller Status des Urlaubsantrags.",
        example = "ANTRAG_GESTELLT"
    )
    UrlaubsantragStatusDto status,
    @Schema(
        description = "Chronologische Historie der erfassten Urlaubsantragsstatus."
    )
    List<UrlaubsantragStatusHistorieneintragDto> statusHistorie
) {

    public static UrlaubsantragDto ausDomain(Urlaubsantrag urlaubsantrag) {
        return new UrlaubsantragDto(
            urlaubsantrag.id().value(),
            urlaubsantrag.zeitraum().von(),
            urlaubsantrag.zeitraum().bis(),
            BenutzerDto.ausDomain(urlaubsantrag.antragsteller()),
            BenutzerDto.ausOptionalerDomain(urlaubsantrag.vertretung()),
            UrlaubsantragStatusDto.ausDomain(urlaubsantrag.status()),
            urlaubsantrag.statusHistorie().stream()
                .map(UrlaubsantragStatusHistorieneintragDto::ausDomain)
                .toList()
        );
    }

    public static UrlaubsantragDto ausErstellenErgebnis(UrlaubsantragErstellenErgebnis ergebnis, LocalDate von, LocalDate bis) {
        return new UrlaubsantragDto(
            ergebnis.urlaubsantragId().value(),
            von,
            bis,
            BenutzerDto.ausDomain(ergebnis.antragsteller()),
            BenutzerDto.ausOptionalerDomain(ergebnis.vertretung()),
            UrlaubsantragStatusDto.ausDomain(ergebnis.status()),
            ergebnis.statusHistorie().stream()
                .map(UrlaubsantragStatusHistorieneintragDto::ausDomain)
                .toList()
        );
    }
}
