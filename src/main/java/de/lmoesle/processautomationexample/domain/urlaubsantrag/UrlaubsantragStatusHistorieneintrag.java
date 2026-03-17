package de.lmoesle.processautomationexample.domain.urlaubsantrag;

import org.springframework.util.Assert;

public record UrlaubsantragStatusHistorieneintrag(
    UrlaubsantragStatus status,
    String kommentar
) {

    public UrlaubsantragStatusHistorieneintrag {
        Assert.notNull(status, "status darf nicht null sein");
        if (kommentar != null) {
            Assert.hasText(kommentar, "kommentar darf nicht leer sein");
        }
    }

    public static UrlaubsantragStatusHistorieneintrag ohneKommentar(UrlaubsantragStatus status) {
        return new UrlaubsantragStatusHistorieneintrag(status, null);
    }
}
