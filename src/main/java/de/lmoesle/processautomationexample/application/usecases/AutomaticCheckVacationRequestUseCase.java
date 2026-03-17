package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.AutomaticCheckVacationRequestInPort;
import de.lmoesle.processautomationexample.application.ports.out.LoadVacationRequestsOutPort;
import de.lmoesle.processautomationexample.application.ports.out.SaveVacationRequestOutPort;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AutomaticCheckVacationRequestUseCase implements AutomaticCheckVacationRequestInPort {

    private final LoadVacationRequestsOutPort loadVacationRequestsOutPort;
    private final SaveVacationRequestOutPort saveVacationRequestOutPort;

    @Override
    public boolean automaticCheckVacationRequest(AutomaticCheckVacationRequestCommand command) {
        Assert.notNull(command, "command must not be null");
        Assert.notNull(command.vacationRequestId(), "vacationRequestId must not be null");

        VacationRequest vacationRequest = loadVacationRequestsOutPort.findById(command.vacationRequestId())
            .orElseThrow(() -> new IllegalArgumentException("vacationRequestId does not reference an existing vacation request"));

        vacationRequest.startAutomaticCheck();
        saveVacationRequestOutPort.save(vacationRequest);

        List<VacationRequest> substituteVacationRequests = vacationRequest.substituteUser() == null
            ? List.of()
            : loadVacationRequestsOutPort.findAllByApplicantUserId(vacationRequest.substituteUser().id());

        return vacationRequest.isAutomaticallyValidAgainst(substituteVacationRequests);
    }
}
