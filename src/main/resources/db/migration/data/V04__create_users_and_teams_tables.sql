CREATE TABLE users
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

CREATE TABLE team_memberships
(
    team_id UUID        NOT NULL REFERENCES teams (id),
    user_id UUID        NOT NULL REFERENCES users (id),
    role    VARCHAR(32) NOT NULL,
    PRIMARY KEY (team_id, user_id),
    CONSTRAINT chk_team_memberships_role CHECK (role IN ('USER', 'LEAD'))
);

CREATE INDEX idx_team_memberships_user_id ON team_memberships (user_id);
CREATE INDEX idx_team_memberships_team_role ON team_memberships (team_id, role);
