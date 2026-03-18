package de.lmoesle.processautomationexample.adapter.in.rest;

import de.lmoesle.processautomationexample.application.ports.in.UrlaubsantragErstellenInPort;
import de.lmoesle.processautomationexample.application.ports.in.UrlaubsantragErstellenInPort.UrlaubsantragErstellenErgebnis;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragId;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragStatus;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UrlaubsantragErstellenController.class)
@Import(RestExceptionHandler.class)
class UrlaubsantragErstellenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlaubsantragErstellenInPort erstelleUrlaubsantragInPort;

    @Test
    void createsUrlaubsantragViaRestApi() throws Exception {
        UUID urlaubsantragId = UrlaubsantragTestData.VACATION_REQUEST_UUID;
        UUID antragstellerId = UrlaubsantragTestData.APPLICANT_USER_UUID;
        UUID vertretungId = UrlaubsantragTestData.SUBSTITUTE_USER_UUID;

        when(erstelleUrlaubsantragInPort.erstelleUrlaubsantrag(any()))
            .thenReturn(new UrlaubsantragErstellenErgebnis(
                UrlaubsantragId.of(urlaubsantragId),
                UrlaubsantragTestData.prozessinstanzId(),
                UrlaubsantragStatus.ANTRAG_GESTELLT,
                UrlaubsantragTestData.initialStatusHistory(),
                BenutzerTestdaten.ada(),
                BenutzerTestdaten.carla()
            ));

        mockMvc.perform(post("/api/urlaubsantraege")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "von": "2026-07-01",
                      "bis": "2026-07-10",
                      "antragstellerId": "%s",
                      "vertretungId": "%s"
                    }
                    """.formatted(antragstellerId, vertretungId)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/urlaubsantraege/" + urlaubsantragId))
            .andExpect(jsonPath("$.id").value(urlaubsantragId.toString()))
            .andExpect(jsonPath("$.von").value("2026-07-01"))
            .andExpect(jsonPath("$.bis").value("2026-07-10"))
            .andExpect(jsonPath("$.antragsteller.id").doesNotExist())
            .andExpect(jsonPath("$.antragsteller.name").value(BenutzerTestdaten.ada().name()))
            .andExpect(jsonPath("$.antragsteller.email").value(BenutzerTestdaten.ada().email()))
            .andExpect(jsonPath("$.vertretung.id").doesNotExist())
            .andExpect(jsonPath("$.vertretung.name").value(BenutzerTestdaten.carla().name()))
            .andExpect(jsonPath("$.vertretung.email").value(BenutzerTestdaten.carla().email()))
            .andExpect(jsonPath("$.vorgesetzter").value(org.hamcrest.Matchers.nullValue()))
            .andExpect(jsonPath("$.status").value("ANTRAG_GESTELLT"))
            .andExpect(jsonPath("$.statusHistorie[0].status").value("ANTRAG_GESTELLT"))
            .andExpect(jsonPath("$.prozessinstanzId").doesNotExist());
    }

    @Test
    void rejectsInvalidDateRange() throws Exception {
        mockMvc.perform(post("/api/urlaubsantraege")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "von": "2026-07-10",
                      "bis": "2026-07-01",
                      "antragstellerId": "%s"
                    }
                    """.formatted(UrlaubsantragTestData.APPLICANT_USER_UUID)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void returnsProblemDetailWhenUseCaseRejectsRequest() throws Exception {
        when(erstelleUrlaubsantragInPort.erstelleUrlaubsantrag(any()))
            .thenThrow(new IllegalArgumentException("prozessinstanzId darf nicht null sein"));

        mockMvc.perform(post("/api/urlaubsantraege")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "von": "2026-07-01",
                      "bis": "2026-07-10",
                      "antragstellerId": "%s"
                    }
                    """.formatted(UrlaubsantragTestData.APPLICANT_USER_UUID)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Ungueltige Anfrage"))
            .andExpect(jsonPath("$.detail").value("prozessinstanzId darf nicht null sein"));
    }
}
