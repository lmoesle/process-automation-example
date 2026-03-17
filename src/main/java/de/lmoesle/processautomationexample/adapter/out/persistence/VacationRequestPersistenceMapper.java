package de.lmoesle.processautomationexample.adapter.out.persistence;

import de.lmoesle.processautomationexample.domain.user.User;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequest;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationPeriod;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestId;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestStatusHistoryEntry;

import java.util.ArrayList;

public final class VacationRequestPersistenceMapper {

    private VacationRequestPersistenceMapper() {
    }

    public static VacationRequestEntity toEntity(VacationRequest vacationRequest) {
        return new VacationRequestEntity(
            vacationRequest.id().value(),
            vacationRequest.period().from(),
            vacationRequest.period().to(),
            vacationRequest.applicantUser().id().value(),
            vacationRequest.substituteUser() == null ? null : vacationRequest.substituteUser().id().value(),
            vacationRequest.processInstanceId() == null ? null : vacationRequest.processInstanceId().value(),
            vacationRequest.status(),
            new ArrayList<>(vacationRequest.statusHistory().stream()
                .map(statusHistoryEntry -> new VacationRequestStatusHistoryEntryEmbeddable(
                    statusHistoryEntry.status(),
                    statusHistoryEntry.comment()
                ))
                .toList())
        );
    }

    public static VacationRequest toDomain(
        VacationRequestEntity vacationRequestEntity,
        User applicantUser,
        User substituteUser
    ) {
        return VacationRequest.reconstitute(
            VacationRequestId.of(vacationRequestEntity.getId()),
            VacationPeriod.of(vacationRequestEntity.getFrom(), vacationRequestEntity.getTo()),
            applicantUser,
            substituteUser,
            vacationRequestEntity.getStatus(),
            vacationRequestEntity.getStatusHistory().stream()
                .map(statusHistoryEntry -> new VacationRequestStatusHistoryEntry(
                    statusHistoryEntry.getStatus(),
                    statusHistoryEntry.getComment()
                ))
                .toList(),
            vacationRequestEntity.getProcessInstanceId() == null ? null : de.lmoesle.processautomationexample.domain.vacationrequest.ProcessInstanceId.of(
                vacationRequestEntity.getProcessInstanceId()
            )
        );
    }
}
