package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.LoadVacationRequestsForUserInPort.LoadVacationRequestsForUserCommand;
import de.lmoesle.processautomationexample.application.ports.out.LoadVacationRequestsOutPort;
import de.lmoesle.processautomationexample.domain.user.UserTestData;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoadVacationRequestsForUserUseCaseTest {

    private LoadVacationRequestsOutPort loadVacationRequestsOutPort;
    private LoadVacationRequestsForUserUseCase loadVacationRequestsForUserUseCase;

    @BeforeEach
    void setUp() {
        loadVacationRequestsOutPort = mock(LoadVacationRequestsOutPort.class);
        loadVacationRequestsForUserUseCase = new LoadVacationRequestsForUserUseCase(loadVacationRequestsOutPort);
    }

    @Test
    void loadsVacationRequestsForUser() {
        var expectedVacationRequests = List.of(
            VacationRequestTestData.vacationRequest(
                VacationRequestTestData.vacationRequestId(),
                VacationRequestTestData.vacationPeriod(),
                UserTestData.ada(),
                UserTestData.carla(),
                VacationRequestTestData.processInstanceId()
            )
        );
        when(loadVacationRequestsOutPort.findAllByApplicantUserId(UserTestData.adaId()))
            .thenReturn(expectedVacationRequests);

        var vacationRequests = loadVacationRequestsForUserUseCase.loadVacationRequestsForUser(
            new LoadVacationRequestsForUserCommand(UserTestData.adaId())
        );

        verify(loadVacationRequestsOutPort).findAllByApplicantUserId(UserTestData.adaId());
        assertThat(vacationRequests).containsExactlyElementsOf(expectedVacationRequests);
    }

    @Test
    void rejectsNullCommand() {
        assertThatThrownBy(() -> loadVacationRequestsForUserUseCase.loadVacationRequestsForUser(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("command must not be null");
    }

    @Test
    void rejectsNullUserId() {
        assertThatThrownBy(() -> loadVacationRequestsForUserUseCase.loadVacationRequestsForUser(
            new LoadVacationRequestsForUserCommand(null)
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("userId must not be null");
    }
}
