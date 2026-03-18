package de.lmoesle.processautomationexample.domain.benutzer;

import org.springframework.util.Assert;

public record Team(TeamId id, String name, TeamRolle rolle) {

    public Team {
        Assert.notNull(id, "id darf nicht null sein");
        Assert.hasText(name, "name darf nicht leer sein");
        Assert.notNull(rolle, "rolle darf nicht null sein");
    }
}
