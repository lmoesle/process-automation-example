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
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class BenutzeraufgabenBenachrichtigungTaskListener implements TaskListener {

    private static final String URLAUBSANTRAG_ID_VARIABLE = "urlaubsantragId";

    private final ObjectProvider<BenutzeraufgabenLifecycleInPort> benutzeraufgabenLifecycleInPortProvider;

    @Override
    public void notify(DelegateTask delegateTask) {
        validiere(delegateTask);

        try {
            verarbeiteTaskEvent(delegateTask);
        } catch (RuntimeException exception) {
            log.error(
                "Benutzeraufgaben-Lifecycle fuer taskId={} konnte nicht verarbeitet werden",
                delegateTask.getId(),
                exception
            );
            throw exception;
        }
    }

    private void verarbeiteTaskEvent(DelegateTask delegateTask) {
        if (TaskListener.EVENTNAME_CREATE.equals(delegateTask.getEventName())
            || TaskListener.EVENTNAME_ASSIGNMENT.equals(delegateTask.getEventName())
            || TaskListener.EVENTNAME_UPDATE.equals(delegateTask.getEventName())) {
            verarbeiteAktiveBenutzeraufgabe(delegateTask);
            return;
        }

        if (TaskListener.EVENTNAME_COMPLETE.equals(delegateTask.getEventName())
            || TaskListener.EVENTNAME_DELETE.equals(delegateTask.getEventName())) {
            verarbeiteEntfernteBenutzeraufgabe(delegateTask);
            return;
        }

        throw new IllegalArgumentException("Unbekanntes Benutzeraufgaben-Event: " + delegateTask.getEventName());
    }

    private void verarbeiteAktiveBenutzeraufgabe(DelegateTask delegateTask) {
        benutzeraufgabenLifecycleInPortProvider.getObject().verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(toDomain(delegateTask))
        );
    }

    private void verarbeiteEntfernteBenutzeraufgabe(DelegateTask delegateTask) {
        benutzeraufgabenLifecycleInPortProvider.getObject().verarbeiteEntfernteBenutzeraufgabe(
            new EntfernteBenutzeraufgabeCommand(UserTaskId.of(delegateTask.getId()))
        );
    }

    private void validiere(DelegateTask delegateTask) {
        Assert.notNull(delegateTask, "delegateTask darf nicht null sein");
        Assert.hasText(delegateTask.getId(), "delegateTask.id darf nicht leer sein");
        Assert.hasText(delegateTask.getEventName(), "delegateTask.eventName darf nicht leer sein");
    }

    private OffeneBenutzeraufgabe toDomain(DelegateTask delegateTask) {
        Assert.notNull(delegateTask.getExecution(), "delegateTask.execution darf nicht null sein");
        return new OffeneBenutzeraufgabe(
            UserTaskId.of(delegateTask.getId()),
            delegateTask.getName(),
            ProzessinstanzId.of(delegateTask.getProcessInstanceId()),
            ermittleBusinessKey(delegateTask),
            ermittleCandidateUserIds(delegateTask),
            parseBenutzerId(delegateTask.getAssignee()).orElse(null)
        );
    }

    private String ermittleBusinessKey(DelegateTask delegateTask) {
        final String businessKey = delegateTask.getExecution().getProcessBusinessKey();
        if (StringUtils.hasText(businessKey)) {
            return businessKey;
        }

        final Object rawValue = delegateTask.getVariable(URLAUBSANTRAG_ID_VARIABLE);
        Assert.isInstanceOf(String.class, rawValue, "urlaubsantragId muss ein String sein");
        final String urlaubsantragId = (String) rawValue;
        Assert.hasText(urlaubsantragId, "urlaubsantragId darf nicht leer sein");
        return urlaubsantragId;
    }

    private List<BenutzerId> ermittleCandidateUserIds(DelegateTask delegateTask) {
        return Optional.ofNullable(delegateTask.getCandidates()).orElse(Set.of()).stream()
            .filter(identityLink -> IdentityLinkType.CANDIDATE.equals(identityLink.getType()))
            .map(IdentityLink::getUserId)
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
