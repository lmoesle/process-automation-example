package de.lmoesle.processautomationexample.application.ports.out;

import de.lmoesle.processautomationexample.domain.user.UserId;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequest;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestId;

import java.util.List;
import java.util.Optional;

public interface LoadVacationRequestsOutPort {

    Optional<VacationRequest> findById(VacationRequestId vacationRequestId);

    List<VacationRequest> findAllByApplicantUserId(UserId applicantUserId);
}
