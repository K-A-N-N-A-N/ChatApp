-- ============================
-- USER PRESENCE
-- ============================
CREATE TABLE user_presence (
    user_id CHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL,        -- ONLINE / OFFLINE / AWAY
    last_seen DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    PRIMARY KEY (user_id),

    CONSTRAINT fk_presence_user
        FOREIGN KEY (user_id) REFERENCES chat_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================
-- MESSAGE STATUS (READ RECEIPTS)
-- ============================
CREATE TABLE message_status (
    id CHAR(36) NOT NULL,
    message_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL,        -- DELIVERED / READ
    timestamp DATETIME(6) NOT NULL,
    PRIMARY KEY (id),

    CONSTRAINT fk_msg_status_message
        FOREIGN KEY (message_id) REFERENCES message(id),

    CONSTRAINT fk_msg_status_user
        FOREIGN KEY (user_id) REFERENCES chat_user(id),

    CONSTRAINT uq_message_user_status
        UNIQUE (message_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;