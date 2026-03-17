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
public final class BenutzerId {

    private final UUID value;

    private BenutzerId(UUID value) {
        Assert.notNull(value, "wert darf nicht null sein");
        this.value = value;
    }

    public static BenutzerId of(UUID value) {
        return new BenutzerId(value);
    }
}
