package gms.shared.signaldetection.testfixtures;

import gms.shared.signaldetection.dao.css.AmplitudeDao;
import gms.shared.signaldetection.dao.css.AridOridKey;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.signaldetection.dao.css.AssocDao;
import gms.shared.signaldetection.dao.css.enums.AmplitudeUnits;
import gms.shared.signaldetection.dao.css.enums.DefiningFlag;
import gms.shared.signaldetection.dao.css.enums.SignalType;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.stationdefinition.dao.css.StationChannelTimeKey;
import gms.shared.stationdefinition.dao.css.WfTagDao;
import gms.shared.stationdefinition.dao.css.WfTagKey;
import gms.shared.stationdefinition.dao.css.enums.TagName;
import gms.shared.utilities.bridge.database.enums.ClipFlag;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Duration;
import java.time.Instant;

import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHAN1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHAN2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHAN3;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STA1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STA2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.WFID_1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.WFID_3;

public class SignalDetectionDaoTestFixtures {
  public static final ArrivalDao ARRIVAL_1;
  public static final ArrivalDao ARRIVAL_2;
  public static final ArrivalDao ARRIVAL_3;
  public static final ArrivalDao ARRIVAL_4;

  public static final ArrivalDao ARRIVAL_TEST_1;
  public static final ArrivalDao ARRIVAL_TEST_2;
  public static final ArrivalDao ARRIVAL_TEST_3;
  public static final ArrivalDao ARRIVAL_TEST_4;

  public static final AssocDao ASSOC_DAO_1;
  public static final AssocDao ASSOC_DAO_2;
  public static final AssocDao ASSOC_DAO_3;
  public static final AssocDao ASSOC_DAO_4;

  public static final AssocDao ASSOC_TEST_1;
  public static final AssocDao ASSOC_TEST_3;

  public static final AmplitudeDao AMPLITUDE_DAO_1;

  public static final WfTagDao WFTAG_1;
  public static final WfTagDao WFTAG_3;
  public static final WfTagDao WFTAG_TEST_1;
  public static final WfTagDao WFTAG_TEST_3;

  public static final String STAGE_1 = "Stage 1";
  public static final String STAGE_2 = "Stage 2";
  public static final String STAGE_3 = "Stage 3";
  public static final String STAGE_3A = "Stage 3A";
  public static final String STAGE_3B = "Stage 3B";

  public static final String STAGE_1_ACCT = "Acc1";
  public static final String STAGE_2_ACCT = "Acc2";
  public static final String STAGE_3_ACCT = "Acc3";

  public static final String CHAN = "CHAN";
  public static final String CHANNEL_CODE = "BHZ";

  public static final String TEST_USERS = "test users";

  public static final long ORID_1 = 254L;
  public static final long ORID_2 = 255L;
  public static final long ARID = 23L;
  public static final long AMPID = 554;
  public static final String STA = "TEST";
  public static final double DEFAULT_DOUBLE = 21.22;
  public static final String DEFAULT_STRING = "test string";
  public static final long DEFAULT_LONG = 314L;
  public static final Instant DEFAULT_INSTANT = Instant.parse("2015-12-07T12:13:14.000Z");
  public static final Duration DEFAULT_DURATION = Duration.ofMillis(134897);
  public static final double DEFAULT_PERIOD = 0.433923183;
  public static final Duration DEFAULT_PERIOD_DURATION = Duration.ofNanos(433923183);
  public static final AmplitudeUnits DEFAULT_AMPLITUDE_UNITS = AmplitudeUnits.HERTZ;
  public static final Units CORRESPONDING_UNITS = Units.HERTZ;
  public static final ClipFlag CLIP_FLAG = ClipFlag.CLIPPED;
  public static final boolean CORRESPONDING_CLIP_BOOLEAN = true;

