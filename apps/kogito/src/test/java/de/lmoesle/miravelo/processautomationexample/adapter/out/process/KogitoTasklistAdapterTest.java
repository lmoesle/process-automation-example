package de.lmoesle.miravelo.processautomationexample.adapter.out.process;

import de.lmoesle.miravelo.processautomationexample.application.ports.out.AssignTaskOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.TasklistRepositoryOutPort;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.OffeneBenutzeraufgabe;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.Application;
import org.kie.kogito.auth.SecurityPolicy;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstances;
import org.kie.kogito.process.Processes;
import org.kie.kogito.uow.UnitOfWork;
import org.kie.kogito.uow.UnitOfWorkManager;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KogitoTasklistAdapterTest {

    private Processes processes;
    private Application application;
    private TasklistRepositoryOutPort tasklistRepositoryOutPort;
    private AssignTaskOutPort assignTaskOutPort;
    private UnitOfWork unitOfWork;
    private ProcessInstance<?> processInstance;
    private OffeneBenutzeraufgabe aufgabe;
    private KogitoTasklistAdapter adapter;

    @BeforeEach
    @SuppressWarnings({"rawtypes", "unchecked"})
    void setUp() {
        processes = mock(Processes.class);
        application = mock(Application.class);
        tasklistRepositoryOutPort = mock(TasklistRepositoryOutPort.class);
        assignTaskOutPort = mock(AssignTaskOutPort.class);
        unitOfWork = mock(UnitOfWork.class);
        processInstance = mock(ProcessInstance.class);
        final UnitOfWorkManager unitOfWorkManager = mock(UnitOfWorkManager.class);
        final Process process = mock(Process.class);
        final ProcessInstances processInstances = mock(ProcessInstances.class);
        when(application.unitOfWorkManager()).thenReturn(unitOfWorkManager);
        when(unitOfWorkManager.newUnitOfWork()).thenReturn(unitOfWork);
        when(processes.processById(KogitoUrlaubsantragGenehmigungsprozessAdapter.PROCESS_ID)).thenReturn(process);
        when(process.instances()).thenReturn(processInstances);
        when(processInstances.findById(UrlaubsantragTestData.PROCESS_INSTANCE_ID_VALUE))
            .thenReturn(Optional.of(processInstance));
        aufgabe = UserTaskTestdaten.offeneBenutzeraufgabe();
        when(tasklistRepositoryOutPort.findeOffeneAufgabe(UserTaskTestdaten.taskId()))
            .thenReturn(Optional.of(aufgabe));
        adapter = new KogitoTasklistAdapter(
            processes,
            application,
            tasklistRepositoryOutPort,
            assignTaskOutPort
        );
    }

    @Test
    void assignsTaskBeforeCompletingKogitoWorkItem() {
        adapter.completeTask(UserTaskTestdaten.taskId(), BenutzerTestdaten.adaId(), true);

        final var inOrder = inOrder(assignTaskOutPort, tasklistRepositoryOutPort, processInstance);
        inOrder.verify(assignTaskOutPort).assignTaskToUser(
            UserTaskTestdaten.taskId(),
            BenutzerTestdaten.adaId()
        );
        inOrder.verify(tasklistRepositoryOutPort).findeOffeneAufgabe(UserTaskTestdaten.taskId());
        inOrder.verify(processInstance).completeWorkItem(
            eq(UserTaskTestdaten.TASK_ID),
            eq(Map.of("genehmigt", true, "ActorId", BenutzerTestdaten.ADA_UUID.toString())),
            any(SecurityPolicy.class)
        );
        verify(unitOfWork).start();
        verify(unitOfWork).end();
    }

    @Test
    void wrapsKogitoErrorsAndAbortsUnitOfWork() {
        doThrow(new RuntimeException("boom")).when(processInstance).completeWorkItem(
            eq(UserTaskTestdaten.TASK_ID),
            eq(Map.of("genehmigt", false, "ActorId", BenutzerTestdaten.ADA_UUID.toString())),
            any(SecurityPolicy.class)
        );

        assertThatThrownBy(() -> adapter.completeTask(
            UserTaskTestdaten.taskId(),
            BenutzerTestdaten.adaId(),
            false
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Aufgabe " + UserTaskTestdaten.TASK_ID + " konnte nicht abgeschlossen werden")
            .hasRootCauseMessage("boom");
        verify(unitOfWork).abort();
    }
}
