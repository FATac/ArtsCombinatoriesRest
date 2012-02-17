-- Base script which creates necessary data structure for managing users, rights and uploaded media and objects
-- Suitable (only?) for Openlink Virtuoso sql implementation

DROP TABLE db.dba._media;
DROP TABLE db.dba._thumbnail;
DROP TABLE db.dba._right;
DROP TABLE db.dba._identifier_counter;
DROP TABLE db.dba._autodata;
DROP TABLE db.dba._resource_statistics;

CREATE TABLE db.dba._resource_statistics (
	identifier VARCHAR(150),
	visitCounter BIGINT,
	creationMoment BIGINT,
	lastMoment BIGINT
)

CREATE TABLE db.dba._right (
	SID INT IDENTITY,
	objectId VARCHAR(150),
	rightLevel INT
)

CREATE TABLE db.dba._identifier_counter (
	identifier VARCHAR(150),
	counter INT
)

CREATE TABLE db.dba._autodata (
	keyName VARCHAR(100),
	keyValue VARCHAR(100),
	name VARCHAR(100),
	defaultValue VARCHAR(100)
)

--GRANT EXECUTE ON DB.DBA.SPARQL_DELETE_DICT_CONTENT TO "SPARQL";
GRANT ALL PRIVILEGES TO "SPARQL";