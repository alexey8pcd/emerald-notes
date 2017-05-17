/*
Сценарий создан Aqua Data Studio 14.0.19 дата апр-08-2017 11:57:33 PM
База данных: База данныхБаза данных: null
Схема: <Все схемы>
*/

CREATE TABLE "categories_of_things"  ( 
	"id_category"	serial NOT NULL,
	"category"   	varchar(50) NOT NULL,
	PRIMARY KEY("id_category")
);
CREATE TABLE "countries"  ( 
	"id_country"	serial NOT NULL,
	"country"   	varchar(50) NOT NULL,
	PRIMARY KEY("id_country")
);
CREATE TABLE "images"  ( 
	"id_image"  	serial NOT NULL,
	"image_data"	bytea NOT NULL,
	PRIMARY KEY("id_image")
);
CREATE TABLE "images_for_place"  ( 
	"id_image_for_place"	bigserial NOT NULL,
	"id_image"          	integer NOT NULL,
	"id_place"          	integer NOT NULL,
	PRIMARY KEY("id_image_for_place")
);
CREATE TABLE "images_for_thing"  ( 
	"id_image_for_thing"	bigserial NOT NULL,
	"id_image"          	integer NOT NULL,
	"id_thing"          	integer NOT NULL,
	PRIMARY KEY("id_image_for_thing")
);
CREATE TABLE "messages"  ( 
	"id_message"  	serial NOT NULL,
	"id_user_from"	integer NOT NULL,
	"id_user_to"  	integer NOT NULL,
	"theme"       	varchar(100) NOT NULL,
	"message"     	text NOT NULL,
	"date_send"   	timestamp NOT NULL,
	PRIMARY KEY("id_message")
);
CREATE TABLE "note_types"  ( 
	"id_note_type"	serial NOT NULL,
	"note_type"   	varchar(100) NOT NULL,
	PRIMARY KEY("id_note_type")
);
CREATE TABLE "notes"  ( 
	"id_note"     	serial NOT NULL,
	"note_text"   	text NOT NULL,
	"id_note_type"	integer NOT NULL,
	PRIMARY KEY("id_note")
);
CREATE TABLE "place_types"  ( 
	"id_place_type"	serial NOT NULL,
	"type"         	varchar(40) NOT NULL,
	PRIMARY KEY("id_place_type")
);
CREATE TABLE "places"  ( 
	"id_place"     	serial NOT NULL,
	"description"  	text NULL,
	"latitude"     	decimal(15,6) NULL,
	"longitude"    	decimal(15,6) NULL,
	"address"      	varchar(100) NULL,
	"date_create"  	timestamp NULL,
	"id_place_type"	integer NOT NULL,
	"id_country"   	integer NULL,
	PRIMARY KEY("id_place")
);
CREATE TABLE "things"  ( 
	"id_thing"                 	serial NOT NULL,
	"name"                     	varchar(100) NOT NULL,
	"description"              	text NOT NULL,
	"id_danger_for_environment"	integer NOT NULL,
	"decomposition_time"       	integer NULL,
	"id_country"               	integer NULL,
	"id_category"              	integer NOT NULL,
	PRIMARY KEY("id_thing")
);
CREATE TABLE "users"  ( 
	"id_user"   	serial NOT NULL,
	"login"     	varchar(40) NOT NULL,
	"password"  	varchar(40) NOT NULL,
	"id_country"	integer NOT NULL,
	PRIMARY KEY("id_user")
);
ALTER TABLE "categories_of_things"
	ADD CONSTRAINT "UNIQUE_categories_of_things_1"
	UNIQUE ("category");
ALTER TABLE "countries"
	ADD CONSTRAINT "UNIQUE_countries_1"
	UNIQUE ("country");
ALTER TABLE "note_types"
	ADD CONSTRAINT "UNIQUE_note_types_1"
	UNIQUE ("note_type");
ALTER TABLE "place_types"
	ADD CONSTRAINT "UNIQUE_place_types_1"
	UNIQUE ("type");
ALTER TABLE "users"
	ADD CONSTRAINT "UNIQUE_users_1"
	UNIQUE ("login");
ALTER TABLE "things"
	ADD CONSTRAINT "REL_5"
	FOREIGN KEY("id_category")
	REFERENCES "categories_of_things"("id_category")
	ON DELETE RESTRICT 
	ON UPDATE CASCADE ;
ALTER TABLE "things"
	ADD CONSTRAINT "REL_4"
	FOREIGN KEY("id_country")
	REFERENCES "countries"("id_country")
	ON DELETE RESTRICT 
	ON UPDATE CASCADE ;
ALTER TABLE "places"
	ADD CONSTRAINT "REL_8"
	FOREIGN KEY("id_country")
	REFERENCES "countries"("id_country")
	ON DELETE RESTRICT 
	ON UPDATE CASCADE ;
ALTER TABLE "images_for_place"
	ADD CONSTRAINT "REL_1"
	FOREIGN KEY("id_image")
	REFERENCES "images"("id_image")
	ON DELETE RESTRICT 
	ON UPDATE CASCADE ;
ALTER TABLE "images_for_thing"
	ADD CONSTRAINT "REL_6"
	FOREIGN KEY("id_image")
	REFERENCES "images"("id_image")
	ON DELETE RESTRICT 
	ON UPDATE CASCADE ;
ALTER TABLE "notes"
	ADD CONSTRAINT "REL_9"
	FOREIGN KEY("id_note_type")
	REFERENCES "note_types"("id_note_type")
	ON DELETE RESTRICT 
	ON UPDATE CASCADE ;
ALTER TABLE "places"
	ADD CONSTRAINT "REL_3"
	FOREIGN KEY("id_place_type")
	REFERENCES "place_types"("id_place_type")
	ON DELETE RESTRICT 
	ON UPDATE CASCADE ;
ALTER TABLE "images_for_place"
	ADD CONSTRAINT "REL_2"
	FOREIGN KEY("id_place")
	REFERENCES "places"("id_place")
	ON DELETE RESTRICT 
	ON UPDATE CASCADE ;
ALTER TABLE "images_for_thing"
	ADD CONSTRAINT "REL_7"
	FOREIGN KEY("id_thing")
	REFERENCES "things"("id_thing")
	ON DELETE RESTRICT 
	ON UPDATE CASCADE ;
ALTER TABLE "messages"
	ADD CONSTRAINT "REL_10"
	FOREIGN KEY("id_user_from")
	REFERENCES "users"("id_user")
	ON DELETE RESTRICT 
	ON UPDATE CASCADE ;
ALTER TABLE "messages"
	ADD CONSTRAINT "REL_11"
	FOREIGN KEY("id_user_to")
	REFERENCES "users"("id_user")
	ON DELETE RESTRICT 
	ON UPDATE CASCADE ;
