package de.lmoesle.processautomationexample.adapter.out.persistence;

import de.lmoesle.processautomationexample.domain.vacationrequest.ProcessInstanceId;
import de.lmoesle.processautomationexample.domain.vacationrequest.UserId;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationPeriod;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequest;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequestId;

public final class VacationRequestPersistenceMapper {

    private VacationRequestPersistenceMapper() {
    }

    public static VacationRequestEntity toEntity(VacationRequest vacationRequest) {
        return new VacationRequestEntity(
            vacationRequest.id().value(),
            vacationRequest.period().from(),
            vacationRequest.period().to(),
            vacationRequest.applicantUserId().value(),
            vacationRequest.substituteUserId() == null ? null : vacationRequest.substituteUserId().value(),
            vacationRequest.processInstanceId() == null ? null : vacationRequest.processInstanceId().value()
        );
    }

    public static VacationRequest toDomain(VacationRequestEntity vacationRequestEntity) {
        return VacationRequest.reconstitute(
            VacationRequestId.of(vacationRequestEntity.getId()),
            VacationPeriod.of(vacationRequestEntity.getFrom(), vacationRequestEntity.getTo()),
            UserId.of(vacationRequestEntity.getApplicantUserId()),
            vacationRequestEntity.getSubstituteUserId() == null
                ? null
                : UserId.of(vacationRequestEntity.getSubstituteUserId()),
            vacationRequestEntity.getProcessInstanceId() == null
                ? null
                : ProcessInstanceId.of(vacationRequestEntity.getProcessInstanceId())
        );
    }
}
