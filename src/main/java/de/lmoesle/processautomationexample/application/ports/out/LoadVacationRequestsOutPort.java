package de.lmoesle.processautomationexample.application.ports.out;

import de.lmoesle.processautomationexample.domain.user.UserId;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequest;

import java.util.List;

public interface LoadVacationRequestsOutPort {

    List<VacationRequest> findAllByApplicantUserId(UserId applicantUserId);
}
