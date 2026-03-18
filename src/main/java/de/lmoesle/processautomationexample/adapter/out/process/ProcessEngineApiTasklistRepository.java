package de.lmoesle.processautomationexample.adapter.out.process;

import de.lmoesle.processautomationexample.application.ports.out.BenutzerRepositoryOutPort;
import de.lmoesle.processautomationexample.application.ports.out.TasklistRepositoryOutPort;
import de.lmoesle.processautomationexample.application.ports.out.UrlaubsantraegeLadenOutPort;
import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.processautomationexample.domain.tasklist.UserTask;
import de.lmoesle.processautomationexample.domain.tasklist.UserTaskId;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragId;
import dev.bpmcrafters.processengineapi.task.ChangeAssignmentModifyTaskCmd.AssignTaskCmd;
import dev.bpmcrafters.processengineapi.task.TaskInformation;
import dev.bpmcrafters.processengineapi.task.UserTaskModificationApi;
import dev.bpmcrafters.processengineapi.task.support.UserTaskSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class ProcessEngineApiTasklistRepository implements TasklistRepositoryOutPort {

    private static final long TASK_MODIFICATION_TIMEOUT_SECONDS = 10;

    private final UserTaskSupport userTaskSupport;
    private final UserTaskModificationApi userTaskModificationApi;
    private final UrlaubsantraegeLadenOutPort urlaubsantraegeLadenOutPort;
    private final BenutzerRepositoryOutPort benutzerRepositoryOutPort;

    @Override
    public List<UserTask> getAllTasks(BenutzerId benutzerId) {
        Assert.notNull(benutzerId, "benutzerId darf nicht null sein");
        return userTaskSupport.getAllTasks().stream()
            .map(taskInformation -> mapTask(taskInformation, userTaskSupport.getPayload(taskInformation.getTaskId())))
            .filter(task -> task.istSichtbarFuer(benutzerId))
            .toList();
    }

    @Override
    public Optional<UserTask> getTaskById(UserTaskId taskId) {
        Assert.notNull(taskId, "taskId darf nicht null sein");
        return ladeTask(taskId);
    }

    @Override
    public Optional<UserTask> getTaskById(UserTaskId taskId, BenutzerId benutzerId) {
        Assert.notNull(taskId, "taskId darf nicht null sein");
        Assert.notNull(benutzerId, "benutzerId darf nicht null sein");

        return ladeTask(taskId)
            .filter(task -> task.istSichtbarFuer(benutzerId));
    }

    @Override
    public void assignTaskToUser(UserTaskId taskId, BenutzerId benutzerId) {
        Assert.notNull(taskId, "taskId darf nicht null sein");
        Assert.notNull(benutzerId, "benutzerId darf nicht null sein");

        try {
            userTaskModificationApi.update(new AssignTaskCmd(taskId.value(), benutzerId.value().toString()))
                .get(TASK_MODIFICATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | IllegalStateException | ExecutionException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException(
                "Aufgabe " + taskId.value() + " konnte Benutzer " + benutzerId.value() + " nicht zugewiesen werden",
                exception
            );
        }
    }

    private Optional<UserTask> ladeTask(UserTaskId taskId) {
        try {
            TaskInformation taskInformation = userTaskSupport.getTaskInformation(taskId.value());
            return Optional.of(mapTask(taskInformation, userTaskSupport.getPayload(taskId.value())));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private UserTask mapTask(TaskInformation taskInformation, Map<String, Object> payload) {
        return new UserTask(
            UserTaskId.of(taskInformation.getTaskId()),
            ladeUrlaubsantrag(payload).orElse(null),
            ladeCandidateUsers(payload),
            ladeBearbeiter(taskInformation.getMeta()).orElse(null)
        );
    }

    private Optional<Urlaubsantrag> ladeUrlaubsantrag(Map<String, Object> payload) {
        return parseUrlaubsantragId(payload.get("urlaubsantragId"))
            .flatMap(urlaubsantraegeLadenOutPort::findeNachId);
    }

    private List<Benutzer> ladeCandidateUsers(Map<String, Object> payload) {
        return extrahiereBenutzerIds(payload.get("teamLeadIds")).stream()
            .map(benutzerRepositoryOutPort::findeNachId)
            .flatMap(Optional::stream)
            .toList();
    }

    private Optional<Benutzer> ladeBearbeiter(Map<String, String> meta) {
        return extrahiereBenutzerIds(meta.get("assignee")).stream()
            .findFirst()
            .flatMap(benutzerRepositoryOutPort::findeNachId);
    }

    private Optional<UrlaubsantragId> parseUrlaubsantragId(Object rawValue) {
        if (rawValue instanceof UUID uuid) {
            return Optional.of(UrlaubsantragId.of(uuid));
        }

        if (rawValue instanceof String stringValue && StringUtils.hasText(stringValue)) {
            try {
                return Optional.of(UrlaubsantragId.of(stringValue.trim()));
            } catch (IllegalArgumentException exception) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    private List<BenutzerId> extrahiereBenutzerIds(Object rawValue) {
        return rohenWertAlsStream(rawValue)
            .flatMap(this::parseBenutzerId)
            .distinct()
            .toList();
    }

    private Stream<Object> rohenWertAlsStream(Object rawValue) {
        if (rawValue == null) {
            return Stream.empty();
        }

        if (rawValue instanceof Iterable<?> iterable) {
            return StreamSupport.stream(iterable.spliterator(), false)
                .map(value -> (Object) value);
        }

        if (rawValue instanceof Object[] array) {
            return Arrays.stream(array);
        }

        if (rawValue instanceof String stringValue && stringValue.contains(",")) {
            return Arrays.stream(stringValue.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(value -> value);
        }

        return Stream.of(rawValue);
    }

    private Stream<BenutzerId> parseBenutzerId(Object rawValue) {
        if (rawValue instanceof BenutzerId benutzerId) {
            return Stream.of(benutzerId);
        }

        if (rawValue instanceof UUID uuid) {
            return Stream.of(BenutzerId.of(uuid));
        }

        if (rawValue instanceof String stringValue && StringUtils.hasText(stringValue)) {
            try {
                return Stream.of(BenutzerId.of(UUID.fromString(stringValue.trim())));
            } catch (IllegalArgumentException exception) {
                return Stream.empty();
            }
        }

        return Stream.empty();
    }
}
