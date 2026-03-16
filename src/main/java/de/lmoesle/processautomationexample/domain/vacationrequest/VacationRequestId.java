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
public final class VacationRequestId {

    private final UUID value;

    private VacationRequestId(UUID value) {
        Assert.notNull(value, "value must not be null");
        this.value = value;
    }

    public static VacationRequestId newId() {
        return new VacationRequestId(UUID.randomUUID());
    }

    public static VacationRequestId of(UUID value) {
        return new VacationRequestId(value);
    }
}
