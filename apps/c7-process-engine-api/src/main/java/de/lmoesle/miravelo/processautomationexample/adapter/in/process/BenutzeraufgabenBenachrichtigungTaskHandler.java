package de.lmoesle.miravelo.processautomationexample.adapter.in.process;

import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort.AktiveBenutzeraufgabeCommand;
import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort.EntfernteBenutzeraufgabeCommand;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskId;
import dev.bpmcrafters.processengineapi.task.TaskHandler;
import dev.bpmcrafters.processengineapi.task.TaskInformation;
import dev.bpmcrafters.processengineapi.task.TaskTerminationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class BenutzeraufgabenBenachrichtigungTaskHandler implements TaskHandler, TaskTerminationHandler {

    private final ObjectProvider<BenutzeraufgabenLifecycleInPort> benutzeraufgabenLifecycleInPortProvider;

    @Override
    public void accept(TaskInformation taskInformation, Map<String, ? extends Object> payload) {
        validiere(taskInformation);

        try {
            verarbeiteTaskEvent(taskInformation);
        } catch (RuntimeException exception) {
            log.error(
                "Benutzeraufgaben-Lifecycle fuer taskId={} konnte nicht verarbeitet werden",
                taskInformation.getTaskId(),
                exception
            );
            throw exception;
        }
    }

    @Override
    public void accept(TaskInformation taskInformation) {
        validiere(taskInformation);

        try {
            verarbeiteEntfernteBenutzeraufgabe(taskInformation);
        } catch (RuntimeException exception) {
            log.error(
                "Entfernte Benutzeraufgabe fuer taskId={} konnte nicht verarbeitet werden",
                taskInformation.getTaskId(),
                exception
            );
            throw exception;
        }
    }

    private void verarbeiteTaskEvent(TaskInformation taskInformation) {
        final String reason = taskInformation.getMeta().get(TaskInformation.REASON);
        if (TaskInformation.COMPLETE.equals(reason) || TaskInformation.DELETE.equals(reason)) {
            verarbeiteEntfernteBenutzeraufgabe(taskInformation);
            return;
        }

        benutzeraufgabenLifecycleInPortProvider.getObject().verarbeiteAktiveBenutzeraufgabe(
            new AktiveBenutzeraufgabeCommand(UserTaskId.of(taskInformation.getTaskId()))
        );
    }

    private void verarbeiteEntfernteBenutzeraufgabe(TaskInformation taskInformation) {
        benutzeraufgabenLifecycleInPortProvider.getObject().verarbeiteEntfernteBenutzeraufgabe(
            new EntfernteBenutzeraufgabeCommand(UserTaskId.of(taskInformation.getTaskId()))
        );
    }

    private void validiere(TaskInformation taskInformation) {
        Assert.notNull(taskInformation, "taskInformation darf nicht null sein");
        Assert.hasText(taskInformation.getTaskId(), "taskInformation.taskId darf nicht leer sein");
    }
}
