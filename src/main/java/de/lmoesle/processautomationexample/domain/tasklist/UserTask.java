package de.lmoesle.processautomationexample.domain.tasklist;

import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
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
        candidateUsers = candidateUsers;
    }
}
