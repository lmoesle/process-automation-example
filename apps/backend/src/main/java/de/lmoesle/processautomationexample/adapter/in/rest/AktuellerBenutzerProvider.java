package de.lmoesle.processautomationexample.adapter.in.rest;

import de.lmoesle.processautomationexample.application.ports.in.AngemeldetenBenutzerLadenInPort;
import de.lmoesle.processautomationexample.application.ports.in.AngemeldetenBenutzerLadenInPort.AngemeldetenBenutzerLadenCommand;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class AktuellerBenutzerProvider {

    private final AngemeldetenBenutzerLadenInPort angemeldetenBenutzerLadenInPort;

    BenutzerId benutzerId(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("Kein angemeldeter Benutzer im Security-Kontext vorhanden");
        }

        return angemeldetenBenutzerLadenInPort.ladeAngemeldetenBenutzer(
            new AngemeldetenBenutzerLadenCommand(principal.getName())
        ).id();
    }
}
