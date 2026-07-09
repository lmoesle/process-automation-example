package de.lmoesle.miravelo.processautomationexample.adapter.out.persistence;

import de.lmoesle.miravelo.processautomationexample.application.ports.out.AktiveBenutzeraufgabenOutPort;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Component
@RequiredArgsConstructor
public class AktiveBenutzeraufgabePersistenceAdapter implements AktiveBenutzeraufgabenOutPort {

    private final AktiveBenutzeraufgabeJpaRepository aktiveBenutzeraufgabeJpaRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean speichereWennNeu(UserTaskId taskId) {
        Assert.notNull(taskId, "taskId darf nicht null sein");

        return aktiveBenutzeraufgabeJpaRepository.insertiereWennNichtVorhanden(taskId.value()) == 1;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void entferne(UserTaskId taskId) {
        Assert.notNull(taskId, "taskId darf nicht null sein");

        aktiveBenutzeraufgabeJpaRepository.deleteById(taskId.value());
    }
}
