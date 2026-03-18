package de.lmoesle.processautomationexample.domain.urlaubsantrag;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.util.Assert;

@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
@ToString
public final class ProzessinstanzId {

    private final String value;

    private ProzessinstanzId(String value) {
        Assert.hasText(value, "wert darf nicht leer sein");
        this.value = value;
    }

    public static ProzessinstanzId of(String value) {
        return new ProzessinstanzId(value);
    }
}
