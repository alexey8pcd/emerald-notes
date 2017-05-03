--
-- Файл сгенерирован с помощью SQLiteStudio v3.1.1 в Вс апр 23 21:11:01 2017
--
-- Использованная кодировка текста: UTF-8
--
PRAGMA foreign_keys = off;
BEGIN TRANSACTION;

-- Таблица: categories_of_things
DROP TABLE IF EXISTS categories_of_things;
CREATE TABLE categories_of_things (id_category INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, category VARCHAR (50) NOT NULL UNIQUE);

-- Таблица: countries
DROP TABLE IF EXISTS countries;
CREATE TABLE countries (id_country INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, country VARCHAR (50) NOT NULL UNIQUE);

-- Таблица: images_for_place
DROP TABLE IF EXISTS images_for_place;
CREATE TABLE images_for_place (id_image_for_place INTEGER PRIMARY KEY NOT NULL, id_place INTEGER NOT NULL REFERENCES places (id_place) ON DELETE RESTRICT ON UPDATE CASCADE, image VARCHAR NOT NULL);

-- Таблица: images_for_thing
DROP TABLE IF EXISTS images_for_thing;
CREATE TABLE images_for_thing (id_image_for_thing INTEGER NOT NULL, id_thing INTEGER NOT NULL REFERENCES things (id_thing) ON DELETE RESTRICT ON UPDATE CASCADE, image VARCHAR NOT NULL);

-- Таблица: note_types
DROP TABLE IF EXISTS note_types;
CREATE TABLE note_types (id_note_type INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, note_type VARCHAR (100) NOT NULL);

-- Таблица: notes
DROP TABLE IF EXISTS notes;
CREATE TABLE notes (id_note INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, note_text TEXT NOT NULL, id_note_type INTEGER NOT NULL REFERENCES note_types (id_note_type) ON DELETE RESTRICT ON UPDATE CASCADE);

-- Таблица: place_types
DROP TABLE IF EXISTS place_types;
CREATE TABLE place_types (id_place_type INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, type VARCHAR (40) NOT NULL);

-- Таблица: places
DROP TABLE IF EXISTS places;
CREATE TABLE places (id_place INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, description TEXT, latitude DECIMAL (15, 6), longitude DECIMAL (15, 6), address VARCHAR (100), date_create DATETIME, id_place_type INTEGER NOT NULL REFERENCES place_types (id_place_type) ON DELETE RESTRICT ON UPDATE CASCADE, id_country INTEGER REFERENCES countries (id_country) ON DELETE RESTRICT ON UPDATE CASCADE);

-- Таблица: things
DROP TABLE IF EXISTS things;
CREATE TABLE things (id_thing INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name VARCHAR (100) NOT NULL, description TEXT NOT NULL, id_danger_for_environment INTEGER NOT NULL, decomposition_time INTEGER, id_country INTEGER REFERENCES countries (id_country) ON DELETE RESTRICT ON UPDATE CASCADE, id_category INTEGER NOT NULL REFERENCES categories_of_things (id_category) ON DELETE RESTRICT ON UPDATE CASCADE);

COMMIT TRANSACTION;
PRAGMA foreign_keys = on;
