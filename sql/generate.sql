-- Base script which creates necessary data structure for managing users, rights and uploaded media and objects
-- Suitable (only?) for Openlink Virtuoso sql implementation

DROP TABLE db.dba._user;
CREATE TABLE db.dba._user (
	SID INT IDENTITY,
	userName VARCHAR(30),
	pwd VARCHAR(60),
	userType INT
);

DROP TABLE db.dba._media;
CREATE TABLE db.dba._media (
	SID INT IDENTITY,
	path VARCHAR(500),
	objectId VARCHAR(60),
	moment TIMESTAMP
)

DROP TABLE db.dba._right;
CREATE TABLE db.dba._right (
	SID INT IDENTITY,
	objectId VARCHAR(60),
	rightLevel INT
)

-- DROP TABLE db.dba._object_counter;
CREATE TABLE db.dba._object_counter (
	objectClass VARCHAR(80),
	counter INT
)

GRANT EXECUTE ON DB.DBA.SPARQL_DELETE_DICT_CONTENT TO "SPARQL";