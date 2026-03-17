package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.CreateVacationRequestInPort;
import de.lmoesle.processautomationexample.application.ports.out.SaveVacationRequestOutPort;
import de.lmoesle.processautomationexample.application.ports.out.StartVacationApprovalProcessOutPort;
import de.lmoesle.processautomationexample.application.ports.out.UserRepositoryOutPort;
import de.lmoesle.processautomationexample.domain.vacationrequest.ProcessInstanceId;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequest;
import de.lmoesle.processautomationexample.domain.user.User;
import de.lmoesle.processautomationexample.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateVacationRequestUseCase implements CreateVacationRequestInPort {

    private final UserRepositoryOutPort userRepositoryOutPort;
    private final SaveVacationRequestOutPort saveVacationRequestOutPort;
    private final StartVacationApprovalProcessOutPort startVacationApprovalProcessOutPort;

    @Override
    public CreateVacationRequestResult createVacationRequest(CreateVacationRequestCommand command) {
        User applicantUser = loadUser(command.applicantUserId(), "applicantUserId");
        User substituteUser = command.substituteUserId() == null
            ? null
            : loadUser(command.substituteUserId(), "substituteUserId");

        VacationRequest vacationRequest = VacationRequest.submit(
            command.from(),
            command.to(),
            applicantUser,
            substituteUser
        );

        saveVacationRequestOutPort.save(vacationRequest);

        ProcessInstanceId processInstanceId = startVacationApprovalProcessOutPort.startApprovalProcessFor(vacationRequest);
        vacationRequest.markApprovalProcessStarted(processInstanceId);
        vacationRequest = saveVacationRequestOutPort.save(vacationRequest);

        return new CreateVacationRequestResult(
            vacationRequest.id(),
            processInstanceId,
            vacationRequest.status(),
            vacationRequest.statusHistory(),
            applicantUser,
            substituteUser
        );
    }

    private User loadUser(UserId userId, String fieldName) {
        return userRepositoryOutPort.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException(fieldName + " does not reference an existing user"));
    }
}
