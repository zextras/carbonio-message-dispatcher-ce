CREATE TABLE chats_messaging_db_version
(
    version VARCHAR(16) NOT NULL,
    PRIMARY KEY (version)
);
INSERT INTO carbonio_chats_messaging_db_version (version) VALUES ('5.1.0');