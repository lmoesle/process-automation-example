package de.lmoesle.processautomationexample.adapter.in.rest;

import de.lmoesle.processautomationexample.application.ports.in.LoadVacationRequestsForUserInPort;
import de.lmoesle.processautomationexample.application.ports.in.LoadVacationRequestsForUserInPort.LoadVacationRequestsForUserCommand;
import de.lmoesle.processautomationexample.domain.user.UserTestData;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationPeriod;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestTestData;
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

@WebMvcTest(LoadVacationRequestsController.class)
class LoadVacationRequestsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoadVacationRequestsForUserInPort loadVacationRequestsForUserInPort;

    @Test
    void loadsVacationRequestsForHardcodedCurrentUser() throws Exception {
        var firstVacationRequest = VacationRequestTestData.vacationRequest(
            VacationRequestTestData.vacationRequestId(),
            VacationRequestTestData.vacationPeriod(),
            UserTestData.ada(),
            UserTestData.carla(),
            VacationRequestTestData.processInstanceId()
        );
        var secondVacationRequest = VacationRequestTestData.vacationRequest(
            VacationRequestTestData.secondVacationRequestId(),
            VacationPeriod.of(VacationRequestTestData.SECOND_FROM, VacationRequestTestData.SECOND_TO),
            UserTestData.ada(),
            null,
            null
        );
        when(loadVacationRequestsForUserInPort.loadVacationRequestsForUser(
            new LoadVacationRequestsForUserCommand(UserTestData.adaId())
        )).thenReturn(List.of(firstVacationRequest, secondVacationRequest));

        mockMvc.perform(get("/api/vacation-requests"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(VacationRequestTestData.VACATION_REQUEST_UUID.toString()))
            .andExpect(jsonPath("$[0].from").value(VacationRequestTestData.FROM.toString()))
            .andExpect(jsonPath("$[0].to").value(VacationRequestTestData.TO.toString()))
            .andExpect(jsonPath("$[0].applicantUser.id").doesNotExist())
            .andExpect(jsonPath("$[0].applicantUser.name").value(UserTestData.ada().name()))
            .andExpect(jsonPath("$[0].applicantUser.email").value(UserTestData.ada().email()))
            .andExpect(jsonPath("$[0].substituteUser.id").doesNotExist())
            .andExpect(jsonPath("$[0].substituteUser.name").value(UserTestData.carla().name()))
            .andExpect(jsonPath("$[0].substituteUser.email").value(UserTestData.carla().email()))
            .andExpect(jsonPath("$[0].status").value("ANTRAG_GESTELLT"))
            .andExpect(jsonPath("$[0].statusHistory[0].status").value("ANTRAG_GESTELLT"))
            .andExpect(jsonPath("$[0].processInstanceId").doesNotExist())
            .andExpect(jsonPath("$[1].id").value(VacationRequestTestData.SECOND_VACATION_REQUEST_UUID.toString()))
            .andExpect(jsonPath("$[1].from").value(VacationRequestTestData.SECOND_FROM.toString()))
            .andExpect(jsonPath("$[1].to").value(VacationRequestTestData.SECOND_TO.toString()))
            .andExpect(jsonPath("$[1].applicantUser.id").doesNotExist())
            .andExpect(jsonPath("$[1].applicantUser.name").value(UserTestData.ada().name()))
            .andExpect(jsonPath("$[1].applicantUser.email").value(UserTestData.ada().email()))
            .andExpect(jsonPath("$[1].substituteUser").value(nullValue()))
            .andExpect(jsonPath("$[1].status").value("ANTRAG_GESTELLT"))
            .andExpect(jsonPath("$[1].statusHistory[0].status").value("ANTRAG_GESTELLT"))
            .andExpect(jsonPath("$[1].processInstanceId").doesNotExist());

        verify(loadVacationRequestsForUserInPort).loadVacationRequestsForUser(
            new LoadVacationRequestsForUserCommand(UserTestData.adaId())
        );
    }

    @Test
    void returnsEmptyListWhenNoVacationRequestsExist() throws Exception {
        when(loadVacationRequestsForUserInPort.loadVacationRequestsForUser(
            new LoadVacationRequestsForUserCommand(UserTestData.adaId())
        )).thenReturn(List.of());

        mockMvc.perform(get("/api/vacation-requests"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }
}
