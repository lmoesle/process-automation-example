package de.lmoesle.processautomationexample.adapter.in.rest;

import de.lmoesle.processautomationexample.application.ports.in.CreateVacationRequestInPort;
import de.lmoesle.processautomationexample.application.ports.in.CreateVacationRequestInPort.CreateVacationRequestResult;
import de.lmoesle.processautomationexample.domain.user.UserTestData;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestId;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestTestData;
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

@WebMvcTest(CreateVacationRequestController.class)
@Import(RestExceptionHandler.class)
class CreateVacationRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateVacationRequestInPort createVacationRequestInPort;

    @Test
    void createsVacationRequestViaRestApi() throws Exception {
        UUID vacationRequestId = VacationRequestTestData.VACATION_REQUEST_UUID;
        UUID applicantUserId = VacationRequestTestData.APPLICANT_USER_UUID;
        UUID substituteUserId = VacationRequestTestData.SUBSTITUTE_USER_UUID;

        when(createVacationRequestInPort.createVacationRequest(any()))
            .thenReturn(new CreateVacationRequestResult(
                VacationRequestId.of(vacationRequestId),
                VacationRequestTestData.processInstanceId(),
                UserTestData.ada(),
                UserTestData.carla()
            ));

        mockMvc.perform(post("/api/vacation-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "from": "2026-07-01",
                      "to": "2026-07-10",
                      "applicantUserId": "%s",
                      "substituteUserId": "%s"
                    }
                    """.formatted(applicantUserId, substituteUserId)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/vacation-requests/" + vacationRequestId))
            .andExpect(jsonPath("$.id").value(vacationRequestId.toString()))
            .andExpect(jsonPath("$.from").value("2026-07-01"))
            .andExpect(jsonPath("$.to").value("2026-07-10"))
            .andExpect(jsonPath("$.applicantUser.id").doesNotExist())
            .andExpect(jsonPath("$.applicantUser.name").value(UserTestData.ada().name()))
            .andExpect(jsonPath("$.applicantUser.email").value(UserTestData.ada().email()))
            .andExpect(jsonPath("$.substituteUser.id").doesNotExist())
            .andExpect(jsonPath("$.substituteUser.name").value(UserTestData.carla().name()))
            .andExpect(jsonPath("$.substituteUser.email").value(UserTestData.carla().email()))
            .andExpect(jsonPath("$.processInstanceId").doesNotExist());
    }

    @Test
    void rejectsInvalidDateRange() throws Exception {
        mockMvc.perform(post("/api/vacation-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "from": "2026-07-10",
                      "to": "2026-07-01",
                      "applicantUserId": "%s"
                    }
                    """.formatted(VacationRequestTestData.APPLICANT_USER_UUID)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void returnsProblemDetailWhenUseCaseRejectsRequest() throws Exception {
        when(createVacationRequestInPort.createVacationRequest(any()))
            .thenThrow(new IllegalArgumentException("processInstanceId must not be null"));

        mockMvc.perform(post("/api/vacation-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "from": "2026-07-01",
                      "to": "2026-07-10",
                      "applicantUserId": "%s"
                    }
                    """.formatted(VacationRequestTestData.APPLICANT_USER_UUID)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Invalid vacation request"))
            .andExpect(jsonPath("$.detail").value("processInstanceId must not be null"));
    }
}
