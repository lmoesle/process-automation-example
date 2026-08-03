package de.lmoesle.miravelo.processautomationexample.application.usecases;

import de.lmoesle.miravelo.processautomationexample.application.ports.in.AngemeldetenBenutzerLadenInPort.AngemeldetenBenutzerLadenCommand;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.BenutzerRepositoryOutPort;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerTestdaten;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AngemeldetenBenutzerLadenUseCaseTest {

    private BenutzerRepositoryOutPort benutzerRepositoryOutPort;
    private AngemeldetenBenutzerLadenUseCase useCase;

    @BeforeEach
    void setUp() {
        benutzerRepositoryOutPort = mock(BenutzerRepositoryOutPort.class);
        useCase = new AngemeldetenBenutzerLadenUseCase(benutzerRepositoryOutPort);
    }

    @Test
    void loadsAuthenticatedUserByUsername() {
        when(benutzerRepositoryOutPort.findeNachBenutzername("john")).thenReturn(Optional.of(BenutzerTestdaten.ada()));

        final var benutzer = useCase.ladeAngemeldetenBenutzer(new AngemeldetenBenutzerLadenCommand("john"));

        assertThat(benutzer).isEqualTo(BenutzerTestdaten.ada());
    }

    @Test
    void rejectsUnknownUsername() {
        when(benutzerRepositoryOutPort.findeNachBenutzername("unbekannt")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.ladeAngemeldetenBenutzer(new AngemeldetenBenutzerLadenCommand("unbekannt")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("benutzername verweist auf keinen vorhandenen Benutzer");
    }
}
