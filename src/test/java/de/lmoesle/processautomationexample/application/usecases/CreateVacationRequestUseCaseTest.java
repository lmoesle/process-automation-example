package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.CreateVacationRequestInPort.CreateVacationRequestCommand;
import de.lmoesle.processautomationexample.application.ports.out.SaveVacationRequestOutPort;
import de.lmoesle.processautomationexample.application.ports.out.StartVacationApprovalProcessOutPort;
import de.lmoesle.processautomationexample.application.ports.out.UserRepositoryOutPort;
import de.lmoesle.processautomationexample.domain.vacationrequest.ProcessInstanceId;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequest;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class CreateVacationRequestUseCaseTest {

    private UserRepositoryOutPort userRepositoryOutPort;
    private SaveVacationRequestOutPort saveVacationRequestOutPort;
    private StartVacationApprovalProcessOutPort startVacationApprovalProcessOutPort;
    private CreateVacationRequestUseCase createVacationRequestUseCase;

    @BeforeEach
    void setUp() {
        userRepositoryOutPort = mock(UserRepositoryOutPort.class);
        saveVacationRequestOutPort = mock(SaveVacationRequestOutPort.class);
        startVacationApprovalProcessOutPort = mock(StartVacationApprovalProcessOutPort.class);
        createVacationRequestUseCase = new CreateVacationRequestUseCase(
            userRepositoryOutPort,
            saveVacationRequestOutPort,
            startVacationApprovalProcessOutPort
        );
    }

    @Test
    void savesRequestStartsProcessAndPersistsProcessInstanceId() {
        AtomicInteger saveInvocationCounter = new AtomicInteger();
        doAnswer(invocation -> {
            VacationRequest vacationRequest = invocation.getArgument(0);
            int currentInvocation = saveInvocationCounter.incrementAndGet();

            if (currentInvocation == 1) {
                assertThat(vacationRequest.processInstanceId()).isNull();
            }

            assertThat(vacationRequest.applicantUser()).isEqualTo(VacationRequestTestData.applicantUser());
            assertThat(vacationRequest.substituteUser()).isEqualTo(VacationRequestTestData.substituteUser());

            if (currentInvocation == 2) {
                assertThat(vacationRequest.processInstanceId()).isEqualTo(VacationRequestTestData.processInstanceId());
            }

            return vacationRequest;
        }).when(saveVacationRequestOutPort).save(any(VacationRequest.class));
        when(userRepositoryOutPort.findById(VacationRequestTestData.applicantUserId()))
            .thenReturn(Optional.of(VacationRequestTestData.applicantUser()));
        when(userRepositoryOutPort.findById(VacationRequestTestData.substituteUserId()))
            .thenReturn(Optional.of(VacationRequestTestData.substituteUser()));
        when(startVacationApprovalProcessOutPort.startApprovalProcessFor(any(VacationRequest.class)))
            .thenAnswer(invocation -> {
                VacationRequest vacationRequest = invocation.getArgument(0);
                assertThat(vacationRequest.processInstanceId()).isNull();
                return VacationRequestTestData.processInstanceId();
            });

        var result = createVacationRequestUseCase.createVacationRequest(
            new CreateVacationRequestCommand(
                VacationRequestTestData.FROM,
                VacationRequestTestData.TO,
                VacationRequestTestData.applicantUserId(),
                VacationRequestTestData.substituteUserId()
            )
        );

        InOrder inOrder = inOrder(userRepositoryOutPort, saveVacationRequestOutPort, startVacationApprovalProcessOutPort);
        inOrder.verify(userRepositoryOutPort).findById(VacationRequestTestData.applicantUserId());
        inOrder.verify(userRepositoryOutPort).findById(VacationRequestTestData.substituteUserId());
        inOrder.verify(saveVacationRequestOutPort).save(any(VacationRequest.class));
        inOrder.verify(startVacationApprovalProcessOutPort).startApprovalProcessFor(any(VacationRequest.class));
        inOrder.verify(saveVacationRequestOutPort).save(any(VacationRequest.class));
        verifyNoMoreInteractions(userRepositoryOutPort, saveVacationRequestOutPort, startVacationApprovalProcessOutPort);

        assertThat(result.vacationRequestId()).isNotNull();
        assertThat(result.processInstanceId()).isEqualTo(VacationRequestTestData.processInstanceId());
        assertThat(result.applicantUser()).isEqualTo(VacationRequestTestData.applicantUser());
        assertThat(result.substituteUser()).isEqualTo(VacationRequestTestData.substituteUser());
        assertThat(saveInvocationCounter.get()).isEqualTo(2);
    }

    @Test
    void propagatesIllegalArgumentExceptionFromDomainValidation() {
        when(userRepositoryOutPort.findById(VacationRequestTestData.applicantUserId()))
            .thenReturn(Optional.of(VacationRequestTestData.applicantUser()));
        when(userRepositoryOutPort.findById(VacationRequestTestData.substituteUserId()))
            .thenReturn(Optional.of(VacationRequestTestData.substituteUser()));

        assertThatThrownBy(() -> createVacationRequestUseCase.createVacationRequest(
            new CreateVacationRequestCommand(
                VacationRequestTestData.TO,
                VacationRequestTestData.FROM,
                VacationRequestTestData.applicantUserId(),
                VacationRequestTestData.substituteUserId()
            )
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("'from' must be on or before 'to'.");
    }

    @Test
    void propagatesStartedProcessInstanceId() {
        when(userRepositoryOutPort.findById(VacationRequestTestData.applicantUserId()))
            .thenReturn(Optional.of(VacationRequestTestData.applicantUser()));
        when(saveVacationRequestOutPort.save(any(VacationRequest.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(startVacationApprovalProcessOutPort.startApprovalProcessFor(any(VacationRequest.class)))
            .thenReturn(ProcessInstanceId.of("process-instance-9000"));

        var result = createVacationRequestUseCase.createVacationRequest(
            new CreateVacationRequestCommand(
                VacationRequestTestData.FROM,
                VacationRequestTestData.TO,
                VacationRequestTestData.applicantUserId(),
                null
            )
        );

        assertThat(result.processInstanceId()).isEqualTo(ProcessInstanceId.of("process-instance-9000"));
        assertThat(result.applicantUser()).isEqualTo(VacationRequestTestData.applicantUser());
        assertThat(result.substituteUser()).isNull();
    }

    @Test
    void rejectsMissingApplicantUser() {
        when(userRepositoryOutPort.findById(VacationRequestTestData.applicantUserId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createVacationRequestUseCase.createVacationRequest(
            new CreateVacationRequestCommand(
                VacationRequestTestData.FROM,
                VacationRequestTestData.TO,
                VacationRequestTestData.applicantUserId(),
                null
            )
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("applicantUserId does not reference an existing user");

        inOrder(userRepositoryOutPort)
            .verify(userRepositoryOutPort)
            .findById(VacationRequestTestData.applicantUserId());
        verifyNoInteractions(saveVacationRequestOutPort, startVacationApprovalProcessOutPort);
        verifyNoMoreInteractions(userRepositoryOutPort);
    }

    @Test
    void rejectsMissingSubstituteUser() {
        when(userRepositoryOutPort.findById(VacationRequestTestData.applicantUserId()))
            .thenReturn(Optional.of(VacationRequestTestData.applicantUser()));
        when(userRepositoryOutPort.findById(VacationRequestTestData.substituteUserId()))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> createVacationRequestUseCase.createVacationRequest(
            new CreateVacationRequestCommand(
                VacationRequestTestData.FROM,
                VacationRequestTestData.TO,
                VacationRequestTestData.applicantUserId(),
                VacationRequestTestData.substituteUserId()
            )
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("substituteUserId does not reference an existing user");

        InOrder inOrder = inOrder(userRepositoryOutPort);
        inOrder.verify(userRepositoryOutPort).findById(VacationRequestTestData.applicantUserId());
        inOrder.verify(userRepositoryOutPort).findById(VacationRequestTestData.substituteUserId());
        verifyNoInteractions(saveVacationRequestOutPort, startVacationApprovalProcessOutPort);
        verifyNoMoreInteractions(userRepositoryOutPort);
    }
}
