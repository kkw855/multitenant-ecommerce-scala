-- noinspection SqlNoDataSourceInspectionForFile

CREATE DATABASE ecommerce;
\c ecommerce;

CREATE TABLE users
(
  email          text NOT NULL,
  hashedPassword text NOT NULL,
  role           text NOT NULL
);
ALTER TABLE users
  ADD CONSTRAINT pk_users PRIMARY KEY (email);

CREATE TABLE categories
(
  id   uuid DEFAULT gen_random_uuid(),
  name text NOT NULL
);

ALTER TABLE categories
  ADD CONSTRAINT pk_categories PRIMARY KEY (id);
