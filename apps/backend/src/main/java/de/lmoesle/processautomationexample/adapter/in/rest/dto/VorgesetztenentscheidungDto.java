package de.lmoesle.processautomationexample.adapter.in.rest.dto;

import de.lmoesle.processautomationexample.application.ports.in.GenehmigungVomVorgesetztenInPort.GenehmigungVomVorgesetztenCommand;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.processautomationexample.domain.tasklist.UserTaskId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.util.Assert;

public record VorgesetztenentscheidungDto(
    @NotNull
    @Schema(
        description = "Entscheidung des Vorgesetzten. `true` genehmigt den Antrag, `false` lehnt ihn ab.",
        example = "true"
    )
    Boolean genehmigt,
    @Schema(
        description = "Optionaler Kommentar des Vorgesetzten, der in der Statushistorie sichtbar wird.",
        nullable = true,
        example = "Vertretung ist organisiert."
    )
    String kommentar
) {

    public GenehmigungVomVorgesetztenCommand alsCommand(UserTaskId taskId, BenutzerId benutzerId) {
        Assert.notNull(genehmigt, "genehmigt darf nicht null sein");
        return new GenehmigungVomVorgesetztenCommand(taskId, benutzerId, genehmigt, kommentar);
    }
}
