package de.lmoesle.processautomationexample.adapter.out.mail;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@Getter
@Setter
@ConfigurationProperties(prefix = "de.lmoesle.processautomationexample.mail")
public class EmailBenachrichtigungProperties {

    @NotBlank
    private String fromAddress;

    @NotNull
    private Resource standardTemplate;

    @NotBlank
    private String frontendBaseUrl;
}
