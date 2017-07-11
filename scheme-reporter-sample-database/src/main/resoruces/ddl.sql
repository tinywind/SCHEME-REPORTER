-- Schema of "http://www.guidemon.com"

CREATE DOMAIN user_type AS enum ('admin', 'guide', 'tourist');

CREATE DOMAIN gender_type AS enum ('male', 'female');

CREATE DOMAIN account_status_type AS enum ('normalcy', 'blackout');

CREATE DOMAIN guide_status_type AS enum ('standby', 'pass', 'reject');

CREATE DOMAIN due_date_type AS enum ('day', 'hour');

CREATE DOMAIN tour_scale_type AS enum ('solo', 'plural');

CREATE DOMAIN age_type AS enum ('ten1', 'ten2', 'ten3', 'ten4', 'ten5', 'ten6', 'ten7', 'over');

CREATE DOMAIN tour_sketch_node_type AS enum ('Folder', 'Item');

CREATE DOMAIN guide_specialty_type AS enum ('common', 'special');

CREATE DOMAIN order_type AS enum ('expensive', 'cheapest', 'popularity', 'newest');

CREATE DOMAIN message_type AS enum ('total', 'received', 'sent');

CREATE DOMAIN product_status_type AS enum ('temporary_save', 'defer', 'reject', 'standby', 'on_sale');

CREATE DOMAIN tour_type AS enum ('unit', 'custom');

CREATE DOMAIN message_tag AS enum ('notice', 'request_tour', 'reservation', 'inquire');

CREATE DOMAIN reservation_status_type AS enum ('payment', 'confirmation', 'complete', 'canceled');

CREATE DOMAIN payout_status_type AS enum ('do_not', 'request', 'done');

CREATE DOMAIN transportation_type AS enum ('bicycle', 'car', 'public');

CREATE DOMAIN product_type AS enum ('tour', 'service');

CREATE DOMAIN payment_method_type AS enum ('card', 'deposit');

CREATE TABLE public_file
(
  id BIGSERIAL                          NOT NULL
    CONSTRAINT public_file_pkey
    PRIMARY KEY,
  original_name VARCHAR(256)            NOT NULL,
  name          VARCHAR(256)            NOT NULL,
  size          BIGINT                  NOT NULL,
  created_at    TIMESTAMP DEFAULT now() NOT NULL,
  updated_at    TIMESTAMP,
  deleted_at    TIMESTAMP
);

COMMENT ON TABLE public_file IS 'saved file at server';

COMMENT ON COLUMN public_file.id IS 'table identifier';

COMMENT ON COLUMN public_file.original_name IS 'file name what is file name of user end side in uploading';

COMMENT ON COLUMN public_file.name IS 'saved file name at server side after upload';

COMMENT ON COLUMN public_file.size IS 'file size';

COMMENT ON COLUMN public_file.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN public_file.updated_at IS 'timestamp when this record is updated';

COMMENT ON COLUMN public_file.deleted_at IS 'present that this record is deleted';

CREATE TABLE theme
(
  id BIGSERIAL  NOT NULL
    CONSTRAINT theme_pkey
    PRIMARY KEY,
  name VARCHAR(2048) NOT NULL,
  ref_id BIGINT
    CONSTRAINT theme_ref_id_fkey
    REFERENCES theme,
  trend  BOOLEAN DEFAULT FALSE NOT NULL,
  image_id BIGINT
    CONSTRAINT theme_image_id_fkey
    REFERENCES public_file,
  background_image_id BIGINT
    CONSTRAINT theme_background_image_id_fkey
    REFERENCES public_file,
  created_at          TIMESTAMP DEFAULT now() NOT NULL,
  updated_at          TIMESTAMP,
  deleted_at          TIMESTAMP
);

COMMENT ON TABLE theme IS 'define theme';

COMMENT ON COLUMN theme.id IS 'table identifier';

COMMENT ON COLUMN theme.name IS 'name of theme';

COMMENT ON COLUMN theme.ref_id IS 'reference anyone of "theme"';

COMMENT ON COLUMN theme.trend IS 'is representative';

COMMENT ON COLUMN theme.image_id IS 'reference icon';

COMMENT ON COLUMN theme.background_image_id IS 'reference picture';

COMMENT ON COLUMN theme.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN theme.updated_at IS 'timestamp when this record is updated';

COMMENT ON COLUMN theme.deleted_at IS 'present that this record is deleted';

CREATE TABLE language
(
  id BIGSERIAL           NOT NULL
    CONSTRAINT language_pkey
    PRIMARY KEY,
  name      VARCHAR(2048) NOT NULL,
  sort_name VARCHAR(100) NOT NULL
    CONSTRAINT language_sort_name_key
    UNIQUE
);

COMMENT ON TABLE language IS 'define language for guiding';

COMMENT ON COLUMN language.id IS 'table identifier';

COMMENT ON COLUMN language.name IS 'name of language';

COMMENT ON COLUMN language.sort_name IS 'name of language for sorting';

CREATE TABLE keyword
(
  id BIGSERIAL       NOT NULL
    CONSTRAINT keyword_pkey
    PRIMARY KEY,
  name VARCHAR(2048) NOT NULL
    CONSTRAINT keyword_name_key
    UNIQUE
);

COMMENT ON TABLE keyword IS 'define keyword for product classification';

COMMENT ON COLUMN keyword.id IS 'table identifier';

COMMENT ON COLUMN keyword.name IS 'name of keyword';

CREATE TABLE currency
(
  id BIGSERIAL       NOT NULL
    CONSTRAINT currency_pkey
    PRIMARY KEY,
  name VARCHAR(2048) NOT NULL
    CONSTRAINT currency_name_key
    UNIQUE
);

COMMENT ON TABLE currency IS 'define currency';

COMMENT ON COLUMN currency.id IS 'table identifier';

COMMENT ON COLUMN currency.name IS 'name of currency';

CREATE TABLE location_country
(
  id BIGSERIAL          NOT NULL
    CONSTRAINT location_country_pkey
    PRIMARY KEY,
  name         VARCHAR(100) NOT NULL
    CONSTRAINT location_country_name_key
    UNIQUE,
  sort_name VARCHAR(10) NOT NULL
    CONSTRAINT location_country_sort_name_key
    UNIQUE,
  korean_name VARCHAR(100),
  country_code INTEGER,
  latitude     DOUBLE PRECISION,
  longitude    DOUBLE PRECISION,
  CONSTRAINT location_country_latitude_longitude_key
  UNIQUE (latitude, longitude)
);

COMMENT ON TABLE location_country IS 'real country';

COMMENT ON COLUMN location_country.id IS 'table identifier';

COMMENT ON COLUMN location_country.name IS 'name of country';

COMMENT ON COLUMN location_country.sort_name IS 'name of country for sorting';

COMMENT ON COLUMN location_country.korean_name IS 'korean name of country';

COMMENT ON COLUMN location_country.country_code IS 'international dialling code';

COMMENT ON COLUMN location_country.latitude IS 'latitude of country';

COMMENT ON COLUMN location_country.longitude IS 'longitude of country';

CREATE TABLE location_state
(
  id BIGSERIAL            NOT NULL
    CONSTRAINT location_state_pkey
    PRIMARY KEY,
  country_id BIGINT NOT NULL
    CONSTRAINT location_state_country_id_fkey
    REFERENCES location_country,
  name       VARCHAR(100) NOT NULL,
  latitude   DOUBLE PRECISION,
  longitude  DOUBLE PRECISION,
  CONSTRAINT location_state_country_id_name_key
  UNIQUE (country_id, name),
  CONSTRAINT location_state_latitude_longitude_key
  UNIQUE (latitude, longitude)
);

COMMENT ON TABLE location_state IS 'real state';

COMMENT ON COLUMN location_state.id IS 'table identifier';

COMMENT ON COLUMN location_state.country_id IS 'reference country';

COMMENT ON COLUMN location_state.name IS 'name of state';

COMMENT ON COLUMN location_state.latitude IS 'latitude of state';

COMMENT ON COLUMN location_state.longitude IS 'longitude of state';

CREATE TABLE location_city
(
  id BIGSERIAL          NOT NULL
    CONSTRAINT location_city_pkey
    PRIMARY KEY,
  state_id  BIGINT NOT NULL
    CONSTRAINT location_city_state_id_fkey
    REFERENCES location_state,
  name     VARCHAR(100) NOT NULL,
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  CONSTRAINT location_city_state_id_name_key
  UNIQUE (state_id, name),
  CONSTRAINT location_city_latitude_longitude_key
  UNIQUE (latitude, longitude)
);

