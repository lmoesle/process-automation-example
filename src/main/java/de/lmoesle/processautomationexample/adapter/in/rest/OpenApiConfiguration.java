package de.lmoesle.processautomationexample.adapter.in.rest;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Process Automation Example API",
        version = "v1",
        description = "REST API for vacation requests and the associated BPMN approval workflow.",
        contact = @Contact(name = "Process Automation Example")
    ),
    tags = {
        @Tag(
            name = "Vacation Requests",
            description = "Create vacation requests and start the vacation approval process."
        )
    }
)
public class OpenApiConfiguration {
}
