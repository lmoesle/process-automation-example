package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.AutomaticCheckVacationRequestInPort.AutomaticCheckVacationRequestCommand;
import de.lmoesle.processautomationexample.application.ports.out.LoadVacationRequestsOutPort;
import de.lmoesle.processautomationexample.application.ports.out.SaveVacationRequestOutPort;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequest;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestStatus;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestStatusHistoryEntry;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestTestData;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class AutomaticCheckVacationRequestUseCaseTest {

    private LoadVacationRequestsOutPort loadVacationRequestsOutPort;
    private SaveVacationRequestOutPort saveVacationRequestOutPort;
    private AutomaticCheckVacationRequestUseCase automaticCheckVacationRequestUseCase;

    @BeforeEach
    void setUp() {
        loadVacationRequestsOutPort = mock(LoadVacationRequestsOutPort.class);
        saveVacationRequestOutPort = mock(SaveVacationRequestOutPort.class);
        automaticCheckVacationRequestUseCase = new AutomaticCheckVacationRequestUseCase(
            loadVacationRequestsOutPort,
            saveVacationRequestOutPort
        );
    }

    @Test
    void returnsTrueWhenNoSubstituteUserExists() {
        VacationRequest vacationRequestWithoutSubstitute = VacationRequest.submit(
            VacationRequestTestData.FROM,
            VacationRequestTestData.TO,
            VacationRequestTestData.applicantUser(),
            null
        );
        when(loadVacationRequestsOutPort.findById(vacationRequestWithoutSubstitute.id()))
            .thenReturn(Optional.of(vacationRequestWithoutSubstitute));

        boolean result = automaticCheckVacationRequestUseCase.automaticCheckVacationRequest(
            new AutomaticCheckVacationRequestCommand(vacationRequestWithoutSubstitute.id())
        );

        assertThat(result).isTrue();
        verify(loadVacationRequestsOutPort).findById(vacationRequestWithoutSubstitute.id());
        ArgumentCaptor<VacationRequest> savedCaptor = ArgumentCaptor.forClass(VacationRequest.class);
        verify(saveVacationRequestOutPort).save(savedCaptor.capture());
        assertThat(savedCaptor.getValue().status()).isEqualTo(VacationRequestStatus.AUTOMATISCHE_PRUEFUNG);
        assertThat(savedCaptor.getValue().statusHistory()).hasSize(2)
            .last()
            .satisfies(entry -> assertThat(entry.comment()).isNull());
        verifyNoMoreInteractions(loadVacationRequestsOutPort);
        verifyNoMoreInteractions(saveVacationRequestOutPort);
    }

    @Test
    void returnsTrueWhenSubstituteUserHasNoOverlappingVacationRequest() {
        when(loadVacationRequestsOutPort.findById(VacationRequestTestData.vacationRequestId()))
            .thenReturn(Optional.of(VacationRequestTestData.vacationRequest()));
        when(loadVacationRequestsOutPort.findAllByApplicantUserId(VacationRequestTestData.substituteUserId()))
            .thenReturn(List.of(VacationRequestTestData.secondVacationRequest(VacationRequestTestData.substituteUser(), null)));

        boolean result = automaticCheckVacationRequestUseCase.automaticCheckVacationRequest(
            new AutomaticCheckVacationRequestCommand(VacationRequestTestData.vacationRequestId())
        );

        assertThat(result).isTrue();
        verify(loadVacationRequestsOutPort).findById(VacationRequestTestData.vacationRequestId());
        verify(loadVacationRequestsOutPort).findAllByApplicantUserId(VacationRequestTestData.substituteUserId());
        verify(saveVacationRequestOutPort).save(any(VacationRequest.class));
        verifyNoMoreInteractions(loadVacationRequestsOutPort);
        verifyNoMoreInteractions(saveVacationRequestOutPort);
    }

    @Test
    void returnsFalseWhenSubstituteUserHasOverlappingVacationRequest() {
        VacationRequest overlappingVacationRequest = VacationRequestTestData.vacationRequest(
            VacationRequestTestData.secondVacationRequestId(),
            VacationRequestTestData.vacationPeriod(),
            VacationRequestTestData.substituteUser(),
            null,
            VacationRequestTestData.secondProcessInstanceId()
        );
        when(loadVacationRequestsOutPort.findById(VacationRequestTestData.vacationRequestId()))
            .thenReturn(Optional.of(VacationRequestTestData.vacationRequest()));
        when(loadVacationRequestsOutPort.findAllByApplicantUserId(VacationRequestTestData.substituteUserId()))
            .thenReturn(List.of(overlappingVacationRequest));

        boolean result = automaticCheckVacationRequestUseCase.automaticCheckVacationRequest(
            new AutomaticCheckVacationRequestCommand(VacationRequestTestData.vacationRequestId())
        );

        assertThat(result).isFalse();
        verify(loadVacationRequestsOutPort).findById(VacationRequestTestData.vacationRequestId());
        verify(loadVacationRequestsOutPort).findAllByApplicantUserId(VacationRequestTestData.substituteUserId());
        verify(saveVacationRequestOutPort).save(any(VacationRequest.class));
        verifyNoMoreInteractions(loadVacationRequestsOutPort);
        verifyNoMoreInteractions(saveVacationRequestOutPort);
    }

    @Test
    void rejectsNullCommand() {
        assertThatThrownBy(() -> automaticCheckVacationRequestUseCase.automaticCheckVacationRequest(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("command must not be null");
    }

    @Test
    void rejectsNullVacationRequestId() {
        assertThatThrownBy(() -> automaticCheckVacationRequestUseCase.automaticCheckVacationRequest(
            new AutomaticCheckVacationRequestCommand(null)
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("vacationRequestId must not be null");
    }

    @Test
    void rejectsMissingVacationRequest() {
        when(loadVacationRequestsOutPort.findById(VacationRequestTestData.vacationRequestId()))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> automaticCheckVacationRequestUseCase.automaticCheckVacationRequest(
            new AutomaticCheckVacationRequestCommand(VacationRequestTestData.vacationRequestId())
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("vacationRequestId does not reference an existing vacation request");
    }
}
