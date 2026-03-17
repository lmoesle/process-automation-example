package de.lmoesle.processautomationexample.domain.vacationrequest;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.util.Assert;

import java.time.LocalDate;

@Getter
@Accessors(fluent = true)
public final class VacationPeriod {

    private final LocalDate from;
    private final LocalDate to;

    private VacationPeriod(LocalDate from, LocalDate to) {
        Assert.notNull(from, "from must not be null");
        Assert.notNull(to, "to must not be null");
        Assert.isTrue(!from.isAfter(to), "'from' must be on or before 'to'.");
        this.from = from;
        this.to = to;
    }

    public static VacationPeriod of(LocalDate from, LocalDate to) {
        return new VacationPeriod(from, to);
    }

    public boolean overlaps(VacationPeriod other) {
        Assert.notNull(other, "other must not be null");
        return !from.isAfter(other.to) && !other.from.isAfter(to);
    }
}
