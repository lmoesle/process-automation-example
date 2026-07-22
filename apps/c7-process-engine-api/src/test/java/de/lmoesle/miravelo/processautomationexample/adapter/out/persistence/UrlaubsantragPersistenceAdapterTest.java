package de.lmoesle.miravelo.processautomationexample.adapter.out.persistence;

import de.lmoesle.miravelo.processautomationexample.adapter.out.persistence.entities.*;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragStatus;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
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
    private EntityManager entityManager;
    private UrlaubsantragPersistenceAdapter urlaubsantragPersistenceAdapter;

    @BeforeEach
    void setUp() {
        urlaubsantragJpaRepository = mock(UrlaubsantragJpaRepository.class);
        benutzerJpaRepository = mock(BenutzerJpaRepository.class);
        entityManager = mock(EntityManager.class);
        urlaubsantragPersistenceAdapter = new UrlaubsantragPersistenceAdapter(
            urlaubsantragJpaRepository,
            benutzerJpaRepository,
            entityManager
        );
    }

    @Test
    void preservesExistingProcessInstanceIdWhenSavingStaleUrlaubsantrag() {
        final UrlaubsantragEntity gespeicherterUrlaubsantrag = UrlaubsantragPersistenceMapper.toEntity(
            UrlaubsantragTestData.urlaubsantragWithStartedProcess()
        );
        when(urlaubsantragJpaRepository.findById(UrlaubsantragTestData.VACATION_REQUEST_UUID))
            .thenReturn(Optional.of(gespeicherterUrlaubsantrag));

        urlaubsantragPersistenceAdapter.speichere(UrlaubsantragTestData.urlaubsantrag());

        verify(urlaubsantragJpaRepository).findById(UrlaubsantragTestData.VACATION_REQUEST_UUID);
        verify(entityManager).refresh(gespeicherterUrlaubsantrag, LockModeType.PESSIMISTIC_WRITE);
        verify(urlaubsantragJpaRepository).saveAndFlush(argThat(entity ->
            UrlaubsantragTestData.PROCESS_INSTANCE_ID_VALUE.equals(entity.getProzessinstanzId())
        ));
    }

    @Test
    void rejectsConflictingProcessInstanceId() {
        final UrlaubsantragEntity gespeicherterUrlaubsantrag = UrlaubsantragPersistenceMapper.toEntity(
            UrlaubsantragTestData.urlaubsantragWithStartedProcess()
        );
        when(urlaubsantragJpaRepository.setzeProzessinstanzIdWennLeer(
            UrlaubsantragTestData.VACATION_REQUEST_UUID,
            UrlaubsantragTestData.SECOND_PROCESS_INSTANCE_ID_VALUE
        )).thenReturn(0);
        when(urlaubsantragJpaRepository.findById(UrlaubsantragTestData.VACATION_REQUEST_UUID))
            .thenReturn(Optional.of(gespeicherterUrlaubsantrag));

        assertThatThrownBy(() -> urlaubsantragPersistenceAdapter.speichereProzessinstanzId(
            UrlaubsantragTestData.urlaubsantragId(),
            UrlaubsantragTestData.secondProzessinstanzId()
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(UrlaubsantragTestData.VACATION_REQUEST_UUID.toString());

        verify(entityManager).refresh(gespeicherterUrlaubsantrag, LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    void storesProcessInstanceIdWithNarrowUpdate() {
        when(urlaubsantragJpaRepository.setzeProzessinstanzIdWennLeer(
            UrlaubsantragTestData.VACATION_REQUEST_UUID,
            UrlaubsantragTestData.PROCESS_INSTANCE_ID_VALUE
        )).thenReturn(1);

        urlaubsantragPersistenceAdapter.speichereProzessinstanzId(
            UrlaubsantragTestData.urlaubsantragId(),
            UrlaubsantragTestData.prozessinstanzId()
        );

        verify(urlaubsantragJpaRepository).setzeProzessinstanzIdWennLeer(
            UrlaubsantragTestData.VACATION_REQUEST_UUID,
            UrlaubsantragTestData.PROCESS_INSTANCE_ID_VALUE
        );
    }

    @Test
    void returnsEmptyWhenNoUrlaubsantragsExistForApplicant() {
        when(urlaubsantragJpaRepository.findAllByAntragstellerId(eq(BenutzerTestdaten.ADA_UUID), any(Sort.class)))
            .thenReturn(List.of());

        final var urlaubsantrags = urlaubsantragPersistenceAdapter.findeAlleNachAntragstellerId(BenutzerTestdaten.adaId());

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

        final var urlaubsantrag = urlaubsantragPersistenceAdapter.findeNachId(UrlaubsantragTestData.urlaubsantragId());

        verify(urlaubsantragJpaRepository).findById(UrlaubsantragTestData.VACATION_REQUEST_UUID);
        verifyNoInteractions(benutzerJpaRepository);
        assertThat(urlaubsantrag).isEmpty();
    }

    @Test
    void loadsUrlaubsantragByIdAndMapsUsers() {
        final UrlaubsantragEntity urlaubsantragEntity = new UrlaubsantragEntity(
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

        final var urlaubsantrag = urlaubsantragPersistenceAdapter.findeNachId(UrlaubsantragTestData.urlaubsantragId());

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
        final UrlaubsantragEntity firstUrlaubsantragEntity = new UrlaubsantragEntity(
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
        final UrlaubsantragEntity secondUrlaubsantragEntity = new UrlaubsantragEntity(
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

        final var urlaubsantrags = urlaubsantragPersistenceAdapter.findeAlleNachAntragstellerId(BenutzerTestdaten.adaId());

        verify(urlaubsantragJpaRepository).findAllByAntragstellerId(
            eq(BenutzerTestdaten.ADA_UUID),
            argThat(this::isDescendingByFromAndTo)
        );
        verify(benutzerJpaRepository).findDistinctByIdIn(argThat(this::containsAdaAndCarlaOnly));
        assertThat(urlaubsantrags).hasSize(2);
        assertThat(urlaubsantrags.getFirst().id()).isEqualTo(UrlaubsantragTestData.urlaubsantragId());
        assertThat(urlaubsantrags.getFirst().zeitraum().von()).isEqualTo(UrlaubsantragTestData.FROM);
        assertThat(urlaubsantrags.getFirst().zeitraum().bis()).isEqualTo(UrlaubsantragTestData.TO);
        assertThat(urlaubsantrags.getFirst().antragsteller()).isEqualTo(BenutzerTestdaten.ada());
        assertThat(urlaubsantrags.getFirst().vertretung()).isEqualTo(BenutzerTestdaten.carla());
        assertThat(urlaubsantrags.getFirst().vorgesetzter()).isEqualTo(BenutzerTestdaten.carla());
        assertThat(urlaubsantrags.getFirst().prozessinstanzId()).isEqualTo(UrlaubsantragTestData.prozessinstanzId());
        assertThat(urlaubsantrags.getFirst().status()).isEqualTo(UrlaubsantragStatus.ANTRAG_GESTELLT);
        assertThat(urlaubsantrags.getFirst().statusHistorie()).hasSize(1);

        assertThat(urlaubsantrags.getLast().id()).isEqualTo(UrlaubsantragTestData.secondUrlaubsantragId());
        assertThat(urlaubsantrags.getLast().zeitraum().von()).isEqualTo(UrlaubsantragTestData.SECOND_FROM);
        assertThat(urlaubsantrags.getLast().zeitraum().bis()).isEqualTo(UrlaubsantragTestData.SECOND_TO);
        assertThat(urlaubsantrags.getLast().antragsteller()).isEqualTo(BenutzerTestdaten.ada());
        assertThat(urlaubsantrags.getLast().vertretung()).isNull();
        assertThat(urlaubsantrags.getLast().vorgesetzter()).isNull();
        assertThat(urlaubsantrags.getLast().prozessinstanzId()).isNull();
        assertThat(urlaubsantrags.getLast().status()).isEqualTo(UrlaubsantragStatus.ANTRAG_GESTELLT);
        assertThat(urlaubsantrags.getLast().statusHistorie()).hasSize(1);
    }

    @Test
    void failsWhenReferencedSubstituteUserCannotBeLoaded() {
        final UrlaubsantragEntity urlaubsantragEntity = new UrlaubsantragEntity(
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
        final UrlaubsantragEntity urlaubsantragEntity = new UrlaubsantragEntity(
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
        final BenutzerEntity benutzerEntity = new BenutzerEntity(
            benutzer.id().value(),
            benutzer.name(),
            benutzer.email(),
            new LinkedHashSet<>()
        );
        for (final var team : benutzer.teams()) {
            final UUID teamId = team.name().equals(BenutzerTestdaten.ENGINEERING_TEAM)
                ? BenutzerTestdaten.ENGINEERING_TEAM_UUID
                : BenutzerTestdaten.PLATFORM_TEAM_UUID;
            final TeamEntity teamEntity = new TeamEntity(teamId, team.name());
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
