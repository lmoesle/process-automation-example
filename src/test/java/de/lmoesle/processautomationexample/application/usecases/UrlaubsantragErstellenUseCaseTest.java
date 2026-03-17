package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.UrlaubsantragErstellenInPort.UrlaubsantragErstellenCommand;
import de.lmoesle.processautomationexample.application.ports.out.UrlaubsantragSpeichernOutPort;
import de.lmoesle.processautomationexample.application.ports.out.UrlaubsantragGenehmigungsprozessStartenOutPort;
import de.lmoesle.processautomationexample.application.ports.out.BenutzerRepositoryOutPort;
import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.ProzessinstanzId;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragStatus;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class UrlaubsantragErstellenUseCaseTest {

    private BenutzerRepositoryOutPort benutzerRepositoryOutPort;
    private UrlaubsantragSpeichernOutPort urlaubsantragSpeichernOutPort;
    private UrlaubsantragGenehmigungsprozessStartenOutPort genehmigungsprozessStartenOutPort;
    private UrlaubsantragErstellenUseCase erstelleUrlaubsantragUseCase;

    @BeforeEach
    void setUp() {
        benutzerRepositoryOutPort = mock(BenutzerRepositoryOutPort.class);
        urlaubsantragSpeichernOutPort = mock(UrlaubsantragSpeichernOutPort.class);
        genehmigungsprozessStartenOutPort = mock(UrlaubsantragGenehmigungsprozessStartenOutPort.class);
        erstelleUrlaubsantragUseCase = new UrlaubsantragErstellenUseCase(
            benutzerRepositoryOutPort,
            urlaubsantragSpeichernOutPort,
            genehmigungsprozessStartenOutPort
        );
    }

    @Test
    void savesRequestStartsProcessAndPersistsProzessinstanzId() {
        Benutzer antragstellerMitTeams = Benutzer.rekonstituiere(
            UrlaubsantragTestData.antragstellerId(),
            "Applicant Benutzer",
            "applicant.user@example.com",
            List.of(BenutzerTestdaten.engineeringLeadTeam(), BenutzerTestdaten.platformUserTeam())
        );
        AtomicInteger saveInvocationCounter = new AtomicInteger();
        doAnswer(invocation -> {
            Urlaubsantrag urlaubsantrag = invocation.getArgument(0);
            int currentInvocation = saveInvocationCounter.incrementAndGet();

            if (currentInvocation == 1) {
                assertThat(urlaubsantrag.prozessinstanzId()).isNull();
            }

            assertThat(urlaubsantrag.antragsteller()).isEqualTo(antragstellerMitTeams);
            assertThat(urlaubsantrag.vertretung()).isEqualTo(UrlaubsantragTestData.vertretung());

            if (currentInvocation == 2) {
                assertThat(urlaubsantrag.prozessinstanzId()).isEqualTo(UrlaubsantragTestData.prozessinstanzId());
            }

            return urlaubsantrag;
        }).when(urlaubsantragSpeichernOutPort).speichere(any(Urlaubsantrag.class));
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
                Urlaubsantrag urlaubsantrag = invocation.getArgument(0);
                assertThat(urlaubsantrag.prozessinstanzId()).isNull();
                assertThat(invocation.<List<de.lmoesle.processautomationexample.domain.benutzer.BenutzerId>>getArgument(1))
                    .containsExactly(BenutzerTestdaten.adaId(), BenutzerTestdaten.carlaId());
                return UrlaubsantragTestData.prozessinstanzId();
            });

        var result = erstelleUrlaubsantragUseCase.erstelleUrlaubsantrag(
            new UrlaubsantragErstellenCommand(
                UrlaubsantragTestData.FROM,
                UrlaubsantragTestData.TO,
                UrlaubsantragTestData.antragstellerId(),
                UrlaubsantragTestData.vertretungId()
            )
        );

        InOrder inOrder = inOrder(benutzerRepositoryOutPort, urlaubsantragSpeichernOutPort, genehmigungsprozessStartenOutPort);
        inOrder.verify(benutzerRepositoryOutPort).findeNachId(UrlaubsantragTestData.antragstellerId());
        inOrder.verify(benutzerRepositoryOutPort).findeNachId(UrlaubsantragTestData.vertretungId());
        inOrder.verify(urlaubsantragSpeichernOutPort).speichere(any(Urlaubsantrag.class));
        inOrder.verify(benutzerRepositoryOutPort).findeAlleLeitendenNachTeamId(BenutzerTestdaten.engineeringTeamId());
        inOrder.verify(benutzerRepositoryOutPort).findeAlleLeitendenNachTeamId(BenutzerTestdaten.platformTeamId());
        inOrder.verify(genehmigungsprozessStartenOutPort).starteGenehmigungsprozessFuer(
            any(Urlaubsantrag.class),
            eq(List.of(BenutzerTestdaten.adaId(), BenutzerTestdaten.carlaId()))
        );
        inOrder.verify(urlaubsantragSpeichernOutPort).speichere(any(Urlaubsantrag.class));
        verifyNoMoreInteractions(benutzerRepositoryOutPort, urlaubsantragSpeichernOutPort, genehmigungsprozessStartenOutPort);

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
        when(benutzerRepositoryOutPort.findeNachId(UrlaubsantragTestData.antragstellerId()))
            .thenReturn(Optional.of(UrlaubsantragTestData.antragsteller()));
        when(urlaubsantragSpeichernOutPort.speichere(any(Urlaubsantrag.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(genehmigungsprozessStartenOutPort.starteGenehmigungsprozessFuer(any(Urlaubsantrag.class), any()))
            .thenAnswer(invocation -> {
                assertThat(invocation.<List<?>>getArgument(1)).isEmpty();
                return ProzessinstanzId.of("process-instance-9000");
            });

        var result = erstelleUrlaubsantragUseCase.erstelleUrlaubsantrag(
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

        InOrder inOrder = inOrder(benutzerRepositoryOutPort);
        inOrder.verify(benutzerRepositoryOutPort).findeNachId(UrlaubsantragTestData.antragstellerId());
        inOrder.verify(benutzerRepositoryOutPort).findeNachId(UrlaubsantragTestData.vertretungId());
        verifyNoInteractions(urlaubsantragSpeichernOutPort, genehmigungsprozessStartenOutPort);
        verifyNoMoreInteractions(benutzerRepositoryOutPort);
    }
}