COMMENT ON TABLE location_city IS 'real city';

COMMENT ON COLUMN location_city.id IS 'table identifier';

COMMENT ON COLUMN location_city.state_id IS 'reference state';

COMMENT ON COLUMN location_city.name IS 'name of city';

COMMENT ON COLUMN location_city.latitude IS 'latitude of city';

COMMENT ON COLUMN location_city.longitude IS 'longitude of city';

CREATE TABLE country_x_language
(
  id BIGSERIAL       NOT NULL
    CONSTRAINT country_x_language_pkey
    PRIMARY KEY,
  country_id  BIGINT NOT NULL
    CONSTRAINT country_x_language_country_id_fkey
    REFERENCES location_country,
  language_id BIGINT NOT NULL
    CONSTRAINT country_x_language_language_id_fkey
    REFERENCES language
);

COMMENT ON TABLE country_x_language IS 'relationship of country with language. languages are used as representative country.';

COMMENT ON COLUMN country_x_language.id IS 'table identifier';

COMMENT ON COLUMN country_x_language.country_id IS 'reference country';

COMMENT ON COLUMN country_x_language.language_id IS 'reference language';

CREATE TABLE "user"
(
  id BIGSERIAL                                NOT NULL
    CONSTRAINT user_pkey
    PRIMARY KEY,
  type user_type NOT NULL,
  email               VARCHAR(100),
  first_name VARCHAR(100),
  middle_name VARCHAR(100),
  last_name   VARCHAR(100),
  profile_image_id BIGINT
    CONSTRAINT user_profile_image_id_fkey
    REFERENCES public_file,
  profile_description TEXT,
  gender gender_type,
  mobile_country_id   BIGINT
    CONSTRAINT user_mobile_country_id_fkey
    REFERENCES location_country,
  mobile              VARCHAR(20),
  nationality_id      BIGINT
    CONSTRAINT user_nationality_id_fkey
    REFERENCES location_country,
  account_status account_status_type DEFAULT 'normalcy'::account_status_type NOT NULL,
  created_at          TIMESTAMP DEFAULT now() NOT NULL,
  updated_at          TIMESTAMP,
  deleted_at          TIMESTAMP,
  agree_receive       BOOLEAN DEFAULT FALSE   NOT NULL,
  locale              VARCHAR(16)
);

COMMENT ON COLUMN "user".id IS 'table identifier';

COMMENT ON COLUMN "user".created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN "user".updated_at IS 'timestamp when this record is updated';

COMMENT ON COLUMN "user".deleted_at IS 'present that this record is deleted';

COMMENT ON COLUMN "user".id IS 'table identifier';

COMMENT ON COLUMN "user".created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN "user".updated_at IS 'timestamp when this record is updated';

COMMENT ON COLUMN "user".deleted_at IS 'present that this record is deleted';

CREATE TABLE admin
(
  id             BIGINT       NOT NULL
    CONSTRAINT admin_pkey
    PRIMARY KEY,
  login_id VARCHAR(100) NOT NULL
    CONSTRAINT admin_login_id_key
    UNIQUE,
  login_password VARCHAR(100) NOT NULL,

  CONSTRAINT admin_id_fkey
  FOREIGN KEY (id)
  REFERENCES "user" (id)
);

COMMENT ON TABLE admin IS 'user of admin type';

COMMENT ON COLUMN admin.id IS 'table identifier';

COMMENT ON COLUMN admin.login_id IS 'login ID';

COMMENT ON COLUMN admin.login_password IS 'login password';

CREATE TABLE message
(
  id BIGSERIAL                             NOT NULL
    CONSTRAINT message_pkey
    PRIMARY KEY,
  ref_id           BIGINT
    CONSTRAINT message_ref_id_fkey
    REFERENCES message,
  sender_id BIGINT NOT NULL
    CONSTRAINT message_sender_id_fkey
    REFERENCES "user",
  receiver_id BIGINT NOT NULL
    CONSTRAINT message_receiver_id_fkey
    REFERENCES "user",
  message_header VARCHAR(1024),
  message_body   VARCHAR(2048) NOT NULL,
  message_footer VARCHAR(1024),
  read           BOOLEAN DEFAULT FALSE NOT NULL,
  sender_deleted BOOLEAN DEFAULT FALSE NOT NULL,
  receiver_deleted BOOLEAN DEFAULT FALSE NOT NULL,
  created_at       TIMESTAMP DEFAULT now() NOT NULL,
  updated_at       TIMESTAMP,
  deleted_at       TIMESTAMP,
  tag message_tag,
  tour_plan_id     BIGINT
);

CREATE INDEX message_sender_id_idx
  ON message (sender_id);

CREATE INDEX message_receiver_id_read_idx
  ON message (receiver_id, read);

CREATE INDEX message_receiver_id_idx
  ON message (receiver_id);

COMMENT ON TABLE message IS 'message form user to user';

COMMENT ON COLUMN message.id IS 'table identifier';

COMMENT ON COLUMN message.ref_id IS 'represented message';

COMMENT ON COLUMN message.sender_id IS 'reference sender';

COMMENT ON COLUMN message.receiver_id IS 'reference receiver';

COMMENT ON COLUMN message.message_header IS 'header content of message';

COMMENT ON COLUMN message.message_body IS 'body content of message';

COMMENT ON COLUMN message.message_footer IS 'footer content of message';

COMMENT ON COLUMN message.read IS 'read message by receiver';

COMMENT ON COLUMN message.sender_deleted IS 'deleted message by sender';

COMMENT ON COLUMN message.receiver_deleted IS 'deleted message by receiver';

COMMENT ON COLUMN message.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN message.updated_at IS 'timestamp when this record is updated';

COMMENT ON COLUMN message.deleted_at IS 'present that this record is deleted';

COMMENT ON COLUMN message.tag IS 'for classification';

COMMENT ON COLUMN message.tour_plan_id IS 'reference tour-plan';

CREATE TABLE guide_event_type
(
  id BIGSERIAL                       NOT NULL
    CONSTRAINT guide_event_type_pkey
    PRIMARY KEY,
  name       VARCHAR(255) NOT NULL
    CONSTRAINT guide_event_type_name_key
    UNIQUE,
  marker VARCHAR(2048),
  created_at TIMESTAMP DEFAULT now() NOT NULL,
  updated_at TIMESTAMP,
  deleted_at TIMESTAMP
);

COMMENT ON TABLE guide_event_type IS 'define promotion type about guide';

COMMENT ON COLUMN guide_event_type.id IS 'table identifier';

COMMENT ON COLUMN guide_event_type.name IS 'name of promotion';

COMMENT ON COLUMN guide_event_type.marker IS 'mark as';

COMMENT ON COLUMN guide_event_type.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN guide_event_type.updated_at IS 'timestamp when this record is updated';

COMMENT ON COLUMN guide_event_type.deleted_at IS 'present that this record is deleted';

CREATE TABLE guide
(
  id                  BIGINT                  NOT NULL
    CONSTRAINT guide_pkey
    PRIMARY KEY,
  status guide_status_type DEFAULT 'standby'::guide_status_type NOT NULL,
  guide_event_type_id BIGINT
    CONSTRAINT guide_guide_event_type_id_fkey
    REFERENCES guide_event_type,
  login_id            VARCHAR(100) NOT NULL
    CONSTRAINT guide_login_id_key
    UNIQUE,
  login_password      VARCHAR(100) NOT NULL,
  email_generate_key  VARCHAR(255) NOT NULL,
  email_certified_at  TIMESTAMP,
  skype_id            VARCHAR(40),
  residential_city_id BIGINT       NOT NULL
    CONSTRAINT guide_residential_city_id_fkey
    REFERENCES location_city,
  experience          INTEGER,
  age                 INTEGER,
  created_at          TIMESTAMP DEFAULT now() NOT NULL,
  updated_at          TIMESTAMP,

  CONSTRAINT guide_id_fkey
  FOREIGN KEY (id)
  REFERENCES "user"
);

COMMENT ON TABLE guide IS 'user of guide/guide manager type';

COMMENT ON COLUMN guide.id IS 'table identifier';

COMMENT ON COLUMN guide.status IS 'approved by administrator';

