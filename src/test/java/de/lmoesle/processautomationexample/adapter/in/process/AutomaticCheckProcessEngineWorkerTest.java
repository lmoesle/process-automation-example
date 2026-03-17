package de.lmoesle.processautomationexample.adapter.in.process;

import de.lmoesle.processautomationexample.application.ports.in.AutomaticCheckVacationRequestInPort;
import de.lmoesle.processautomationexample.application.ports.in.AutomaticCheckVacationRequestInPort.AutomaticCheckVacationRequestCommand;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestTestData;
import dev.bpmcrafters.processengine.worker.registrar.ReflectionUtilsKt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class AutomaticCheckProcessEngineWorkerTest {

    private AutomaticCheckVacationRequestInPort automaticCheckVacationRequestInPort;
    private AutomaticCheckProcessEngineWorker automaticCheckProcessEngineWorker;

    @BeforeEach
    void setUp() {
        automaticCheckVacationRequestInPort = mock(AutomaticCheckVacationRequestInPort.class);
        automaticCheckProcessEngineWorker = new AutomaticCheckProcessEngineWorker(automaticCheckVacationRequestInPort);
    }

    @Test
    void passesVacationRequestIdToInPortAndReturnsGueltigVariable() {
        String vacationRequestId = VacationRequestTestData.vacationRequestId().value().toString();
        when(automaticCheckVacationRequestInPort.automaticCheckVacationRequest(
            new AutomaticCheckVacationRequestCommand(VacationRequestTestData.vacationRequestId())
        )).thenReturn(true);

        var result = automaticCheckProcessEngineWorker.automaticCheck(vacationRequestId);

        assertThat(result)
            .containsEntry("gueltig", true)
            .hasSize(1);
        verify(automaticCheckVacationRequestInPort).automaticCheckVacationRequest(
            new AutomaticCheckVacationRequestCommand(VacationRequestTestData.vacationRequestId())
        );
        verifyNoMoreInteractions(automaticCheckVacationRequestInPort);
    }

    @Test
    void exposesReturnTypeAsWorkerPayloadForProcessVariables() throws NoSuchMethodException {
        var method = AutomaticCheckProcessEngineWorker.class.getMethod("automaticCheck", String.class);

        assertThat(ReflectionUtilsKt.hasPayloadReturnType(method)).isTrue();
    }
}
