package de.lmoesle.miravelo.processautomationexample.adapter.out.persistence.entities;

import de.lmoesle.miravelo.processautomationexample.adapter.out.process.ProzessEngineOutboxAuftragStatus;
import de.lmoesle.miravelo.processautomationexample.adapter.out.process.ProzessEngineOutboxAuftragTyp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "prozess_engine_outbox_auftraege")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProzessEngineOutboxAuftragEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProzessEngineOutboxAuftragTyp typ;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProzessEngineOutboxAuftragStatus status;

    @Column(name = "urlaubsantrag_id")
    private UUID urlaubsantragId;

    @Column(name = "prozessinstanz_id")
    private String prozessinstanzId;

    @Column(name = "task_id")
    private String taskId;

    @Column(name = "benutzer_id")
    private UUID benutzerId;

    @Column(name = "team_lead_ids", length = 2000)
    private String teamLeadIds;

    @Column(name = "genehmigt")
    private Boolean genehmigt;

    @Column(name = "versuche", nullable = false)
    private int versuche;

    @Column(name = "erstellt_am", nullable = false)
    private Instant erstelltAm;

    @Column(name = "zuletzt_geaendert_am", nullable = false)
    private Instant zuletztGeaendertAm;

    @Column(name = "naechster_versuch_am", nullable = false)
    private Instant naechsterVersuchAm;

    @Column(name = "abgeschlossen_am")
    private Instant abgeschlossenAm;

    @Column(name = "letzte_fehlermeldung")
    private String letzteFehlermeldung;

    public void registriereVersuch(Instant zeitpunkt) {
        versuche++;
        zuletztGeaendertAm = zeitpunkt;
    }

    public void markiereErfolgreich(Instant zeitpunkt) {
        status = ProzessEngineOutboxAuftragStatus.ERFOLGREICH;
        abgeschlossenAm = zeitpunkt;
        zuletztGeaendertAm = zeitpunkt;
        letzteFehlermeldung = null;
    }

    public void markiereFehlgeschlagen(String fehlermeldung, Instant zeitpunkt, Instant naechsterVersuchAm) {
        status = ProzessEngineOutboxAuftragStatus.FEHLGESCHLAGEN;
        abgeschlossenAm = null;
        zuletztGeaendertAm = zeitpunkt;
        this.naechsterVersuchAm = naechsterVersuchAm;
        letzteFehlermeldung = fehlermeldung;
    }

    public void markiereEndgueltigFehlgeschlagen(String fehlermeldung, Instant zeitpunkt) {
        status = ProzessEngineOutboxAuftragStatus.ENDGUELTIG_FEHLGESCHLAGEN;
        abgeschlossenAm = zeitpunkt;
        zuletztGeaendertAm = zeitpunkt;
        naechsterVersuchAm = zeitpunkt;
        letzteFehlermeldung = fehlermeldung;
    }
}
