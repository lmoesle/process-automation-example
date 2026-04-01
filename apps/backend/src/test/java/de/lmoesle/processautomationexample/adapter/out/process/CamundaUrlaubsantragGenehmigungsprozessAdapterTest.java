package de.lmoesle.processautomationexample.adapter.out.process;

import de.lmoesle.processautomationexample.bpmn.VacationApprovalProcessApi;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
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
import java.util.concurrent.ExecutionException;

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

        var prozessinstanzId = camundaVacationApprovalProcessEngineAdapter.starteGenehmigungsprozessFuer(
            UrlaubsantragTestData.urlaubsantrag(),
            List.of(BenutzerTestdaten.adaId(), BenutzerTestdaten.carlaId())
        );

        ArgumentCaptor<StartProcessCommand> commandCaptor = ArgumentCaptor.forClass(StartProcessCommand.class);
        verify(startProcessApi).startProcess(commandCaptor.capture());

        StartProcessByDefinitionCmd command = (StartProcessByDefinitionCmd) commandCaptor.getValue();
        assertThat(command.getDefinitionKey()).isEqualTo(VacationApprovalProcessApi.PROCESS_ID);
        assertThat(command.get())
            .containsEntry(
                VacationApprovalProcessApi.Variables.URLAUBSANTRAG_ID,
                UrlaubsantragTestData.urlaubsantragId().value().toString()
            )
            .containsEntry(
                "teamLeadIds",
                BenutzerTestdaten.adaId().value() + "," + BenutzerTestdaten.carlaId().value()
            );
        assertThat(prozessinstanzId).isEqualTo(UrlaubsantragTestData.prozessinstanzId());
    }

    @Test
    void raisesErrorWhenStartingApprovalProcessFails() {
        when(startProcessApi.startProcess(any())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("boom")));

        assertThatThrownBy(() -> camundaVacationApprovalProcessEngineAdapter.starteGenehmigungsprozessFuer(
            UrlaubsantragTestData.urlaubsantrag(),
            List.of(BenutzerTestdaten.adaId())
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Genehmigungsprozess fuer Urlaubsantrag " + UrlaubsantragTestData.urlaubsantragId().value() + " konnte nicht gestartet werden")
            .hasCauseInstanceOf(ExecutionException.class)
            .hasRootCauseInstanceOf(RuntimeException.class);
    }
}
