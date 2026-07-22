package de.lmoesle.miravelo.processautomationexample.adapter.out.process;

import de.lmoesle.miravelo.processautomationexample.application.ports.out.UrlaubsantragGenehmigungsprozessStartenOutPort;
import de.lmoesle.miravelo.processautomationexample.bpmn.VacationApprovalProcessApi;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.ProzessinstanzId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CamundaUrlaubsantragGenehmigungsprozessAdapter implements UrlaubsantragGenehmigungsprozessStartenOutPort {

    private static final String TEAM_LEAD_VARIABLE = "teamLeadIds";

    private final RuntimeService runtimeService;

    @Override
    public ProzessinstanzId starteGenehmigungsprozessFuer(Urlaubsantrag urlaubsantrag, List<BenutzerId> teamLeadIds) {
        try {
            final var prozessinstanz = runtimeService.startProcessInstanceByKey(
                VacationApprovalProcessApi.PROCESS_ID.getValue(),
                urlaubsantrag.id().value().toString(),
                Map.of(
                    VacationApprovalProcessApi.Variables.AutomaticCheck.URLAUBSANTRAG_ID.getValue(),
                    urlaubsantrag.id().value().toString(),
                    TEAM_LEAD_VARIABLE,
                    mapTeamLeadIds(teamLeadIds)
                )
            );
            return ProzessinstanzId.of(prozessinstanz.getProcessInstanceId());
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                "Genehmigungsprozess fuer Urlaubsantrag " + urlaubsantrag.id().value() + " konnte nicht gestartet werden",
                exception
            );
        }
    }

    private List<String> mapTeamLeadIds(List<BenutzerId> teamLeadIds) {
        return teamLeadIds.stream()
            .map(benutzerId -> benutzerId.value().toString())
            .distinct()
            .toList();
    }
}
