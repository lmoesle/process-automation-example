package de.lmoesle.processautomationexample.adapter.out.persistence;

import de.lmoesle.processautomationexample.adapter.out.persistence.entities.AktiveBenutzeraufgabeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AktiveBenutzeraufgabeJpaRepository extends JpaRepository<AktiveBenutzeraufgabeEntity, String> {

    @Modifying
    @Query(
        value = "INSERT INTO aktive_benutzeraufgaben (task_id) VALUES (:taskId) ON CONFLICT DO NOTHING",
        nativeQuery = true
    )
    int insertiereWennNichtVorhanden(@Param("taskId") String taskId);
}
