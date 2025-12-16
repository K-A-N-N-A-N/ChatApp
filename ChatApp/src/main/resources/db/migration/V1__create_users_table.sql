-- ============================
-- CHAT USERS
-- ============================
CREATE TABLE chat_user (
    id CHAR(36) NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    PRIMARY KEY (id)
);

-- ============================
-- CHAT ROOMS
-- ============================
CREATE TABLE chat_room (
    id CHAR(36) NOT NULL,
    type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    PRIMARY KEY (id)
);

-- ============================
-- CHAT ROOM MEMBERS
-- ============================
CREATE TABLE chat_room_member (
    id CHAR(36) NOT NULL,
    chat_room_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    role VARCHAR(20) DEFAULT 'MEMBER',
    joined_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_room_member_room
        FOREIGN KEY (chat_room_id) REFERENCES chat_room(id),
    CONSTRAINT fk_room_member_user
        FOREIGN KEY (user_id) REFERENCES chat_user(id)
);

-- ============================
-- MESSAGES
-- ============================
CREATE TABLE message (
    id CHAR(36) NOT NULL,
    chat_room_id CHAR(36) NOT NULL,
    sender_id CHAR(36) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (id),
    CONSTRAINT fk_message_room
        FOREIGN KEY (chat_room_id) REFERENCES chat_room(id),
    CONSTRAINT fk_message_sender
        FOREIGN KEY (sender_id) REFERENCES chat_user(id)
);
