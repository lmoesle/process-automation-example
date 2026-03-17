package de.lmoesle.processautomationexample.adapter.out.persistence;

import de.lmoesle.processautomationexample.domain.user.TeamRole;
import de.lmoesle.processautomationexample.domain.user.User;
import de.lmoesle.processautomationexample.domain.user.UserTestData;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationPeriod;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequest;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestStatus;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestTestData;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestId;
import de.lmoesle.processautomationexample.adapter.out.persistence.VacationRequestStatusHistoryEntryEmbeddable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Sort.Direction.DESC;

class VacationRequestPersistenceAdapterTest {

    private VacationRequestJpaRepository vacationRequestJpaRepository;
    private UserJpaRepository userJpaRepository;
    private VacationRequestPersistenceAdapter vacationRequestPersistenceAdapter;

    @BeforeEach
    void setUp() {
        vacationRequestJpaRepository = mock(VacationRequestJpaRepository.class);
        userJpaRepository = mock(UserJpaRepository.class);
        vacationRequestPersistenceAdapter = new VacationRequestPersistenceAdapter(
            vacationRequestJpaRepository,
            userJpaRepository
        );
    }

    @Test
    void returnsEmptyWhenNoVacationRequestsExistForApplicant() {
        when(vacationRequestJpaRepository.findAllByApplicantUserId(eq(UserTestData.ADA_UUID), any(Sort.class)))
            .thenReturn(List.of());

        var vacationRequests = vacationRequestPersistenceAdapter.findAllByApplicantUserId(UserTestData.adaId());

        verify(vacationRequestJpaRepository).findAllByApplicantUserId(
            eq(UserTestData.ADA_UUID),
            argThat(this::isDescendingByFromAndTo)
        );
        verifyNoInteractions(userJpaRepository);
        assertThat(vacationRequests).isEmpty();
    }

    @Test
    void returnsEmptyWhenVacationRequestDoesNotExistById() {
        when(vacationRequestJpaRepository.findById(VacationRequestTestData.VACATION_REQUEST_UUID))
            .thenReturn(java.util.Optional.empty());

        var vacationRequest = vacationRequestPersistenceAdapter.findById(VacationRequestTestData.vacationRequestId());

        verify(vacationRequestJpaRepository).findById(VacationRequestTestData.VACATION_REQUEST_UUID);
        verifyNoInteractions(userJpaRepository);
        assertThat(vacationRequest).isEmpty();
    }

    @Test
    void loadsVacationRequestByIdAndMapsUsers() {
        VacationRequestEntity vacationRequestEntity = new VacationRequestEntity(
            VacationRequestTestData.VACATION_REQUEST_UUID,
            VacationRequestTestData.FROM,
            VacationRequestTestData.TO,
            UserTestData.ADA_UUID,
            UserTestData.CARLA_UUID,
            VacationRequestTestData.PROCESS_INSTANCE_ID_VALUE,
            VacationRequestStatus.ANTRAG_GESTELLT,
            history(VacationRequestStatus.ANTRAG_GESTELLT)
        );
        when(vacationRequestJpaRepository.findById(VacationRequestTestData.VACATION_REQUEST_UUID))
            .thenReturn(java.util.Optional.of(vacationRequestEntity));
        when(userJpaRepository.findDistinctByIdIn(argThat(this::containsAdaAndCarlaOnly)))
            .thenReturn(List.of(userEntity(UserTestData.ada()), userEntity(UserTestData.carla())));

        var vacationRequest = vacationRequestPersistenceAdapter.findById(VacationRequestTestData.vacationRequestId());

        verify(vacationRequestJpaRepository).findById(VacationRequestTestData.VACATION_REQUEST_UUID);
        verify(userJpaRepository).findDistinctByIdIn(argThat(this::containsAdaAndCarlaOnly));
        assertThat(vacationRequest).hasValueSatisfying(request -> {
            assertThat(request.id()).isEqualTo(VacationRequestTestData.vacationRequestId());
            assertThat(request.period().from()).isEqualTo(VacationRequestTestData.FROM);
            assertThat(request.period().to()).isEqualTo(VacationRequestTestData.TO);
            assertThat(request.applicantUser()).isEqualTo(UserTestData.ada());
            assertThat(request.substituteUser()).isEqualTo(UserTestData.carla());
            assertThat(request.processInstanceId()).isEqualTo(VacationRequestTestData.processInstanceId());
            assertThat(request.status()).isEqualTo(VacationRequestStatus.ANTRAG_GESTELLT);
            assertThat(request.statusHistory()).hasSize(1)
                .first()
                .satisfies(entry -> {
                    assertThat(entry.status()).isEqualTo(VacationRequestStatus.ANTRAG_GESTELLT);
                    assertThat(entry.comment()).isNull();
                });
        });
    }

