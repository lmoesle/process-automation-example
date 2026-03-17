package de.lmoesle.processautomationexample.adapter.out.persistence;

import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestStatus;
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
public class VacationRequestStatusHistoryEntryEmbeddable {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VacationRequestStatus status;

    @Column(name = "comment_text")
    private String comment;
}
