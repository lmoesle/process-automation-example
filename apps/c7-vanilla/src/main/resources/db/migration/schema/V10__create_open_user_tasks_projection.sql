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

WITH legacy_business_keys AS (
    SELECT prozessinstanz_id, MIN(CAST(id AS VARCHAR)) AS business_key
    FROM urlaubsantraege
    WHERE prozessinstanz_id IS NOT NULL
    GROUP BY prozessinstanz_id
)
INSERT INTO offene_benutzeraufgaben (task_id, assignee, task_name, prozessinstanz_id, business_key)
SELECT task.ID_,
       assignee.id,
       COALESCE(task.NAME_, task.TASK_DEF_KEY_),
       task.PROC_INST_ID_,
       COALESCE(process_instance.BUSINESS_KEY_, legacy_business_key.business_key)
FROM ACT_RU_TASK task
JOIN ACT_RU_EXECUTION process_instance ON process_instance.ID_ = task.PROC_INST_ID_
LEFT JOIN benutzer assignee ON CAST(assignee.id AS VARCHAR) = LOWER(task.ASSIGNEE_)
LEFT JOIN legacy_business_keys legacy_business_key
    ON legacy_business_key.prozessinstanz_id = task.PROC_INST_ID_
WHERE task.SUSPENSION_STATE_ = 1
  AND COALESCE(task.NAME_, task.TASK_DEF_KEY_) IS NOT NULL
  AND COALESCE(process_instance.BUSINESS_KEY_, legacy_business_key.business_key) IS NOT NULL;

INSERT INTO offene_benutzeraufgaben_kandidaten (task_id, benutzer_id)
SELECT DISTINCT identity_link.TASK_ID_, candidate.id
FROM ACT_RU_IDENTITYLINK identity_link
JOIN offene_benutzeraufgaben task ON task.task_id = identity_link.TASK_ID_
JOIN benutzer candidate ON CAST(candidate.id AS VARCHAR) = LOWER(identity_link.USER_ID_)
WHERE identity_link.TYPE_ = 'candidate';

DROP TABLE aktive_benutzeraufgaben;
