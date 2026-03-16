package de.lmoesle.processautomationexample.application.ports.out;

import de.lmoesle.processautomationexample.domain.vacationrequest.ProcessInstanceId;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequest;

public interface StartVacationApprovalProcessOutPort {

    ProcessInstanceId startApprovalProcessFor(VacationRequest vacationRequest);
}
