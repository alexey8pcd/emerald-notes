CREATE SEQUENCE public.categories_of_things_id_category_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.categories_of_things_id_category_seq
  OWNER TO postgres;

CREATE SEQUENCE public.countries_id_country_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.countries_id_country_seq
  OWNER TO postgres;

CREATE SEQUENCE public.images_for_place_id_image_for_place_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.images_for_place_id_image_for_place_seq
  OWNER TO postgres;

CREATE SEQUENCE public.images_for_thing_id_image_for_thing_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.images_for_thing_id_image_for_thing_seq
  OWNER TO postgres;

CREATE SEQUENCE public.images_id_image_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.images_id_image_seq
  OWNER TO postgres;

CREATE SEQUENCE public.messages_id_message_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.messages_id_message_seq
  OWNER TO postgres;

CREATE SEQUENCE public.note_types_id_note_type_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.note_types_id_note_type_seq
  OWNER TO postgres;

CREATE SEQUENCE public.notes_id_note_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.notes_id_note_seq
  OWNER TO postgres;

CREATE SEQUENCE public.place_types_id_place_type_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.place_types_id_place_type_seq
  OWNER TO postgres;

CREATE SEQUENCE public.places_id_place_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.places_id_place_seq
  OWNER TO postgres;


CREATE SEQUENCE public.things_id_thing_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.things_id_thing_seq
  OWNER TO postgres;
CREATE SEQUENCE public.users_id_user_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.users_id_user_seq
  OWNER TO postgres;

