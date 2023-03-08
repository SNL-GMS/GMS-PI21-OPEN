package gms.shared.stationdefinition.testfixtures;

import gms.shared.stationdefinition.dao.css.enums.Band;
import gms.shared.stationdefinition.dao.css.enums.ChannelType;
import gms.shared.stationdefinition.dao.css.enums.DataType;
import gms.shared.stationdefinition.dao.css.enums.Digital;
import gms.shared.stationdefinition.dao.css.enums.SegType;
import gms.shared.stationdefinition.dao.css.enums.StaType;
import gms.shared.utilities.bridge.database.enums.ClipFlag;

import java.time.Instant;
import java.util.Map;

public final class CssDaoAndCoiParameters {

  private CssDaoAndCoiParameters() {
  }

  public enum SITE_ARGS {
    LONGITUDE, LATITUDE, ELEVATION, DNORTH, DEAST
  }

  public enum SITE_CHAN_ARGS {
    EMPLACEMENT, HORIZONTAL, VERTICAL
  }

  public enum SENSOR_ARGS {
    CALIBRATION, CALIBPER, SHIFT
  }

  public enum INSTRUMENT_ARGS {
    SAMPLERATE, CALIBFACTOR, CALIBRATIO
  }

  public enum WFDISC_ARGS {
    CALIBPER, CALIB, SAMPLERATE, NSAMP, FOFF, COMMID
  }

  public static final Instant ONDATE = Instant.parse("2000-04-01T00:00:00Z");
  public static final Instant ONDATE2 = Instant.parse("2000-06-02T00:00:00Z");
  public static final Instant ONDATE3 = Instant.parse("2000-08-13T07:00:00Z");
  public static final Instant ONDATE4 = Instant.parse("2000-11-29T09:00:00Z");
  public static final Instant ONDATE5 = Instant.parse("2003-09-20T00:00:00Z");
  public static final Instant ONDATE6 = Instant.parse("2000-06-01T00:00:00Z");
  public static final Instant ONDATE7 = Instant.parse("2000-06-16T00:00:00Z");
  public static final Instant ONDATE8 = Instant.parse("2000-06-02T00:00:00Z");
  public static final Instant ONDATE9 = Instant.parse("2000-08-14T07:00:00Z");
  public static final Instant OFFDATE = Instant.parse("2020-04-01T00:00:00Z");
  public static final Instant OFFDATE2 = Instant.parse("2020-06-02T02:00:00Z");
  public static final Instant OFFDATE3 = Instant.parse("2020-08-13T07:00:00Z");
  public static final Instant OFFDATE4 = Instant.parse("2020-10-13T07:00:00Z");
  public static final Instant OFFDATE5 = Instant.parse("2286-11-20T00:00:00Z");
  public static final Instant OFFDATE6 = Instant.parse("2000-06-15T00:00:00Z");
  public static final Instant OFFDATE7 = Instant.parse("2021-12-15T00:00:00Z");
  public static final Instant MIN_START_TIME = Instant.ofEpochSecond(954547199);
  public static final Instant MAX_END_TIME = Instant.ofEpochSecond(1585742400).plusNanos(999000000);
  public static final Instant OFFDATE_NA = Instant.MAX;
  public static final String STATION_NAME = "A_TEST_STATION_NOWHERE";
  public static final String CHAN_DESC = "a-test-channel";
  public static final StaType STATION_TYPE_1 = StaType.SINGLE_STATION;
  public static final StaType STATION_TYPE_2 = StaType.SINGLE_STATION;
  public static final StaType STATION_TYPE_3 = StaType.ARRAY_STATION;
  public static final ChannelType CHANNEL_TYPE = ChannelType.N;
  public static final String AUTHOR1 = "person1";
  public static final String REFERENCE_STATION = "STA";
  public static final String REFERENCE_STATION_2 = "XXTA";
  public static final String REFERENCE_STATION_3 = "YYTA";
  public static final String REFERENCE_STATION_4 = "WWTA";

