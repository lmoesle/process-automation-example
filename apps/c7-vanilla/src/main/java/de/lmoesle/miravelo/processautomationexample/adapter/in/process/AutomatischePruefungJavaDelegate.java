package de.lmoesle.miravelo.processautomationexample.adapter.in.process;

import de.lmoesle.miravelo.processautomationexample.application.ports.in.UrlaubsantragAutomatischPruefenInPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.in.UrlaubsantragAutomatischPruefenInPort.UrlaubsantragAutomatischPruefenCommand;
import de.lmoesle.miravelo.processautomationexample.bpmn.VacationApprovalProcessApi;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragId;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@RequiredArgsConstructor
public class AutomatischePruefungJavaDelegate implements JavaDelegate {

    private static final String URLAUBSANTRAG_ID_VARIABLE = "urlaubsantragId";

    private final UrlaubsantragAutomatischPruefenInPort pruefeUrlaubsantragAutomatischInPort;

    @Override
    public void execute(DelegateExecution execution) {
        final String urlaubsantragId = ladeUrlaubsantragId(execution);
        final var gueltig = pruefeUrlaubsantragAutomatischInPort.pruefeUrlaubsantragAutomatisch(
            new UrlaubsantragAutomatischPruefenCommand(UrlaubsantragId.of(urlaubsantragId))
        );
        execution.setVariable(VacationApprovalProcessApi.Variables.AutomaticCheck.GUELTIG.getValue(), gueltig);
    }

    private String ladeUrlaubsantragId(DelegateExecution execution) {
        final Object rawValue = execution.getVariable(URLAUBSANTRAG_ID_VARIABLE);
        Assert.isInstanceOf(String.class, rawValue, "urlaubsantragId muss ein String sein");
        final String urlaubsantragId = (String) rawValue;
        Assert.hasText(urlaubsantragId, "urlaubsantragId darf nicht leer sein");
        return urlaubsantragId;
    }
}
