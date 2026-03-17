package de.lmoesle.processautomationexample.adapter.out.process;

import de.lmoesle.processautomationexample.application.ports.out.UrlaubsantragGenehmigungsprozessStartenOutPort;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.ProzessinstanzId;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import dev.bpmcrafters.processengineapi.process.ProcessInformation;
import dev.bpmcrafters.processengineapi.process.StartProcessApi;
import dev.bpmcrafters.processengineapi.process.StartProcessByDefinitionCmd;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class CamundaUrlaubsantragGenehmigungsprozessAdapter implements UrlaubsantragGenehmigungsprozessStartenOutPort {

    private static final long PROCESS_START_TIMEOUT_SECONDS = 10;
    private static final String PROCESS_DEFINITION_KEY = "vacation_approval";
    private static final String URLAUBSANTRAG_ID_VARIABLE = "vacationRequestId";

    private final StartProcessApi startProcessApi;

    @Override
    public ProzessinstanzId starteGenehmigungsprozessFuer(Urlaubsantrag urlaubsantrag) {
        final var prozessinstanzInfo = startProcessApi.startProcess(new StartProcessByDefinitionCmd(
            PROCESS_DEFINITION_KEY,
            () -> Map.of(URLAUBSANTRAG_ID_VARIABLE, urlaubsantrag.id().value().toString()),
            Map.of()
        ));

        try {
            ProcessInformation aufgeloesteProzessinstanzInfo = prozessinstanzInfo.get(
                PROCESS_START_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            );
            return ProzessinstanzId.of(aufgeloesteProzessinstanzInfo.getInstanceId());
        } catch (TimeoutException | InterruptedException | IllegalStateException | ExecutionException  exception) {
            throw new IllegalStateException(
                "Genehmigungsprozess fuer Urlaubsantrag " + urlaubsantrag.id().value() + " konnte nicht gestartet werden",
                exception
            );
        }
    }
}
