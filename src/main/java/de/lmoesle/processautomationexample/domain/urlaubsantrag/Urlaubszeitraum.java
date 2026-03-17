package de.lmoesle.processautomationexample.domain.urlaubsantrag;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.util.Assert;

import java.time.LocalDate;

@Getter
@Accessors(fluent = true)
public final class Urlaubszeitraum {

    private final LocalDate von;
    private final LocalDate bis;

    private Urlaubszeitraum(LocalDate von, LocalDate bis) {
        Assert.notNull(von, "von darf nicht null sein");
        Assert.notNull(bis, "bis darf nicht null sein");
        Assert.isTrue(!von.isAfter(bis), "'von' muss vor oder gleich 'bis' liegen.");
        this.von = von;
        this.bis = bis;
    }

    public static Urlaubszeitraum of(LocalDate von, LocalDate bis) {
        return new Urlaubszeitraum(von, bis);
    }

    public boolean ueberschneidetSichMit(Urlaubszeitraum andererZeitraum) {
        Assert.notNull(andererZeitraum, "andererZeitraum darf nicht null sein");
        return !von.isAfter(andererZeitraum.bis) && !andererZeitraum.von.isAfter(bis);
    }
}
