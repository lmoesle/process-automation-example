package de.lmoesle.miravelo.processautomationexample.adapter.out.process;

import de.lmoesle.miravelo.processautomationexample.adapter.out.persistence.entities.ProzessEngineOutboxAuftragEntity;
import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class ProzessEngineOutboxMapper {

    private ProzessEngineOutboxMapper() {
    }

    public static ProzessEngineOutboxAuftragEntity starteGenehmigungsprozess(
        Urlaubsantrag urlaubsantrag,
        List<BenutzerId> teamLeadIds,
        Instant zeitpunkt
    ) {
        return new ProzessEngineOutboxAuftragEntity(
            UUID.randomUUID(),
            ProzessEngineOutboxAuftragTyp.STARTE_GENEHMIGUNGSPROZESS,
            ProzessEngineOutboxAuftragStatus.OFFEN,
            urlaubsantrag.id().value(),
            null,
            null,
            null,
            mapTeamLeadIds(teamLeadIds),
            null,
            0,
            zeitpunkt,
            zeitpunkt,
            zeitpunkt,
            null,
            null
        );
    }

    public static ProzessEngineOutboxAuftragEntity weiseTaskZu(UserTaskId taskId, BenutzerId benutzerId, Instant zeitpunkt) {
        return new ProzessEngineOutboxAuftragEntity(
            UUID.randomUUID(),
            ProzessEngineOutboxAuftragTyp.WEISE_TASK_ZU,
            ProzessEngineOutboxAuftragStatus.OFFEN,
            null,
            null,
            taskId.value(),
            benutzerId.value(),
            null,
            null,
            0,
            zeitpunkt,
            zeitpunkt,
            zeitpunkt,
            null,
            null
        );
    }

    public static ProzessEngineOutboxAuftragEntity schliesseTaskAb(
        UserTaskId taskId,
        BenutzerId benutzerId,
        boolean genehmigt,
        Instant zeitpunkt
    ) {
        return new ProzessEngineOutboxAuftragEntity(
            UUID.randomUUID(),
            ProzessEngineOutboxAuftragTyp.SCHLIESSE_TASK_AB,
            ProzessEngineOutboxAuftragStatus.OFFEN,
            null,
            null,
            taskId.value(),
            benutzerId.value(),
            null,
            genehmigt,
            0,
            zeitpunkt,
            zeitpunkt,
            zeitpunkt,
            null,
            null
        );
    }

    public static List<BenutzerId> teamLeadIds(ProzessEngineOutboxAuftragEntity auftrag) {
        if (auftrag.getTeamLeadIds() == null || auftrag.getTeamLeadIds().isBlank()) {
            return List.of();
        }

        return Arrays.stream(auftrag.getTeamLeadIds().split(","))
            .map(UUID::fromString)
            .map(BenutzerId::of)
            .toList();
    }

    private static String mapTeamLeadIds(List<BenutzerId> teamLeadIds) {
        return teamLeadIds.stream()
            .map(benutzerId -> benutzerId.value().toString())
            .distinct()
            .reduce((left, right) -> left + "," + right)
            .orElse("");
    }
}
