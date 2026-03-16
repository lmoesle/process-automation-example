package de.lmoesle.processautomationexample.domain.user;

import org.springframework.util.Assert;

public record Team(String name, TeamRole role) {

    public Team {
        Assert.hasText(name, "name must not be blank");
        Assert.notNull(role, "role must not be null");
    }
}