COMMENT ON COLUMN guide.guide_event_type_id IS 'promotion about guide';

COMMENT ON COLUMN guide.login_id IS 'login ID';

COMMENT ON COLUMN guide.login_password IS 'login password';

COMMENT ON COLUMN guide.email_generate_key IS 'generated key for certificating email';

COMMENT ON COLUMN guide.email_certified_at IS 'timestamp when email is certificated';

COMMENT ON COLUMN guide.skype_id IS 'skype ID';

COMMENT ON COLUMN guide.residential_city_id IS 'city of residence';

COMMENT ON COLUMN guide.experience IS 'n years experience about guiding';

COMMENT ON COLUMN guide.age IS 'age';

COMMENT ON COLUMN guide.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN guide.updated_at IS 'timestamp when this record is updated';

CREATE TABLE guide_x_license
(
  id BIGSERIAL                         NOT NULL
    CONSTRAINT guide_x_license_pkey
    PRIMARY KEY,
  guide_id                BIGINT NOT NULL
    CONSTRAINT guide_x_license_guide_id_fkey
    REFERENCES guide,
  license_picture_file_id BIGINT
    CONSTRAINT guide_x_license_license_picture_file_id_fkey
    REFERENCES public_file,
  license_name            VARCHAR(255) NOT NULL
);

COMMENT ON TABLE guide_x_license IS 'relationship of guide with license.';

COMMENT ON COLUMN guide_x_license.id IS 'table identifier';

COMMENT ON COLUMN guide_x_license.guide_id IS 'reference guide';

COMMENT ON COLUMN guide_x_license.license_picture_file_id IS 'reference picture file about license';

COMMENT ON COLUMN guide_x_license.license_name IS 'name of license';

CREATE TABLE guide_x_language
(
  id BIGSERIAL       NOT NULL
    CONSTRAINT guide_x_language_pkey
    PRIMARY KEY,
  guide_id    BIGINT NOT NULL
    CONSTRAINT guide_x_language_guide_id_fkey
    REFERENCES guide,
  language_id BIGINT NOT NULL
    CONSTRAINT guide_x_language_language_id_fkey
    REFERENCES language,
  CONSTRAINT guide_x_language_guide_id_language_id_key
  UNIQUE (guide_id, language_id)
);

COMMENT ON TABLE guide_x_language IS 'relationship of guide with language. One guide has 1 guiding-language or more.';

COMMENT ON COLUMN guide_x_language.id IS 'table identifier';

COMMENT ON COLUMN guide_x_language.guide_id IS 'reference guide';

COMMENT ON COLUMN guide_x_language.language_id IS 'reference language';

CREATE TABLE user_x_interest_guide
(
  id BIGSERIAL                       NOT NULL
    CONSTRAINT user_x_interest_guide_pkey
    PRIMARY KEY,
  user_id    BIGINT NOT NULL
    CONSTRAINT user_x_interest_guide_user_id_fkey
    REFERENCES "user",
  guide_id BIGINT NOT NULL
    CONSTRAINT user_x_interest_guide_guide_id_fkey
    REFERENCES guide,
  created_at TIMESTAMP DEFAULT now() NOT NULL,
  updated_at TIMESTAMP,
  CONSTRAINT user_x_interest_guide_user_id_guide_id_key
  UNIQUE (user_id, guide_id)
);

COMMENT ON TABLE user_x_interest_guide IS 'relationship of user with guide.';

COMMENT ON COLUMN user_x_interest_guide.id IS 'table identifier';

COMMENT ON COLUMN user_x_interest_guide.user_id IS 'reference user';

COMMENT ON COLUMN user_x_interest_guide.guide_id IS 'reference guide';

COMMENT ON COLUMN user_x_interest_guide.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN user_x_interest_guide.updated_at IS 'timestamp when this record is updated';

CREATE TABLE tourist
(
  id         BIGINT                  NOT NULL
    CONSTRAINT tourist_pkey
    PRIMARY KEY,
  created_at TIMESTAMP DEFAULT now() NOT NULL,
  updated_at TIMESTAMP,

  CONSTRAINT tourist_id_fkey
  FOREIGN KEY (id)
  REFERENCES "user"
);

COMMENT ON TABLE tourist IS 'user of tourist type';

COMMENT ON COLUMN tourist.id IS 'table identifier';

COMMENT ON COLUMN tourist.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN tourist.updated_at IS 'timestamp when this record is updated';

CREATE TABLE tourist_facebook
(
  id          BIGINT                  NOT NULL
    CONSTRAINT tourist_facebook_pkey
    PRIMARY KEY,
  social_id VARCHAR(255) NOT NULL
    CONSTRAINT tourist_facebook_social_id_key
    UNIQUE,
  first_name VARCHAR(100),
  middle_name VARCHAR(100),
  last_name   VARCHAR(100),
  email       VARCHAR(255),
  gender gender_type,
  created_at  TIMESTAMP DEFAULT now() NOT NULL,
  updated_at  TIMESTAMP,

  CONSTRAINT tourist_facebook_id_fkey
  FOREIGN KEY (id)
  REFERENCES tourist
);

COMMENT ON TABLE tourist_facebook IS 'joined tourist through facebook';

COMMENT ON COLUMN tourist_facebook.id IS 'table identifier';

COMMENT ON COLUMN tourist_facebook.social_id IS 'facebook ID. login ID';

COMMENT ON COLUMN tourist_facebook.first_name IS 'first name of tourist that is received from facebook';

COMMENT ON COLUMN tourist_facebook.middle_name IS 'middle name of tourist that is received from facebook';

COMMENT ON COLUMN tourist_facebook.last_name IS 'last name of tourist that is received from facebook';

COMMENT ON COLUMN tourist_facebook.email IS 'email of tourist that is received from facebook';

COMMENT ON COLUMN tourist_facebook.gender IS 'gender of tourist that is received from facebook';

COMMENT ON COLUMN tourist_facebook.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN tourist_facebook.updated_at IS 'timestamp when this record is updated';

CREATE TABLE tourist_googleplus
(
  id         BIGINT                  NOT NULL
    CONSTRAINT tourist_googleplus_pkey
    PRIMARY KEY,
  social_id VARCHAR(255) NOT NULL
    CONSTRAINT tourist_googleplus_social_id_key
    UNIQUE,
  full_name VARCHAR(255),
  first_name VARCHAR(100),
  last_name  VARCHAR(100),
  email      VARCHAR(255),
  gender gender_type,
  created_at TIMESTAMP DEFAULT now() NOT NULL,
  updated_at TIMESTAMP,

  CONSTRAINT tourist_googleplus_id_fkey
  FOREIGN KEY (id)
  REFERENCES tourist
);

COMMENT ON TABLE tourist_googleplus IS 'joined tourist through googleplus';

COMMENT ON COLUMN tourist_googleplus.id IS 'table identifier';

COMMENT ON COLUMN tourist_googleplus.social_id IS 'googleplus ID. login ID';

COMMENT ON COLUMN tourist_googleplus.full_name IS 'full name of tourist that is received from googleplus';

COMMENT ON COLUMN tourist_googleplus.first_name IS 'first name of tourist that is received from googleplus';

COMMENT ON COLUMN tourist_googleplus.last_name IS 'last name of tourist that is received from googleplus';

COMMENT ON COLUMN tourist_googleplus.email IS 'email of tourist that is received from googleplus';

COMMENT ON COLUMN tourist_googleplus.gender IS 'gender of tourist that is received from googleplus';

COMMENT ON COLUMN tourist_googleplus.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN tourist_googleplus.updated_at IS 'timestamp when this record is updated';

CREATE TABLE tourist_twitter
(
  id          BIGINT                  NOT NULL
    CONSTRAINT tourist_twitter_pkey
    PRIMARY KEY,
  social_id VARCHAR(255) NOT NULL
    CONSTRAINT tourist_twitter_social_id_key
    UNIQUE,
  screen_name VARCHAR(255) NOT NULL,
  name        VARCHAR(255),
  created_at  TIMESTAMP DEFAULT now() NOT NULL,
  updated_at  TIMESTAMP,

  CONSTRAINT tourist_twitter_id_fkey
  FOREIGN KEY (id)
  REFERENCES tourist
);

COMMENT ON TABLE tourist_twitter IS 'joined tourist through twitter';

COMMENT ON COLUMN tourist_twitter.id IS 'table identifier';

