package de.lmoesle.miravelo.processautomationexample.adapter.out.persistence;

import de.lmoesle.miravelo.processautomationexample.adapter.out.persistence.entities.ProzessEngineOutboxAuftragEntity;
import de.lmoesle.miravelo.processautomationexample.adapter.out.process.ProzessEngineOutboxAuftragStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProzessEngineOutboxAuftragJpaRepository extends JpaRepository<ProzessEngineOutboxAuftragEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select auftrag from ProzessEngineOutboxAuftragEntity auftrag
        where auftrag.status in :status
          and auftrag.naechsterVersuchAm <= :zeitpunkt
          and auftrag.versuche < :maximaleVersuche
        order by auftrag.erstelltAm asc
        """)
    List<ProzessEngineOutboxAuftragEntity> findeFaelligeAuftraege(
        Collection<ProzessEngineOutboxAuftragStatus> status,
        Instant zeitpunkt,
        int maximaleVersuche,
        Pageable pageable
    );
}
