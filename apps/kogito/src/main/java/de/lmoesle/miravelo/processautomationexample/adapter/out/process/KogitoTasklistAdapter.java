package de.lmoesle.miravelo.processautomationexample.adapter.out.process;

import de.lmoesle.miravelo.processautomationexample.application.ports.out.AssignTaskOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.CompleteTaskOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.TasklistRepositoryOutPort;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.OffeneBenutzeraufgabe;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskId;
import lombok.RequiredArgsConstructor;
import org.kie.kogito.Application;
import org.kie.kogito.auth.SecurityPolicy;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.Processes;
import org.kie.kogito.services.uow.UnitOfWorkExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KogitoTasklistAdapter implements CompleteTaskOutPort {

    private final Processes processes;
    private final Application application;
    private final TasklistRepositoryOutPort tasklistRepositoryOutPort;
    private final AssignTaskOutPort assignTaskOutPort;

    @Override
    public void completeTask(UserTaskId taskId, BenutzerId benutzerId, boolean genehmigt) {
        assignTaskOutPort.assignTaskToUser(taskId, benutzerId);
        final var aufgabe = ladeAufgabe(taskId);

        try {
            UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
                final var prozessinstanz = ladeProzessinstanz(aufgabe);
                prozessinstanz.completeWorkItem(
                    taskId.value(),
                    Map.of("genehmigt", genehmigt, "ActorId", benutzerId.value().toString()),
                    SecurityPolicy.of(benutzerId.value().toString(), List.of())
                );
                return null;
            });
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                "Aufgabe " + taskId.value() + " konnte nicht abgeschlossen werden",
                exception
            );
        }
    }

    private OffeneBenutzeraufgabe ladeAufgabe(UserTaskId taskId) {
        return tasklistRepositoryOutPort.findeOffeneAufgabe(taskId)
            .orElseThrow(() -> new IllegalStateException("Aufgabe " + taskId.value() + " wurde nicht gefunden"));
    }

    private ProcessInstance<?> ladeProzessinstanz(OffeneBenutzeraufgabe aufgabe) {
        final Process<?> process = processes.processById(KogitoUrlaubsantragGenehmigungsprozessAdapter.PROCESS_ID);
        if (process == null) {
            throw new IllegalStateException(
                "Kogito-Prozess " + KogitoUrlaubsantragGenehmigungsprozessAdapter.PROCESS_ID + " wurde nicht gefunden"
            );
        }

        return process.instances().findById(aufgabe.prozessinstanzId().value())
            .orElseThrow(() -> new IllegalStateException(
                "Prozessinstanz " + aufgabe.prozessinstanzId().value() + " wurde nicht gefunden"
            ));
    }
}
