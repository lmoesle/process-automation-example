INSERT INTO benutzer (id, name, email)
VALUES ('2d88b39b-e7b0-4a3f-b9c6-b3d8e6fbe100', 'Ada Lovelace', 'ada.lovelace@example.com'),
       ('fe530ec8-3894-46fa-95af-c42441f6a101', 'Bob Martin', 'bob.martin@example.com'),
       ('f9821988-db4f-4daa-9414-6cc5227f7102', 'Carla Gomez', 'carla.gomez@example.com'),
       ('6b0bda26-5c94-42d5-b26a-b59c0fadb103', 'David Miller', 'david.miller@example.com'),
       ('a4b2a7ef-4d2d-4c59-8129-f9b09c1af104', 'Erin Stone', 'erin.stone@example.com');

INSERT INTO teams (id, name)
VALUES ('c9d0c7dc-3ed5-4877-95e3-df8c8af1f201', 'Engineering'),
       ('57bc8807-59f4-44dc-9056-740678242202', 'Platform'),
       ('c19e5b79-7162-4118-a9ec-a95140cb3203', 'Operations');

INSERT INTO team_mitgliedschaften (team_id, benutzer_id, rolle)
VALUES ('c9d0c7dc-3ed5-4877-95e3-df8c8af1f201', '2d88b39b-e7b0-4a3f-b9c6-b3d8e6fbe100', 'LEITUNG'),
       ('c9d0c7dc-3ed5-4877-95e3-df8c8af1f201', 'fe530ec8-3894-46fa-95af-c42441f6a101', 'MITGLIED'),
       ('c9d0c7dc-3ed5-4877-95e3-df8c8af1f201', 'a4b2a7ef-4d2d-4c59-8129-f9b09c1af104', 'MITGLIED'),
       ('57bc8807-59f4-44dc-9056-740678242202', '2d88b39b-e7b0-4a3f-b9c6-b3d8e6fbe100', 'MITGLIED'),
       ('57bc8807-59f4-44dc-9056-740678242202', 'f9821988-db4f-4daa-9414-6cc5227f7102', 'LEITUNG'),
       ('57bc8807-59f4-44dc-9056-740678242202', '6b0bda26-5c94-42d5-b26a-b59c0fadb103', 'MITGLIED'),
       ('c19e5b79-7162-4118-a9ec-a95140cb3203', '6b0bda26-5c94-42d5-b26a-b59c0fadb103', 'LEITUNG'),
       ('c19e5b79-7162-4118-a9ec-a95140cb3203', 'a4b2a7ef-4d2d-4c59-8129-f9b09c1af104', 'MITGLIED');
