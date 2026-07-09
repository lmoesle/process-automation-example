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
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CamundaUrlaubsantragGenehmigungsprozessAdapterTest {

    private StartProcessApi startProcessApi;
    private RuntimeService runtimeService;
    private HistoryService historyService;
    private ProcessInstanceQuery processInstanceQuery;
    private HistoricProcessInstanceQuery historicProcessInstanceQuery;
    private CamundaUrlaubsantragGenehmigungsprozessAdapter camundaVacationApprovalProcessEngineAdapter;

    @BeforeEach
    void setUp() {
        startProcessApi = mock(StartProcessApi.class);
        runtimeService = mock(RuntimeService.class);
        historyService = mock(HistoryService.class);
        processInstanceQuery = mock(ProcessInstanceQuery.class);
        historicProcessInstanceQuery = mock(HistoricProcessInstanceQuery.class);
        when(runtimeService.createProcessInstanceQuery()).thenReturn(processInstanceQuery);
        when(processInstanceQuery.processDefinitionKey(VacationApprovalProcessApi.PROCESS_ID.getValue()))
            .thenReturn(processInstanceQuery);
        when(processInstanceQuery.processInstanceBusinessKey(UrlaubsantragTestData.VACATION_REQUEST_UUID.toString()))
            .thenReturn(processInstanceQuery);
        when(processInstanceQuery.active()).thenReturn(processInstanceQuery);
        when(processInstanceQuery.singleResult()).thenReturn(null);
        when(historyService.createHistoricProcessInstanceQuery()).thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.processDefinitionKey(VacationApprovalProcessApi.PROCESS_ID.getValue()))
            .thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.processInstanceBusinessKey(UrlaubsantragTestData.VACATION_REQUEST_UUID.toString()))
            .thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.orderByProcessInstanceStartTime()).thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.desc()).thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.listPage(0, 1)).thenReturn(List.of());
        camundaVacationApprovalProcessEngineAdapter = new CamundaUrlaubsantragGenehmigungsprozessAdapter(
            startProcessApi,
            runtimeService,
            historyService
        );
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
    void returnsExistingApprovalProcessByBusinessKey() {
        final ProcessInstance bestehendeProzessinstanz = mock(ProcessInstance.class);
        when(bestehendeProzessinstanz.getId()).thenReturn(UrlaubsantragTestData.PROCESS_INSTANCE_ID_VALUE);
        when(processInstanceQuery.singleResult()).thenReturn(bestehendeProzessinstanz);

        final var prozessinstanzId = camundaVacationApprovalProcessEngineAdapter.starteGenehmigungsprozess(
            UrlaubsantragTestData.urlaubsantragId(),
            List.of(BenutzerTestdaten.adaId())
        );

        verify(startProcessApi, never()).startProcess(any());
        assertThat(prozessinstanzId).isEqualTo(UrlaubsantragTestData.prozessinstanzId());
    }

    @Test
    void returnsExistingHistoricApprovalProcessByBusinessKey() {
        final HistoricProcessInstance bestehendeProzessinstanz = mock(HistoricProcessInstance.class);
        when(bestehendeProzessinstanz.getId()).thenReturn(UrlaubsantragTestData.PROCESS_INSTANCE_ID_VALUE);
        when(historicProcessInstanceQuery.listPage(0, 1)).thenReturn(List.of(bestehendeProzessinstanz));

        final var prozessinstanzId = camundaVacationApprovalProcessEngineAdapter.starteGenehmigungsprozess(
            UrlaubsantragTestData.urlaubsantragId(),
            List.of(BenutzerTestdaten.adaId())
        );

        verify(startProcessApi, never()).startProcess(any());
        assertThat(prozessinstanzId).isEqualTo(UrlaubsantragTestData.prozessinstanzId());
    }

    @Test
    void resolvesExistingApprovalProcessOnRetryAfterTimeout() {
        when(startProcessApi.startProcess(any())).thenReturn(CompletableFuture.failedFuture(new TimeoutException("timeout")));

        assertThatThrownBy(() -> camundaVacationApprovalProcessEngineAdapter.starteGenehmigungsprozess(
            UrlaubsantragTestData.urlaubsantragId(),
            List.of(BenutzerTestdaten.adaId())
        )).isInstanceOf(ProzessEngineAuftragUnklarException.class);

        final ProcessInstance bestehendeProzessinstanz = mock(ProcessInstance.class);
        when(bestehendeProzessinstanz.getId()).thenReturn(UrlaubsantragTestData.PROCESS_INSTANCE_ID_VALUE);
        when(processInstanceQuery.singleResult()).thenReturn(bestehendeProzessinstanz);

        final var prozessinstanzId = camundaVacationApprovalProcessEngineAdapter.starteGenehmigungsprozess(
            UrlaubsantragTestData.urlaubsantragId(),
            List.of(BenutzerTestdaten.adaId())
        );

        verify(startProcessApi).startProcess(any());
        assertThat(prozessinstanzId).isEqualTo(UrlaubsantragTestData.prozessinstanzId());
    }

    @Test
    void raisesErrorWhenStartingApprovalProcessFails() {
        when(startProcessApi.startProcess(any())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("boom")));

        assertThatThrownBy(() -> camundaVacationApprovalProcessEngineAdapter.starteGenehmigungsprozess(
            UrlaubsantragTestData.urlaubsantragId(),
            List.of(BenutzerTestdaten.adaId())
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Genehmigungsprozess fuer Urlaubsantrag " + UrlaubsantragTestData.urlaubsantragId().value() + " konnte nicht gestartet werden")
            .hasCauseInstanceOf(ExecutionException.class)
            .hasRootCauseInstanceOf(RuntimeException.class);
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
}
