# Addresses schema

# --- !Ups

CREATE TABLE PRODUCTS (
    id varchar(36) NOT NULL,
    name varchar(256) NOT NULL,
    volume int NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE ORDERS (
    id varchar(36) NOT NULL,
    product_id varchar(36) NOT NULL,
    volume int NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ORDER_FK FOREIGN KEY (product_id) REFERENCES PRODUCTS(id)
);

# --- !Downs

DROP TABLE ORDERS;
DROP TABLE PRODUCTS;
