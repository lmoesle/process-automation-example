package de.lmoesle.processautomationexample.domain.urlaubsantrag;

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
public final class UrlaubsantragId {

    private final UUID value;

    private UrlaubsantragId(UUID value) {
        Assert.notNull(value, "wert darf nicht null sein");
        this.value = value;
    }

    public static UrlaubsantragId neu() {
        return new UrlaubsantragId(UUID.randomUUID());
    }

    public static UrlaubsantragId newId() {
        return neu();
    }

    public static UrlaubsantragId of(UUID value) {
        return new UrlaubsantragId(value);
    }

    public static UrlaubsantragId of(String value) {
        return new UrlaubsantragId(UUID.fromString(value));
    }
}