COMMENT ON COLUMN tourist_twitter.social_id IS 'twitter ID. login ID';

COMMENT ON COLUMN tourist_twitter.screen_name IS 'screen-name of tourist that is received from twitter';

COMMENT ON COLUMN tourist_twitter.name IS 'name of tourist that is received from twitter';

COMMENT ON COLUMN tourist_twitter.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN tourist_twitter.updated_at IS 'timestamp when this record is updated';

CREATE TABLE tourist_kakaotalk
(
  id         BIGINT                  NOT NULL
    CONSTRAINT tourist_kakaotalk_pkey
    PRIMARY KEY,
  social_id VARCHAR(255) NOT NULL
    CONSTRAINT tourist_kakaotalk_social_id_key
    UNIQUE,
  nickname  VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT now() NOT NULL,
  updated_at TIMESTAMP,

  CONSTRAINT tourist_kakaotalk_id_fkey
  FOREIGN KEY (id)
  REFERENCES tourist
);

COMMENT ON TABLE tourist_kakaotalk IS 'joined tourist through kakaotalk';

COMMENT ON COLUMN tourist_kakaotalk.id IS 'table identifier';

COMMENT ON COLUMN tourist_kakaotalk.social_id IS 'kakaotalk ID. login ID';

COMMENT ON COLUMN tourist_kakaotalk.nickname IS 'nickname of tourist that is received from kakaotalk';

COMMENT ON COLUMN tourist_kakaotalk.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN tourist_kakaotalk.updated_at IS 'timestamp when this record is updated';

CREATE TABLE tourist_email
(
  id           BIGINT                  NOT NULL
    CONSTRAINT tourist_email_pkey
    PRIMARY KEY,
  email VARCHAR(100) NOT NULL
    CONSTRAINT tourist_email_email_key
    UNIQUE,
  password VARCHAR(100) NOT NULL,
  generate_key VARCHAR(255) NOT NULL,
  certified_at TIMESTAMP,
  created_at   TIMESTAMP DEFAULT now() NOT NULL,
  updated_at   TIMESTAMP,

  CONSTRAINT tourist_email_id_fkey
  FOREIGN KEY (id)
  REFERENCES tourist
);

COMMENT ON TABLE tourist_email IS 'joined tourist through input email information';

COMMENT ON COLUMN tourist_email.id IS 'table identifier';

COMMENT ON COLUMN tourist_email.email IS 'email of tourist. login ID';

COMMENT ON COLUMN tourist_email.password IS 'login password';

COMMENT ON COLUMN tourist_email.generate_key IS 'generated key for certificating email';

COMMENT ON COLUMN tourist_email.certified_at IS 'timestamp when email is certificated';

COMMENT ON COLUMN tourist_email.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN tourist_email.updated_at IS 'timestamp when this record is updated';

CREATE TABLE product_event_type
(
  id BIGSERIAL                       NOT NULL
    CONSTRAINT product_event_type_pkey
    PRIMARY KEY,
  name       VARCHAR(255) NOT NULL
    CONSTRAINT product_event_type_name_key
    UNIQUE,
  discount INTEGER DEFAULT 0 NOT NULL,
  marker   VARCHAR(2048),
  created_at TIMESTAMP DEFAULT now() NOT NULL,
  updated_at TIMESTAMP,
  deleted_at TIMESTAMP
);

COMMENT ON TABLE product_event_type IS 'define promotion type about product';

COMMENT ON COLUMN product_event_type.id IS 'table identifier';

COMMENT ON COLUMN product_event_type.name IS 'name of promotion';

COMMENT ON COLUMN product_event_type.discount IS 'discount rate(0 ~ 99)';

COMMENT ON COLUMN product_event_type.marker IS 'mark as';

COMMENT ON COLUMN product_event_type.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN product_event_type.updated_at IS 'timestamp when this record is updated';

COMMENT ON COLUMN product_event_type.deleted_at IS 'present that this record is deleted';

CREATE TABLE product
(
  id BIGSERIAL                                  NOT NULL
    CONSTRAINT product_pkey
    PRIMARY KEY,
  title                 VARCHAR(100),
  status product_status_type DEFAULT 'temporary_save'::product_status_type,
  product_event_type_id BIGINT
    CONSTRAINT product_product_event_type_id_fkey
    REFERENCES product_event_type,
  seller_id             BIGINT
    CONSTRAINT product_seller_id_fkey
    REFERENCES "user",
  currency_id           BIGINT
    CONSTRAINT product_currency_id_fkey
    REFERENCES currency,
  image_id              BIGINT,
  summary               TEXT,
  description           TEXT,
  inclusion             TEXT,
  exclusion             TEXT,
  etc                   TEXT,
  created_at            TIMESTAMP DEFAULT now() NOT NULL,
  updated_at            TIMESTAMP,
  deleted_at            TIMESTAMP,
  unload                VARCHAR(1000),
  type product_type DEFAULT 'tour'::product_type NOT NULL
);

COMMENT ON TABLE product IS 'selling product by guide or guide manager';

COMMENT ON COLUMN product.id IS 'table identifier';

COMMENT ON COLUMN product.title IS 'title of product';

COMMENT ON COLUMN product.status IS 'approved by administrator';

COMMENT ON COLUMN product.product_event_type_id IS 'promotion about product';

COMMENT ON COLUMN product.seller_id IS 'reference seller';

COMMENT ON COLUMN product.currency_id IS 'base currency';

COMMENT ON COLUMN product.image_id IS 'representative image of product';

COMMENT ON COLUMN product.summary IS 'summary of product';

COMMENT ON COLUMN product.description IS 'description';

COMMENT ON COLUMN product.inclusion IS 'extra options what are include in the price';

COMMENT ON COLUMN product.exclusion IS 'extra options what are not include in the price';

COMMENT ON COLUMN product.etc IS 'extra description';

COMMENT ON COLUMN product.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN product.updated_at IS 'timestamp when this record is updated';

COMMENT ON COLUMN product.deleted_at IS 'present that this record is deleted';

COMMENT ON COLUMN product.unload IS 'request message for stop sale product';

COMMENT ON COLUMN product.type IS 'product type(tour/service)';

CREATE TABLE product_price
(
  id BIGSERIAL                           NOT NULL
    CONSTRAINT product_price_pkey
    PRIMARY KEY,
  product_id     BIGINT NOT NULL
    CONSTRAINT product_price_product_id_fkey
    REFERENCES product,
  minimum_people INTEGER,
  maximum_people INTEGER,
  price          DOUBLE PRECISION,
  created_at     TIMESTAMP DEFAULT now() NOT NULL,
  updated_at     TIMESTAMP,
  CONSTRAINT product_price_product_id_minimum_people_maximum_people_key
  UNIQUE (product_id, minimum_people, maximum_people)
);

COMMENT ON TABLE product_price IS 'price information of product. price of one product is comprised one price information or more.';

COMMENT ON COLUMN product_price.id IS 'table identifier';

COMMENT ON COLUMN product_price.product_id IS 'reference product';

COMMENT ON COLUMN product_price.minimum_people IS 'minimum number of people is affected by this price information';

COMMENT ON COLUMN product_price.maximum_people IS 'maximum number of people is affected by this price information';

COMMENT ON COLUMN product_price.price IS 'price';

COMMENT ON COLUMN product_price.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN product_price.updated_at IS 'timestamp when this record is updated';

CREATE TABLE course
(
  id BIGSERIAL                         NOT NULL
    CONSTRAINT product_course_pkey
    PRIMARY KEY,
  unit_tour_id BIGINT,
  title        VARCHAR(100),
  description  TEXT,
  sequence     INTEGER NOT NULL,
  created_at   TIMESTAMP DEFAULT now() NOT NULL,
  updated_at   TIMESTAMP,
  deleted_at   TIMESTAMP
);

COMMENT ON TABLE course IS 'component what constitutes product. they are sequential about time.';

COMMENT ON COLUMN course.id IS 'table identifier';

COMMENT ON COLUMN course.unit_tour_id IS 'reference product';

COMMENT ON COLUMN course.title IS 'title of course';

COMMENT ON COLUMN course.description IS 'description';

COMMENT ON COLUMN course.sequence IS 'sequence number';

COMMENT ON COLUMN course.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN course.updated_at IS 'timestamp when this record is updated';

COMMENT ON COLUMN course.deleted_at IS 'present that this record is deleted';

