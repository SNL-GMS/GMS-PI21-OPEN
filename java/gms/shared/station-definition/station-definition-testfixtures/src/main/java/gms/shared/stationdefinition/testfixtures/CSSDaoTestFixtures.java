package gms.shared.stationdefinition.testfixtures;


import gms.shared.signaldetection.dao.css.AmplitudeDao;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.signaldetection.dao.css.enums.AmplitudeUnits;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.FrequencyAmplitudePhase;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.stationdefinition.dao.css.AffiliationDao;
import gms.shared.stationdefinition.dao.css.BeamDao;
import gms.shared.stationdefinition.dao.css.InstrumentDao;
import gms.shared.stationdefinition.dao.css.NetworkDao;
import gms.shared.stationdefinition.dao.css.NetworkStationTimeKey;
import gms.shared.stationdefinition.dao.css.SensorDao;
import gms.shared.stationdefinition.dao.css.SensorKey;
import gms.shared.stationdefinition.dao.css.SiteAndSurroundingDates;
import gms.shared.stationdefinition.dao.css.SiteChanAndSurroundingDates;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteChanKey;
import gms.shared.stationdefinition.dao.css.SiteDao;
import gms.shared.stationdefinition.dao.css.SiteKey;
import gms.shared.stationdefinition.dao.css.StationChannelTimeKey;
import gms.shared.stationdefinition.dao.css.WfTagDao;
import gms.shared.stationdefinition.dao.css.WfTagKey;
import gms.shared.stationdefinition.dao.css.WfdiscDao;
import gms.shared.stationdefinition.dao.css.enums.NetworkType;
import gms.shared.stationdefinition.dao.css.enums.StaType;
import gms.shared.stationdefinition.dao.css.enums.TagName;
import gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.SITE_CHAN_ARGS;
import gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.WFDISC_ARGS;
import gms.shared.utilities.bridge.database.enums.ClipFlag;
import gms.shared.workflow.dao.IntervalDao;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ANOTHER_STA;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ARID_1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ARID_2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ARID_3;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.AUTHOR1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHAN1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHAN2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHAN3;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHAN4;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHANID_1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHANID_2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHANID_3;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHANID_4;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHANID_5;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHANID_6;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHANNEL_TYPE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHAN_DESC;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHAN_PARAM_MAP;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CLIP_FLAG;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.DATA_TYPE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.DEGREES_EAST;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.DEGREES_NORTH;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.DFILE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.DIR;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ELEVATION1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ELEVATION2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.END_TIME;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.INSTID_1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.INSTID_2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.INSTID_3;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.INSTRUMENT_TYPE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.LATITUDE1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.LDATE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.LONGITUDE1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.MODDATE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.NETWORK_DESC_1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.NETWORK_DESC_2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.NETWORK_DESC_3;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.NETWORK_DESC_4;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.NETWORK_ID_10;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.NETWORK_ID_11;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.NETWORK_ID_12;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.NETWORK_ID_15;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.NETWORK_ID_31;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.NETWORK_ID_32;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.NETWORK_ID_33;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.NETWORK_NAME_1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.NETWORK_NAME_2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.OFFDATE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.OFFDATE2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.OFFDATE3;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.OFFDATE4;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.OFFDATE5;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.OFFDATE6;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.OFFDATE_NA;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE3;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE4;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE5;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE6;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE7;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE8;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE9;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.REFERENCE_STATION;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.REFERENCE_STATION_2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.REFERENCE_STATION_3;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.REFERENCE_STATION_4;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.SEG_TYPE_1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.SENSOR_PARAM_MAP;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.SITE_ARGS;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STA1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STA1_PARAM_MAP;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STA2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STA2_PARAM_MAP;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STA3;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STA3_PARAM_MAP;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STA4;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.START_TIME;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STATION_NAME;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STATION_TYPE_1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STATION_TYPE_2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STATION_TYPE_3;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STAWW1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STAWW2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STAXX1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STAXX2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STAYY1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STAYY2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STAYY3;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.UN_STA;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.WFDISC_PARAM_MAP;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.WFID_1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.WFID_2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.WFID_3;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.WFID_4;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.WFID_5;

public class CSSDaoTestFixtures {


