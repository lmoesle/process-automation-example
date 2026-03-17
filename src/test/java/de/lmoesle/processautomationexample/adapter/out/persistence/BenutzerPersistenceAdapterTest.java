package de.lmoesle.processautomationexample.adapter.out.persistence;

import de.lmoesle.processautomationexample.domain.benutzer.TeamRolle;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerTestdaten;
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

class BenutzerPersistenceAdapterTest {

    private BenutzerJpaRepository benutzerJpaRepository;
    private BenutzerPersistenceAdapter benutzerPersistenceAdapter;

    @BeforeEach
    void setUp() {
        benutzerJpaRepository = mock(BenutzerJpaRepository.class);
        benutzerPersistenceAdapter = new BenutzerPersistenceAdapter(benutzerJpaRepository);
    }

    @Test
    void findsUserByIdIncludingTeams() {
        BenutzerEntity adaEntity = userEntity(
            BenutzerTestdaten.ADA_UUID,
            "Ada Lovelace",
            "ada.lovelace@example.com",
            new MembershipRecord(
                BenutzerTestdaten.ENGINEERING_TEAM_UUID,
                BenutzerTestdaten.ENGINEERING_TEAM,
                TeamRolle.LEITUNG
            ),
            new MembershipRecord(
                BenutzerTestdaten.PLATFORM_TEAM_UUID,
                BenutzerTestdaten.PLATFORM_TEAM,
                TeamRolle.MITGLIED
            )
        );
        when(benutzerJpaRepository.findById(BenutzerTestdaten.ADA_UUID)).thenReturn(Optional.of(adaEntity));

        var geladenerBenutzer = benutzerPersistenceAdapter.findeNachId(BenutzerTestdaten.adaId());

        assertThat(geladenerBenutzer).contains(BenutzerTestdaten.ada());
    }

    @Test
    void returnsEmptyWhenUserDoesNotExist() {
        when(benutzerJpaRepository.findById(BenutzerTestdaten.ADA_UUID)).thenReturn(Optional.empty());

        var geladenerBenutzer = benutzerPersistenceAdapter.findeNachId(BenutzerTestdaten.adaId());

        assertThat(geladenerBenutzer).isEmpty();
    }

    @Test
    void findsAllLeadsOfATeam() {
        BenutzerEntity leiterKandidat = userEntity(
            BenutzerTestdaten.ADA_UUID,
            "Ada Lovelace",
            "ada.lovelace@example.com",
            new MembershipRecord(
                BenutzerTestdaten.ENGINEERING_TEAM_UUID,
                BenutzerTestdaten.ENGINEERING_TEAM,
                TeamRolle.LEITUNG
            ),
            new MembershipRecord(
                BenutzerTestdaten.PLATFORM_TEAM_UUID,
                BenutzerTestdaten.PLATFORM_TEAM,
                TeamRolle.MITGLIED
            )
        );
        BenutzerEntity geladenerLeiter = userEntity(
            BenutzerTestdaten.ADA_UUID,
            "Ada Lovelace",
            "ada.lovelace@example.com",
            new MembershipRecord(
                BenutzerTestdaten.ENGINEERING_TEAM_UUID,
                BenutzerTestdaten.ENGINEERING_TEAM,
                TeamRolle.LEITUNG
            ),
            new MembershipRecord(
                BenutzerTestdaten.PLATFORM_TEAM_UUID,
                BenutzerTestdaten.PLATFORM_TEAM,
                TeamRolle.MITGLIED
            )
        );
        when(benutzerJpaRepository.findDistinctByTeamMitgliedschaftenIdTeamIdAndTeamMitgliedschaftenRolle(
            BenutzerTestdaten.ENGINEERING_TEAM_UUID,
            TeamRolle.LEITUNG
        )).thenReturn(List.of(leiterKandidat));
        when(benutzerJpaRepository.findDistinctByIdIn(List.of(BenutzerTestdaten.ADA_UUID))).thenReturn(List.of(geladenerLeiter));

        var leitende = benutzerPersistenceAdapter.findeAlleLeitendenNachTeamId(BenutzerTestdaten.engineeringTeamId());

        verify(benutzerJpaRepository).findDistinctByTeamMitgliedschaftenIdTeamIdAndTeamMitgliedschaftenRolle(
            eq(BenutzerTestdaten.ENGINEERING_TEAM_UUID),
            eq(TeamRolle.LEITUNG)
        );
        verify(benutzerJpaRepository).findDistinctByIdIn(List.of(BenutzerTestdaten.ADA_UUID));
        assertThat(leitende).containsExactly(BenutzerTestdaten.ada());
    }

    @Test
    void rejectsMissingTeamIdWhenLoadingLeads() {
        assertThatThrownBy(() -> benutzerPersistenceAdapter.findeAlleLeitendenNachTeamId(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("teamId darf nicht null sein");
    }

    @Test
    void returnsEmptyWhenNoLeadExistsForTeam() {
        when(benutzerJpaRepository.findDistinctByTeamMitgliedschaftenIdTeamIdAndTeamMitgliedschaftenRolle(
            BenutzerTestdaten.ENGINEERING_TEAM_UUID,
            TeamRolle.LEITUNG
        )).thenReturn(List.of());

        var leitende = benutzerPersistenceAdapter.findeAlleLeitendenNachTeamId(BenutzerTestdaten.engineeringTeamId());

        assertThat(leitende).isEmpty();
    }

    private static BenutzerEntity userEntity(UUID benutzerId, String name, String email, MembershipRecord... memberships) {
        BenutzerEntity benutzerEntity = new BenutzerEntity(benutzerId, name, email, new LinkedHashSet<>());
        for (MembershipRecord membership : memberships) {
            TeamEntity teamEntity = new TeamEntity(membership.teamId(), membership.teamName());
            benutzerEntity.getTeamMitgliedschaften().add(new TeamMitgliedschaftEntity(
                new TeamMitgliedschaftId(teamEntity.getId(), benutzerId),
                teamEntity,
                benutzerEntity,
                membership.rolle()
            ));
        }
        return benutzerEntity;
    }

    private record MembershipRecord(UUID teamId, String teamName, TeamRolle rolle) {
    }
}
