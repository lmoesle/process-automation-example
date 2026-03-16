CREATE TABLE vacation_requests
(
    id                  UUID PRIMARY KEY,
    vacation_from       DATE         NOT NULL,
    vacation_to         DATE         NOT NULL,
    applicant_user_id   UUID         NOT NULL,
    substitute_user_id  UUID,
    process_instance_id VARCHAR(255)
);
