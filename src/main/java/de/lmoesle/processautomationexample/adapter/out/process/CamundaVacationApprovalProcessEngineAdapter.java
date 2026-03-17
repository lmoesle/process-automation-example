package de.lmoesle.processautomationexample.adapter.out.process;

import de.lmoesle.processautomationexample.application.ports.out.StartVacationApprovalProcessOutPort;
import de.lmoesle.processautomationexample.domain.vacationrequest.ProcessInstanceId;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequest;
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
public class CamundaVacationApprovalProcessEngineAdapter implements StartVacationApprovalProcessOutPort {

    private static final long PROCESS_START_TIMEOUT_SECONDS = 10;
    private static final String PROCESS_DEFINITION_KEY = "vacation_approval";
    private static final String VACATION_REQUEST_ID_VARIABLE = "vacationRequestId";

    private final StartProcessApi startProcessApi;

    @Override
    public ProcessInstanceId startApprovalProcessFor(VacationRequest vacationRequest) {
        final var processInstanceInfo = startProcessApi.startProcess(new StartProcessByDefinitionCmd(
            PROCESS_DEFINITION_KEY,
            () -> Map.of(VACATION_REQUEST_ID_VARIABLE, vacationRequest.id().value().toString()),
            Map.of()
        ));

        try {
            ProcessInformation resolvedProcessInstanceInfo = processInstanceInfo.get(
                PROCESS_START_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            );
            return ProcessInstanceId.of(resolvedProcessInstanceInfo.getInstanceId());
        } catch (TimeoutException | InterruptedException | IllegalStateException | ExecutionException  exception) {
            throw new IllegalStateException(
                "Failed to start approval process for vacation request " + vacationRequest.id().value(),
                exception
            );
        }
    }
}
