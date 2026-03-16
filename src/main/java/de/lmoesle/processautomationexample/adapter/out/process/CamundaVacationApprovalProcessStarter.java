package de.lmoesle.processautomationexample.adapter.out.process;

import de.lmoesle.processautomationexample.application.ports.out.StartVacationApprovalProcessOutPort;
import de.lmoesle.processautomationexample.domain.vacationrequest.ProcessInstanceId;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequest;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CamundaVacationApprovalProcessStarter implements StartVacationApprovalProcessOutPort {

    private static final String PROCESS_DEFINITION_KEY = "vacation_approval";
    private static final String BPMN_RESOURCE = "bpmn/vacation-approval.bpmn";

    private final RuntimeService runtimeService;
    private final RepositoryService repositoryService;

    @Override
    public ProcessInstanceId startApprovalProcessFor(VacationRequest vacationRequest) {
        deployProcessDefinitionIfMissing();

        return ProcessInstanceId.of(runtimeService.startProcessInstanceByKey(
            PROCESS_DEFINITION_KEY,
            vacationRequest.id().value().toString(),
            buildVariables(vacationRequest)
        ).getProcessInstanceId());
    }

    private void deployProcessDefinitionIfMissing() {
        long deployedDefinitions = repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey(PROCESS_DEFINITION_KEY)
            .count();

        if (deployedDefinitions > 0) {
            return;
        }

        repositoryService.createDeployment()
            .name(PROCESS_DEFINITION_KEY)
            .enableDuplicateFiltering(true)
            .addClasspathResource(BPMN_RESOURCE)
            .deploy();
    }

    private Map<String, Object> buildVariables(VacationRequest vacationRequest) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("vacationRequestId", vacationRequest.id().value().toString());
        variables.put("from", vacationRequest.period().from().toString());
        variables.put("to", vacationRequest.period().to().toString());
        variables.put("applicantUserId", vacationRequest.applicantUserId().value().toString());

        if (vacationRequest.substituteUserId() != null) {
            variables.put("substituteUserId", vacationRequest.substituteUserId().value().toString());
        }

        return variables;
    }
}
