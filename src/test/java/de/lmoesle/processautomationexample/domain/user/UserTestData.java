package de.lmoesle.processautomationexample.domain.user;

import java.util.List;
import java.util.UUID;

public final class UserTestData {

    public static final UUID ADA_UUID = UUID.fromString("2d88b39b-e7b0-4a3f-b9c6-b3d8e6fbe100");
    public static final UUID CARLA_UUID = UUID.fromString("f9821988-db4f-4daa-9414-6cc5227f7102");
    public static final UUID ENGINEERING_TEAM_UUID = UUID.fromString("c9d0c7dc-3ed5-4877-95e3-df8c8af1f201");
    public static final UUID PLATFORM_TEAM_UUID = UUID.fromString("57bc8807-59f4-44dc-9056-740678242202");
    public static final String ENGINEERING_TEAM = "Engineering";
    public static final String PLATFORM_TEAM = "Platform";

    private UserTestData() {
    }

    public static UserId adaId() {
        return UserId.of(ADA_UUID);
    }

    public static UserId carlaId() {
        return UserId.of(CARLA_UUID);
    }

    public static TeamId engineeringTeamId() {
        return TeamId.of(ENGINEERING_TEAM_UUID);
    }

    public static TeamId platformTeamId() {
        return TeamId.of(PLATFORM_TEAM_UUID);
    }

    public static Team engineeringLeadTeam() {
        return new Team(ENGINEERING_TEAM, TeamRole.LEAD);
    }

    public static Team platformUserTeam() {
        return new Team(PLATFORM_TEAM, TeamRole.USER);
    }

    public static Team platformLeadTeam() {
        return new Team(PLATFORM_TEAM, TeamRole.LEAD);
    }

    public static User ada() {
        return User.reconstitute(
            adaId(),
            "Ada Lovelace",
            "ada.lovelace@example.com",
            List.of(engineeringLeadTeam(), platformUserTeam())
        );
    }

    public static User carla() {
        return User.reconstitute(
            carlaId(),
            "Carla Gomez",
            "carla.gomez@example.com",
            List.of(platformLeadTeam())
        );
    }
}
