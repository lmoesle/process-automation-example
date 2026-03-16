package de.lmoesle.processautomationexample.adapter.out.persistence;

import de.lmoesle.processautomationexample.application.ports.out.LoadVacationRequestsOutPort;
import de.lmoesle.processautomationexample.application.ports.out.SaveVacationRequestOutPort;
import de.lmoesle.processautomationexample.domain.user.User;
import de.lmoesle.processautomationexample.domain.user.UserId;
import de.lmoesle.processautomationexample.domain.vacationrequest.VacationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static java.util.stream.Collectors.toMap;

@Component
@RequiredArgsConstructor
public class VacationRequestPersistenceAdapter implements SaveVacationRequestOutPort, LoadVacationRequestsOutPort {

    private final VacationRequestJpaRepository vacationRequestJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public VacationRequest save(VacationRequest vacationRequest) {
        vacationRequestJpaRepository.saveAndFlush(VacationRequestPersistenceMapper.toEntity(vacationRequest));
        return vacationRequest;
    }

    @Override
    public List<VacationRequest> findAllByApplicantUserId(UserId applicantUserId) {
        Assert.notNull(applicantUserId, "applicantUserId must not be null");

        List<VacationRequestEntity> vacationRequestEntities = vacationRequestJpaRepository.findAllByApplicantUserId(
            applicantUserId.value(),
            Sort.by(
                new Sort.Order(DESC, "from"),
                new Sort.Order(DESC, "to")
            )
        );

        if (vacationRequestEntities.isEmpty()) {
            return List.of();
        }

        Map<UUID, User> usersById = userJpaRepository.findDistinctByIdIn(
                vacationRequestEntities.stream()
                    .flatMap(entity -> java.util.stream.Stream.of(entity.getApplicantUserId(), entity.getSubstituteUserId()))
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList()
            ).stream()
            .map(UserPersistenceMapper::toDomain)
            .collect(toMap(user -> user.id().value(), Function.identity()));

        return vacationRequestEntities.stream()
            .map(entity -> VacationRequestPersistenceMapper.toDomain(
                entity,
                requireUser(usersById, entity.getApplicantUserId(), entity.getId(), "applicantUserId"),
                entity.getSubstituteUserId() == null
                    ? null
                    : requireUser(usersById, entity.getSubstituteUserId(), entity.getId(), "substituteUserId")
            ))
            .toList();
    }

    private static User requireUser(Map<UUID, User> usersById, UUID userId, UUID vacationRequestId, String fieldName) {
        User user = usersById.get(userId);

        if (user == null) {
            throw new IllegalStateException(
                "Could not load " + fieldName + " " + userId + " for vacation request " + vacationRequestId
            );
        }

        return user;
    }
}
