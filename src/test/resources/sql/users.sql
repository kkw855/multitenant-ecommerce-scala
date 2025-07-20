-- noinspection SqlNoDataSourceInspectionForFile

CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE users
(
    username    text NOT NULL,
    email       text NOT NULL,
    stored_hash text NOT NULL,
    created_at  timestamp with time zone DEFAULT now(),
    updated_at  timestamp with time zone
);
ALTER TABLE users
    ADD CONSTRAINT pk_users PRIMARY KEY (username);

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE
    ON users
    FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_column();

INSERT INTO users(username, email, stored_hash)
VALUES ('Lee',
        'lee@gmail.com',
        '/ELXh1K0RhxExGt5iy+M+g==,ycaoYHXvOuVDNSdBcHf2AfL3t+Blm+5D/BlSwfA+fhg=');

INSERT INTO users(username, email, stored_hash)
VALUES ('Hong',
        'hong@gmail.com',
        '$2a$10$5tVg0GGcZ/INAq/dfApDPufBcOR1kAUumwrRm5w7WrMdJG159f382');
