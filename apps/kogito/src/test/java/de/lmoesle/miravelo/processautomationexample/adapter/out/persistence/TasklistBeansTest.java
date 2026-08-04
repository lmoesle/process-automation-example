package de.lmoesle.miravelo.processautomationexample.adapter.out.persistence;

import de.lmoesle.miravelo.processautomationexample.adapter.in.process.BenutzeraufgabenBenachrichtigungProcessEventListener;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.BenutzerRepositoryOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.SendeBenutzeraufgabenBenachrichtigungOutPort;
import de.lmoesle.miravelo.processautomationexample.application.ports.out.UrlaubsantraegeLadenOutPort;
import de.lmoesle.miravelo.processautomationexample.application.usecases.SendeBenutzeraufgabenBenachrichtigungUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class TasklistBeansTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(OffeneBenutzeraufgabeJpaRepository.class, () -> mock(OffeneBenutzeraufgabeJpaRepository.class))
        .withBean(UrlaubsantraegeLadenOutPort.class, () -> mock(UrlaubsantraegeLadenOutPort.class))
        .withBean(BenutzerRepositoryOutPort.class, () -> mock(BenutzerRepositoryOutPort.class))
        .withBean(
            SendeBenutzeraufgabenBenachrichtigungOutPort.class,
            () -> mock(SendeBenutzeraufgabenBenachrichtigungOutPort.class)
        )
        .withUserConfiguration(
            BenutzeraufgabenBenachrichtigungProcessEventListener.class,
            SendeBenutzeraufgabenBenachrichtigungUseCase.class,
            TasklistPersistenceAdapter.class
        );

    @Test
    void createsTasklistBeansWithoutCircularDependency() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(BenutzeraufgabenBenachrichtigungProcessEventListener.class);
            assertThat(context).hasSingleBean(SendeBenutzeraufgabenBenachrichtigungUseCase.class);
            assertThat(context).hasSingleBean(TasklistPersistenceAdapter.class);
        });
    }
}
