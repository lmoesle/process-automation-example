package de.lmoesle.processautomationexample.application.usecases;

import de.lmoesle.processautomationexample.application.ports.in.UrlaubsantragAutomatischPruefenInPort.UrlaubsantragAutomatischPruefenCommand;
import de.lmoesle.processautomationexample.application.ports.out.UrlaubsantraegeLadenOutPort;
import de.lmoesle.processautomationexample.application.ports.out.UrlaubsantragSpeichernOutPort;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragStatus;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UrlaubsantragAutomatischPruefenUseCaseTest {

    private UrlaubsantraegeLadenOutPort urlaubsantraegeLadenOutPort;
    private UrlaubsantragSpeichernOutPort urlaubsantragSpeichernOutPort;
    private UrlaubsantragAutomatischPruefenUseCase pruefeUrlaubsantragAutomatischUseCase;

    @BeforeEach
    void setUp() {
        urlaubsantraegeLadenOutPort = mock(UrlaubsantraegeLadenOutPort.class);
        urlaubsantragSpeichernOutPort = mock(UrlaubsantragSpeichernOutPort.class);
        pruefeUrlaubsantragAutomatischUseCase = new UrlaubsantragAutomatischPruefenUseCase(
            urlaubsantraegeLadenOutPort,
            urlaubsantragSpeichernOutPort
        );
    }

    @Test
    void returnsTrueWhenNoSubstituteUserExists() {
        Urlaubsantrag urlaubsantragWithoutSubstitute = Urlaubsantrag.stelle(
            UrlaubsantragTestData.FROM,
            UrlaubsantragTestData.TO,
            UrlaubsantragTestData.antragsteller(),
            null
        );
        when(urlaubsantraegeLadenOutPort.findeNachId(urlaubsantragWithoutSubstitute.id()))
            .thenReturn(Optional.of(urlaubsantragWithoutSubstitute));

        boolean result = pruefeUrlaubsantragAutomatischUseCase.pruefeUrlaubsantragAutomatisch(
            new UrlaubsantragAutomatischPruefenCommand(urlaubsantragWithoutSubstitute.id())
        );

        assertThat(result).isTrue();
        verify(urlaubsantraegeLadenOutPort).findeNachId(urlaubsantragWithoutSubstitute.id());
        ArgumentCaptor<Urlaubsantrag> savedCaptor = ArgumentCaptor.forClass(Urlaubsantrag.class);
        verify(urlaubsantragSpeichernOutPort).speichere(savedCaptor.capture());
        assertThat(savedCaptor.getValue().status()).isEqualTo(UrlaubsantragStatus.VORGESETZTEN_PRUEFUNG);
        assertThat(savedCaptor.getValue().statusHistorie()).hasSize(3)
            .extracting(entry -> entry.status())
            .containsExactly(
                UrlaubsantragStatus.ANTRAG_GESTELLT,
                UrlaubsantragStatus.AUTOMATISCHE_PRUEFUNG,
                UrlaubsantragStatus.VORGESETZTEN_PRUEFUNG
            );
        verifyNoMoreInteractions(urlaubsantraegeLadenOutPort);
        verifyNoMoreInteractions(urlaubsantragSpeichernOutPort);
    }

    @Test
    void returnsTrueWhenSubstituteUserHasNoOverlappingUrlaubsantrag() {
        when(urlaubsantraegeLadenOutPort.findeNachId(UrlaubsantragTestData.urlaubsantragId()))
            .thenReturn(Optional.of(UrlaubsantragTestData.urlaubsantrag()));
        when(urlaubsantraegeLadenOutPort.findeAlleNachAntragstellerId(UrlaubsantragTestData.vertretungId()))
            .thenReturn(List.of(UrlaubsantragTestData.secondUrlaubsantrag(UrlaubsantragTestData.vertretung(), null)));

        boolean result = pruefeUrlaubsantragAutomatischUseCase.pruefeUrlaubsantragAutomatisch(
            new UrlaubsantragAutomatischPruefenCommand(UrlaubsantragTestData.urlaubsantragId())
        );

        assertThat(result).isTrue();
        verify(urlaubsantraegeLadenOutPort).findeNachId(UrlaubsantragTestData.urlaubsantragId());
        verify(urlaubsantraegeLadenOutPort).findeAlleNachAntragstellerId(UrlaubsantragTestData.vertretungId());
        ArgumentCaptor<Urlaubsantrag> savedCaptor = ArgumentCaptor.forClass(Urlaubsantrag.class);
        verify(urlaubsantragSpeichernOutPort).speichere(savedCaptor.capture());
        assertThat(savedCaptor.getValue().status()).isEqualTo(UrlaubsantragStatus.VORGESETZTEN_PRUEFUNG);
        verifyNoMoreInteractions(urlaubsantraegeLadenOutPort);
        verifyNoMoreInteractions(urlaubsantragSpeichernOutPort);
    }

    @Test
    void returnsFalseWhenSubstituteUserHasOverlappingUrlaubsantrag() {
        Urlaubsantrag overlappingUrlaubsantrag = UrlaubsantragTestData.urlaubsantrag(
            UrlaubsantragTestData.secondUrlaubsantragId(),
            UrlaubsantragTestData.vacationPeriod(),
            UrlaubsantragTestData.vertretung(),
            null,
            UrlaubsantragTestData.secondProzessinstanzId()
        );
        when(urlaubsantraegeLadenOutPort.findeNachId(UrlaubsantragTestData.urlaubsantragId()))
            .thenReturn(Optional.of(UrlaubsantragTestData.urlaubsantrag()));
        when(urlaubsantraegeLadenOutPort.findeAlleNachAntragstellerId(UrlaubsantragTestData.vertretungId()))
            .thenReturn(List.of(overlappingUrlaubsantrag));

        boolean result = pruefeUrlaubsantragAutomatischUseCase.pruefeUrlaubsantragAutomatisch(
            new UrlaubsantragAutomatischPruefenCommand(UrlaubsantragTestData.urlaubsantragId())
        );

        assertThat(result).isFalse();
        verify(urlaubsantraegeLadenOutPort).findeNachId(UrlaubsantragTestData.urlaubsantragId());
        verify(urlaubsantraegeLadenOutPort).findeAlleNachAntragstellerId(UrlaubsantragTestData.vertretungId());
        ArgumentCaptor<Urlaubsantrag> savedCaptor = ArgumentCaptor.forClass(Urlaubsantrag.class);
        verify(urlaubsantragSpeichernOutPort).speichere(savedCaptor.capture());
        assertThat(savedCaptor.getValue().status()).isEqualTo(UrlaubsantragStatus.ABGELEHNT);
        verifyNoMoreInteractions(urlaubsantraegeLadenOutPort);
        verifyNoMoreInteractions(urlaubsantragSpeichernOutPort);
    }

    @Test
    void returnsTrueWithoutSavingWhenAutomaticCheckWasAlreadyCompletedSuccessfully() {
        Urlaubsantrag bereitsGepruefterUrlaubsantrag = new Urlaubsantrag(
            UrlaubsantragTestData.urlaubsantragId(),
            UrlaubsantragTestData.vacationPeriod(),
            UrlaubsantragTestData.antragsteller(),
            UrlaubsantragTestData.vertretung(),
            null,
            UrlaubsantragStatus.VORGESETZTEN_PRUEFUNG,
            UrlaubsantragTestData.statushistorie(
                UrlaubsantragStatus.ANTRAG_GESTELLT,
                UrlaubsantragStatus.AUTOMATISCHE_PRUEFUNG,
                UrlaubsantragStatus.VORGESETZTEN_PRUEFUNG
            ),
            UrlaubsantragTestData.prozessinstanzId()
        );
        when(urlaubsantraegeLadenOutPort.findeNachId(UrlaubsantragTestData.urlaubsantragId()))
            .thenReturn(Optional.of(bereitsGepruefterUrlaubsantrag));

        boolean result = pruefeUrlaubsantragAutomatischUseCase.pruefeUrlaubsantragAutomatisch(
            new UrlaubsantragAutomatischPruefenCommand(UrlaubsantragTestData.urlaubsantragId())
        );

        assertThat(result).isTrue();
        verify(urlaubsantraegeLadenOutPort).findeNachId(UrlaubsantragTestData.urlaubsantragId());
        verifyNoMoreInteractions(urlaubsantraegeLadenOutPort);
        verifyNoInteractions(urlaubsantragSpeichernOutPort);
    }

    @Test
    void returnsFalseWithoutSavingWhenAutomaticCheckWasAlreadyCompletedWithRejection() {
        Urlaubsantrag bereitsAbgelehnterUrlaubsantrag = new Urlaubsantrag(
            UrlaubsantragTestData.urlaubsantragId(),
            UrlaubsantragTestData.vacationPeriod(),
            UrlaubsantragTestData.antragsteller(),
            UrlaubsantragTestData.vertretung(),
            null,
            UrlaubsantragStatus.ABGELEHNT,
            UrlaubsantragTestData.statushistorie(
                UrlaubsantragStatus.ANTRAG_GESTELLT,
                UrlaubsantragStatus.AUTOMATISCHE_PRUEFUNG,
                UrlaubsantragStatus.ABGELEHNT
            ),
            UrlaubsantragTestData.prozessinstanzId()
        );
        when(urlaubsantraegeLadenOutPort.findeNachId(UrlaubsantragTestData.urlaubsantragId()))
            .thenReturn(Optional.of(bereitsAbgelehnterUrlaubsantrag));

        boolean result = pruefeUrlaubsantragAutomatischUseCase.pruefeUrlaubsantragAutomatisch(
            new UrlaubsantragAutomatischPruefenCommand(UrlaubsantragTestData.urlaubsantragId())
        );

        assertThat(result).isFalse();
        verify(urlaubsantraegeLadenOutPort).findeNachId(UrlaubsantragTestData.urlaubsantragId());
        verifyNoMoreInteractions(urlaubsantraegeLadenOutPort);
        verifyNoInteractions(urlaubsantragSpeichernOutPort);
    }

    @Test
    void rejectsNullCommand() {
        assertThatThrownBy(() -> pruefeUrlaubsantragAutomatischUseCase.pruefeUrlaubsantragAutomatisch(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("command darf nicht null sein");
    }

    @Test
    void rejectsNullUrlaubsantragId() {
        assertThatThrownBy(() -> pruefeUrlaubsantragAutomatischUseCase.pruefeUrlaubsantragAutomatisch(
            new UrlaubsantragAutomatischPruefenCommand(null)
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("urlaubsantragId darf nicht null sein");
    }

    @Test
    void rejectsMissingUrlaubsantrag() {
        when(urlaubsantraegeLadenOutPort.findeNachId(UrlaubsantragTestData.urlaubsantragId()))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> pruefeUrlaubsantragAutomatischUseCase.pruefeUrlaubsantragAutomatisch(
            new UrlaubsantragAutomatischPruefenCommand(UrlaubsantragTestData.urlaubsantragId())
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("urlaubsantragId verweist auf keinen vorhandenen Urlaubsantrag");
    }
}
