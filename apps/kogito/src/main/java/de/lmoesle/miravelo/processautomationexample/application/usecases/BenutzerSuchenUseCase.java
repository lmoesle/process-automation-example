package de.lmoesle.miravelo.processautomationexample.application.usecases;

import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzerSuchenInPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.BenutzerRepositoryOutPort;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.Benutzer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BenutzerSuchenUseCase implements BenutzerSuchenInPort {

    private final BenutzerRepositoryOutPort benutzerRepositoryOutPort;

    @Override
    public List<Benutzer> sucheBenutzer(BenutzerSuchenCommand command) {
        final String suchbegriff = command.suchbegriff() == null
            ? ""
            : command.suchbegriff().trim();

        if (!StringUtils.hasText(suchbegriff)) {
            return benutzerRepositoryOutPort.findeAlleAuswaehlbaren();
        }

        return benutzerRepositoryOutPort.sucheAuswaehlbareNachNameOderEmail(suchbegriff);
    }
}
