CREATE SCHEMA chatServer
CREATE TABLE chatServer.RegTable(Mobile VARCHAR(20) NOT NULL, RegId VARCHAR(40) NOT NULL, PRIMARY KEY(Mobile));
CREATE TABLE chatServer.ProfileTable(UserMobile VARCHAR(20) NOT NULL PRIMARY KEY, UserPhoto BLOB NOT NULL);
ALTER TABLE chatServer.ProfileTable ALTER COLUMN UserPhoto BLOB(1000);
INSERT INTO chatServer.RegTable(Mobile, RegId) VALUES ('+2348132229044', 'qwerty123ASDF');

jdbc:hsqldb:file:C:\Users\Jesz\Documents\Web Application\ChatServerDual\src\db_script