package de.lmoesle.processautomationexample.domain.urlaubsantrag;

import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class UrlaubsantragTestdaten {

    public static final UUID VACATION_REQUEST_UUID = UUID.fromString("c7a6939b-a97b-4445-bd66-4a0f98781899");
    public static final UUID SECOND_VACATION_REQUEST_UUID = UUID.fromString("a91e8877-f17a-40d4-a9ee-1b0350f27b52");
    public static final UUID APPLICANT_USER_UUID = UUID.fromString("772699cf-4ebd-4eb1-bf18-0f6d7569d9bb");
    public static final UUID SUBSTITUTE_USER_UUID = UUID.fromString("1deec1f5-2f8d-456d-bef5-b3fb75f6f028");
    public static final String PROCESS_INSTANCE_ID_VALUE = "process-instance-42";
    public static final String SECOND_PROCESS_INSTANCE_ID_VALUE = "process-instance-84";
    public static final LocalDate FROM = LocalDate.parse("2026-07-01");
    public static final LocalDate TO = LocalDate.parse("2026-07-10");
    public static final LocalDate SECOND_FROM = LocalDate.parse("2026-08-15");
    public static final LocalDate SECOND_TO = LocalDate.parse("2026-08-22");

    private UrlaubsantragTestdaten() {
    }

    public static UrlaubsantragId urlaubsantragId() {
        return UrlaubsantragId.of(VACATION_REQUEST_UUID);
    }

    public static BenutzerId antragstellerId() {
        return BenutzerId.of(APPLICANT_USER_UUID);
    }

    public static Benutzer antragsteller() {
        return Benutzer.rekonstituiere(
            antragstellerId(),
            "Applicant Benutzer",
            "applicant.user@example.com",
            List.of()
        );
    }

    public static BenutzerId vertretungId() {
        return BenutzerId.of(SUBSTITUTE_USER_UUID);
    }

    public static Benutzer vertretung() {
        return Benutzer.rekonstituiere(
            vertretungId(),
            "Substitute Benutzer",
            "substitute.user@example.com",
            List.of()
        );
    }

    public static ProzessinstanzId prozessinstanzId() {
        return ProzessinstanzId.of(PROCESS_INSTANCE_ID_VALUE);
    }

    public static Urlaubszeitraum urlaubszeitraum() {
        return Urlaubszeitraum.of(FROM, TO);
    }

    public static Urlaubszeitraum vacationPeriod() {
        return urlaubszeitraum();
    }

    public static Urlaubsantrag urlaubsantrag() {
        return urlaubsantrag(urlaubsantragId(), urlaubszeitraum(), antragsteller(), vertretung(), null);
    }

    public static Urlaubsantrag urlaubsantragWithStartedProcess() {
        return urlaubsantrag(
            urlaubsantragId(),
            urlaubszeitraum(),
            antragsteller(),
            vertretung(),
            prozessinstanzId()
        );
    }

    public static UrlaubsantragId secondUrlaubsantragId() {
        return UrlaubsantragId.of(SECOND_VACATION_REQUEST_UUID);
    }

    public static Urlaubszeitraum secondUrlaubszeitraum() {
        return Urlaubszeitraum.of(SECOND_FROM, SECOND_TO);
    }

    public static ProzessinstanzId secondProzessinstanzId() {
        return ProzessinstanzId.of(SECOND_PROCESS_INSTANCE_ID_VALUE);
    }

    public static Urlaubsantrag secondUrlaubsantrag(Benutzer antragsteller, Benutzer vertretung) {
        return urlaubsantrag(
            secondUrlaubsantragId(),
            secondUrlaubszeitraum(),
            antragsteller,
            vertretung,
            secondProzessinstanzId()
        );
    }

    public static Urlaubsantrag urlaubsantrag(
        UrlaubsantragId urlaubsantragId,
        Urlaubszeitraum urlaubszeitraum,
        Benutzer antragsteller,
        Benutzer vertretung,
        ProzessinstanzId prozessinstanzId
    ) {
        return new Urlaubsantrag(
            urlaubsantragId,
            urlaubszeitraum,
            antragsteller,
            vertretung,
            UrlaubsantragStatus.ANTRAG_GESTELLT,
            initialeStatusHistorie(),
            prozessinstanzId
        );
    }

    public static List<UrlaubsantragStatusHistorieneintrag> initialeStatusHistorie() {
        return List.of(UrlaubsantragStatusHistorieneintrag.ohneKommentar(UrlaubsantragStatus.ANTRAG_GESTELLT));
    }

    public static List<UrlaubsantragStatusHistorieneintrag> initialStatusHistory() {
        return initialeStatusHistorie();
    }

    public static List<UrlaubsantragStatusHistorieneintrag> statusHistorie(UrlaubsantragStatus... statuses) {
        return Arrays.stream(statuses)
            .map(UrlaubsantragStatusHistorieneintrag::ohneKommentar)
            .toList();
    }

    public static List<UrlaubsantragStatusHistorieneintrag> statushistorie(UrlaubsantragStatus... statuses) {
        return statusHistorie(statuses);
    }
}