  public static final NetworkDao NETWORK_DAO_1;
  public static final NetworkDao NETWORK_DAO_1_NO_OFFDATE;
  public static final NetworkDao NETWORK_DAO_1_1;
  public static final NetworkDao NETWORK_DAO_2;
  public static final NetworkDao NETWORK_DAO_3;
  public static final NetworkDao NETWORK_DAO_4;
  public static final NetworkDao NETWORK_DAO_5;
  public static final NetworkDao NETWORK_DAO_6;
  public static final NetworkDao NETWORK_DAO_7;
  public static final NetworkDao NETWORK_DAO_8;
  public static final NetworkDao NETWORK_DAO_9;
  public static final NetworkDao NETWORK_DAO_10;
  public static final AffiliationDao AFFILIATION_DAO_1;
  public static final AffiliationDao AFFILIATION_DAO_1_1;
  public static final AffiliationDao AFFILIATION_DAO_1_2;
  public static final AffiliationDao AFFILIATION_DAO_2;
  public static final AffiliationDao AFFILIATION_DAO_3;
  public static final AffiliationDao AFFILIATION_DAO_4;
  public static final AffiliationDao AFFILIATION_DAO_5;
  public static final AffiliationDao AFFILIATION_DAO_6;
  public static final AffiliationDao AFFILIATION_DAO_7;
  public static final AffiliationDao NEXT_AFFILIATION_DAO_1;
  public static final AffiliationDao NEXT_AFFILIATION_DAO_1_2;
  public static final AffiliationDao NEXT_AFFILIATION_DAO_2;
  public static final AffiliationDao NEXT_AFFILIATION_DAO_3;
  public static final AffiliationDao AFFILIATION_DAO_SAME_START1;
  public static final SiteDao SITE_DAO_REF_1;
  public static final SiteDao SITE_DAO_REF_11;
  public static final SiteDao SITE_DAO_REF_12;
  public static final SiteDao SITE_DAO_REF_13;
  public static final SiteDao SITE_DAO_REF_14;
  public static final SiteDao SITE_DAO_REF_2;
  public static final SiteDao SITE_DAO_REF_21;
  public static final SiteDao SITE_DAO_REF_22;
  public static final SiteDao SITE_DAO_REF_3;
  public static final SiteDao SITE_DAO_REF_31;
  public static final SiteDao SITE_DAO_REF_32;
  public static final SiteDao SITE_DAO_REF_33;
  public static final SiteDao SITE_DAO_REF_4;
  public static final SiteDao SITE_DAO_REF_41;
  public static final SiteDao SITE_DAO_REF_42;
  public static final SiteDao SITE_DAO_1;
  public static final SiteDao SITE_DAO_1_1;
  public static final SiteDao SITE_DAO_2;
  public static final SiteDao SITE_DAO_3;
  public static final SiteDao SITE_DAO_4;
  public static final SiteDao SITE_DAO_5;
  public static final SiteDao SINGLE_SITE;
  public static final SiteDao SITE_OVERLAPPING_1;
  public static final SiteDao SITE_OVERLAPPING_2;
  public static final SiteDao SITE_ACTIVE_TIME;
  public static final SiteKey SITE_KEY_OVERLAPPING_1;
  public static final SiteKey SITE_KEY_OVERLAPPING_2;
  public static final SiteKey SITE_KEY_ACTIVE_TIME;
  public static final SiteChanDao SITE_CHAN_DAO_REF_11;
  public static final SiteChanDao SITE_CHAN_DAO_REF_13;
  public static final SiteChanDao SITE_CHAN_DAO_REF_14;
  public static final SiteChanDao SITE_CHAN_DAO_REF_21;
  public static final SiteChanDao SITE_CHAN_DAO_REF_22;
  public static final SiteChanDao SITE_CHAN_DAO_REF_31;
  public static final SiteChanDao SITE_CHAN_DAO_REF_32;
  public static final SiteChanDao SITE_CHAN_DAO_REF_33;
  public static final SiteChanDao SITE_CHAN_DAO_REF_41;
  public static final SiteChanDao SITE_CHAN_DAO_REF_42;
  public static final SiteChanDao SITE_CHAN_DAO_1;
  public static final SiteChanDao SITE_CHAN_DAO_NA_ENDTIME;
  public static final SiteChanDao SITE_CHAN_DAO_BDA;
  public static final SiteChanDao SITE_CHAN_DAO_LDA;
  public static final SiteChanDao SITE_CHAN_DAO_1_1;
  public static final SiteChanDao SITE_CHAN_DAO_2;
  public static final SiteChanDao SITE_CHAN_DAO_3;
  public static final SiteChanDao SITE_CHAN_DAO_4;
  public static final SiteChanDao UNRELATED_SITE_CHAN_DAO;
  public static final SiteChanDao SITE_CHAN_STA_1_DAO_TIME_1;
  public static final SiteChanDao SITE_CHAN_STA_1_DAO_TIME_2;
  public static final SiteChanDao SITE_CHAN_STA_1_DAO_TIME_3;
  public static final SiteChanDao SITE_CHAN_STA_1_DAO_TIME_4;
  public static final SiteChanDao SITE_CHAN_ANOTHER_STA;
  public static final SiteChanDao SITE_CHAN_2_LETTER_CHANNEL;
  public static final SiteChanDao SINGLE_SITE_CHAN;
  public static final InstrumentDao INSTRUMENT_DAO_1;
  public static final InstrumentDao INSTRUMENT_DAO_1_1;
  public static final InstrumentDao INSTRUMENT_DAO_2;
  public static final InstrumentDao INSTRUMENT_DAO_3;
  public static final InstrumentDao UNRELATED_INSTRUMENT_DAO;
  public static final SensorDao SENSOR_DAO_REF_11;
  public static final SensorDao SENSOR_DAO_REF_12;
  public static final SensorDao SENSOR_DAO_REF_13;
  public static final SensorDao SENSOR_DAO_REF_14;
  public static final SensorDao SENSOR_DAO_REF_21;
  public static final SensorDao SENSOR_DAO_REF_22;
  public static final SensorDao SENSOR_DAO_REF_31;
  public static final SensorDao SENSOR_DAO_REF_32;
  public static final SensorDao SENSOR_DAO_REF_33;
  public static final SensorDao SENSOR_DAO_REF_41;
  public static final SensorDao SENSOR_DAO_REF_42;
  public static final SensorDao SENSOR_DAO_1;
  public static final SensorDao SENSOR_DAO_1_1;
  public static final SensorDao SENSOR_DAO_1_2;
  public static final SensorDao SENSOR_DAO_1_3;
  public static final SensorDao SENSOR_DAO_2;
  public static final SensorDao SENSOR_DAO_3;
  public static final SensorDao SENSOR_DAO_4;
  public static final SensorDao SENSOR_DAO_5;
  public static final SensorDao SENSOR_DAO_6;
  public static final SensorDao SENSOR_DAO_7;
  public static final SensorDao SENSOR_DAO_8;
  public static final SensorDao UNRELATED_SENSOR_DAO;
  public static final WfdiscDao WFDISC_DAO_1;
  public static final WfdiscDao WFDISC_DAO_END_TIME;
  public static final WfdiscDao WFDISC_DAO_2;
  public static final WfdiscDao WFDISC_DAO_3;
  public static final WfdiscDao WFDISC_DAO_4;
  public static final WfdiscDao WFDISC_DAO_5;
  public static final ArrivalDao ARRIVAL_DAO_1;
  public static final ArrivalDao ARRIVAL_DAO_2;
  public static final ArrivalDao ARRIVAL_DAO_3;
  public static final WfdiscDao WFDISC_TEST_DAO_1;
  public static final WfdiscDao WFDISC_TEST_DAO_2;
  public static final WfdiscDao WFDISC_TEST_DAO_3;
  public static final WfdiscDao WFDISC_TEST_DAO_4;
  public static final WfdiscDao WFDISC_TEST_DAO_5;
  public static final WfdiscDao WFDISC_TEST_DAO_6;
  public static final WfdiscDao WFDISC_TEST_DAO_7;
  public static final WfdiscDao WFDISC_TEST_DAO_8;
  public static final FrequencyAmplitudePhase FREQUENCY_AMPLITUDE_PHASE;
  public static final IntervalDao INTERVAL_DAO_NET_NETS1_DONE;
  public static final IntervalDao INTERVAL_DAO_NET_NETS1_ACTIVE;
  public static final IntervalDao INTERVAL_DAO_ARS_AL1_DONE;
  public static final IntervalDao INTERVAL_DAO_ARS_AL1_ACTIVE;
  public static final IntervalDao INTERVAL_DAO_AUTO_AL1_DONE;
  public static final IntervalDao INTERVAL_DAO_AUTO_AL1_ACTIVE;
  public static final IntervalDao INTERVAL_DAO_ARS_AL2_DONE;
  public static final IntervalDao INTERVAL_DAO_ARS_AL2_ACTIVE;
  public static final IntervalDao INTERVAL_DAO_ARS_AL1_SKIPPED;
  public static final Instant DEFAULT_LOAD_DATE = Instant.parse("2021-02-13T02:48:04.486Z");

  private static final double DEFAULT_DOUBLE = 0.0;
  private static final int DEFAULT_INT = 0;
  private static final String DEFAULT_DESCRIPTION = "A description.";
  private static final String STA = "STA";

  /**
   * Information from SITE_DAO_1, SITE_CHAN_DAO_1, SENSOR_DAO_1, and INSTRUMENT DAO_1 will be used to create Channel_1.
   * Information from SITE_DAO_1, SITE_CHAN_DAO_2, SENSOR_DAO_2, and INSTRUMENT DAO_2 will be used to create Channel_1.
   * Information from SITE_DAO_2, SITE_CHAN_DAO_3, SENSOR_DAO_3, and INSTRUMENT DAO_3 will be used to create Channel_1.
   *
   * Channel_1 and Channel_2 belong to the same ChannelGroup, Channel_3 is a separate ChannelGroup.
   *
   * SITE_DAO_1, SITE_DAO_2, and SITE_DAO_3 along with all the other information will be used to create
   * a station with the channel groups and channels above.
   *
   */

