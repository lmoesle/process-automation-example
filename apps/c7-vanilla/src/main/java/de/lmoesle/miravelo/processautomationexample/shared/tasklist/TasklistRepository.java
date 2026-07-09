package de.lmoesle.miravelo.processautomationexample.shared.tasklist;

import de.lmoesle.miravelo.processautomationexample.application.ports.out.BenutzerRepositoryOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.TasklistRepositoryOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.UrlaubsantraegeLadenOutPort;
import de.lmoesle.miravelo.processautomationexample.bpmn.VacationApprovalProcessApi;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTask;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragId;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class TasklistRepository implements TasklistRepositoryOutPort {

    private final TaskService taskService;
    private final UrlaubsantraegeLadenOutPort urlaubsantraegeLadenOutPort;
    private final BenutzerRepositoryOutPort benutzerRepositoryOutPort;

    @Override
    public List<UserTask> getAllTasks(BenutzerId benutzerId) {
        Assert.notNull(benutzerId, "benutzerId darf nicht null sein");
        return taskService.createTaskQuery()
            .or()
            .taskCandidateUser(benutzerId.value().toString())
            .includeAssignedTasks()
            .taskAssignee(benutzerId.value().toString())
            .endOr()
            .active()
            .list()
            .stream()
            .map(this::mapTask)
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

        return Optional.ofNullable(taskService.createTaskQuery()
                .taskId(taskId.value())
                .or()
                .taskCandidateUser(benutzerId.value().toString())
                .includeAssignedTasks()
                .taskAssignee(benutzerId.value().toString())
                .endOr()
                .singleResult())
            .map(this::mapTask)
            .filter(task -> task.istSichtbarFuer(benutzerId));
    }

    private Optional<UserTask> ladeTask(UserTaskId taskId) {
        return Optional.ofNullable(taskService.createTaskQuery().taskId(taskId.value()).singleResult())
            .map(this::mapTask);
    }

    private UserTask mapTask(Task task) {
        final Map<String, Object> payload = taskService.getVariables(task.getId());
        return new UserTask(
            UserTaskId.of(task.getId()),
            ladeUrlaubsantrag(payload).orElse(null),
            ladeCandidateUsers(task),
            ladeBearbeiter(task).orElse(null)
        );
    }

    private Optional<Urlaubsantrag> ladeUrlaubsantrag(Map<String, Object> payload) {
        return parseUrlaubsantragId(payload.get(VacationApprovalProcessApi.Variables.AutomaticCheck.URLAUBSANTRAG_ID.getValue()))
            .flatMap(urlaubsantraegeLadenOutPort::findeNachId);
    }

    private List<Benutzer> ladeCandidateUsers(Task task) {
        return taskService.getIdentityLinksForTask(task.getId()).stream()
            .filter(identityLink -> IdentityLinkType.CANDIDATE.equals(identityLink.getType()))
            .map(IdentityLink::getUserId)
            .flatMap(this::parseBenutzerId)
            .map(benutzerRepositoryOutPort::findeNachId)
            .flatMap(Optional::stream)
            .toList();
    }

    private Optional<Benutzer> ladeBearbeiter(Task task) {
        return parseBenutzerId(task.getAssignee())
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
