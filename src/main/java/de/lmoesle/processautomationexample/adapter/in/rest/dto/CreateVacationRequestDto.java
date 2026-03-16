package de.lmoesle.processautomationexample.adapter.in.rest.dto;

import de.lmoesle.processautomationexample.application.ports.in.CreateVacationRequestInPort.CreateVacationRequestCommand;
import de.lmoesle.processautomationexample.domain.user.UserId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateVacationRequestDto(
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
        example = "2d88b39b-e7b0-4a3f-b9c6-b3d8e6fbe100"
    )
    @NotNull UUID applicantUserId,
    @Schema(
        description = "Optional user id of the substitute during the vacation period.",
        example = "f9821988-db4f-4daa-9414-6cc5227f7102",
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
