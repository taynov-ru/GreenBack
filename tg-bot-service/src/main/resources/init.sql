CREATE TABLE if not exists device_info
(
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(255),
    user_id   BIGINT NOT NULL DEFAULT -1,
    device_id VARCHAR(255)
);

CREATE TABLE if not exists device_user_assignment
(
    device_id VARCHAR(255) PRIMARY KEY
);

CREATE TABLE if not exists device_user_assignment_users
(
    device_id VARCHAR(255),
    user_id   VARCHAR(255),
    PRIMARY KEY (device_id, user_id),
    FOREIGN KEY (device_id) REFERENCES device_user_assignment (device_id) ON DELETE CASCADE
);

CREATE TABLE if not exists user_state_entity
(
    user_id         BIGINT PRIMARY KEY,
    username        VARCHAR(255),
    selected_device VARCHAR(255),
    payload         VARCHAR(255)
);

CREATE TABLE esp_data
(
    id         VARCHAR(255) PRIMARY KEY,
    data       VARCHAR(2048) NOT NULL,
    created_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
