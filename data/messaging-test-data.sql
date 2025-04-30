
USE messaging_db;

-- Username : admin / Password : Epai123
INSERT INTO user (user_id, username, display_name, user_password, user_role) VALUES (1, "admin@example.com", "Administrator", "$2a$10$xqaCFCdUiMEJq85ar7XjhuTiHvD6hzml5Pu4ZbMMYhG.ePUV0uyFC", "ADMIN");
INSERT INTO user (user_id, username, display_name, user_password, user_role) VALUES (11, "sheana@example.com", "sheana", "$2a$10$xqaCFCdUiMEJq85ar7XjhuTiHvD6hzml5Pu4ZbMMYhG.ePUV0uyFC", "USER");
INSERT INTO user (user_id, username, display_name, user_password, user_role) VALUES (12, "idaho@example.com", "idaho", "$2a$10$xqaCFCdUiMEJq85ar7XjhuTiHvD6hzml5Pu4ZbMMYhG.ePUV0uyFC", "USER");
INSERT INTO user (user_id, username, display_name, user_password, user_role) VALUES (13, "siona@example.com", "siona", "$2a$10$xqaCFCdUiMEJq85ar7XjhuTiHvD6hzml5Pu4ZbMMYhG.ePUV0uyFC", "USER");
INSERT INTO user (user_id, username, display_name, user_password, user_role) VALUES (14, "trevize@example.com", "trevize", "$2a$10$xqaCFCdUiMEJq85ar7XjhuTiHvD6hzml5Pu4ZbMMYhG.ePUV0uyFC", "USER");
INSERT INTO user (user_id, username, display_name, user_password, user_role) VALUES (15, "fallom@example.com", "fallom", "$2a$10$xqaCFCdUiMEJq85ar7XjhuTiHvD6hzml5Pu4ZbMMYhG.ePUV0uyFC", "USER");
INSERT INTO user (user_id, username, display_name, user_password, user_role) VALUES (16, "daneel@example.com", "daneel", "$2a$10$xqaCFCdUiMEJq85ar7XjhuTiHvD6hzml5Pu4ZbMMYhG.ePUV0uyFC", "USER");
USE messaging_db;

INSERT INTO token (token, user_id) VALUES ('865b1cb2-bd0e-4468-8370-a8ae9ae4bd11', 11);

-- Conversation 100: sheana and idaho
INSERT INTO conversation (conversation_id) VALUES (100);
INSERT INTO participant (conversation_id, user_id, participant_role, participant_status)
VALUES 
    (100, 11, 'MEMBER', 'ACTIVE'), 
    (100, 12, 'MEMBER', 'ACTIVE');
INSERT INTO message (message_id, conversation_id, sender_id, body)
VALUES 
    (100, 100, 11, 'Hi Idaho!'),
    (101, 100, 12, 'Hey Sheana, good to hear from you.'),
    (102, 100, 11, 'How have you been?'),
    (112, 100, 11, 'Hi Idaho, let’s catch up!'),
    (113, 100, 12, 'Sure Sheana, let’s do it.');
INSERT INTO message_status (message_id, user_id, read_at, deleted)
VALUES 
    (100, 11, NULL, FALSE), (100, 12, NULL, FALSE),
    (101, 11, NULL, FALSE), (101, 12, NULL, FALSE),
    (102, 11, NULL, FALSE), (102, 12, NULL, FALSE),
    (112, 11, NULL, FALSE), (112, 12, NULL, FALSE),
    (113, 11, NULL, FALSE), (113, 12, NULL, FALSE);

-- Conversation 101: siona and trevize
INSERT INTO conversation (conversation_id) VALUES (101);
INSERT INTO participant (conversation_id, user_id, participant_role, participant_status)
VALUES 
    (101, 13, 'MEMBER', 'ACTIVE'), 
    (101, 14, 'MEMBER', 'ACTIVE');
INSERT INTO message (message_id, conversation_id, sender_id, body)
VALUES 
    (103, 101, 13, 'Hi Trevize!'),
    (104, 101, 14, 'Hey Siona! Glad to see you.'),
    (122, 101, 13, 'Hi Trevize, any updates?'),
    (123, 101, 14, 'Yes Siona, I’ll share them.');
INSERT INTO message_status (message_id, user_id, read_at, deleted)
VALUES 
    (103, 13, NULL, FALSE), (103, 14, NULL, FALSE),
    (104, 13, NULL, FALSE), (104, 14, NULL, FALSE),
    (122, 13, NULL, FALSE), (122, 14, NULL, FALSE),
    (123, 13, NULL, FALSE), (123, 14, NULL, FALSE);

