
CREATE DATABASE message_api DEFAULT CHARSET UTF8;

USE message_api;

CREATE TABLE user (
    user_id       BIGINT UNSIGNED NOT NULL,
    email         VARCHAR(50),
    display_name  VARCHAR(50),
    user_role     VARCHAR(50),
    user_password VARCHAR(255),
PRIMARY KEY (user_id)
);

CREATE TABLE entity_counter (
    entity_id     BIGINT UNSIGNED NOT NULL,
    counter_value BIGINT UNSIGNED NOT NULL,
    entity_name   VARCHAR(50),
PRIMARY KEY (entity_id)
);

INSERT INTO entity_counter (entity_id, counter_value, entity_name) VALUES (1, 1000, "user");

DELIMITER //

CREATE PROCEDURE get_next_id(IN entity_name VARCHAR(50), OUT next_id BIGINT)
BEGIN
    DECLARE current_id BIGINT;

    START TRANSACTION;
        SELECT counter_value INTO current_id 
        FROM entity_counter 
        WHERE entity_name = entity_name;
        UPDATE entity_counter 
        SET counter_value = current_id + 1 
        WHERE entity_name = entity_name;
    COMMIT;

    SET next_id = current_id;
END //

DELIMITER ;