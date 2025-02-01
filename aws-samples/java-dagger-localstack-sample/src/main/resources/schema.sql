CREATE TABLE IF NOT EXISTS product
(
    id    VARCHAR(36) PRIMARY KEY,
    name  STRING,
    price NUMBER(16, 4)
)
;

INSERT INTO product (id, name, price)
VALUES (uuid_string(), 'Pineapple', '15.32')
;
