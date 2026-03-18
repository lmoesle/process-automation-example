package de.lmoesle.processautomationexample.adapter.out.persistence.entities;

import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.FetchType.EAGER;

@Entity
@Table(name = "urlaubsantraege")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UrlaubsantragEntity {

    @Id
    private UUID id;

    @Column(name = "von", nullable = false)
    private LocalDate von;

    @Column(name = "bis", nullable = false)
    private LocalDate bis;

    @Column(name = "antragsteller_id", nullable = false)
    private UUID antragstellerId;

    @Column(name = "vertretung_id")
    private UUID vertretungId;

    @Column(name = "vorgesetzter_id")
    private UUID vorgesetzterId;

    @Column(name = "prozessinstanz_id")
    private String prozessinstanzId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UrlaubsantragStatus status;

    @ElementCollection(fetch = EAGER)
    @CollectionTable(
        name = "urlaubsantrag_statushistorie",
        joinColumns = @JoinColumn(name = "urlaubsantrag_id")
    )
    @OrderColumn(name = "historien_index")
    private List<UrlaubsantragStatusHistorieneintragEmbeddable> statusHistorie = new ArrayList<>();
}
