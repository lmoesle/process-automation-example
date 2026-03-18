package de.lmoesle.processautomationexample.adapter.out.persistence;

import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragId;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragStatus;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.data.domain.Sort.Direction.DESC;

class UrlaubsantragPersistenceAdapterTest {

    private UrlaubsantragJpaRepository urlaubsantragJpaRepository;
    private BenutzerJpaRepository benutzerJpaRepository;
    private UrlaubsantragPersistenceAdapter urlaubsantragPersistenceAdapter;

    @BeforeEach
    void setUp() {
        urlaubsantragJpaRepository = mock(UrlaubsantragJpaRepository.class);
        benutzerJpaRepository = mock(BenutzerJpaRepository.class);
        urlaubsantragPersistenceAdapter = new UrlaubsantragPersistenceAdapter(
            urlaubsantragJpaRepository,
            benutzerJpaRepository
        );
    }

    @Test
    void returnsEmptyWhenNoUrlaubsantragsExistForApplicant() {
        when(urlaubsantragJpaRepository.findAllByAntragstellerId(eq(BenutzerTestdaten.ADA_UUID), any(Sort.class)))
            .thenReturn(List.of());

        var urlaubsantrags = urlaubsantragPersistenceAdapter.findeAlleNachAntragstellerId(BenutzerTestdaten.adaId());

        verify(urlaubsantragJpaRepository).findAllByAntragstellerId(
            eq(BenutzerTestdaten.ADA_UUID),
            argThat(this::isDescendingByFromAndTo)
        );
        verifyNoInteractions(benutzerJpaRepository);
        assertThat(urlaubsantrags).isEmpty();
    }

    @Test
    void returnsEmptyWhenUrlaubsantragDoesNotExistById() {
        when(urlaubsantragJpaRepository.findById(UrlaubsantragTestData.VACATION_REQUEST_UUID))
            .thenReturn(java.util.Optional.empty());

        var urlaubsantrag = urlaubsantragPersistenceAdapter.findeNachId(UrlaubsantragTestData.urlaubsantragId());

        verify(urlaubsantragJpaRepository).findById(UrlaubsantragTestData.VACATION_REQUEST_UUID);
        verifyNoInteractions(benutzerJpaRepository);
        assertThat(urlaubsantrag).isEmpty();
    }

    @Test
    void loadsUrlaubsantragByIdAndMapsUsers() {
        UrlaubsantragEntity urlaubsantragEntity = new UrlaubsantragEntity(
            UrlaubsantragTestData.VACATION_REQUEST_UUID,
            UrlaubsantragTestData.FROM,
            UrlaubsantragTestData.TO,
            BenutzerTestdaten.ADA_UUID,
            BenutzerTestdaten.CARLA_UUID,
            BenutzerTestdaten.CARLA_UUID,
            UrlaubsantragTestData.PROCESS_INSTANCE_ID_VALUE,
            UrlaubsantragStatus.ANTRAG_GESTELLT,
            history(UrlaubsantragStatus.ANTRAG_GESTELLT)
        );
        when(urlaubsantragJpaRepository.findById(UrlaubsantragTestData.VACATION_REQUEST_UUID))
            .thenReturn(java.util.Optional.of(urlaubsantragEntity));
        when(benutzerJpaRepository.findDistinctByIdIn(argThat(this::containsAdaAndCarlaOnly)))
            .thenReturn(List.of(userEntity(BenutzerTestdaten.ada()), userEntity(BenutzerTestdaten.carla())));

        var urlaubsantrag = urlaubsantragPersistenceAdapter.findeNachId(UrlaubsantragTestData.urlaubsantragId());

        verify(urlaubsantragJpaRepository).findById(UrlaubsantragTestData.VACATION_REQUEST_UUID);
        verify(benutzerJpaRepository).findDistinctByIdIn(argThat(this::containsAdaAndCarlaOnly));
        assertThat(urlaubsantrag).hasValueSatisfying(request -> {
            assertThat(request.id()).isEqualTo(UrlaubsantragTestData.urlaubsantragId());
            assertThat(request.zeitraum().von()).isEqualTo(UrlaubsantragTestData.FROM);
            assertThat(request.zeitraum().bis()).isEqualTo(UrlaubsantragTestData.TO);
            assertThat(request.antragsteller()).isEqualTo(BenutzerTestdaten.ada());
            assertThat(request.vertretung()).isEqualTo(BenutzerTestdaten.carla());
            assertThat(request.vorgesetzter()).isEqualTo(BenutzerTestdaten.carla());
            assertThat(request.prozessinstanzId()).isEqualTo(UrlaubsantragTestData.prozessinstanzId());
            assertThat(request.status()).isEqualTo(UrlaubsantragStatus.ANTRAG_GESTELLT);
            assertThat(request.statusHistorie()).hasSize(1)
                .first()
                .satisfies(entry -> {
                    assertThat(entry.status()).isEqualTo(UrlaubsantragStatus.ANTRAG_GESTELLT);
                    assertThat(entry.kommentar()).isNull();
                });
        });
    }

