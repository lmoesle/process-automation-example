package de.lmoesle.processautomationexample.domain.benutzer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.util.Assert;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
@ToString
public final class TeamId {

    private final UUID value;

    private TeamId(UUID value) {
        Assert.notNull(value, "wert darf nicht null sein");
        this.value = value;
    }

    public static TeamId of(UUID value) {
        return new TeamId(value);
    }
}
