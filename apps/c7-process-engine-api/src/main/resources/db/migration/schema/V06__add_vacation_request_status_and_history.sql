ALTER TABLE urlaubsantraege
    ADD COLUMN status VARCHAR(64);

UPDATE urlaubsantraege
SET status = 'ANTRAG_GESTELLT'
WHERE status IS NULL;

ALTER TABLE urlaubsantraege
    ALTER COLUMN status SET NOT NULL;

CREATE TABLE urlaubsantrag_statushistorie
(
    urlaubsantrag_id UUID         NOT NULL,
    historien_index  INT          NOT NULL,
    status           VARCHAR(64)  NOT NULL,
    kommentar        VARCHAR(1000),
    PRIMARY KEY (urlaubsantrag_id, historien_index),
    CONSTRAINT fk_urlaubsantrag_statushistorie_urlaubsantrag
        FOREIGN KEY (urlaubsantrag_id)
            REFERENCES urlaubsantraege (id)
            ON DELETE CASCADE
);

INSERT INTO urlaubsantrag_statushistorie (urlaubsantrag_id, historien_index, status, kommentar)
SELECT id, 0, 'ANTRAG_GESTELLT', NULL
FROM urlaubsantraege;
