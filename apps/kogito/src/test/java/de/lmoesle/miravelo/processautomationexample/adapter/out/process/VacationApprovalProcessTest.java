package de.lmoesle.miravelo.processautomationexample.adapter.out.process;

import de.lmoesle.miravelo.processautomationexample.adapter.in.process.AutomatischePruefungWorkItemHandler;
import de.lmoesle.miravelo.processautomationexample.application.ports.in.UrlaubsantragAutomatischPruefenInPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.in.UrlaubsantragAutomatischPruefenInPort.UrlaubsantragAutomatischPruefenCommand;
import de.lmoesle.miravelo.processautomationexample.bpmn.Vacation_approvalProcess;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import org.junit.jupiter.api.Test;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.impl.DefaultWorkItemHandlerConfig;
import org.kie.kogito.process.workitems.impl.DefaultKogitoWorkItemHandler;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VacationApprovalProcessTest {

    @Test
    void createsManagerTaskAndCompletesApprovedRequest() {
        final var process = processWithAutomaticCheckResult(true);
        try {
            final var processInstance = startProcess(process);

            assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
            assertThat(processInstance.workItems()).singleElement().satisfies(workItem -> {
                assertThat(workItem.getName()).isEqualTo("Vorgesetztenentscheidung");
                assertThat(workItem.getParameters())
                    .containsEntry("TaskName", "Vorgesetztenentscheidung")
                    .containsEntry("ActorId", BenutzerTestdaten.ADA_UUID.toString());
                processInstance.completeWorkItem(workItem.getId(), Map.of("genehmigt", true));
            });
            assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
            assertThat(processInstance.variables().getGenehmigt()).isTrue();
        } finally {
            process.deactivate();
        }
    }

    @Test
    void rejectsAutomaticallyInvalidRequestWithoutManagerTask() {
        final var process = processWithAutomaticCheckResult(false);
        try {
            final var processInstance = startProcess(process);

            assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
            assertThat(processInstance.workItems()).isEmpty();
            assertThat(processInstance.variables().getGueltig()).isFalse();
        } finally {
            process.deactivate();
        }
    }

    private Vacation_approvalProcess processWithAutomaticCheckResult(boolean gueltig) {
        final UrlaubsantragAutomatischPruefenInPort inPort = mock(UrlaubsantragAutomatischPruefenInPort.class);
        when(inPort.pruefeUrlaubsantragAutomatisch(
            new UrlaubsantragAutomatischPruefenCommand(UrlaubsantragTestData.urlaubsantragId())
        )).thenReturn(gueltig);
        final var handler = new AutomatischePruefungWorkItemHandler(inPort);
        final var handlerConfig = new DefaultWorkItemHandlerConfig();
        handlerConfig.register(handler.getName(), handler);
        handlerConfig.register("Human Task", new DefaultKogitoWorkItemHandler());
        final var configBean = new org.kie.kogito.app.ConfigBean();
        final var processConfig = new org.kie.kogito.app.ProcessConfig(
            java.util.List.of(handlerConfig),
            java.util.List.of(),
            java.util.List.of(),
            java.util.List.of(),
            java.util.List.of(),
            java.util.List.of(),
            configBean,
            java.util.List.of(),
            java.util.List.of(),
            java.util.List.of(),
            java.util.List.of()
        );
        final var applicationConfig = new org.kie.kogito.app.ApplicationConfig(
            java.util.List.of(configBean, processConfig)
        );
        final var application = new org.kie.kogito.app.Application(applicationConfig, java.util.List.of());
        return new Vacation_approvalProcess(application, null, null);
    }

    private de.lmoesle.miravelo.processautomationexample.bpmn.Vacation_approvalProcessInstance startProcess(
        Vacation_approvalProcess process
    ) {
        final var model = process.createModel();
        model.setUrlaubsantragId(UrlaubsantragTestData.VACATION_REQUEST_UUID.toString());
        model.setTeamLeadIds(BenutzerTestdaten.ADA_UUID.toString());
        final var processInstance = process.createInstance(
            UrlaubsantragTestData.VACATION_REQUEST_UUID.toString(),
            model
        );
        processInstance.start();
        return processInstance;
    }
}
