package de.lmoesle.miravelo.processautomationexample.adapter.in.process;

import de.lmoesle.miravelo.processautomationexample.application.ports.in.UrlaubsantragAutomatischPruefenInPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.in.UrlaubsantragAutomatischPruefenInPort.UrlaubsantragAutomatischPruefenCommand;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.kie.kogito.internal.process.workitem.KogitoWorkItemHandler;
import org.kie.kogito.internal.process.workitem.KogitoWorkItemManager;
import org.kie.kogito.internal.process.workitem.WorkItemTransition;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AutomatischePruefungWorkItemHandlerTest {

    private UrlaubsantragAutomatischPruefenInPort pruefeUrlaubsantragAutomatischInPort;
    private AutomatischePruefungWorkItemHandler handler;

    @BeforeEach
    void setUp() {
        pruefeUrlaubsantragAutomatischInPort = mock(UrlaubsantragAutomatischPruefenInPort.class);
        handler = new AutomatischePruefungWorkItemHandler(pruefeUrlaubsantragAutomatischInPort);
    }

    @Test
    void completesWorkItemWithAutomaticCheckResult() {
        final var workItem = mock(KogitoWorkItem.class);
        final var transitionHandler = mock(KogitoWorkItemHandler.class);
        final var completion = mock(WorkItemTransition.class);
        final String urlaubsantragId = UrlaubsantragTestData.urlaubsantragId().value().toString();
        when(workItem.getParameter("urlaubsantragId")).thenReturn(urlaubsantragId);
        when(workItem.getPhaseStatus()).thenReturn("Active");
        when(pruefeUrlaubsantragAutomatischInPort.pruefeUrlaubsantragAutomatisch(
            new UrlaubsantragAutomatischPruefenCommand(UrlaubsantragTestData.urlaubsantragId())
        )).thenReturn(true);
        when(transitionHandler.completeTransition("Active", Map.of("gueltig", true))).thenReturn(completion);

        final var result = handler.activateWorkItemHandler(
            mock(KogitoWorkItemManager.class),
            transitionHandler,
            workItem,
            mock(WorkItemTransition.class)
        );

        assertThat(result).contains(completion);
        verify(pruefeUrlaubsantragAutomatischInPort).pruefeUrlaubsantragAutomatisch(
            new UrlaubsantragAutomatischPruefenCommand(UrlaubsantragTestData.urlaubsantragId())
        );
        verify(transitionHandler).completeTransition("Active", Map.of("gueltig", true));
    }

    @Test
    void rejectsMissingUrlaubsantragIdParameter() {
        final var workItem = mock(KogitoWorkItem.class);

        assertThatThrownBy(() -> handler.activateWorkItemHandler(
            mock(KogitoWorkItemManager.class),
            mock(KogitoWorkItemHandler.class),
            workItem,
            mock(WorkItemTransition.class)
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("urlaubsantragId muss ein String sein");
        verifyNoInteractions(pruefeUrlaubsantragAutomatischInPort);
    }

    @Test
    void exposesBpmnWorkItemName() {
        assertThat(handler.getName()).isEqualTo("AutomatischePruefung");
    }
}
