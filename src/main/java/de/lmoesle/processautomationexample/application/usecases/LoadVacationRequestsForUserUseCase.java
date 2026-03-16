package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.LoadVacationRequestsForUserInPort;
import de.lmoesle.processautomationexample.application.ports.out.LoadVacationRequestsOutPort;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoadVacationRequestsForUserUseCase implements LoadVacationRequestsForUserInPort {

    private final LoadVacationRequestsOutPort loadVacationRequestsOutPort;

    @Override
    public List<VacationRequest> loadVacationRequestsForUser(LoadVacationRequestsForUserCommand command) {
        Assert.notNull(command, "command must not be null");
        Assert.notNull(command.userId(), "userId must not be null");
        return loadVacationRequestsOutPort.findAllByApplicantUserId(command.userId());
    }
}
