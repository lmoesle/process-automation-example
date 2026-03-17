package de.lmoesle.processautomationexample.adapter.out.process;

import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestTestData;
import dev.bpmcrafters.processengineapi.process.ProcessInformation;
import dev.bpmcrafters.processengineapi.process.StartProcessApi;
import dev.bpmcrafters.processengineapi.process.StartProcessByDefinitionCmd;
import dev.bpmcrafters.processengineapi.process.StartProcessCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CamundaVacationApprovalProcessEngineAdapterTest {

    private StartProcessApi startProcessApi;
    private CamundaVacationApprovalProcessEngineAdapter camundaVacationApprovalProcessEngineAdapter;

    @BeforeEach
    void setUp() {
        startProcessApi = mock(StartProcessApi.class);
        camundaVacationApprovalProcessEngineAdapter = new CamundaVacationApprovalProcessEngineAdapter(startProcessApi);
    }

    @Test
    void startsApprovalProcess() {
        when(startProcessApi.startProcess(any())).thenReturn(
            CompletableFuture.completedFuture(new ProcessInformation("process-instance-42", Map.of()))
        );

        var processInstanceId = camundaVacationApprovalProcessEngineAdapter.startApprovalProcessFor(
            VacationRequestTestData.vacationRequest()
        );

        ArgumentCaptor<StartProcessCommand> commandCaptor = ArgumentCaptor.forClass(StartProcessCommand.class);
        verify(startProcessApi).startProcess(commandCaptor.capture());

        StartProcessByDefinitionCmd command = (StartProcessByDefinitionCmd) commandCaptor.getValue();
        assertThat(command.getDefinitionKey()).isEqualTo("vacation_approval");
        assertThat(command.get())
            .containsEntry("vacationRequestId", VacationRequestTestData.vacationRequestId().value().toString());
        assertThat(processInstanceId).isEqualTo(VacationRequestTestData.processInstanceId());
    }

    @Test
    void raisesErrorWhenStartingApprovalProcessFails() {
        when(startProcessApi.startProcess(any())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("boom")));

        assertThatThrownBy(() -> camundaVacationApprovalProcessEngineAdapter.startApprovalProcessFor(
            VacationRequestTestData.vacationRequest()
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Failed to start approval process for vacation request " + VacationRequestTestData.vacationRequestId().value())
            .hasCauseInstanceOf(ExecutionException.class)
            .hasRootCauseInstanceOf(RuntimeException.class);
    }
}
