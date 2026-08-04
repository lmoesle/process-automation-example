package de.lmoesle.miravelo.processautomationexample.domain.tasklist;

import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.ProzessinstanzId;
import org.springframework.util.Assert;

import java.util.List;

public record OffeneBenutzeraufgabe(
    UserTaskId id,
    String name,
    ProzessinstanzId prozessinstanzId,
    String businessKey,
    List<BenutzerId> candidateUserIds,
    BenutzerId bearbeiterId
) {

    public OffeneBenutzeraufgabe {
        Assert.notNull(id, "id darf nicht null sein");
        Assert.hasText(name, "name darf nicht leer sein");
        Assert.notNull(prozessinstanzId, "prozessinstanzId darf nicht null sein");
        Assert.hasText(businessKey, "businessKey darf nicht leer sein");
        Assert.notNull(candidateUserIds, "candidateUserIds duerfen nicht null sein");
        candidateUserIds = List.copyOf(candidateUserIds);
    }
}
