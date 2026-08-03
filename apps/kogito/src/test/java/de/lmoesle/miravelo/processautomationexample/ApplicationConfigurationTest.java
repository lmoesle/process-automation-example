package de.lmoesle.miravelo.processautomationexample;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationConfigurationTest {

    @Test
    void scansKogitoRuntimeAndGeneratedComponents() {
        final var application = ProcessAutomationExampleKogitoApplication.class
            .getAnnotation(SpringBootApplication.class);

        assertThat(application.scanBasePackages()).contains("org.kie.kogito");
    }

    @Test
    void baselinesApplicationMigrationsAfterKogitoMigratesTheSharedSchema() throws IOException {
        final var properties = loadApplicationProperties();

        assertThat(properties.getProperty("spring.flyway.baseline-on-migrate")).isEqualTo(true);
    }

    @Test
    void enablesOptimisticLockingForPersistedKogitoProcesses() throws IOException {
        final var properties = loadApplicationProperties();

        assertThat(properties.getProperty("kogito.persistence.optimistic.lock")).isEqualTo(true);
    }

    private org.springframework.core.env.PropertySource<?> loadApplicationProperties() throws IOException {
        final var applicationYaml = new FileSystemResource(Path.of("src/main/resources/application.yml"));
        return new YamlPropertySourceLoader().load("application", applicationYaml).getFirst();
    }
}
