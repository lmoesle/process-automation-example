package de.lmoesle.processautomationexample.domain.benutzer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.util.Assert;

import java.util.List;

@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
@ToString
public final class Benutzer {

    private final BenutzerId id;
    private final String name;
    private final String email;
    private final List<Team> teams;

    private Benutzer(BenutzerId id, String name, String email, List<Team> teams) {
        Assert.notNull(id, "id darf nicht null sein");
        Assert.hasText(name, "name darf nicht leer sein");
        Assert.hasText(email, "email darf nicht leer sein");
        Assert.notNull(teams, "teams duerfen nicht null sein");
        this.id = id;
        this.name = name;
        this.email = email;
        this.teams = List.copyOf(teams);
    }

    public static Benutzer rekonstituiere(BenutzerId id, String name, String email, List<Team> teams) {
        return new Benutzer(id, name, email, teams);
    }
}
