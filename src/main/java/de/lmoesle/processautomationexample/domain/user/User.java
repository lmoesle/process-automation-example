package de.lmoesle.processautomationexample.domain.user;

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
public final class User {

    private final UserId id;
    private final String name;
    private final String email;
    private final List<Team> teams;

    private User(UserId id, String name, String email, List<Team> teams) {
        Assert.notNull(id, "id must not be null");
        Assert.hasText(name, "name must not be blank");
        Assert.hasText(email, "email must not be blank");
        Assert.notNull(teams, "teams must not be null");
        this.id = id;
        this.name = name;
        this.email = email;
        this.teams = List.copyOf(teams);
    }

    public static User reconstitute(UserId id, String name, String email, List<Team> teams) {
        return new User(id, name, email, teams);
    }
}