CREATE TABLE course_x_media_file
(
  id BIGSERIAL                           NOT NULL
    CONSTRAINT product_course_x_media_file_pkey
    PRIMARY KEY,
  course_id      BIGINT NOT NULL
    CONSTRAINT product_course_x_media_file_product_course_id_fkey
    REFERENCES course,
  public_file_id BIGINT NOT NULL
    CONSTRAINT product_course_x_media_file_public_file_id_fkey
    REFERENCES public_file,
  created_at     TIMESTAMP DEFAULT now() NOT NULL,
  updated_at     TIMESTAMP
);

COMMENT ON TABLE course_x_media_file IS 'image file what is comprised in one course.';

COMMENT ON COLUMN course_x_media_file.id IS 'table identifier';

COMMENT ON COLUMN course_x_media_file.course_id IS 'reference course';

COMMENT ON COLUMN course_x_media_file.public_file_id IS 'reference image file';

COMMENT ON COLUMN course_x_media_file.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN course_x_media_file.updated_at IS 'timestamp when this record is updated';

CREATE TABLE product_x_city
(
  id BIGSERIAL                       NOT NULL
    CONSTRAINT product_x_city_pkey
    PRIMARY KEY,
  product_id BIGINT NOT NULL
    CONSTRAINT product_x_city_product_id_fkey
    REFERENCES product,
  city_id    BIGINT NOT NULL
    CONSTRAINT product_x_city_city_id_fkey
    REFERENCES location_city,
  created_at TIMESTAMP DEFAULT now() NOT NULL,
  updated_at TIMESTAMP,
  deleted_at TIMESTAMP,
  CONSTRAINT product_x_city_product_id_city_id_key
  UNIQUE (product_id, city_id)
);

COMMENT ON TABLE product_x_city IS 'relationship of product with city. tour city is one or more.';

COMMENT ON COLUMN product_x_city.id IS 'table identifier';

COMMENT ON COLUMN product_x_city.product_id IS 'reference product';

COMMENT ON COLUMN product_x_city.city_id IS 'reference city';

COMMENT ON COLUMN product_x_city.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN product_x_city.updated_at IS 'timestamp when this record is updated';

COMMENT ON COLUMN product_x_city.deleted_at IS 'present that this record is deleted';

CREATE TABLE product_x_keyword
(
  id BIGSERIAL                       NOT NULL
    CONSTRAINT product_x_keyword_pkey
    PRIMARY KEY,
  product_id BIGINT NOT NULL
    CONSTRAINT product_x_keyword_product_id_fkey
    REFERENCES product,
  keyword_id BIGINT NOT NULL
    CONSTRAINT product_x_keyword_keyword_id_fkey
    REFERENCES keyword,
  created_at TIMESTAMP DEFAULT now() NOT NULL,
  updated_at TIMESTAMP,
  deleted_at TIMESTAMP
);

COMMENT ON TABLE product_x_keyword IS 'relationship of product with keyword.';

COMMENT ON COLUMN product_x_keyword.id IS 'table identifier';

COMMENT ON COLUMN product_x_keyword.product_id IS 'reference product';

COMMENT ON COLUMN product_x_keyword.keyword_id IS 'reference keyword';

COMMENT ON COLUMN product_x_keyword.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN product_x_keyword.updated_at IS 'timestamp when this record is updated';

COMMENT ON COLUMN product_x_keyword.deleted_at IS 'present that this record is deleted';

CREATE TABLE product_x_theme
(
  id BIGSERIAL                       NOT NULL
    CONSTRAINT product_x_theme_pkey
    PRIMARY KEY,
  product_id BIGINT NOT NULL
    CONSTRAINT product_x_theme_product_id_fkey
    REFERENCES product,
  theme_id   BIGINT NOT NULL
    CONSTRAINT product_x_theme_theme_id_fkey
    REFERENCES theme,
  created_at TIMESTAMP DEFAULT now() NOT NULL,
  updated_at TIMESTAMP,
  deleted_at TIMESTAMP
);

COMMENT ON TABLE product_x_theme IS 'relationship of product with theme';

COMMENT ON COLUMN product_x_theme.id IS 'table identifier';

COMMENT ON COLUMN product_x_theme.product_id IS 'reference product';

COMMENT ON COLUMN product_x_theme.theme_id IS 'reference theme';

COMMENT ON COLUMN product_x_theme.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN product_x_theme.updated_at IS 'timestamp when this record is updated';

COMMENT ON COLUMN product_x_theme.deleted_at IS 'present that this record is deleted';

CREATE TABLE product_reservation
(
  id BIGSERIAL                                 NOT NULL
    CONSTRAINT product_reservation_pkey
    PRIMARY KEY,
  user_id             BIGINT NOT NULL
    CONSTRAINT product_reservation_user_id_fkey
    REFERENCES "user",
  product_id BIGINT NOT NULL
    CONSTRAINT product_reservation_product_id_fkey
    REFERENCES product,
  currency_id BIGINT NOT NULL
    CONSTRAINT product_reservation_currency_id_fkey
    REFERENCES currency,
  book_date   DATE   NOT NULL,
  number_people INTEGER NOT NULL,
  age age_type,
  purpose_id    BIGINT
    CONSTRAINT product_reservation_purpose_id_fkey
    REFERENCES theme,
  request_message TEXT,
  status reservation_status_type DEFAULT 'payment'::reservation_status_type NOT NULL,
  name            VARCHAR(100) NOT NULL,
  english_name    VARCHAR(100) NOT NULL,
  mobile_country_id BIGINT     NOT NULL
    CONSTRAINT product_reservation_mobile_country_id_fkey
    REFERENCES location_country,
  mobile            VARCHAR(20) NOT NULL,
  email             VARCHAR(100) NOT NULL,
  payout_status payout_status_type DEFAULT 'do_not'::payout_status_type NOT NULL,
  created_at        TIMESTAMP DEFAULT now() NOT NULL,
  updated_at        TIMESTAMP,
  deleted_at        TIMESTAMP,
  event_name        VARCHAR(255),
  event_discount    INTEGER DEFAULT 0       NOT NULL,
  original_price    DOUBLE PRECISION DEFAULT 0 NOT NULL,
  final_price       DOUBLE PRECISION DEFAULT 0 NOT NULL,
  epilogue_star_point INTEGER,
  epilogue_comment    TEXT,
  epilogue_wrote_at   TIMESTAMP,
  epilogue_reply      TEXT
);

COMMENT ON TABLE product_reservation IS 'reservation of product';

COMMENT ON COLUMN product_reservation.id IS 'table identifier';

COMMENT ON COLUMN product_reservation.user_id IS 'reference people who has booked';

COMMENT ON COLUMN product_reservation.product_id IS 'reference product';

COMMENT ON COLUMN product_reservation.currency_id IS 'reference currency';

COMMENT ON COLUMN product_reservation.book_date IS 'booked date';

COMMENT ON COLUMN product_reservation.number_people IS 'number of people';

COMMENT ON COLUMN product_reservation.age IS 'age of representative';

COMMENT ON COLUMN product_reservation.purpose_id IS 'purpose of tour. reference trend theme.';

COMMENT ON COLUMN product_reservation.request_message IS 'request message about tour';

COMMENT ON COLUMN product_reservation.status IS 'tour progress situation';

COMMENT ON COLUMN product_reservation.name IS 'name of representative';

COMMENT ON COLUMN product_reservation.english_name IS 'english name of representative';

COMMENT ON COLUMN product_reservation.mobile_country_id IS 'reference activated country of cellphone';

COMMENT ON COLUMN product_reservation.mobile IS 'mobile number of representative';

COMMENT ON COLUMN product_reservation.email IS 'email of representative';

COMMENT ON COLUMN product_reservation.payout_status IS 'is paid';

COMMENT ON COLUMN product_reservation.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN product_reservation.updated_at IS 'timestamp when this record is updated';

COMMENT ON COLUMN product_reservation.deleted_at IS 'present that this record is deleted';

COMMENT ON COLUMN product_reservation.event_name IS 'promotion about product';

COMMENT ON COLUMN product_reservation.event_discount IS 'promotion about product';

COMMENT ON COLUMN product_reservation.original_price IS 'non-discounted price by event';

COMMENT ON COLUMN product_reservation.final_price IS 'discounted price by event';

COMMENT ON COLUMN product_reservation.epilogue_star_point IS 'score (0 ~ 5) about tour satisfaction';

