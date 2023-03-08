---- MAIN SIGNAL DETECTION DDL

CREATE SCHEMA IF NOT EXISTS GMS_GLOBAL AUTHORIZATION GMS_GLOBAL;
CREATE TABLE IF NOT EXISTS GMS_GLOBAL.ARRIVAL
(
    STA     VARCHAR2(6) not null,
    TIME    FLOAT(53)   not null,
    ARID    NUMBER(10)  not null,
    JDATE   NUMBER(8),
    STASSID NUMBER(10),
    CHANID  NUMBER(8),
    CHAN    VARCHAR2(8),
    IPHASE  VARCHAR2(8),
    STYPE   VARCHAR2(1),
    DELTIM  FLOAT(24),
    AZIMUTH FLOAT(24),
    DELAZ   FLOAT(24),
    SLOW    FLOAT(24),
    DELSLO  FLOAT(24),
    EMA     FLOAT(24),
    RECT    FLOAT(24),
    AMP     FLOAT(24),
    PER     FLOAT(24),
    LOGAT   FLOAT(24),
    CLIP    VARCHAR2(1),
    FM      VARCHAR2(2),
    SNR     FLOAT(24),
    QUAL    VARCHAR2(1),
    AUTH    VARCHAR2(15),
    COMMID  NUMBER(10),
    LDDATE  DATE
);
CREATE TABLE IF NOT EXISTS GMS_GLOBAL.WFDISC
(
    STA      VARCHAR2(6)
        constraint WFDISC__STA__CK
            check (sta = UPPER(sta)),
    CHAN     VARCHAR2(8),
    TIME     FLOAT(53)
        constraint WFDISC__TIME__CK
            check (time >= -9999999999.999),
    WFID     NUMBER(18)
        constraint WFDISC_UK
            unique,
        constraint WFDISC__WFID__CK
            check (wfid > 0),
    CHANID   NUMBER(18)
        constraint WFDISC__CHANID__CK
            check (chanid > 0 OR chanid = -1),
    JDATE    NUMBER(8)
        constraint WFDISC__JDATE__CK
            check ((jdate > 1901348 AND jdate < 3001000) OR jdate = -1),
    ENDTIME  FLOAT(53)
        constraint WFDISC__ENDTIME__CK
            check (endtime < 9999999999.999 OR endtime = 9999999999.999),
    NSAMP    NUMBER(8)
        constraint WFDISC__NSAMP__CK
            check (nsamp > 0),
    SAMPRATE FLOAT(24)
        constraint WFDISC__SAMPRATE__CK
            check (samprate > 0.0),
    CALIB    FLOAT(24)
        constraint WFDISC__CALIB__CK
            check (calib != 0.0),
    CALPER   FLOAT(24)
        constraint WFDISC__CALPER__CK
            check (calper > 0.0),
    INSTYPE  VARCHAR2(6),
    SEGTYPE  VARCHAR2(1)
        constraint WFDISC__SEGTYPE__CK
            check (segtype IN ('o', 'v', 's', 'd', '-')),
    DATATYPE VARCHAR2(2)
        constraint WFDISC__DATATYPE__CK
            check (datatype in ('t4', 'e1', 's4', 's3', 's2', 'g2', 'i4', '-')),
    CLIP     VARCHAR2(1)
        constraint WFDISC__CLIP__CK
            check (clip IN ('c', 'n', '-')),
    DIR      VARCHAR2(64),
    DFILE    VARCHAR2(32),
    FOFF     NUMBER(10)
        constraint WFDISC__FOFF__CK
            check (foff >= 0),
    COMMID   NUMBER(18)
        constraint WFDISC__COMMID__CK
            check (commid > 0 OR commid = -1),
    LDDATE   DATE
);

CREATE TABLE IF NOT EXISTS GMS_GLOBAL.ASSOC
(
ARID NUMBER(18,0),
ORID NUMBER(18,0),
STA VARCHAR2(6),
PHASE VARCHAR2(8),
BELIEF FLOAT(24),
DELTA FLOAT(24),
SEAZ FLOAT(24),
ESAZ FLOAT(24),
TIMERES FLOAT(24),
TIMEDEF VARCHAR2(1),
AZRES FLOAT(24),
AZDEF VARCHAR2(1),
SLORES FLOAT(24),
SLODEF VARCHAR2(1),
EMARES FLOAT(24),
WGT FLOAT(24),
VMODEL VARCHAR2(15),
COMMID NUMBER(18,0),
LDDATE DATE);

CREATE TABLE GMS_GLOBAL.AMPLITUDE
(
AMPID NUMBER(18,0),
ARID NUMBER(18,0),
PARID NUMBER(18,0),
CHAN VARCHAR2(8),
AMP FLOAT(24),
PER FLOAT(24),
SNR FLOAT(24),
AMPTIME FLOAT(53),
TIME FLOAT(53),
DURATION FLOAT(24),
DELTAF FLOAT(24),
AMPTYPE VARCHAR2(8),
UNITS VARCHAR2(15),
CLIP VARCHAR2(1),
INARRIVAL VARCHAR2(1),
AUTH VARCHAR2(15),
LDDATE DATE) ;