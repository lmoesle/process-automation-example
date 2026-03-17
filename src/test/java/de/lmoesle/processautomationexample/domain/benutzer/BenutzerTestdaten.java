package de.lmoesle.processautomationexample.domain.benutzer;

import java.util.List;
import java.util.UUID;

public final class BenutzerTestdaten {

    public static final UUID ADA_UUID = UUID.fromString("2d88b39b-e7b0-4a3f-b9c6-b3d8e6fbe100");
    public static final UUID CARLA_UUID = UUID.fromString("f9821988-db4f-4daa-9414-6cc5227f7102");
    public static final UUID ENGINEERING_TEAM_UUID = UUID.fromString("c9d0c7dc-3ed5-4877-95e3-df8c8af1f201");
    public static final UUID PLATFORM_TEAM_UUID = UUID.fromString("57bc8807-59f4-44dc-9056-740678242202");
    public static final String ENGINEERING_TEAM = "Engineering";
    public static final String PLATFORM_TEAM = "Platform";

    private BenutzerTestdaten() {
    }

    public static BenutzerId adaId() {
        return BenutzerId.of(ADA_UUID);
    }

    public static BenutzerId carlaId() {
        return BenutzerId.of(CARLA_UUID);
    }

    public static TeamId engineeringTeamId() {
        return TeamId.of(ENGINEERING_TEAM_UUID);
    }

    public static TeamId platformTeamId() {
        return TeamId.of(PLATFORM_TEAM_UUID);
    }

    public static Team engineeringLeadTeam() {
        return new Team(ENGINEERING_TEAM, TeamRolle.LEITUNG);
    }

    public static Team platformUserTeam() {
        return new Team(PLATFORM_TEAM, TeamRolle.MITGLIED);
    }

    public static Team platformLeadTeam() {
        return new Team(PLATFORM_TEAM, TeamRolle.LEITUNG);
    }

    public static Benutzer ada() {
        return Benutzer.rekonstituiere(
            adaId(),
            "Ada Lovelace",
            "ada.lovelace@example.com",
            List.of(engineeringLeadTeam(), platformUserTeam())
        );
    }

    public static Benutzer carla() {
        return Benutzer.rekonstituiere(
            carlaId(),
            "Carla Gomez",
            "carla.gomez@example.com",
            List.of(platformLeadTeam())
        );
    }
}
