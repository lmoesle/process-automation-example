package de.lmoesle.miravelo.processautomationexample.application.ports.in;

import de.lmoesle.miravelo.processautomationexample.domain.benutzer.Benutzer;

public interface AngemeldetenBenutzerLadenInPort {

    Benutzer ladeAngemeldetenBenutzer(AngemeldetenBenutzerLadenCommand command);

    record AngemeldetenBenutzerLadenCommand(String benutzername) {
    }
}
