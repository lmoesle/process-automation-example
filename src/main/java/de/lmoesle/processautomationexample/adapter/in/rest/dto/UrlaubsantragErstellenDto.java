package de.lmoesle.processautomationexample.adapter.in.rest.dto;

import de.lmoesle.processautomationexample.application.ports.in.UrlaubsantragErstellenInPort.UrlaubsantragErstellenCommand;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record UrlaubsantragErstellenDto(
    @Schema(
        description = "Startdatum des beantragten Urlaubszeitraums.",
        example = "2026-07-01"
    )
    @NotNull LocalDate von,
    @Schema(
        description = "Endedatum des beantragten Urlaubszeitraums.",
        example = "2026-07-10"
    )
    @NotNull LocalDate bis,
    @Schema(
        description = "Benutzer-ID des Antragstellers.",
        example = "2d88b39b-e7b0-4a3f-b9c6-b3d8e6fbe100"
    )
    @NotNull UUID antragstellerId,
    @Schema(
        description = "Optionale Benutzer-ID der Vertretung waehrend des Urlaubszeitraums.",
        example = "f9821988-db4f-4daa-9414-6cc5227f7102",
        nullable = true
    )
    UUID vertretungId
) {

    public UrlaubsantragErstellenCommand alsCommand() {
        return new UrlaubsantragErstellenCommand(
            von,
            bis,
            BenutzerId.of(antragstellerId),
            vertretungId == null ? null : BenutzerId.of(vertretungId)
        );
    }

    @AssertTrue(message = "'von' muss vor oder gleich 'bis' liegen.")
    public boolean isZeitraumGueltig() {
        return von == null || bis == null || !von.isAfter(bis);
    }
}
