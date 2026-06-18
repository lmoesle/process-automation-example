package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.AngemeldetenBenutzerLadenInPort;
import de.lmoesle.processautomationexample.application.ports.out.BenutzerRepositoryOutPort;
import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@RequiredArgsConstructor
public class AngemeldetenBenutzerLadenUseCase implements AngemeldetenBenutzerLadenInPort {

    private final BenutzerRepositoryOutPort benutzerRepositoryOutPort;

    @Override
    public Benutzer ladeAngemeldetenBenutzer(AngemeldetenBenutzerLadenCommand command) {
        Assert.notNull(command, "command darf nicht null sein");
        Assert.hasText(command.benutzername(), "benutzername darf nicht leer sein");

        return benutzerRepositoryOutPort.findeNachBenutzername(command.benutzername())
            .orElseThrow(() -> new IllegalArgumentException("benutzername verweist auf keinen vorhandenen Benutzer"));
    }
}
