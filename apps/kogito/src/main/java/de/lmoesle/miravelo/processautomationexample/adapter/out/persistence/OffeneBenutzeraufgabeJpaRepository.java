package de.lmoesle.miravelo.processautomationexample.adapter.out.persistence;

import de.lmoesle.miravelo.processautomationexample.adapter.out.persistence.entities.OffeneBenutzeraufgabeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OffeneBenutzeraufgabeJpaRepository extends JpaRepository<OffeneBenutzeraufgabeEntity, String> {

    @Modifying
    @Query(
        value = """
            INSERT INTO offene_benutzeraufgaben (
                task_id, assignee, task_name, prozessinstanz_id, business_key
            ) VALUES (
                :taskId, :assignee, :taskName, :prozessinstanzId, :businessKey
            ) ON CONFLICT DO NOTHING
            """,
        nativeQuery = true
    )
    int insertiereWennNichtVorhanden(
        @Param("taskId") String taskId,
        @Param("assignee") UUID assignee,
        @Param("taskName") String taskName,
        @Param("prozessinstanzId") String prozessinstanzId,
        @Param("businessKey") String businessKey
    );

    @Modifying(flushAutomatically = true)
    @Query(
        value = """
            UPDATE offene_benutzeraufgaben
            SET assignee = :benutzerId
            WHERE task_id = :taskId
              AND (assignee IS NULL OR assignee = :benutzerId)
            """,
        nativeQuery = true
    )
    int weiseZuWennNichtZugewiesen(
        @Param("taskId") String taskId,
        @Param("benutzerId") UUID benutzerId
    );

    @Query("""
        SELECT DISTINCT task
        FROM OffeneBenutzeraufgabeEntity task
        LEFT JOIN task.candidateUserIds candidateUserId
        WHERE task.assignee = :benutzerId
           OR (task.assignee IS NULL AND candidateUserId = :benutzerId)
        """)
    List<OffeneBenutzeraufgabeEntity> findAllVisibleFor(@Param("benutzerId") UUID benutzerId);

    @Query("""
        SELECT DISTINCT task
        FROM OffeneBenutzeraufgabeEntity task
        LEFT JOIN task.candidateUserIds candidateUserId
        WHERE task.taskId = :taskId
          AND (
              task.assignee = :benutzerId
              OR (task.assignee IS NULL AND candidateUserId = :benutzerId)
          )
        """)
    Optional<OffeneBenutzeraufgabeEntity> findVisibleById(
        @Param("taskId") String taskId,
        @Param("benutzerId") UUID benutzerId
    );
}
