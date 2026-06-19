package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.BenutzerSuchenInPort;
import de.lmoesle.processautomationexample.application.ports.out.BenutzerRepositoryOutPort;
import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BenutzerSuchenUseCase implements BenutzerSuchenInPort {

    private final BenutzerRepositoryOutPort benutzerRepositoryOutPort;

    @Override
    public List<Benutzer> sucheBenutzer(BenutzerSuchenCommand command) {
        Assert.notNull(command, "command darf nicht null sein");

        final String suchbegriff = command.suchbegriff() == null
            ? ""
            : command.suchbegriff().trim();

        if (!StringUtils.hasText(suchbegriff)) {
            return benutzerRepositoryOutPort.findeAlleAuswaehlbaren();
        }

        return benutzerRepositoryOutPort.sucheAuswaehlbareNachNameOderEmail(suchbegriff);
    }
}