  static {
    // create new networks for each affiliation
    // network ondate 1 - 10
    // affilliation 2
    NETWORK_DAO_1 = createNetworkDao(1, NETWORK_NAME_1, NETWORK_DESC_1, ONDATE, OFFDATE, AUTHOR1,
      LDATE);
    NETWORK_DAO_1_NO_OFFDATE = createNetworkDao(1, NETWORK_NAME_1, NETWORK_DESC_1, ONDATE, null, AUTHOR1,
      LDATE);
    NETWORK_DAO_1_1 = createNetworkDao(2, NETWORK_NAME_1, NETWORK_DESC_1, ONDATE, OFFDATE, AUTHOR1,
      LDATE.plusSeconds(30000));
    NETWORK_DAO_2 = createNetworkDao(3, NETWORK_NAME_2, NETWORK_DESC_2, ONDATE, OFFDATE, "pers2",
      LDATE.plusSeconds(30000));
    NETWORK_DAO_3 = createNetworkDao(NETWORK_ID_10, NETWORK_NAME_1, NETWORK_DESC_1, ONDATE6, OFFDATE6, AUTHOR1, LDATE);
    NETWORK_DAO_4 = createNetworkDao(NETWORK_ID_15, NETWORK_NAME_1, NETWORK_DESC_2, OFFDATE6, null, AUTHOR1, LDATE);
    NETWORK_DAO_5 = createNetworkDao(NETWORK_ID_11, NETWORK_NAME_1, NETWORK_DESC_2, ONDATE7, null, AUTHOR1, LDATE);
    NETWORK_DAO_6 = createNetworkDao(NETWORK_ID_12, NETWORK_NAME_1, NETWORK_DESC_1, ONDATE8, ONDATE3, AUTHOR1, LDATE);
    NETWORK_DAO_7 = createNetworkDao(NETWORK_ID_31, NETWORK_NAME_1, NETWORK_DESC_3, ONDATE3, null, AUTHOR1, LDATE);
    NETWORK_DAO_8 = createNetworkDao(NETWORK_ID_32, NETWORK_NAME_1, NETWORK_DESC_4, ONDATE9, null, AUTHOR1, LDATE);
    NETWORK_DAO_9 = createNetworkDao(NETWORK_ID_33, NETWORK_NAME_1, NETWORK_DESC_2, ONDATE8, null, AUTHOR1, LDATE);
    NETWORK_DAO_10 = createNetworkDao(NETWORK_ID_33, NETWORK_NAME_1, NETWORK_DESC_2, null, null, AUTHOR1, LDATE);

    // create affiliations linked to network daos
    AFFILIATION_DAO_1 = createAffiliationDao(NETWORK_NAME_1, REFERENCE_STATION, ONDATE2);
    AFFILIATION_DAO_1_1 = createAffiliationDao(NETWORK_NAME_1, REFERENCE_STATION, OFFDATE2);
    AFFILIATION_DAO_2 = createAffiliationDao(NETWORK_NAME_1, REFERENCE_STATION_2, ONDATE3);
    AFFILIATION_DAO_1_2 = createAffiliationDao(NETWORK_NAME_1, REFERENCE_STATION, ONDATE3);
    AFFILIATION_DAO_3 = createAffiliationDao(NETWORK_NAME_1, REFERENCE_STATION_2, ONDATE4);
    AFFILIATION_DAO_4 = createAffiliationDao(NETWORK_NAME_2, REFERENCE_STATION_3, ONDATE3);
    AFFILIATION_DAO_5 = createAffiliationDao(NETWORK_NAME_2, REFERENCE_STATION_4, ONDATE4);
    AFFILIATION_DAO_6 = createAffiliationDao(NETWORK_NAME_1, REFERENCE_STATION, ONDATE2);
    AFFILIATION_DAO_7 = createAffiliationDao(NETWORK_NAME_1, REFERENCE_STATION_3, ONDATE5);

    NEXT_AFFILIATION_DAO_1 = createAffiliationDao(NETWORK_NAME_1, REFERENCE_STATION, OFFDATE);
    NEXT_AFFILIATION_DAO_1_2 = createAffiliationDao(NETWORK_NAME_1, REFERENCE_STATION_2, OFFDATE);
    NEXT_AFFILIATION_DAO_2 = createAffiliationDao(NETWORK_NAME_2, REFERENCE_STATION, OFFDATE2);
    NEXT_AFFILIATION_DAO_3 = createAffiliationDao(NETWORK_NAME_1, REFERENCE_STATION_2, OFFDATE2);

    AFFILIATION_DAO_SAME_START1 = createAffiliationDao(NETWORK_NAME_1, REFERENCE_STATION_2, OFFDATE);
    // site daos associated with reference station 1
    SITE_DAO_REF_1 = createSiteDao(REFERENCE_STATION, ONDATE, CssDaoAndCoiParameters.OFFDATE,
      STA1_PARAM_MAP, STATION_TYPE_1, REFERENCE_STATION);
    SITE_DAO_REF_11 = createSiteDao(STA1, ONDATE, CssDaoAndCoiParameters.OFFDATE,
      STA1_PARAM_MAP, STATION_TYPE_1, REFERENCE_STATION);
    SITE_DAO_REF_12 = createSiteDao(STA2, ONDATE, CssDaoAndCoiParameters.OFFDATE,
      STA2_PARAM_MAP, STATION_TYPE_2, REFERENCE_STATION);
    SITE_DAO_REF_13 = createSiteDao(STA3, ONDATE, CssDaoAndCoiParameters.OFFDATE,
      STA3_PARAM_MAP, STATION_TYPE_1, REFERENCE_STATION);
    SITE_DAO_REF_14 = createSiteDao(STA4, ONDATE, CssDaoAndCoiParameters.OFFDATE,
      STA1_PARAM_MAP, STATION_TYPE_2, REFERENCE_STATION);

    // site daos associated with reference station 2
    SITE_DAO_REF_2 = createSiteDao(REFERENCE_STATION_2, ONDATE2, CssDaoAndCoiParameters.OFFDATE,
      STA1_PARAM_MAP, STATION_TYPE_1, REFERENCE_STATION_2);
    SITE_DAO_REF_21 = createSiteDao(STAXX1, ONDATE2, CssDaoAndCoiParameters.OFFDATE,
      STA1_PARAM_MAP, STATION_TYPE_1, REFERENCE_STATION_2);
    SITE_DAO_REF_22 = createSiteDao(STAXX2, ONDATE2, CssDaoAndCoiParameters.OFFDATE,
      STA2_PARAM_MAP, STATION_TYPE_2, REFERENCE_STATION_2);

    // site daos associated with reference station 3
    SITE_DAO_REF_3 = createSiteDao(REFERENCE_STATION_3, ONDATE2, CssDaoAndCoiParameters.OFFDATE,
      STA1_PARAM_MAP, STATION_TYPE_1, REFERENCE_STATION_3);
    SITE_DAO_REF_31 = createSiteDao(STAYY1, ONDATE2, CssDaoAndCoiParameters.OFFDATE,
      STA2_PARAM_MAP, STATION_TYPE_2, REFERENCE_STATION_3);
    SITE_DAO_REF_32 = createSiteDao(STAYY2, ONDATE2, CssDaoAndCoiParameters.OFFDATE,
      STA3_PARAM_MAP, STATION_TYPE_1, REFERENCE_STATION_3);
    SITE_DAO_REF_33 = createSiteDao(STAYY3, ONDATE2, CssDaoAndCoiParameters.OFFDATE,
      STA1_PARAM_MAP, STATION_TYPE_2, REFERENCE_STATION_3);

    // site daos associated with reference station 4
    SITE_DAO_REF_4 = createSiteDao(REFERENCE_STATION_4, ONDATE3, CssDaoAndCoiParameters.OFFDATE,
      STA1_PARAM_MAP, STATION_TYPE_1, REFERENCE_STATION_4);
    SITE_DAO_REF_41 = createSiteDao(STAWW1, ONDATE3, CssDaoAndCoiParameters.OFFDATE,
      STA1_PARAM_MAP, STATION_TYPE_1, REFERENCE_STATION_4);
    SITE_DAO_REF_42 = createSiteDao(STAWW2, ONDATE3, CssDaoAndCoiParameters.OFFDATE,
      STA2_PARAM_MAP, STATION_TYPE_2, REFERENCE_STATION_4);

    // set the sitechan daos for reference 1
    SITE_CHAN_DAO_REF_11 = createSiteChanDao(STA1, CHAN1, ONDATE, OFFDATE, CHAN_PARAM_MAP,
      CHANID_1);
    SITE_CHAN_DAO_REF_13 = createSiteChanDao(STA3, CHAN3, ONDATE, OFFDATE, CHAN_PARAM_MAP,
      CHANID_3);
    SITE_CHAN_DAO_REF_14 = createSiteChanDao(STA4, CHAN4, ONDATE, OFFDATE, CHAN_PARAM_MAP,
      CHANID_4);

    // sitechan daos for reference 2
    SITE_CHAN_DAO_REF_21 = createSiteChanDao(STAXX1, CHAN1, ONDATE2, OFFDATE2, CHAN_PARAM_MAP,
      CHANID_1);
    SITE_CHAN_DAO_REF_22 = createSiteChanDao(STAXX2, CHAN2, ONDATE2, OFFDATE2, CHAN_PARAM_MAP,
      CHANID_2);

    // sitechan daos for reference 3
    SITE_CHAN_DAO_REF_31 = createSiteChanDao(STAYY1, CHAN2, ONDATE2, OFFDATE2, CHAN_PARAM_MAP,
      CHANID_2);
    SITE_CHAN_DAO_REF_32 = createSiteChanDao(STAYY2, CHAN3, ONDATE2, OFFDATE2, CHAN_PARAM_MAP,
      CHANID_3);
    SITE_CHAN_DAO_REF_33 = createSiteChanDao(STAYY3, CHAN4, ONDATE2, OFFDATE2, CHAN_PARAM_MAP,
      CHANID_4);

    // sitechan daos for reference 4
    SITE_CHAN_DAO_REF_41 = createSiteChanDao(STAWW1, CHAN3, ONDATE3, OFFDATE3, CHAN_PARAM_MAP,
      CHANID_3);
    SITE_CHAN_DAO_REF_42 = createSiteChanDao(STAWW2, CHAN4, ONDATE3, OFFDATE3, CHAN_PARAM_MAP,
      CHANID_4);

    INSTRUMENT_DAO_1 = createInstrumentDao(INSTID_1, CssDaoAndCoiParameters.INSTRUMENT_PARAM_MAP,
      LDATE);
    INSTRUMENT_DAO_1_1 = createInstrumentDao(INSTID_1, CssDaoAndCoiParameters.INSTRUMENT_PARAM_MAP,
      LDATE.plusSeconds(30000));
    INSTRUMENT_DAO_2 = createInstrumentDao(INSTID_2, CssDaoAndCoiParameters.INSTRUMENT_PARAM_MAP_2,
      LDATE);
    INSTRUMENT_DAO_3 = createInstrumentDao(INSTID_3, CssDaoAndCoiParameters.INSTRUMENT_PARAM_MAP,
      LDATE);
    UNRELATED_INSTRUMENT_DAO = createInstrumentDao(CssDaoAndCoiParameters.UN_INSTID,
      CssDaoAndCoiParameters.INSTRUMENT_PARAM_MAP, LDATE);

    // sensor daos for for site chans
    SENSOR_DAO_REF_11 = createSensorDao(STA1, CHAN1, CHANID_1, INSTRUMENT_DAO_1, SENSOR_PARAM_MAP, ONDATE,
      OFFDATE);
    SENSOR_DAO_REF_12 = createSensorDao(STA2, CHAN2, CHANID_2, INSTRUMENT_DAO_2, SENSOR_PARAM_MAP, ONDATE,
      OFFDATE);
    SENSOR_DAO_REF_13 = createSensorDao(STA3, CHAN3, CHANID_3, INSTRUMENT_DAO_3, SENSOR_PARAM_MAP, ONDATE,
      OFFDATE);
    SENSOR_DAO_REF_14 = createSensorDao(STA4, CHAN4, CHANID_4, INSTRUMENT_DAO_1, SENSOR_PARAM_MAP, ONDATE,
      OFFDATE);
    SENSOR_DAO_REF_21 = createSensorDao(STAXX1, CHAN1, CHANID_1, INSTRUMENT_DAO_1, SENSOR_PARAM_MAP,
      ONDATE2, OFFDATE2);
    SENSOR_DAO_REF_22 = createSensorDao(STAXX2, CHAN2, CHANID_2, INSTRUMENT_DAO_2, SENSOR_PARAM_MAP,
      ONDATE2, OFFDATE2);
    SENSOR_DAO_REF_31 = createSensorDao(STAYY1, CHAN1, CHANID_1, INSTRUMENT_DAO_1, SENSOR_PARAM_MAP,
      ONDATE2, OFFDATE2);
    SENSOR_DAO_REF_32 = createSensorDao(STAYY2, CHAN2, CHANID_2, INSTRUMENT_DAO_2, SENSOR_PARAM_MAP,
      ONDATE2, OFFDATE2);
    SENSOR_DAO_REF_33 = createSensorDao(STAYY3, CHAN3, CHANID_3, INSTRUMENT_DAO_3, SENSOR_PARAM_MAP,
      ONDATE2, OFFDATE2);
    SENSOR_DAO_REF_41 = createSensorDao(STAWW1, CHAN1, CHANID_1, INSTRUMENT_DAO_1, SENSOR_PARAM_MAP,
      ONDATE3, OFFDATE3);
    SENSOR_DAO_REF_42 = createSensorDao(STAWW2, CHAN2, CHANID_2, INSTRUMENT_DAO_2, SENSOR_PARAM_MAP,
      ONDATE3, OFFDATE3);

    // stations with multiple channel groups
    SITE_DAO_1 = createSiteDao(STA1, ONDATE, CssDaoAndCoiParameters.OFFDATE,
      STA1_PARAM_MAP, STATION_TYPE_1, REFERENCE_STATION);
    SITE_DAO_1_1 = createSiteDao(STA1, ONDATE, CssDaoAndCoiParameters.OFFDATE,
      STA1_PARAM_MAP, STATION_TYPE_1, REFERENCE_STATION_2);
    SITE_DAO_2 = createSiteDao(STA2, ONDATE, CssDaoAndCoiParameters.OFFDATE,
      STA2_PARAM_MAP, STATION_TYPE_2, REFERENCE_STATION);
    SITE_DAO_3 = createSiteDao(REFERENCE_STATION, ONDATE, CssDaoAndCoiParameters.OFFDATE,
      STA3_PARAM_MAP, STATION_TYPE_3, REFERENCE_STATION);
    SITE_DAO_4 = createSiteDao(ANOTHER_STA, ONDATE, CssDaoAndCoiParameters.OFFDATE,
      STA3_PARAM_MAP, STATION_TYPE_1, REFERENCE_STATION);
    SITE_DAO_5 = createSiteDao(ANOTHER_STA, ONDATE.plusSeconds(30000), CssDaoAndCoiParameters.OFFDATE,
      STA3_PARAM_MAP, STATION_TYPE_1, REFERENCE_STATION);
    SINGLE_SITE = createSiteDao(REFERENCE_STATION, ONDATE, OFFDATE,
      STA3_PARAM_MAP, StaType.SINGLE_STATION, REFERENCE_STATION);

    SITE_CHAN_DAO_1 = createSiteChanDao(STA1, CHAN1, ONDATE, OFFDATE, CHAN_PARAM_MAP, CHANID_1);
    SITE_CHAN_DAO_NA_ENDTIME = createSiteChanDao(STA1, CHAN1, ONDATE, OFFDATE_NA, CHAN_PARAM_MAP, CHANID_1);
    SITE_CHAN_DAO_BDA = createSiteChanDao(STA1, "BDA", ONDATE, OFFDATE, CHAN_PARAM_MAP, CHANID_1);
    SITE_CHAN_DAO_LDA = createSiteChanDao(STA1, "LDA", ONDATE, OFFDATE, CHAN_PARAM_MAP, CHANID_1);
    SITE_CHAN_DAO_1_1 = createSiteChanDao(STA1, CHAN1, ONDATE.plusSeconds(30000), OFFDATE,
      CHAN_PARAM_MAP, CHANID_1);
    SITE_CHAN_DAO_2 = createSiteChanDao(STA1, CHAN2, ONDATE, OFFDATE, CHAN_PARAM_MAP, CHANID_2);
    SITE_CHAN_DAO_3 = createSiteChanDao(STA2, CHAN3, ONDATE, OFFDATE, CHAN_PARAM_MAP, CHANID_3);
    SITE_CHAN_DAO_4 = createSiteChanDao(STA1, CHAN1, OFFDATE2, OFFDATE4, CHAN_PARAM_MAP, CHANID_1);
    SINGLE_SITE_CHAN = createSiteChanDao(REFERENCE_STATION, CHAN1, ONDATE, OFFDATE, CHAN_PARAM_MAP, CHANID_1);


    UNRELATED_SITE_CHAN_DAO = createSiteChanDao(UN_STA, CHAN2, OFFDATE, ONDATE, CHAN_PARAM_MAP,
      CHANID_3);

    SITE_CHAN_2_LETTER_CHANNEL = createSiteChanDao(STA1, "bz", ONDATE, OFFDATE, CHAN_PARAM_MAP,
      CHANID_1);

    SITE_CHAN_STA_1_DAO_TIME_1 = createSiteChanDao(STA1, CHAN1, ONDATE.minusSeconds(400000),
      OFFDATE.minusSeconds(80000), CHAN_PARAM_MAP, CHANID_1);
    SITE_CHAN_STA_1_DAO_TIME_2 = createSiteChanDao(STA1, CHAN2, ONDATE.plusSeconds(30000),
      OFFDATE.minusSeconds(300000), CHAN_PARAM_MAP, CHANID_2);
    SITE_CHAN_STA_1_DAO_TIME_3 = createSiteChanDao(STA1, CHAN3, OFFDATE.minusSeconds(400000),
      OFFDATE.plusSeconds(200000), CHAN_PARAM_MAP, CHANID_4);
    SITE_CHAN_STA_1_DAO_TIME_4 = createSiteChanDao(STA1, CHAN4, ONDATE.plusSeconds(30000),
      OFFDATE.minusSeconds(80000), CHAN_PARAM_MAP, CHANID_6);

    SITE_CHAN_ANOTHER_STA = createSiteChanDao(ANOTHER_STA, CHAN3, ONDATE, OFFDATE, CHAN_PARAM_MAP,
      CHANID_5);

    SENSOR_DAO_1 = createSensorDao(STA1, CHAN1, CHANID_1, INSTRUMENT_DAO_1, SENSOR_PARAM_MAP, START_TIME,
      END_TIME);
    SENSOR_DAO_1_1 = createSensorDao(STA1, CHAN1, CHANID_1, INSTRUMENT_DAO_2, SENSOR_PARAM_MAP,
      START_TIME.plusSeconds(30000), END_TIME);
    SENSOR_DAO_1_2 = createSensorDao(STA1, CHAN1, CHANID_1, INSTRUMENT_DAO_1_1, SENSOR_PARAM_MAP,
      START_TIME, START_TIME.plusSeconds(30000));
    SENSOR_DAO_1_3 = createSensorDao(STA1, CHAN1, CHANID_1, INSTRUMENT_DAO_2, SENSOR_PARAM_MAP, ONDATE,
      OFFDATE);
    SENSOR_DAO_2 = createSensorDao(STA1, CHAN2, CHANID_2, INSTRUMENT_DAO_2, SENSOR_PARAM_MAP, ONDATE,
      END_TIME);
    SENSOR_DAO_4 = createSensorDao(STA1, CHAN3, CHANID_4, INSTRUMENT_DAO_1, SENSOR_PARAM_MAP, START_TIME,
      END_TIME);
    SENSOR_DAO_6 = createSensorDao(STA1, CHAN4, CHANID_6, INSTRUMENT_DAO_3, SENSOR_PARAM_MAP, START_TIME,
      END_TIME);
    SENSOR_DAO_3 = createSensorDao(STA2, CHAN3, CHANID_3, INSTRUMENT_DAO_3, SENSOR_PARAM_MAP, START_TIME,
      END_TIME);
    SENSOR_DAO_5 = createSensorDao(ANOTHER_STA, CHAN1, CHANID_5, INSTRUMENT_DAO_3, SENSOR_PARAM_MAP,
      START_TIME, END_TIME);
    SENSOR_DAO_7 = createSensorDao(STA1, CHAN1, CHANID_1, INSTRUMENT_DAO_1, SENSOR_PARAM_MAP, ONDATE2,
      ONDATE4);
    SENSOR_DAO_8 = createSensorDao(STA1, CHAN1, CHANID_1, INSTRUMENT_DAO_2, SENSOR_PARAM_MAP, ONDATE3,
      OFFDATE);


    UNRELATED_SENSOR_DAO = createSensorDao(STA2, CHAN3, CssDaoAndCoiParameters.UN_CHANID, INSTRUMENT_DAO_3,
      SENSOR_PARAM_MAP, START_TIME, END_TIME);

    WFDISC_DAO_1 = createWfdiscDao(WFID_1, CHANID_1, WFDISC_PARAM_MAP);
    WFDISC_DAO_2 = createWfdiscDao(WFID_2, CHANID_2, WFDISC_PARAM_MAP);
    WFDISC_DAO_3 = createWfdiscDao(WFID_3, CHANID_3, WFDISC_PARAM_MAP);
    WFDISC_DAO_4 = createWfdiscDao(WFID_4, CHANID_3, WFDISC_PARAM_MAP);
    WFDISC_DAO_5 = createWfdiscDao(WFID_5, CHANID_3, WFDISC_PARAM_MAP);
    WFDISC_DAO_END_TIME = createWfdiscDao(WFID_1, CHANID_1, ONDATE.plusSeconds(10), WFDISC_PARAM_MAP);

    WFDISC_TEST_DAO_1 = createWfdiscDao(STA1, CHAN1, ONDATE, END_TIME, WFID_1, CHANID_1, WFDISC_PARAM_MAP);
    WFDISC_TEST_DAO_2 = createWfdiscDao(STA1, CHAN2, ONDATE, END_TIME, WFID_2, CHANID_2, WFDISC_PARAM_MAP);
    WFDISC_TEST_DAO_3 = createWfdiscDao(STA2, CHAN3, ONDATE, END_TIME, WFID_3, CHANID_3, WFDISC_PARAM_MAP);
    WFDISC_TEST_DAO_4 = createWfdiscDao(STA1, CHAN1, ONDATE2, ONDATE3, WFID_3, CHANID_3, WFDISC_PARAM_MAP);
    WFDISC_TEST_DAO_5 = createWfdiscDao(STA1, CHAN1, ONDATE, ONDATE2, WFID_1, CHANID_1, WFDISC_PARAM_MAP);
    WFDISC_TEST_DAO_6 = createWfdiscDao(STA1, CHAN1, ONDATE2, ONDATE3, WFID_1, CHANID_1, WFDISC_PARAM_MAP);
    WFDISC_TEST_DAO_7 = createWfdiscDao(STA1, CHAN1, ONDATE3, OFFDATE4, WFID_1, CHANID_1, WFDISC_PARAM_MAP);
    WFDISC_TEST_DAO_8 = createWfdiscDao(STA2, CHAN2, ONDATE, END_TIME, WFID_2, CHANID_2, WFDISC_PARAM_MAP);

    ARRIVAL_DAO_1 = createArrivalDao(STA1, CHAN1, ONDATE, ARID_1);
    ARRIVAL_DAO_2 = createArrivalDao(STA1, CHAN2, ONDATE, ARID_2);
    ARRIVAL_DAO_3 = createArrivalDao(STA2, CHAN3, ONDATE, ARID_3);


    FREQUENCY_AMPLITUDE_PHASE = createFAP();

    INTERVAL_DAO_NET_NETS1_DONE = new IntervalDao.Builder()
      .intervalIdentifier(1002533784416L)
      .type("NET")
      .name("NETS1")
      .time(1619720400.00000)
      .endTime(1619722800.00000)
      .state("network-done")
      .author("-")
      .percentAvailable(1.00000)
      .processStartDate(IntervalDao.convertTableDate("29-APR-2021 19:05:14"))
      .processEndDate(IntervalDao.convertTableDate("29-APR-2021 19:05:55"))
      .lastModificationDate(IntervalDao.convertTableDate("29-APR-2021 19:05:55"))
      .loadDate(IntervalDao.convertTableDate("29-APR-2021 19:05:01"))
      .build();

    INTERVAL_DAO_NET_NETS1_ACTIVE = null;

    INTERVAL_DAO_AUTO_AL1_DONE = new IntervalDao.Builder()
      .intervalIdentifier(1002521971180L)
      .type("AUTO")
      .name("AL1")
      .time(1618884000.00000)
      .endTime(1618887600.00000)
      .state("done")
      .author("analyst")
      .percentAvailable(1.00000)
      .processStartDate(IntervalDao.convertTableDate("20-APR-2021 04:58:41"))
      .processEndDate(IntervalDao.convertTableDate("20-APR-2021 05:08:44"))
      .lastModificationDate(IntervalDao.convertTableDate("20-APR-2021 05:08:44"))
      .loadDate(IntervalDao.convertTableDate("20-APR-2021 04:57:04"))
      .build();

    INTERVAL_DAO_AUTO_AL1_ACTIVE = null;

    INTERVAL_DAO_ARS_AL1_DONE = new IntervalDao.Builder()
      .intervalIdentifier(1002526012274L)
      .type("ARS")
      .name("AL1")
      .time(1619172000.00000)
      .endTime(1619175600.00000)
      .state("done")
      .author("analyst5")
      .percentAvailable(.00000)
      .processStartDate(IntervalDao.convertTableDate("23-APR-2021 12:57:49"))
      .processEndDate(IntervalDao.convertTableDate("23-APR-2021 13:49:00"))
      .lastModificationDate(IntervalDao.convertTableDate("23-APR-2021 13:49:00"))
      .loadDate(IntervalDao.convertTableDate("23-APR-2021 10:05:04"))
      .build();

    INTERVAL_DAO_ARS_AL1_ACTIVE = new IntervalDao.Builder()
      .intervalIdentifier(1002526052456L)
      .type("ARS")
      .name("AL1")
      .time(1619175600.00000)
      .endTime(1619179200.00000)
      .state("active")
      .author("analyst6")
      .percentAvailable(.00000)
      .processStartDate(IntervalDao.convertTableDate("23-APR-2021 13:48:04"))
      .processEndDate(IntervalDao.convertTableDate("23-APR-2021 12:36:28"))
      .lastModificationDate(IntervalDao.convertTableDate("23-APR-2021 13:48:19"))
      .loadDate(IntervalDao.convertTableDate("23-APR-2021 11:05:04"))
      .build();

    INTERVAL_DAO_ARS_AL2_DONE = null;
    INTERVAL_DAO_ARS_AL2_ACTIVE = null;

    String date = "23-APR-2021 15:05:04";

    INTERVAL_DAO_ARS_AL1_SKIPPED = new IntervalDao.Builder()
      .intervalIdentifier(1002526262084L)
      .type("ARS")
      .name("AL1")
      .time(1619190000.00000)
      .endTime(1619193600.00000)
      .state("skipped")
      .author("-")
      .percentAvailable(.00000)
      .processStartDate(IntervalDao.convertTableDate(date))
      .processEndDate(IntervalDao.convertTableDate(date))
      .lastModificationDate(IntervalDao.convertTableDate(date))
      .loadDate(IntervalDao.convertTableDate(date))
      .build();

    SITE_KEY_OVERLAPPING_1 = new SiteKey(STA1, ONDATE);
    SITE_OVERLAPPING_1 = new SiteDao(
      SITE_KEY_OVERLAPPING_1,
      ONDATE2,
      LATITUDE1,
      LONGITUDE1,
      ELEVATION1,
      STATION_NAME,
      StaType.SINGLE_STATION,
      REFERENCE_STATION,
      DEGREES_NORTH,
      DEGREES_EAST,
      LDATE);

    SITE_KEY_OVERLAPPING_2 = new SiteKey(STA1, ONDATE2);
    SITE_OVERLAPPING_2 = new SiteDao(
      SITE_KEY_OVERLAPPING_2,
      OFFDATE,
      LATITUDE1,
      LONGITUDE1,
      ELEVATION2,
      STATION_NAME,
      StaType.SINGLE_STATION,
      REFERENCE_STATION,
      DEGREES_NORTH,
      DEGREES_EAST,
      LDATE);

    SITE_KEY_ACTIVE_TIME = new SiteKey(STA1, ONDATE5);
    SITE_ACTIVE_TIME = new SiteDao(
      SITE_KEY_ACTIVE_TIME,
      OFFDATE5,
      LATITUDE1,
      LONGITUDE1,
      ELEVATION2,
      STATION_NAME,
      StaType.SINGLE_STATION,
      REFERENCE_STATION,
      DEGREES_NORTH,
      DEGREES_EAST,
      LDATE);
  }

