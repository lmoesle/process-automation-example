package de.lmoesle.processautomationexample.adapter.out.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "benutzer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BenutzerEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "benutzer", fetch = LAZY)
    private Set<TeamMitgliedschaftEntity> teamMitgliedschaften = new LinkedHashSet<>();
}
