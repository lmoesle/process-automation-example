package de.lmoesle.processautomationexample.adapter.in.rest.dto;

import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestStatusHistoryEntry;
import io.swagger.v3.oas.annotations.media.Schema;

public record VacationRequestStatusHistoryEntryDto(
    @Schema(
        description = "Status that was recorded for the vacation request.",
        example = "ANTRAG_GESTELLT"
    )
    VacationRequestStatusDto status,
    @Schema(
        description = "Optional comment that explains the status change.",
        nullable = true
    )
    String comment
) {

    public static VacationRequestStatusHistoryEntryDto fromDomain(VacationRequestStatusHistoryEntry statusHistoryEntry) {
        return new VacationRequestStatusHistoryEntryDto(
            VacationRequestStatusDto.fromDomain(statusHistoryEntry.status()),
            statusHistoryEntry.comment()
        );
    }
}