  private CSSDaoTestFixtures() {
  }

  public static SiteDao createSiteDao(String siteName, Instant onDate, Instant offDate,
    Map<SITE_ARGS, Double> argMap,
    StaType staType, String referenceStation) {

    SiteDao siteDao = new SiteDao();
    SiteKey siteKey = new SiteKey();

    siteKey.setStationCode(siteName);
    siteKey.setOnDate(onDate);

    siteDao.setId(siteKey);
    siteDao.setOffDate(offDate);
    siteDao.setLatitude(argMap.get(CssDaoAndCoiParameters.SITE_ARGS.LATITUDE));
    siteDao.setLongitude(argMap.get(CssDaoAndCoiParameters.SITE_ARGS.LONGITUDE));
    siteDao.setElevation(argMap.get(CssDaoAndCoiParameters.SITE_ARGS.ELEVATION));
    siteDao.setStationName(CssDaoAndCoiParameters.STATION_NAME);
    siteDao.setStaType(staType);
    siteDao.setReferenceStation(referenceStation);
    siteDao.setDegreesNorth(argMap.get(SITE_ARGS.DNORTH));
    siteDao.setDegreesEast(argMap.get(SITE_ARGS.DEAST));
    siteDao.setLoadDate(LDATE);

    return siteDao;

  }

