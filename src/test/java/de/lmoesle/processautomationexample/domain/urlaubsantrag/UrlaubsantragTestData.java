package de.lmoesle.processautomationexample.domain.urlaubsantrag;

import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class UrlaubsantragTestData {

    public static final UUID VACATION_REQUEST_UUID = UrlaubsantragTestdaten.VACATION_REQUEST_UUID;
    public static final UUID SECOND_VACATION_REQUEST_UUID = UrlaubsantragTestdaten.SECOND_VACATION_REQUEST_UUID;
    public static final UUID APPLICANT_USER_UUID = UrlaubsantragTestdaten.APPLICANT_USER_UUID;
    public static final UUID SUBSTITUTE_USER_UUID = UrlaubsantragTestdaten.SUBSTITUTE_USER_UUID;
    public static final String PROCESS_INSTANCE_ID_VALUE = UrlaubsantragTestdaten.PROCESS_INSTANCE_ID_VALUE;
    public static final String SECOND_PROCESS_INSTANCE_ID_VALUE = UrlaubsantragTestdaten.SECOND_PROCESS_INSTANCE_ID_VALUE;
    public static final LocalDate FROM = UrlaubsantragTestdaten.FROM;
    public static final LocalDate TO = UrlaubsantragTestdaten.TO;
    public static final LocalDate SECOND_FROM = UrlaubsantragTestdaten.SECOND_FROM;
    public static final LocalDate SECOND_TO = UrlaubsantragTestdaten.SECOND_TO;

    private UrlaubsantragTestData() {
    }

    public static UrlaubsantragId urlaubsantragId() {
        return UrlaubsantragTestdaten.urlaubsantragId();
    }

    public static BenutzerId antragstellerId() {
        return UrlaubsantragTestdaten.antragstellerId();
    }

    public static Benutzer antragsteller() {
        return UrlaubsantragTestdaten.antragsteller();
    }

    public static BenutzerId vertretungId() {
        return UrlaubsantragTestdaten.vertretungId();
    }

    public static Benutzer vertretung() {
        return UrlaubsantragTestdaten.vertretung();
    }

    public static ProzessinstanzId prozessinstanzId() {
        return UrlaubsantragTestdaten.prozessinstanzId();
    }

    public static Urlaubszeitraum vacationPeriod() {
        return UrlaubsantragTestdaten.vacationPeriod();
    }

    public static Urlaubsantrag urlaubsantrag() {
        return UrlaubsantragTestdaten.urlaubsantrag();
    }

    public static Urlaubsantrag urlaubsantragWithStartedProcess() {
        return UrlaubsantragTestdaten.urlaubsantragWithStartedProcess();
    }

    public static UrlaubsantragId secondUrlaubsantragId() {
        return UrlaubsantragTestdaten.secondUrlaubsantragId();
    }

    public static Urlaubszeitraum secondUrlaubszeitraum() {
        return UrlaubsantragTestdaten.secondUrlaubszeitraum();
    }

    public static ProzessinstanzId secondProzessinstanzId() {
        return UrlaubsantragTestdaten.secondProzessinstanzId();
    }

    public static Urlaubsantrag secondUrlaubsantrag(Benutzer antragsteller, Benutzer vertretung) {
        return UrlaubsantragTestdaten.secondUrlaubsantrag(antragsteller, vertretung);
    }

    public static Urlaubsantrag urlaubsantrag(
        UrlaubsantragId urlaubsantragId,
        Urlaubszeitraum vacationPeriod,
        Benutzer antragsteller,
        Benutzer vertretung,
        ProzessinstanzId prozessinstanzId
    ) {
        return UrlaubsantragTestdaten.urlaubsantrag(
            urlaubsantragId,
            vacationPeriod,
            antragsteller,
            vertretung,
            prozessinstanzId
        );
    }

    public static List<UrlaubsantragStatusHistorieneintrag> initialStatusHistory() {
        return UrlaubsantragTestdaten.initialStatusHistory();
    }

    public static List<UrlaubsantragStatusHistorieneintrag> statushistorie(UrlaubsantragStatus... statuses) {
        return UrlaubsantragTestdaten.statushistorie(statuses);
    }
}
