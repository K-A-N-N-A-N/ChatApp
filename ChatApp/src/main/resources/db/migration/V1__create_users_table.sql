-- ============================
-- CHAT USERS
-- ============================
CREATE TABLE chat_user (
    id VARCHAR(36) NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    PRIMARY KEY (id)
);

-- ============================
-- CHAT ROOMS
-- ============================
CREATE TABLE chat_room (
    id VARCHAR(36) NOT NULL,
    type VARCHAR(20) NOT NULL,        -- PRIVATE / GROUP
    name VARCHAR(100),                -- group name, NULL for private chat
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    PRIMARY KEY (id)
);

-- ============================
-- CHAT ROOM MEMBERS
-- ============================
CREATE TABLE chat_room_member (
    id VARCHAR(36) NOT NULL,
    chat_room_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    role VARCHAR(20) NOT NULL,        -- ADMIN / MEMBER
    joined_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),

    CONSTRAINT fk_room_member_room
        FOREIGN KEY (chat_room_id) REFERENCES chat_room(id),

    CONSTRAINT fk_room_member_user
        FOREIGN KEY (user_id) REFERENCES chat_user(id),

    CONSTRAINT uq_room_user
        UNIQUE (chat_room_id, user_id)
);

-- ============================
-- MESSAGES
-- ============================
CREATE TABLE message (
    id VARCHAR(36) NOT NULL,
    chat_room_id VARCHAR(36) NOT NULL,
    sender_id VARCHAR(36) NOT NULL,
    message_type VARCHAR(20) NOT NULL,   -- TEXT / IMAGE / SYSTEM
    content TEXT NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    PRIMARY KEY (id),

    CONSTRAINT fk_message_room
        FOREIGN KEY (chat_room_id) REFERENCES chat_room(id),

    CONSTRAINT fk_message_sender
        FOREIGN KEY (sender_id) REFERENCES chat_user(id)
);
