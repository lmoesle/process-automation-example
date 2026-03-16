package de.lmoesle.processautomationexample.domain.vacationrequest;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.util.Assert;

@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
@ToString
public final class ProcessInstanceId {

    private final String value;

    private ProcessInstanceId(String value) {
        Assert.hasText(value, "value must not be blank");
        this.value = value;
    }

    public static ProcessInstanceId of(String value) {
        return new ProcessInstanceId(value);
    }
}
