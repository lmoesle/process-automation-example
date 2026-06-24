package de.lmoesle.miravelo.processautomationexample.adapter.in.process;

import de.lmoesle.miravelo.processautomationexample.application.ports.in.UrlaubsantragAutomatischPruefenInPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.in.UrlaubsantragAutomatischPruefenInPort.UrlaubsantragAutomatischPruefenCommand;
import de.lmoesle.miravelo.processautomationexample.bpmn.VacationApprovalProcessApi;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragId;
import dev.bpmcrafters.processengine.worker.ProcessEngineWorker;
import dev.bpmcrafters.processengine.worker.Variable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AutomatischePruefungProzessEngineWorker {

    private static final String URLAUBSANTRAG_ID_VARIABLE = "urlaubsantragId";

    private final UrlaubsantragAutomatischPruefenInPort pruefeUrlaubsantragAutomatischInPort;

    @ProcessEngineWorker(topic = VacationApprovalProcessApi.ServiceTasks.AUTOMATIC_CHECK)
    public Map<String, Object> pruefeAutomatisch(
        @Variable(
            name = URLAUBSANTRAG_ID_VARIABLE
        ) final String urlaubsantragId
    ) {
        final var gueltig = pruefeUrlaubsantragAutomatischInPort.pruefeUrlaubsantragAutomatisch(
            new UrlaubsantragAutomatischPruefenCommand(UrlaubsantragId.of(urlaubsantragId))
        );
        return Map.of(VacationApprovalProcessApi.Variables.AutomaticCheck.GUELTIG.getValue(), gueltig);
    }
}
