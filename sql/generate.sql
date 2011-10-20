-- Base script which creates necessary data structure for managing users, rights and uploaded media and objects
-- Suitable (only?) for Openlink Virtuoso sql implementation

DROP TABLE db.dba._media;
CREATE TABLE db.dba._media (
	SID INT IDENTITY,
	mediaId VARCHAR(60),
	path VARCHAR(500),
	moment TIMESTAMP
)

DROP TABLE db.dba._thumbnail;
CREATE TABLE db.dba._thumbnail (
	SID INT IDENTITY,
	objectId VARCHAR(60),
	path VARCHAR(500),
	moment TIMESTAMP
)

DROP TABLE db.dba._right;
CREATE TABLE db.dba._right (
	SID INT IDENTITY,
	objectId VARCHAR(60),
	rightLevel INT
)

DROP TABLE db.dba._identifier_counter;
CREATE TABLE db.dba._identifier_counter (
	identifier VARCHAR(150),
	counter INT
)

--GRANT EXECUTE ON DB.DBA.SPARQL_DELETE_DICT_CONTENT TO "SPARQL";
GRANT ALL PRIVILEGES TO "SPARQL";