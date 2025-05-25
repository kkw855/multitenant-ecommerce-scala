-- noinspection SqlNoDataSourceInspectionForFile

CREATE TABLE subcategories
(
    category_id uuid NOT NULL,
    name        text NOT NULL,
    slug        text NOT NULL
);
CREATE UNIQUE INDEX idx_subcategories_slug ON subcategories (slug);

INSERT INTO subcategories (category_id, name, slug)
VALUES ('acdcd251-9001-41ae-adb4-ad601c171cea', 'Frontend', 'frontend');

INSERT INTO subcategories (category_id, name, slug)
VALUES ('acdcd251-9001-41ae-adb4-ad601c171cea', 'Backend', 'backend');

INSERT INTO subcategories (category_id, name, slug)
VALUES ('7ef31fe6-9459-4832-ab26-fe246a3d6d10', 'User experience', 'user-experience');