    @Test
    void loadsVacationRequestsForApplicantAndMapsUsers() {
        VacationRequestEntity firstVacationRequestEntity = new VacationRequestEntity(
            VacationRequestTestData.VACATION_REQUEST_UUID,
            VacationRequestTestData.FROM,
            VacationRequestTestData.TO,
            UserTestData.ADA_UUID,
            UserTestData.CARLA_UUID,
            VacationRequestTestData.PROCESS_INSTANCE_ID_VALUE,
            VacationRequestStatus.ANTRAG_GESTELLT,
            history(VacationRequestStatus.ANTRAG_GESTELLT)
        );
        VacationRequestEntity secondVacationRequestEntity = new VacationRequestEntity(
            VacationRequestTestData.SECOND_VACATION_REQUEST_UUID,
            VacationRequestTestData.SECOND_FROM,
            VacationRequestTestData.SECOND_TO,
            UserTestData.ADA_UUID,
            null,
            null,
            VacationRequestStatus.ANTRAG_GESTELLT,
            history(VacationRequestStatus.ANTRAG_GESTELLT)
        );
        when(vacationRequestJpaRepository.findAllByApplicantUserId(eq(UserTestData.ADA_UUID), any(Sort.class)))
            .thenReturn(List.of(firstVacationRequestEntity, secondVacationRequestEntity));
        when(userJpaRepository.findDistinctByIdIn(argThat(this::containsAdaAndCarlaOnly)))
            .thenReturn(List.of(userEntity(UserTestData.ada()), userEntity(UserTestData.carla())));

        var vacationRequests = vacationRequestPersistenceAdapter.findAllByApplicantUserId(UserTestData.adaId());

        verify(vacationRequestJpaRepository).findAllByApplicantUserId(
            eq(UserTestData.ADA_UUID),
            argThat(this::isDescendingByFromAndTo)
        );
        verify(userJpaRepository).findDistinctByIdIn(argThat(this::containsAdaAndCarlaOnly));
        assertThat(vacationRequests).hasSize(2);
        assertThat(vacationRequests.get(0).id()).isEqualTo(VacationRequestTestData.vacationRequestId());
        assertThat(vacationRequests.get(0).period().from()).isEqualTo(VacationRequestTestData.FROM);
        assertThat(vacationRequests.get(0).period().to()).isEqualTo(VacationRequestTestData.TO);
        assertThat(vacationRequests.get(0).applicantUser()).isEqualTo(UserTestData.ada());
        assertThat(vacationRequests.get(0).substituteUser()).isEqualTo(UserTestData.carla());
        assertThat(vacationRequests.get(0).processInstanceId()).isEqualTo(VacationRequestTestData.processInstanceId());
        assertThat(vacationRequests.get(0).status()).isEqualTo(VacationRequestStatus.ANTRAG_GESTELLT);
        assertThat(vacationRequests.get(0).statusHistory()).hasSize(1);

        assertThat(vacationRequests.get(1).id()).isEqualTo(VacationRequestTestData.secondVacationRequestId());
        assertThat(vacationRequests.get(1).period().from()).isEqualTo(VacationRequestTestData.SECOND_FROM);
        assertThat(vacationRequests.get(1).period().to()).isEqualTo(VacationRequestTestData.SECOND_TO);
        assertThat(vacationRequests.get(1).applicantUser()).isEqualTo(UserTestData.ada());
        assertThat(vacationRequests.get(1).substituteUser()).isNull();
        assertThat(vacationRequests.get(1).processInstanceId()).isNull();
        assertThat(vacationRequests.get(1).status()).isEqualTo(VacationRequestStatus.ANTRAG_GESTELLT);
        assertThat(vacationRequests.get(1).statusHistory()).hasSize(1);
    }

