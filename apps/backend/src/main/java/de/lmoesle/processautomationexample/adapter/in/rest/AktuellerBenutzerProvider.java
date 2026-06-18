package de.lmoesle.processautomationexample.adapter.in.rest;

import de.lmoesle.processautomationexample.application.ports.in.AngemeldetenBenutzerLadenInPort;
import de.lmoesle.processautomationexample.application.ports.in.AngemeldetenBenutzerLadenInPort.AngemeldetenBenutzerLadenCommand;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AktuellerBenutzerProvider {

    private final AngemeldetenBenutzerLadenInPort angemeldetenBenutzerLadenInPort;

    BenutzerId benutzerId() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("Kein angemeldeter Benutzer im Security-Kontext vorhanden");
        }

        return angemeldetenBenutzerLadenInPort.ladeAngemeldetenBenutzer(
            new AngemeldetenBenutzerLadenCommand(authentication.getName())
        ).id();
    }
}
