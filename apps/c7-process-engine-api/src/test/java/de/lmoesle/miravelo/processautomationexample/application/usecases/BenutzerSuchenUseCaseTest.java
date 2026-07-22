package de.lmoesle.miravelo.processautomationexample.application.usecases;

import de.lmoesle.miravelo.processautomationexample.application.ports.in.BenutzerSuchenInPort.BenutzerSuchenCommand;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.BenutzerRepositoryOutPort;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerTestdaten;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class BenutzerSuchenUseCaseTest {

    private BenutzerRepositoryOutPort benutzerRepositoryOutPort;
    private BenutzerSuchenUseCase useCase;

    @BeforeEach
    void setUp() {
        benutzerRepositoryOutPort = mock(BenutzerRepositoryOutPort.class);
        useCase = new BenutzerSuchenUseCase(benutzerRepositoryOutPort);
    }

    @Test
    void loadsSelectableUsersWhenSearchTermIsBlank() {
        when(benutzerRepositoryOutPort.findeAlleAuswaehlbaren())
            .thenReturn(List.of(BenutzerTestdaten.ada(), BenutzerTestdaten.carla()));

        final var benutzer = useCase.sucheBenutzer(new BenutzerSuchenCommand(" "));

        verify(benutzerRepositoryOutPort).findeAlleAuswaehlbaren();
        assertThat(benutzer).containsExactly(BenutzerTestdaten.ada(), BenutzerTestdaten.carla());
    }

    @Test
    void searchesSelectableUsersByNameOrEmail() {
        when(benutzerRepositoryOutPort.sucheAuswaehlbareNachNameOderEmail("gomez"))
            .thenReturn(List.of(BenutzerTestdaten.carla()));

        final var benutzer = useCase.sucheBenutzer(new BenutzerSuchenCommand("gomez"));

        verify(benutzerRepositoryOutPort).sucheAuswaehlbareNachNameOderEmail("gomez");
        assertThat(benutzer).containsExactly(BenutzerTestdaten.carla());
    }

    @Test
    void trimsSearchTerm() {
        when(benutzerRepositoryOutPort.sucheAuswaehlbareNachNameOderEmail("gomez"))
            .thenReturn(List.of(BenutzerTestdaten.carla()));

        final var benutzer = useCase.sucheBenutzer(new BenutzerSuchenCommand(" gomez "));

        verify(benutzerRepositoryOutPort).sucheAuswaehlbareNachNameOderEmail("gomez");
        assertThat(benutzer).containsExactly(BenutzerTestdaten.carla());
    }

    @Test
    void rejectsMissingCommand() {
        assertThatThrownBy(() -> useCase.sucheBenutzer(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("command darf nicht null sein");
    }
}
