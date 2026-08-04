package de.lmoesle.miravelo.processautomationexample.adapter.in.process;

import org.junit.jupiter.api.Test;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.kogito.process.ProcessEventListenerConfig;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class KogitoProcessConfigurationTest {

    private final BenutzeraufgabenBenachrichtigungProcessEventListener listener =
        mock(BenutzeraufgabenBenachrichtigungProcessEventListener.class);

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(BenutzeraufgabenBenachrichtigungProcessEventListener.class, () -> listener)
        .withBean(AutomatischePruefungWorkItemHandler.class, () -> mock(AutomatischePruefungWorkItemHandler.class))
        .withUserConfiguration(KogitoProcessConfiguration.class);

    @Test
    void registersUserTaskLifecycleListenerExactlyOnce() {
        contextRunner.run(context -> {
            final var componentListeners = context.getBeansOfType(ProcessEventListener.class).values().stream();
            final var configuredListeners = context.getBeansOfType(ProcessEventListenerConfig.class).values().stream()
                .flatMap(config -> config.listeners().stream());

            assertThat(Stream.concat(componentListeners, configuredListeners))
                .filteredOn(listener::equals)
                .hasSize(1);
        });
    }
}
