package de.lmoesle.processautomationexample.adapter.in.rest;

import de.lmoesle.processautomationexample.application.ports.in.BenutzerSuchenInPort;
import de.lmoesle.processautomationexample.application.ports.in.BenutzerSuchenInPort.BenutzerSuchenCommand;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerTestdaten;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BenutzerSuchenController.class)
@AutoConfigureMockMvc(addFilters = false)
class BenutzerSuchenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BenutzerSuchenInPort benutzerSuchenInPort;

    @Test
    void loadsAvailableUsers() throws Exception {
        when(benutzerSuchenInPort.sucheBenutzer(new BenutzerSuchenCommand(null)))
            .thenReturn(List.of(BenutzerTestdaten.ada(), BenutzerTestdaten.carla()));

        mockMvc.perform(get("/api/benutzer"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(BenutzerTestdaten.ADA_UUID.toString()))
            .andExpect(jsonPath("$[0].name").value("Ada Lovelace"))
            .andExpect(jsonPath("$[0].email").value("ada.lovelace@example.com"))
            .andExpect(jsonPath("$[1].id").value(BenutzerTestdaten.CARLA_UUID.toString()))
            .andExpect(jsonPath("$[1].name").value("Carla Gomez"))
            .andExpect(jsonPath("$[1].email").value("carla.gomez@example.com"));
    }

    @Test
    void passesSearchTermToUseCase() throws Exception {
        when(benutzerSuchenInPort.sucheBenutzer(new BenutzerSuchenCommand("ada")))
            .thenReturn(List.of(BenutzerTestdaten.ada()));

        mockMvc.perform(get("/api/benutzer").param("suchbegriff", "ada"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(BenutzerTestdaten.ADA_UUID.toString()));

        verify(benutzerSuchenInPort).sucheBenutzer(new BenutzerSuchenCommand("ada"));
    }
}
