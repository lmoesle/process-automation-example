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

    private final UrlaubsantragAutomatischPruefenInPort pruefeUrlaubsantragAutomatischInPort;

    @ProcessEngineWorker(topic = VacationApprovalProcessApi.TaskTypes.AUTOMATIC_CHECK)
    public Map<String, Object> pruefeAutomatisch(
        @Variable(
            name = VacationApprovalProcessApi.Variables.URLAUBSANTRAG_ID
        ) final String urlaubsantragId
    ) {
        final var gueltig = pruefeUrlaubsantragAutomatischInPort.pruefeUrlaubsantragAutomatisch(
            new UrlaubsantragAutomatischPruefenCommand(UrlaubsantragId.of(urlaubsantragId))
        );
        return Map.of(VacationApprovalProcessApi.Variables.GUELTIG, gueltig);
    }
}
