package de.lmoesle.processautomationexample.adapter.out.persistence;

import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UrlaubsantragStatusHistorieneintragEmbeddable {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UrlaubsantragStatus status;

    @Column(name = "kommentar")
    private String kommentar;
}
