package de.lmoesle.miravelo.processautomationexample.adapter.in.process;

import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort.AktiveBenutzeraufgabeCommand;
import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort.EntfernteBenutzeraufgabeCommand;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@Slf4j
@RequiredArgsConstructor
public class BenutzeraufgabenBenachrichtigungTaskListener implements TaskListener {

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
            || TaskListener.EVENTNAME_ASSIGNMENT.equals(delegateTask.getEventName())) {
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
            new AktiveBenutzeraufgabeCommand(UserTaskId.of(delegateTask.getId()))
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
}
