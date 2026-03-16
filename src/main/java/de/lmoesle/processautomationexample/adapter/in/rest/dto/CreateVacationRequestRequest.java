package de.lmoesle.processautomationexample.adapter.in.rest.dto;

import de.lmoesle.processautomationexample.application.ports.in.CreateVacationRequestInPort.CreateVacationRequestCommand;
import de.lmoesle.processautomationexample.domain.vacationrequest.UserId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateVacationRequestRequest(
    @Schema(
        description = "Start date of the requested vacation period.",
        example = "2026-07-01"
    )
    @NotNull LocalDate from,
    @Schema(
        description = "End date of the requested vacation period.",
        example = "2026-07-10"
    )
    @NotNull LocalDate to,
    @Schema(
        description = "User id of the applicant creating the vacation request.",
        example = "772699cf-4ebd-4eb1-bf18-0f6d7569d9bb"
    )
    @NotNull UUID applicantUserId,
    @Schema(
        description = "Optional user id of the substitute during the vacation period.",
        example = "1deec1f5-2f8d-456d-bef5-b3fb75f6f028",
        nullable = true
    )
    UUID substituteUserId
) {

    public CreateVacationRequestCommand toCommand() {
        return new CreateVacationRequestCommand(
            from,
            to,
            UserId.of(applicantUserId),
            substituteUserId == null ? null : UserId.of(substituteUserId)
        );
    }

    @AssertTrue(message = "'from' must be on or before 'to'.")
    public boolean isDateRangeValid() {
        return from == null || to == null || !from.isAfter(to);
    }
}
