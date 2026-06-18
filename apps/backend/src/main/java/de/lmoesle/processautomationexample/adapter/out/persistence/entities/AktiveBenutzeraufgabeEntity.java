package de.lmoesle.processautomationexample.adapter.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "aktive_benutzeraufgaben")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AktiveBenutzeraufgabeEntity {

    @Id
    @Column(name = "task_id", nullable = false, updatable = false)
    private String taskId;
}
