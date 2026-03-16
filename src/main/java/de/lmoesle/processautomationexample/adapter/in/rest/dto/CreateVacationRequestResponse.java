package de.lmoesle.processautomationexample.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

public record CreateVacationRequestResponse(
    @Schema(
        description = "Technical id of the created vacation request.",
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
        description = "User id of the applicant who created the vacation request.",
        example = "772699cf-4ebd-4eb1-bf18-0f6d7569d9bb"
    )
    UUID applicantUserId,
    @Schema(
        description = "Optional user id of the substitute during the vacation period.",
        example = "1deec1f5-2f8d-456d-bef5-b3fb75f6f028",
        nullable = true
    )
    UUID substituteUserId,
    @Schema(
        description = "Id of the started BPMN process instance.",
        example = "process-instance-42"
    )
    String processInstanceId
) {
}
