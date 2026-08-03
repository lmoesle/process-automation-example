package de.lmoesle.miravelo.processautomationexample.adapter.in.process;

import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort.AktiveBenutzeraufgabeCommand;
import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort.EntfernteBenutzeraufgabeCommand;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.OffeneBenutzeraufgabe;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.ProzessinstanzId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.kogito.internal.process.event.DefaultKogitoProcessEventListener;
import org.kie.kogito.internal.process.event.ProcessWorkItemTransitionEvent;
import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class BenutzeraufgabenBenachrichtigungProcessEventListener extends DefaultKogitoProcessEventListener {

    private static final String TASK_NAME_PARAMETER = "TaskName";
    private static final String ACTOR_ID_PARAMETER = "ActorId";
    private static final String URLAUBSANTRAG_ID_VARIABLE = "urlaubsantragId";

    private final ObjectProvider<BenutzeraufgabenLifecycleInPort> benutzeraufgabenLifecycleInPortProvider;

    @Override
    public void afterWorkItemTransition(ProcessWorkItemTransitionEvent event) {
        Assert.notNull(event, "event darf nicht null sein");
        if (!event.isTransitioned() || !istBenutzeraufgabe(event.getWorkItem())) {
            return;
        }

        final var workItem = event.getWorkItem();
        try {
            if (event.getTransition().termination().isPresent()) {
                entferneBenutzeraufgabe(workItem);
            } else {
                verarbeiteAktiveBenutzeraufgabe(workItem);
            }
        } catch (RuntimeException exception) {
            log.error(
                "Benutzeraufgaben-Lifecycle fuer taskId={} konnte nicht verarbeitet werden",
                workItem.getStringId(),
                exception
            );
            throw exception;
        }
    }

    private boolean istBenutzeraufgabe(KogitoWorkItem workItem) {
        return workItem != null && workItem.getParameter(TASK_NAME_PARAMETER) instanceof String taskName
            && StringUtils.hasText(taskName);
    }

    private void verarbeiteAktiveBenutzeraufgabe(KogitoWorkItem workItem) {
        benutzeraufgabenLifecycleInPortProvider.getObject().verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(toDomain(workItem))
        );
    }

    private void entferneBenutzeraufgabe(KogitoWorkItem workItem) {
        benutzeraufgabenLifecycleInPortProvider.getObject().verarbeiteEntfernteBenutzeraufgabe(
            new EntfernteBenutzeraufgabeCommand(UserTaskId.of(workItem.getStringId()))
        );
    }

    private OffeneBenutzeraufgabe toDomain(KogitoWorkItem workItem) {
        return new OffeneBenutzeraufgabe(
            UserTaskId.of(workItem.getStringId()),
            (String) workItem.getParameter(TASK_NAME_PARAMETER),
            ProzessinstanzId.of(workItem.getProcessInstanceStringId()),
            ermittleBusinessKey(workItem),
            ermittleCandidateUserIds(workItem),
            parseBenutzerId(workItem.getActualOwner()).orElse(null)
        );
    }

    private String ermittleBusinessKey(KogitoWorkItem workItem) {
        final String businessKey = workItem.getProcessInstance().getBusinessKey();
        if (StringUtils.hasText(businessKey)) {
            return businessKey;
        }

        final Object rawValue = workItem.getProcessInstance().getVariables().get(URLAUBSANTRAG_ID_VARIABLE);
        Assert.isInstanceOf(String.class, rawValue, "urlaubsantragId muss ein String sein");
        final String urlaubsantragId = (String) rawValue;
        Assert.hasText(urlaubsantragId, "urlaubsantragId darf nicht leer sein");
        return urlaubsantragId;
    }

    private List<BenutzerId> ermittleCandidateUserIds(KogitoWorkItem workItem) {
        final Object rawValue = workItem.getParameter(ACTOR_ID_PARAMETER);
        if (!(rawValue instanceof String actorIds) || !StringUtils.hasText(actorIds)) {
            return List.of();
        }

        return Arrays.stream(actorIds.split(","))
            .map(this::parseBenutzerId)
            .flatMap(Optional::stream)
            .distinct()
            .toList();
    }

    private Optional<BenutzerId> parseBenutzerId(String rawValue) {
        if (!StringUtils.hasText(rawValue)) {
            return Optional.empty();
        }

        try {
            return Optional.of(BenutzerId.of(UUID.fromString(rawValue.trim())));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
