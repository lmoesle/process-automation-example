package de.lmoesle.processautomationexample.adapter.out.persistence;

import de.lmoesle.processautomationexample.domain.user.TeamRole;
import de.lmoesle.processautomationexample.domain.user.UserTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserPersistenceAdapterTest {

    private UserJpaRepository userJpaRepository;
    private UserPersistenceAdapter userPersistenceAdapter;

    @BeforeEach
    void setUp() {
        userJpaRepository = mock(UserJpaRepository.class);
        userPersistenceAdapter = new UserPersistenceAdapter(userJpaRepository);
    }

    @Test
    void findsUserByIdIncludingTeams() {
        UserEntity adaEntity = userEntity(
            UserTestData.ADA_UUID,
            "Ada Lovelace",
            "ada.lovelace@example.com",
            new MembershipRecord(
                UserTestData.ENGINEERING_TEAM_UUID,
                UserTestData.ENGINEERING_TEAM,
                TeamRole.LEAD
            ),
            new MembershipRecord(
                UserTestData.PLATFORM_TEAM_UUID,
                UserTestData.PLATFORM_TEAM,
                TeamRole.USER
            )
        );
        when(userJpaRepository.findById(UserTestData.ADA_UUID)).thenReturn(Optional.of(adaEntity));

        var loadedUser = userPersistenceAdapter.findById(UserTestData.adaId());

        assertThat(loadedUser).contains(UserTestData.ada());
    }

    @Test
    void returnsEmptyWhenUserDoesNotExist() {
        when(userJpaRepository.findById(UserTestData.ADA_UUID)).thenReturn(Optional.empty());

        var loadedUser = userPersistenceAdapter.findById(UserTestData.adaId());

        assertThat(loadedUser).isEmpty();
    }

    @Test
    void findsAllLeadsOfATeam() {
        UserEntity leadCandidate = userEntity(
            UserTestData.ADA_UUID,
            "Ada Lovelace",
            "ada.lovelace@example.com",
            new MembershipRecord(
                UserTestData.ENGINEERING_TEAM_UUID,
                UserTestData.ENGINEERING_TEAM,
                TeamRole.LEAD
            ),
            new MembershipRecord(
                UserTestData.PLATFORM_TEAM_UUID,
                UserTestData.PLATFORM_TEAM,
                TeamRole.USER
            )
        );
        UserEntity hydratedLead = userEntity(
            UserTestData.ADA_UUID,
            "Ada Lovelace",
            "ada.lovelace@example.com",
            new MembershipRecord(
                UserTestData.ENGINEERING_TEAM_UUID,
                UserTestData.ENGINEERING_TEAM,
                TeamRole.LEAD
            ),
            new MembershipRecord(
                UserTestData.PLATFORM_TEAM_UUID,
                UserTestData.PLATFORM_TEAM,
                TeamRole.USER
            )
        );
        when(userJpaRepository.findDistinctByTeamMembershipsIdTeamIdAndTeamMembershipsRole(
            UserTestData.ENGINEERING_TEAM_UUID,
            TeamRole.LEAD
        )).thenReturn(List.of(leadCandidate));
        when(userJpaRepository.findDistinctByIdIn(List.of(UserTestData.ADA_UUID))).thenReturn(List.of(hydratedLead));

        var leads = userPersistenceAdapter.findAllLeadsByTeamId(UserTestData.engineeringTeamId());

        verify(userJpaRepository).findDistinctByTeamMembershipsIdTeamIdAndTeamMembershipsRole(
            eq(UserTestData.ENGINEERING_TEAM_UUID),
            eq(TeamRole.LEAD)
        );
        verify(userJpaRepository).findDistinctByIdIn(List.of(UserTestData.ADA_UUID));
        assertThat(leads).containsExactly(UserTestData.ada());
    }

    @Test
    void rejectsMissingTeamIdWhenLoadingLeads() {
        assertThatThrownBy(() -> userPersistenceAdapter.findAllLeadsByTeamId(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("teamId must not be null");
    }

    @Test
    void returnsEmptyWhenNoLeadExistsForTeam() {
        when(userJpaRepository.findDistinctByTeamMembershipsIdTeamIdAndTeamMembershipsRole(
            UserTestData.ENGINEERING_TEAM_UUID,
            TeamRole.LEAD
        )).thenReturn(List.of());

        var leads = userPersistenceAdapter.findAllLeadsByTeamId(UserTestData.engineeringTeamId());

        assertThat(leads).isEmpty();
    }

    private static UserEntity userEntity(UUID userId, String name, String email, MembershipRecord... memberships) {
        UserEntity userEntity = new UserEntity(userId, name, email, new LinkedHashSet<>());
        for (MembershipRecord membership : memberships) {
            TeamEntity teamEntity = new TeamEntity(membership.teamId(), membership.teamName());
            userEntity.getTeamMemberships().add(new TeamMembershipEntity(
                new TeamMembershipId(teamEntity.getId(), userId),
                teamEntity,
                userEntity,
                membership.role()
            ));
        }
        return userEntity;
    }

    private record MembershipRecord(UUID teamId, String teamName, TeamRole role) {
    }
}
