package de.lmoesle.processautomationexample.adapter.out.persistence;

import de.lmoesle.processautomationexample.adapter.out.persistence.entities.UrlaubsantragEntity;
import de.lmoesle.processautomationexample.adapter.out.persistence.entities.UrlaubsantragStatusHistorieneintragEmbeddable;
import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragId;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragStatusHistorieneintrag;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.Urlaubszeitraum;

import java.util.ArrayList;

public final class UrlaubsantragPersistenceMapper {

    private UrlaubsantragPersistenceMapper() {
    }

    public static UrlaubsantragEntity toEntity(Urlaubsantrag urlaubsantrag) {
        return new UrlaubsantragEntity(
            urlaubsantrag.id().value(),
            urlaubsantrag.zeitraum().von(),
            urlaubsantrag.zeitraum().bis(),
            urlaubsantrag.antragsteller().id().value(),
            urlaubsantrag.vertretung() == null ? null : urlaubsantrag.vertretung().id().value(),
            urlaubsantrag.vorgesetzter() == null ? null : urlaubsantrag.vorgesetzter().id().value(),
            urlaubsantrag.prozessinstanzId() == null ? null : urlaubsantrag.prozessinstanzId().value(),
            urlaubsantrag.status(),
            new ArrayList<>(urlaubsantrag.statusHistorie().stream()
                .map(statusHistorienEintrag -> new UrlaubsantragStatusHistorieneintragEmbeddable(
                    statusHistorienEintrag.status(),
                    statusHistorienEintrag.kommentar()
                ))
                .toList())
        );
    }

    public static Urlaubsantrag toDomain(
        UrlaubsantragEntity urlaubsantragEntity,
        Benutzer antragsteller,
        Benutzer vertretung,
        Benutzer vorgesetzter
    ) {
        return new Urlaubsantrag(
            UrlaubsantragId.of(urlaubsantragEntity.getId()),
            Urlaubszeitraum.of(urlaubsantragEntity.getVon(), urlaubsantragEntity.getBis()),
            antragsteller,
            vertretung,
            vorgesetzter,
            urlaubsantragEntity.getStatus(),
            urlaubsantragEntity.getStatusHistorie().stream()
                .map(statusHistorienEintrag -> new UrlaubsantragStatusHistorieneintrag(
                    statusHistorienEintrag.getStatus(),
                    statusHistorienEintrag.getKommentar()
                ))
                .toList(),
            urlaubsantragEntity.getProzessinstanzId() == null ? null : de.lmoesle.processautomationexample.domain.urlaubsantrag.ProzessinstanzId.of(
                urlaubsantragEntity.getProzessinstanzId()
            )
        );
    }
}