  public static SiteChanDao createSiteChanDao(String siteName, String channelName, Instant onDate,
    Instant offDate, Map<SITE_CHAN_ARGS, Double> argMap, int chanId) {
    SiteChanDao siteChanDao = new SiteChanDao();
    SiteChanKey siteChanKey = new SiteChanKey();
    siteChanKey.setStationCode(siteName);
    siteChanKey.setChannelCode(channelName);
    siteChanKey.setOnDate(onDate);
    siteChanDao.setId(siteChanKey);
    siteChanDao.setChannelId(chanId);
    siteChanDao.setOffDate(offDate);
    siteChanDao.setChannelType(CHANNEL_TYPE);
    siteChanDao.setEmplacementDepth(argMap.get(SITE_CHAN_ARGS.EMPLACEMENT));
    siteChanDao.setHorizontalAngle(argMap.get(SITE_CHAN_ARGS.HORIZONTAL));
    siteChanDao.setVerticalAngle(argMap.get(SITE_CHAN_ARGS.VERTICAL));
    siteChanDao.setChannelDescription(CHAN_DESC);
    siteChanDao.setLoadDate(LDATE);

    return siteChanDao;

  }


  public static SensorDao createSensorDao(String siteName, String channelName,
    int chanid, InstrumentDao instDao, Map<CssDaoAndCoiParameters.SENSOR_ARGS, Double> argMap,
    Instant onDate, Instant offDate) {

    SensorDao sensorDao = new SensorDao();
    SensorKey sensorKey = new SensorKey();
    sensorKey.setStation(siteName);
    sensorKey.setChannel(channelName);
    sensorKey.setTime(onDate);
    sensorKey.setEndTime(offDate);
    sensorDao.setSensorKey(sensorKey);
    sensorDao.setInstrument(instDao);
    sensorDao.setChannelId(chanid);
    sensorDao.setjDate(ONDATE);
    sensorDao.setCalibrationRatio(argMap.get(CssDaoAndCoiParameters.SENSOR_ARGS.CALIBRATION));
    sensorDao.setCalibrationPeriod(argMap.get(CssDaoAndCoiParameters.SENSOR_ARGS.CALIBPER));
    sensorDao.settShift(argMap.get(CssDaoAndCoiParameters.SENSOR_ARGS.SHIFT));
    sensorDao.setSnapshotIndicator("y");
    sensorDao.setLoadDate(LDATE);

    return sensorDao;

  }