  static {
    ARRIVAL_1 = new ArrivalDao();
    StationChannelTimeKey arrivalKey1 = new StationChannelTimeKey();
    arrivalKey1.setStationCode(CHAN);
    arrivalKey1.setChannelCode(CHANNEL_CODE);
    arrivalKey1.setTime(Instant.EPOCH);
    ARRIVAL_1.setArrivalKey(arrivalKey1);
    ARRIVAL_1.setId(1);
    ARRIVAL_1.setjDate(Instant.EPOCH);
    ARRIVAL_1.setSingleStationOriginId(2);
    ARRIVAL_1.setChannelId(2);
    ARRIVAL_1.setPhase("P");
    ARRIVAL_1.setSignalType(SignalType.LOCAL_EVENT);
    ARRIVAL_1.setCommid(2);
    ARRIVAL_1.setTimeUncertainty(0.001);
    ARRIVAL_1.setAzimuth(180);
    ARRIVAL_1.setAzimuthUncertainty(0.5);
    ARRIVAL_1.setSlowness(32.1);
    ARRIVAL_1.setSlownessUncertainty(0.23);
    ARRIVAL_1.setEmergenceAngle(37);
    ARRIVAL_1.setRectilinearity(0.23);
    ARRIVAL_1.setAmplitude(45.03);
    ARRIVAL_1.setPeriod(0.234);
    ARRIVAL_1.setLogAmpliterPeriod(76.23);
    ARRIVAL_1.setClipped(ClipFlag.CLIPPED);
    ARRIVAL_1.setFirstMotion("c");
    ARRIVAL_1.setSnr(23.41);
    ARRIVAL_1.setSignalOnsetQuality("test");
    ARRIVAL_1.setAuthor(TEST_USERS);
    ARRIVAL_1.setLoadDate(Instant.EPOCH.plusSeconds(300));

    ARRIVAL_3 = new ArrivalDao();
    StationChannelTimeKey arrivalKey3 = new StationChannelTimeKey();
    arrivalKey3.setStationCode(CHAN);
    arrivalKey3.setChannelCode(CHANNEL_CODE);
    arrivalKey3.setTime(Instant.EPOCH);
    ARRIVAL_3.setArrivalKey(arrivalKey3);
    ARRIVAL_3.setId(2);
    ARRIVAL_3.setjDate(Instant.EPOCH);
    ARRIVAL_3.setSingleStationOriginId(2);
    ARRIVAL_3.setChannelId(2);
    ARRIVAL_3.setPhase("P");
    ARRIVAL_3.setSignalType(SignalType.LOCAL_EVENT);
    ARRIVAL_3.setCommid(2);
    ARRIVAL_3.setTimeUncertainty(0.01);
    ARRIVAL_3.setAzimuth(180);
    ARRIVAL_3.setAzimuthUncertainty(0.5);
    ARRIVAL_3.setSlowness(32.1);
    ARRIVAL_3.setSlownessUncertainty(0.23);
    ARRIVAL_3.setEmergenceAngle(37);
    ARRIVAL_3.setRectilinearity(0.23);
    ARRIVAL_3.setAmplitude(45.03);
    ARRIVAL_3.setPeriod(0.234);
    ARRIVAL_3.setLogAmpliterPeriod(76.23);
    ARRIVAL_3.setClipped(ClipFlag.CLIPPED);
    ARRIVAL_3.setFirstMotion("r");
    ARRIVAL_3.setSnr(23.41);
    ARRIVAL_3.setSignalOnsetQuality("test");
    ARRIVAL_3.setAuthor(TEST_USERS);
    ARRIVAL_3.setLoadDate(Instant.EPOCH.plusSeconds(300));

    ASSOC_DAO_1 = new AssocDao();
    ASSOC_DAO_1.setId(new AridOridKey.Builder()
      .withOriginId(ORID_1)
      .withArrivalId(ARID)
      .build());
    ASSOC_DAO_1.setStationCode(STA);
    ASSOC_DAO_1.setPhase("S");
    ASSOC_DAO_1.setBelief(0.8);
    ASSOC_DAO_1.setDelta(DEFAULT_DOUBLE);
    ASSOC_DAO_1.setStationToEventAzimuth(DEFAULT_DOUBLE);
    ASSOC_DAO_1.setEventToStationAzimuth(DEFAULT_DOUBLE);
    ASSOC_DAO_1.setTimeResidual(DEFAULT_DOUBLE);
    ASSOC_DAO_1.setTimeDefining(DefiningFlag.DEFAULT_DEFINING);
    ASSOC_DAO_1.setAzimuthResidual(DEFAULT_DOUBLE);
    ASSOC_DAO_1.setAzimuthDefining(DefiningFlag.DEFAULT_DEFINING);
    ASSOC_DAO_1.setSlownessResidual(DEFAULT_DOUBLE);
    ASSOC_DAO_1.setSlownessDefining(DefiningFlag.DEFAULT_DEFINING);
    ASSOC_DAO_1.setEmergenceAngleResidual(DEFAULT_DOUBLE);
    ASSOC_DAO_1.setLocationWeight(DEFAULT_DOUBLE);
    ASSOC_DAO_1.setVelocityModel(DEFAULT_STRING);
    ASSOC_DAO_1.setCommentId(DEFAULT_LONG);
    ASSOC_DAO_1.setLoadDate(DEFAULT_INSTANT);

    ASSOC_DAO_2 = new AssocDao();
    ASSOC_DAO_2.setId(new AridOridKey.Builder()
      .withOriginId(ORID_1)
      .withArrivalId(ARID)
      .build());
    ASSOC_DAO_2.setStationCode(STA);
    ASSOC_DAO_2.setPhase("P");
    ASSOC_DAO_2.setBelief(0.5);
    ASSOC_DAO_2.setDelta(0.1);
    ASSOC_DAO_2.setStationToEventAzimuth(DEFAULT_DOUBLE);
    ASSOC_DAO_2.setEventToStationAzimuth(DEFAULT_DOUBLE);
    ASSOC_DAO_2.setTimeResidual(DEFAULT_DOUBLE);
    ASSOC_DAO_2.setTimeDefining(DefiningFlag.DEFAULT_DEFINING);
    ASSOC_DAO_2.setAzimuthResidual(DEFAULT_DOUBLE);
    ASSOC_DAO_2.setAzimuthDefining(DefiningFlag.DEFAULT_DEFINING);
    ASSOC_DAO_2.setSlownessResidual(DEFAULT_DOUBLE);
    ASSOC_DAO_2.setSlownessDefining(DefiningFlag.DEFAULT_DEFINING);
    ASSOC_DAO_2.setEmergenceAngleResidual(DEFAULT_DOUBLE);
    ASSOC_DAO_2.setLocationWeight(DEFAULT_DOUBLE);
    ASSOC_DAO_2.setVelocityModel(DEFAULT_STRING);
    ASSOC_DAO_2.setCommentId(DEFAULT_LONG);
    ASSOC_DAO_2.setLoadDate(DEFAULT_INSTANT);

    ASSOC_DAO_3 = new AssocDao();
    ASSOC_DAO_3.setId(new AridOridKey.Builder()
      .withOriginId(ORID_1)
      .withArrivalId(ARID)
      .build());
    ASSOC_DAO_3.setStationCode(STA);
    ASSOC_DAO_3.setPhase("P");
    ASSOC_DAO_3.setBelief(5);
    ASSOC_DAO_3.setDelta(0.1);
    ASSOC_DAO_3.setStationToEventAzimuth(DEFAULT_DOUBLE);
    ASSOC_DAO_3.setEventToStationAzimuth(DEFAULT_DOUBLE);
    ASSOC_DAO_3.setTimeResidual(DEFAULT_DOUBLE);
    ASSOC_DAO_3.setTimeDefining(DefiningFlag.DEFAULT_DEFINING);
    ASSOC_DAO_3.setAzimuthResidual(DEFAULT_DOUBLE);
    ASSOC_DAO_3.setAzimuthDefining(DefiningFlag.DEFAULT_DEFINING);
    ASSOC_DAO_3.setSlownessResidual(DEFAULT_DOUBLE);
    ASSOC_DAO_3.setSlownessDefining(DefiningFlag.DEFAULT_DEFINING);
    ASSOC_DAO_3.setEmergenceAngleResidual(DEFAULT_DOUBLE);
    ASSOC_DAO_3.setLocationWeight(DEFAULT_DOUBLE);
    ASSOC_DAO_3.setVelocityModel(DEFAULT_STRING);
    ASSOC_DAO_3.setCommentId(DEFAULT_LONG);
    ASSOC_DAO_3.setLoadDate(DEFAULT_INSTANT);

    ASSOC_DAO_4 = new AssocDao();
    ASSOC_DAO_4.setId(new AridOridKey.Builder()
      .withOriginId(ORID_1)
      .withArrivalId(ARID)
      .build());
    ASSOC_DAO_4.setStationCode(STA);
    ASSOC_DAO_4.setPhase("P");
    ASSOC_DAO_4.setBelief(-1);
    ASSOC_DAO_4.setDelta(0.1);
    ASSOC_DAO_4.setStationToEventAzimuth(DEFAULT_DOUBLE);
    ASSOC_DAO_4.setEventToStationAzimuth(DEFAULT_DOUBLE);
    ASSOC_DAO_4.setTimeResidual(DEFAULT_DOUBLE);
    ASSOC_DAO_4.setTimeDefining(DefiningFlag.DEFAULT_DEFINING);
    ASSOC_DAO_4.setAzimuthResidual(DEFAULT_DOUBLE);
    ASSOC_DAO_4.setAzimuthDefining(DefiningFlag.DEFAULT_DEFINING);
    ASSOC_DAO_4.setSlownessResidual(DEFAULT_DOUBLE);
    ASSOC_DAO_4.setSlownessDefining(DefiningFlag.DEFAULT_DEFINING);
    ASSOC_DAO_4.setEmergenceAngleResidual(DEFAULT_DOUBLE);
    ASSOC_DAO_4.setLocationWeight(DEFAULT_DOUBLE);
    ASSOC_DAO_4.setVelocityModel(DEFAULT_STRING);
    ASSOC_DAO_4.setCommentId(DEFAULT_LONG);
    ASSOC_DAO_4.setLoadDate(DEFAULT_INSTANT);

    AMPLITUDE_DAO_1 = new AmplitudeDao();
    AMPLITUDE_DAO_1.setId(AMPID);
    AMPLITUDE_DAO_1.setArrivalId(ARID);
    AMPLITUDE_DAO_1.setPredictedArrivalId(ARID);
    AMPLITUDE_DAO_1.setChannelCode(CHAN);
    AMPLITUDE_DAO_1.setAmplitude(432.23);
    AMPLITUDE_DAO_1.setPeriod(DEFAULT_PERIOD);
    AMPLITUDE_DAO_1.setSnr(DEFAULT_DOUBLE);
    AMPLITUDE_DAO_1.setAmplitudeTime(DEFAULT_INSTANT);
    AMPLITUDE_DAO_1.setTime(DEFAULT_INSTANT);
    AMPLITUDE_DAO_1.setDuration(DEFAULT_DURATION);
    AMPLITUDE_DAO_1.setSampleIntervalWidth(DEFAULT_DOUBLE);
    AMPLITUDE_DAO_1.setAmplitudeType(DEFAULT_STRING);
    AMPLITUDE_DAO_1.setUnits(DEFAULT_AMPLITUDE_UNITS);
    AMPLITUDE_DAO_1.setClip(CLIP_FLAG);
    AMPLITUDE_DAO_1.setInArrival(true);
    AMPLITUDE_DAO_1.setAuthor(DEFAULT_STRING);
    AMPLITUDE_DAO_1.setLoadDate(DEFAULT_INSTANT);

    WFTAG_1 = new WfTagDao();
    WfTagKey wftag1Key = new WfTagKey();
    wftag1Key.setId(ARRIVAL_1.getId());
    wftag1Key.setTagName(TagName.ARID);
    wftag1Key.setWfId(WFID_1);
    WFTAG_1.setWfTagKey(wftag1Key);
    WFTAG_1.setLoadDate(Instant.EPOCH);

    ARRIVAL_TEST_1 = new ArrivalDao();
    StationChannelTimeKey arrivalTestKey1 = new StationChannelTimeKey();
    arrivalTestKey1.setStationCode(STA1);
    arrivalTestKey1.setChannelCode(CHAN1);
    arrivalTestKey1.setTime(Instant.EPOCH);
    ARRIVAL_TEST_1.setArrivalKey(arrivalTestKey1);
    ARRIVAL_TEST_1.setId(1);
    ARRIVAL_TEST_1.setjDate(Instant.EPOCH);
    ARRIVAL_TEST_1.setSingleStationOriginId(2);
    ARRIVAL_TEST_1.setChannelId(2);
    ARRIVAL_TEST_1.setPhase("P");
    ARRIVAL_TEST_1.setSignalType(SignalType.LOCAL_EVENT);
    ARRIVAL_TEST_1.setCommid(2);
    ARRIVAL_TEST_1.setTimeUncertainty(0.001);
    ARRIVAL_TEST_1.setAzimuth(180);
    ARRIVAL_TEST_1.setAzimuthUncertainty(0.5);
    ARRIVAL_TEST_1.setSlowness(32.1);
    ARRIVAL_TEST_1.setSlownessUncertainty(0.23);
    ARRIVAL_TEST_1.setEmergenceAngle(37);
    ARRIVAL_TEST_1.setRectilinearity(0.23);
    ARRIVAL_TEST_1.setAmplitude(45.03);
    ARRIVAL_TEST_1.setPeriod(0.234);
    ARRIVAL_TEST_1.setLogAmpliterPeriod(76.23);
    ARRIVAL_TEST_1.setClipped(ClipFlag.CLIPPED);
    ARRIVAL_TEST_1.setFirstMotion("c");
    ARRIVAL_TEST_1.setSnr(23.41);
    ARRIVAL_TEST_1.setSignalOnsetQuality("test");
    ARRIVAL_TEST_1.setAuthor(TEST_USERS);
    ARRIVAL_TEST_1.setLoadDate(Instant.EPOCH.plusSeconds(300));

    ASSOC_TEST_1 = new AssocDao();
    ASSOC_TEST_1.setId(new AridOridKey.Builder()
      .withOriginId(ORID_1)
      .withArrivalId(ARRIVAL_1.getId())
      .build());
    ASSOC_TEST_1.setStationCode(STA);
    ASSOC_TEST_1.setPhase("S");
    ASSOC_TEST_1.setBelief(0.8);
    ASSOC_TEST_1.setDelta(DEFAULT_DOUBLE);
    ASSOC_TEST_1.setStationToEventAzimuth(DEFAULT_DOUBLE);
    ASSOC_TEST_1.setEventToStationAzimuth(DEFAULT_DOUBLE);
    ASSOC_TEST_1.setTimeResidual(DEFAULT_DOUBLE);
    ASSOC_TEST_1.setTimeDefining(DefiningFlag.DEFAULT_DEFINING);
    ASSOC_TEST_1.setAzimuthResidual(DEFAULT_DOUBLE);
    ASSOC_TEST_1.setAzimuthDefining(DefiningFlag.DEFAULT_DEFINING);
    ASSOC_TEST_1.setSlownessResidual(DEFAULT_DOUBLE);
    ASSOC_TEST_1.setSlownessDefining(DefiningFlag.DEFAULT_DEFINING);
    ASSOC_TEST_1.setEmergenceAngleResidual(DEFAULT_DOUBLE);
    ASSOC_TEST_1.setLocationWeight(DEFAULT_DOUBLE);
    ASSOC_TEST_1.setVelocityModel(DEFAULT_STRING);
    ASSOC_TEST_1.setCommentId(DEFAULT_LONG);
    ASSOC_TEST_1.setLoadDate(DEFAULT_INSTANT);

    ARRIVAL_TEST_2 = new ArrivalDao();
    StationChannelTimeKey arrivalTestKey2 = new StationChannelTimeKey();
    arrivalTestKey2.setStationCode(STA2);
    arrivalTestKey2.setChannelCode(CHAN2);
    arrivalTestKey2.setTime(Instant.EPOCH);
    ARRIVAL_TEST_2.setArrivalKey(arrivalTestKey2);
    ARRIVAL_TEST_2.setId(3);
    ARRIVAL_TEST_2.setjDate(Instant.EPOCH);
    ARRIVAL_TEST_2.setSingleStationOriginId(2);
    ARRIVAL_TEST_2.setChannelId(2);
    ARRIVAL_TEST_2.setPhase("P");
    ARRIVAL_TEST_2.setSignalType(SignalType.LOCAL_EVENT);
    ARRIVAL_TEST_2.setCommid(2);
    ARRIVAL_TEST_2.setTimeUncertainty(0.001);
    ARRIVAL_TEST_2.setAzimuth(180);
    ARRIVAL_TEST_2.setAzimuthUncertainty(0.5);
    ARRIVAL_TEST_2.setSlowness(32.1);
    ARRIVAL_TEST_2.setSlownessUncertainty(0.23);
    ARRIVAL_TEST_2.setEmergenceAngle(37);
    ARRIVAL_TEST_2.setRectilinearity(0.23);
    ARRIVAL_TEST_2.setAmplitude(45.03);
    ARRIVAL_TEST_2.setPeriod(0.234);
    ARRIVAL_TEST_2.setLogAmpliterPeriod(76.23);
    ARRIVAL_TEST_2.setClipped(ClipFlag.CLIPPED);
    ARRIVAL_TEST_2.setFirstMotion("c");
    ARRIVAL_TEST_2.setSnr(23.41);
    ARRIVAL_TEST_2.setSignalOnsetQuality("test");
    ARRIVAL_TEST_2.setAuthor(TEST_USERS);
    ARRIVAL_TEST_2.setLoadDate(Instant.EPOCH.plusSeconds(300));

    ARRIVAL_TEST_3 = new ArrivalDao();
    StationChannelTimeKey arrivalTestKey3 = new StationChannelTimeKey();
    arrivalTestKey3.setStationCode(STA2);
    arrivalTestKey3.setChannelCode(CHAN3);
    arrivalTestKey3.setTime(Instant.EPOCH);
    ARRIVAL_TEST_3.setArrivalKey(arrivalTestKey3);
    ARRIVAL_TEST_3.setId(2);
    ARRIVAL_TEST_3.setjDate(Instant.EPOCH);
    ARRIVAL_TEST_3.setSingleStationOriginId(2);
    ARRIVAL_TEST_3.setChannelId(2);
    ARRIVAL_TEST_3.setPhase("P");
    ARRIVAL_TEST_3.setSignalType(SignalType.LOCAL_EVENT);
    ARRIVAL_TEST_3.setCommid(2);
    ARRIVAL_TEST_3.setTimeUncertainty(0.01);
    ARRIVAL_TEST_3.setAzimuth(180);
    ARRIVAL_TEST_3.setAzimuthUncertainty(0.5);
    ARRIVAL_TEST_3.setSlowness(32.1);
    ARRIVAL_TEST_3.setSlownessUncertainty(0.23);
    ARRIVAL_TEST_3.setEmergenceAngle(37);
    ARRIVAL_TEST_3.setRectilinearity(0.23);
    ARRIVAL_TEST_3.setAmplitude(45.03);
    ARRIVAL_TEST_3.setPeriod(0.234);
    ARRIVAL_TEST_3.setLogAmpliterPeriod(76.23);
    ARRIVAL_TEST_3.setClipped(ClipFlag.CLIPPED);
    ARRIVAL_TEST_3.setFirstMotion("r");
    ARRIVAL_TEST_3.setSnr(23.41);
    ARRIVAL_TEST_3.setSignalOnsetQuality("test");
    ARRIVAL_TEST_3.setAuthor(TEST_USERS);
    ARRIVAL_TEST_3.setLoadDate(Instant.EPOCH.plusSeconds(300));

    ARRIVAL_TEST_4 = new ArrivalDao();
    arrivalTestKey3.setStationCode(STA2);
    arrivalTestKey3.setChannelCode(CHAN3);
    arrivalTestKey3.setTime(Instant.EPOCH);
    ARRIVAL_TEST_4.setArrivalKey(arrivalTestKey3);
    ARRIVAL_TEST_4.setId(2);
    ARRIVAL_TEST_4.setjDate(Instant.EPOCH);
    ARRIVAL_TEST_4.setSingleStationOriginId(2);
    ARRIVAL_TEST_4.setChannelId(2);
    ARRIVAL_TEST_4.setPhase("N");
    ARRIVAL_TEST_4.setSignalType(SignalType.LOCAL_EVENT);
    ARRIVAL_TEST_4.setCommid(2);
    ARRIVAL_TEST_4.setTimeUncertainty(0.01);
    ARRIVAL_TEST_4.setAzimuth(180);
    ARRIVAL_TEST_4.setAzimuthUncertainty(0.5);
    ARRIVAL_TEST_4.setSlowness(32.1);
    ARRIVAL_TEST_4.setSlownessUncertainty(0.23);
    ARRIVAL_TEST_4.setEmergenceAngle(37);
    ARRIVAL_TEST_4.setRectilinearity(0.23);
    ARRIVAL_TEST_4.setAmplitude(45.03);
    ARRIVAL_TEST_4.setPeriod(0.234);
    ARRIVAL_TEST_4.setLogAmpliterPeriod(76.23);
    ARRIVAL_TEST_4.setClipped(ClipFlag.CLIPPED);
    ARRIVAL_TEST_4.setFirstMotion("r");
    ARRIVAL_TEST_4.setSnr(23.41);
    ARRIVAL_TEST_4.setSignalOnsetQuality("test");
    ARRIVAL_TEST_4.setAuthor(TEST_USERS);
    ARRIVAL_TEST_4.setLoadDate(Instant.EPOCH.plusSeconds(300));

    ASSOC_TEST_3 = new AssocDao();
    ASSOC_TEST_3.setId(new AridOridKey.Builder()
      .withOriginId(ORID_2)
      .withArrivalId(ARRIVAL_3.getId())
      .build());
    ASSOC_TEST_3.setStationCode(STA);
    ASSOC_TEST_3.setPhase("S");
    ASSOC_TEST_3.setBelief(0.8);
    ASSOC_TEST_3.setDelta(DEFAULT_DOUBLE);
    ASSOC_TEST_3.setStationToEventAzimuth(DEFAULT_DOUBLE);
    ASSOC_TEST_3.setEventToStationAzimuth(DEFAULT_DOUBLE);
    ASSOC_TEST_3.setTimeResidual(DEFAULT_DOUBLE);
    ASSOC_TEST_3.setTimeDefining(DefiningFlag.DEFAULT_DEFINING);
    ASSOC_TEST_3.setAzimuthResidual(DEFAULT_DOUBLE);
    ASSOC_TEST_3.setAzimuthDefining(DefiningFlag.DEFAULT_DEFINING);
    ASSOC_TEST_3.setSlownessResidual(DEFAULT_DOUBLE);
    ASSOC_TEST_3.setSlownessDefining(DefiningFlag.DEFAULT_DEFINING);
    ASSOC_TEST_3.setEmergenceAngleResidual(DEFAULT_DOUBLE);
    ASSOC_TEST_3.setLocationWeight(DEFAULT_DOUBLE);
    ASSOC_TEST_3.setVelocityModel(DEFAULT_STRING);
    ASSOC_TEST_3.setCommentId(DEFAULT_LONG);
    ASSOC_TEST_3.setLoadDate(DEFAULT_INSTANT);

    WFTAG_TEST_1 = new WfTagDao();
    WfTagKey wftagTest1Key = new WfTagKey();
    wftagTest1Key.setId(ARRIVAL_TEST_1.getId());
    wftagTest1Key.setTagName(TagName.ARID);
    wftagTest1Key.setWfId(WFID_1);
    WFTAG_TEST_1.setWfTagKey(wftagTest1Key);
    WFTAG_TEST_1.setLoadDate(Instant.EPOCH);

    WFTAG_TEST_3 = new WfTagDao();
    WfTagKey wftagTest3Key = new WfTagKey();
    wftagTest3Key.setId(ARRIVAL_TEST_3.getId());
    wftagTest3Key.setTagName(TagName.ARID);
    wftagTest3Key.setWfId(WFID_3);
    WFTAG_TEST_3.setWfTagKey(wftagTest3Key);
    WFTAG_TEST_3.setLoadDate(Instant.EPOCH);

    ARRIVAL_2 = new ArrivalDao();
    StationChannelTimeKey arrivalKey2 = new StationChannelTimeKey();
    arrivalKey2.setStationCode(CHAN);
    arrivalKey2.setChannelCode(CHANNEL_CODE);
    arrivalKey2.setTime(Instant.EPOCH.plusSeconds(1));
    ARRIVAL_2.setArrivalKey(arrivalKey2);
    ARRIVAL_2.setId(1);
    ARRIVAL_2.setjDate(Instant.EPOCH);
    ARRIVAL_2.setSingleStationOriginId(2);
    ARRIVAL_2.setChannelId(2);
    ARRIVAL_2.setPhase("P");
    ARRIVAL_2.setSignalType(SignalType.LOCAL_EVENT);
    ARRIVAL_2.setCommid(2);
    ARRIVAL_2.setTimeUncertainty(0.01);
    ARRIVAL_2.setAzimuth(180);
    ARRIVAL_2.setAzimuthUncertainty(0.5);
    ARRIVAL_2.setSlowness(32.1);
    ARRIVAL_2.setSlownessUncertainty(0.23);
    ARRIVAL_2.setEmergenceAngle(37);
    ARRIVAL_2.setRectilinearity(0.23);
    ARRIVAL_2.setAmplitude(45.03);
    ARRIVAL_2.setPeriod(0.234);
    ARRIVAL_2.setLogAmpliterPeriod(76.23);
    ARRIVAL_2.setClipped(ClipFlag.CLIPPED);
    ARRIVAL_2.setFirstMotion("cr");
    ARRIVAL_2.setSnr(23.41);
    ARRIVAL_2.setSignalOnsetQuality("test");
    ARRIVAL_2.setAuthor(TEST_USERS);
    ARRIVAL_2.setLoadDate(Instant.EPOCH.plusSeconds(300));

    WFTAG_3 = new WfTagDao();
    WfTagKey wftag3Key = new WfTagKey();
    wftag3Key.setId(ARRIVAL_3.getId());
    wftag3Key.setTagName(TagName.ARID);
    wftag3Key.setWfId(WFID_3);
    WFTAG_3.setWfTagKey(wftag3Key);
    WFTAG_3.setLoadDate(Instant.EPOCH);

    ARRIVAL_4 = new ArrivalDao();
    StationChannelTimeKey arrivalKey4 = new StationChannelTimeKey();
    arrivalKey4.setStationCode(CHAN);
    arrivalKey4.setChannelCode(CHANNEL_CODE);
    arrivalKey4.setTime(Instant.EPOCH.plusSeconds(100));
    ARRIVAL_4.setArrivalKey(arrivalKey4);
    ARRIVAL_4.setId(2);
    ARRIVAL_4.setjDate(Instant.EPOCH);
    ARRIVAL_4.setSingleStationOriginId(2);
    ARRIVAL_4.setChannelId(2);
    ARRIVAL_4.setPhase("I");
    ARRIVAL_4.setSignalType(SignalType.LOCAL_EVENT);
    ARRIVAL_4.setCommid(2);
    ARRIVAL_4.setTimeUncertainty(0.01);
    ARRIVAL_4.setAzimuth(180);
    ARRIVAL_4.setAzimuthUncertainty(0.5);
    ARRIVAL_4.setSlowness(32.1);
    ARRIVAL_4.setSlownessUncertainty(0.23);
    ARRIVAL_4.setEmergenceAngle(37);
    ARRIVAL_4.setRectilinearity(0.23);
    ARRIVAL_4.setAmplitude(45.03);
    ARRIVAL_4.setPeriod(0.234);
    ARRIVAL_4.setLogAmpliterPeriod(76.23);
    ARRIVAL_4.setClipped(ClipFlag.CLIPPED);
    ARRIVAL_4.setFirstMotion("-");
    ARRIVAL_4.setSnr(23.41);
    ARRIVAL_4.setSignalOnsetQuality("test");
    ARRIVAL_4.setAuthor(TEST_USERS);
    ARRIVAL_4.setLoadDate(Instant.EPOCH.plusSeconds(100));
  }

