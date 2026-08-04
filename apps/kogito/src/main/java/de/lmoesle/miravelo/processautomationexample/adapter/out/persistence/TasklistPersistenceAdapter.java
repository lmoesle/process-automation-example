package de.lmoesle.miravelo.processautomationexample.adapter.out.persistence;

import de.lmoesle.miravelo.processautomationexample.adapter.out.persistence.entities.OffeneBenutzeraufgabeEntity;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.AssignTaskOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.BenutzerRepositoryOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.TasklistRepositoryOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.UrlaubsantraegeLadenOutPort;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.OffeneBenutzeraufgabe;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.TaskZugriffVerweigertException;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTask;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.ProzessinstanzId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TasklistPersistenceAdapter implements TasklistRepositoryOutPort, AssignTaskOutPort {

    private final OffeneBenutzeraufgabeJpaRepository offeneBenutzeraufgabeJpaRepository;
    private final UrlaubsantraegeLadenOutPort urlaubsantraegeLadenOutPort;
    private final BenutzerRepositoryOutPort benutzerRepositoryOutPort;

    @Override
    public boolean speichere(OffeneBenutzeraufgabe aufgabe) {
        Assert.notNull(aufgabe, "aufgabe darf nicht null sein");

        final var entity = toEntity(aufgabe);
        final boolean neueAufgabe = offeneBenutzeraufgabeJpaRepository.insertiereWennNichtVorhanden(
            entity.getTaskId(),
            entity.getAssignee(),
            entity.getTaskName(),
            entity.getProzessinstanzId(),
            entity.getBusinessKey()
        ) == 1;
        offeneBenutzeraufgabeJpaRepository.save(entity);
        return neueAufgabe;
    }

    @Override
    public void entferne(UserTaskId taskId) {
        Assert.notNull(taskId, "taskId darf nicht null sein");
        offeneBenutzeraufgabeJpaRepository.deleteById(taskId.value());
    }

    @Override
    public Optional<OffeneBenutzeraufgabe> findeOffeneAufgabe(UserTaskId taskId) {
        Assert.notNull(taskId, "taskId darf nicht null sein");
        return offeneBenutzeraufgabeJpaRepository.findById(taskId.value())
            .map(this::toOffeneBenutzeraufgabe);
    }

    @Override
    public void assignTaskToUser(UserTaskId taskId, BenutzerId benutzerId) {
        Assert.notNull(taskId, "taskId darf nicht null sein");
        Assert.notNull(benutzerId, "benutzerId darf nicht null sein");
        final int aktualisierteAufgaben;
        try {
            aktualisierteAufgaben = offeneBenutzeraufgabeJpaRepository.weiseZuWennNichtZugewiesen(
                taskId.value(),
                benutzerId.value()
            );
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                "Aufgabe " + taskId.value() + " konnte Benutzer " + benutzerId.value() + " nicht zugewiesen werden",
                exception
            );
        }
        if (aktualisierteAufgaben != 1) {
            throw new TaskZugriffVerweigertException(taskId);
        }
    }

    @Override
    public List<UserTask> getAllTasks(BenutzerId benutzerId) {
        Assert.notNull(benutzerId, "benutzerId darf nicht null sein");
        return offeneBenutzeraufgabeJpaRepository.findAllVisibleFor(benutzerId.value()).stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public Optional<UserTask> getTaskById(UserTaskId taskId) {
        Assert.notNull(taskId, "taskId darf nicht null sein");
        return offeneBenutzeraufgabeJpaRepository.findById(taskId.value())
            .map(this::toDomain);
    }

    @Override
    public Optional<UserTask> getTaskById(UserTaskId taskId, BenutzerId benutzerId) {
        Assert.notNull(taskId, "taskId darf nicht null sein");
        Assert.notNull(benutzerId, "benutzerId darf nicht null sein");
        return offeneBenutzeraufgabeJpaRepository.findVisibleById(taskId.value(), benutzerId.value())
            .map(this::toDomain);
    }

    private OffeneBenutzeraufgabeEntity toEntity(OffeneBenutzeraufgabe aufgabe) {
        return new OffeneBenutzeraufgabeEntity(
            aufgabe.id().value(),
            aufgabe.bearbeiterId() == null ? null : aufgabe.bearbeiterId().value(),
            aufgabe.name(),
            aufgabe.prozessinstanzId().value(),
            aufgabe.businessKey(),
            aufgabe.candidateUserIds().stream()
                .map(BenutzerId::value)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new))
        );
    }

    private OffeneBenutzeraufgabe toOffeneBenutzeraufgabe(OffeneBenutzeraufgabeEntity entity) {
        return new OffeneBenutzeraufgabe(
            UserTaskId.of(entity.getTaskId()),
            entity.getTaskName(),
            ProzessinstanzId.of(entity.getProzessinstanzId()),
            entity.getBusinessKey(),
            entity.getCandidateUserIds().stream().map(BenutzerId::of).toList(),
            entity.getAssignee() == null ? null : BenutzerId.of(entity.getAssignee())
        );
    }

    private UserTask toDomain(OffeneBenutzeraufgabeEntity entity) {
        return new UserTask(
            UserTaskId.of(entity.getTaskId()),
            ladeUrlaubsantrag(entity.getBusinessKey()).orElse(null),
            entity.getCandidateUserIds().stream()
                .map(BenutzerId::of)
                .map(benutzerRepositoryOutPort::findeNachId)
                .flatMap(Optional::stream)
                .toList(),
            ladeBenutzer(entity.getAssignee()).orElse(null)
        );
    }

    private Optional<Urlaubsantrag> ladeUrlaubsantrag(String businessKey) {
        try {
            return urlaubsantraegeLadenOutPort.findeNachId(UrlaubsantragId.of(businessKey));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private Optional<Benutzer> ladeBenutzer(UUID benutzerId) {
        return benutzerId == null
            ? Optional.empty()
            : benutzerRepositoryOutPort.findeNachId(BenutzerId.of(benutzerId));
    }
}
