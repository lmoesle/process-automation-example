package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.UrlaubsantraegeFuerBenutzerLadenInPort.UrlaubsantraegeFuerBenutzerLadenCommand;
import de.lmoesle.processautomationexample.application.ports.out.UrlaubsantraegeLadenOutPort;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UrlaubsantraegeFuerBenutzerLadenUseCaseTest {

    private UrlaubsantraegeLadenOutPort urlaubsantraegeLadenOutPort;
    private UrlaubsantraegeFuerBenutzerLadenUseCase urlaubsantraegeFuerBenutzerLadenUseCase;

    @BeforeEach
    void setUp() {
        urlaubsantraegeLadenOutPort = mock(UrlaubsantraegeLadenOutPort.class);
        urlaubsantraegeFuerBenutzerLadenUseCase = new UrlaubsantraegeFuerBenutzerLadenUseCase(urlaubsantraegeLadenOutPort);
    }

    @Test
    void loadsUrlaubsantraegeFuerBenutzer() {
        var expectedUrlaubsantrags = List.of(
            UrlaubsantragTestData.urlaubsantrag(
                UrlaubsantragTestData.urlaubsantragId(),
                UrlaubsantragTestData.vacationPeriod(),
                BenutzerTestdaten.ada(),
                BenutzerTestdaten.carla(),
                UrlaubsantragTestData.prozessinstanzId()
            )
        );
        when(urlaubsantraegeLadenOutPort.findeAlleNachAntragstellerId(BenutzerTestdaten.adaId()))
            .thenReturn(expectedUrlaubsantrags);

        var urlaubsantrags = urlaubsantraegeFuerBenutzerLadenUseCase.ladeUrlaubsantraegeFuerBenutzer(
            new UrlaubsantraegeFuerBenutzerLadenCommand(BenutzerTestdaten.adaId())
        );

        verify(urlaubsantraegeLadenOutPort).findeAlleNachAntragstellerId(BenutzerTestdaten.adaId());
        assertThat(urlaubsantrags).containsExactlyElementsOf(expectedUrlaubsantrags);
    }

    @Test
    void rejectsNullCommand() {
        assertThatThrownBy(() -> urlaubsantraegeFuerBenutzerLadenUseCase.ladeUrlaubsantraegeFuerBenutzer(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("command darf nicht null sein");
    }

    @Test
    void rejectsNullBenutzerId() {
        assertThatThrownBy(() -> urlaubsantraegeFuerBenutzerLadenUseCase.ladeUrlaubsantraegeFuerBenutzer(
            new UrlaubsantraegeFuerBenutzerLadenCommand(null)
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("benutzerId darf nicht null sein");
    }
}
