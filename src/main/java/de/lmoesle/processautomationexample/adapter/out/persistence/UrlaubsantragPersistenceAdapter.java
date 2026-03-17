package de.lmoesle.processautomationexample.adapter.out.persistence;

import de.lmoesle.processautomationexample.application.ports.out.UrlaubsantraegeLadenOutPort;
import de.lmoesle.processautomationexample.application.ports.out.UrlaubsantragSpeichernOutPort;
import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static java.util.stream.Collectors.toMap;

@Component
@RequiredArgsConstructor
public class UrlaubsantragPersistenceAdapter implements UrlaubsantragSpeichernOutPort, UrlaubsantraegeLadenOutPort {

    private final UrlaubsantragJpaRepository urlaubsantragJpaRepository;
    private final BenutzerJpaRepository benutzerJpaRepository;

    @Override
    public Urlaubsantrag speichere(Urlaubsantrag urlaubsantrag) {
        urlaubsantragJpaRepository.saveAndFlush(UrlaubsantragPersistenceMapper.toEntity(urlaubsantrag));
        return urlaubsantrag;
    }

    @Override
    public Optional<Urlaubsantrag> findeNachId(UrlaubsantragId urlaubsantragId) {
        Assert.notNull(urlaubsantragId, "urlaubsantragId darf nicht null sein");

        return urlaubsantragJpaRepository.findById(urlaubsantragId.value())
            .map(this::toDomain);
    }

    @Override
    public List<Urlaubsantrag> findeAlleNachAntragstellerId(BenutzerId antragstellerId) {
        Assert.notNull(antragstellerId, "antragstellerId darf nicht null sein");

        List<UrlaubsantragEntity> urlaubsantragEntities = urlaubsantragJpaRepository.findAllByAntragstellerId(
            antragstellerId.value(),
            Sort.by(
                new Sort.Order(DESC, "von"),
                new Sort.Order(DESC, "bis")
            )
        );

        if (urlaubsantragEntities.isEmpty()) {
            return List.of();
        }

        Map<UUID, Benutzer> benutzerNachId = ladeBenutzerNachId(urlaubsantragEntities);

        return urlaubsantragEntities.stream()
            .map(entity -> toDomain(entity, benutzerNachId))
            .toList();
    }

    private Urlaubsantrag toDomain(UrlaubsantragEntity urlaubsantragEntity) {
        Map<UUID, Benutzer> benutzerNachId = ladeBenutzerNachId(List.of(urlaubsantragEntity));
        return toDomain(urlaubsantragEntity, benutzerNachId);
    }

    private Map<UUID, Benutzer> ladeBenutzerNachId(List<UrlaubsantragEntity> urlaubsantragEntities) {
        Map<UUID, Benutzer> benutzerNachId = benutzerJpaRepository.findDistinctByIdIn(
            urlaubsantragEntities.stream()
                .flatMap(entity -> Stream.of(entity.getAntragstellerId(), entity.getVertretungId()))
                .filter(Objects::nonNull)
                .distinct()
                .toList()
        ).stream()
            .map(BenutzerPersistenceMapper::toDomain)
            .collect(toMap(benutzer -> benutzer.id().value(), Function.identity()));

        return benutzerNachId;
    }

    private Urlaubsantrag toDomain(UrlaubsantragEntity entity, Map<UUID, Benutzer> benutzerNachId) {
        return UrlaubsantragPersistenceMapper.toDomain(
            entity,
            erfordereBenutzer(benutzerNachId, entity.getAntragstellerId(), entity.getId(), "antragstellerId"),
            entity.getVertretungId() == null
                ? null
                : erfordereBenutzer(benutzerNachId, entity.getVertretungId(), entity.getId(), "vertretungId")
        );
    }

    private static Benutzer erfordereBenutzer(Map<UUID, Benutzer> benutzerNachId, UUID benutzerId, UUID urlaubsantragId, String feldname) {
        Benutzer benutzer = benutzerNachId.get(benutzerId);

        if (benutzer == null) {
            throw new IllegalStateException(
                "Konnte " + feldname + " " + benutzerId + " fuer Urlaubsantrag " + urlaubsantragId + " nicht laden"
            );
        }

        return benutzer;
    }
}
