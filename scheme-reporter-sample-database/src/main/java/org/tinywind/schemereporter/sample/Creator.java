/*
  Copyright (c) 2016, Jeon JaeHyeong (http://github.com/tinywind)
  All rights reserved.
  <p>
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package org.tinywind.schemereporter.sample;

import org.h2.Driver;
import org.h2.tools.Server;
import org.h2.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * @author tinywind
 */
public class Creator {
    private final static String ddl =
            "CREATE DOMAIN user_type AS enum ('admin', 'guide', 'tourist');\n" +
                    "\n" +
                    "CREATE DOMAIN gender_type AS enum ('male', 'female');\n" +
                    "\n" +
                    "CREATE DOMAIN account_status_type AS enum ('normalcy', 'blackout');\n" +
                    "\n" +
                    "CREATE DOMAIN guide_status_type AS enum ('standby', 'pass', 'reject');\n" +
                    "\n" +
                    "CREATE DOMAIN due_date_type AS enum ('day', 'hour');\n" +
                    "\n" +
                    "CREATE DOMAIN tour_scale_type AS enum ('solo', 'plural');\n" +
                    "\n" +
                    "CREATE DOMAIN age_type AS enum ('ten1', 'ten2', 'ten3', 'ten4', 'ten5', 'ten6', 'ten7', 'over');\n" +
                    "\n" +
                    "CREATE DOMAIN tour_sketch_node_type AS enum ('Folder', 'Item');\n" +
                    "\n" +
                    "CREATE DOMAIN guide_specialty_type AS enum ('common', 'special');\n" +
                    "\n" +
                    "CREATE DOMAIN order_type AS enum ('expensive', 'cheapest', 'popularity', 'newest');\n" +
                    "\n" +
                    "CREATE DOMAIN message_type AS enum ('total', 'received', 'sent');\n" +
                    "\n" +
                    "CREATE DOMAIN product_status_type AS enum ('temporary_save', 'defer', 'reject', 'standby', 'on_sale');\n" +
                    "\n" +
                    "CREATE DOMAIN tour_type AS enum ('unit', 'custom');\n" +
                    "\n" +
                    "CREATE DOMAIN message_tag AS enum ('notice', 'request_tour', 'reservation', 'inquire');\n" +
                    "\n" +
                    "CREATE DOMAIN reservation_status_type AS enum ('payment', 'confirmation', 'complete', 'canceled');\n" +
                    "\n" +
                    "CREATE DOMAIN payout_status_type AS enum ('do_not', 'request', 'done');\n" +
                    "\n" +
                    "CREATE DOMAIN transportation_type AS enum ('bicycle', 'car', 'public');\n" +
                    "\n" +
                    "CREATE DOMAIN product_type AS enum ('tour', 'service');\n" +
                    "\n" +
                    "CREATE DOMAIN payment_method_type AS enum ('card', 'deposit');\n" +
                    "\n" +
                    "CREATE TABLE public_file\n" +
                    "(\n" +
                    "  id BIGSERIAL                          NOT NULL\n" +
                    "    CONSTRAINT public_file_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  original_name VARCHAR(256)            NOT NULL,\n" +
                    "  name          VARCHAR(256)            NOT NULL,\n" +
                    "  size          BIGINT                  NOT NULL,\n" +
                    "  created_at    TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at    TIMESTAMP,\n" +
                    "  deleted_at    TIMESTAMP\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE public_file IS 'saved file at server';\n" +
                    "\n" +
                    "COMMENT ON COLUMN public_file.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN public_file.original_name IS 'file name what is file name of user end side in uploading';\n" +
                    "\n" +
                    "COMMENT ON COLUMN public_file.name IS 'saved file name at server side after upload';\n" +
                    "\n" +
                    "COMMENT ON COLUMN public_file.size IS 'file size';\n" +
                    "\n" +
                    "COMMENT ON COLUMN public_file.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN public_file.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "COMMENT ON COLUMN public_file.deleted_at IS 'present that this record is deleted';\n" +
                    "\n" +
                    "CREATE TABLE theme\n" +
                    "(\n" +
                    "  id BIGSERIAL  NOT NULL\n" +
                    "    CONSTRAINT theme_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  name VARCHAR(2048) NOT NULL,\n" +
                    "  ref_id BIGINT\n" +
                    "    CONSTRAINT theme_ref_id_fkey\n" +
                    "    REFERENCES theme,\n" +
                    "  trend  BOOLEAN DEFAULT FALSE NOT NULL,\n" +
                    "  image_id BIGINT\n" +
                    "    CONSTRAINT theme_image_id_fkey\n" +
                    "    REFERENCES public_file,\n" +
                    "  background_image_id BIGINT\n" +
                    "    CONSTRAINT theme_background_image_id_fkey\n" +
                    "    REFERENCES public_file,\n" +
                    "  created_at          TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at          TIMESTAMP,\n" +
                    "  deleted_at          TIMESTAMP\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE theme IS 'define theme';\n" +
                    "\n" +
                    "COMMENT ON COLUMN theme.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN theme.name IS 'name of theme';\n" +
                    "\n" +
                    "COMMENT ON COLUMN theme.ref_id IS 'reference anyone of \"theme\"';\n" +
                    "\n" +
                    "COMMENT ON COLUMN theme.trend IS 'is representative';\n" +
                    "\n" +
                    "COMMENT ON COLUMN theme.image_id IS 'reference icon';\n" +
                    "\n" +
                    "COMMENT ON COLUMN theme.background_image_id IS 'reference picture';\n" +
                    "\n" +
                    "COMMENT ON COLUMN theme.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN theme.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "COMMENT ON COLUMN theme.deleted_at IS 'present that this record is deleted';\n" +
                    "\n" +
                    "CREATE TABLE language\n" +
                    "(\n" +
                    "  id BIGSERIAL           NOT NULL\n" +
                    "    CONSTRAINT language_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  name      VARCHAR(2048) NOT NULL,\n" +
                    "  sort_name VARCHAR(100) NOT NULL\n" +
                    "    CONSTRAINT language_sort_name_key\n" +
                    "    UNIQUE\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE language IS 'define language for guiding';\n" +
                    "\n" +
                    "COMMENT ON COLUMN language.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN language.name IS 'name of language';\n" +
                    "\n" +
                    "COMMENT ON COLUMN language.sort_name IS 'name of language for sorting';\n" +
                    "\n" +
                    "CREATE TABLE keyword\n" +
                    "(\n" +
                    "  id BIGSERIAL       NOT NULL\n" +
                    "    CONSTRAINT keyword_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  name VARCHAR(2048) NOT NULL\n" +
                    "    CONSTRAINT keyword_name_key\n" +
                    "    UNIQUE\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE keyword IS 'define keyword for product classification';\n" +
                    "\n" +
                    "COMMENT ON COLUMN keyword.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN keyword.name IS 'name of keyword';\n" +
                    "\n" +
                    "CREATE TABLE currency\n" +
                    "(\n" +
                    "  id BIGSERIAL       NOT NULL\n" +
                    "    CONSTRAINT currency_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  name VARCHAR(2048) NOT NULL\n" +
                    "    CONSTRAINT currency_name_key\n" +
                    "    UNIQUE\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE currency IS 'define currency';\n" +
                    "\n" +
                    "COMMENT ON COLUMN currency.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN currency.name IS 'name of currency';\n" +
                    "\n" +
                    "CREATE TABLE location_country\n" +
                    "(\n" +
                    "  id BIGSERIAL          NOT NULL\n" +
                    "    CONSTRAINT location_country_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  name         VARCHAR(100) NOT NULL\n" +
                    "    CONSTRAINT location_country_name_key\n" +
                    "    UNIQUE,\n" +
                    "  sort_name VARCHAR(10) NOT NULL\n" +
                    "    CONSTRAINT location_country_sort_name_key\n" +
                    "    UNIQUE,\n" +
                    "  korean_name VARCHAR(100),\n" +
                    "  country_code INTEGER,\n" +
                    "  latitude     DOUBLE PRECISION,\n" +
                    "  longitude    DOUBLE PRECISION,\n" +
                    "  CONSTRAINT location_country_latitude_longitude_key\n" +
                    "  UNIQUE (latitude, longitude)\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE location_country IS 'real country';\n" +
                    "\n" +
                    "COMMENT ON COLUMN location_country.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN location_country.name IS 'name of country';\n" +
                    "\n" +
                    "COMMENT ON COLUMN location_country.sort_name IS 'name of country for sorting';\n" +
                    "\n" +
                    "COMMENT ON COLUMN location_country.korean_name IS 'korean name of country';\n" +
                    "\n" +
                    "COMMENT ON COLUMN location_country.country_code IS 'international dialling code';\n" +
                    "\n" +
                    "COMMENT ON COLUMN location_country.latitude IS 'latitude of country';\n" +
                    "\n" +
                    "COMMENT ON COLUMN location_country.longitude IS 'longitude of country';\n" +
                    "\n" +
                    "CREATE TABLE location_state\n" +
                    "(\n" +
                    "  id BIGSERIAL            NOT NULL\n" +
                    "    CONSTRAINT location_state_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  country_id BIGINT NOT NULL\n" +
                    "    CONSTRAINT location_state_country_id_fkey\n" +
                    "    REFERENCES location_country,\n" +
                    "  name       VARCHAR(100) NOT NULL,\n" +
                    "  latitude   DOUBLE PRECISION,\n" +
                    "  longitude  DOUBLE PRECISION,\n" +
                    "  CONSTRAINT location_state_country_id_name_key\n" +
                    "  UNIQUE (country_id, name),\n" +
                    "  CONSTRAINT location_state_latitude_longitude_key\n" +
                    "  UNIQUE (latitude, longitude)\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE location_state IS 'real state';\n" +
                    "\n" +
                    "COMMENT ON COLUMN location_state.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN location_state.country_id IS 'reference country';\n" +
                    "\n" +
                    "COMMENT ON COLUMN location_state.name IS 'name of state';\n" +
                    "\n" +
                    "COMMENT ON COLUMN location_state.latitude IS 'latitude of state';\n" +
                    "\n" +
                    "COMMENT ON COLUMN location_state.longitude IS 'longitude of state';\n" +
                    "\n" +
                    "CREATE TABLE location_city\n" +
                    "(\n" +
                    "  id BIGSERIAL          NOT NULL\n" +
                    "    CONSTRAINT location_city_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  state_id  BIGINT NOT NULL\n" +
                    "    CONSTRAINT location_city_state_id_fkey\n" +
                    "    REFERENCES location_state,\n" +
                    "  name     VARCHAR(100) NOT NULL,\n" +
                    "  latitude DOUBLE PRECISION,\n" +
                    "  longitude DOUBLE PRECISION,\n" +
                    "  CONSTRAINT location_city_state_id_name_key\n" +
                    "  UNIQUE (state_id, name),\n" +
                    "  CONSTRAINT location_city_latitude_longitude_key\n" +
                    "  UNIQUE (latitude, longitude)\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE location_city IS 'real city';\n" +
                    "\n" +
                    "COMMENT ON COLUMN location_city.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN location_city.state_id IS 'reference state';\n" +
                    "\n" +
                    "COMMENT ON COLUMN location_city.name IS 'name of city';\n" +
                    "\n" +
                    "COMMENT ON COLUMN location_city.latitude IS 'latitude of city';\n" +
                    "\n" +
                    "COMMENT ON COLUMN location_city.longitude IS 'longitude of city';\n" +
                    "\n" +
                    "CREATE TABLE country_x_language\n" +
                    "(\n" +
                    "  id BIGSERIAL       NOT NULL\n" +
                    "    CONSTRAINT country_x_language_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  country_id  BIGINT NOT NULL\n" +
                    "    CONSTRAINT country_x_language_country_id_fkey\n" +
                    "    REFERENCES location_country,\n" +
                    "  language_id BIGINT NOT NULL\n" +
                    "    CONSTRAINT country_x_language_language_id_fkey\n" +
                    "    REFERENCES language\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE country_x_language IS 'relationship of country with language. languages are used as representative country.';\n" +
                    "\n" +
                    "COMMENT ON COLUMN country_x_language.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN country_x_language.country_id IS 'reference country';\n" +
                    "\n" +
                    "COMMENT ON COLUMN country_x_language.language_id IS 'reference language';\n" +
                    "\n" +
                    "CREATE TABLE \"user\"\n" +
                    "(\n" +
                    "  id BIGSERIAL                                NOT NULL\n" +
                    "    CONSTRAINT user_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  type user_type NOT NULL,\n" +
                    "  email               VARCHAR(100),\n" +
                    "  first_name VARCHAR(100),\n" +
                    "  middle_name VARCHAR(100),\n" +
                    "  last_name   VARCHAR(100),\n" +
                    "  profile_image_id BIGINT\n" +
                    "    CONSTRAINT user_profile_image_id_fkey\n" +
                    "    REFERENCES public_file,\n" +
                    "  profile_description TEXT,\n" +
                    "  gender gender_type,\n" +
                    "  mobile_country_id   BIGINT\n" +
                    "    CONSTRAINT user_mobile_country_id_fkey\n" +
                    "    REFERENCES location_country,\n" +
                    "  mobile              VARCHAR(20),\n" +
                    "  nationality_id      BIGINT\n" +
                    "    CONSTRAINT user_nationality_id_fkey\n" +
                    "    REFERENCES location_country,\n" +
                    "  account_status account_status_type DEFAULT 'normalcy'::account_status_type NOT NULL,\n" +
                    "  created_at          TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at          TIMESTAMP,\n" +
                    "  deleted_at          TIMESTAMP,\n" +
                    "  agree_receive       BOOLEAN DEFAULT FALSE   NOT NULL,\n" +
                    "  locale              VARCHAR(16)\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON COLUMN \"user\".id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN \"user\".created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN \"user\".updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "COMMENT ON COLUMN \"user\".deleted_at IS 'present that this record is deleted';\n" +
                    "\n" +
                    "COMMENT ON COLUMN \"user\".id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN \"user\".created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN \"user\".updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "COMMENT ON COLUMN \"user\".deleted_at IS 'present that this record is deleted';\n" +
                    "\n" +
                    "CREATE TABLE admin\n" +
                    "(\n" +
                    "  id             BIGINT       NOT NULL\n" +
                    "    CONSTRAINT admin_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  login_id VARCHAR(100) NOT NULL\n" +
                    "    CONSTRAINT admin_login_id_key\n" +
                    "    UNIQUE,\n" +
                    "  login_password VARCHAR(100) NOT NULL,\n" +
                    "\n" +
                    "  CONSTRAINT admin_id_fkey\n" +
                    "  FOREIGN KEY (id)\n" +
                    "  REFERENCES \"user\" (id)\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE admin IS 'user of admin type';\n" +
                    "\n" +
                    "COMMENT ON COLUMN admin.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN admin.login_id IS 'login ID';\n" +
                    "\n" +
                    "COMMENT ON COLUMN admin.login_password IS 'login password';\n" +
                    "\n" +
                    "CREATE TABLE message\n" +
                    "(\n" +
                    "  id BIGSERIAL                             NOT NULL\n" +
                    "    CONSTRAINT message_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  ref_id           BIGINT\n" +
                    "    CONSTRAINT message_ref_id_fkey\n" +
                    "    REFERENCES message,\n" +
                    "  sender_id BIGINT NOT NULL\n" +
                    "    CONSTRAINT message_sender_id_fkey\n" +
                    "    REFERENCES \"user\",\n" +
                    "  receiver_id BIGINT NOT NULL\n" +
                    "    CONSTRAINT message_receiver_id_fkey\n" +
                    "    REFERENCES \"user\",\n" +
                    "  message_header VARCHAR(1024),\n" +
                    "  message_body   VARCHAR(2048) NOT NULL,\n" +
                    "  message_footer VARCHAR(1024),\n" +
                    "  read           BOOLEAN DEFAULT FALSE NOT NULL,\n" +
                    "  sender_deleted BOOLEAN DEFAULT FALSE NOT NULL,\n" +
                    "  receiver_deleted BOOLEAN DEFAULT FALSE NOT NULL,\n" +
                    "  created_at       TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at       TIMESTAMP,\n" +
                    "  deleted_at       TIMESTAMP,\n" +
                    "  tag message_tag,\n" +
                    "  tour_plan_id     BIGINT\n" +
                    ");\n" +
                    "\n" +
                    "CREATE INDEX message_sender_id_idx\n" +
                    "  ON message (sender_id);\n" +
                    "\n" +
                    "CREATE INDEX message_receiver_id_read_idx\n" +
                    "  ON message (receiver_id, read);\n" +
                    "\n" +
                    "CREATE INDEX message_receiver_id_idx\n" +
                    "  ON message (receiver_id);\n" +
                    "\n" +
                    "COMMENT ON TABLE message IS 'message form user to user';\n" +
                    "\n" +
                    "COMMENT ON COLUMN message.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN message.ref_id IS 'represented message';\n" +
                    "\n" +
                    "COMMENT ON COLUMN message.sender_id IS 'reference sender';\n" +
                    "\n" +
                    "COMMENT ON COLUMN message.receiver_id IS 'reference receiver';\n" +
                    "\n" +
                    "COMMENT ON COLUMN message.message_header IS 'header content of message';\n" +
                    "\n" +
                    "COMMENT ON COLUMN message.message_body IS 'body content of message';\n" +
                    "\n" +
                    "COMMENT ON COLUMN message.message_footer IS 'footer content of message';\n" +
                    "\n" +
                    "COMMENT ON COLUMN message.read IS 'read message by receiver';\n" +
                    "\n" +
                    "COMMENT ON COLUMN message.sender_deleted IS 'deleted message by sender';\n" +
                    "\n" +
                    "COMMENT ON COLUMN message.receiver_deleted IS 'deleted message by receiver';\n" +
                    "\n" +
                    "COMMENT ON COLUMN message.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN message.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "COMMENT ON COLUMN message.deleted_at IS 'present that this record is deleted';\n" +
                    "\n" +
                    "COMMENT ON COLUMN message.tag IS 'for classification';\n" +
                    "\n" +
                    "COMMENT ON COLUMN message.tour_plan_id IS 'reference tour-plan';\n" +
                    "\n" +
                    "CREATE TABLE guide_event_type\n" +
                    "(\n" +
                    "  id BIGSERIAL                       NOT NULL\n" +
                    "    CONSTRAINT guide_event_type_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  name       VARCHAR(255) NOT NULL\n" +
                    "    CONSTRAINT guide_event_type_name_key\n" +
                    "    UNIQUE,\n" +
                    "  marker VARCHAR(2048),\n" +
                    "  created_at TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at TIMESTAMP,\n" +
                    "  deleted_at TIMESTAMP\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE guide_event_type IS 'define promotion type about guide';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide_event_type.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide_event_type.name IS 'name of promotion';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide_event_type.marker IS 'mark as';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide_event_type.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide_event_type.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide_event_type.deleted_at IS 'present that this record is deleted';\n" +
                    "\n" +
                    "CREATE TABLE guide\n" +
                    "(\n" +
                    "  id                  BIGINT                  NOT NULL\n" +
                    "    CONSTRAINT guide_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  status guide_status_type DEFAULT 'standby'::guide_status_type NOT NULL,\n" +
                    "  guide_event_type_id BIGINT\n" +
                    "    CONSTRAINT guide_guide_event_type_id_fkey\n" +
                    "    REFERENCES guide_event_type,\n" +
                    "  login_id            VARCHAR(100) NOT NULL\n" +
                    "    CONSTRAINT guide_login_id_key\n" +
                    "    UNIQUE,\n" +
                    "  login_password      VARCHAR(100) NOT NULL,\n" +
                    "  email_generate_key  VARCHAR(255) NOT NULL,\n" +
                    "  email_certified_at  TIMESTAMP,\n" +
                    "  skype_id            VARCHAR(40),\n" +
                    "  residential_city_id BIGINT       NOT NULL\n" +
                    "    CONSTRAINT guide_residential_city_id_fkey\n" +
                    "    REFERENCES location_city,\n" +
                    "  experience          INTEGER,\n" +
                    "  age                 INTEGER,\n" +
                    "  created_at          TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at          TIMESTAMP,\n" +
                    "\n" +
                    "  CONSTRAINT guide_id_fkey\n" +
                    "  FOREIGN KEY (id)\n" +
                    "  REFERENCES \"user\"\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE guide IS 'user of guide/guide manager type';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide.status IS 'approved by administrator';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide.guide_event_type_id IS 'promotion about guide';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide.login_id IS 'login ID';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide.login_password IS 'login password';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide.email_generate_key IS 'generated key for certificating email';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide.email_certified_at IS 'timestamp when email is certificated';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide.skype_id IS 'skype ID';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide.residential_city_id IS 'city of residence';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide.experience IS 'n years experience about guiding';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide.age IS 'age';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "CREATE TABLE guide_x_license\n" +
                    "(\n" +
                    "  id BIGSERIAL                         NOT NULL\n" +
                    "    CONSTRAINT guide_x_license_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  guide_id                BIGINT NOT NULL\n" +
                    "    CONSTRAINT guide_x_license_guide_id_fkey\n" +
                    "    REFERENCES guide,\n" +
                    "  license_picture_file_id BIGINT\n" +
                    "    CONSTRAINT guide_x_license_license_picture_file_id_fkey\n" +
                    "    REFERENCES public_file,\n" +
                    "  license_name            VARCHAR(255) NOT NULL\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE guide_x_license IS 'relationship of guide with license.';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide_x_license.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide_x_license.guide_id IS 'reference guide';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide_x_license.license_picture_file_id IS 'reference picture file about license';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide_x_license.license_name IS 'name of license';\n" +
                    "\n" +
                    "CREATE TABLE guide_x_language\n" +
                    "(\n" +
                    "  id BIGSERIAL       NOT NULL\n" +
                    "    CONSTRAINT guide_x_language_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  guide_id    BIGINT NOT NULL\n" +
                    "    CONSTRAINT guide_x_language_guide_id_fkey\n" +
                    "    REFERENCES guide,\n" +
                    "  language_id BIGINT NOT NULL\n" +
                    "    CONSTRAINT guide_x_language_language_id_fkey\n" +
                    "    REFERENCES language,\n" +
                    "  CONSTRAINT guide_x_language_guide_id_language_id_key\n" +
                    "  UNIQUE (guide_id, language_id)\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE guide_x_language IS 'relationship of guide with language. One guide has 1 guiding-language or more.';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide_x_language.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide_x_language.guide_id IS 'reference guide';\n" +
                    "\n" +
                    "COMMENT ON COLUMN guide_x_language.language_id IS 'reference language';\n" +
                    "\n" +
                    "CREATE TABLE user_x_interest_guide\n" +
                    "(\n" +
                    "  id BIGSERIAL                       NOT NULL\n" +
                    "    CONSTRAINT user_x_interest_guide_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  user_id    BIGINT NOT NULL\n" +
                    "    CONSTRAINT user_x_interest_guide_user_id_fkey\n" +
                    "    REFERENCES \"user\",\n" +
                    "  guide_id BIGINT NOT NULL\n" +
                    "    CONSTRAINT user_x_interest_guide_guide_id_fkey\n" +
                    "    REFERENCES guide,\n" +
                    "  created_at TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at TIMESTAMP,\n" +
                    "  CONSTRAINT user_x_interest_guide_user_id_guide_id_key\n" +
                    "  UNIQUE (user_id, guide_id)\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE user_x_interest_guide IS 'relationship of user with guide.';\n" +
                    "\n" +
                    "COMMENT ON COLUMN user_x_interest_guide.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN user_x_interest_guide.user_id IS 'reference user';\n" +
                    "\n" +
                    "COMMENT ON COLUMN user_x_interest_guide.guide_id IS 'reference guide';\n" +
                    "\n" +
                    "COMMENT ON COLUMN user_x_interest_guide.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN user_x_interest_guide.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "CREATE TABLE tourist\n" +
                    "(\n" +
                    "  id         BIGINT                  NOT NULL\n" +
                    "    CONSTRAINT tourist_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  created_at TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at TIMESTAMP,\n" +
                    "\n" +
                    "  CONSTRAINT tourist_id_fkey\n" +
                    "  FOREIGN KEY (id)\n" +
                    "  REFERENCES \"user\"\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE tourist IS 'user of tourist type';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "CREATE TABLE tourist_facebook\n" +
                    "(\n" +
                    "  id          BIGINT                  NOT NULL\n" +
                    "    CONSTRAINT tourist_facebook_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  social_id VARCHAR(255) NOT NULL\n" +
                    "    CONSTRAINT tourist_facebook_social_id_key\n" +
                    "    UNIQUE,\n" +
                    "  first_name VARCHAR(100),\n" +
                    "  middle_name VARCHAR(100),\n" +
                    "  last_name   VARCHAR(100),\n" +
                    "  email       VARCHAR(255),\n" +
                    "  gender gender_type,\n" +
                    "  created_at  TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at  TIMESTAMP,\n" +
                    "\n" +
                    "  CONSTRAINT tourist_facebook_id_fkey\n" +
                    "  FOREIGN KEY (id)\n" +
                    "  REFERENCES tourist\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE tourist_facebook IS 'joined tourist through facebook';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_facebook.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_facebook.social_id IS 'facebook ID. login ID';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_facebook.first_name IS 'first name of tourist that is received from facebook';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_facebook.middle_name IS 'middle name of tourist that is received from facebook';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_facebook.last_name IS 'last name of tourist that is received from facebook';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_facebook.email IS 'email of tourist that is received from facebook';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_facebook.gender IS 'gender of tourist that is received from facebook';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_facebook.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_facebook.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "CREATE TABLE tourist_googleplus\n" +
                    "(\n" +
                    "  id         BIGINT                  NOT NULL\n" +
                    "    CONSTRAINT tourist_googleplus_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  social_id VARCHAR(255) NOT NULL\n" +
                    "    CONSTRAINT tourist_googleplus_social_id_key\n" +
                    "    UNIQUE,\n" +
                    "  full_name VARCHAR(255),\n" +
                    "  first_name VARCHAR(100),\n" +
                    "  last_name  VARCHAR(100),\n" +
                    "  email      VARCHAR(255),\n" +
                    "  gender gender_type,\n" +
                    "  created_at TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at TIMESTAMP,\n" +
                    "\n" +
                    "  CONSTRAINT tourist_googleplus_id_fkey\n" +
                    "  FOREIGN KEY (id)\n" +
                    "  REFERENCES tourist\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE tourist_googleplus IS 'joined tourist through googleplus';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_googleplus.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_googleplus.social_id IS 'googleplus ID. login ID';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_googleplus.full_name IS 'full name of tourist that is received from googleplus';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_googleplus.first_name IS 'first name of tourist that is received from googleplus';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_googleplus.last_name IS 'last name of tourist that is received from googleplus';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_googleplus.email IS 'email of tourist that is received from googleplus';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_googleplus.gender IS 'gender of tourist that is received from googleplus';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_googleplus.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_googleplus.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "CREATE TABLE tourist_twitter\n" +
                    "(\n" +
                    "  id          BIGINT                  NOT NULL\n" +
                    "    CONSTRAINT tourist_twitter_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  social_id VARCHAR(255) NOT NULL\n" +
                    "    CONSTRAINT tourist_twitter_social_id_key\n" +
                    "    UNIQUE,\n" +
                    "  screen_name VARCHAR(255) NOT NULL,\n" +
                    "  name        VARCHAR(255),\n" +
                    "  created_at  TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at  TIMESTAMP,\n" +
                    "\n" +
                    "  CONSTRAINT tourist_twitter_id_fkey\n" +
                    "  FOREIGN KEY (id)\n" +
                    "  REFERENCES tourist\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE tourist_twitter IS 'joined tourist through twitter';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_twitter.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_twitter.social_id IS 'twitter ID. login ID';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_twitter.screen_name IS 'screen-name of tourist that is received from twitter';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_twitter.name IS 'name of tourist that is received from twitter';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_twitter.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_twitter.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "CREATE TABLE tourist_kakaotalk\n" +
                    "(\n" +
                    "  id         BIGINT                  NOT NULL\n" +
                    "    CONSTRAINT tourist_kakaotalk_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  social_id VARCHAR(255) NOT NULL\n" +
                    "    CONSTRAINT tourist_kakaotalk_social_id_key\n" +
                    "    UNIQUE,\n" +
                    "  nickname  VARCHAR(255) NOT NULL,\n" +
                    "  created_at TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at TIMESTAMP,\n" +
                    "\n" +
                    "  CONSTRAINT tourist_kakaotalk_id_fkey\n" +
                    "  FOREIGN KEY (id)\n" +
                    "  REFERENCES tourist\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE tourist_kakaotalk IS 'joined tourist through kakaotalk';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_kakaotalk.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_kakaotalk.social_id IS 'kakaotalk ID. login ID';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_kakaotalk.nickname IS 'nickname of tourist that is received from kakaotalk';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_kakaotalk.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_kakaotalk.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "CREATE TABLE tourist_email\n" +
                    "(\n" +
                    "  id           BIGINT                  NOT NULL\n" +
                    "    CONSTRAINT tourist_email_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  email VARCHAR(100) NOT NULL\n" +
                    "    CONSTRAINT tourist_email_email_key\n" +
                    "    UNIQUE,\n" +
                    "  password VARCHAR(100) NOT NULL,\n" +
                    "  generate_key VARCHAR(255) NOT NULL,\n" +
                    "  certified_at TIMESTAMP,\n" +
                    "  created_at   TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at   TIMESTAMP,\n" +
                    "\n" +
                    "  CONSTRAINT tourist_email_id_fkey\n" +
                    "  FOREIGN KEY (id)\n" +
                    "  REFERENCES tourist\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE tourist_email IS 'joined tourist through input email information';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_email.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_email.email IS 'email of tourist. login ID';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_email.password IS 'login password';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_email.generate_key IS 'generated key for certificating email';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_email.certified_at IS 'timestamp when email is certificated';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_email.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tourist_email.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "CREATE TABLE product_event_type\n" +
                    "(\n" +
                    "  id BIGSERIAL                       NOT NULL\n" +
                    "    CONSTRAINT product_event_type_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  name       VARCHAR(255) NOT NULL\n" +
                    "    CONSTRAINT product_event_type_name_key\n" +
                    "    UNIQUE,\n" +
                    "  discount INTEGER DEFAULT 0 NOT NULL,\n" +
                    "  marker   VARCHAR(2048),\n" +
                    "  created_at TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at TIMESTAMP,\n" +
                    "  deleted_at TIMESTAMP\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE product_event_type IS 'define promotion type about product';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_event_type.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_event_type.name IS 'name of promotion';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_event_type.discount IS 'discount rate(0 ~ 99)';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_event_type.marker IS 'mark as';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_event_type.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_event_type.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_event_type.deleted_at IS 'present that this record is deleted';\n" +
                    "\n" +
                    "CREATE TABLE product\n" +
                    "(\n" +
                    "  id BIGSERIAL                                  NOT NULL\n" +
                    "    CONSTRAINT product_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  title                 VARCHAR(100),\n" +
                    "  status product_status_type DEFAULT 'temporary_save'::product_status_type,\n" +
                    "  product_event_type_id BIGINT\n" +
                    "    CONSTRAINT product_product_event_type_id_fkey\n" +
                    "    REFERENCES product_event_type,\n" +
                    "  seller_id             BIGINT\n" +
                    "    CONSTRAINT product_seller_id_fkey\n" +
                    "    REFERENCES \"user\",\n" +
                    "  currency_id           BIGINT\n" +
                    "    CONSTRAINT product_currency_id_fkey\n" +
                    "    REFERENCES currency,\n" +
                    "  image_id              BIGINT,\n" +
                    "  summary               TEXT,\n" +
                    "  description           TEXT,\n" +
                    "  inclusion             TEXT,\n" +
                    "  exclusion             TEXT,\n" +
                    "  etc                   TEXT,\n" +
                    "  created_at            TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at            TIMESTAMP,\n" +
                    "  deleted_at            TIMESTAMP,\n" +
                    "  unload                VARCHAR(1000),\n" +
                    "  type product_type DEFAULT 'tour'::product_type NOT NULL\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE product IS 'selling product by guide or guide manager';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product.title IS 'title of product';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product.status IS 'approved by administrator';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product.product_event_type_id IS 'promotion about product';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product.seller_id IS 'reference seller';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product.currency_id IS 'base currency';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product.image_id IS 'representative image of product';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product.summary IS 'summary of product';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product.description IS 'description';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product.inclusion IS 'extra options what are include in the price';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product.exclusion IS 'extra options what are not include in the price';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product.etc IS 'extra description';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product.deleted_at IS 'present that this record is deleted';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product.unload IS 'request message for stop sale product';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product.type IS 'product type(tour/service)';\n" +
                    "\n" +
                    "CREATE TABLE product_price\n" +
                    "(\n" +
                    "  id BIGSERIAL                           NOT NULL\n" +
                    "    CONSTRAINT product_price_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  product_id     BIGINT NOT NULL\n" +
                    "    CONSTRAINT product_price_product_id_fkey\n" +
                    "    REFERENCES product,\n" +
                    "  minimum_people INTEGER,\n" +
                    "  maximum_people INTEGER,\n" +
                    "  price          DOUBLE PRECISION,\n" +
                    "  created_at     TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at     TIMESTAMP,\n" +
                    "  CONSTRAINT product_price_product_id_minimum_people_maximum_people_key\n" +
                    "  UNIQUE (product_id, minimum_people, maximum_people)\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE product_price IS 'price information of product. price of one product is comprised one price information or more.';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_price.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_price.product_id IS 'reference product';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_price.minimum_people IS 'minimum number of people is affected by this price information';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_price.maximum_people IS 'maximum number of people is affected by this price information';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_price.price IS 'price';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_price.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_price.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "CREATE TABLE course\n" +
                    "(\n" +
                    "  id BIGSERIAL                         NOT NULL\n" +
                    "    CONSTRAINT product_course_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  unit_tour_id BIGINT,\n" +
                    "  title        VARCHAR(100),\n" +
                    "  description  TEXT,\n" +
                    "  sequence     INTEGER NOT NULL,\n" +
                    "  created_at   TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at   TIMESTAMP,\n" +
                    "  deleted_at   TIMESTAMP\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE course IS 'component what constitutes product. they are sequential about time.';\n" +
                    "\n" +
                    "COMMENT ON COLUMN course.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN course.unit_tour_id IS 'reference product';\n" +
                    "\n" +
                    "COMMENT ON COLUMN course.title IS 'title of course';\n" +
                    "\n" +
                    "COMMENT ON COLUMN course.description IS 'description';\n" +
                    "\n" +
                    "COMMENT ON COLUMN course.sequence IS 'sequence number';\n" +
                    "\n" +
                    "COMMENT ON COLUMN course.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN course.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "COMMENT ON COLUMN course.deleted_at IS 'present that this record is deleted';\n" +
                    "\n" +
                    "CREATE TABLE course_x_media_file\n" +
                    "(\n" +
                    "  id BIGSERIAL                           NOT NULL\n" +
                    "    CONSTRAINT product_course_x_media_file_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  course_id      BIGINT NOT NULL\n" +
                    "    CONSTRAINT product_course_x_media_file_product_course_id_fkey\n" +
                    "    REFERENCES course,\n" +
                    "  public_file_id BIGINT NOT NULL\n" +
                    "    CONSTRAINT product_course_x_media_file_public_file_id_fkey\n" +
                    "    REFERENCES public_file,\n" +
                    "  created_at     TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at     TIMESTAMP\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE course_x_media_file IS 'image file what is comprised in one course.';\n" +
                    "\n" +
                    "COMMENT ON COLUMN course_x_media_file.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN course_x_media_file.course_id IS 'reference course';\n" +
                    "\n" +
                    "COMMENT ON COLUMN course_x_media_file.public_file_id IS 'reference image file';\n" +
                    "\n" +
                    "COMMENT ON COLUMN course_x_media_file.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN course_x_media_file.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "CREATE TABLE product_x_city\n" +
                    "(\n" +
                    "  id BIGSERIAL                       NOT NULL\n" +
                    "    CONSTRAINT product_x_city_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  product_id BIGINT NOT NULL\n" +
                    "    CONSTRAINT product_x_city_product_id_fkey\n" +
                    "    REFERENCES product,\n" +
                    "  city_id    BIGINT NOT NULL\n" +
                    "    CONSTRAINT product_x_city_city_id_fkey\n" +
                    "    REFERENCES location_city,\n" +
                    "  created_at TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at TIMESTAMP,\n" +
                    "  deleted_at TIMESTAMP,\n" +
                    "  CONSTRAINT product_x_city_product_id_city_id_key\n" +
                    "  UNIQUE (product_id, city_id)\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE product_x_city IS 'relationship of product with city. tour city is one or more.';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_x_city.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_x_city.product_id IS 'reference product';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_x_city.city_id IS 'reference city';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_x_city.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_x_city.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_x_city.deleted_at IS 'present that this record is deleted';\n" +
                    "\n" +
                    "CREATE TABLE product_x_keyword\n" +
                    "(\n" +
                    "  id BIGSERIAL                       NOT NULL\n" +
                    "    CONSTRAINT product_x_keyword_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  product_id BIGINT NOT NULL\n" +
                    "    CONSTRAINT product_x_keyword_product_id_fkey\n" +
                    "    REFERENCES product,\n" +
                    "  keyword_id BIGINT NOT NULL\n" +
                    "    CONSTRAINT product_x_keyword_keyword_id_fkey\n" +
                    "    REFERENCES keyword,\n" +
                    "  created_at TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at TIMESTAMP,\n" +
                    "  deleted_at TIMESTAMP\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE product_x_keyword IS 'relationship of product with keyword.';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_x_keyword.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_x_keyword.product_id IS 'reference product';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_x_keyword.keyword_id IS 'reference keyword';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_x_keyword.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_x_keyword.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_x_keyword.deleted_at IS 'present that this record is deleted';\n" +
                    "\n" +
                    "CREATE TABLE product_x_theme\n" +
                    "(\n" +
                    "  id BIGSERIAL                       NOT NULL\n" +
                    "    CONSTRAINT product_x_theme_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  product_id BIGINT NOT NULL\n" +
                    "    CONSTRAINT product_x_theme_product_id_fkey\n" +
                    "    REFERENCES product,\n" +
                    "  theme_id   BIGINT NOT NULL\n" +
                    "    CONSTRAINT product_x_theme_theme_id_fkey\n" +
                    "    REFERENCES theme,\n" +
                    "  created_at TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at TIMESTAMP,\n" +
                    "  deleted_at TIMESTAMP\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE product_x_theme IS 'relationship of product with theme';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_x_theme.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_x_theme.product_id IS 'reference product';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_x_theme.theme_id IS 'reference theme';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_x_theme.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_x_theme.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_x_theme.deleted_at IS 'present that this record is deleted';\n" +
                    "\n" +
                    "CREATE TABLE product_reservation\n" +
                    "(\n" +
                    "  id BIGSERIAL                                 NOT NULL\n" +
                    "    CONSTRAINT product_reservation_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  user_id             BIGINT NOT NULL\n" +
                    "    CONSTRAINT product_reservation_user_id_fkey\n" +
                    "    REFERENCES \"user\",\n" +
                    "  product_id BIGINT NOT NULL\n" +
                    "    CONSTRAINT product_reservation_product_id_fkey\n" +
                    "    REFERENCES product,\n" +
                    "  currency_id BIGINT NOT NULL\n" +
                    "    CONSTRAINT product_reservation_currency_id_fkey\n" +
                    "    REFERENCES currency,\n" +
                    "  book_date   DATE   NOT NULL,\n" +
                    "  number_people INTEGER NOT NULL,\n" +
                    "  age age_type,\n" +
                    "  purpose_id    BIGINT\n" +
                    "    CONSTRAINT product_reservation_purpose_id_fkey\n" +
                    "    REFERENCES theme,\n" +
                    "  request_message TEXT,\n" +
                    "  status reservation_status_type DEFAULT 'payment'::reservation_status_type NOT NULL,\n" +
                    "  name            VARCHAR(100) NOT NULL,\n" +
                    "  english_name    VARCHAR(100) NOT NULL,\n" +
                    "  mobile_country_id BIGINT     NOT NULL\n" +
                    "    CONSTRAINT product_reservation_mobile_country_id_fkey\n" +
                    "    REFERENCES location_country,\n" +
                    "  mobile            VARCHAR(20) NOT NULL,\n" +
                    "  email             VARCHAR(100) NOT NULL,\n" +
                    "  payout_status payout_status_type DEFAULT 'do_not'::payout_status_type NOT NULL,\n" +
                    "  created_at        TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at        TIMESTAMP,\n" +
                    "  deleted_at        TIMESTAMP,\n" +
                    "  event_name        VARCHAR(255),\n" +
                    "  event_discount    INTEGER DEFAULT 0       NOT NULL,\n" +
                    "  original_price    DOUBLE PRECISION DEFAULT 0 NOT NULL,\n" +
                    "  final_price       DOUBLE PRECISION DEFAULT 0 NOT NULL,\n" +
                    "  epilogue_star_point INTEGER,\n" +
                    "  epilogue_comment    TEXT,\n" +
                    "  epilogue_wrote_at   TIMESTAMP,\n" +
                    "  epilogue_reply      TEXT\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE product_reservation IS 'reservation of product';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.user_id IS 'reference people who has booked';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.product_id IS 'reference product';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.currency_id IS 'reference currency';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.book_date IS 'booked date';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.number_people IS 'number of people';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.age IS 'age of representative';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.purpose_id IS 'purpose of tour. reference trend theme.';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.request_message IS 'request message about tour';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.status IS 'tour progress situation';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.name IS 'name of representative';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.english_name IS 'english name of representative';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.mobile_country_id IS 'reference activated country of cellphone';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.mobile IS 'mobile number of representative';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.email IS 'email of representative';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.payout_status IS 'is paid';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.deleted_at IS 'present that this record is deleted';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.event_name IS 'promotion about product';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.event_discount IS 'promotion about product';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.original_price IS 'non-discounted price by event';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.final_price IS 'discounted price by event';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.epilogue_star_point IS 'score (0 ~ 5) about tour satisfaction';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.epilogue_comment IS 'comment of epilogue';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_reservation.epilogue_wrote_at IS 'timestamp when this epilogue is wrote';\n" +
                    "\n" +
                    "CREATE TABLE confirmation\n" +
                    "(\n" +
                    "  id               BIGINT                  NOT NULL\n" +
                    "    CONSTRAINT confirmation_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  name VARCHAR(100) NOT NULL,\n" +
                    "  english_name VARCHAR(100) NOT NULL,\n" +
                    "  email        VARCHAR(100) NOT NULL,\n" +
                    "  phone        VARCHAR(100) NOT NULL,\n" +
                    "  tour_title   VARCHAR(100) NOT NULL,\n" +
                    "  tourist_num  VARCHAR(100) NOT NULL,\n" +
                    "  tour_date    VARCHAR(100) NOT NULL,\n" +
                    "  cost         VARCHAR(100) NOT NULL,\n" +
                    "  paid         VARCHAR(100) NOT NULL,\n" +
                    "  contactable_time VARCHAR(100) NOT NULL,\n" +
                    "  meeting_place    VARCHAR(100) NOT NULL,\n" +
                    "  guide_phone      VARCHAR(100) NOT NULL,\n" +
                    "  guide            VARCHAR(100) NOT NULL,\n" +
                    "  created_at       TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at       TIMESTAMP,\n" +
                    "  deleted_at       TIMESTAMP,\n" +
                    "\n" +
                    "  CONSTRAINT confirmation_id_fkey\n" +
                    "  FOREIGN KEY (id)\n" +
                    "  REFERENCES product_reservation\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE confirmation IS 'serves as evidence of tour';\n" +
                    "\n" +
                    "COMMENT ON COLUMN confirmation.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN confirmation.name IS 'name of tourist';\n" +
                    "\n" +
                    "COMMENT ON COLUMN confirmation.english_name IS 'english name of tourist';\n" +
                    "\n" +
                    "COMMENT ON COLUMN confirmation.email IS 'email of tourist';\n" +
                    "\n" +
                    "COMMENT ON COLUMN confirmation.phone IS 'phone number of tourist';\n" +
                    "\n" +
                    "COMMENT ON COLUMN confirmation.tour_title IS 'tour title';\n" +
                    "\n" +
                    "COMMENT ON COLUMN confirmation.tourist_num IS 'tourist number';\n" +
                    "\n" +
                    "COMMENT ON COLUMN confirmation.tour_date IS 'day when tour start';\n" +
                    "\n" +
                    "COMMENT ON COLUMN confirmation.cost IS 'total tour cost';\n" +
                    "\n" +
                    "COMMENT ON COLUMN confirmation.paid IS 'present payment';\n" +
                    "\n" +
                    "COMMENT ON COLUMN confirmation.contactable_time IS 'contactable time with guide';\n" +
                    "\n" +
                    "COMMENT ON COLUMN confirmation.meeting_place IS 'place where meet with guide';\n" +
                    "\n" +
                    "COMMENT ON COLUMN confirmation.guide_phone IS 'phone number of guide';\n" +
                    "\n" +
                    "COMMENT ON COLUMN confirmation.guide IS 'name of guide';\n" +
                    "\n" +
                    "COMMENT ON COLUMN confirmation.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN confirmation.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "COMMENT ON COLUMN confirmation.deleted_at IS 'present that this record is deleted';\n" +
                    "\n" +
                    "CREATE TABLE user_x_interest_product\n" +
                    "(\n" +
                    "  id BIGSERIAL                       NOT NULL\n" +
                    "    CONSTRAINT user_x_interest_product_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  user_id    BIGINT NOT NULL\n" +
                    "    CONSTRAINT user_x_interest_product_user_id_fkey\n" +
                    "    REFERENCES \"user\",\n" +
                    "  product_id BIGINT NOT NULL\n" +
                    "    CONSTRAINT user_x_interest_product_product_id_fkey\n" +
                    "    REFERENCES product,\n" +
                    "  created_at TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at TIMESTAMP,\n" +
                    "  CONSTRAINT user_x_interest_product_user_id_product_id_key\n" +
                    "  UNIQUE (user_id, product_id)\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE user_x_interest_product IS 'relationship of user with product.';\n" +
                    "\n" +
                    "COMMENT ON COLUMN user_x_interest_product.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN user_x_interest_product.user_id IS 'reference user';\n" +
                    "\n" +
                    "COMMENT ON COLUMN user_x_interest_product.product_id IS 'reference product';\n" +
                    "\n" +
                    "COMMENT ON COLUMN user_x_interest_product.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN user_x_interest_product.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "CREATE TABLE tour_plan\n" +
                    "(\n" +
                    "  id BIGSERIAL                        NOT NULL\n" +
                    "    CONSTRAINT tour_plan_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  user_id     BIGINT NOT NULL\n" +
                    "    CONSTRAINT tour_plan_user_id_fkey\n" +
                    "    REFERENCES \"user\",\n" +
                    "  title   VARCHAR(200) NOT NULL,\n" +
                    "  description TEXT,\n" +
                    "  start_at    DATE,\n" +
                    "  end_at      DATE,\n" +
                    "  created_at  TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at  TIMESTAMP,\n" +
                    "  deleted_at  TIMESTAMP\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE tour_plan IS 'plan tour by user. also it is used component of custom tour.';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_plan.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_plan.user_id IS 'reference writer';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_plan.title IS 'title of tour-plan';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_plan.description IS 'description';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_plan.start_at IS 'start date of tour';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_plan.end_at IS 'end date of tour';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_plan.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_plan.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_plan.deleted_at IS 'present that this record is deleted';\n" +
                    "\n" +
                    "ALTER TABLE message\n" +
                    "  ADD CONSTRAINT message_tour_plan_id_fkey\n" +
                    "FOREIGN KEY (tour_plan_id) REFERENCES tour_plan;\n" +
                    "\n" +
                    "CREATE TABLE tour_plan_x_theme\n" +
                    "(\n" +
                    "  id BIGSERIAL                         NOT NULL\n" +
                    "    CONSTRAINT tour_plan_x_theme_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  tour_plan_id BIGINT NOT NULL\n" +
                    "    CONSTRAINT tour_plan_x_theme_tour_plan_id_fkey\n" +
                    "    REFERENCES tour_plan,\n" +
                    "  theme_id     BIGINT NOT NULL\n" +
                    "    CONSTRAINT tour_plan_x_theme_theme_id_fkey\n" +
                    "    REFERENCES theme,\n" +
                    "  created_at   TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at   TIMESTAMP,\n" +
                    "  CONSTRAINT tour_plan_x_theme_tour_plan_id_theme_id_key\n" +
                    "  UNIQUE (tour_plan_id, theme_id)\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE tour_plan_x_theme IS 'relationship of tour-plan with theme';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_plan_x_theme.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_plan_x_theme.tour_plan_id IS 'reference tour-plan';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_plan_x_theme.theme_id IS 'reference theme';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_plan_x_theme.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_plan_x_theme.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "CREATE TABLE tour_sketch\n" +
                    "(\n" +
                    "  id         BIGINT                  NOT NULL\n" +
                    "    CONSTRAINT tour_sketch_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  created_at TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at TIMESTAMP,\n" +
                    "\n" +
                    "  CONSTRAINT tour_sketch_id_fkey\n" +
                    "  FOREIGN KEY (id)\n" +
                    "  REFERENCES tour_plan\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE tour_sketch IS 'collection tour-information';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_sketch.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_sketch.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_sketch.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "CREATE TABLE tour_sketch_node\n" +
                    "(\n" +
                    "  id BIGSERIAL                           NOT NULL\n" +
                    "    CONSTRAINT tour_sketch_node_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  tour_sketch_id BIGINT NOT NULL\n" +
                    "    CONSTRAINT tour_sketch_node_tour_sketch_id_fkey\n" +
                    "    REFERENCES tour_sketch,\n" +
                    "  sequence       DOUBLE PRECISION NOT NULL,\n" +
                    "  name           VARCHAR(200)     NOT NULL,\n" +
                    "  ref_id         BIGINT\n" +
                    "    CONSTRAINT tour_sketch_node_ref_id_fkey\n" +
                    "    REFERENCES tour_sketch_node,\n" +
                    "  type tour_sketch_node_type NOT NULL,\n" +
                    "  unit_tour_id   BIGINT,\n" +
                    "  created_at     TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at     TIMESTAMP\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE tour_sketch_node IS 'tour-information';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_sketch_node.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_sketch_node.tour_sketch_id IS 'reference tour-sketch';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_sketch_node.sequence IS 'sequence number';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_sketch_node.name IS 'name of information';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_sketch_node.ref_id IS 'reference tour-sketch-node';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_sketch_node.type IS 'tour-sketch-node is two types: folder(no information. for classification), item(belong folder-node)';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_sketch_node.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_sketch_node.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "CREATE TABLE tour_schedule\n" +
                    "(\n" +
                    "  id BIGSERIAL                         NOT NULL\n" +
                    "    CONSTRAINT tour_schedule_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  tour_plan_id BIGINT NOT NULL\n" +
                    "    CONSTRAINT tour_schedule_tour_plan_id_fkey\n" +
                    "    REFERENCES tour_plan,\n" +
                    "  created_at   TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at   TIMESTAMP,\n" +
                    "  deleted_at   TIMESTAMP\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE tour_schedule IS 'schedule sheet of tour-plan';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule.tour_plan_id IS 'reference tour-plan';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule.deleted_at IS 'present that this record is deleted';\n" +
                    "\n" +
                    "CREATE TABLE faq\n" +
                    "(\n" +
                    "  id BIGSERIAL                       NOT NULL\n" +
                    "    CONSTRAINT faq_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  user_id     BIGINT NOT NULL\n" +
                    "    CONSTRAINT faq_user_id_fkey\n" +
                    "    REFERENCES \"user\",\n" +
                    "  question TEXT  NOT NULL,\n" +
                    "  answer   TEXT  NOT NULL,\n" +
                    "  created_at TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at TIMESTAMP,\n" +
                    "  deleted_at TIMESTAMP,\n" +
                    "  category_id BIGINT\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE faq IS 'frequently asked questions';\n" +
                    "\n" +
                    "COMMENT ON COLUMN faq.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN faq.user_id IS 'reference writer';\n" +
                    "\n" +
                    "COMMENT ON COLUMN faq.question IS 'question';\n" +
                    "\n" +
                    "COMMENT ON COLUMN faq.answer IS 'answer';\n" +
                    "\n" +
                    "COMMENT ON COLUMN faq.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN faq.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "COMMENT ON COLUMN faq.deleted_at IS 'present that this record is deleted';\n" +
                    "\n" +
                    "COMMENT ON COLUMN faq.category_id IS 'reference category';\n" +
                    "\n" +
                    "CREATE TABLE seller_impossible_date\n" +
                    "(\n" +
                    "  seller_id BIGINT NOT NULL\n" +
                    "    CONSTRAINT seller_impossible_date_seller_id_fkey\n" +
                    "    REFERENCES guide,\n" +
                    "  date      DATE   NOT NULL,\n" +
                    "  CONSTRAINT seller_impossible_date_pkey\n" +
                    "  PRIMARY KEY (seller_id, date)\n" +
                    ");\n" +
                    "\n" +
                    "CREATE INDEX seller_impossible_date_seller_id_idx\n" +
                    "  ON seller_impossible_date (seller_id);\n" +
                    "\n" +
                    "COMMENT ON TABLE seller_impossible_date IS 'impossible date to reserve a product what is sale by seller';\n" +
                    "\n" +
                    "COMMENT ON COLUMN seller_impossible_date.seller_id IS 'reference seller';\n" +
                    "\n" +
                    "COMMENT ON COLUMN seller_impossible_date.date IS 'impossible date';\n" +
                    "\n" +
                    "CREATE TABLE product_impossible_date\n" +
                    "(\n" +
                    "  product_id BIGINT NOT NULL\n" +
                    "    CONSTRAINT product_impossible_date_product_id_fkey\n" +
                    "    REFERENCES product,\n" +
                    "  date       DATE   NOT NULL,\n" +
                    "  CONSTRAINT product_impossible_date_pkey\n" +
                    "  PRIMARY KEY (product_id, date)\n" +
                    ");\n" +
                    "\n" +
                    "CREATE INDEX product_impossible_date_product_id_idx\n" +
                    "  ON product_impossible_date (product_id);\n" +
                    "\n" +
                    "COMMENT ON TABLE product_impossible_date IS 'impossible date to reserve a product';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_impossible_date.product_id IS 'reference product';\n" +
                    "\n" +
                    "COMMENT ON COLUMN product_impossible_date.date IS 'impossible date';\n" +
                    "\n" +
                    "CREATE TABLE tour_schedule_day\n" +
                    "(\n" +
                    "  id BIGSERIAL                                         NOT NULL\n" +
                    "    CONSTRAINT tour_schedule_day_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  tour_schedule_id             BIGINT NOT NULL\n" +
                    "    CONSTRAINT tour_schedule_day_tour_schedule_id_fkey\n" +
                    "    REFERENCES tour_schedule,\n" +
                    "  sequence         INTEGER NOT NULL,\n" +
                    "  title            VARCHAR(100),\n" +
                    "  description      TEXT,\n" +
                    "  transportation_type transportation_type DEFAULT 'public'::transportation_type,\n" +
                    "  meeting_time     VARCHAR(100),\n" +
                    "  meeting_location_name VARCHAR(50),\n" +
                    "  meeting_location_map_zoom INTEGER DEFAULT 3,\n" +
                    "  meeting_location_latitude DOUBLE PRECISION,\n" +
                    "  meeting_location_longitude DOUBLE PRECISION,\n" +
                    "  meeting_location_description TEXT,\n" +
                    "  created_at                   TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at                   TIMESTAMP,\n" +
                    "  deleted_at                   TIMESTAMP\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE tour_schedule_day IS 'information about specific day in tour-plan. they are sequential about time.';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_day.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_day.tour_schedule_id IS 'reference tour-schedule';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_day.sequence IS 'sequence number';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_day.title IS 'title';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_day.description IS 'description';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_day.transportation_type IS 'kind of transportation in the day';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_day.meeting_time IS 'when meet with guide';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_day.meeting_location_name IS 'name of place where meet with guide';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_day.meeting_location_map_zoom IS 'zoom level of google map about place where meet with guide';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_day.meeting_location_latitude IS 'latitude of place where meet with guide';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_day.meeting_location_longitude IS 'longitude of place where meet with guide';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_day.meeting_location_description IS 'description about place where meet with guide';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_day.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_day.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_day.deleted_at IS 'present that this record is deleted';\n" +
                    "\n" +
                    "CREATE TABLE tour_schedule_node\n" +
                    "(\n" +
                    "  id BIGSERIAL                                 NOT NULL\n" +
                    "    CONSTRAINT tour_schedule_node_pkey1\n" +
                    "    PRIMARY KEY,\n" +
                    "  tour_schedule_day_id BIGINT NOT NULL\n" +
                    "    CONSTRAINT tour_schedule_node_tour_schedule_day_id_fkey\n" +
                    "    REFERENCES tour_schedule_day,\n" +
                    "  course_id            BIGINT\n" +
                    "    CONSTRAINT tour_schedule_node_course_id_fkey\n" +
                    "    REFERENCES course,\n" +
                    "  start                INTEGER NOT NULL,\n" +
                    "  size                 INTEGER NOT NULL,\n" +
                    "  title                VARCHAR(100) NOT NULL,\n" +
                    "  description          TEXT,\n" +
                    "  latitude             DOUBLE PRECISION,\n" +
                    "  longitude            DOUBLE PRECISION,\n" +
                    "  created_at           TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at           TIMESTAMP,\n" +
                    "  deleted_at           TIMESTAMP\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE tour_schedule_node IS 'information about course in specific tour day. detailed information about course is reference \"product_course\" entity.';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_node.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_node.tour_schedule_day_id IS 'reference tour-schedule-day';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_node.course_id IS 'reference course';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_node.start IS 'start time. unit is minute. \"0\" is mean twelve at night.';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_node.size IS 'period time about course. unit is minute.';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_node.title IS 'title';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_node.description IS 'description';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_node.latitude IS 'latitude of course';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_node.longitude IS 'longitude of course';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_node.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_node.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_node.deleted_at IS 'present that this record is deleted';\n" +
                    "\n" +
                    "CREATE TABLE tour_schedule_node_media_file\n" +
                    "(\n" +
                    "  id BIGSERIAL                                  NOT NULL\n" +
                    "    CONSTRAINT tour_schedule_node_media_file_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  tour_schedule_node_id BIGINT NOT NULL\n" +
                    "    CONSTRAINT tour_schedule_node_media_file_tour_schedule_node_id_fkey\n" +
                    "    REFERENCES tour_schedule_node,\n" +
                    "  public_file_id        BIGINT NOT NULL\n" +
                    "    CONSTRAINT tour_schedule_node_media_file_public_file_id_fkey\n" +
                    "    REFERENCES public_file,\n" +
                    "  created_at            TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at            TIMESTAMP\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE tour_schedule_node_media_file IS 'image file what is comprised in one tour-schedule-node';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_node_media_file.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_node_media_file.tour_schedule_node_id IS 'reference tour-schedule-node';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_node_media_file.public_file_id IS 'reference image file';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_node_media_file.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour_schedule_node_media_file.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "CREATE TABLE notice\n" +
                    "(\n" +
                    "  id BIGSERIAL                        NOT NULL\n" +
                    "    CONSTRAINT notice_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  user_id     BIGINT NOT NULL\n" +
                    "    CONSTRAINT notice_user_id_fkey\n" +
                    "    REFERENCES \"user\",\n" +
                    "  title   TEXT   NOT NULL,\n" +
                    "  description TEXT NOT NULL,\n" +
                    "  created_at  TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at  TIMESTAMP,\n" +
                    "  deleted_at  TIMESTAMP\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE notice IS 'notify user(tourist/guide/guide manager) by administrator';\n" +
                    "\n" +
                    "COMMENT ON COLUMN notice.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN notice.user_id IS 'reference writer';\n" +
                    "\n" +
                    "COMMENT ON COLUMN notice.title IS 'title';\n" +
                    "\n" +
                    "COMMENT ON COLUMN notice.description IS 'description';\n" +
                    "\n" +
                    "COMMENT ON COLUMN notice.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN notice.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "COMMENT ON COLUMN notice.deleted_at IS 'present that this record is deleted';\n" +
                    "\n" +
                    "CREATE TABLE faq_category\n" +
                    "(\n" +
                    "  id BIGSERIAL                       NOT NULL\n" +
                    "    CONSTRAINT faq_category_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  name       VARCHAR(2048) NOT NULL,\n" +
                    "  ref_id BIGINT\n" +
                    "    CONSTRAINT faq_category_ref_id_fkey\n" +
                    "    REFERENCES faq_category,\n" +
                    "  created_at TIMESTAMP DEFAULT now() NOT NULL,\n" +
                    "  updated_at TIMESTAMP,\n" +
                    "  deleted_at TIMESTAMP\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE faq_category IS 'category of \"faq\"';\n" +
                    "\n" +
                    "COMMENT ON COLUMN faq_category.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN faq_category.name IS 'name of category';\n" +
                    "\n" +
                    "COMMENT ON COLUMN faq_category.ref_id IS 'refer anyone of \"faq_category\"';\n" +
                    "\n" +
                    "COMMENT ON COLUMN faq_category.created_at IS 'timestamp when this record is created';\n" +
                    "\n" +
                    "COMMENT ON COLUMN faq_category.updated_at IS 'timestamp when this record is updated';\n" +
                    "\n" +
                    "COMMENT ON COLUMN faq_category.deleted_at IS 'present that this record is deleted';\n" +
                    "\n" +
                    "ALTER TABLE faq\n" +
                    "  ADD CONSTRAINT faq_category_id_fkey\n" +
                    "FOREIGN KEY (category_id) REFERENCES faq_category;\n" +
                    "\n" +
                    "CREATE TABLE service_type\n" +
                    "(\n" +
                    "  id BIGSERIAL       NOT NULL\n" +
                    "    CONSTRAINT service_type_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  name VARCHAR(2048) NOT NULL\n" +
                    "    CONSTRAINT service_type_name_key\n" +
                    "    UNIQUE\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE service_type IS 'define service type for service provide by guide or guide manager';\n" +
                    "\n" +
                    "COMMENT ON COLUMN service_type.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN service_type.name IS 'name of service type';\n" +
                    "\n" +
                    "CREATE TABLE service\n" +
                    "(\n" +
                    "  id              BIGINT NOT NULL\n" +
                    "    CONSTRAINT service_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  service_type_id BIGINT NOT NULL\n" +
                    "    CONSTRAINT service_service_type_id_fkey\n" +
                    "    REFERENCES service_type,\n" +
                    "  transportation_type transportation_type,\n" +
                    "\n" +
                    "  CONSTRAINT service_id_fkey\n" +
                    "  FOREIGN KEY (id)\n" +
                    "  REFERENCES product\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE service IS 'define service for service provide by guide or guide manager';\n" +
                    "\n" +
                    "COMMENT ON COLUMN service.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN service.service_type_id IS 'representative service type';\n" +
                    "\n" +
                    "COMMENT ON COLUMN service.transportation_type IS 'kind of transportation in the service';\n" +
                    "\n" +
                    "CREATE TABLE tour\n" +
                    "(\n" +
                    "  id              BIGINT NOT NULL\n" +
                    "    CONSTRAINT tour_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  type tour_type NOT NULL,\n" +
                    "  adjustable_time BOOLEAN DEFAULT FALSE,\n" +
                    "  must_inquire    BOOLEAN DEFAULT FALSE,\n" +
                    "  tour_scale tour_scale_type,\n" +
                    "  due_date_type due_date_type,\n" +
                    "  due_date        DOUBLE PRECISION,\n" +
                    "\n" +
                    "  CONSTRAINT tour_id_fkey\n" +
                    "  FOREIGN KEY (id)\n" +
                    "  REFERENCES product\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE tour IS 'selling tour-product by guide or guide manager';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour.type IS 'tour type(unit tour/custom tour)';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour.adjustable_time IS 'is adjustable when meet with guide';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour.must_inquire IS 'must inquire, before reserve';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour.tour_scale IS 'tour type(independent travel/associated travel)';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour.due_date_type IS 'unit about period of trip';\n" +
                    "\n" +
                    "COMMENT ON COLUMN tour.due_date IS 'period of trip';\n" +
                    "\n" +
                    "CREATE TABLE unit_tour\n" +
                    "(\n" +
                    "  id                           BIGINT NOT NULL\n" +
                    "    CONSTRAINT unit_tour_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  transportation_type transportation_type,\n" +
                    "  meeting_time VARCHAR(100),\n" +
                    "  meeting_location_name VARCHAR(50),\n" +
                    "  meeting_location_map_zoom INTEGER DEFAULT 3,\n" +
                    "  meeting_location_latitude DOUBLE PRECISION,\n" +
                    "  meeting_location_longitude DOUBLE PRECISION,\n" +
                    "  meeting_location_description TEXT,\n" +
                    "\n" +
                    "  CONSTRAINT unit_tour_id_fkey\n" +
                    "  FOREIGN KEY (id)\n" +
                    "  REFERENCES tour\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE unit_tour IS 'selling tour-product by guide';\n" +
                    "\n" +
                    "COMMENT ON COLUMN unit_tour.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN unit_tour.transportation_type IS 'kind of transportation in the trip';\n" +
                    "\n" +
                    "COMMENT ON COLUMN unit_tour.meeting_time IS 'when meet with guide';\n" +
                    "\n" +
                    "COMMENT ON COLUMN unit_tour.meeting_location_name IS 'name of place where meet with guide';\n" +
                    "\n" +
                    "COMMENT ON COLUMN unit_tour.meeting_location_map_zoom IS 'zoom level of google map about place where meet with guide';\n" +
                    "\n" +
                    "COMMENT ON COLUMN unit_tour.meeting_location_latitude IS 'latitude of place where meet with guide';\n" +
                    "\n" +
                    "COMMENT ON COLUMN unit_tour.meeting_location_longitude IS 'longitude of place where meet with guide';\n" +
                    "\n" +
                    "COMMENT ON COLUMN unit_tour.meeting_location_description IS 'description about place where meet with guide';\n" +
                    "\n" +
                    "ALTER TABLE course\n" +
                    "  ADD CONSTRAINT course_unit_tour_id_fkey\n" +
                    "FOREIGN KEY (unit_tour_id) REFERENCES unit_tour;\n" +
                    "\n" +
                    "ALTER TABLE tour_sketch_node\n" +
                    "  ADD CONSTRAINT tour_sketch_node_unit_tour_id_fkey\n" +
                    "FOREIGN KEY (unit_tour_id) REFERENCES unit_tour;\n" +
                    "\n" +
                    "CREATE TABLE custom_tour\n" +
                    "(\n" +
                    "  id           BIGINT NOT NULL\n" +
                    "    CONSTRAINT custom_tour_pkey\n" +
                    "    PRIMARY KEY,\n" +
                    "  tour_plan_id BIGINT\n" +
                    "    CONSTRAINT custom_tour_tour_plan_id_fkey\n" +
                    "    REFERENCES tour_plan,\n" +
                    "\n" +
                    "  CONSTRAINT custom_tour_id_fkey\n" +
                    "  FOREIGN KEY (id)\n" +
                    "  REFERENCES tour\n" +
                    ");\n" +
                    "\n" +
                    "COMMENT ON TABLE custom_tour IS 'selling tour-product by guide manager';\n" +
                    "\n" +
                    "COMMENT ON COLUMN custom_tour.id IS 'table identifier';\n" +
                    "\n" +
                    "COMMENT ON COLUMN custom_tour.tour_plan_id IS 'reference tour plan';\n";


    public static Connection connection() throws SQLException {

        final String url = "jdbc:h2:tcp://localhost:9092/mem:test;DB_CLOSE_DELAY=-1";
        final Properties properties = new Properties();
        properties.put("user", "sa");
        properties.put("password", "");

        return new Driver().connect(url, properties);
    }

    public static void create() throws SQLException, IOException {
        Server.createTcpServer().start();

        try (final Connection connect = connection()) {
            String[] strings = ddl.split(";");

            for (String string : strings) {
                final String query = string.trim();
                if (StringUtils.isNullOrEmpty(query))
                    continue;

                try (final Statement statement = connect.createStatement()) {
                    statement.execute(query);
                }
            }
        }
    }
}
