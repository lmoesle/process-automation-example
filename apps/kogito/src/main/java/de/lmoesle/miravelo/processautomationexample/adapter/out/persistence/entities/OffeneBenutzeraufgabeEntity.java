package de.lmoesle.miravelo.processautomationexample.adapter.out.persistence.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "offene_benutzeraufgaben")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OffeneBenutzeraufgabeEntity {

    @Id
    @Column(name = "task_id", nullable = false, updatable = false)
    private String taskId;

    @Column(name = "assignee")
    private UUID assignee;

    @Column(name = "task_name", nullable = false)
    private String taskName;

    @Column(name = "prozessinstanz_id", nullable = false)
    private String prozessinstanzId;

    @Column(name = "business_key", nullable = false)
    private String businessKey;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "offene_benutzeraufgaben_kandidaten",
        joinColumns = @JoinColumn(name = "task_id")
    )
    @Column(name = "benutzer_id", nullable = false)
    private Set<UUID> candidateUserIds = new LinkedHashSet<>();

    public void weiseZu(UUID benutzerId) {
        this.assignee = benutzerId;
    }
}
