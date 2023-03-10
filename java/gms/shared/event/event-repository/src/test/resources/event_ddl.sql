CREATE SCHEMA IF NOT EXISTS GMS_GLOBAL AUTHORIZATION GMS_GLOBAL;

CREATE TABLE IF NOT EXISTS GMS_GLOBAL.EVENT
(
    EVID NUMBER(18),
    EVNAME VARCHAR2(32),
    PREFOR NUMBER(18),
    AUTH VARCHAR2(15),
    COMMID NUMBER(18),
    LDDATE DATE,
    CONSTRAINT EVENT_PK PRIMARY KEY (EVID)
);