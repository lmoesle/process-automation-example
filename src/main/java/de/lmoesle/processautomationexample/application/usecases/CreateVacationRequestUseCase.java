package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.CreateVacationRequestInPort;
import de.lmoesle.processautomationexample.application.ports.out.SaveVacationRequestOutPort;
import de.lmoesle.processautomationexample.application.ports.out.StartVacationApprovalProcessOutPort;
import de.lmoesle.processautomationexample.domain.vacationrequest.ProcessInstanceId;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateVacationRequestUseCase implements CreateVacationRequestInPort {

    private final SaveVacationRequestOutPort saveVacationRequestOutPort;
    private final StartVacationApprovalProcessOutPort startVacationApprovalProcessOutPort;

    @Override
    public CreateVacationRequestResult createVacationRequest(CreateVacationRequestCommand command) {
        VacationRequest vacationRequest = VacationRequest.submit(
            command.from(),
            command.to(),
            command.applicantUserId(),
            command.substituteUserId()
        );

        saveVacationRequestOutPort.save(vacationRequest);

        ProcessInstanceId processInstanceId = startVacationApprovalProcessOutPort.startApprovalProcessFor(vacationRequest);
        vacationRequest.markApprovalProcessStarted(processInstanceId);
        vacationRequest = saveVacationRequestOutPort.save(vacationRequest);

        return new CreateVacationRequestResult(vacationRequest.id(), processInstanceId);
    }
}
