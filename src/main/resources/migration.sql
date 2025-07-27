CREATE TABLE users (
   unique_id CHAR(36) NOT NULL PRIMARY KEY
);

CREATE TABLE achievements (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_unique_id CHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  CONSTRAINT fk_user
      FOREIGN KEY (user_unique_id)
          REFERENCES users(unique_id)
          ON DELETE CASCADE
);