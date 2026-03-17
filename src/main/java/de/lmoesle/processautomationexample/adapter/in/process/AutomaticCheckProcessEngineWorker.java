package de.lmoesle.processautomationexample.adapter.in.process;

import de.lmoesle.processautomationexample.application.ports.in.AutomaticCheckVacationRequestInPort;
import de.lmoesle.processautomationexample.application.ports.in.AutomaticCheckVacationRequestInPort.AutomaticCheckVacationRequestCommand;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestId;
import dev.bpmcrafters.processengine.worker.ProcessEngineWorker;
import dev.bpmcrafters.processengine.worker.Variable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AutomaticCheckProcessEngineWorker {

    private static final String TOPIC = "automatic_check";
    private static final String VACATION_REQUEST_ID_VARIABLE = "vacationRequestId";

    private static final String GUELTIG_VARIABLE = "gueltig";

    private final AutomaticCheckVacationRequestInPort automaticCheckVacationRequestInPort;

    @ProcessEngineWorker(topic = TOPIC)
    public Map<String, Object> automaticCheck(
        @Variable(
            name = VACATION_REQUEST_ID_VARIABLE
        ) final String vacationRequestId
    ) {
        final var gueltig = automaticCheckVacationRequestInPort.automaticCheckVacationRequest(
            new AutomaticCheckVacationRequestCommand(VacationRequestId.of(vacationRequestId))
        );
        return Map.of(GUELTIG_VARIABLE, gueltig);
    }
}
