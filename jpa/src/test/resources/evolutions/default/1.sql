# Users schema

# --- !Ups

CREATE TABLE USER (
    id varchar(36) NOT NULL,
    email varchar(255) NOT NULL,
    fullname varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE User;