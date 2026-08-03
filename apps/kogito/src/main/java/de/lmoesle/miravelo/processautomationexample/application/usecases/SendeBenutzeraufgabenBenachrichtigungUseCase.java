package de.lmoesle.miravelo.processautomationexample.application.usecases;

import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzeraufgabenLifecycleInPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.SendeBenutzeraufgabenBenachrichtigungOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.TasklistRepositoryOutPort;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.TaskNichtGefundenException;
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
@Transactional
public class SendeBenutzeraufgabenBenachrichtigungUseCase implements BenutzeraufgabenLifecycleInPort {

    private final TasklistRepositoryOutPort tasklistRepositoryOutPort;
    private final SendeBenutzeraufgabenBenachrichtigungOutPort sendeBenutzeraufgabenBenachrichtigungOutPort;

    @Override
    public void verarbeiteAktiveBenutzeraufgabe(AktiveBenutzeraufgabeCommand command) {
        Assert.notNull(command, "command darf nicht null sein");

        final var isNewUsertask = tasklistRepositoryOutPort.speichere(command.aufgabe());
        if (!isNewUsertask) {
            log.debug(
                "Benutzeraufgabe {} wurde aktualisiert, es wird keine weitere Benachrichtigung versendet",
                command.aufgabe().id().value()
            );
            return;
        }

        final var userTask = tasklistRepositoryOutPort.getTaskById(command.aufgabe().id())
            .orElseThrow(() -> new TaskNichtGefundenException(command.aufgabe().id()));

        final List<Benutzer> empfaenger = userTask.candidateUsers().stream()
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
            command.aufgabe().id().value(),
            empfaenger.size()
        );
    }

    @Override
    public void verarbeiteEntfernteBenutzeraufgabe(EntfernteBenutzeraufgabeCommand command) {
        Assert.notNull(command, "command darf nicht null sein");

        tasklistRepositoryOutPort.entferne(command.taskId());

        log.debug("Benutzeraufgabe entfernt: taskId={}", command.taskId().value());
    }
}
