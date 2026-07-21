CREATE TABLE urlaubsantraege
(
    id                UUID PRIMARY KEY,
    von               DATE         NOT NULL,
    bis               DATE         NOT NULL,
    antragsteller_id  UUID         NOT NULL,
    vertretung_id     UUID,
    prozessinstanz_id VARCHAR(255)
);
