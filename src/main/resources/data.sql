INSERT INTO roles (authority) VALUES ('User');
INSERT INTO roles (authority) VALUES ('Supervisor');
INSERT INTO roles (authority) VALUES ('Admin');

INSERT INTO users(name, age, login, email, password, role_id, enabled, twoauth) VALUES ('Анютка', 23, 'shirokowa', 'shirokowa@yandex.ru', 123, 1, true, true);
