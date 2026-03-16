package de.lmoesle.processautomationexample.application.ports.in;

import de.lmoesle.processautomationexample.domain.user.UserId;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequest;

import java.util.List;

public interface LoadVacationRequestsForUserInPort {

    List<VacationRequest> loadVacationRequestsForUser(LoadVacationRequestsForUserCommand command);

    record LoadVacationRequestsForUserCommand(UserId userId) {
    }
}
