package de.lmoesle.processautomationexample.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VacationRequestJpaRepository extends JpaRepository<VacationRequestEntity, UUID> {
}
