-- ============================
-- CHAT USERS
-- ============================
CREATE TABLE chat_user (
    id CHAR(36) NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    active BIT(1) NOT NULL DEFAULT b'1',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================
-- CHAT ROOMS
-- ============================
CREATE TABLE chat_room (
    id CHAR(36) NOT NULL,
    type VARCHAR(20) NOT NULL,     -- PRIVATE / GROUP
    name VARCHAR(100),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================
-- CHAT ROOM MEMBERS
-- ============================
CREATE TABLE chat_room_member (
    id CHAR(36) NOT NULL,
    chat_room_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    role VARCHAR(20) NOT NULL,     -- ADMIN / MEMBER
    joined_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),

    CONSTRAINT fk_room_member_room
        FOREIGN KEY (chat_room_id) REFERENCES chat_room(id),

    CONSTRAINT fk_room_member_user
        FOREIGN KEY (user_id) REFERENCES chat_user(id),

    CONSTRAINT uq_room_user
        UNIQUE (chat_room_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================
-- MESSAGES
-- ============================
CREATE TABLE message (
    id CHAR(36) NOT NULL,
    chat_room_id CHAR(36) NOT NULL,
    sender_id CHAR(36) NOT NULL,
    message_type VARCHAR(20) NOT NULL,   -- TEXT / IMAGE / SYSTEM
    content TEXT NOT NULL,
    deleted BIT(1) DEFAULT b'0',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    PRIMARY KEY (id),

    CONSTRAINT fk_message_room
        FOREIGN KEY (chat_room_id) REFERENCES chat_room(id),

    CONSTRAINT fk_message_sender
        FOREIGN KEY (sender_id) REFERENCES chat_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
