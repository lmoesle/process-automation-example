package de.lmoesle.miravelo.processautomationexample.application.usecases;

import de.lmoesle.miravelo.processautomationexample.application.ports.in.UrlaubsantragErstellenInPort.UrlaubsantragErstellenCommand;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.UrlaubsantraegeLadenOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.UrlaubsantragSpeichernOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.UrlaubsantragGenehmigungsprozessStartenOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.BenutzerRepositoryOutPort;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.ProzessinstanzId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragStatus;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class UrlaubsantragErstellenUseCaseTest {

    private BenutzerRepositoryOutPort benutzerRepositoryOutPort;
    private UrlaubsantragSpeichernOutPort urlaubsantragSpeichernOutPort;
    private UrlaubsantraegeLadenOutPort urlaubsantraegeLadenOutPort;
    private UrlaubsantragGenehmigungsprozessStartenOutPort genehmigungsprozessStartenOutPort;
    private UrlaubsantragErstellenUseCase erstelleUrlaubsantragUseCase;

    @BeforeEach
    void setUp() {
        benutzerRepositoryOutPort = mock(BenutzerRepositoryOutPort.class);
        urlaubsantragSpeichernOutPort = mock(UrlaubsantragSpeichernOutPort.class);
        urlaubsantraegeLadenOutPort = mock(UrlaubsantraegeLadenOutPort.class);
        genehmigungsprozessStartenOutPort = mock(UrlaubsantragGenehmigungsprozessStartenOutPort.class);
        erstelleUrlaubsantragUseCase = new UrlaubsantragErstellenUseCase(
            benutzerRepositoryOutPort,
            urlaubsantragSpeichernOutPort,
            urlaubsantraegeLadenOutPort,
            genehmigungsprozessStartenOutPort
        );
    }

    @Test
    void savesRequestStartsProcessAndPersistsProzessinstanzId() {
        final Benutzer antragstellerMitTeams = Benutzer.rekonstituiere(
            UrlaubsantragTestData.antragstellerId(),
            "Applicant Benutzer",
            "applicant.user@example.com",
            List.of(BenutzerTestdaten.engineeringLeadTeam(), BenutzerTestdaten.platformUserTeam())
        );
        final AtomicInteger saveInvocationCounter = new AtomicInteger();
        final AtomicReference<Urlaubsantrag> initiallySaved = new AtomicReference<>();
        doAnswer(invocation -> {
            final Urlaubsantrag urlaubsantrag = invocation.getArgument(0);
            final int currentInvocation = saveInvocationCounter.incrementAndGet();

            if (currentInvocation == 1) {
                assertThat(urlaubsantrag.prozessinstanzId()).isNull();
                initiallySaved.set(urlaubsantrag);
            }

            assertThat(urlaubsantrag.antragsteller()).isEqualTo(antragstellerMitTeams);
            assertThat(urlaubsantrag.vertretung()).isEqualTo(UrlaubsantragTestData.vertretung());

            if (currentInvocation == 2) {
                assertThat(urlaubsantrag.prozessinstanzId()).isEqualTo(UrlaubsantragTestData.prozessinstanzId());
            }

            return urlaubsantrag;
        }).when(urlaubsantragSpeichernOutPort).speichere(any(Urlaubsantrag.class));
        when(urlaubsantraegeLadenOutPort.findeNachId(any()))
            .thenAnswer(invocation -> Optional.of(initiallySaved.get()));
        when(benutzerRepositoryOutPort.findeNachId(UrlaubsantragTestData.antragstellerId()))
            .thenReturn(Optional.of(antragstellerMitTeams));
        when(benutzerRepositoryOutPort.findeNachId(UrlaubsantragTestData.vertretungId()))
            .thenReturn(Optional.of(UrlaubsantragTestData.vertretung()));
        when(benutzerRepositoryOutPort.findeAlleLeitendenNachTeamId(BenutzerTestdaten.engineeringTeamId()))
            .thenReturn(List.of(BenutzerTestdaten.ada()));
        when(benutzerRepositoryOutPort.findeAlleLeitendenNachTeamId(BenutzerTestdaten.platformTeamId()))
            .thenReturn(List.of(BenutzerTestdaten.carla()));
        when(genehmigungsprozessStartenOutPort.starteGenehmigungsprozessFuer(any(Urlaubsantrag.class), any()))
            .thenAnswer(invocation -> {
                final Urlaubsantrag urlaubsantrag = invocation.getArgument(0);
                assertThat(urlaubsantrag.prozessinstanzId()).isNull();
                assertThat(invocation.<List<de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId>>getArgument(1))
                    .containsExactly(BenutzerTestdaten.adaId(), BenutzerTestdaten.carlaId());
                return UrlaubsantragTestData.prozessinstanzId();
            });

        final var result = erstelleUrlaubsantragUseCase.erstelleUrlaubsantrag(
            new UrlaubsantragErstellenCommand(
                UrlaubsantragTestData.FROM,
                UrlaubsantragTestData.TO,
                UrlaubsantragTestData.antragstellerId(),
                UrlaubsantragTestData.vertretungId()
            )
        );

        final InOrder inOrder = inOrder(
            benutzerRepositoryOutPort,
            urlaubsantragSpeichernOutPort,
            urlaubsantraegeLadenOutPort,
            genehmigungsprozessStartenOutPort
        );
        inOrder.verify(benutzerRepositoryOutPort).findeNachId(UrlaubsantragTestData.antragstellerId());
        inOrder.verify(benutzerRepositoryOutPort).findeNachId(UrlaubsantragTestData.vertretungId());
        inOrder.verify(urlaubsantragSpeichernOutPort).speichere(any(Urlaubsantrag.class));
        inOrder.verify(benutzerRepositoryOutPort).findeAlleLeitendenNachTeamId(BenutzerTestdaten.engineeringTeamId());
        inOrder.verify(benutzerRepositoryOutPort).findeAlleLeitendenNachTeamId(BenutzerTestdaten.platformTeamId());
        inOrder.verify(genehmigungsprozessStartenOutPort).starteGenehmigungsprozessFuer(
            any(Urlaubsantrag.class),
            eq(List.of(BenutzerTestdaten.adaId(), BenutzerTestdaten.carlaId()))
        );
        inOrder.verify(urlaubsantraegeLadenOutPort).findeNachId(any());
        inOrder.verify(urlaubsantragSpeichernOutPort).speichere(any(Urlaubsantrag.class));
        verifyNoMoreInteractions(
            benutzerRepositoryOutPort,
            urlaubsantragSpeichernOutPort,
            urlaubsantraegeLadenOutPort,
            genehmigungsprozessStartenOutPort
        );

        assertThat(result.urlaubsantragId()).isNotNull();
        assertThat(result.prozessinstanzId()).isEqualTo(UrlaubsantragTestData.prozessinstanzId());
        assertThat(result.antragsteller()).isEqualTo(antragstellerMitTeams);
        assertThat(result.vertretung()).isEqualTo(UrlaubsantragTestData.vertretung());
        assertThat(result.status()).isEqualTo(UrlaubsantragStatus.ANTRAG_GESTELLT);
        assertThat(result.statushistorie()).hasSize(1)
            .first()
            .satisfies(entry -> {
                assertThat(entry.status()).isEqualTo(UrlaubsantragStatus.ANTRAG_GESTELLT);
                assertThat(entry.kommentar()).isNull();
            });
        assertThat(saveInvocationCounter.get()).isEqualTo(2);
    }

    @Test
    void preservesSynchronousProcessUpdatesWhenPersistingProzessinstanzId() {
        final AtomicReference<Urlaubsantrag> persisted = new AtomicReference<>();
        final UrlaubsantragSpeichernOutPort speichernOutPort = urlaubsantrag -> {
            persisted.set(copyOf(urlaubsantrag));
            return urlaubsantrag;
        };
        final UrlaubsantragGenehmigungsprozessStartenOutPort processOutPort = (urlaubsantrag, teamLeadIds) -> {
            final Urlaubsantrag processed = copyOf(persisted.get());
            processed.starteAutomatischePruefung();
            processed.schliesseAutomatischePruefungAb(true);
            persisted.set(processed);
            return UrlaubsantragTestData.prozessinstanzId();
        };
        final var useCase = new UrlaubsantragErstellenUseCase(
            benutzerRepositoryOutPort,
            speichernOutPort,
            urlaubsantraegeLadenOutPort,
            processOutPort
        );
        when(benutzerRepositoryOutPort.findeNachId(UrlaubsantragTestData.antragstellerId()))
            .thenReturn(Optional.of(UrlaubsantragTestData.antragsteller()));
        when(urlaubsantraegeLadenOutPort.findeNachId(any()))
            .thenAnswer(invocation -> Optional.of(copyOf(persisted.get())));

        final var result = useCase.erstelleUrlaubsantrag(new UrlaubsantragErstellenCommand(
            UrlaubsantragTestData.FROM,
            UrlaubsantragTestData.TO,
            UrlaubsantragTestData.antragstellerId(),
            null
        ));

        assertThat(persisted.get().status()).isEqualTo(UrlaubsantragStatus.VORGESETZTEN_PRUEFUNG);
        assertThat(result.status()).isEqualTo(UrlaubsantragStatus.VORGESETZTEN_PRUEFUNG);
    }

    @Test
    void propagatesIllegalArgumentExceptionFromDomainValidation() {
        when(benutzerRepositoryOutPort.findeNachId(UrlaubsantragTestData.antragstellerId()))
            .thenReturn(Optional.of(UrlaubsantragTestData.antragsteller()));
        when(benutzerRepositoryOutPort.findeNachId(UrlaubsantragTestData.vertretungId()))
            .thenReturn(Optional.of(UrlaubsantragTestData.vertretung()));

        assertThatThrownBy(() -> erstelleUrlaubsantragUseCase.erstelleUrlaubsantrag(
            new UrlaubsantragErstellenCommand(
                UrlaubsantragTestData.TO,
                UrlaubsantragTestData.FROM,
                UrlaubsantragTestData.antragstellerId(),
                UrlaubsantragTestData.vertretungId()
            )
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("'von' muss vor oder gleich 'bis' liegen.");
    }

    @Test
    void propagatesStartedProzessinstanzId() {
        final AtomicReference<Urlaubsantrag> initiallySaved = new AtomicReference<>();
        when(benutzerRepositoryOutPort.findeNachId(UrlaubsantragTestData.antragstellerId()))
            .thenReturn(Optional.of(UrlaubsantragTestData.antragsteller()));
        when(urlaubsantragSpeichernOutPort.speichere(any(Urlaubsantrag.class)))
            .thenAnswer(invocation -> {
                final Urlaubsantrag urlaubsantrag = invocation.getArgument(0);
                initiallySaved.compareAndSet(null, urlaubsantrag);
                return urlaubsantrag;
            });
        when(urlaubsantraegeLadenOutPort.findeNachId(any()))
            .thenAnswer(invocation -> Optional.of(initiallySaved.get()));
        when(genehmigungsprozessStartenOutPort.starteGenehmigungsprozessFuer(any(Urlaubsantrag.class), any()))
            .thenAnswer(invocation -> {
                assertThat(invocation.<List<?>>getArgument(1)).isEmpty();
                return ProzessinstanzId.of("process-instance-9000");
            });

        final var result = erstelleUrlaubsantragUseCase.erstelleUrlaubsantrag(
            new UrlaubsantragErstellenCommand(
                UrlaubsantragTestData.FROM,
                UrlaubsantragTestData.TO,
                UrlaubsantragTestData.antragstellerId(),
                null
            )
        );

        assertThat(result.prozessinstanzId()).isEqualTo(ProzessinstanzId.of("process-instance-9000"));
        assertThat(result.antragsteller()).isEqualTo(UrlaubsantragTestData.antragsteller());
        assertThat(result.vertretung()).isNull();
    }

    @Test
    void excludesApplicantFromTeamLeadCandidates() {
        final AtomicReference<Urlaubsantrag> initiallySaved = new AtomicReference<>();
        when(benutzerRepositoryOutPort.findeNachId(BenutzerTestdaten.adaId()))
            .thenReturn(Optional.of(BenutzerTestdaten.ada()));
        when(benutzerRepositoryOutPort.findeAlleLeitendenNachTeamId(BenutzerTestdaten.engineeringTeamId()))
            .thenReturn(List.of(BenutzerTestdaten.ada()));
        when(benutzerRepositoryOutPort.findeAlleLeitendenNachTeamId(BenutzerTestdaten.platformTeamId()))
            .thenReturn(List.of(BenutzerTestdaten.carla()));
        when(urlaubsantragSpeichernOutPort.speichere(any(Urlaubsantrag.class)))
            .thenAnswer(invocation -> {
                final Urlaubsantrag urlaubsantrag = invocation.getArgument(0);
                initiallySaved.compareAndSet(null, urlaubsantrag);
                return urlaubsantrag;
            });
        when(urlaubsantraegeLadenOutPort.findeNachId(any()))
            .thenAnswer(invocation -> Optional.of(initiallySaved.get()));
        when(genehmigungsprozessStartenOutPort.starteGenehmigungsprozessFuer(any(Urlaubsantrag.class), any()))
            .thenAnswer(invocation -> {
                assertThat(invocation.<List<de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId>>getArgument(1))
                    .containsExactly(BenutzerTestdaten.carlaId());
                return UrlaubsantragTestData.prozessinstanzId();
            });

        erstelleUrlaubsantragUseCase.erstelleUrlaubsantrag(new UrlaubsantragErstellenCommand(
            UrlaubsantragTestData.FROM,
            UrlaubsantragTestData.TO,
            BenutzerTestdaten.adaId(),
            null
        ));
    }

    @Test
    void rejectsMissingApplicantUser() {
        when(benutzerRepositoryOutPort.findeNachId(UrlaubsantragTestData.antragstellerId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> erstelleUrlaubsantragUseCase.erstelleUrlaubsantrag(
            new UrlaubsantragErstellenCommand(
                UrlaubsantragTestData.FROM,
                UrlaubsantragTestData.TO,
                UrlaubsantragTestData.antragstellerId(),
                null
            )
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("antragstellerId verweist auf keinen vorhandenen Benutzer");

        inOrder(benutzerRepositoryOutPort)
            .verify(benutzerRepositoryOutPort)
            .findeNachId(UrlaubsantragTestData.antragstellerId());
        verifyNoInteractions(urlaubsantragSpeichernOutPort, genehmigungsprozessStartenOutPort);
        verifyNoMoreInteractions(benutzerRepositoryOutPort);
    }

    @Test
    void rejectsMissingSubstituteUser() {
        when(benutzerRepositoryOutPort.findeNachId(UrlaubsantragTestData.antragstellerId()))
            .thenReturn(Optional.of(UrlaubsantragTestData.antragsteller()));
        when(benutzerRepositoryOutPort.findeNachId(UrlaubsantragTestData.vertretungId()))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> erstelleUrlaubsantragUseCase.erstelleUrlaubsantrag(
            new UrlaubsantragErstellenCommand(
                UrlaubsantragTestData.FROM,
                UrlaubsantragTestData.TO,
                UrlaubsantragTestData.antragstellerId(),
                UrlaubsantragTestData.vertretungId()
            )
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("vertretungId verweist auf keinen vorhandenen Benutzer");

        final InOrder inOrder = inOrder(benutzerRepositoryOutPort);
        inOrder.verify(benutzerRepositoryOutPort).findeNachId(UrlaubsantragTestData.antragstellerId());
        inOrder.verify(benutzerRepositoryOutPort).findeNachId(UrlaubsantragTestData.vertretungId());
        verifyNoInteractions(urlaubsantragSpeichernOutPort, genehmigungsprozessStartenOutPort);
        verifyNoMoreInteractions(benutzerRepositoryOutPort);
    }

    @Test
    void rejectsApplicantAsSubstituteUser() {
        when(benutzerRepositoryOutPort.findeNachId(UrlaubsantragTestData.antragstellerId()))
            .thenReturn(Optional.of(UrlaubsantragTestData.antragsteller()));

        assertThatThrownBy(() -> erstelleUrlaubsantragUseCase.erstelleUrlaubsantrag(
            new UrlaubsantragErstellenCommand(
                UrlaubsantragTestData.FROM,
                UrlaubsantragTestData.TO,
                UrlaubsantragTestData.antragstellerId(),
                UrlaubsantragTestData.antragstellerId()
            )
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("vertretung darf nicht antragsteller sein");

        verify(benutzerRepositoryOutPort, times(2)).findeNachId(UrlaubsantragTestData.antragstellerId());
        verifyNoInteractions(urlaubsantragSpeichernOutPort, genehmigungsprozessStartenOutPort);
        verifyNoMoreInteractions(benutzerRepositoryOutPort);
    }

    private static Urlaubsantrag copyOf(Urlaubsantrag urlaubsantrag) {
        return new Urlaubsantrag(
            urlaubsantrag.id(),
            urlaubsantrag.zeitraum(),
            urlaubsantrag.antragsteller(),
            urlaubsantrag.vertretung(),
            urlaubsantrag.vorgesetzter(),
            urlaubsantrag.status(),
            urlaubsantrag.statusHistorie(),
            urlaubsantrag.prozessinstanzId()
        );
    }
}