    @Test
    void loadsUrlaubsantragsForApplicantAndMapsUsers() {
        UrlaubsantragEntity firstUrlaubsantragEntity = new UrlaubsantragEntity(
            UrlaubsantragTestData.VACATION_REQUEST_UUID,
            UrlaubsantragTestData.FROM,
            UrlaubsantragTestData.TO,
            BenutzerTestdaten.ADA_UUID,
            BenutzerTestdaten.CARLA_UUID,
            BenutzerTestdaten.CARLA_UUID,
            UrlaubsantragTestData.PROCESS_INSTANCE_ID_VALUE,
            UrlaubsantragStatus.ANTRAG_GESTELLT,
            history(UrlaubsantragStatus.ANTRAG_GESTELLT)
        );
        UrlaubsantragEntity secondUrlaubsantragEntity = new UrlaubsantragEntity(
            UrlaubsantragTestData.SECOND_VACATION_REQUEST_UUID,
            UrlaubsantragTestData.SECOND_FROM,
            UrlaubsantragTestData.SECOND_TO,
            BenutzerTestdaten.ADA_UUID,
            null,
            null,
            null,
            UrlaubsantragStatus.ANTRAG_GESTELLT,
            history(UrlaubsantragStatus.ANTRAG_GESTELLT)
        );
        when(urlaubsantragJpaRepository.findAllByAntragstellerId(eq(BenutzerTestdaten.ADA_UUID), any(Sort.class)))
            .thenReturn(List.of(firstUrlaubsantragEntity, secondUrlaubsantragEntity));
        when(benutzerJpaRepository.findDistinctByIdIn(argThat(this::containsAdaAndCarlaOnly)))
            .thenReturn(List.of(userEntity(BenutzerTestdaten.ada()), userEntity(BenutzerTestdaten.carla())));

        var urlaubsantrags = urlaubsantragPersistenceAdapter.findeAlleNachAntragstellerId(BenutzerTestdaten.adaId());

        verify(urlaubsantragJpaRepository).findAllByAntragstellerId(
            eq(BenutzerTestdaten.ADA_UUID),
            argThat(this::isDescendingByFromAndTo)
        );
        verify(benutzerJpaRepository).findDistinctByIdIn(argThat(this::containsAdaAndCarlaOnly));
        assertThat(urlaubsantrags).hasSize(2);
        assertThat(urlaubsantrags.get(0).id()).isEqualTo(UrlaubsantragTestData.urlaubsantragId());
        assertThat(urlaubsantrags.get(0).zeitraum().von()).isEqualTo(UrlaubsantragTestData.FROM);
        assertThat(urlaubsantrags.get(0).zeitraum().bis()).isEqualTo(UrlaubsantragTestData.TO);
        assertThat(urlaubsantrags.get(0).antragsteller()).isEqualTo(BenutzerTestdaten.ada());
        assertThat(urlaubsantrags.get(0).vertretung()).isEqualTo(BenutzerTestdaten.carla());
        assertThat(urlaubsantrags.get(0).vorgesetzter()).isEqualTo(BenutzerTestdaten.carla());
        assertThat(urlaubsantrags.get(0).prozessinstanzId()).isEqualTo(UrlaubsantragTestData.prozessinstanzId());
        assertThat(urlaubsantrags.get(0).status()).isEqualTo(UrlaubsantragStatus.ANTRAG_GESTELLT);
        assertThat(urlaubsantrags.get(0).statusHistorie()).hasSize(1);

        assertThat(urlaubsantrags.get(1).id()).isEqualTo(UrlaubsantragTestData.secondUrlaubsantragId());
        assertThat(urlaubsantrags.get(1).zeitraum().von()).isEqualTo(UrlaubsantragTestData.SECOND_FROM);
        assertThat(urlaubsantrags.get(1).zeitraum().bis()).isEqualTo(UrlaubsantragTestData.SECOND_TO);
        assertThat(urlaubsantrags.get(1).antragsteller()).isEqualTo(BenutzerTestdaten.ada());
        assertThat(urlaubsantrags.get(1).vertretung()).isNull();
        assertThat(urlaubsantrags.get(1).vorgesetzter()).isNull();
        assertThat(urlaubsantrags.get(1).prozessinstanzId()).isNull();
        assertThat(urlaubsantrags.get(1).status()).isEqualTo(UrlaubsantragStatus.ANTRAG_GESTELLT);
        assertThat(urlaubsantrags.get(1).statusHistorie()).hasSize(1);
    }

