
-- Table: areas
CREATE TABLE areas (
	ID					INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
	name				VARCHAR,
	startChunkpositionX	INTEGER,
	startChunkpositionY	INTEGER,
	startChunkpositionZ	INTEGER,
	startBlockpositionX	INTEGER,
	startBlockpositionY	INTEGER,
	startBlockpositionZ	INTEGER,
	endChunkpositionX	INTEGER,
	endChunkpositionY	INTEGER,
	endChunkpositionZ	INTEGER,
	endBlockpositionX	INTEGER,
	endBlockpositionY	INTEGER,
	endBlockpositionZ	INTEGER,
	playerID			INTEGER 
);


-- Table: rights
CREATE TABLE rights (
	ID			INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
	areaID		INTEGER,
	playerID	INTEGER,
	[group]		VARCHAR 
);


-- Table: chests
CREATE TABLE chests (
	ID				INTEGER,
	chunkOffsetX	INTEGER,
	chunkOffsetY	INTEGER,
	chunkOffsetZ	INTEGER,
	positionX		INTEGER,
	positionY		INTEGER,
	positionZ		INTEGER 
);

