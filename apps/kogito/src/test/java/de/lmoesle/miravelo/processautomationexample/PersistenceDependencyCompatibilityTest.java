package de.lmoesle.miravelo.processautomationexample;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class PersistenceDependencyCompatibilityTest {

    @Test
    void providesHibernateApiRequiredBySpringDataJpa() {
        assertThatCode(() -> Class.forName("org.hibernate.query.BindableType"))
            .doesNotThrowAnyException();
    }

    @Test
    void usesJakartaPersistenceApiSupportedBySpringBoot() {
        assertThat(EntityManager.class.getPackage().getImplementationVersion()).isEqualTo("3.1.0");
    }
}
