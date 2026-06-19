package de.lmoesle.processautomationexample.adapter.in.rest;

import de.lmoesle.processautomationexample.application.ports.in.AngemeldetenBenutzerLadenInPort;
import de.lmoesle.processautomationexample.application.ports.in.AngemeldetenBenutzerLadenInPort.AngemeldetenBenutzerLadenCommand;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerTestdaten;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AktuellerBenutzerProviderTest {

    private AngemeldetenBenutzerLadenInPort angemeldetenBenutzerLadenInPort;
    private AktuellerBenutzerProvider aktuellerBenutzerProvider;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        angemeldetenBenutzerLadenInPort = mock(AngemeldetenBenutzerLadenInPort.class);
        aktuellerBenutzerProvider = new AktuellerBenutzerProvider(angemeldetenBenutzerLadenInPort);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void loadsCurrentUserIdFromSecurityContextAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("john", "test", List.of())
        );
        when(angemeldetenBenutzerLadenInPort.ladeAngemeldetenBenutzer(new AngemeldetenBenutzerLadenCommand("john")))
            .thenReturn(BenutzerTestdaten.ada());

        final var benutzerId = aktuellerBenutzerProvider.benutzerId();

        assertThat(benutzerId).isEqualTo(BenutzerTestdaten.adaId());
    }

    @Test
    void rejectsMissingAuthentication() {
        assertThatThrownBy(aktuellerBenutzerProvider::benutzerId)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Kein angemeldeter Benutzer im Security-Kontext vorhanden");
    }

    @Test
    void rejectsUnauthenticatedAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("john", "test"));

        assertThatThrownBy(aktuellerBenutzerProvider::benutzerId)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Kein angemeldeter Benutzer im Security-Kontext vorhanden");
    }

    @Test
    void rejectsAnonymousAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
            "key",
            "anonymousUser",
            List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        ));

        assertThatThrownBy(aktuellerBenutzerProvider::benutzerId)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Kein angemeldeter Benutzer im Security-Kontext vorhanden");
    }
}
