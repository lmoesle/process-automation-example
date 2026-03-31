package de.lmoesle.processautomationexample.adapter.in.process;

import de.lmoesle.processautomationexample.application.ports.in.UrlaubsantragAutomatischPruefenInPort;
import de.lmoesle.processautomationexample.application.ports.in.UrlaubsantragAutomatischPruefenInPort.UrlaubsantragAutomatischPruefenCommand;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragId;
import de.lmoesle.processautomationexample.shared.bpmn.VacationApprovalBpmnApi;
import dev.bpmcrafters.processengine.worker.ProcessEngineWorker;
import dev.bpmcrafters.processengine.worker.Variable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AutomatischePruefungProzessEngineWorker {

    private final UrlaubsantragAutomatischPruefenInPort pruefeUrlaubsantragAutomatischInPort;

    @ProcessEngineWorker(topic = VacationApprovalBpmnApi.AUTOMATIC_CHECK_TASK_TOPIC)
    public Map<String, Object> pruefeAutomatisch(
        @Variable(
            name = VacationApprovalBpmnApi.PROCESS_VARIABLE_VACATION_REQUEST_ID
        ) final String urlaubsantragId
    ) {
        final var gueltig = pruefeUrlaubsantragAutomatischInPort.pruefeUrlaubsantragAutomatisch(
            new UrlaubsantragAutomatischPruefenCommand(UrlaubsantragId.of(urlaubsantragId))
        );
        return Map.of(VacationApprovalBpmnApi.PROCESS_VARIABLE_VALID, gueltig);
    }
}
