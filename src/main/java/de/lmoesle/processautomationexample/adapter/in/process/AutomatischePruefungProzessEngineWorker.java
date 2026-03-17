package de.lmoesle.processautomationexample.adapter.in.process;

import de.lmoesle.processautomationexample.application.ports.in.UrlaubsantragAutomatischPruefenInPort;
import de.lmoesle.processautomationexample.application.ports.in.UrlaubsantragAutomatischPruefenInPort.UrlaubsantragAutomatischPruefenCommand;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragId;
import dev.bpmcrafters.processengine.worker.ProcessEngineWorker;
import dev.bpmcrafters.processengine.worker.Variable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AutomatischePruefungProzessEngineWorker {

    private static final String TOPIC = "automatic_check";
    private static final String URLAUBSANTRAG_ID_VARIABLE = "vacationRequestId";

    private static final String GUELTIG_VARIABLE = "gueltig";

    private final UrlaubsantragAutomatischPruefenInPort pruefeUrlaubsantragAutomatischInPort;

    @ProcessEngineWorker(topic = TOPIC)
    public Map<String, Object> pruefeAutomatisch(
        @Variable(
            name = URLAUBSANTRAG_ID_VARIABLE
        ) final String urlaubsantragId
    ) {
        final var gueltig = pruefeUrlaubsantragAutomatischInPort.pruefeUrlaubsantragAutomatisch(
            new UrlaubsantragAutomatischPruefenCommand(UrlaubsantragId.of(urlaubsantragId))
        );
        return Map.of(GUELTIG_VARIABLE, gueltig);
    }
}
