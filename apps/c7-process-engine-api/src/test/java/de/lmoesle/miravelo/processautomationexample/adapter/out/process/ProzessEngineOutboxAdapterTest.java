package de.lmoesle.miravelo.processautomationexample.adapter.out.process;

import de.lmoesle.miravelo.processautomationexample.adapter.out.persistence.ProzessEngineOutboxAuftragJpaRepository;
import de.lmoesle.miravelo.processautomationexample.adapter.out.persistence.entities.ProzessEngineOutboxAuftragEntity;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.StarteGenehmigungsprozessDirektOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.TaskBearbeitenDirektOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.UrlaubsantragProzessinstanzSpeichernOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.UrlaubsantraegeLadenOutPort;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProzessEngineOutboxAdapterTest {

    private ProzessEngineOutboxAuftragJpaRepository outboxAuftragJpaRepository;
    private ProzessEngineOutboxTransaktionen outboxTransaktionen;
    private StarteGenehmigungsprozessDirektOutPort starteGenehmigungsprozessDirektOutPort;
    private TaskBearbeitenDirektOutPort taskBearbeitenDirektOutPort;
    private UrlaubsantraegeLadenOutPort urlaubsantraegeLadenOutPort;
    private UrlaubsantragProzessinstanzSpeichernOutPort urlaubsantragProzessinstanzSpeichernOutPort;
    private ProzessEngineOutboxAdapter prozessEngineOutboxAdapter;

    @BeforeEach
    void setUp() {
        outboxAuftragJpaRepository = mock(ProzessEngineOutboxAuftragJpaRepository.class);
        outboxTransaktionen = new ProzessEngineOutboxTransaktionen(outboxAuftragJpaRepository);
        starteGenehmigungsprozessDirektOutPort = mock(StarteGenehmigungsprozessDirektOutPort.class);
        taskBearbeitenDirektOutPort = mock(TaskBearbeitenDirektOutPort.class);
        urlaubsantraegeLadenOutPort = mock(UrlaubsantraegeLadenOutPort.class);
        urlaubsantragProzessinstanzSpeichernOutPort = mock(UrlaubsantragProzessinstanzSpeichernOutPort.class);
        prozessEngineOutboxAdapter = new ProzessEngineOutboxAdapter(
            outboxAuftragJpaRepository,
            outboxTransaktionen,
            starteGenehmigungsprozessDirektOutPort,
            taskBearbeitenDirektOutPort,
            urlaubsantraegeLadenOutPort,
            urlaubsantragProzessinstanzSpeichernOutPort
        );
    }

    @Test
    void enqueuesProcessStart() {
        prozessEngineOutboxAdapter.starteGenehmigungsprozessFuer(
            UrlaubsantragTestData.urlaubsantrag(),
            List.of(BenutzerTestdaten.adaId(), BenutzerTestdaten.carlaId())
        );

        final ArgumentCaptor<ProzessEngineOutboxAuftragEntity> auftragCaptor = ArgumentCaptor.forClass(
            ProzessEngineOutboxAuftragEntity.class
        );
        verify(outboxAuftragJpaRepository).save(auftragCaptor.capture());

        assertThat(auftragCaptor.getValue().getTyp()).isEqualTo(ProzessEngineOutboxAuftragTyp.STARTE_GENEHMIGUNGSPROZESS);
        assertThat(auftragCaptor.getValue().getStatus()).isEqualTo(ProzessEngineOutboxAuftragStatus.OFFEN);
        assertThat(auftragCaptor.getValue().getUrlaubsantragId()).isEqualTo(UrlaubsantragTestData.VACATION_REQUEST_UUID);
        assertThat(ProzessEngineOutboxMapper.teamLeadIds(auftragCaptor.getValue()))
            .containsExactly(BenutzerTestdaten.adaId(), BenutzerTestdaten.carlaId());
    }

    @Test
    void startsProcessAndStoresProcessInstanceId() {
        final var auftrag = ProzessEngineOutboxMapper.starteGenehmigungsprozess(
            UrlaubsantragTestData.urlaubsantrag(),
            List.of(BenutzerTestdaten.adaId()),
            java.time.Instant.now()
        );
        when(outboxAuftragJpaRepository.findeFaelligeAuftraege(any(), any(), anyInt(), any()))
            .thenReturn(List.of(auftrag), List.of());
        when(urlaubsantraegeLadenOutPort.findeNachId(UrlaubsantragTestData.urlaubsantragId()))
            .thenReturn(Optional.of(UrlaubsantragTestData.urlaubsantrag()))
            .thenReturn(Optional.of(UrlaubsantragTestData.urlaubsantrag()));
        when(starteGenehmigungsprozessDirektOutPort.starteGenehmigungsprozess(
            UrlaubsantragTestData.urlaubsantragId(),
            List.of(BenutzerTestdaten.adaId())
        )).thenReturn(UrlaubsantragTestData.prozessinstanzId());

        prozessEngineOutboxAdapter.verarbeiteFaelligeAuftraege();

        verify(urlaubsantragProzessinstanzSpeichernOutPort).speichereProzessinstanzId(
            UrlaubsantragTestData.urlaubsantragId(),
            UrlaubsantragTestData.prozessinstanzId()
        );
        final InOrder inOrder = inOrder(outboxAuftragJpaRepository, starteGenehmigungsprozessDirektOutPort);
        inOrder.verify(outboxAuftragJpaRepository).flush();
        inOrder.verify(starteGenehmigungsprozessDirektOutPort).starteGenehmigungsprozess(
            UrlaubsantragTestData.urlaubsantragId(),
            List.of(BenutzerTestdaten.adaId())
        );
        assertThat(auftrag.getStatus()).isEqualTo(ProzessEngineOutboxAuftragStatus.ERFOLGREICH);
        assertThat(auftrag.getVersuche()).isEqualTo(1);
    }

    @Test
    void marksProcessStartAsSuccessfulWhenProcessInstanceIdAlreadyExists() {
        final var auftrag = ProzessEngineOutboxMapper.starteGenehmigungsprozess(
            UrlaubsantragTestData.urlaubsantrag(),
            List.of(BenutzerTestdaten.adaId()),
            java.time.Instant.now()
        );
        when(outboxAuftragJpaRepository.findeFaelligeAuftraege(any(), any(), anyInt(), any()))
            .thenReturn(List.of(auftrag), List.of());
        when(urlaubsantraegeLadenOutPort.findeNachId(UrlaubsantragTestData.urlaubsantragId()))
            .thenReturn(Optional.of(UrlaubsantragTestData.urlaubsantragWithStartedProcess()));

        prozessEngineOutboxAdapter.verarbeiteFaelligeAuftraege();

        verify(starteGenehmigungsprozessDirektOutPort, never()).starteGenehmigungsprozess(any(), any());
        verify(urlaubsantragProzessinstanzSpeichernOutPort, never()).speichereProzessinstanzId(any(), any());
        assertThat(auftrag.getStatus()).isEqualTo(ProzessEngineOutboxAuftragStatus.ERFOLGREICH);
    }

    @Test
    void marksUnclearProcessStartAsTerminalFailure() {
        final var auftrag = ProzessEngineOutboxMapper.starteGenehmigungsprozess(
            UrlaubsantragTestData.urlaubsantrag(),
            List.of(BenutzerTestdaten.adaId()),
            java.time.Instant.now()
        );
        when(outboxAuftragJpaRepository.findeFaelligeAuftraege(any(), any(), anyInt(), any()))
            .thenReturn(List.of(auftrag), List.of());
        when(urlaubsantraegeLadenOutPort.findeNachId(UrlaubsantragTestData.urlaubsantragId()))
            .thenReturn(Optional.of(UrlaubsantragTestData.urlaubsantrag()));
        when(starteGenehmigungsprozessDirektOutPort.starteGenehmigungsprozess(
            UrlaubsantragTestData.urlaubsantragId(),
            List.of(BenutzerTestdaten.adaId())
        )).thenThrow(new ProzessEngineAuftragUnklarException("timeout", new TimeoutException()));

        prozessEngineOutboxAdapter.verarbeiteFaelligeAuftraege();

        verify(starteGenehmigungsprozessDirektOutPort).starteGenehmigungsprozess(
            UrlaubsantragTestData.urlaubsantragId(),
            List.of(BenutzerTestdaten.adaId())
        );
        verify(urlaubsantragProzessinstanzSpeichernOutPort, never()).speichereProzessinstanzId(any(), any());
        assertThat(auftrag.getVersuche()).isEqualTo(1);
        assertThat(auftrag.getProzessinstanzId()).isNull();
        assertThat(auftrag.getStatus()).isEqualTo(ProzessEngineOutboxAuftragStatus.ENDGUELTIG_FEHLGESCHLAGEN);
        assertThat(auftrag.getLetzteFehlermeldung()).isEqualTo("timeout");
    }

    @Test
    void stopsBeforeClaimingAnotherJobWhenInterrupted() {
        final var auftrag = ProzessEngineOutboxMapper.starteGenehmigungsprozess(
            UrlaubsantragTestData.urlaubsantrag(),
            List.of(BenutzerTestdaten.adaId()),
            java.time.Instant.now()
        );
        when(outboxAuftragJpaRepository.findeFaelligeAuftraege(any(), any(), anyInt(), any()))
            .thenReturn(List.of(auftrag));
        when(urlaubsantraegeLadenOutPort.findeNachId(UrlaubsantragTestData.urlaubsantragId()))
            .thenReturn(Optional.of(UrlaubsantragTestData.urlaubsantrag()));
        when(starteGenehmigungsprozessDirektOutPort.starteGenehmigungsprozess(any(), any()))
            .thenAnswer(invocation -> {
                Thread.currentThread().interrupt();
                throw new ProzessEngineAuftragUnklarException("interrupted");
            });

        try {
            prozessEngineOutboxAdapter.verarbeiteFaelligeAuftraege();

            verify(outboxAuftragJpaRepository, times(1)).findeFaelligeAuftraege(any(), any(), anyInt(), any());
            verify(starteGenehmigungsprozessDirektOutPort, times(1)).starteGenehmigungsprozess(any(), any());
            assertThat(auftrag.getStatus()).isEqualTo(ProzessEngineOutboxAuftragStatus.ENDGUELTIG_FEHLGESCHLAGEN);
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void marksConflictingProcessInstanceIdAsTerminalFailure() {
        final var auftrag = ProzessEngineOutboxMapper.starteGenehmigungsprozess(
            UrlaubsantragTestData.urlaubsantrag(),
            List.of(BenutzerTestdaten.adaId()),
            java.time.Instant.now()
        );
        auftrag.setProzessinstanzId(UrlaubsantragTestData.PROCESS_INSTANCE_ID_VALUE);
        final var urlaubsantragMitAbweichenderProzessinstanz = UrlaubsantragTestData.urlaubsantrag(
            UrlaubsantragTestData.urlaubsantragId(),
            UrlaubsantragTestData.vacationPeriod(),
            UrlaubsantragTestData.antragsteller(),
            UrlaubsantragTestData.vertretung(),
            UrlaubsantragTestData.secondProzessinstanzId()
        );
        when(outboxAuftragJpaRepository.findeFaelligeAuftraege(any(), any(), anyInt(), any()))
            .thenReturn(List.of(auftrag), List.of());
        when(urlaubsantraegeLadenOutPort.findeNachId(UrlaubsantragTestData.urlaubsantragId()))
            .thenReturn(Optional.of(urlaubsantragMitAbweichenderProzessinstanz));

        prozessEngineOutboxAdapter.verarbeiteFaelligeAuftraege();

        verify(starteGenehmigungsprozessDirektOutPort, never()).starteGenehmigungsprozess(any(), any());
        assertThat(auftrag.getStatus()).isEqualTo(ProzessEngineOutboxAuftragStatus.ENDGUELTIG_FEHLGESCHLAGEN);
        assertThat(auftrag.getLetzteFehlermeldung()).contains("statt auf die gestartete Prozessinstanz");
    }

    @Test
    void retriesOnlyProcessInstanceWriteBackAfterSuccessfulProcessStart() {
        final var auftrag = ProzessEngineOutboxMapper.starteGenehmigungsprozess(
            UrlaubsantragTestData.urlaubsantrag(),
            List.of(BenutzerTestdaten.adaId()),
            java.time.Instant.now()
        );
        when(outboxAuftragJpaRepository.findeFaelligeAuftraege(any(), any(), anyInt(), any()))
            .thenReturn(List.of(auftrag), List.of(), List.of(auftrag), List.of());
        when(urlaubsantraegeLadenOutPort.findeNachId(UrlaubsantragTestData.urlaubsantragId()))
            .thenReturn(Optional.of(UrlaubsantragTestData.urlaubsantrag()))
            .thenReturn(Optional.of(UrlaubsantragTestData.urlaubsantrag()))
            .thenReturn(Optional.of(UrlaubsantragTestData.urlaubsantrag()))
            .thenReturn(Optional.of(UrlaubsantragTestData.urlaubsantrag()));
        when(starteGenehmigungsprozessDirektOutPort.starteGenehmigungsprozess(
            UrlaubsantragTestData.urlaubsantragId(),
            List.of(BenutzerTestdaten.adaId())
        )).thenReturn(UrlaubsantragTestData.prozessinstanzId());
        doThrow(new IllegalStateException("db down"))
            .doNothing()
            .when(urlaubsantragProzessinstanzSpeichernOutPort)
            .speichereProzessinstanzId(UrlaubsantragTestData.urlaubsantragId(), UrlaubsantragTestData.prozessinstanzId());

        prozessEngineOutboxAdapter.verarbeiteFaelligeAuftraege();
        prozessEngineOutboxAdapter.verarbeiteFaelligeAuftraege();

        verify(starteGenehmigungsprozessDirektOutPort, times(1)).starteGenehmigungsprozess(any(), any());
        verify(urlaubsantragProzessinstanzSpeichernOutPort, times(2)).speichereProzessinstanzId(
            UrlaubsantragTestData.urlaubsantragId(),
            UrlaubsantragTestData.prozessinstanzId()
        );
        assertThat(auftrag.getProzessinstanzId()).isEqualTo(UrlaubsantragTestData.PROCESS_INSTANCE_ID_VALUE);
        assertThat(auftrag.getStatus()).isEqualTo(ProzessEngineOutboxAuftragStatus.ERFOLGREICH);
    }

    @Test
    void marksFailedTaskAssignmentForRetry() {
        final var auftrag = ProzessEngineOutboxMapper.weiseTaskZu(
            UserTaskTestdaten.taskId(),
            BenutzerTestdaten.adaId(),
            java.time.Instant.now()
        );
        when(outboxAuftragJpaRepository.findeFaelligeAuftraege(any(), any(), anyInt(), any()))
            .thenReturn(List.of(auftrag), List.of());
        doThrow(new IllegalStateException("boom"))
            .when(taskBearbeitenDirektOutPort)
            .assignTaskToUser(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId());

        prozessEngineOutboxAdapter.verarbeiteFaelligeAuftraege();

        assertThat(auftrag.getStatus()).isEqualTo(ProzessEngineOutboxAuftragStatus.FEHLGESCHLAGEN);
        assertThat(auftrag.getVersuche()).isEqualTo(1);
        assertThat(auftrag.getLetzteFehlermeldung()).isEqualTo("boom");
        assertThat(auftrag.getNaechsterVersuchAm()).isAfter(auftrag.getZuletztGeaendertAm());
    }

    @Test
    void marksUnclearEngineStateAsTerminalFailure() {
        final var auftrag = ProzessEngineOutboxMapper.schliesseTaskAb(
            UserTaskTestdaten.taskId(),
            BenutzerTestdaten.adaId(),
            true,
            java.time.Instant.now()
        );
        when(outboxAuftragJpaRepository.findeFaelligeAuftraege(any(), any(), anyInt(), any()))
            .thenReturn(List.of(auftrag), List.of());
        doThrow(new ProzessEngineAuftragUnklarException("Auftrag konnte nicht eindeutig abgeschlossen werden", new TimeoutException()))
            .when(taskBearbeitenDirektOutPort)
            .completeTask(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId(), true);

        prozessEngineOutboxAdapter.verarbeiteFaelligeAuftraege();

        assertThat(auftrag.getStatus()).isEqualTo(ProzessEngineOutboxAuftragStatus.ENDGUELTIG_FEHLGESCHLAGEN);
        assertThat(auftrag.getVersuche()).isEqualTo(1);
        assertThat(auftrag.getLetzteFehlermeldung()).isEqualTo("Auftrag konnte nicht eindeutig abgeschlossen werden");
    }
}