COMMENT ON COLUMN product_reservation.epilogue_comment IS 'comment of epilogue';

COMMENT ON COLUMN product_reservation.epilogue_wrote_at IS 'timestamp when this epilogue is wrote';

CREATE TABLE confirmation
(
  id               BIGINT                  NOT NULL
    CONSTRAINT confirmation_pkey
    PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  english_name VARCHAR(100) NOT NULL,
  email        VARCHAR(100) NOT NULL,
  phone        VARCHAR(100) NOT NULL,
  tour_title   VARCHAR(100) NOT NULL,
  tourist_num  VARCHAR(100) NOT NULL,
  tour_date    VARCHAR(100) NOT NULL,
  cost         VARCHAR(100) NOT NULL,
  paid         VARCHAR(100) NOT NULL,
  contactable_time VARCHAR(100) NOT NULL,
  meeting_place    VARCHAR(100) NOT NULL,
  guide_phone      VARCHAR(100) NOT NULL,
  guide            VARCHAR(100) NOT NULL,
  created_at       TIMESTAMP DEFAULT now() NOT NULL,
  updated_at       TIMESTAMP,
  deleted_at       TIMESTAMP,

  CONSTRAINT confirmation_id_fkey
  FOREIGN KEY (id)
  REFERENCES product_reservation
);

COMMENT ON TABLE confirmation IS 'serves as evidence of tour';

COMMENT ON COLUMN confirmation.id IS 'table identifier';

COMMENT ON COLUMN confirmation.name IS 'name of tourist';

COMMENT ON COLUMN confirmation.english_name IS 'english name of tourist';

COMMENT ON COLUMN confirmation.email IS 'email of tourist';

COMMENT ON COLUMN confirmation.phone IS 'phone number of tourist';

COMMENT ON COLUMN confirmation.tour_title IS 'tour title';

COMMENT ON COLUMN confirmation.tourist_num IS 'tourist number';

COMMENT ON COLUMN confirmation.tour_date IS 'day when tour start';

COMMENT ON COLUMN confirmation.cost IS 'total tour cost';

COMMENT ON COLUMN confirmation.paid IS 'present payment';

COMMENT ON COLUMN confirmation.contactable_time IS 'contactable time with guide';

COMMENT ON COLUMN confirmation.meeting_place IS 'place where meet with guide';

COMMENT ON COLUMN confirmation.guide_phone IS 'phone number of guide';

COMMENT ON COLUMN confirmation.guide IS 'name of guide';

COMMENT ON COLUMN confirmation.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN confirmation.updated_at IS 'timestamp when this record is updated';

COMMENT ON COLUMN confirmation.deleted_at IS 'present that this record is deleted';

CREATE TABLE user_x_interest_product
(
  id BIGSERIAL                       NOT NULL
    CONSTRAINT user_x_interest_product_pkey
    PRIMARY KEY,
  user_id    BIGINT NOT NULL
    CONSTRAINT user_x_interest_product_user_id_fkey
    REFERENCES "user",
  product_id BIGINT NOT NULL
    CONSTRAINT user_x_interest_product_product_id_fkey
    REFERENCES product,
  created_at TIMESTAMP DEFAULT now() NOT NULL,
  updated_at TIMESTAMP,
  CONSTRAINT user_x_interest_product_user_id_product_id_key
  UNIQUE (user_id, product_id)
);

COMMENT ON TABLE user_x_interest_product IS 'relationship of user with product.';

COMMENT ON COLUMN user_x_interest_product.id IS 'table identifier';

COMMENT ON COLUMN user_x_interest_product.user_id IS 'reference user';

COMMENT ON COLUMN user_x_interest_product.product_id IS 'reference product';

COMMENT ON COLUMN user_x_interest_product.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN user_x_interest_product.updated_at IS 'timestamp when this record is updated';

CREATE TABLE tour_plan
(
  id BIGSERIAL                        NOT NULL
    CONSTRAINT tour_plan_pkey
    PRIMARY KEY,
  user_id     BIGINT NOT NULL
    CONSTRAINT tour_plan_user_id_fkey
    REFERENCES "user",
  title   VARCHAR(200) NOT NULL,
  description TEXT,
  start_at    DATE,
  end_at      DATE,
  created_at  TIMESTAMP DEFAULT now() NOT NULL,
  updated_at  TIMESTAMP,
  deleted_at  TIMESTAMP
);

COMMENT ON TABLE tour_plan IS 'plan tour by user. also it is used component of custom tour.';

COMMENT ON COLUMN tour_plan.id IS 'table identifier';

COMMENT ON COLUMN tour_plan.user_id IS 'reference writer';

COMMENT ON COLUMN tour_plan.title IS 'title of tour-plan';

COMMENT ON COLUMN tour_plan.description IS 'description';

COMMENT ON COLUMN tour_plan.start_at IS 'start date of tour';

COMMENT ON COLUMN tour_plan.end_at IS 'end date of tour';

COMMENT ON COLUMN tour_plan.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN tour_plan.updated_at IS 'timestamp when this record is updated';

COMMENT ON COLUMN tour_plan.deleted_at IS 'present that this record is deleted';

ALTER TABLE message
  ADD CONSTRAINT message_tour_plan_id_fkey
FOREIGN KEY (tour_plan_id) REFERENCES tour_plan;

CREATE TABLE tour_plan_x_theme
(
  id BIGSERIAL                         NOT NULL
    CONSTRAINT tour_plan_x_theme_pkey
    PRIMARY KEY,
  tour_plan_id BIGINT NOT NULL
    CONSTRAINT tour_plan_x_theme_tour_plan_id_fkey
    REFERENCES tour_plan,
  theme_id     BIGINT NOT NULL
    CONSTRAINT tour_plan_x_theme_theme_id_fkey
    REFERENCES theme,
  created_at   TIMESTAMP DEFAULT now() NOT NULL,
  updated_at   TIMESTAMP,
  CONSTRAINT tour_plan_x_theme_tour_plan_id_theme_id_key
  UNIQUE (tour_plan_id, theme_id)
);

COMMENT ON TABLE tour_plan_x_theme IS 'relationship of tour-plan with theme';

COMMENT ON COLUMN tour_plan_x_theme.id IS 'table identifier';

COMMENT ON COLUMN tour_plan_x_theme.tour_plan_id IS 'reference tour-plan';

COMMENT ON COLUMN tour_plan_x_theme.theme_id IS 'reference theme';

COMMENT ON COLUMN tour_plan_x_theme.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN tour_plan_x_theme.updated_at IS 'timestamp when this record is updated';

CREATE TABLE tour_sketch
(
  id         BIGINT                  NOT NULL
    CONSTRAINT tour_sketch_pkey
    PRIMARY KEY,
  created_at TIMESTAMP DEFAULT now() NOT NULL,
  updated_at TIMESTAMP,

  CONSTRAINT tour_sketch_id_fkey
  FOREIGN KEY (id)
  REFERENCES tour_plan
);

COMMENT ON TABLE tour_sketch IS 'collection tour-information';

COMMENT ON COLUMN tour_sketch.id IS 'table identifier';

COMMENT ON COLUMN tour_sketch.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN tour_sketch.updated_at IS 'timestamp when this record is updated';

CREATE TABLE tour_sketch_node
(
  id BIGSERIAL                           NOT NULL
    CONSTRAINT tour_sketch_node_pkey
    PRIMARY KEY,
  tour_sketch_id BIGINT NOT NULL
    CONSTRAINT tour_sketch_node_tour_sketch_id_fkey
    REFERENCES tour_sketch,
  sequence       DOUBLE PRECISION NOT NULL,
  name           VARCHAR(200)     NOT NULL,
  ref_id         BIGINT
    CONSTRAINT tour_sketch_node_ref_id_fkey
    REFERENCES tour_sketch_node,
  type tour_sketch_node_type NOT NULL,
  unit_tour_id   BIGINT,
  created_at     TIMESTAMP DEFAULT now() NOT NULL,
  updated_at     TIMESTAMP
);

COMMENT ON TABLE tour_sketch_node IS 'tour-information';

COMMENT ON COLUMN tour_sketch_node.id IS 'table identifier';

COMMENT ON COLUMN tour_sketch_node.tour_sketch_id IS 'reference tour-sketch';

COMMENT ON COLUMN tour_sketch_node.sequence IS 'sequence number';

