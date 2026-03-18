package de.lmoesle.processautomationexample.domain.tasklist;

import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import org.springframework.util.Assert;

import java.util.List;

public record UserTask(
    UserTaskId id,
    Urlaubsantrag urlaubsantrag,
    List<Benutzer> candidateUsers,
    Benutzer bearbeiter
) {

    public UserTask {
        Assert.notNull(id, "id darf nicht null sein");
        Assert.notNull(candidateUsers, "candidateUsers duerfen nicht null sein");
        candidateUsers = List.copyOf(candidateUsers);
    }

    public boolean istSichtbarFuer(BenutzerId benutzerId) {
        Assert.notNull(benutzerId, "benutzerId darf nicht null sein");
        return istCandidateUser(benutzerId) || istBearbeiter(benutzerId);
    }

    public boolean istCandidateUser(BenutzerId benutzerId) {
        Assert.notNull(benutzerId, "benutzerId darf nicht null sein");
        return candidateUsers.stream()
            .map(Benutzer::id)
            .anyMatch(benutzerId::equals);
    }

    private boolean istBearbeiter(BenutzerId benutzerId) {
        return bearbeiter != null && benutzerId.equals(bearbeiter.id());
    }
}