-- Conversation 102: fallom and daneel
INSERT INTO conversation (conversation_id) VALUES (102);
INSERT INTO participant (conversation_id, user_id, participant_role, participant_status)
VALUES 
    (102, 15, 'MEMBER', 'ACTIVE'),
    (102, 16, 'MEMBER', 'ACTIVE');
INSERT INTO message (message_id, conversation_id, sender_id, body)
VALUES 
    (105, 102, 15, 'Hi Daneel! Ready for the project?'),
    (106, 102, 16, 'Absolutely Fallom!');
INSERT INTO message_status (message_id, user_id, read_at, deleted)
VALUES 
    (105, 15, NULL, FALSE), (105, 16, NULL, FALSE),
    (106, 15, NULL, FALSE), (106, 16, NULL, FALSE);

-- Conversation 103: sheana and siona
INSERT INTO conversation (conversation_id) VALUES (103);
INSERT INTO participant (conversation_id, user_id, participant_role, participant_status)
VALUES 
    (103, 11, 'MEMBER', 'ACTIVE'), 
    (103, 13, 'MEMBER', 'ACTIVE');
INSERT INTO message (message_id, conversation_id, sender_id, body)
VALUES 
    (107, 103, 11, 'Hi Siona, are you available for a quick meeting?'),
    (108, 103, 13, 'Sure Sheana, when?'),
    (114, 103, 11, 'Hi Siona, how’s it going?'),
    (115, 103, 13, 'All good, Sheana!');
INSERT INTO message_status (message_id, user_id, read_at, deleted)
VALUES 
    (107, 11, NULL, FALSE), (107, 13, NULL, FALSE),
    (108, 11, NULL, FALSE), (108, 13, NULL, FALSE),
    (114, 11, NULL, FALSE), (114, 13, NULL, FALSE),
    (115, 11, NULL, FALSE), (115, 13, NULL, FALSE);

-- Conversation 104: trevize, fallom and idaho (3 participants pour varier)
INSERT INTO conversation (conversation_id) VALUES (104);
INSERT INTO participant (conversation_id, user_id, participant_role, participant_status)
VALUES 
    (104, 14, 'MEMBER', 'ACTIVE'), 
    (104, 15, 'MEMBER', 'ACTIVE'), 
    (104, 12, 'MEMBER', 'ACTIVE');
INSERT INTO message (message_id, conversation_id, sender_id, body)
VALUES 
    (109, 104, 14, 'Hello group!'),
    (110, 104, 15, 'Hi everyone!'),
    (111, 104, 12, 'Good to be here!');
INSERT INTO message_status (message_id, user_id, read_at, deleted)
VALUES 
    (109, 14, NULL, FALSE), (109, 15, NULL, FALSE), (109, 12, NULL, FALSE),
    (110, 14, NULL, FALSE), (110, 15, NULL, FALSE), (110, 12, NULL, FALSE),
    (111, 14, NULL, FALSE), (111, 15, NULL, FALSE), (111, 12, NULL, FALSE);

-- Conversation 105: daneel and idaho
INSERT INTO conversation (conversation_id) VALUES (105);
INSERT INTO participant (conversation_id, user_id, participant_role, participant_status)
VALUES 
    (105, 16, 'MEMBER', 'ACTIVE'), 
    (105, 12, 'MEMBER', 'ACTIVE');
INSERT INTO message (message_id, conversation_id, sender_id, body)
VALUES 
    (132, 105, 16, 'Hi Idaho, let’s review the document.'),
    (133, 105, 12, 'Sure Daneel, I’ll check it.');
INSERT INTO message_status (message_id, user_id, read_at, deleted)
VALUES 
    (132, 16, NULL, FALSE), (132, 12, NULL, FALSE),
    (133, 16, NULL, FALSE), (133, 12, NULL, FALSE);

-- Conversation 106: fallom and siona
INSERT INTO conversation (conversation_id) VALUES (106);
INSERT INTO participant (conversation_id, user_id, participant_role, participant_status)
VALUES 
    (106, 15, 'MEMBER', 'ACTIVE'), 
    (106, 13, 'MEMBER', 'ACTIVE');
INSERT INTO message (message_id, conversation_id, sender_id, body)
VALUES 
    (130, 106, 15, 'Hi Siona, any updates?'),
    (131, 106, 13, 'Yes Fallom, I’ll share them.');
INSERT INTO message_status (message_id, user_id, read_at, deleted)
VALUES 
    (130, 15, NULL, FALSE), (130, 13, NULL, FALSE),
    (131, 15, NULL, FALSE), (131, 13, NULL, FALSE);

-- Conversation 107: idaho and trevize
INSERT INTO conversation (conversation_id) VALUES (107);
INSERT INTO participant (conversation_id, user_id, participant_role, participant_status)
VALUES 
    (107, 12, 'MEMBER', 'ACTIVE'), 
    (107, 14, 'MEMBER', 'ACTIVE');
