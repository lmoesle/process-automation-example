package de.lmoesle.processautomationexample.application.ports.in;

import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;

import java.util.List;

public interface BenutzerSuchenInPort {

    List<Benutzer> sucheBenutzer(BenutzerSuchenCommand command);

    record BenutzerSuchenCommand(String suchbegriff) {
    }
}
