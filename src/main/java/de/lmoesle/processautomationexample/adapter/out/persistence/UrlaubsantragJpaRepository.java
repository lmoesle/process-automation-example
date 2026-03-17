package de.lmoesle.processautomationexample.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface UrlaubsantragJpaRepository extends JpaRepository<UrlaubsantragEntity, UUID> {

    List<UrlaubsantragEntity> findAllByAntragstellerId(UUID antragstellerId, Sort sort);
}
