CREATE TABLE offene_benutzeraufgaben
(
    task_id             VARCHAR(255) PRIMARY KEY,
    assignee            UUID REFERENCES benutzer (id),
    task_name           VARCHAR(255) NOT NULL,
    prozessinstanz_id   VARCHAR(255) NOT NULL,
    business_key        VARCHAR(255) NOT NULL
);

CREATE TABLE offene_benutzeraufgaben_kandidaten
(
    task_id       VARCHAR(255) NOT NULL REFERENCES offene_benutzeraufgaben (task_id) ON DELETE CASCADE,
    benutzer_id   UUID         NOT NULL REFERENCES benutzer (id),
    PRIMARY KEY (task_id, benutzer_id)
);

CREATE INDEX idx_offene_benutzeraufgaben_assignee
    ON offene_benutzeraufgaben (assignee);
CREATE INDEX idx_offene_benutzeraufgaben_kandidaten_benutzer
    ON offene_benutzeraufgaben_kandidaten (benutzer_id);
