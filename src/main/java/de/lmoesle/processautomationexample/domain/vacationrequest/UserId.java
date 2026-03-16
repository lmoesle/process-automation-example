package de.lmoesle.processautomationexample.domain.vacationrequest;

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
public final class UserId {

    private final UUID value;

    private UserId(UUID value) {
        Assert.notNull(value, "value must not be null");
        this.value = value;
    }

    public static UserId of(UUID value) {
        return new UserId(value);
    }
}
