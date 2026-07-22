package de.lmoesle.miravelo.processautomationexample.application.usecases;

import de.lmoesle.miravelo.processautomationexample.application.ports.in.UrlaubsantraegeFuerBenutzerLadenInPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.UrlaubsantraegeLadenOutPort;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UrlaubsantraegeFuerBenutzerLadenUseCase implements UrlaubsantraegeFuerBenutzerLadenInPort {

    private final UrlaubsantraegeLadenOutPort urlaubsantraegeLadenOutPort;

    @Override
    public List<Urlaubsantrag> ladeUrlaubsantraegeFuerBenutzer(UrlaubsantraegeFuerBenutzerLadenCommand command) {
        Assert.notNull(command, "command darf nicht null sein");
        return urlaubsantraegeLadenOutPort.findeAlleNachAntragstellerId(command.benutzerId());
    }
}
