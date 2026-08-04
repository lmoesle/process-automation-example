package de.lmoesle.miravelo.processautomationexample.adapter.out.process;

import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.Model;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessService;
import org.kie.kogito.process.Processes;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KogitoUrlaubsantragGenehmigungsprozessAdapterTest {

    private Processes processes;
    private ProcessService processService;
    private Process<Model> process;
    private Model model;
    private KogitoUrlaubsantragGenehmigungsprozessAdapter adapter;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        processes = mock(Processes.class);
        processService = mock(ProcessService.class);
        process = mock(Process.class);
        model = mock(Model.class);
        doReturn(process)
            .when(processes)
            .processById(KogitoUrlaubsantragGenehmigungsprozessAdapter.PROCESS_ID);
        when(process.createModel()).thenReturn(model);
        adapter = new KogitoUrlaubsantragGenehmigungsprozessAdapter(processes, processService);
    }

    @Test
    void startsApprovalProcessWithBusinessKeyAndVariables() {
        final ProcessInstance<Model> processInstance = mock(ProcessInstance.class);
        when(processInstance.id()).thenReturn(UrlaubsantragTestData.PROCESS_INSTANCE_ID_VALUE);
        when(processService.createProcessInstance(
            process,
            UrlaubsantragTestData.VACATION_REQUEST_UUID.toString(),
            model,
            null
        )).thenReturn(processInstance);

        final var prozessinstanzId = adapter.starteGenehmigungsprozessFuer(
            UrlaubsantragTestData.urlaubsantrag(),
            List.of(BenutzerTestdaten.adaId(), BenutzerTestdaten.carlaId(), BenutzerTestdaten.adaId())
        );

        final ArgumentCaptor<Map<String, Object>> variablesCaptor = ArgumentCaptor.captor();
        verify(model).fromMap(variablesCaptor.capture());
        assertThat(variablesCaptor.getValue())
            .containsEntry("urlaubsantragId", UrlaubsantragTestData.VACATION_REQUEST_UUID.toString())
            .containsEntry(
                "teamLeadIds",
                BenutzerTestdaten.ADA_UUID + "," + BenutzerTestdaten.CARLA_UUID
            );
        verify(processService).createProcessInstance(
            process,
            UrlaubsantragTestData.VACATION_REQUEST_UUID.toString(),
            model,
            null
        );
        assertThat(prozessinstanzId).isEqualTo(UrlaubsantragTestData.prozessinstanzId());
    }

    @Test
    void raisesErrorWhenStartingApprovalProcessFails() {
        when(processService.createProcessInstance(
            process,
            UrlaubsantragTestData.VACATION_REQUEST_UUID.toString(),
            model,
            null
        )).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> adapter.starteGenehmigungsprozessFuer(
            UrlaubsantragTestData.urlaubsantrag(),
            List.of(BenutzerTestdaten.adaId())
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage(
                "Genehmigungsprozess fuer Urlaubsantrag "
                    + UrlaubsantragTestData.urlaubsantragId().value()
                    + " konnte nicht gestartet werden"
            )
            .hasRootCauseMessage("boom");
    }

    @Test
    void raisesErrorWhenProcessIsMissing() {
        when(processes.processById(KogitoUrlaubsantragGenehmigungsprozessAdapter.PROCESS_ID)).thenReturn(null);

        assertThatThrownBy(() -> adapter.starteGenehmigungsprozessFuer(
            UrlaubsantragTestData.urlaubsantrag(),
            List.of(BenutzerTestdaten.adaId())
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasRootCauseMessage("Kogito-Prozess vacation_approval wurde nicht gefunden");
    }
}