    @Test
    void rejectsNullApplicantBenutzerIdWhenLoadingUrlaubsantrags() {
        assertThatThrownBy(() -> urlaubsantragPersistenceAdapter.findeAlleNachAntragstellerId(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("antragstellerId darf nicht null sein");
    }

    @Test
    void rejectsNullUrlaubsantragIdWhenLoadingUrlaubsantragById() {
        assertThatThrownBy(() -> urlaubsantragPersistenceAdapter.findeNachId((UrlaubsantragId) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("urlaubsantragId darf nicht null sein");
    }

    @Test
    void failsWhenReferencedSubstituteUserCannotBeLoaded() {
        UrlaubsantragEntity urlaubsantragEntity = new UrlaubsantragEntity(
            UrlaubsantragTestData.VACATION_REQUEST_UUID,
            UrlaubsantragTestData.FROM,
            UrlaubsantragTestData.TO,
            BenutzerTestdaten.ADA_UUID,
            BenutzerTestdaten.CARLA_UUID,
            null,
            UrlaubsantragTestData.PROCESS_INSTANCE_ID_VALUE,
            UrlaubsantragStatus.ANTRAG_GESTELLT,
            history(UrlaubsantragStatus.ANTRAG_GESTELLT)
        );
        when(urlaubsantragJpaRepository.findAllByAntragstellerId(eq(BenutzerTestdaten.ADA_UUID), any(Sort.class)))
            .thenReturn(List.of(urlaubsantragEntity));
        when(benutzerJpaRepository.findDistinctByIdIn(argThat(this::containsAdaAndCarlaOnly)))
            .thenReturn(List.of(userEntity(BenutzerTestdaten.ada())));

        assertThatThrownBy(() -> urlaubsantragPersistenceAdapter.findeAlleNachAntragstellerId(BenutzerTestdaten.adaId()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("vertretungId")
            .hasMessageContaining(BenutzerTestdaten.CARLA_UUID.toString())
            .hasMessageContaining(UrlaubsantragTestData.VACATION_REQUEST_UUID.toString());
    }

    @Test
    void failsWhenReferencedSupervisorUserCannotBeLoaded() {
        UrlaubsantragEntity urlaubsantragEntity = new UrlaubsantragEntity(
            UrlaubsantragTestData.VACATION_REQUEST_UUID,
            UrlaubsantragTestData.FROM,
            UrlaubsantragTestData.TO,
            BenutzerTestdaten.ADA_UUID,
            null,
            BenutzerTestdaten.CARLA_UUID,
            UrlaubsantragTestData.PROCESS_INSTANCE_ID_VALUE,
            UrlaubsantragStatus.ANTRAG_GESTELLT,
            history(UrlaubsantragStatus.ANTRAG_GESTELLT)
        );
        when(urlaubsantragJpaRepository.findAllByAntragstellerId(eq(BenutzerTestdaten.ADA_UUID), any(Sort.class)))
            .thenReturn(List.of(urlaubsantragEntity));
        when(benutzerJpaRepository.findDistinctByIdIn(argThat(benutzerIds ->
            benutzerIds.size() == 2
                && benutzerIds.contains(BenutzerTestdaten.ADA_UUID)
                && benutzerIds.contains(BenutzerTestdaten.CARLA_UUID)
        ))).thenReturn(List.of(userEntity(BenutzerTestdaten.ada())));

        assertThatThrownBy(() -> urlaubsantragPersistenceAdapter.findeAlleNachAntragstellerId(BenutzerTestdaten.adaId()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("vorgesetzterId")
            .hasMessageContaining(BenutzerTestdaten.CARLA_UUID.toString())
            .hasMessageContaining(UrlaubsantragTestData.VACATION_REQUEST_UUID.toString());
    }

    private boolean isDescendingByFromAndTo(Sort sort) {
        return sort.getOrderFor("von") != null
            && sort.getOrderFor("von").getDirection() == DESC
            && sort.getOrderFor("bis") != null
            && sort.getOrderFor("bis").getDirection() == DESC;
    }

    private boolean containsAdaAndCarlaOnly(Collection<UUID> benutzerIds) {
        return benutzerIds.size() == 2
            && benutzerIds.contains(BenutzerTestdaten.ADA_UUID)
            && benutzerIds.contains(BenutzerTestdaten.CARLA_UUID);
    }

    private static BenutzerEntity userEntity(Benutzer benutzer) {
        BenutzerEntity benutzerEntity = new BenutzerEntity(
            benutzer.id().value(),
            benutzer.name(),
            benutzer.email(),
            new LinkedHashSet<>()
        );
        for (var team : benutzer.teams()) {
            UUID teamId = team.name().equals(BenutzerTestdaten.ENGINEERING_TEAM)
                ? BenutzerTestdaten.ENGINEERING_TEAM_UUID
                : BenutzerTestdaten.PLATFORM_TEAM_UUID;
            TeamEntity teamEntity = new TeamEntity(teamId, team.name());
            benutzerEntity.getTeamMitgliedschaften().add(new TeamMitgliedschaftEntity(
                new TeamMitgliedschaftId(teamId, benutzer.id().value()),
                teamEntity,
                benutzerEntity,
                team.rolle()
            ));
        }
        return benutzerEntity;
    }

    private static List<UrlaubsantragStatusHistorieneintragEmbeddable> history(UrlaubsantragStatus... statuses) {
        return Arrays.stream(statuses)
            .map(status -> new UrlaubsantragStatusHistorieneintragEmbeddable(status, null))
            .toList();
    }
}
