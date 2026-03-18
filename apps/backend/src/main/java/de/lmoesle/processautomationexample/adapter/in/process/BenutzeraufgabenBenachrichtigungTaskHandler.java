package de.lmoesle.processautomationexample.adapter.in.process;

import de.lmoesle.processautomationexample.application.ports.in.SendeBenutzeraufgabenBenachrichtigungInPort;
import de.lmoesle.processautomationexample.application.ports.in.SendeBenutzeraufgabenBenachrichtigungInPort.SendeBenutzeraufgabenBenachrichtigungCommand;
import de.lmoesle.processautomationexample.domain.tasklist.UserTaskId;
import dev.bpmcrafters.processengineapi.task.TaskHandler;
import dev.bpmcrafters.processengineapi.task.TaskInformation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class BenutzeraufgabenBenachrichtigungTaskHandler implements TaskHandler {

    private final ObjectProvider<SendeBenutzeraufgabenBenachrichtigungInPort> sendeBenutzeraufgabenBenachrichtigungInPortProvider;

    @Override
    public void accept(TaskInformation taskInformation, Map<String, ? extends Object> payload) {
        Assert.notNull(taskInformation, "taskInformation darf nicht null sein");
        Assert.hasText(taskInformation.getTaskId(), "taskInformation.taskId darf nicht leer sein");

        try {
            sendeBenutzeraufgabenBenachrichtigungInPortProvider.getObject().sendeBenutzeraufgabenBenachrichtigung(
                new SendeBenutzeraufgabenBenachrichtigungCommand(UserTaskId.of(taskInformation.getTaskId()))
            );
        } catch (RuntimeException exception) {
            log.error(
                "Benutzeraufgabenbenachrichtigung fuer taskId={} konnte nicht versendet werden",
                taskInformation.getTaskId(),
                exception
            );
        }
    }
}
