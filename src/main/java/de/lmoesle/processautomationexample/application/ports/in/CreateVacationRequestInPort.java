package de.lmoesle.processautomationexample.application.ports.in;

import de.lmoesle.processautomationexample.domain.vacationrequest.ProcessInstanceId;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestId;
import de.lmoesle.processautomationexample.domain.user.User;
import de.lmoesle.processautomationexample.domain.user.UserId;

import java.time.LocalDate;

public interface CreateVacationRequestInPort {

    CreateVacationRequestResult createVacationRequest(CreateVacationRequestCommand command);

    record CreateVacationRequestCommand(
        LocalDate from,
        LocalDate to,
        UserId applicantUserId,
        UserId substituteUserId
    ) {
    }

    record CreateVacationRequestResult(
        VacationRequestId vacationRequestId,
        ProcessInstanceId processInstanceId,
        User applicantUser,
        User substituteUser
    ) {
    }
}
