package de.lmoesle.miravelo.processautomationexample.adapter.out.process;

import de.lmoesle.miravelo.processautomationexample.bpmn.VacationApprovalProcessApi;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import dev.bpmcrafters.processengineapi.CommonRestrictions;
import dev.bpmcrafters.processengineapi.process.ProcessInformation;
import dev.bpmcrafters.processengineapi.process.StartProcessApi;
import dev.bpmcrafters.processengineapi.process.StartProcessByDefinitionCmd;
import dev.bpmcrafters.processengineapi.process.StartProcessCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CamundaUrlaubsantragGenehmigungsprozessAdapterTest {

    private StartProcessApi startProcessApi;
    private CamundaUrlaubsantragGenehmigungsprozessAdapter camundaVacationApprovalProcessEngineAdapter;

    @BeforeEach
    void setUp() {
        startProcessApi = mock(StartProcessApi.class);
        camundaVacationApprovalProcessEngineAdapter = new CamundaUrlaubsantragGenehmigungsprozessAdapter(startProcessApi);
    }

    @Test
    void startsApprovalProcess() {
        when(startProcessApi.startProcess(any())).thenReturn(
            CompletableFuture.completedFuture(new ProcessInformation("process-instance-42", Map.of()))
        );

        final var prozessinstanzId = camundaVacationApprovalProcessEngineAdapter.starteGenehmigungsprozess(
            UrlaubsantragTestData.urlaubsantragId(),
            List.of(BenutzerTestdaten.adaId(), BenutzerTestdaten.carlaId())
        );

        final ArgumentCaptor<StartProcessCommand> commandCaptor = ArgumentCaptor.forClass(StartProcessCommand.class);
        verify(startProcessApi).startProcess(commandCaptor.capture());

        final StartProcessByDefinitionCmd command = (StartProcessByDefinitionCmd) commandCaptor.getValue();
        assertThat(command.getDefinitionKey()).isEqualTo(VacationApprovalProcessApi.PROCESS_ID.getValue());
        assertThat(command.get())
            .containsEntry(
                VacationApprovalProcessApi.Variables.AutomaticCheck.URLAUBSANTRAG_ID.getValue(),
                UrlaubsantragTestData.urlaubsantragId().value().toString()
            )
            .containsEntry(
                CommonRestrictions.BUSINESS_KEY,
                UrlaubsantragTestData.VACATION_REQUEST_UUID.toString()
            )
            .containsEntry(
                "teamLeadIds",
                BenutzerTestdaten.adaId().value() + "," + BenutzerTestdaten.carlaId().value()
            );
        assertThat(prozessinstanzId).isEqualTo(UrlaubsantragTestData.prozessinstanzId());
    }

    @Test
    void treatsFailedProcessStartAsUnclearEngineState() {
        when(startProcessApi.startProcess(any())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("boom")));

        assertThatThrownBy(() -> camundaVacationApprovalProcessEngineAdapter.starteGenehmigungsprozess(
            UrlaubsantragTestData.urlaubsantragId(),
            List.of(BenutzerTestdaten.adaId())
        ))
            .isInstanceOf(ProzessEngineAuftragUnklarException.class)
            .hasRootCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void treatsSynchronousProcessStartFailureAsUnclearEngineState() {
        when(startProcessApi.startProcess(any())).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> camundaVacationApprovalProcessEngineAdapter.starteGenehmigungsprozess(
            UrlaubsantragTestData.urlaubsantragId(),
            List.of(BenutzerTestdaten.adaId())
        ))
            .isInstanceOf(ProzessEngineAuftragUnklarException.class)
            .hasRootCauseMessage("boom");
    }

    @Test
    void treatsTimeoutFailureAsUnclearEngineState() {
        when(startProcessApi.startProcess(any())).thenReturn(CompletableFuture.failedFuture(new TimeoutException("timeout")));

        assertThatThrownBy(() -> camundaVacationApprovalProcessEngineAdapter.starteGenehmigungsprozess(
            UrlaubsantragTestData.urlaubsantragId(),
            List.of(BenutzerTestdaten.adaId())
        ))
            .isInstanceOf(ProzessEngineAuftragUnklarException.class)
            .hasRootCauseInstanceOf(TimeoutException.class);
    }

    @Test
    void treatsInterruptedWaitAsUnclearEngineStateAndRestoresInterrupt() {
        when(startProcessApi.startProcess(any())).thenReturn(new CompletableFuture<>());
        Thread.currentThread().interrupt();

        try {
            assertThatThrownBy(() -> camundaVacationApprovalProcessEngineAdapter.starteGenehmigungsprozess(
                UrlaubsantragTestData.urlaubsantragId(),
                List.of(BenutzerTestdaten.adaId())
            )).isInstanceOf(ProzessEngineAuftragUnklarException.class);
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
    }
}
