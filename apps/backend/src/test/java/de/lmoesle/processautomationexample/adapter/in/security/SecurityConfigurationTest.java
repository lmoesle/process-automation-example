package de.lmoesle.processautomationexample.adapter.in.security;

import de.lmoesle.processautomationexample.adapter.in.rest.AktuellerBenutzerProvider;
import de.lmoesle.processautomationexample.adapter.in.rest.UrlaubsantraegeLadenController;
import de.lmoesle.processautomationexample.application.ports.in.AngemeldetenBenutzerLadenInPort;
import de.lmoesle.processautomationexample.application.ports.in.AngemeldetenBenutzerLadenInPort.AngemeldetenBenutzerLadenCommand;
import de.lmoesle.processautomationexample.application.ports.in.UrlaubsantraegeFuerBenutzerLadenInPort;
import de.lmoesle.processautomationexample.application.ports.in.UrlaubsantraegeFuerBenutzerLadenInPort.UrlaubsantraegeFuerBenutzerLadenCommand;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerTestdaten;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UrlaubsantraegeLadenController.class)
@Import({SecurityConfiguration.class, AktuellerBenutzerProvider.class})
class SecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private AngemeldetenBenutzerLadenInPort angemeldetenBenutzerLadenInPort;

    @MockitoBean
    private UrlaubsantraegeFuerBenutzerLadenInPort urlaubsantraegeFuerBenutzerLadenInPort;

    @BeforeEach
    void setUp() {
        when(userDetailsService.loadUserByUsername("john"))
            .thenReturn(User.withUsername("john").password("{noop}test").roles("BENUTZER").build());
        when(angemeldetenBenutzerLadenInPort.ladeAngemeldetenBenutzer(new AngemeldetenBenutzerLadenCommand("john")))
            .thenReturn(BenutzerTestdaten.ada());
        when(urlaubsantraegeFuerBenutzerLadenInPort.ladeUrlaubsantraegeFuerBenutzer(
            new UrlaubsantraegeFuerBenutzerLadenCommand(BenutzerTestdaten.adaId())
        )).thenReturn(List.of());
    }

    @Test
    void rejectsApiRequestsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/urlaubsantraege"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void acceptsApiRequestsWithValidBasicAuthentication() throws Exception {
        mockMvc.perform(get("/api/urlaubsantraege").with(httpBasic("john", "test")))
            .andExpect(status().isOk());
    }

    @Test
    void rejectsApiRequestsWithInvalidBasicAuthentication() throws Exception {
        mockMvc.perform(get("/api/urlaubsantraege").with(httpBasic("john", "falsch")))
            .andExpect(status().isUnauthorized());
    }
}
