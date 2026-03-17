package de.lmoesle.processautomationexample.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TeamMitgliedschaftId implements Serializable {

    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    @Column(name = "benutzer_id", nullable = false)
    private UUID benutzerId;
}
