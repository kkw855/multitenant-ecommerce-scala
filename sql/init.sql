-- noinspection SqlNoDataSourceInspectionForFile

CREATE DATABASE ecommerce;
\c ecommerce;

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

INSERT INTO categories (name, slug, color)
VALUES ('Photography', 'photography', '#FF6B6B');
INSERT INTO categories (name, slug, color)
VALUES ('Music', 'music', '#FFD700');
INSERT INTO categories (name, slug, color)
VALUES ('Drawing & Painting', 'drawing-painting', '#FFCAB0');
INSERT INTO categories (name, slug, color)
VALUES ('Design', 'design', '#B5B9FF');
INSERT INTO categories (name, slug, color)
VALUES ('Fitness & Health', 'fitness-health', '#FF9AA2');
INSERT INTO categories (name, slug, color)
VALUES ('Self Improvement', 'self-improvement', '#96E6B3');
INSERT INTO categories (name, slug, color)
VALUES ('Education', 'education', '#FFE066');
INSERT INTO categories (name, slug, color)
VALUES ('Other', 'other', '#FFFFFF');
INSERT INTO categories (name, slug, color)
VALUES ('Writing & Publishing', 'writing-publishing', '#D8B5FF');
INSERT INTO categories (name, slug, color)
VALUES ('Software Development', 'software-development', '#7EC8E3');
INSERT INTO categories (name, slug, color)
VALUES ('Business & Money', 'business-money', '#FFB347');
INSERT INTO categories (name, slug, color)
VALUES ('All', 'all', '#FFFFFF');

INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Portrait', 'portrait'
FROM categories
WHERE slug = 'photography';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Landscape', 'landscape'
FROM categories
WHERE slug = 'photography';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Street Photography', 'street-photography'
FROM categories
WHERE slug = 'photography';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Nature', 'nature'
FROM categories
WHERE slug = 'photography';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Macro', 'macro'
FROM categories
WHERE slug = 'photography';

INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Songwriting', 'songwriting'
FROM categories
WHERE slug = 'music';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Music Production', 'music-production'
FROM categories
WHERE slug = 'music';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Music Theory', 'music-theory'
FROM categories
WHERE slug = 'music';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Music History', 'music-history'
FROM categories
WHERE slug = 'music';

INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Watercolor', 'watercolor'
FROM categories
WHERE slug = 'drawing-painting';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Acrylic', 'acrylic'
FROM categories
WHERE slug = 'drawing-painting';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Oil', 'oil'
FROM categories
WHERE slug = 'drawing-painting';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Pastel', 'pastel'
FROM categories
WHERE slug = 'drawing-painting';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Charcoal', 'charcoal'
FROM categories
WHERE slug = 'drawing-painting';

INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'UI/UX', 'ui-ux'
FROM categories
WHERE slug = 'design';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Graphic Design', 'graphic-design'
FROM categories
WHERE slug = 'design';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, '3D Modeling', '3d-modeling'
FROM categories
WHERE slug = 'design';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Typography', 'typography'
FROM categories
WHERE slug = 'design';

INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Workout Plans', 'workout-plans'
FROM categories
WHERE slug = 'fitness-health';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Nutrition', 'nutrition'
FROM categories
WHERE slug = 'fitness-health';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Mental Health', 'mental-health'
FROM categories
WHERE slug = 'fitness-health';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Yoga', 'yoga'
FROM categories
WHERE slug = 'fitness-health';

INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Productivity', 'productivity'
FROM categories
WHERE slug = 'self-improvement';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Personal Development', 'personal-development'
FROM categories
WHERE slug = 'self-improvement';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Mindfulness', 'mindfulness'
FROM categories
WHERE slug = 'self-improvement';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Career Growth', 'career-growth'
FROM categories
WHERE slug = 'self-improvement';

INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Online Courses', 'online-courses'
FROM categories
WHERE slug = 'education';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Tutoring', 'tutoring'
FROM categories
WHERE slug = 'education';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Test Preparation', 'test-preparation'
FROM categories
WHERE slug = 'education';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Language Learning', 'language-learning'
FROM categories
WHERE slug = 'education';

INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Fiction', 'fiction'
FROM categories
WHERE slug = 'writing-publishing';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Non-Fiction', 'non-fiction'
FROM categories
WHERE slug = 'writing-publishing';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Blogging', 'blogging'
FROM categories
WHERE slug = 'writing-publishing';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Copywriting', 'copywriting'
FROM categories
WHERE slug = 'writing-publishing';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Self-Publishing', 'self-publishing'
FROM categories
WHERE slug = 'writing-publishing';

INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Web Development', 'web-development'
FROM categories
WHERE slug = 'software-development';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Mobile Development', 'mobile-development'
FROM categories
WHERE slug = 'software-development';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Game Development', 'game-development'
FROM categories
WHERE slug = 'software-development';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Programming Languages', 'programming-languages'
FROM categories
WHERE slug = 'software-development';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'DevOps', 'devops'
FROM categories
WHERE slug = 'software-development';

INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Accounting', 'accounting'
FROM categories
WHERE slug = 'business-money';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Entrepreneurship', 'entrepreneurship'
FROM categories
WHERE slug = 'business-money';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Gigs & Side Projects', 'gigs-side-projects'
FROM categories
WHERE slug = 'business-money';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Investing', 'investing'
FROM categories
WHERE slug = 'business-money';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Management & Leadership', 'management-leadership'
FROM categories
WHERE slug = 'business-money';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Marketing & Sales', 'marketing-sales'
FROM categories
WHERE slug = 'business-money';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Networking, Careers & Jobs', 'networking-careers-jobs'
FROM categories
WHERE slug = 'business-money';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Personal Finance', 'personal-finance'
FROM categories
WHERE slug = 'business-money';
INSERT INTO subcategories (category_id, name, slug)
SELECT id, 'Real Estate', 'real-estate'
FROM categories
WHERE slug = 'business-money';
