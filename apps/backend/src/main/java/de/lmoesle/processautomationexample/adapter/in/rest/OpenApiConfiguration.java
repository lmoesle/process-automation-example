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
        description = "REST-API fuer Urlaubsantraege und den dazugehoerigen BPMN-Genehmigungsworkflow.",
        contact = @Contact(name = "Process Automation Example")
    ),
    tags = {
        @Tag(
            name = "Urlaubsanträge",
            description = "Urlaubsantraege anlegen und den Genehmigungsprozess starten."
        )
    }
)
public class OpenApiConfiguration {
}
