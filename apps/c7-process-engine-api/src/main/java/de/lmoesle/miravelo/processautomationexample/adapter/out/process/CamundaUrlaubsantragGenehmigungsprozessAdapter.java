package de.lmoesle.miravelo.processautomationexample.adapter.out.process;

import de.lmoesle.miravelo.processautomationexample.application.ports.out.StarteGenehmigungsprozessDirektOutPort;
import de.lmoesle.miravelo.processautomationexample.bpmn.VacationApprovalProcessApi;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.ProzessinstanzId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragId;
import dev.bpmcrafters.processengineapi.CommonRestrictions;
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
public class CamundaUrlaubsantragGenehmigungsprozessAdapter implements StarteGenehmigungsprozessDirektOutPort {

    private static final long PROCESS_START_TIMEOUT_SECONDS = 10;
    private static final String TEAM_LEAD_VARIABLE = "teamLeadIds";

    private final StartProcessApi startProcessApi;

    @Override
    public ProzessinstanzId starteGenehmigungsprozess(UrlaubsantragId urlaubsantragId, List<BenutzerId> teamLeadIds) {
        final String businessKey = businessKey(urlaubsantragId);
        final var startCommand = new StartProcessByDefinitionCmd(
            VacationApprovalProcessApi.PROCESS_ID.getValue(),
            () -> Map.of(
                VacationApprovalProcessApi.Variables.AutomaticCheck.URLAUBSANTRAG_ID.getValue(), urlaubsantragId.value().toString(),
                CommonRestrictions.BUSINESS_KEY, businessKey,
                TEAM_LEAD_VARIABLE, mapTeamLeadIds(teamLeadIds)
            ),
            Map.of()
        );

        try {
            final var prozessinstanzInfo = startProcessApi.startProcess(startCommand);
            final ProcessInformation aufgeloesteProzessinstanzInfo = prozessinstanzInfo.get(
                PROCESS_START_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            );
            return ProzessinstanzId.of(aufgeloesteProzessinstanzInfo.getInstanceId());
        } catch (TimeoutException exception) {
            throw new ProzessEngineAuftragUnklarException(
                "Genehmigungsprozess fuer Urlaubsantrag " + urlaubsantragId.value()
                    + " konnte innerhalb des Timeouts nicht eindeutig gestartet werden",
                exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ProzessEngineAuftragUnklarException(
                "Genehmigungsprozess fuer Urlaubsantrag " + urlaubsantragId.value()
                    + " konnte wegen einer Unterbrechung nicht eindeutig gestartet werden",
                exception
            );
        } catch (ExecutionException | RuntimeException exception) {
            throw new ProzessEngineAuftragUnklarException(
                "Genehmigungsprozess fuer Urlaubsantrag " + urlaubsantragId.value()
                    + " konnte nicht eindeutig gestartet werden",
                exception
            );
        }
    }

    private String businessKey(UrlaubsantragId urlaubsantragId) {
        return urlaubsantragId.value().toString();
    }

    private String mapTeamLeadIds(List<BenutzerId> teamLeadIds) {
        return teamLeadIds.stream()
            .map(benutzerId -> benutzerId.value().toString())
            .distinct()
            .reduce((left, right) -> left + "," + right)
            .orElse("");
    }

}
