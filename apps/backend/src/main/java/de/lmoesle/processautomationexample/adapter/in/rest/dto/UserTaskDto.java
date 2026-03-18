package de.lmoesle.processautomationexample.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.lmoesle.processautomationexample.domain.tasklist.UserTask;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserTaskDto(
    @Schema(
        description = "Technische ID des User Tasks.",
        example = "approve-vacation-1"
    )
    String taskId,
    @Schema(
        description = "Zugehoeriger Urlaubsantrag, falls im Payload eine urlaubsantragId enthalten ist.",
        nullable = true
    )
    UrlaubsantragDto urlaubsantrag,
    @Schema(
        description = "Aufgeloeste Nutzer aus der Variable candidateUsers."
    )
    List<BenutzerDto> candidateUsers,
    @Schema(
        description = "Aufgeloester Bearbeiter aus dem assignee des User Tasks.",
        nullable = true
    )
    BenutzerDto bearbeiter
) {

    public static UserTaskDto ausDomain(UserTask userTask) {
        return new UserTaskDto(
            userTask.id().value(),
            userTask.urlaubsantrag() == null ? null : UrlaubsantragDto.ausDomain(userTask.urlaubsantrag()),
            userTask.candidateUsers().stream()
                .map(BenutzerDto::ausDomain)
                .toList(),
            BenutzerDto.ausOptionalerDomain(userTask.bearbeiter())
        );
    }
}
