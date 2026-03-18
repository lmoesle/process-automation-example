package de.lmoesle.processautomationexample.shared.tasklist;

import de.lmoesle.processautomationexample.adapter.in.process.BenutzeraufgabenBenachrichtigungTaskHandler;
import de.lmoesle.processautomationexample.application.ports.out.BenutzerRepositoryOutPort;
import de.lmoesle.processautomationexample.application.ports.out.SendeBenutzeraufgabenBenachrichtigungOutPort;
import de.lmoesle.processautomationexample.application.ports.out.UrlaubsantraegeLadenOutPort;
import de.lmoesle.processautomationexample.application.usecases.SendeBenutzeraufgabenBenachrichtigungUseCase;
import dev.bpmcrafters.processengineapi.task.TaskSubscription;
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi;
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi;
import dev.bpmcrafters.processengineapi.task.UserTaskModificationApi;
import dev.bpmcrafters.processengineapi.task.support.UserTaskSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserTaskSupportConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(TaskSubscriptionApi.class, () -> {
            TaskSubscriptionApi taskSubscriptionApi = mock(TaskSubscriptionApi.class);
            when(taskSubscriptionApi.subscribeForTask(any()))
                .thenReturn(CompletableFuture.completedFuture(mock(TaskSubscription.class)));
            return taskSubscriptionApi;
        })
        .withBean(UserTaskModificationApi.class, () -> mock(UserTaskModificationApi.class))
        .withBean(UserTaskCompletionApi.class, () -> mock(UserTaskCompletionApi.class))
        .withBean(UrlaubsantraegeLadenOutPort.class, () -> mock(UrlaubsantraegeLadenOutPort.class))
        .withBean(BenutzerRepositoryOutPort.class, () -> mock(BenutzerRepositoryOutPort.class))
        .withBean(SendeBenutzeraufgabenBenachrichtigungOutPort.class, () -> mock(SendeBenutzeraufgabenBenachrichtigungOutPort.class))
        .withUserConfiguration(
            UserTaskSupportConfiguration.class,
            BenutzeraufgabenBenachrichtigungTaskHandler.class,
            SendeBenutzeraufgabenBenachrichtigungUseCase.class,
            TasklistRepository.class
        );

    @Test
    void createsBeansWithoutCircularDependency() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(UserTaskSupport.class);
            assertThat(context).hasSingleBean(BenutzeraufgabenBenachrichtigungTaskHandler.class);
            assertThat(context).hasSingleBean(SendeBenutzeraufgabenBenachrichtigungUseCase.class);
            assertThat(context).hasSingleBean(TasklistRepository.class);
        });
    }
}