  protected static InstrumentDao createInstrumentDao(long inid,
    Map<CssDaoAndCoiParameters.INSTRUMENT_ARGS, Double> argMap, Instant ldate) {

    InstrumentDao instrumentDao = new InstrumentDao();
    instrumentDao.setInstrumentId(inid);
    instrumentDao.setInstrumentName(CssDaoAndCoiParameters.INS_NAME);
    instrumentDao.setBand(CssDaoAndCoiParameters.BAND_TYPE);
    instrumentDao.setDigital(CssDaoAndCoiParameters.DIGITAL_TYPE);
    instrumentDao.setSampleRate(argMap.get(CssDaoAndCoiParameters.INSTRUMENT_ARGS.SAMPLERATE));
    instrumentDao.setNominalCalibrationFactor(
      argMap.get(CssDaoAndCoiParameters.INSTRUMENT_ARGS.CALIBFACTOR));
    instrumentDao
      .setNominalCalibrationPeriod(argMap.get(CssDaoAndCoiParameters.INSTRUMENT_ARGS.CALIBRATIO));
    instrumentDao.setDirectory(DIR);
    instrumentDao.setDataFile(DFILE);
    instrumentDao.setResponseType(CssDaoAndCoiParameters.RESPONSE_TYPE);
    instrumentDao.setLoadDate(ldate);

    return instrumentDao;

  }