  public static final Instant MODDATE = Instant.parse("2015-04-11T12:00:00Z");
  public static final Instant LDATE = Instant.parse("2015-04-11T12:00:00Z");
  public static final String STA1 = "STA01";
  public static final String STA2 = "STA02";
  public static final String STA3 = "STA03";
  public static final String STA4 = "STA04";
  public static final String STAXX1 = "XXTA01";
  public static final String STAXX2 = "XXTA02";
  public static final String STAYY1 = "YYTA01";
  public static final String STAYY2 = "YYTA02";
  public static final String STAYY3 = "YYTA03";
  public static final String STAWW1 = "WWTA01";
  public static final String STAWW2 = "WWTA02";
  public static final String UN_STA = "BLAH";
  public static final String ANOTHER_STA = "SSTA";
  public static final String ANOTHER_STA2 = "SSTA0";
  public static final String ANOTHER_REF_STA = "SSTA";
  public static final String CHAN1 = "BHE";
  public static final String CHAN2 = "BHN";
  public static final String CHAN3 = "SHZ";
  public static final String CHAN4 = "BHZ";
  public static final String CHAN5 = "sz";
  public static final String CHAN6 = "BH1";
  public static final String CHAN7 = "BH2";

  public static final String CHANNEL_DESCRIPTION_1 = "short-period_vertical";
  public static final String CHANNEL_DESCRIPTION_2 = "broad-band_one";
  public static final String CHANNEL_DESCRIPTION_3 = "broad-band_two";
  public static final String CHANNEL_DESCRIPTION_4 = "broad-band_vertical";
  public static final String CHANNEL_DESCRIPTION_5 = "broad-band_east";
  public static final String CHANNEL_DESCRIPTION_6 = "broad-band_north";

  public static final Instant START_TIME = Instant.parse("1970-02-13T02:48:04.486Z");
  public static final Instant END_TIME = Instant.MAX;
  public static final int CHANID_1 = 7;
  public static final int CHANID_2 = 8;
  public static final int CHANID_3 = 9;
  public static final int CHANID_4 = 10;
  public static final int CHANID_5 = 11;
  public static final int CHANID_6 = 12;
  public static final int WFID_1 = 15;
  public static final int WFID_2 = 16;
  public static final int WFID_3 = 17;
  public static final int WFID_4 = 22;
  public static final int WFID_5 = 33;
  public static final SegType SEG_TYPE_1 = SegType.ORIGINAL;
  public static final SegType SEG_TYPE_2 = SegType.NA;
  public static final int UN_CHANID = 2891;
  public static final long INSTID_1 = 111;
  public static final long INSTID_2 = 112;
  public static final long INSTID_3 = 113;
  public static final long UN_INSTID = 3659;
  public static final String INS_NAME = "a-test-instrument";
  public static final Band BAND_TYPE = Band.BROADBAND;
  public static final Digital DIGITAL_TYPE = Digital.DIGITAL;
  public static final String DIR = "src/test/resources/FAP/";
  public static final String DFILE = "fakefile.fap";
  public static final String RESPONSE_TYPE = "fap";
  public static final String INSTRUMENT_TYPE = "NA";
  public static final DataType DATA_TYPE = DataType.E1;
  public static final ClipFlag CLIP_FLAG = ClipFlag.CLIPPED;

  public static final String NETWORK_NAME_1 = "GROUP";
  public static final String NETWORK_NAME_2 = "TEAM";
  public static final String NETWORK_DESC_1 = "Test Station Group 1";
  public static final String NETWORK_DESC_2 = "Test Station Group 2";
  public static final String NETWORK_DESC_3 = "Test Station Group 3";
  public static final String NETWORK_DESC_4 = "Test Station Group 4";

  public static final long NETWORK_ID_10 = 10;
  public static final long NETWORK_ID_11 = 11;
  public static final long NETWORK_ID_12 = 12;
  public static final long NETWORK_ID_15 = 15;
  public static final long NETWORK_ID_31 = 31;
  public static final long NETWORK_ID_32 = 32;
  public static final long NETWORK_ID_33 = 33;

  public static final double LATITUDE1 = 52.86897;
  public static final double LONGITUDE1 = 76.31056;
  public static final double ELEVATION1 = 0.618;
  public static final double ELEVATION2 = 0.7;

  public static final double DEGREES_NORTH = 0.123;
  public static final double DEGREES_EAST = 4.567;

  public static final long ARID_1 = 1001L;
  public static final long ARID_2 = 1002L;
  public static final long ARID_3 = 1003L;