CREATE TABLE public.categories_of_things
(
  id_category integer NOT NULL DEFAULT nextval('categories_of_things_id_category_seq'::regclass),
  category character varying(50) NOT NULL,
  guid bytea NOT NULL,
  CONSTRAINT categories_of_things_pk PRIMARY KEY (id_category)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.categories_of_things
  OWNER TO postgres;

CREATE TABLE public.countries
(
  id_country integer NOT NULL DEFAULT nextval('countries_id_country_seq'::regclass),
  country character varying(50) NOT NULL,
  guid bytea NOT NULL,
  CONSTRAINT countries_pkey PRIMARY KEY (id_country),
  CONSTRAINT "UNIQUE_countries_1" UNIQUE (country),
  CONSTRAINT countries_guid_key UNIQUE (guid)
)
WITH (
  OIDS=FALSE
);

CREATE TABLE public.images
(
  id_image integer NOT NULL DEFAULT nextval('images_id_image_seq'::regclass),
  image_data bytea NOT NULL,
  guid bytea NOT NULL,
  CONSTRAINT images_pkey PRIMARY KEY (id_image),
  CONSTRAINT images_guid_key UNIQUE (guid)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.images

CREATE TABLE public.images_for_place
(
  id_image_for_place bigint NOT NULL DEFAULT nextval('images_for_place_id_image_for_place_seq'::regclass),
  id_image integer NOT NULL,
  id_place integer NOT NULL,
  CONSTRAINT images_for_place_pkey PRIMARY KEY (id_image_for_place),
  CONSTRAINT "REL_1" FOREIGN KEY (id_image)
      REFERENCES public.images (id_image) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT "REL_2" FOREIGN KEY (id_place)
      REFERENCES public.places (id_place) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.images_for_place
  OWNER TO postgres;

CREATE TABLE public.images_for_thing
(
  id_image_for_thing bigint NOT NULL DEFAULT nextval('images_for_thing_id_image_for_thing_seq'::regclass),
  id_image integer NOT NULL,
  id_thing integer NOT NULL,
  CONSTRAINT images_for_thing_pkey PRIMARY KEY (id_image_for_thing),
  CONSTRAINT "REL_6" FOREIGN KEY (id_image)
      REFERENCES public.images (id_image) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT "REL_7" FOREIGN KEY (id_thing)
      REFERENCES public.things (id_thing) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.images_for_thing
  OWNER TO postgres;


CREATE TABLE public.messages
(
  id_message integer NOT NULL DEFAULT nextval('messages_id_message_seq'::regclass),
  id_user_from integer NOT NULL,
  id_user_to integer NOT NULL,
  theme character varying(100) NOT NULL,
  message text NOT NULL,
  date_send timestamp without time zone NOT NULL,
  CONSTRAINT messages_pkey PRIMARY KEY (id_message),
  CONSTRAINT "REL_10" FOREIGN KEY (id_user_from)
      REFERENCES public.users (id_user) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT "REL_11" FOREIGN KEY (id_user_to)
      REFERENCES public.users (id_user) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.messages
  OWNER TO postgres;


CREATE TABLE public.note_types
(
  id_note_type integer NOT NULL DEFAULT nextval('note_types_id_note_type_seq'::regclass),
  note_type character varying(100) NOT NULL,
  guid bytea NOT NULL,
  CONSTRAINT note_types_pkey PRIMARY KEY (id_note_type),
  CONSTRAINT "UNIQUE_note_types_1" UNIQUE (note_type),
  CONSTRAINT note_types_guid_key UNIQUE (guid)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.note_types
  OWNER TO postgres;

CREATE TABLE public.notes
(
  id_note integer NOT NULL DEFAULT nextval('notes_id_note_seq'::regclass),
  note_text text NOT NULL,
  id_note_type integer NOT NULL,
  guid bytea NOT NULL,
  CONSTRAINT notes_pkey PRIMARY KEY (id_note),
  CONSTRAINT "REL_9" FOREIGN KEY (id_note_type)
      REFERENCES public.note_types (id_note_type) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT notes_guid_key UNIQUE (guid)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.notes
  OWNER TO postgres;

CREATE TABLE public.place_types
(
  id_place_type integer NOT NULL DEFAULT nextval('place_types_id_place_type_seq'::regclass),
  type character varying(40) NOT NULL,
  guid bytea NOT NULL,
  CONSTRAINT place_types_pkey PRIMARY KEY (id_place_type),
  CONSTRAINT "UNIQUE_place_types_1" UNIQUE (type),
  CONSTRAINT place_types_guid_key UNIQUE (guid)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.place_types
  OWNER TO postgres;

CREATE TABLE public.places
(
  id_place integer NOT NULL DEFAULT nextval('places_id_place_seq'::regclass),
  description text,
  latitude numeric(15,6),
  longitude numeric(15,6),
  address character varying(100),
  date_create timestamp without time zone,
  id_place_type integer NOT NULL,
  id_country integer,
  guid bytea NOT NULL,
  CONSTRAINT places_pkey PRIMARY KEY (id_place),
  CONSTRAINT "REL_3" FOREIGN KEY (id_place_type)
      REFERENCES public.place_types (id_place_type) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT "REL_8" FOREIGN KEY (id_country)
      REFERENCES public.countries (id_country) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT places_guid_key UNIQUE (guid)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.places
  OWNER TO postgres;


CREATE TABLE public.things
(
  id_thing integer NOT NULL DEFAULT nextval('things_id_thing_seq'::regclass),
  name character varying(100) NOT NULL,
  description text NOT NULL,
  id_danger_for_environment integer NOT NULL,
  decomposition_time integer,
  id_country integer,
  id_category integer NOT NULL,
  guid bytea,
  CONSTRAINT things_pkey PRIMARY KEY (id_thing),
  CONSTRAINT "REL_4" FOREIGN KEY (id_country)
      REFERENCES public.countries (id_country) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT categories_of_things_fk FOREIGN KEY (id_category)
      REFERENCES public.categories_of_things (id_category) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT things_guid_key UNIQUE (guid)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.things
  OWNER TO postgres;

CREATE TABLE public.users
(
  id_user integer NOT NULL DEFAULT nextval('users_id_user_seq'::regclass),
  login character varying(40) NOT NULL,
  password character varying(40) NOT NULL,
  id_country integer NOT NULL,
  CONSTRAINT users_pkey PRIMARY KEY (id_user),
  CONSTRAINT "UNIQUE_users_1" UNIQUE (login)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.users
  OWNER TO postgres;