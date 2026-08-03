package de.lmoesle.miravelo.processautomationexample.adapter.in.process;

import org.kie.kogito.process.WorkItemHandlerConfig;
import org.kie.kogito.process.impl.DefaultWorkItemHandlerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class KogitoProcessConfiguration {

    @Bean
    WorkItemHandlerConfig automatischePruefungWorkItemHandlerConfig(
        AutomatischePruefungWorkItemHandler handler
    ) {
        final var config = new DefaultWorkItemHandlerConfig();
        config.register(handler.getName(), handler);
        return config;
    }
}