  public static final Map<SITE_ARGS, Double> STA1_PARAM_MAP = Map.ofEntries(
    Map.entry(SITE_ARGS.LATITUDE, LATITUDE1), Map.entry(SITE_ARGS.LONGITUDE, LONGITUDE1),
    Map.entry(SITE_ARGS.ELEVATION, ELEVATION1), Map.entry(SITE_ARGS.DEAST, -3.649),
    Map.entry(SITE_ARGS.DNORTH, 0.770));
  public static final Map<SITE_ARGS, Double> STA2_PARAM_MAP = Map.ofEntries(
    Map.entry(SITE_ARGS.LATITUDE, 52.72137), Map.entry(SITE_ARGS.LONGITUDE, 76.20036),
    Map.entry(SITE_ARGS.ELEVATION, 0.620), Map.entry(SITE_ARGS.DEAST, -2.700),
    Map.entry(SITE_ARGS.DNORTH, 0.771));
  public static final Map<SITE_ARGS, Double> STA3_PARAM_MAP = Map.ofEntries(
    Map.entry(SITE_ARGS.LATITUDE, 52.42311), Map.entry(SITE_ARGS.LONGITUDE, 76.12066),
    Map.entry(SITE_ARGS.ELEVATION, 0.621), Map.entry(SITE_ARGS.DEAST, 0.0),
    Map.entry(SITE_ARGS.DNORTH, 0.0));

  public static final Map<SITE_CHAN_ARGS, Double> CHAN_PARAM_MAP = Map.ofEntries(
    Map.entry(SITE_CHAN_ARGS.EMPLACEMENT, 0.03), Map.entry(SITE_CHAN_ARGS.HORIZONTAL, -1.0),
    Map.entry(SITE_CHAN_ARGS.VERTICAL, 0.0));

  public static final Map<SENSOR_ARGS, Double> SENSOR_PARAM_MAP = Map.ofEntries(
    Map.entry(SENSOR_ARGS.CALIBRATION, 1.0), Map.entry(SENSOR_ARGS.CALIBPER, 1.0),
    Map.entry(SENSOR_ARGS.SHIFT, 0.0));
  public static final Map<SENSOR_ARGS, Double> SENSOR_PARAM_MAP_2 = Map.ofEntries(
    Map.entry(SENSOR_ARGS.CALIBRATION, 1.0), Map.entry(SENSOR_ARGS.CALIBPER, 1.0),
    Map.entry(SENSOR_ARGS.SHIFT, 1.0));

  public static final Map<INSTRUMENT_ARGS, Double> INSTRUMENT_PARAM_MAP = Map.ofEntries(
    Map.entry(INSTRUMENT_ARGS.CALIBFACTOR, 0.061607), Map.entry(INSTRUMENT_ARGS.CALIBRATIO, 1.0),
    Map.entry(INSTRUMENT_ARGS.SAMPLERATE, 40.0));

  public static final Map<INSTRUMENT_ARGS, Double> INSTRUMENT_PARAM_MAP_2 = Map.ofEntries(
    Map.entry(INSTRUMENT_ARGS.CALIBFACTOR, 0.061607), Map.entry(INSTRUMENT_ARGS.CALIBRATIO, 1.0),
    Map.entry(INSTRUMENT_ARGS.SAMPLERATE, 50.0));

  public static final Map<WFDISC_ARGS, Number> WFDISC_PARAM_MAP = Map.ofEntries(
    Map.entry(WFDISC_ARGS.CALIB, 0.061607), Map.entry(WFDISC_ARGS.CALIBPER, 1.0),
    Map.entry(WFDISC_ARGS.NSAMP, 1), Map.entry(WFDISC_ARGS.SAMPLERATE, 40.0),
    Map.entry(WFDISC_ARGS.FOFF, 20L), Map.entry(WFDISC_ARGS.COMMID, 21L));

  public static final Map<WFDISC_ARGS, Number> WFDISC_PARAM_MAP_2 = Map.ofEntries(
    Map.entry(WFDISC_ARGS.CALIB, 0.999999), Map.entry(WFDISC_ARGS.CALIBPER, 1.0),
    Map.entry(WFDISC_ARGS.NSAMP, 1), Map.entry(WFDISC_ARGS.SAMPLERATE, 40.0),
    Map.entry(WFDISC_ARGS.FOFF, 20L), Map.entry(WFDISC_ARGS.COMMID, 21L));
}
