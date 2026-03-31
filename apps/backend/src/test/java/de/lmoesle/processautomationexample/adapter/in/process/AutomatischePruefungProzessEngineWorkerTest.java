package de.lmoesle.processautomationexample.adapter.in.process;

import de.lmoesle.processautomationexample.application.ports.in.UrlaubsantragAutomatischPruefenInPort;
import de.lmoesle.processautomationexample.application.ports.in.UrlaubsantragAutomatischPruefenInPort.UrlaubsantragAutomatischPruefenCommand;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import de.lmoesle.processautomationexample.shared.bpmn.VacationApprovalBpmnApi;
import dev.bpmcrafters.processengine.worker.registrar.ReflectionUtilsKt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class AutomatischePruefungProzessEngineWorkerTest {

    private UrlaubsantragAutomatischPruefenInPort pruefeUrlaubsantragAutomatischInPort;
    private AutomatischePruefungProzessEngineWorker automatischePruefungProzessEngineWorker;

    @BeforeEach
    void setUp() {
        pruefeUrlaubsantragAutomatischInPort = mock(UrlaubsantragAutomatischPruefenInPort.class);
        automatischePruefungProzessEngineWorker = new AutomatischePruefungProzessEngineWorker(pruefeUrlaubsantragAutomatischInPort);
    }

    @Test
    void passesUrlaubsantragIdToInPortAndReturnsGueltigVariable() {
        String urlaubsantragId = UrlaubsantragTestData.urlaubsantragId().value().toString();
        when(pruefeUrlaubsantragAutomatischInPort.pruefeUrlaubsantragAutomatisch(
            new UrlaubsantragAutomatischPruefenCommand(UrlaubsantragTestData.urlaubsantragId())
        )).thenReturn(true);

        var result = automatischePruefungProzessEngineWorker.pruefeAutomatisch(urlaubsantragId);

        assertThat(result)
            .containsEntry(VacationApprovalBpmnApi.PROCESS_VARIABLE_VALID, true)
            .hasSize(1);
        verify(pruefeUrlaubsantragAutomatischInPort).pruefeUrlaubsantragAutomatisch(
            new UrlaubsantragAutomatischPruefenCommand(UrlaubsantragTestData.urlaubsantragId())
        );
        verifyNoMoreInteractions(pruefeUrlaubsantragAutomatischInPort);
    }

    @Test
    void exposesReturnTypeAsWorkerPayloadForProcessVariables() throws NoSuchMethodException {
        var method = AutomatischePruefungProzessEngineWorker.class.getMethod("pruefeAutomatisch", String.class);

        assertThat(ReflectionUtilsKt.hasPayloadReturnType(method)).isTrue();
    }
}
