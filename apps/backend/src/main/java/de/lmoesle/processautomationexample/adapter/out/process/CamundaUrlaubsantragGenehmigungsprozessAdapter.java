package de.lmoesle.processautomationexample.adapter.out.process;

import de.lmoesle.processautomationexample.application.ports.out.UrlaubsantragGenehmigungsprozessStartenOutPort;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.ProzessinstanzId;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import de.lmoesle.processautomationexample.shared.bpmn.VacationApprovalBpmnApi;
import dev.bpmcrafters.processengineapi.process.ProcessInformation;
import dev.bpmcrafters.processengineapi.process.StartProcessApi;
import dev.bpmcrafters.processengineapi.process.StartProcessByDefinitionCmd;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class CamundaUrlaubsantragGenehmigungsprozessAdapter implements UrlaubsantragGenehmigungsprozessStartenOutPort {

    private static final long PROCESS_START_TIMEOUT_SECONDS = 10;

    private final StartProcessApi startProcessApi;

    @Override
    public ProzessinstanzId starteGenehmigungsprozessFuer(Urlaubsantrag urlaubsantrag, List<BenutzerId> teamLeadIds) {
        final var prozessinstanzInfo = startProcessApi.startProcess(new StartProcessByDefinitionCmd(
            VacationApprovalBpmnApi.PROCESS_ID,
            () -> Map.of(
                VacationApprovalBpmnApi.PROCESS_VARIABLE_VACATION_REQUEST_ID, urlaubsantrag.id().value().toString(),
                VacationApprovalBpmnApi.PROCESS_VARIABLE_TEAM_LEAD_IDS, mapTeamLeadIds(teamLeadIds)
            ),
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

    private String mapTeamLeadIds(List<BenutzerId> teamLeadIds) {
        return teamLeadIds.stream()
            .map(benutzerId -> benutzerId.value().toString())
            .distinct()
            .reduce((left, right) -> left + "," + right)
            .orElse("");
    }
}
