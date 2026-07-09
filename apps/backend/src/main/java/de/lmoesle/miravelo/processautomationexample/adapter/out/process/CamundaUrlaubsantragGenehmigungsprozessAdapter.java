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
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class CamundaUrlaubsantragGenehmigungsprozessAdapter implements StarteGenehmigungsprozessDirektOutPort {

    private static final long PROCESS_START_TIMEOUT_SECONDS = 10;
    private static final String TEAM_LEAD_VARIABLE = "teamLeadIds";

    private final StartProcessApi startProcessApi;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;

    @Override
    public ProzessinstanzId starteGenehmigungsprozess(UrlaubsantragId urlaubsantragId, List<BenutzerId> teamLeadIds) {
        final String businessKey = businessKey(urlaubsantragId);
        final Optional<ProzessinstanzId> bestehendeProzessinstanzId = findeBestehendeProzessinstanz(businessKey);
        if (bestehendeProzessinstanzId.isPresent()) {
            return bestehendeProzessinstanzId.get();
        }

        final var prozessinstanzInfo = startProcessApi.startProcess(new StartProcessByDefinitionCmd(
            VacationApprovalProcessApi.PROCESS_ID.getValue(),
            () -> Map.of(
                VacationApprovalProcessApi.Variables.AutomaticCheck.URLAUBSANTRAG_ID.getValue(), urlaubsantragId.value().toString(),
                CommonRestrictions.BUSINESS_KEY, businessKey,
                TEAM_LEAD_VARIABLE, mapTeamLeadIds(teamLeadIds)
            ),
            Map.of()
        ));

        try {
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
        } catch (InterruptedException | IllegalStateException | ExecutionException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            if (istDurchTimeoutVerursacht(exception)) {
                throw new ProzessEngineAuftragUnklarException(
                    "Genehmigungsprozess fuer Urlaubsantrag " + urlaubsantragId.value()
                        + " konnte nicht eindeutig gestartet werden",
                    exception
                );
            }
            throw new IllegalStateException(
                "Genehmigungsprozess fuer Urlaubsantrag " + urlaubsantragId.value() + " konnte nicht gestartet werden",
                exception
            );
        }
    }

    private Optional<ProzessinstanzId> findeBestehendeProzessinstanz(String businessKey) {
        return findeAktiveProzessinstanz(businessKey)
            .or(() -> findeHistorischeProzessinstanz(businessKey));
    }

    private Optional<ProzessinstanzId> findeAktiveProzessinstanz(String businessKey) {
        final ProcessInstance prozessinstanz = runtimeService.createProcessInstanceQuery()
            .processDefinitionKey(VacationApprovalProcessApi.PROCESS_ID.getValue())
            .processInstanceBusinessKey(businessKey)
            .active()
            .singleResult();

        return Optional.ofNullable(prozessinstanz)
            .map(ProcessInstance::getId)
            .map(ProzessinstanzId::of);
    }

    private Optional<ProzessinstanzId> findeHistorischeProzessinstanz(String businessKey) {
        return historyService.createHistoricProcessInstanceQuery()
            .processDefinitionKey(VacationApprovalProcessApi.PROCESS_ID.getValue())
            .processInstanceBusinessKey(businessKey)
            .orderByProcessInstanceStartTime()
            .desc()
            .listPage(0, 1)
            .stream()
            .findFirst()
            .map(HistoricProcessInstance::getId)
            .map(ProzessinstanzId::of);
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

    private boolean istDurchTimeoutVerursacht(Throwable exception) {
        Throwable currentException = exception;
        while (currentException != null) {
            if (currentException instanceof TimeoutException) {
                return true;
            }
            currentException = currentException.getCause();
        }
        return false;
    }
}
