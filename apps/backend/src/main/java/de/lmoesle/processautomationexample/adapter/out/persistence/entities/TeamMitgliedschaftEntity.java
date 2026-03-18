package de.lmoesle.processautomationexample.adapter.out.persistence.entities;

import de.lmoesle.processautomationexample.domain.benutzer.TeamRolle;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "team_mitgliedschaften")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"team", "benutzer"})
public class TeamMitgliedschaftEntity {

    @EmbeddedId
    private TeamMitgliedschaftId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("teamId")
    @JoinColumn(name = "team_id", nullable = false)
    private TeamEntity team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("benutzerId")
    @JoinColumn(name = "benutzer_id", nullable = false)
    private BenutzerEntity benutzer;

    @Enumerated(EnumType.STRING)
    @Column(name = "rolle", nullable = false)
    private TeamRolle rolle;
}