  private static ArrivalDao getArrivalDao(long arid) {
    var arrivalDao = new ArrivalDao();
    var arrivalKey1 = new StationChannelTimeKey();
    arrivalKey1.setStationCode(STA);
    arrivalKey1.setChannelCode(CHANNEL_CODE);
    arrivalKey1.setTime(Instant.EPOCH);
    arrivalDao.setArrivalKey(arrivalKey1);
    arrivalDao.setId(arid);
    arrivalDao.setjDate(Instant.EPOCH);
    arrivalDao.setSingleStationOriginId(2);
    arrivalDao.setChannelId(2);
    arrivalDao.setPhase("P");
    arrivalDao.setSignalType(SignalType.LOCAL_EVENT);
    arrivalDao.setCommid(2);
    arrivalDao.setTimeUncertainty(0.001);
    arrivalDao.setAzimuth(180);
    arrivalDao.setAzimuthUncertainty(0.5);
    arrivalDao.setSlowness(32.1);
    arrivalDao.setSlownessUncertainty(0.23);
    arrivalDao.setEmergenceAngle(37);
    arrivalDao.setRectilinearity(0.23);
    arrivalDao.setAmplitude(45.03);
    arrivalDao.setPeriod(0.234);
    arrivalDao.setLogAmpliterPeriod(76.23);
    arrivalDao.setClipped(ClipFlag.CLIPPED);
    arrivalDao.setFirstMotion("c");
    arrivalDao.setSnr(23.41);
    arrivalDao.setSignalOnsetQuality("test");
    arrivalDao.setAuthor(TEST_USERS);
    arrivalDao.setLoadDate(Instant.EPOCH.plusSeconds(300));

    return arrivalDao;
  }

