DROP TABLE IF EXISTS user;

CREATE TABLE user (
                      user_id SERIAL PRIMARY KEY,
                      user_username VARCHAR(255) NOT NULL,
                      user_password VARCHAR(255) NOT NULL,
                      user_nickname VARCHAR(255) NOT NULL
);