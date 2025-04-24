CREATE DATABASE messaging_db DEFAULT CHARSET UTF8;

USE messaging_db;

-- Entities

CREATE TABLE user (
    user_id         BIGINT NOT NULL,
    email           VARCHAR(50),
    display_name    VARCHAR(50),
    user_role       VARCHAR(50),
    user_password   VARCHAR(255),
    
    PRIMARY KEY (user_id)
);

CREATE TABLE conversation (
    conversation_id     BIGINT NOT NULL,

    PRIMARY KEY (conversation_id)
);

CREATE TABLE conversation_participant (
    conversation_id     BIGINT NOT NULL,
    user_id             BIGINT NOT NULL,

    PRIMARY KEY (conversation_id, user_id),
    FOREIGN KEY (conversation_id) 
        REFERENCES conversation(conversation_id)
        ON DELETE CASCADE,
    FOREIGN KEY (user_id) 
        REFERENCES user(user_id)
        ON DELETE CASCADE
);

 CREATE TABLE message (
    message_id      BIGINT NOT NULL,
    sender_id       BIGINT NOT NULL,
    conversation_id BIGINT NOT NULL,
    content         VARCHAR(2048),
    
    PRIMARY KEY (message_id),
    FOREIGN KEY (sender_id) 
        REFERENCES user(user_id) 
        ON DELETE CASCADE,
    FOREIGN KEY (conversation_id) 
        REFERENCES conversation(conversation_id)
        ON DELETE CASCADE
);

CREATE TABLE token (
    token       VARCHAR(1024) NOT NULL,
    user_id     BIGINT NOT NULL,

    PRIMARY KEY (token),
    FOREIGN KEY (user_id) 
        REFERENCES user(user_id) 
        ON DELETE CASCADE
);


 -- Entity counters
CREATE TABLE entity_counter (
    entity_id       BIGINT NOT NULL,
    counter_value   BIGINT NOT NULL,
    entity_name     VARCHAR(50),

    PRIMARY KEY (entity_id)
);

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
END//
DELIMITER ;

INSERT INTO entity_counter (entity_id, counter_value, entity_name) VALUES (1, 1000, "user");
INSERT INTO entity_counter (entity_id, counter_value, entity_name) VALUES (2, 1000, "conversation");
INSERT INTO entity_counter (entity_id, counter_value, entity_name) VALUES (3, 1000, "message");