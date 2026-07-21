package de.lmoesle.miravelo.processautomationexample.adapter.out.process;

import de.lmoesle.miravelo.processautomationexample.adapter.out.persistence.ProzessEngineOutboxAuftragJpaRepository;
import de.lmoesle.miravelo.processautomationexample.adapter.out.persistence.entities.ProzessEngineOutboxAuftragEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProzessEngineOutboxTransaktionen {

    private final ProzessEngineOutboxAuftragJpaRepository outboxAuftragJpaRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<ProzessEngineOutboxAuftragEntity> beansprucheNaechstenFaelligenAuftrag(
        Collection<ProzessEngineOutboxAuftragStatus> status,
        Instant zeitpunkt,
        int maximaleVersuche,
        Pageable pageable
    ) {
        final List<ProzessEngineOutboxAuftragEntity> auftraege = outboxAuftragJpaRepository.findeFaelligeAuftraege(
            status,
            zeitpunkt,
            maximaleVersuche,
            pageable
        );
        if (auftraege.isEmpty()) {
            return Optional.empty();
        }

        final ProzessEngineOutboxAuftragEntity auftrag = auftraege.getFirst();
        auftrag.markiereInBearbeitung(zeitpunkt);
        outboxAuftragJpaRepository.flush();
        return Optional.of(auftrag);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void speichere(ProzessEngineOutboxAuftragEntity auftrag) {
        outboxAuftragJpaRepository.saveAndFlush(auftrag);
    }
}
