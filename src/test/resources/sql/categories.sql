-- noinspection SqlNoDataSourceInspectionForFile

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

INSERT INTO categories (id, name, slug, color)
VALUES ('acdcd251-9001-41ae-adb4-ad601c171cea', 'Software Development', 'software-development', '#FFB347');

INSERT INTO categories (id, name, slug, color)
VALUES ('7ef31fe6-9459-4832-ab26-fe246a3d6d10', 'Design & UX', 'design', '#7EC8E3');