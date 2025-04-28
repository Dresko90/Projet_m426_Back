
USE messaging_db;

-- Username : admin / Password : epai321
INSERT INTO user (user_id, username, display_name, user_password, user_role) VALUES (1, "admin@exemple.com", "Administrator", "$2a$10$Zl/F8Thud.gqRw/vmuad3.fGFcfCRUnbDpe0Jo9S3hduROi0peA4i", "ADMIN");
INSERT INTO user (user_id, username, display_name, user_password, user_role) VALUES (11, "sheana@exemple.com", "sheana", "$2a$10$Zl/F8Thud.gqRw/vmuad3.fGFcfCRUnbDpe0Jo9S3hduROi0peA4i", "USER");
INSERT INTO user (user_id, username, display_name, user_password, user_role) VALUES (12, "idaho@exemple.com", "idaho", "$2a$10$Zl/F8Thud.gqRw/vmuad3.fGFcfCRUnbDpe0Jo9S3hduROi0peA4i", "USER");
INSERT INTO user (user_id, username, display_name, user_password, user_role) VALUES (13, "siona@exemple.com", "siona", "$2a$10$Zl/F8Thud.gqRw/vmuad3.fGFcfCRUnbDpe0Jo9S3hduROi0peA4i", "USER");
INSERT INTO user (user_id, username, display_name, user_password, user_role) VALUES (14, "trevize@exemple.com", "trevize", "$2a$10$Zl/F8Thud.gqRw/vmuad3.fGFcfCRUnbDpe0Jo9S3hduROi0peA4i", "USER");
INSERT INTO user (user_id, username, display_name, user_password, user_role) VALUES (15, "fallom@exemple.com", "fallom", "$2a$10$Zl/F8Thud.gqRw/vmuad3.fGFcfCRUnbDpe0Jo9S3hduROi0peA4i", "USER");
INSERT INTO user (user_id, username, display_name, user_password, user_role) VALUES (16, "daneel@exemple.com", "daneel", "$2a$10$Zl/F8Thud.gqRw/vmuad3.fGFcfCRUnbDpe0Jo9S3hduROi0peA4i", "USER");
USE messaging_db;

-- Conversations (id < 1000 pour les tests)
INSERT INTO conversation (conversation_id) VALUES (100);
INSERT INTO conversation (conversation_id) VALUES (101);
INSERT INTO conversation (conversation_id) VALUES (102);
INSERT INTO conversation (conversation_id) VALUES (103);
INSERT INTO conversation (conversation_id) VALUES (104);

-- Participants
-- Conversation 100: sheana and idaho
INSERT INTO participant (conversation_id, user_id, participant_role, participant_status)
VALUES (100, 11, 'USER', 'ACTIVE'), (100, 12, 'USER', 'ACTIVE');

-- Conversation 101: siona and trevize
INSERT INTO participant (conversation_id, user_id, participant_role, participant_status)
VALUES (101, 13, 'USER', 'ACTIVE'), (101, 14, 'USER', 'ACTIVE');

-- Conversation 102: fallom and daneel
INSERT INTO participant (conversation_id, user_id, participant_role, participant_status)
VALUES (102, 15, 'USER', 'ACTIVE'), (102, 16, 'USER', 'ACTIVE');

-- Conversation 103: sheana and siona
INSERT INTO participant (conversation_id, user_id, participant_role, participant_status)
VALUES (103, 11, 'USER', 'ACTIVE'), (103, 13, 'USER', 'ACTIVE');

-- Conversation 104: trevize, fallom and idaho (3 participants pour varier)
INSERT INTO participant (conversation_id, user_id, participant_role, participant_status)
VALUES (104, 14, 'USER', 'ACTIVE'), (104, 15, 'USER', 'ACTIVE'), (104, 12, 'USER', 'ACTIVE');

-- Messages
-- Conversation 100
INSERT INTO message (message_id, conversation_id, sender_id, body)
VALUES 
(100, 100, 11, 'Hi Idaho!'),
(101, 100, 12, 'Hey Sheana, good to hear from you.'),
(102, 100, 11, 'How have you been?');

-- Conversation 101
INSERT INTO message (message_id, conversation_id, sender_id, body)
VALUES 
(103, 101, 13, 'Hi Trevize!'),
(104, 101, 14, 'Hey Siona! Glad to see you.');

-- Conversation 102
INSERT INTO message (message_id, conversation_id, sender_id, body)
VALUES 
(105, 102, 15, 'Hi Daneel! Ready for the project?'),
(106, 102, 16, 'Absolutely Fallom!');

-- Conversation 103
INSERT INTO message (message_id, conversation_id, sender_id, body)
VALUES 
(107, 103, 11, 'Hi Siona, are you available for a quick meeting?'),
(108, 103, 13, 'Sure Sheana, when?');

-- Conversation 104
INSERT INTO message (message_id, conversation_id, sender_id, body)
VALUES 
(109, 104, 14, 'Hello group!'),
(110, 104, 15, 'Hi everyone!'),
(111, 104, 12, 'Good to be here!');