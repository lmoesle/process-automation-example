package de.lmoesle.miravelo.processautomationexample.adapter.in.process;

import de.lmoesle.miravelo.processautomationexample.application.ports.in.UrlaubsantragAutomatischPruefenInPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.in.UrlaubsantragAutomatischPruefenInPort.UrlaubsantragAutomatischPruefenCommand;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragId;
import lombok.RequiredArgsConstructor;
import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.kie.kogito.internal.process.workitem.KogitoWorkItemHandler;
import org.kie.kogito.internal.process.workitem.KogitoWorkItemManager;
import org.kie.kogito.internal.process.workitem.WorkItemTransition;
import org.kie.kogito.process.workitems.impl.DefaultKogitoWorkItemHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AutomatischePruefungWorkItemHandler extends DefaultKogitoWorkItemHandler {

    static final String NAME = "AutomatischePruefung";
    private static final String URLAUBSANTRAG_ID_PARAMETER = "urlaubsantragId";
    private static final String GUELTIG_RESULT = "gueltig";

    private final UrlaubsantragAutomatischPruefenInPort pruefeUrlaubsantragAutomatischInPort;

    @Override
    public Optional<WorkItemTransition> activateWorkItemHandler(
        KogitoWorkItemManager manager,
        KogitoWorkItemHandler handler,
        KogitoWorkItem workItem,
        WorkItemTransition transition
    ) {
        final var urlaubsantragId = ladeUrlaubsantragId(workItem);
        final boolean gueltig = pruefeUrlaubsantragAutomatischInPort.pruefeUrlaubsantragAutomatisch(
            new UrlaubsantragAutomatischPruefenCommand(UrlaubsantragId.of(urlaubsantragId))
        );
        return Optional.of(handler.completeTransition(workItem.getPhaseStatus(), Map.of(GUELTIG_RESULT, gueltig)));
    }

    @Override
    public String getName() {
        return NAME;
    }

    private String ladeUrlaubsantragId(KogitoWorkItem workItem) {
        final Object rawValue = workItem.getParameter(URLAUBSANTRAG_ID_PARAMETER);
        Assert.isInstanceOf(String.class, rawValue, "urlaubsantragId muss ein String sein");
        final String urlaubsantragId = (String) rawValue;
        Assert.hasText(urlaubsantragId, "urlaubsantragId darf nicht leer sein");
        return urlaubsantragId;
    }
}
