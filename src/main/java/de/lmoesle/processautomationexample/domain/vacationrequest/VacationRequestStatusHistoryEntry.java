package de.lmoesle.processautomationexample.domain.vacationrequest;

import org.springframework.util.Assert;

public record VacationRequestStatusHistoryEntry(
    VacationRequestStatus status,
    String comment
) {

    public VacationRequestStatusHistoryEntry {
        Assert.notNull(status, "status must not be null");
        if (comment != null) {
            Assert.hasText(comment, "comment must not be blank");
        }
    }

    public static VacationRequestStatusHistoryEntry withoutComment(VacationRequestStatus status) {
        return new VacationRequestStatusHistoryEntry(status, null);
    }
}
