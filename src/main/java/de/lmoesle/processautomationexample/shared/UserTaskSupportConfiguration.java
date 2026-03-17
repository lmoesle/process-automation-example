package de.lmoesle.processautomationexample.shared;

import dev.bpmcrafters.processengineapi.CommonRestrictions;
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi;
import dev.bpmcrafters.processengineapi.task.support.UserTaskSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserTaskSupportConfiguration {

    @Bean
    public UserTaskSupport createAndRegisterUserTaskSupport(TaskSubscriptionApi taskSubscriptionApi) {
        UserTaskSupport support = new UserTaskSupport();
        support.subscribe(
                taskSubscriptionApi,
                CommonRestrictions.builder().build(),
                null,
                null
        );
        return support;
    }
}