  private static AssocDao getAssocDao(long arid, long orid) {
    var assocDao = new AssocDao();
    assocDao.setId(new AridOridKey.Builder()
      .withOriginId(orid)
      .withArrivalId(arid)
      .build());
    assocDao.setStationCode(STA);
    assocDao.setPhase("S");
    assocDao.setBelief(0.8);
    assocDao.setDelta(DEFAULT_DOUBLE);
    assocDao.setStationToEventAzimuth(DEFAULT_DOUBLE);
    assocDao.setEventToStationAzimuth(DEFAULT_DOUBLE);
    assocDao.setTimeResidual(DEFAULT_DOUBLE);
    assocDao.setTimeDefining(DefiningFlag.DEFAULT_DEFINING);
    assocDao.setAzimuthResidual(DEFAULT_DOUBLE);
    assocDao.setAzimuthDefining(DefiningFlag.DEFAULT_DEFINING);
    assocDao.setSlownessResidual(DEFAULT_DOUBLE);
    assocDao.setSlownessDefining(DefiningFlag.DEFAULT_DEFINING);
    assocDao.setEmergenceAngleResidual(DEFAULT_DOUBLE);
    assocDao.setLocationWeight(DEFAULT_DOUBLE);
    assocDao.setVelocityModel(DEFAULT_STRING);
    assocDao.setCommentId(DEFAULT_LONG);
    assocDao.setLoadDate(DEFAULT_INSTANT);

    return assocDao;
  }

  public static Pair<ArrivalDao, AssocDao> getArrivalAssocPair(long arid, long orid) {
    return Pair.of(getArrivalDao(arid), getAssocDao(arid, orid));
  }

  private SignalDetectionDaoTestFixtures() {
    // prevent instantiation
  }
}