INSERT INTO message (message_id, conversation_id, sender_id, body)
VALUES 
    (116, 107, 12, 'Hi Trevize, ready for the meeting?'),
    (117, 107, 14, 'Yes Idaho, let’s start.');
INSERT INTO message_status (message_id, user_id, read_at, deleted)
VALUES 
    (116, 12, NULL, FALSE), (116, 14, NULL, FALSE),
    (117, 12, NULL, FALSE), (117, 14, NULL, FALSE);

-- Conversation 108: idaho and fallom
INSERT INTO conversation (conversation_id) VALUES (108);
INSERT INTO participant (conversation_id, user_id, participant_role, participant_status)
VALUES 
    (108, 12, 'MEMBER', 'ACTIVE'), 
    (108, 15, 'MEMBER', 'ACTIVE');
INSERT INTO message (message_id, conversation_id, sender_id, body)
VALUES 
    (118, 108, 12, 'Hi Fallom, how’s the project?'),
    (119, 108, 15, 'Going great, Idaho!');
INSERT INTO message_status (message_id, user_id, read_at, deleted)
VALUES 
    (118, 12, NULL, FALSE), (118, 15, NULL, FALSE),
    (119, 12, NULL, FALSE), (119, 15, NULL, FALSE);

-- Conversation 109: siona and daneel
INSERT INTO conversation (conversation_id) VALUES (109);
INSERT INTO participant (conversation_id, user_id, participant_role, participant_status)
VALUES 
    (109, 13, 'MEMBER', 'ACTIVE'),
    (109, 16, 'MEMBER', 'ACTIVE');
INSERT INTO message (message_id, conversation_id, sender_id, body)
VALUES 
    (120, 109, 13, 'Hi Daneel, let’s discuss the plan.'),
    (121, 109, 16, 'Sure Siona, let’s do it.');
INSERT INTO message_status (message_id, user_id, read_at, deleted)
VALUES 
    (120, 13, NULL, FALSE), (120, 16, NULL, FALSE),
    (121, 13, NULL, FALSE), (121, 16, NULL, FALSE);

-- Conversation 110: fallom and sheana
INSERT INTO conversation (conversation_id) VALUES (110);
INSERT INTO participant (conversation_id, user_id, participant_role, participant_status)
VALUES 
    (110, 15, 'MEMBER', 'ACTIVE'), 
    (110, 11, 'MEMBER', 'ACTIVE');
INSERT INTO message (message_id, conversation_id, sender_id, body)
VALUES 
    (128, 110, 15, 'Hi Sheana, let’s collaborate.'),
    (129, 110, 11, 'Sure Fallom, let’s do it.');
INSERT INTO message_status (message_id, user_id, read_at, deleted)
VALUES 
    (128, 15, NULL, FALSE), (128, 11, NULL, FALSE),
    (129, 15, NULL, FALSE), (129, 11, NULL, FALSE);

-- Conversation 111: trevize and fallom
INSERT INTO conversation (conversation_id) VALUES (111);
INSERT INTO participant (conversation_id, user_id, participant_role, participant_status)
VALUES 
    (111, 14, 'MEMBER', 'ACTIVE'),
    (111, 15, 'MEMBER', 'ACTIVE');
INSERT INTO message (message_id, conversation_id, sender_id, body)
VALUES 
    (124, 111, 14, 'Hi Fallom, how’s everything?'),
    (125, 111, 15, 'All good, Trevize!');
INSERT INTO message_status (message_id, user_id, read_at, deleted)
VALUES 
    (124, 14, NULL, FALSE), (124, 15, NULL, FALSE),
    (125, 14, NULL, FALSE), (125, 15, NULL, FALSE);

-- Conversation 112: trevize and daneel
INSERT INTO conversation (conversation_id) VALUES (112);
INSERT INTO participant (conversation_id, user_id, participant_role, participant_status)
VALUES 
    (112, 14, 'MEMBER', 'ACTIVE'), 
    (112, 16, 'MEMBER', 'ACTIVE');
INSERT INTO message (message_id, conversation_id, sender_id, body)
VALUES 
    (126, 112, 14, 'Hi Daneel, let’s sync up.'),
    (127, 112, 16, 'Sure Trevize, let’s do it.'),
    (134, 112, 16, 'Hi Trevize, let’s finalize the plan.'),
    (135, 112, 14, 'Sure Daneel, let’s do it.');
INSERT INTO message_status (message_id, user_id, read_at, deleted)
VALUES 
    (126, 14, NULL, FALSE), (126, 16, NULL, FALSE),
    (127, 14, NULL, FALSE), (127, 16, NULL, FALSE),
    (134, 16, NULL, FALSE), (134, 14, NULL, FALSE),
    (135, 16, NULL, FALSE), (135, 14, NULL, FALSE);







