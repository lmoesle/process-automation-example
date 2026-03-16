package de.lmoesle.processautomationexample.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

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
}
