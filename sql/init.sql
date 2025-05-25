-- noinspection SqlNoDataSourceInspectionForFile

CREATE DATABASE ecommerce;
\c ecommerce;

CREATE TABLE users
(
    email           text NOT NULL,
    hashed_password text NOT NULL,
    role            text NOT NULL
);
ALTER TABLE users
    ADD CONSTRAINT pk_users PRIMARY KEY (email);

CREATE TABLE categories
(
    id    uuid DEFAULT gen_random_uuid(),
    name  text NOT NULL,
    slug  text NOT NULL,
    color text NOT NULL
);

ALTER TABLE categories
    ADD CONSTRAINT pk_categories PRIMARY KEY (id);
CREATE UNIQUE INDEX idx_categories_slug ON categories (slug);

CREATE TABLE subcategories
(
    category_id uuid NOT NULL,
    name        text NOT NULL,
    slug        text NOT NULL
);
CREATE UNIQUE INDEX idx_subcategories_slug ON subcategories (slug);

INSERT INTO categories (id, name, slug, color)
VALUES ('acdcd251-9001-41ae-adb4-ad601c171cea', 'Software Development', 'software-development', '#FFB347');

INSERT INTO categories (id, name, slug, color)
VALUES ('7ef31fe6-9459-4832-ab26-fe246a3d6d10', 'Design & UX', 'design', '#7EC8E3');

INSERT INTO subcategories (category_id, name, slug)
VALUES ('acdcd251-9001-41ae-adb4-ad601c171cea', 'Frontend', 'frontend');

INSERT INTO subcategories (category_id, name, slug)
VALUES ('acdcd251-9001-41ae-adb4-ad601c171cea', 'Backend', 'backend');

INSERT INTO subcategories (category_id, name, slug)
VALUES ('acdcd251-9001-41ae-adb4-ad601c171cea', 'Mobile', 'mobile');

INSERT INTO subcategories (category_id, name, slug)
VALUES ('7ef31fe6-9459-4832-ab26-fe246a3d6d10', 'User experience', 'user-experience');

INSERT INTO subcategories (category_id, name, slug)
VALUES ('7ef31fe6-9459-4832-ab26-fe246a3d6d10', 'User interface', 'user-interface');
