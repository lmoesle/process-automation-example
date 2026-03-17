package de.lmoesle.processautomationexample.domain.tasklist;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.util.Assert;

@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
@ToString
public final class UserTaskId {

    private final String value;

    private UserTaskId(String value) {
        Assert.hasText(value, "wert darf nicht leer sein");
        this.value = value;
    }

    public static UserTaskId of(String value) {
        return new UserTaskId(value);
    }
}
