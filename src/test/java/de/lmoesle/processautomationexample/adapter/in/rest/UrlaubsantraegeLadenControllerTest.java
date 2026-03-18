package de.lmoesle.processautomationexample.adapter.in.rest;

import de.lmoesle.processautomationexample.application.ports.in.UrlaubsantraegeFuerBenutzerLadenInPort;
import de.lmoesle.processautomationexample.application.ports.in.UrlaubsantraegeFuerBenutzerLadenInPort.UrlaubsantraegeFuerBenutzerLadenCommand;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.Urlaubszeitraum;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UrlaubsantraegeLadenController.class)
class UrlaubsantraegeLadenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlaubsantraegeFuerBenutzerLadenInPort urlaubsantraegeFuerBenutzerLadenInPort;

    @Test
    void loadsUrlaubsantragsForHardcodedCurrentUser() throws Exception {
        var firstUrlaubsantrag = UrlaubsantragTestData.urlaubsantrag(
            UrlaubsantragTestData.urlaubsantragId(),
            UrlaubsantragTestData.vacationPeriod(),
            BenutzerTestdaten.ada(),
            BenutzerTestdaten.carla(),
            BenutzerTestdaten.carla(),
            UrlaubsantragTestData.prozessinstanzId()
        );
        var secondUrlaubsantrag = UrlaubsantragTestData.urlaubsantrag(
            UrlaubsantragTestData.secondUrlaubsantragId(),
            Urlaubszeitraum.of(UrlaubsantragTestData.SECOND_FROM, UrlaubsantragTestData.SECOND_TO),
            BenutzerTestdaten.ada(),
            null,
            null,
            null
        );
        when(urlaubsantraegeFuerBenutzerLadenInPort.ladeUrlaubsantraegeFuerBenutzer(
            new UrlaubsantraegeFuerBenutzerLadenCommand(BenutzerTestdaten.adaId())
        )).thenReturn(List.of(firstUrlaubsantrag, secondUrlaubsantrag));

        mockMvc.perform(get("/api/urlaubsantraege"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(UrlaubsantragTestData.VACATION_REQUEST_UUID.toString()))
            .andExpect(jsonPath("$[0].von").value(UrlaubsantragTestData.FROM.toString()))
            .andExpect(jsonPath("$[0].bis").value(UrlaubsantragTestData.TO.toString()))
            .andExpect(jsonPath("$[0].antragsteller.id").doesNotExist())
            .andExpect(jsonPath("$[0].antragsteller.name").value(BenutzerTestdaten.ada().name()))
            .andExpect(jsonPath("$[0].antragsteller.email").value(BenutzerTestdaten.ada().email()))
            .andExpect(jsonPath("$[0].vertretung.id").doesNotExist())
            .andExpect(jsonPath("$[0].vertretung.name").value(BenutzerTestdaten.carla().name()))
            .andExpect(jsonPath("$[0].vertretung.email").value(BenutzerTestdaten.carla().email()))
            .andExpect(jsonPath("$[0].vorgesetzter.id").doesNotExist())
            .andExpect(jsonPath("$[0].vorgesetzter.name").value(BenutzerTestdaten.carla().name()))
            .andExpect(jsonPath("$[0].vorgesetzter.email").value(BenutzerTestdaten.carla().email()))
            .andExpect(jsonPath("$[0].status").value("ANTRAG_GESTELLT"))
            .andExpect(jsonPath("$[0].statusHistorie[0].status").value("ANTRAG_GESTELLT"))
            .andExpect(jsonPath("$[0].prozessinstanzId").doesNotExist())
            .andExpect(jsonPath("$[1].id").value(UrlaubsantragTestData.SECOND_VACATION_REQUEST_UUID.toString()))
            .andExpect(jsonPath("$[1].von").value(UrlaubsantragTestData.SECOND_FROM.toString()))
            .andExpect(jsonPath("$[1].bis").value(UrlaubsantragTestData.SECOND_TO.toString()))
            .andExpect(jsonPath("$[1].antragsteller.id").doesNotExist())
            .andExpect(jsonPath("$[1].antragsteller.name").value(BenutzerTestdaten.ada().name()))
            .andExpect(jsonPath("$[1].antragsteller.email").value(BenutzerTestdaten.ada().email()))
            .andExpect(jsonPath("$[1].vertretung").value(nullValue()))
            .andExpect(jsonPath("$[1].vorgesetzter").value(nullValue()))
            .andExpect(jsonPath("$[1].status").value("ANTRAG_GESTELLT"))
            .andExpect(jsonPath("$[1].statusHistorie[0].status").value("ANTRAG_GESTELLT"))
            .andExpect(jsonPath("$[1].prozessinstanzId").doesNotExist());

        verify(urlaubsantraegeFuerBenutzerLadenInPort).ladeUrlaubsantraegeFuerBenutzer(
            new UrlaubsantraegeFuerBenutzerLadenCommand(BenutzerTestdaten.adaId())
        );
    }

    @Test
    void returnsEmptyListWhenNoUrlaubsantragsExist() throws Exception {
        when(urlaubsantraegeFuerBenutzerLadenInPort.ladeUrlaubsantraegeFuerBenutzer(
            new UrlaubsantraegeFuerBenutzerLadenCommand(BenutzerTestdaten.adaId())
        )).thenReturn(List.of());

        mockMvc.perform(get("/api/urlaubsantraege"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }
}
