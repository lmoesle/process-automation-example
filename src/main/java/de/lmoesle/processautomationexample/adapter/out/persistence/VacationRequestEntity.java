package de.lmoesle.processautomationexample.adapter.out.persistence;

import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.FetchType.EAGER;

@Entity
@Table(name = "vacation_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VacationRequestEntity {

    @Id
    private UUID id;

    @Column(name = "vacation_from", nullable = false)
    private LocalDate from;

    @Column(name = "vacation_to", nullable = false)
    private LocalDate to;

    @Column(name = "applicant_user_id", nullable = false)
    private UUID applicantUserId;

    @Column(name = "substitute_user_id")
    private UUID substituteUserId;

    @Column(name = "process_instance_id")
    private String processInstanceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VacationRequestStatus status;

    @ElementCollection(fetch = EAGER)
    @CollectionTable(
        name = "vacation_request_status_history",
        joinColumns = @JoinColumn(name = "vacation_request_id")
    )
    @OrderColumn(name = "history_index")
    private List<VacationRequestStatusHistoryEntryEmbeddable> statusHistory = new ArrayList<>();
}
