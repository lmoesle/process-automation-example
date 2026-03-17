ALTER TABLE vacation_requests
    ADD COLUMN status VARCHAR(64);

UPDATE vacation_requests
SET status = 'ANTRAG_GESTELLT'
WHERE status IS NULL;

ALTER TABLE vacation_requests
    ALTER COLUMN status SET NOT NULL;

CREATE TABLE vacation_request_status_history
(
    vacation_request_id UUID         NOT NULL,
    history_index       INT          NOT NULL,
    status              VARCHAR(64)  NOT NULL,
    comment_text        VARCHAR(1000),
    PRIMARY KEY (vacation_request_id, history_index),
    CONSTRAINT fk_vacation_request_status_history_request
        FOREIGN KEY (vacation_request_id)
            REFERENCES vacation_requests (id)
            ON DELETE CASCADE
);

INSERT INTO vacation_request_status_history (vacation_request_id, history_index, status, comment_text)
SELECT id, 0, 'ANTRAG_GESTELLT', NULL
FROM vacation_requests;