  private static AffiliationDao createAffiliationDao(String networkName, String station,
    Instant onDate) {
    AffiliationDao affiliationDao = new AffiliationDao();
    final NetworkStationTimeKey networkStationTimeKey = new NetworkStationTimeKey();
    networkStationTimeKey.setNetwork(networkName);
    networkStationTimeKey.setStation(station);
    networkStationTimeKey.setTime(onDate);
    affiliationDao.setNetworkStationTimeKey(networkStationTimeKey);
    affiliationDao.setEndTime(END_TIME);
    affiliationDao.setLoadDate(LDATE);

    return affiliationDao;
  }

  private static NetworkDao createNetworkDao(long networkId, String networkName, String description,
    Instant onDate, Instant offDate, String author, Instant ldDate) {
    NetworkDao networkDao = new NetworkDao();

    networkDao.setNetworkId(networkId);
    networkDao.setNet(networkName);
    networkDao.setNetworkName(networkName);
    networkDao.setDescription(description);
    networkDao.setNetworkType(NetworkType.WORLD_WIDE);
    networkDao.setOnDate(onDate);
    networkDao.setOffDate(offDate);
    networkDao.setAuthor(author);
    networkDao.setModDate(MODDATE);
    networkDao.setLdDate(ldDate);

    return networkDao;
  }

  public static WfdiscDao createWfdiscDao(long wfid, long chanid,
    Map<WFDISC_ARGS, Number> argMap) {
    return createWfdiscDao(STA1, CHAN1, ONDATE, END_TIME, wfid, chanid, argMap);
  }

  public static WfdiscDao createWfdiscDao(long wfid, long chanid, Instant endTime,
    Map<WFDISC_ARGS, Number> argMap) {
    return createWfdiscDao(STA1, CHAN1, ONDATE, endTime, wfid, chanid, argMap);
  }

  public static WfdiscDao createWfdiscDao(String sta, String chan, Instant onDate, Instant endTime, long wfid,
    long chanid,
    Map<WFDISC_ARGS, Number> argMap) {
    WfdiscDao wfdiscDao = new WfdiscDao();
    wfdiscDao.setId(wfid);
    wfdiscDao.setStationCode(sta);
    wfdiscDao.setChannelCode(chan);
    wfdiscDao.setTime(onDate);
    wfdiscDao.setChannelId(chanid);
    wfdiscDao.setjDate(ONDATE);
    wfdiscDao.setEndTime(endTime);
    wfdiscDao.setNsamp(argMap.get(WFDISC_ARGS.NSAMP).intValue());
    wfdiscDao.setSampRate(argMap.get(WFDISC_ARGS.SAMPLERATE).doubleValue());
    wfdiscDao.setCalib(argMap.get(WFDISC_ARGS.CALIB).doubleValue());
    wfdiscDao.setCalper(argMap.get(WFDISC_ARGS.CALIBPER).doubleValue());
    wfdiscDao.setInsType(INSTRUMENT_TYPE);
    wfdiscDao.setSegType(SEG_TYPE_1);
    wfdiscDao.setDataType(DATA_TYPE);
    wfdiscDao.setClip(CLIP_FLAG);
    wfdiscDao.setDir(DIR);
    wfdiscDao.setDfile(DFILE);
    wfdiscDao.setFoff(argMap.get(WFDISC_ARGS.FOFF).longValue());
    wfdiscDao.setCommid(argMap.get(WFDISC_ARGS.COMMID).longValue());
    wfdiscDao.setLoadDate(LDATE);

    return wfdiscDao;
  }

  public static void addArrivalWftagAndBeamDaos(List<WfdiscDao> wfdiscDaos, List<ArrivalDao> arrivalDaos,
    List<WfTagDao> wfTagDaos, List<WfTagDao> originalWfTagDaos, List<BeamDao> beamDaos) {

    int arid = 0;

    for (WfdiscDao wfdiscDao : wfdiscDaos) {
      arrivalDaos.add(createArrivalDao(wfdiscDao.getStationCode(), wfdiscDao.getChannelCode(), wfdiscDao.getTime(), arid));
      wfTagDaos.add(createWfTagDao(arid, wfdiscDao.getId(), TagName.ARID));
      originalWfTagDaos.add(createWfTagDao(arid, wfdiscDao.getId(), TagName.ARID));
      beamDaos.add(createBeamDao(wfdiscDao.getId()));
      arid++;
    }

  }

  public static BeamDao createBeamDao(long wfid) {
    var beamDao = new BeamDao();
    beamDao.setWfId(wfid);
    beamDao.setFilterId(DEFAULT_INT);
    beamDao.setAzimuth(DEFAULT_DOUBLE);
    beamDao.setSlowness(DEFAULT_DOUBLE);
    beamDao.setDescription(DEFAULT_DESCRIPTION);
    beamDao.setLoadDate(DEFAULT_LOAD_DATE);

    return beamDao;
  }

  public static WfTagDao createWfTagDao(long tagid, long wfid, TagName tagName) {
    WfTagKey wfTagKey = new WfTagKey();
    wfTagKey.setTagName(tagName);
    wfTagKey.setId(tagid);
    wfTagKey.setWfId(wfid);

    WfTagDao wfTagDao = new WfTagDao();
    wfTagDao.setWfTagKey(wfTagKey);
    wfTagDao.setLoadDate(DEFAULT_LOAD_DATE);

    return wfTagDao;
  }

  private static ArrivalDao createArrivalDao(String sta, String chan, Instant onDate, long arid) {

    ArrivalDao arrivalDao = new ArrivalDao();
    StationChannelTimeKey arrivalKey = new StationChannelTimeKey();
    arrivalKey.setStationCode(sta);
    arrivalKey.setChannelCode(chan);
    arrivalKey.setTime(onDate);
    arrivalDao.setArrivalKey(arrivalKey);
    arrivalDao.setId(arid);
    arrivalDao.setjDate(onDate);
    arrivalDao.setSingleStationOriginId(-1);
    arrivalDao.setChannelId(-1);
    arrivalDao.setPhase("N");
    arrivalDao.setCommid(-1);
    arrivalDao.setTimeUncertainty(1.598);
    arrivalDao.setAzimuth(192.78);
    arrivalDao.setAzimuthUncertainty(6.69);
    arrivalDao.setSlowness(11.63);
    arrivalDao.setSlownessUncertainty(1.36);
    arrivalDao.setEmergenceAngle(-1);
    arrivalDao.setRectilinearity(-1);
    arrivalDao.setAmplitude(1.57);
    arrivalDao.setPeriod(1);
    arrivalDao.setLogAmpliterPeriod(-999);
    arrivalDao.setSnr(4.83);
    arrivalDao.setSignalOnsetQuality("4");
    arrivalDao.setLoadDate(DEFAULT_LOAD_DATE);

    return arrivalDao;
  }

