package de.lmoesle.processautomationexample.adapter.out.persistence;

import de.lmoesle.processautomationexample.application.ports.out.SaveVacationRequestOutPort;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VacationRequestPersistenceAdapter implements SaveVacationRequestOutPort {

    private final VacationRequestJpaRepository vacationRequestJpaRepository;

    @Override
    public VacationRequest save(VacationRequest vacationRequest) {
        var persistedEntity = vacationRequestJpaRepository.saveAndFlush(
            VacationRequestPersistenceMapper.toEntity(vacationRequest)
        );
        return VacationRequestPersistenceMapper.toDomain(persistedEntity);
    }
}
