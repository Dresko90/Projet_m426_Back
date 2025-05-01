CREATE DATABASE messaging_db DEFAULT CHARSET UTF8;

USE messaging_db;

-- Entities

CREATE TABLE user (
    user_id         BIGINT NOT NULL,
    username        VARCHAR(50),
    user_password   VARCHAR(255),
    user_role       VARCHAR(50),
    display_name    VARCHAR(50),
    
    PRIMARY KEY (user_id)
);

INSERT INTO user (user_id, username, display_name, user_password, user_role) VALUES (0, "deleted", "deleted", "!", "SURROGATE");


CREATE TABLE conversation (
    conversation_id     BIGINT NOT NULL,
    is_group            BOOLEAN DEFAULT FALSE,
    
    PRIMARY KEY (conversation_id)
);

CREATE TABLE participant (
    conversation_id     BIGINT NOT NULL,
    user_id             BIGINT NOT NULL,
    participant_role    VARCHAR(50),
    participant_status  VARCHAR(50),
    
    PRIMARY KEY (conversation_id, user_id),
    FOREIGN KEY (conversation_id) 
        REFERENCES conversation(conversation_id)
        ON DELETE CASCADE,
    FOREIGN KEY (user_id)
        REFERENCES user(user_id)
);

 CREATE TABLE message (
    message_id      BIGINT NOT NULL,
    conversation_id BIGINT NOT NULL,
    sender_id       BIGINT NOT NULL,
    body            VARCHAR(2048),
    send_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (message_id),
    FOREIGN KEY (conversation_id) 
        REFERENCES conversation(conversation_id)
        ON DELETE CASCADE,
    FOREIGN KEY (sender_id) 
        REFERENCES user(user_id)
);

CREATE TABLE message_status (
    message_id      BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    read_at         TIMESTAMP DEFAULT NULL,
    deleted         BOOLEAN DEFAULT FALSE,

    PRIMARY KEY (message_id, user_id),
    FOREIGN KEY (message_id) 
        REFERENCES message(message_id),
    FOREIGN KEY (user_id) 
        REFERENCES user(user_id)
);

CREATE TABLE token (
    token       VARCHAR(1024) NOT NULL,
    user_id     BIGINT NOT NULL,

    PRIMARY KEY (token),
    FOREIGN KEY (user_id) 
        REFERENCES user(user_id) 
        ON DELETE CASCADE
);

DELIMITER //
CREATE PROCEDURE delete_user(IN in_user_id BIGINT)
BEGIN 
    START TRANSACTION;
    
    IF in_user_id = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot delete surrogate user.';
    END IF;

    SET @surrogate_user_id = 0;

    UPDATE message
    SET sender_id = @surrogate_user_id, body = 'deleted'
    WHERE sender_id = in_user_id;

    UPDATE participant
    SET user_id = @surrogate_user_id
    WHERE user_id = in_user_id;

    DELETE FROM message_status
    WHERE user_id = in_user_id;

    DELETE FROM user
    WHERE user_id = in_user_id;

    COMMIT;
END;
//
DELIMITER ;


 -- Entity counters

CREATE TABLE entity_counter (
    entity_id       BIGINT NOT NULL,
    counter_value   BIGINT NOT NULL,
    entity_name     VARCHAR(50),

    PRIMARY KEY (entity_id)
);

DELIMITER //
CREATE PROCEDURE get_next_id(IN in_entity_name VARCHAR(50), OUT out_next_id BIGINT) 
BEGIN 
    DECLARE current_id BIGINT;

    START TRANSACTION;

    SELECT counter_value INTO current_id
    FROM entity_counter
    WHERE entity_name = in_entity_name;

    UPDATE entity_counter
    SET counter_value = current_id + 1
    WHERE entity_name = in_entity_name;

    COMMIT;

    SET out_next_id = current_id;
END;
//
DELIMITER ;

INSERT INTO entity_counter (entity_id, counter_value, entity_name) VALUES (1, 1000, "user");
INSERT INTO entity_counter (entity_id, counter_value, entity_name) VALUES (2, 1000, "conversation");
INSERT INTO entity_counter (entity_id, counter_value, entity_name) VALUES (3, 1000, "message");