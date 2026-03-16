package de.lmoesle.processautomationexample.application.ports.out;

import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequest;

public interface SaveVacationRequestOutPort {

    VacationRequest save(VacationRequest vacationRequest);
}
