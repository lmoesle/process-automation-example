package de.lmoesle.processautomationexample.application.ports.in;

import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestId;

public interface AutomaticCheckVacationRequestInPort {

    boolean automaticCheckVacationRequest(AutomaticCheckVacationRequestCommand command);

    record AutomaticCheckVacationRequestCommand(
        VacationRequestId vacationRequestId
    ) {
    }
}
