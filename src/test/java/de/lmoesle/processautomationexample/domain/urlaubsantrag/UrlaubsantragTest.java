package de.lmoesle.processautomationexample.domain.urlaubsantrag;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData.PROCESS_INSTANCE_ID_VALUE;
import static de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestdaten.statusHistorie;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrlaubsantragTest {

    @Test
    void submitsUrlaubsantragWithGeneratedId() {
        Urlaubsantrag urlaubsantrag = Urlaubsantrag.stelle(
            UrlaubsantragTestData.FROM,
            UrlaubsantragTestData.TO,
            UrlaubsantragTestData.antragsteller(),
            UrlaubsantragTestData.vertretung()
        );

        assertThat(urlaubsantrag.id()).isNotNull();
        assertThat(urlaubsantrag.zeitraum().von()).isEqualTo(UrlaubsantragTestData.FROM);
        assertThat(urlaubsantrag.zeitraum().bis()).isEqualTo(UrlaubsantragTestData.TO);
        assertThat(urlaubsantrag.antragsteller()).isEqualTo(UrlaubsantragTestData.antragsteller());
        assertThat(urlaubsantrag.vertretung()).isEqualTo(UrlaubsantragTestData.vertretung());
        assertThat(urlaubsantrag.prozessinstanzId()).isNull();
        assertThat(urlaubsantrag.status()).isEqualTo(UrlaubsantragStatus.ANTRAG_GESTELLT);
        assertThat(urlaubsantrag.statusHistorie()).hasSize(1)
            .first()
            .satisfies(entry -> {
                assertThat(entry.status()).isEqualTo(UrlaubsantragStatus.ANTRAG_GESTELLT);
                assertThat(entry.kommentar()).isNull();
            });
    }

    @Test
    void reconstitutesUrlaubsantragWithExistingState() {
        List<UrlaubsantragStatusHistorieneintrag> history = statusHistorie(
            UrlaubsantragStatus.ANTRAG_GESTELLT,
            UrlaubsantragStatus.VORGESETZTEN_PRUEFUNG
        );
        Urlaubsantrag urlaubsantrag = new Urlaubsantrag(
            UrlaubsantragTestData.urlaubsantragId(),
            UrlaubsantragTestData.vacationPeriod(),
            UrlaubsantragTestData.antragsteller(),
            UrlaubsantragTestData.vertretung(),
            UrlaubsantragStatus.VORGESETZTEN_PRUEFUNG,
            history,
            UrlaubsantragTestData.prozessinstanzId()
        );

        assertThat(urlaubsantrag.id()).isEqualTo(UrlaubsantragTestData.urlaubsantragId());
        assertThat(urlaubsantrag.zeitraum().von()).isEqualTo(UrlaubsantragTestData.FROM);
        assertThat(urlaubsantrag.zeitraum().bis()).isEqualTo(UrlaubsantragTestData.TO);
        assertThat(urlaubsantrag.antragsteller()).isEqualTo(UrlaubsantragTestData.antragsteller());
        assertThat(urlaubsantrag.vertretung()).isEqualTo(UrlaubsantragTestData.vertretung());
        assertThat(urlaubsantrag.prozessinstanzId()).isEqualTo(UrlaubsantragTestData.prozessinstanzId());
        assertThat(urlaubsantrag.status()).isEqualTo(UrlaubsantragStatus.VORGESETZTEN_PRUEFUNG);
        assertThat(urlaubsantrag.statusHistorie()).isEqualTo(history);
    }

    @Test
    void rejectsUrlaubszeitraumWhenFromIsAfterTo() {
        assertThatThrownBy(() -> Urlaubsantrag.stelle(
            LocalDate.parse("2026-07-10"),
            LocalDate.parse("2026-07-01"),
            UrlaubsantragTestData.antragsteller(),
            null
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("'von' muss vor oder gleich 'bis' liegen.");
    }

    @Test
    void rejectsMissingApplicantUser() {
        assertThatThrownBy(() -> Urlaubsantrag.stelle(
            UrlaubsantragTestData.FROM,
            UrlaubsantragTestData.TO,
            null,
            UrlaubsantragTestData.vertretung()
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("antragsteller darf nicht null sein");
    }

    @Test
    void automaticCheckIsValidWhenNoSubstituteUserIsConfigured() {
        Urlaubsantrag urlaubsantrag = Urlaubsantrag.stelle(
            UrlaubsantragTestData.FROM,
            UrlaubsantragTestData.TO,
            UrlaubsantragTestData.antragsteller(),
            null
        );

        assertThat(urlaubsantrag.istAutomatischGueltigGegen(
            java.util.List.of(UrlaubsantragTestData.secondUrlaubsantrag(UrlaubsantragTestData.vertretung(), null))
        )).isTrue();
    }

    @Test
    void automaticCheckIsValidWhenSubstituteUserHasNoOverlappingUrlaubsantrag() {
        Urlaubsantrag urlaubsantrag = UrlaubsantragTestData.urlaubsantrag();

        assertThat(urlaubsantrag.istAutomatischGueltigGegen(
            java.util.List.of(UrlaubsantragTestData.secondUrlaubsantrag(UrlaubsantragTestData.vertretung(), null))
        )).isTrue();
    }

    @Test
    void automaticCheckIsInvalidWhenSubstituteUserHasOverlappingUrlaubsantrag() {
        Urlaubsantrag urlaubsantrag = UrlaubsantragTestData.urlaubsantrag();
        Urlaubsantrag overlappingUrlaubsantrag = UrlaubsantragTestData.urlaubsantrag(
            UrlaubsantragTestData.secondUrlaubsantragId(),
            UrlaubsantragTestData.vacationPeriod(),
            UrlaubsantragTestData.vertretung(),
            null,
            UrlaubsantragTestData.secondProzessinstanzId()
        );

        assertThat(urlaubsantrag.istAutomatischGueltigGegen(java.util.List.of(overlappingUrlaubsantrag))).isFalse();
    }

    @Test
    void marksApprovalProcessAsStarted() {
        Urlaubsantrag urlaubsantrag = UrlaubsantragTestData.urlaubsantrag();

        urlaubsantrag.markiereGenehmigungsprozessAlsGestartet(ProzessinstanzId.of(PROCESS_INSTANCE_ID_VALUE));

        assertThat(urlaubsantrag.prozessinstanzId()).isEqualTo(UrlaubsantragTestData.prozessinstanzId());
    }

    @Test
    void rejectsNullProzessinstanzIdWhenStartingApprovalProcess() {
        Urlaubsantrag urlaubsantrag = UrlaubsantragTestData.urlaubsantrag();

        assertThatThrownBy(() -> urlaubsantrag.markiereGenehmigungsprozessAlsGestartet(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("prozessinstanzId darf nicht null sein");
    }

    @Test
    void rejectsStartingApprovalProcessTwice() {
        Urlaubsantrag urlaubsantrag = UrlaubsantragTestData.urlaubsantragWithStartedProcess();

        assertThatThrownBy(() -> urlaubsantrag.markiereGenehmigungsprozessAlsGestartet(ProzessinstanzId.of("process-instance-99")))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Der Genehmigungsprozess wurde bereits gestartet.");
    }

    @Test
    void rejectsNullUrlaubsantragsWhenRunningAutomaticCheck() {
        Urlaubsantrag urlaubsantrag = UrlaubsantragTestData.urlaubsantrag();

        assertThatThrownBy(() -> urlaubsantrag.istAutomatischGueltigGegen(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("vertretungsUrlaubsantraege duerfen nicht null sein");
    }

    @Test
    void startsAutomaticCheckAddsStatusHistory() {
        Urlaubsantrag urlaubsantrag = UrlaubsantragTestData.urlaubsantrag();

        urlaubsantrag.starteAutomatischePruefung();

        assertThat(urlaubsantrag.status()).isEqualTo(UrlaubsantragStatus.AUTOMATISCHE_PRUEFUNG);
        assertThat(urlaubsantrag.statusHistorie()).hasSize(2)
            .last()
            .satisfies(entry -> {
                assertThat(entry.status()).isEqualTo(UrlaubsantragStatus.AUTOMATISCHE_PRUEFUNG);
                assertThat(entry.kommentar()).isNull();
            });
    }

    @Test
    void starteAutomatischePruefungIsIdempotent() {
        Urlaubsantrag urlaubsantrag = UrlaubsantragTestData.urlaubsantrag();

        urlaubsantrag.starteAutomatischePruefung();
        int firstEntryCount = urlaubsantrag.statusHistorie().size();

        urlaubsantrag.starteAutomatischePruefung();

        assertThat(urlaubsantrag.statusHistorie()).hasSize(firstEntryCount);
    }

    @Test
    void completesAutomaticCheckWithManagerReviewStatusWhenValid() {
        Urlaubsantrag urlaubsantrag = UrlaubsantragTestData.urlaubsantrag();

        urlaubsantrag.starteAutomatischePruefung();
        urlaubsantrag.schliesseAutomatischePruefungAb(true);

        assertThat(urlaubsantrag.status()).isEqualTo(UrlaubsantragStatus.VORGESETZTEN_PRUEFUNG);
        assertThat(urlaubsantrag.statusHistorie()).hasSize(3)
            .extracting(UrlaubsantragStatusHistorieneintrag::status)
            .containsExactly(
                UrlaubsantragStatus.ANTRAG_GESTELLT,
                UrlaubsantragStatus.AUTOMATISCHE_PRUEFUNG,
                UrlaubsantragStatus.VORGESETZTEN_PRUEFUNG
            );
    }

    @Test
    void completesAutomaticCheckWithRejectedStatusWhenInvalid() {
        Urlaubsantrag urlaubsantrag = UrlaubsantragTestData.urlaubsantrag();

        urlaubsantrag.starteAutomatischePruefung();
        urlaubsantrag.schliesseAutomatischePruefungAb(false);

        assertThat(urlaubsantrag.status()).isEqualTo(UrlaubsantragStatus.ABGELEHNT);
        assertThat(urlaubsantrag.statusHistorie()).hasSize(3)
            .extracting(UrlaubsantragStatusHistorieneintrag::status)
            .containsExactly(
                UrlaubsantragStatus.ANTRAG_GESTELLT,
                UrlaubsantragStatus.AUTOMATISCHE_PRUEFUNG,
                UrlaubsantragStatus.ABGELEHNT
            );
    }

    @Test
    void rejectsCompletingAutomaticCheckBeforeItWasStarted() {
        Urlaubsantrag urlaubsantrag = UrlaubsantragTestData.urlaubsantrag();

        assertThatThrownBy(() -> urlaubsantrag.schliesseAutomatischePruefungAb(true))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Die automatische Pruefung kann nur aus dem Status AUTOMATISCHE_PRUEFUNG abgeschlossen werden.");
    }

    @Test
    void approvesByManagerAddsCommentToStatusHistory() {
        Urlaubsantrag urlaubsantrag = UrlaubsantragTestData.urlaubsantrag();

        urlaubsantrag.starteAutomatischePruefung();
        urlaubsantrag.schliesseAutomatischePruefungAb(true);
        urlaubsantrag.genehmigeDurchVorgesetzten("Vertretung ist organisiert.");

        assertThat(urlaubsantrag.status()).isEqualTo(UrlaubsantragStatus.GENEHMIGT);
        assertThat(urlaubsantrag.statusHistorie()).hasSize(4)
            .last()
            .satisfies(entry -> {
                assertThat(entry.status()).isEqualTo(UrlaubsantragStatus.GENEHMIGT);
                assertThat(entry.kommentar()).isEqualTo("Vertretung ist organisiert.");
            });
    }

    @Test
    void rejectsByManagerWithoutComment() {
        Urlaubsantrag urlaubsantrag = UrlaubsantragTestData.urlaubsantrag();

        urlaubsantrag.starteAutomatischePruefung();
        urlaubsantrag.schliesseAutomatischePruefungAb(true);
        urlaubsantrag.lehneDurchVorgesetztenAb(null);

        assertThat(urlaubsantrag.status()).isEqualTo(UrlaubsantragStatus.ABGELEHNT);
        assertThat(urlaubsantrag.statusHistorie()).hasSize(4)
            .last()
            .satisfies(entry -> {
                assertThat(entry.status()).isEqualTo(UrlaubsantragStatus.ABGELEHNT);
                assertThat(entry.kommentar()).isNull();
            });
    }

    @Test
    void rejectsManagerDecisionOutsideManagerReviewState() {
        Urlaubsantrag urlaubsantrag = UrlaubsantragTestData.urlaubsantrag();

        assertThatThrownBy(() -> urlaubsantrag.genehmigeDurchVorgesetzten("ok"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Die Vorgesetztenentscheidung kann nur aus dem Status VORGESETZTEN_PRUEFUNG getroffen werden.");
    }
}
