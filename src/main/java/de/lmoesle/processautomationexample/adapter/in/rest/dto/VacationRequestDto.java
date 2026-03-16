package de.lmoesle.processautomationexample.adapter.in.rest.dto;

import de.lmoesle.processautomationexample.application.ports.in.CreateVacationRequestInPort.CreateVacationRequestResult;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequest;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

public record VacationRequestDto(
    @Schema(
        description = "Technical id of the vacation request.",
        example = "c7a6939b-a97b-4445-bd66-4a0f98781899"
    )
    UUID id,
    @Schema(
        description = "Start date of the requested vacation period.",
        example = "2026-07-01"
    )
    LocalDate from,
    @Schema(
        description = "End date of the requested vacation period.",
        example = "2026-07-10"
    )
    LocalDate to,
    @Schema(
        description = "Resolved applicant user who created the vacation request."
    )
    UserDto applicantUser,
    @Schema(
        description = "Resolved substitute user during the vacation period.",
        nullable = true
    )
    UserDto substituteUser
) {

    public static VacationRequestDto fromDomain(VacationRequest vacationRequest) {
        return new VacationRequestDto(
            vacationRequest.id().value(),
            vacationRequest.period().from(),
            vacationRequest.period().to(),
            UserDto.fromDomain(vacationRequest.applicantUser()),
            UserDto.fromNullableDomain(vacationRequest.substituteUser())
        );
    }

    public static VacationRequestDto from(CreateVacationRequestResult result, LocalDate from, LocalDate to) {
        return new VacationRequestDto(
            result.vacationRequestId().value(),
            from,
            to,
            UserDto.fromDomain(result.applicantUser()),
            UserDto.fromNullableDomain(result.substituteUser())
        );
    }
}
