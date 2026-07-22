package de.lmoesle.miravelo.processautomationexample.adapter.out.persistence;

import de.lmoesle.miravelo.processautomationexample.adapter.out.persistence.entities.UrlaubsantragEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UrlaubsantragJpaRepository extends JpaRepository<UrlaubsantragEntity, UUID> {

    List<UrlaubsantragEntity> findAllByAntragstellerId(UUID antragstellerId, Sort sort);

    @Modifying
    @Query("""
        update UrlaubsantragEntity urlaubsantrag
        set urlaubsantrag.prozessinstanzId = :prozessinstanzId
        where urlaubsantrag.id = :urlaubsantragId
          and urlaubsantrag.prozessinstanzId is null
        """)
    int setzeProzessinstanzIdWennLeer(
        @Param("urlaubsantragId") UUID urlaubsantragId,
        @Param("prozessinstanzId") String prozessinstanzId
    );
}
