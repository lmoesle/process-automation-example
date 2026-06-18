package de.lmoesle.processautomationexample.application.ports.in;

import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;

public interface AngemeldetenBenutzerLadenInPort {

    Benutzer ladeAngemeldetenBenutzer(AngemeldetenBenutzerLadenCommand command);

    record AngemeldetenBenutzerLadenCommand(String benutzername) {
    }
}
