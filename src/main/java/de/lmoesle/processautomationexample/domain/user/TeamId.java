package de.lmoesle.processautomationexample.domain.user;

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
        Assert.notNull(value, "value must not be null");
        this.value = value;
    }

    public static TeamId of(UUID value) {
        return new TeamId(value);
    }
}
