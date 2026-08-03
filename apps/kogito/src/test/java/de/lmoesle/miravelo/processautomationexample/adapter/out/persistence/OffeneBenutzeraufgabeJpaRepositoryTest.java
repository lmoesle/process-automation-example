package de.lmoesle.miravelo.processautomationexample.adapter.out.persistence;

import de.lmoesle.miravelo.processautomationexample.adapter.out.persistence.entities.OffeneBenutzeraufgabeEntity;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
    "spring.autoconfigure.exclude=",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class OffeneBenutzeraufgabeJpaRepositoryTest {

    @Autowired
    private OffeneBenutzeraufgabeJpaRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        repository.saveAndFlush(new OffeneBenutzeraufgabeEntity(
            UserTaskTestdaten.TASK_ID,
            null,
            UserTaskTestdaten.TASK_NAME,
            UrlaubsantragTestData.PROCESS_INSTANCE_ID_VALUE,
            UserTaskTestdaten.BUSINESS_KEY,
            new LinkedHashSet<>(List.of(BenutzerTestdaten.ADA_UUID, BenutzerTestdaten.CARLA_UUID))
        ));
        entityManager.clear();
    }

    @Test
    void assignsOnlyTheFirstUserAtomically() {
        assertThat(repository.weiseZuWennNichtZugewiesen(
            UserTaskTestdaten.TASK_ID,
            BenutzerTestdaten.ADA_UUID
        )).isOne();
        assertThat(repository.weiseZuWennNichtZugewiesen(
            UserTaskTestdaten.TASK_ID,
            BenutzerTestdaten.CARLA_UUID
        )).isZero();

        entityManager.clear();
        assertThat(repository.findById(UserTaskTestdaten.TASK_ID).orElseThrow().getAssignee())
            .isEqualTo(BenutzerTestdaten.ADA_UUID);
    }

    @Test
    void hidesAssignedTaskFromOtherCandidates() {
        assertThat(repository.findVisibleById(UserTaskTestdaten.TASK_ID, BenutzerTestdaten.CARLA_UUID))
            .isPresent();

        repository.weiseZuWennNichtZugewiesen(UserTaskTestdaten.TASK_ID, BenutzerTestdaten.ADA_UUID);
        entityManager.clear();

        assertThat(repository.findVisibleById(UserTaskTestdaten.TASK_ID, BenutzerTestdaten.ADA_UUID))
            .isPresent();
        assertThat(repository.findVisibleById(UserTaskTestdaten.TASK_ID, BenutzerTestdaten.CARLA_UUID))
            .isEmpty();
    }
}
