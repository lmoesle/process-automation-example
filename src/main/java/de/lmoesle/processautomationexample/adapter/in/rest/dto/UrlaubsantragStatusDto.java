package de.lmoesle.processautomationexample.adapter.in.rest.dto;

import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragStatus;

public enum UrlaubsantragStatusDto {
    ANTRAG_GESTELLT,
    AUTOMATISCHE_PRUEFUNG,
    VORGESETZTEN_PRUEFUNG,
    ABGELEHNT,
    GENEHMIGT;

    public static UrlaubsantragStatusDto ausDomain(UrlaubsantragStatus status) {
        return UrlaubsantragStatusDto.valueOf(status.name());
    }
}
