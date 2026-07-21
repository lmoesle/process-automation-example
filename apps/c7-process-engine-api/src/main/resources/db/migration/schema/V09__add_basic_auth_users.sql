ALTER TABLE benutzer
    ADD COLUMN benutzername VARCHAR(255),
    ADD COLUMN passwort_hash VARCHAR(255);

ALTER TABLE benutzer
    ADD CONSTRAINT uk_benutzer_benutzername UNIQUE (benutzername);

INSERT INTO benutzer (id, name, email, benutzername, passwort_hash)
VALUES ('41f60f4f-1bbb-4469-871f-bf102c46d001', 'John', 'john@example.com', 'john', '{bcrypt}$2b$12$b5Ng2UBy/.5wTV/AWYvC0uEu3W6k.nALPLtBY1MfGoZpKL9SZvroq'),
       ('45a65ce0-5ee9-4b40-bc7d-134837cf3002', 'Jane', 'jane@example.com', 'jane', '{bcrypt}$2b$12$b5Ng2UBy/.5wTV/AWYvC0uEu3W6k.nALPLtBY1MfGoZpKL9SZvroq'),
       ('cd4346cb-e8dc-4ba8-8f94-4f3e5d5ec003', 'Max', 'max@example.com', 'max', '{bcrypt}$2b$12$b5Ng2UBy/.5wTV/AWYvC0uEu3W6k.nALPLtBY1MfGoZpKL9SZvroq');

INSERT INTO team_mitgliedschaften (team_id, benutzer_id, rolle)
VALUES ('c9d0c7dc-3ed5-4877-95e3-df8c8af1f201', '41f60f4f-1bbb-4469-871f-bf102c46d001', 'MITGLIED'),
       ('c9d0c7dc-3ed5-4877-95e3-df8c8af1f201', '45a65ce0-5ee9-4b40-bc7d-134837cf3002', 'LEITUNG'),
       ('c9d0c7dc-3ed5-4877-95e3-df8c8af1f201', 'cd4346cb-e8dc-4ba8-8f94-4f3e5d5ec003', 'MITGLIED');
