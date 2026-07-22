package de.lmoesle.miravelo.processautomationexample.adapter.in.process;

import de.lmoesle.miravelo.processautomationexample.application.ports.in.UrlaubsantragAutomatischPruefenInPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.in.UrlaubsantragAutomatischPruefenInPort.UrlaubsantragAutomatischPruefenCommand;
import de.lmoesle.miravelo.processautomationexample.bpmn.VacationApprovalProcessApi;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class AutomatischePruefungJavaDelegateTest {

    private UrlaubsantragAutomatischPruefenInPort pruefeUrlaubsantragAutomatischInPort;
    private AutomatischePruefungJavaDelegate automatischePruefungJavaDelegate;

    @BeforeEach
    void setUp() {
        pruefeUrlaubsantragAutomatischInPort = mock(UrlaubsantragAutomatischPruefenInPort.class);
        automatischePruefungJavaDelegate = new AutomatischePruefungJavaDelegate(pruefeUrlaubsantragAutomatischInPort);
    }

    @Test
    void passesUrlaubsantragIdToInPortAndStoresGueltigVariable() throws Exception {
        final DelegateExecution execution = mock(DelegateExecution.class);
        final String urlaubsantragId = UrlaubsantragTestData.urlaubsantragId().value().toString();
        when(execution.getVariable(VacationApprovalProcessApi.Variables.AutomaticCheck.URLAUBSANTRAG_ID.getValue()))
            .thenReturn(urlaubsantragId);
        when(pruefeUrlaubsantragAutomatischInPort.pruefeUrlaubsantragAutomatisch(
            new UrlaubsantragAutomatischPruefenCommand(UrlaubsantragTestData.urlaubsantragId())
        )).thenReturn(true);

        automatischePruefungJavaDelegate.execute(execution);

        verify(pruefeUrlaubsantragAutomatischInPort).pruefeUrlaubsantragAutomatisch(
            new UrlaubsantragAutomatischPruefenCommand(UrlaubsantragTestData.urlaubsantragId())
        );
        verify(execution).setVariable(VacationApprovalProcessApi.Variables.AutomaticCheck.GUELTIG.getValue(), true);
        verifyNoMoreInteractions(pruefeUrlaubsantragAutomatischInPort);
    }

    @Test
    void rejectsMissingUrlaubsantragIdVariable() {
        final DelegateExecution execution = mock(DelegateExecution.class);

        assertThatThrownBy(() -> automatischePruefungJavaDelegate.execute(execution))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("urlaubsantragId muss ein String sein");
    }
}
