package de.lmoesle.miravelo.processautomationexample.adapter.out.persistence;

import de.lmoesle.miravelo.processautomationexample.adapter.out.persistence.entities.UrlaubsantragEntity;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.UrlaubsantragProzessinstanzSpeichernOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.UrlaubsantraegeLadenOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.UrlaubsantragSpeichernOutPort;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.ProzessinstanzId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Component
@RequiredArgsConstructor
public class UrlaubsantragPersistenceAdapter implements
    UrlaubsantragSpeichernOutPort,
    UrlaubsantraegeLadenOutPort,
    UrlaubsantragProzessinstanzSpeichernOutPort {

    private final UrlaubsantragJpaRepository urlaubsantragJpaRepository;
    private final BenutzerJpaRepository benutzerJpaRepository;

    @Override
    public Urlaubsantrag speichere(Urlaubsantrag urlaubsantrag) {
        final UrlaubsantragEntity entity = UrlaubsantragPersistenceMapper.toEntity(urlaubsantrag);
        bewahreGespeicherteProzessinstanzId(entity);
        urlaubsantragJpaRepository.saveAndFlush(entity);
        return urlaubsantrag;
    }

    @Override
    public void speichereProzessinstanzId(UrlaubsantragId urlaubsantragId, ProzessinstanzId prozessinstanzId) {
        final int aktualisierteDatensaetze = urlaubsantragJpaRepository.setzeProzessinstanzIdWennLeer(
            urlaubsantragId.value(),
            prozessinstanzId.value()
        );

        if (aktualisierteDatensaetze == 1) {
            return;
        }

        final boolean prozessinstanzIdBereitsGespeichert = urlaubsantragJpaRepository.findeProzessinstanzIdNachId(urlaubsantragId.value())
            .filter(existingProzessinstanzId -> !existingProzessinstanzId.isBlank())
            .isPresent();

        if (!prozessinstanzIdBereitsGespeichert) {
            throw new IllegalStateException(
                "ProzessinstanzId fuer Urlaubsantrag " + urlaubsantragId.value() + " konnte nicht gespeichert werden"
            );
        }
    }

    @Override
    public Optional<Urlaubsantrag> findeNachId(UrlaubsantragId urlaubsantragId) {
        return urlaubsantragJpaRepository.findById(urlaubsantragId.value())
            .map(this::toDomain);
    }

    private void bewahreGespeicherteProzessinstanzId(UrlaubsantragEntity entity) {
        if (entity.getProzessinstanzId() != null) {
            return;
        }

        urlaubsantragJpaRepository.findeProzessinstanzIdNachId(entity.getId())
            .filter(prozessinstanzId -> !prozessinstanzId.isBlank())
            .ifPresent(entity::setProzessinstanzId);
    }

    @Override
    public List<Urlaubsantrag> findeAlleNachAntragstellerId(BenutzerId antragstellerId) {
        final List<UrlaubsantragEntity> urlaubsantragEntities = urlaubsantragJpaRepository.findAllByAntragstellerId(
            antragstellerId.value(),
            Sort.by(
                new Sort.Order(DESC, "von"),
                new Sort.Order(DESC, "bis")
            )
        );

        if (urlaubsantragEntities.isEmpty()) {
            return List.of();
        }

        final Map<UUID, Benutzer> benutzerNachId = ladeBenutzerNachId(urlaubsantragEntities);

        return urlaubsantragEntities.stream()
            .map(entity -> toDomain(entity, benutzerNachId))
            .toList();
    }

    private Urlaubsantrag toDomain(UrlaubsantragEntity urlaubsantragEntity) {
        final Map<UUID, Benutzer> benutzerNachId = ladeBenutzerNachId(List.of(urlaubsantragEntity));
        return toDomain(urlaubsantragEntity, benutzerNachId);
    }

    private Map<UUID, Benutzer> ladeBenutzerNachId(List<UrlaubsantragEntity> urlaubsantragEntities) {
        final Map<UUID, Benutzer> benutzerNachId = benutzerJpaRepository.findDistinctByIdIn(
            urlaubsantragEntities.stream()
                .flatMap(entity -> Stream.of(entity.getAntragstellerId(), entity.getVertretungId(), entity.getVorgesetzterId()))
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
                : erfordereBenutzer(benutzerNachId, entity.getVertretungId(), entity.getId(), "vertretungId"),
            entity.getVorgesetzterId() == null
                ? null
                : erfordereBenutzer(benutzerNachId, entity.getVorgesetzterId(), entity.getId(), "vorgesetzterId")
        );
    }

    private static Benutzer erfordereBenutzer(Map<UUID, Benutzer> benutzerNachId, UUID benutzerId, UUID urlaubsantragId, String feldname) {
        final Benutzer benutzer = benutzerNachId.get(benutzerId);

        if (benutzer == null) {
            throw new IllegalStateException(
                "Konnte " + feldname + " " + benutzerId + " fuer Urlaubsantrag " + urlaubsantragId + " nicht laden"
            );
        }

        return benutzer;
    }
}
