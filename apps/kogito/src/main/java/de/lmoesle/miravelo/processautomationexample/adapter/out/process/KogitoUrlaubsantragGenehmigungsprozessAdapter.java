package de.lmoesle.miravelo.processautomationexample.adapter.out.process;

import de.lmoesle.miravelo.processautomationexample.application.ports.out.UrlaubsantragGenehmigungsprozessStartenOutPort;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.ProzessinstanzId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import lombok.RequiredArgsConstructor;
import org.kie.kogito.Model;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessService;
import org.kie.kogito.process.Processes;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KogitoUrlaubsantragGenehmigungsprozessAdapter implements UrlaubsantragGenehmigungsprozessStartenOutPort {

    static final String PROCESS_ID = "vacation_approval";

    private final Processes processes;
    private final ProcessService processService;

    @Override
    public ProzessinstanzId starteGenehmigungsprozessFuer(Urlaubsantrag urlaubsantrag, List<BenutzerId> teamLeadIds) {
        Assert.notNull(urlaubsantrag, "urlaubsantrag darf nicht null sein");
        Assert.notNull(teamLeadIds, "teamLeadIds duerfen nicht null sein");

        try {
            final Process<Model> process = ladeProzess();
            final Model model = process.createModel();
            model.fromMap(Map.of(
                "urlaubsantragId", urlaubsantrag.id().value().toString(),
                "teamLeadIds", mapTeamLeadIds(teamLeadIds)
            ));
            final var prozessinstanz = processService.createProcessInstance(
                process,
                urlaubsantrag.id().value().toString(),
                model,
                null
            );
            return ProzessinstanzId.of(prozessinstanz.id());
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                "Genehmigungsprozess fuer Urlaubsantrag " + urlaubsantrag.id().value() + " konnte nicht gestartet werden",
                exception
            );
        }
    }

    @SuppressWarnings("unchecked")
    private Process<Model> ladeProzess() {
        final var process = processes.processById(PROCESS_ID);
        Assert.state(process != null, "Kogito-Prozess " + PROCESS_ID + " wurde nicht gefunden");
        return (Process<Model>) process;
    }

    private String mapTeamLeadIds(List<BenutzerId> teamLeadIds) {
        return teamLeadIds.stream()
            .map(benutzerId -> benutzerId.value().toString())
            .distinct()
            .collect(Collectors.joining(","));
    }
}
