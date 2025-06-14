INSERT INTO roles (authority) VALUES ('User');
INSERT INTO roles (authority) VALUES ('Supervisor');
INSERT INTO roles (authority) VALUES ('Admin');

INSERT INTO users(name, age, login, email, password, role_id, enabled) VALUES ('Анютка', 23, 'shirokowa', 'shirokowa@yandex.ru', 123, 1, false);