  public static AmplitudeDao createAmplitudeDao(long ampid, ArrivalDao arrivalDao) {

    var builder = new AmplitudeDao.Builder();
    builder.withId(ampid)
      .withArrivalId(arrivalDao.getId())
      .withPredictedArrivalId(arrivalDao.getId())
      .withChannelCode(arrivalDao.getArrivalKey().getChannelCode())
      .withAmplitude(10)
      .withPeriod(4)
      .withSnr(23)
      .withAmplitudeTime(arrivalDao.getArrivalKey().getTime())
      .withTime(arrivalDao.getArrivalKey().getTime())
      .withDuration(Duration.ZERO)
      .withSampleIntervalWidth(2)
      .withAmplitudeType("Cool")
      .withUnits(AmplitudeUnits.LOG_NM)
      .withClip(ClipFlag.CLIPPED)
      .withInArrival(true)
      .withAuthor("Me")
      .withLoadDate(arrivalDao.getLoadDate());

    return new AmplitudeDao(builder);
  }

  private static FrequencyAmplitudePhase createFAP() {
    double[] frequencies = {2.0E-4, 2.04668E-4};
    double[] amplitudes = {2.775677E-5, 2.974503E-5};
    double[] ampStdDev = {0.0, 0.0};
    double[] phase = {-1.786542, -1.791653};
    double[] phaseStdDev = {0.0, 0.0};
    return FrequencyAmplitudePhase.builder()
      .setData(FrequencyAmplitudePhase.Data.builder()
        .setAmplitudeResponseUnits(Units.COUNTS_PER_NANOMETER)
        .setAmplitudeResponse(amplitudes)
        .setAmplitudeResponseStdDev(ampStdDev)
        .setPhaseResponse(phase)
        .setPhaseResponseStdDev(phaseStdDev)
        .setPhaseResponseUnits(Units.DEGREES)
        .setFrequencies(frequencies)
        .build())
      .setId(UUID.nameUUIDFromBytes("PLACEHOLDER".getBytes()))
      .build();

  }

  public static List<SiteDao> getTestSiteDaos() {
    return List.of(SITE_DAO_1, SITE_DAO_2, SITE_DAO_3);
  }

  public static List<SiteAndSurroundingDates> getTestSiteAndSurroundingDates() {
    return List.of(new SiteAndSurroundingDates(new SiteDao(SITE_DAO_1),
        SITE_DAO_1.getId().getOnDate(), null),
      new SiteAndSurroundingDates(new SiteDao(SITE_DAO_2), null, SITE_DAO_2.getOffDate()),
      new SiteAndSurroundingDates(new SiteDao(SITE_DAO_3), null, null));
  }

  public static List<SiteChanAndSurroundingDates> getTestSiteChanAndSurroundingDates() {
    return List.of(new SiteChanAndSurroundingDates(new SiteChanDao(SITE_CHAN_DAO_1),
        SITE_CHAN_DAO_1.getId().getOnDate(), null),
      new SiteChanAndSurroundingDates(new SiteChanDao(SITE_CHAN_DAO_2), null, SITE_CHAN_DAO_2.getOffDate()),
      new SiteChanAndSurroundingDates(new SiteChanDao(SITE_CHAN_DAO_3), null, null));
  }

  public static List<SiteChanDao> getTestSiteChanDaos() {
    return List.of(SITE_CHAN_DAO_1, SITE_CHAN_DAO_2, SITE_CHAN_DAO_3);
  }

  public static List<Channel> getTestChannels() {
    return getTestSiteChanDaos().stream()
      .map(dao -> Channel.builder()
        .setName(STA + "." + dao.getId().getStationCode() + "." + dao.getId().getChannelCode())
        .setEffectiveAt(ONDATE)
        .build())
      .collect(Collectors.toList());
  }

  public static List<SensorDao> getTestSensorDaos() {
    return List.of(SENSOR_DAO_1, SENSOR_DAO_2, SENSOR_DAO_3);
  }

  public static List<InstrumentDao> getTestInstrumentDaos() {
    return List.of(INSTRUMENT_DAO_1, INSTRUMENT_DAO_2, INSTRUMENT_DAO_3);
  }

  public static List<SensorDao> getTestSensorDaosWithSiteChanOnDate() {
    return Stream.of(SENSOR_DAO_1, SENSOR_DAO_2, SENSOR_DAO_3)
      .map((sensorDao -> {
        sensorDao.getSensorKey().setTime(ONDATE);
        return sensorDao;
      }))
      .collect(Collectors.toList());
  }


  public static List<WfdiscDao> getTestWfdiscDaos() {
    return List.of(WFDISC_TEST_DAO_1, WFDISC_TEST_DAO_2, WFDISC_TEST_DAO_3);
  }

  public static List<NetworkDao> getNetworkDaosForBridged() {
    return List.of(NETWORK_DAO_1, NETWORK_DAO_2);
  }

  public static List<AffiliationDao> getAffiliationDaosForBridged() {
    return List.of(AFFILIATION_DAO_1, AFFILIATION_DAO_2, AFFILIATION_DAO_4, AFFILIATION_DAO_5);
  }

  public static List<AffiliationDao> getNextAffiliationDaosForBridged() {
    return List.of(NEXT_AFFILIATION_DAO_1, NEXT_AFFILIATION_DAO_2);
  }

  public static List<SiteDao> getTestSiteDaosForBridged() {
    List<SiteDao> siteDaosRef1 = List
      .of(SITE_DAO_REF_1, SITE_DAO_REF_11, SITE_DAO_REF_12, SITE_DAO_REF_13, SITE_DAO_REF_14);
    List<SiteDao> siteDaosRef2 = List.of(SITE_DAO_REF_2, SITE_DAO_REF_21, SITE_DAO_REF_22);
    List<SiteDao> siteDaosRef3 = List
      .of(SITE_DAO_REF_3, SITE_DAO_REF_31, SITE_DAO_REF_32, SITE_DAO_REF_33);
    List<SiteDao> siteDaosRef4 = List.of(SITE_DAO_REF_4, SITE_DAO_REF_41, SITE_DAO_REF_42);

    return Stream.of(siteDaosRef1, siteDaosRef2, siteDaosRef3, siteDaosRef4)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());
  }

  public static SiteDao getMainSiteForFirstChannelGroup() {
    return SITE_DAO_3;
  }

  public static SiteDao getDeepCopySiteDao(SiteDao siteDao){

    var newSiteDao = new SiteDao();
    var siteKey = new SiteKey(siteDao.getId().getStationCode(), siteDao.getId().getOnDate());
    newSiteDao.setId(siteKey);
    newSiteDao.setOffDate(siteDao.getOffDate());
    newSiteDao.setLatitude(siteDao.getLatitude());
    newSiteDao.setLongitude(siteDao.getLongitude());
    newSiteDao.setElevation(siteDao.getElevation());
    newSiteDao.setStationName(siteDao.getStationName());
    newSiteDao.setStaType(siteDao.getStaType());
    newSiteDao.setReferenceStation(siteDao.getReferenceStation());
    newSiteDao.setDegreesNorth(siteDao.getDegreesNorth());
    newSiteDao.setDegreesEast(siteDao.getDegreesEast());
    newSiteDao.setLoadDate(siteDao.getLoadDate());

    return newSiteDao;

  }

}
