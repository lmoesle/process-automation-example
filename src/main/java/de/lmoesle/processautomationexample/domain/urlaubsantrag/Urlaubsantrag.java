package de.lmoesle.processautomationexample.domain.urlaubsantrag;

import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(fluent = true)
public final class Urlaubsantrag {

    private final UrlaubsantragId id;
    private final Urlaubszeitraum zeitraum;
    private final Benutzer antragsteller;
    private final Benutzer vertretung;
    private Benutzer vorgesetzter;
    private UrlaubsantragStatus status;
    private final List<UrlaubsantragStatusHistorieneintrag> statusHistorie;
    private ProzessinstanzId prozessinstanzId;

    public Urlaubsantrag(
        UrlaubsantragId id,
        Urlaubszeitraum zeitraum,
        Benutzer antragsteller,
        Benutzer vertretung,
        Benutzer vorgesetzter,
        UrlaubsantragStatus status,
        List<UrlaubsantragStatusHistorieneintrag> statusHistorie,
        ProzessinstanzId prozessinstanzId
    ) {
        Assert.notNull(id, "id darf nicht null sein");
        Assert.notNull(zeitraum, "zeitraum darf nicht null sein");
        Assert.notNull(antragsteller, "antragsteller darf nicht null sein");
        Assert.notNull(status, "status darf nicht null sein");
        Assert.notEmpty(statusHistorie, "statusHistorie darf nicht leer sein");
        Assert.isTrue(
            statusHistorie.getLast().status() == status,
            "Der letzte Statushistorieneintrag muss dem aktuellen Status entsprechen."
        );
        this.id = id;
        this.zeitraum = zeitraum;
        this.antragsteller = antragsteller;
        this.vertretung = vertretung;
        this.vorgesetzter = vorgesetzter;
        this.status = status;
        this.statusHistorie = new ArrayList<>(statusHistorie);
        this.prozessinstanzId = prozessinstanzId;
    }

    public static Urlaubsantrag stelle(LocalDate von, LocalDate bis, Benutzer antragsteller, Benutzer vertretung) {
        return new Urlaubsantrag(
            UrlaubsantragId.neu(),
            Urlaubszeitraum.of(von, bis),
            antragsteller,
            vertretung,
            null,
            UrlaubsantragStatus.ANTRAG_GESTELLT,
            List.of(UrlaubsantragStatusHistorieneintrag.ohneKommentar(UrlaubsantragStatus.ANTRAG_GESTELLT)),
            null
        );
    }

    public void markiereGenehmigungsprozessAlsGestartet(ProzessinstanzId prozessinstanzId) {
        Assert.notNull(prozessinstanzId, "prozessinstanzId darf nicht null sein");
        Assert.state(this.prozessinstanzId == null, "Der Genehmigungsprozess wurde bereits gestartet.");
        this.prozessinstanzId = prozessinstanzId;
    }

    public void starteAutomatischePruefung() {
        if (status == UrlaubsantragStatus.AUTOMATISCHE_PRUEFUNG) {
            return;
        }
        Assert.state(
            status == UrlaubsantragStatus.ANTRAG_GESTELLT,
            "Die automatische Pruefung kann nur fuer gestellte Urlaubsantraege gestartet werden."
        );
        wechsleZu(UrlaubsantragStatus.AUTOMATISCHE_PRUEFUNG, null);
    }

    public boolean istAutomatischGueltigGegen(List<Urlaubsantrag> vertretungsUrlaubsantraege) {
        if (vertretung == null) {
            return true;
        }

        Assert.notNull(vertretungsUrlaubsantraege, "vertretungsUrlaubsantraege duerfen nicht null sein");
        return vertretungsUrlaubsantraege.stream()
                .filter(urlaubsantrag -> urlaubsantrag.status().equals(UrlaubsantragStatus.GENEHMIGT))
                .noneMatch(urlaubsantrag -> zeitraum.ueberschneidetSichMit(urlaubsantrag.zeitraum()));
    }

    public void schliesseAutomatischePruefungAb(boolean gueltig) {
        UrlaubsantragStatus neuerStatus = gueltig
            ? UrlaubsantragStatus.VORGESETZTEN_PRUEFUNG
            : UrlaubsantragStatus.ABGELEHNT;

        if (status == neuerStatus) {
            return;
        }

        Assert.state(
            status == UrlaubsantragStatus.AUTOMATISCHE_PRUEFUNG,
            "Die automatische Pruefung kann nur aus dem Status AUTOMATISCHE_PRUEFUNG abgeschlossen werden."
        );
        wechsleZu(neuerStatus, null);
    }

    public void genehmigeDurchVorgesetzten(String kommentar) {
        pruefeVorgesetztenentscheidungZulaessig();
        wechsleZu(UrlaubsantragStatus.GENEHMIGT, kommentar);
    }

    public void lehneDurchVorgesetztenAb(String kommentar) {
        pruefeVorgesetztenentscheidungZulaessig();
        wechsleZu(UrlaubsantragStatus.ABGELEHNT, kommentar);
    }

    public void weiseVorgesetztenZu(Benutzer vorgesetzter) {
        Assert.notNull(vorgesetzter, "vorgesetzter darf nicht null sein");

        if (vorgesetzter.equals(this.vorgesetzter)) {
            return;
        }

        this.vorgesetzter = vorgesetzter;
    }

    private void pruefeVorgesetztenentscheidungZulaessig() {
        Assert.state(
            status == UrlaubsantragStatus.VORGESETZTEN_PRUEFUNG,
            "Die Vorgesetztenentscheidung kann nur aus dem Status VORGESETZTEN_PRUEFUNG getroffen werden."
        );
    }

    private void wechsleZu(UrlaubsantragStatus neuerStatus, String kommentar) {
        status = neuerStatus;
        statusHistorie.add(new UrlaubsantragStatusHistorieneintrag(neuerStatus, kommentar));
    }
}
