package de.lmoesle.miravelo.processautomationexample.adapter.out.persistence;

import de.lmoesle.miravelo.processautomationexample.adapter.out.persistence.entities.BenutzerEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BenutzerUserDetailsServiceTest {

    private BenutzerJpaRepository benutzerJpaRepository;
    private BenutzerUserDetailsService benutzerUserDetailsService;

    @BeforeEach
    void setUp() {
        benutzerJpaRepository = mock(BenutzerJpaRepository.class);
        benutzerUserDetailsService = new BenutzerUserDetailsService(benutzerJpaRepository);
    }

    @Test
    void loadsUserDetailsFromDatabaseUser() {
        final var benutzer = new BenutzerEntity(
            UUID.fromString("41f60f4f-1bbb-4469-871f-bf102c46d001"),
            "John",
            "john@example.com",
            new LinkedHashSet<>()
        );
        benutzer.setBenutzername("john");
        benutzer.setPasswortHash("{noop}test");
        when(benutzerJpaRepository.findByBenutzername("john")).thenReturn(Optional.of(benutzer));

        final var userDetails = benutzerUserDetailsService.loadUserByUsername("john");

        assertThat(userDetails.getUsername()).isEqualTo("john");
        assertThat(userDetails.getPassword()).isEqualTo("{noop}test");
        assertThat(userDetails.getAuthorities())
            .extracting("authority")
            .containsExactly("ROLE_BENUTZER");
    }

    @Test
    void rejectsUnknownUser() {
        when(benutzerJpaRepository.findByBenutzername("unbekannt")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> benutzerUserDetailsService.loadUserByUsername("unbekannt"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessage("Benutzer nicht gefunden: unbekannt");
    }
}
