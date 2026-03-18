package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.SendeBenutzeraufgabenBenachrichtigungInPort;
import de.lmoesle.processautomationexample.application.ports.out.SendeBenutzeraufgabenBenachrichtigungOutPort;
import de.lmoesle.processautomationexample.application.ports.out.TasklistRepositoryOutPort;
import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.processautomationexample.domain.tasklist.TaskNichtGefundenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.LinkedHashMap;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SendeBenutzeraufgabenBenachrichtigungUseCase implements SendeBenutzeraufgabenBenachrichtigungInPort {

    private final TasklistRepositoryOutPort tasklistRepositoryOutPort;
    private final SendeBenutzeraufgabenBenachrichtigungOutPort sendeBenutzeraufgabenBenachrichtigungOutPort;

    @Override
    public void sendeBenutzeraufgabenBenachrichtigung(SendeBenutzeraufgabenBenachrichtigungCommand command) {
        Assert.notNull(command, "command darf nicht null sein");
        Assert.notNull(command.taskId(), "taskId darf nicht null sein");

        var userTask = tasklistRepositoryOutPort.getTaskById(command.taskId())
            .orElseThrow(() -> new TaskNichtGefundenException(command.taskId()));

        List<Benutzer> empfaenger = userTask.candidateUsers().stream()
            .collect(
                LinkedHashMap<BenutzerId, Benutzer>::new,
                (map, benutzer) -> map.putIfAbsent(benutzer.id(), benutzer),
                LinkedHashMap::putAll
            )
            .values()
            .stream()
            .toList();

        sendeBenutzeraufgabenBenachrichtigungOutPort.sendeBenutzeraufgabenBenachrichtigung(userTask, empfaenger);

        log.info(
            "Benutzeraufgabenbenachrichtigung versendet: taskId={}, empfaengerAnzahl={}",
            command.taskId().value(),
            empfaenger.size()
        );
    }
}
