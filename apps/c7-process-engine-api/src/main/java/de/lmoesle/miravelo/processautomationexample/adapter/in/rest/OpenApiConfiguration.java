package de.lmoesle.miravelo.processautomationexample.adapter.in.rest;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Miravelo Urlaubsantrag API",
        version = "v1",
        description = "REST-API fuer Urlaubsantraege und den dazugehoerigen BPMN-Genehmigungsworkflow.",
        contact = @Contact(name = "Miravelo")
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
