package de.lmoesle.processautomationexample.adapter.in.rest.dto;

import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestStatus;

public enum VacationRequestStatusDto {
    ANTRAG_GESTELLT,
    AUTOMATISCHE_PRUEFUNG,
    LEAD_PRUEFUNG,
    ABGELEHNT,
    GENEHMIGT;

    public static VacationRequestStatusDto fromDomain(VacationRequestStatus status) {
        return VacationRequestStatusDto.valueOf(status.name());
    }
}