COMMENT ON COLUMN tour_sketch_node.name IS 'name of information';

COMMENT ON COLUMN tour_sketch_node.ref_id IS 'reference tour-sketch-node';

COMMENT ON COLUMN tour_sketch_node.type IS 'tour-sketch-node is two types: folder(no information. for classification), item(belong folder-node)';

COMMENT ON COLUMN tour_sketch_node.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN tour_sketch_node.updated_at IS 'timestamp when this record is updated';

CREATE TABLE tour_schedule
(
  id BIGSERIAL                         NOT NULL
    CONSTRAINT tour_schedule_pkey
    PRIMARY KEY,
  tour_plan_id BIGINT NOT NULL
    CONSTRAINT tour_schedule_tour_plan_id_fkey
    REFERENCES tour_plan,
  created_at   TIMESTAMP DEFAULT now() NOT NULL,
  updated_at   TIMESTAMP,
  deleted_at   TIMESTAMP
);

COMMENT ON TABLE tour_schedule IS 'schedule sheet of tour-plan';

COMMENT ON COLUMN tour_schedule.id IS 'table identifier';

COMMENT ON COLUMN tour_schedule.tour_plan_id IS 'reference tour-plan';

COMMENT ON COLUMN tour_schedule.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN tour_schedule.updated_at IS 'timestamp when this record is updated';

COMMENT ON COLUMN tour_schedule.deleted_at IS 'present that this record is deleted';

CREATE TABLE faq
(
  id BIGSERIAL                       NOT NULL
    CONSTRAINT faq_pkey
    PRIMARY KEY,
  user_id     BIGINT NOT NULL
    CONSTRAINT faq_user_id_fkey
    REFERENCES "user",
  question TEXT  NOT NULL,
  answer   TEXT  NOT NULL,
  created_at TIMESTAMP DEFAULT now() NOT NULL,
  updated_at TIMESTAMP,
  deleted_at TIMESTAMP,
  category_id BIGINT
);

COMMENT ON TABLE faq IS 'frequently asked questions';

COMMENT ON COLUMN faq.id IS 'table identifier';

COMMENT ON COLUMN faq.user_id IS 'reference writer';

COMMENT ON COLUMN faq.question IS 'question';

COMMENT ON COLUMN faq.answer IS 'answer';

COMMENT ON COLUMN faq.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN faq.updated_at IS 'timestamp when this record is updated';

COMMENT ON COLUMN faq.deleted_at IS 'present that this record is deleted';

COMMENT ON COLUMN faq.category_id IS 'reference category';

CREATE TABLE seller_impossible_date
(
  seller_id BIGINT NOT NULL
    CONSTRAINT seller_impossible_date_seller_id_fkey
    REFERENCES guide,
  date      DATE   NOT NULL,
  CONSTRAINT seller_impossible_date_pkey
  PRIMARY KEY (seller_id, date)
);

CREATE INDEX seller_impossible_date_seller_id_idx
  ON seller_impossible_date (seller_id);

COMMENT ON TABLE seller_impossible_date IS 'impossible date to reserve a product what is sale by seller';

COMMENT ON COLUMN seller_impossible_date.seller_id IS 'reference seller';

COMMENT ON COLUMN seller_impossible_date.date IS 'impossible date';

CREATE TABLE product_impossible_date
(
  product_id BIGINT NOT NULL
    CONSTRAINT product_impossible_date_product_id_fkey
    REFERENCES product,
  date       DATE   NOT NULL,
  CONSTRAINT product_impossible_date_pkey
  PRIMARY KEY (product_id, date)
);

CREATE INDEX product_impossible_date_product_id_idx
  ON product_impossible_date (product_id);

COMMENT ON TABLE product_impossible_date IS 'impossible date to reserve a product';

COMMENT ON COLUMN product_impossible_date.product_id IS 'reference product';

COMMENT ON COLUMN product_impossible_date.date IS 'impossible date';

CREATE TABLE tour_schedule_day
(
  id BIGSERIAL                                         NOT NULL
    CONSTRAINT tour_schedule_day_pkey
    PRIMARY KEY,
  tour_schedule_id             BIGINT NOT NULL
    CONSTRAINT tour_schedule_day_tour_schedule_id_fkey
    REFERENCES tour_schedule,
  sequence         INTEGER NOT NULL,
  title            VARCHAR(100),
  description      TEXT,
  transportation_type transportation_type DEFAULT 'public'::transportation_type,
  meeting_time     VARCHAR(100),
  meeting_location_name VARCHAR(50),
  meeting_location_map_zoom INTEGER DEFAULT 3,
  meeting_location_latitude DOUBLE PRECISION,
  meeting_location_longitude DOUBLE PRECISION,
  meeting_location_description TEXT,
  created_at                   TIMESTAMP DEFAULT now() NOT NULL,
  updated_at                   TIMESTAMP,
  deleted_at                   TIMESTAMP
);

COMMENT ON TABLE tour_schedule_day IS 'information about specific day in tour-plan. they are sequential about time.';

COMMENT ON COLUMN tour_schedule_day.id IS 'table identifier';

COMMENT ON COLUMN tour_schedule_day.tour_schedule_id IS 'reference tour-schedule';

COMMENT ON COLUMN tour_schedule_day.sequence IS 'sequence number';

COMMENT ON COLUMN tour_schedule_day.title IS 'title';

COMMENT ON COLUMN tour_schedule_day.description IS 'description';

COMMENT ON COLUMN tour_schedule_day.transportation_type IS 'kind of transportation in the day';

COMMENT ON COLUMN tour_schedule_day.meeting_time IS 'when meet with guide';

COMMENT ON COLUMN tour_schedule_day.meeting_location_name IS 'name of place where meet with guide';

COMMENT ON COLUMN tour_schedule_day.meeting_location_map_zoom IS 'zoom level of google map about place where meet with guide';

COMMENT ON COLUMN tour_schedule_day.meeting_location_latitude IS 'latitude of place where meet with guide';

COMMENT ON COLUMN tour_schedule_day.meeting_location_longitude IS 'longitude of place where meet with guide';

COMMENT ON COLUMN tour_schedule_day.meeting_location_description IS 'description about place where meet with guide';

COMMENT ON COLUMN tour_schedule_day.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN tour_schedule_day.updated_at IS 'timestamp when this record is updated';

COMMENT ON COLUMN tour_schedule_day.deleted_at IS 'present that this record is deleted';

CREATE TABLE tour_schedule_node
(
  id BIGSERIAL                                 NOT NULL
    CONSTRAINT tour_schedule_node_pkey1
    PRIMARY KEY,
  tour_schedule_day_id BIGINT NOT NULL
    CONSTRAINT tour_schedule_node_tour_schedule_day_id_fkey
    REFERENCES tour_schedule_day,
  course_id            BIGINT
    CONSTRAINT tour_schedule_node_course_id_fkey
    REFERENCES course,
  start                INTEGER NOT NULL,
  size                 INTEGER NOT NULL,
  title                VARCHAR(100) NOT NULL,
  description          TEXT,
  latitude             DOUBLE PRECISION,
  longitude            DOUBLE PRECISION,
  created_at           TIMESTAMP DEFAULT now() NOT NULL,
  updated_at           TIMESTAMP,
  deleted_at           TIMESTAMP
);

COMMENT ON TABLE tour_schedule_node IS 'information about course in specific tour day. detailed information about course is reference "product_course" entity.';

COMMENT ON COLUMN tour_schedule_node.id IS 'table identifier';

COMMENT ON COLUMN tour_schedule_node.tour_schedule_day_id IS 'reference tour-schedule-day';

COMMENT ON COLUMN tour_schedule_node.course_id IS 'reference course';

COMMENT ON COLUMN tour_schedule_node.start IS 'start time. unit is minute. "0" is mean twelve at night.';

COMMENT ON COLUMN tour_schedule_node.size IS 'period time about course. unit is minute.';

COMMENT ON COLUMN tour_schedule_node.title IS 'title';

COMMENT ON COLUMN tour_schedule_node.description IS 'description';

COMMENT ON COLUMN tour_schedule_node.latitude IS 'latitude of course';

COMMENT ON COLUMN tour_schedule_node.longitude IS 'longitude of course';

COMMENT ON COLUMN tour_schedule_node.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN tour_schedule_node.updated_at IS 'timestamp when this record is updated';

COMMENT ON COLUMN tour_schedule_node.deleted_at IS 'present that this record is deleted';

