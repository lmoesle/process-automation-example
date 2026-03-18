package de.lmoesle.processautomationexample.shared.tasklist;

import de.lmoesle.processautomationexample.adapter.in.process.BenutzeraufgabenBenachrichtigungTaskHandler;
import dev.bpmcrafters.processengineapi.CommonRestrictions;
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi;
import dev.bpmcrafters.processengineapi.task.support.UserTaskSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserTaskSupportConfiguration {

    @Bean
    public UserTaskSupport createAndRegisterUserTaskSupport(
        TaskSubscriptionApi taskSubscriptionApi,
        BenutzeraufgabenBenachrichtigungTaskHandler benutzeraufgabenBenachrichtigungTaskHandler
    ) {
        UserTaskSupport support = new UserTaskSupport();
        support.addHandler(benutzeraufgabenBenachrichtigungTaskHandler);
        support.subscribe(
                taskSubscriptionApi,
                CommonRestrictions.builder().build(),
                null,
                null
        );
        return support;
    }
}
