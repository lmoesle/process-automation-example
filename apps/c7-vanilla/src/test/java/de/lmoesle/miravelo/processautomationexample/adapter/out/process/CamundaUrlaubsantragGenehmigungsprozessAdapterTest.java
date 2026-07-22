package de.lmoesle.miravelo.processautomationexample.adapter.out.process;

import de.lmoesle.miravelo.processautomationexample.bpmn.VacationApprovalProcessApi;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CamundaUrlaubsantragGenehmigungsprozessAdapterTest {

    private RuntimeService runtimeService;
    private CamundaUrlaubsantragGenehmigungsprozessAdapter camundaUrlaubsantragGenehmigungsprozessAdapter;

    @BeforeEach
    void setUp() {
        runtimeService = mock(RuntimeService.class);
        camundaUrlaubsantragGenehmigungsprozessAdapter = new CamundaUrlaubsantragGenehmigungsprozessAdapter(runtimeService);
    }

    @Test
    void startsApprovalProcess() {
        final ProcessInstance processInstance = mock(ProcessInstance.class);
        when(processInstance.getProcessInstanceId()).thenReturn(UrlaubsantragTestData.PROCESS_INSTANCE_ID_VALUE);
        when(runtimeService.startProcessInstanceByKey(
            eq(VacationApprovalProcessApi.PROCESS_ID.getValue()),
            eq(UrlaubsantragTestData.VACATION_REQUEST_UUID.toString()),
            anyMap()
        ))
            .thenReturn(processInstance);

        final var prozessinstanzId = camundaUrlaubsantragGenehmigungsprozessAdapter.starteGenehmigungsprozessFuer(
            UrlaubsantragTestData.urlaubsantrag(),
            List.of(BenutzerTestdaten.adaId(), BenutzerTestdaten.carlaId())
        );

        final ArgumentCaptor<Map<String, Object>> variablesCaptor = ArgumentCaptor.captor();
        verify(runtimeService).startProcessInstanceByKey(
            eq(VacationApprovalProcessApi.PROCESS_ID.getValue()),
            eq(UrlaubsantragTestData.VACATION_REQUEST_UUID.toString()),
            variablesCaptor.capture()
        );
        assertThat(variablesCaptor.getValue())
            .containsEntry(
                VacationApprovalProcessApi.Variables.AutomaticCheck.URLAUBSANTRAG_ID.getValue(),
                UrlaubsantragTestData.urlaubsantragId().value().toString()
            )
            .containsEntry(
                "teamLeadIds",
                List.of(BenutzerTestdaten.adaId().value().toString(), BenutzerTestdaten.carlaId().value().toString())
            );
        assertThat(prozessinstanzId).isEqualTo(UrlaubsantragTestData.prozessinstanzId());
    }

    @Test
    void raisesErrorWhenStartingApprovalProcessFails() {
        when(runtimeService.startProcessInstanceByKey(
            eq(VacationApprovalProcessApi.PROCESS_ID.getValue()),
            eq(UrlaubsantragTestData.VACATION_REQUEST_UUID.toString()),
            anyMap()
        ))
            .thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> camundaUrlaubsantragGenehmigungsprozessAdapter.starteGenehmigungsprozessFuer(
            UrlaubsantragTestData.urlaubsantrag(),
            List.of(BenutzerTestdaten.adaId())
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Genehmigungsprozess fuer Urlaubsantrag " + UrlaubsantragTestData.urlaubsantragId().value() + " konnte nicht gestartet werden")
            .hasRootCauseMessage("boom");
    }
}