CREATE TABLE tour_schedule_node_media_file
(
  id BIGSERIAL                                  NOT NULL
    CONSTRAINT tour_schedule_node_media_file_pkey
    PRIMARY KEY,
  tour_schedule_node_id BIGINT NOT NULL
    CONSTRAINT tour_schedule_node_media_file_tour_schedule_node_id_fkey
    REFERENCES tour_schedule_node,
  public_file_id        BIGINT NOT NULL
    CONSTRAINT tour_schedule_node_media_file_public_file_id_fkey
    REFERENCES public_file,
  created_at            TIMESTAMP DEFAULT now() NOT NULL,
  updated_at            TIMESTAMP
);

COMMENT ON TABLE tour_schedule_node_media_file IS 'image file what is comprised in one tour-schedule-node';

COMMENT ON COLUMN tour_schedule_node_media_file.id IS 'table identifier';

COMMENT ON COLUMN tour_schedule_node_media_file.tour_schedule_node_id IS 'reference tour-schedule-node';

COMMENT ON COLUMN tour_schedule_node_media_file.public_file_id IS 'reference image file';

COMMENT ON COLUMN tour_schedule_node_media_file.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN tour_schedule_node_media_file.updated_at IS 'timestamp when this record is updated';

CREATE TABLE notice
(
  id BIGSERIAL                        NOT NULL
    CONSTRAINT notice_pkey
    PRIMARY KEY,
  user_id     BIGINT NOT NULL
    CONSTRAINT notice_user_id_fkey
    REFERENCES "user",
  title   TEXT   NOT NULL,
  description TEXT NOT NULL,
  created_at  TIMESTAMP DEFAULT now() NOT NULL,
  updated_at  TIMESTAMP,
  deleted_at  TIMESTAMP
);

COMMENT ON TABLE notice IS 'notify user(tourist/guide/guide manager) by administrator';

COMMENT ON COLUMN notice.id IS 'table identifier';

COMMENT ON COLUMN notice.user_id IS 'reference writer';

COMMENT ON COLUMN notice.title IS 'title';

COMMENT ON COLUMN notice.description IS 'description';

COMMENT ON COLUMN notice.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN notice.updated_at IS 'timestamp when this record is updated';

COMMENT ON COLUMN notice.deleted_at IS 'present that this record is deleted';

CREATE TABLE faq_category
(
  id BIGSERIAL                       NOT NULL
    CONSTRAINT faq_category_pkey
    PRIMARY KEY,
  name       VARCHAR(2048) NOT NULL,
  ref_id BIGINT
    CONSTRAINT faq_category_ref_id_fkey
    REFERENCES faq_category,
  created_at TIMESTAMP DEFAULT now() NOT NULL,
  updated_at TIMESTAMP,
  deleted_at TIMESTAMP
);

COMMENT ON TABLE faq_category IS 'category of "faq"';

COMMENT ON COLUMN faq_category.id IS 'table identifier';

COMMENT ON COLUMN faq_category.name IS 'name of category';

COMMENT ON COLUMN faq_category.ref_id IS 'refer anyone of "faq_category"';

COMMENT ON COLUMN faq_category.created_at IS 'timestamp when this record is created';

COMMENT ON COLUMN faq_category.updated_at IS 'timestamp when this record is updated';

COMMENT ON COLUMN faq_category.deleted_at IS 'present that this record is deleted';

ALTER TABLE faq
  ADD CONSTRAINT faq_category_id_fkey
FOREIGN KEY (category_id) REFERENCES faq_category;

CREATE TABLE service_type
(
  id BIGSERIAL       NOT NULL
    CONSTRAINT service_type_pkey
    PRIMARY KEY,
  name VARCHAR(2048) NOT NULL
    CONSTRAINT service_type_name_key
    UNIQUE
);

COMMENT ON TABLE service_type IS 'define service type for service provide by guide or guide manager';

COMMENT ON COLUMN service_type.id IS 'table identifier';

COMMENT ON COLUMN service_type.name IS 'name of service type';

CREATE TABLE service
(
  id              BIGINT NOT NULL
    CONSTRAINT service_pkey
    PRIMARY KEY,
  service_type_id BIGINT NOT NULL
    CONSTRAINT service_service_type_id_fkey
    REFERENCES service_type,
  transportation_type transportation_type,

  CONSTRAINT service_id_fkey
  FOREIGN KEY (id)
  REFERENCES product
);

COMMENT ON TABLE service IS 'define service for service provide by guide or guide manager';

COMMENT ON COLUMN service.id IS 'table identifier';

COMMENT ON COLUMN service.service_type_id IS 'representative service type';

COMMENT ON COLUMN service.transportation_type IS 'kind of transportation in the service';

CREATE TABLE tour
(
  id              BIGINT NOT NULL
    CONSTRAINT tour_pkey
    PRIMARY KEY,
  type tour_type NOT NULL,
  adjustable_time BOOLEAN DEFAULT FALSE,
  must_inquire    BOOLEAN DEFAULT FALSE,
  tour_scale tour_scale_type,
  due_date_type due_date_type,
  due_date        DOUBLE PRECISION,

  CONSTRAINT tour_id_fkey
  FOREIGN KEY (id)
  REFERENCES product
);

COMMENT ON TABLE tour IS 'selling tour-product by guide or guide manager';

COMMENT ON COLUMN tour.id IS 'table identifier';

COMMENT ON COLUMN tour.type IS 'tour type(unit tour/custom tour)';

COMMENT ON COLUMN tour.adjustable_time IS 'is adjustable when meet with guide';

COMMENT ON COLUMN tour.must_inquire IS 'must inquire, before reserve';

COMMENT ON COLUMN tour.tour_scale IS 'tour type(independent travel/associated travel)';

COMMENT ON COLUMN tour.due_date_type IS 'unit about period of trip';

COMMENT ON COLUMN tour.due_date IS 'period of trip';

CREATE TABLE unit_tour
(
  id                           BIGINT NOT NULL
    CONSTRAINT unit_tour_pkey
    PRIMARY KEY,
  transportation_type transportation_type,
  meeting_time VARCHAR(100),
  meeting_location_name VARCHAR(50),
  meeting_location_map_zoom INTEGER DEFAULT 3,
  meeting_location_latitude DOUBLE PRECISION,
  meeting_location_longitude DOUBLE PRECISION,
  meeting_location_description TEXT,

  CONSTRAINT unit_tour_id_fkey
  FOREIGN KEY (id)
  REFERENCES tour
);

COMMENT ON TABLE unit_tour IS 'selling tour-product by guide';

COMMENT ON COLUMN unit_tour.id IS 'table identifier';

COMMENT ON COLUMN unit_tour.transportation_type IS 'kind of transportation in the trip';

COMMENT ON COLUMN unit_tour.meeting_time IS 'when meet with guide';

COMMENT ON COLUMN unit_tour.meeting_location_name IS 'name of place where meet with guide';

COMMENT ON COLUMN unit_tour.meeting_location_map_zoom IS 'zoom level of google map about place where meet with guide';

COMMENT ON COLUMN unit_tour.meeting_location_latitude IS 'latitude of place where meet with guide';

COMMENT ON COLUMN unit_tour.meeting_location_longitude IS 'longitude of place where meet with guide';

COMMENT ON COLUMN unit_tour.meeting_location_description IS 'description about place where meet with guide';

ALTER TABLE course
  ADD CONSTRAINT course_unit_tour_id_fkey
FOREIGN KEY (unit_tour_id) REFERENCES unit_tour;

ALTER TABLE tour_sketch_node
  ADD CONSTRAINT tour_sketch_node_unit_tour_id_fkey
FOREIGN KEY (unit_tour_id) REFERENCES unit_tour;

CREATE TABLE custom_tour
(
  id           BIGINT NOT NULL
    CONSTRAINT custom_tour_pkey
    PRIMARY KEY,
  tour_plan_id BIGINT
    CONSTRAINT custom_tour_tour_plan_id_fkey
    REFERENCES tour_plan,

  CONSTRAINT custom_tour_id_fkey
  FOREIGN KEY (id)
  REFERENCES tour
);

COMMENT ON TABLE custom_tour IS 'selling tour-product by guide manager';

COMMENT ON COLUMN custom_tour.id IS 'table identifier';

COMMENT ON COLUMN custom_tour.tour_plan_id IS 'reference tour plan';
