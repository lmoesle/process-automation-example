CREATE TABLE prozess_engine_outbox_auftraege
(
    id                    UUID PRIMARY KEY,
    typ                   VARCHAR(64)              NOT NULL,
    status                VARCHAR(32)              NOT NULL,
    urlaubsantrag_id      UUID,
    prozessinstanz_id     VARCHAR(255),
    task_id               VARCHAR(255),
    benutzer_id           UUID,
    team_lead_ids         VARCHAR(2000),
    genehmigt             BOOLEAN,
    versuche              INTEGER                  NOT NULL,
    erstellt_am           TIMESTAMP WITH TIME ZONE NOT NULL,
    zuletzt_geaendert_am  TIMESTAMP WITH TIME ZONE NOT NULL,
    naechster_versuch_am  TIMESTAMP WITH TIME ZONE NOT NULL,
    abgeschlossen_am      TIMESTAMP WITH TIME ZONE,
    letzte_fehlermeldung  TEXT
);

CREATE INDEX idx_prozess_engine_outbox_faellige_auftraege
    ON prozess_engine_outbox_auftraege (status, naechster_versuch_am, erstellt_am);