    @Test
    void rejectsNullApplicantUserIdWhenLoadingVacationRequests() {
        assertThatThrownBy(() -> vacationRequestPersistenceAdapter.findAllByApplicantUserId(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("applicantUserId must not be null");
    }

    @Test
    void rejectsNullVacationRequestIdWhenLoadingVacationRequestById() {
        assertThatThrownBy(() -> vacationRequestPersistenceAdapter.findById((VacationRequestId) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("vacationRequestId must not be null");
    }

    @Test
    void failsWhenReferencedSubstituteUserCannotBeLoaded() {
        VacationRequestEntity vacationRequestEntity = new VacationRequestEntity(
            VacationRequestTestData.VACATION_REQUEST_UUID,
            VacationRequestTestData.FROM,
            VacationRequestTestData.TO,
            UserTestData.ADA_UUID,
            UserTestData.CARLA_UUID,
            VacationRequestTestData.PROCESS_INSTANCE_ID_VALUE,
            VacationRequestStatus.ANTRAG_GESTELLT,
            history(VacationRequestStatus.ANTRAG_GESTELLT)
        );
        when(vacationRequestJpaRepository.findAllByApplicantUserId(eq(UserTestData.ADA_UUID), any(Sort.class)))
            .thenReturn(List.of(vacationRequestEntity));
        when(userJpaRepository.findDistinctByIdIn(argThat(this::containsAdaAndCarlaOnly)))
            .thenReturn(List.of(userEntity(UserTestData.ada())));

        assertThatThrownBy(() -> vacationRequestPersistenceAdapter.findAllByApplicantUserId(UserTestData.adaId()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("substituteUserId")
            .hasMessageContaining(UserTestData.CARLA_UUID.toString())
            .hasMessageContaining(VacationRequestTestData.VACATION_REQUEST_UUID.toString());
    }

    private boolean isDescendingByFromAndTo(Sort sort) {
        return sort.getOrderFor("from") != null
            && sort.getOrderFor("from").getDirection() == DESC
            && sort.getOrderFor("to") != null
            && sort.getOrderFor("to").getDirection() == DESC;
    }

    private boolean containsAdaAndCarlaOnly(Collection<UUID> userIds) {
        return userIds.size() == 2
            && userIds.contains(UserTestData.ADA_UUID)
            && userIds.contains(UserTestData.CARLA_UUID);
    }

    private static UserEntity userEntity(User user) {
        UserEntity userEntity = new UserEntity(
            user.id().value(),
            user.name(),
            user.email(),
            new LinkedHashSet<>()
        );
        for (var team : user.teams()) {
            UUID teamId = team.name().equals(UserTestData.ENGINEERING_TEAM)
                ? UserTestData.ENGINEERING_TEAM_UUID
                : UserTestData.PLATFORM_TEAM_UUID;
            TeamEntity teamEntity = new TeamEntity(teamId, team.name());
            userEntity.getTeamMemberships().add(new TeamMembershipEntity(
                new TeamMembershipId(teamId, user.id().value()),
                teamEntity,
                userEntity,
                team.role()
            ));
        }
        return userEntity;
    }

    private static List<VacationRequestStatusHistoryEntryEmbeddable> history(VacationRequestStatus... statuses) {
        return Arrays.stream(statuses)
            .map(status -> new VacationRequestStatusHistoryEntryEmbeddable(status, null))
            .toList();
    }
}
