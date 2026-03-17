CREATE TABLE benutzer
(
    id    UUID PRIMARY KEY,
    name  VARCHAR(255) NOT NULL,
    email VARCHAR(320) NOT NULL UNIQUE
);

CREATE TABLE teams
(
    id   UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE team_mitgliedschaften
(
    team_id      UUID        NOT NULL REFERENCES teams (id),
    benutzer_id  UUID        NOT NULL REFERENCES benutzer (id),
    rolle        VARCHAR(32) NOT NULL,
    PRIMARY KEY (team_id, benutzer_id),
    CONSTRAINT chk_team_mitgliedschaften_rolle CHECK (rolle IN ('MITGLIED', 'LEITUNG'))
);

CREATE INDEX idx_team_mitgliedschaften_benutzer_id ON team_mitgliedschaften (benutzer_id);
CREATE INDEX idx_team_mitgliedschaften_team_rolle ON team_mitgliedschaften (team_id, rolle);
