package gms.shared.stationdefinition.converter.util.assemblers;

import com.google.common.base.Functions;
import com.google.common.collect.Range;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelProcessingMetadataType;
import gms.shared.stationdefinition.coi.channel.Response;
import gms.shared.stationdefinition.coi.utils.comparator.ChannelComparator;
import gms.shared.stationdefinition.converter.DaoCalibrationConverter;
import gms.shared.stationdefinition.converter.DaoChannelConverter;
import gms.shared.stationdefinition.converter.DaoResponseConverter;
import gms.shared.stationdefinition.converter.FileFrequencyAmplitudePhaseConverter;
import gms.shared.stationdefinition.converter.interfaces.ChannelConverter;
import gms.shared.stationdefinition.converter.interfaces.ResponseConverter;
import gms.shared.stationdefinition.converter.interfaces.ResponseConverterTransform;
import gms.shared.stationdefinition.dao.css.BeamDao;
import gms.shared.stationdefinition.dao.css.InstrumentDao;
import gms.shared.stationdefinition.dao.css.SensorDao;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteChanKey;
import gms.shared.stationdefinition.dao.css.SiteDao;
import gms.shared.stationdefinition.dao.css.SiteKey;
import gms.shared.stationdefinition.dao.css.WfdiscDao;
import gms.shared.stationdefinition.dao.css.enums.StaType;
import gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures;
import gms.shared.stationdefinition.testfixtures.UtilsTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.INSTRUMENT_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.INSTRUMENT_DAO_2;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SENSOR_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SENSOR_DAO_1_3;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SENSOR_DAO_7;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SENSOR_DAO_8;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_CHAN_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_CHAN_DAO_4;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.WFDISC_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.WFDISC_TEST_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.WFDISC_TEST_DAO_4;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.WFDISC_TEST_DAO_7;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.createSensorDao;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.createWfdiscDao;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHAN1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHANID_1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.END_TIME;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.OFFDATE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE3;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE4;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.SENSOR_PARAM_MAP;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STA1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.WFDISC_PARAM_MAP;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.WFID_1;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelAssemblerTest {

  private static final BeamDao beamDao = CSSDaoTestFixtures.createBeamDao(WFDISC_TEST_DAO_1.getId());
  private static final SiteDao siteDao = SITE_DAO_1;
  private static final SiteChanDao siteChanDao = SITE_CHAN_DAO_1;
  private static final SensorDao sensorDao = SENSOR_DAO_1;
  private static final WfdiscDao wfdiscDao = WFDISC_DAO_1;

  private final DaoResponseConverter responseConverter = new DaoResponseConverter();
  private final DaoCalibrationConverter calibrationConverter = new DaoCalibrationConverter();
  private final FileFrequencyAmplitudePhaseConverter fapConverter = new FileFrequencyAmplitudePhaseConverter();
  private final DaoChannelConverter channelConverter = new DaoChannelConverter(calibrationConverter, fapConverter);

  @Mock
  private ChannelConverter mockChannelConverter;

  @Mock
  private ResponseConverter mockResponseConverter;

  private ChannelAssembler channelAssembler;

  private ChannelAssembler mockChannelAssembler;

  @BeforeEach
  void setup() {
    channelAssembler = new ChannelAssembler(channelConverter, responseConverter);
    mockChannelAssembler = new ChannelAssembler(mockChannelConverter, mockResponseConverter);
  }

  @Test
  void testGetListOfCssDaosPerChannelGroup() {
    List<SiteDao> siteDaos = CSSDaoTestFixtures.getTestSiteDaos();
    List<SiteChanDao> siteChanDaos = CSSDaoTestFixtures.getTestSiteChanDaos();
    List<SensorDao> sensorDaos = CSSDaoTestFixtures.getTestSensorDaos();
    List<WfdiscDao> wfdiscDaos = CSSDaoTestFixtures.getTestWfdiscDaos();

    List<Channel> compareChannel = UtilsTestFixtures.getListOfChannelsWithResponse();
    compareChannel = compareChannel.stream()
      .map(channel -> {
        if (channel.getData().isPresent() && channel.getData().orElseThrow().getResponse().isPresent()) {
          return channel.toBuilder().setData(channel.getData().get().toBuilder().setResponse(
            Response.createVersionReference(channel.getData().get().getResponse().get().getId(), channel.getEffectiveAt().get())
          ).build()).build();
        }
        return channel;
      }).collect(Collectors.toList());

    List<Channel> channels = channelAssembler.buildAllForTime(ONDATE, siteDaos, siteChanDaos, sensorDaos, wfdiscDaos, getResponses(compareChannel));
    assertEquals(compareChannel, channels);
  }

  @Test
  void testGetListOfCssDaosPerChannelGroupSensorAtStartime() {
    List<SiteDao> siteDaos = CSSDaoTestFixtures.getTestSiteDaos();
    List<SiteChanDao> siteChanDaos = CSSDaoTestFixtures.getTestSiteChanDaos();
    List<SensorDao> sensorDaos = CSSDaoTestFixtures.getTestSensorDaosWithSiteChanOnDate();
    List<InstrumentDao> instrumentDaos = CSSDaoTestFixtures.getTestInstrumentDaos();
    List<WfdiscDao> wfdiscDaos = CSSDaoTestFixtures.getTestWfdiscDaos();

    List<Channel> compareChannel = UtilsTestFixtures.getListOfChannelsWithResponse();
    compareChannel = compareChannel.stream()
      .map(channel -> {
        if (channel.getData().isPresent() && channel.getData().orElseThrow().getResponse().isPresent()) {
          return channel.toBuilder().setData(channel.getData().get().toBuilder().setResponse(
            Response.createVersionReference(channel.getData().get().getResponse().get().getId(), channel.getEffectiveAt().get())
          ).build()).build();
        }
        return channel;
      }).collect(Collectors.toList());
    List<Channel> channels = channelAssembler.buildAllForTime(ONDATE, siteDaos, siteChanDaos, sensorDaos, wfdiscDaos, getResponses(compareChannel));

    assertEquals(compareChannel, channels);
  }

  @Test
  void testResponseAddedAndRemoved() {
    List<SiteDao> siteDaos = List.of(SITE_DAO_1);//ONDATE //OFFDATE
    List<SiteChanDao> siteChanDaos = List.of(SITE_CHAN_DAO_1);//ONDATE //OFFDATE
    List<SensorDao> sensorDaos = List.of(SENSOR_DAO_1);//START_TIME //END_TIME Instant.MAX
    List<WfdiscDao> wfdiscDaos = List.of(WFDISC_TEST_DAO_4);//ONDATE2 //ONDATE3
    String id = WFDISC_TEST_DAO_4.getStationCode() + WFDISC_TEST_DAO_4.getChannelCode();
    Response response = Response.builder()
      .setId(UUID.nameUUIDFromBytes(id.getBytes()))
      .setEffectiveAt(ONDATE2)
      .setData(Response.Data.builder().setEffectiveUntil(ONDATE3).build()).build();

    List<Channel> channels = channelAssembler.buildAllForTime(ONDATE4, siteDaos, siteChanDaos, sensorDaos, wfdiscDaos, List.of(response));

    channels.sort(Channel::compareTo);
    assertEquals(1, channels.size());
    assertTrue(!channels.get(0).getResponse().isPresent());
    assertEquals( AssemblerUtils.getImmediatelyAfterInstant(wfdiscDaos.get(0).getEndTime()),
      channels.get(0).getEffectiveAt().get());
  }

  @Test
  void testNewVersionDueToResponseRemovedAndThenSampleRateChanged() {
    var sampleRateChanged =
      createSensorDao(STA1, CHAN1, CHANID_1, INSTRUMENT_DAO_2, SENSOR_PARAM_MAP, ONDATE4, OFFDATE);

    List<SiteDao> siteDaos = List.of(SITE_DAO_1);//ONDATE //OFFDATE
    List<SiteChanDao> siteChanDaos = List.of(SITE_CHAN_DAO_1);//ONDATE //OFFDATE
    List<SensorDao> sensorDaos = List.of(SENSOR_DAO_1, sampleRateChanged);//START_TIME //END_TIME Instant.MAX
    List<WfdiscDao> wfdiscDaos = List.of(WFDISC_TEST_DAO_4);//ONDATE2 //ONDATE3
    String id = WFDISC_TEST_DAO_4.getStationCode() + WFDISC_TEST_DAO_4.getChannelCode();
    var response = Response.builder()
      .setId(UUID.nameUUIDFromBytes(id.getBytes()))
      .setEffectiveAt(WFDISC_TEST_DAO_4.getTime())
      .setData(Response.Data.builder().setEffectiveUntil(WFDISC_TEST_DAO_4.getEndTime()).build()).build();

    List<Channel> channels = channelAssembler.buildAllForTime(AssemblerUtils.effectiveAtNoonOffset.apply(ONDATE4), siteDaos, siteChanDaos, sensorDaos, wfdiscDaos, List.of(response));

    assertEquals(1, channels.size());
    assertTrue(!channels.get(0).getResponse().isPresent());
    assertEquals(ONDATE4, channels.get(0).getEffectiveAt().get());
  }



  @Test
  void testWfdiscEndsBeforeSiteChan() {
    List<SiteDao> siteDaos = List.of(SITE_DAO_1);//ONDATE //OFFDATE
    List<SiteChanDao> siteChanDaos = List.of(SITE_CHAN_DAO_1);//ONDATE //OFFDATE
    List<SensorDao> sensorDaos = List.of();
    List<WfdiscDao> wfdiscDaos = List.of(WFDISC_TEST_DAO_4);//ONDATE2 //ONDATE3

    List<Channel> channels = channelAssembler.buildAllForTime(ONDATE3, siteDaos, siteChanDaos, sensorDaos, wfdiscDaos, List.of());

    channels.sort(Channel::compareTo);
    List<Channel> compareChannel = UtilsTestFixtures.getSingleChannelFromWfDisc();

    assertEquals(compareChannel, channels);
  }

  @Test
  void testSensorEndsBeforeSiteChan() {
    List<SiteDao> siteDaos = List.of(SITE_DAO_1);//ONDATE //OFFDATE
    List<SiteChanDao> siteChanDaos = List.of(SITE_CHAN_DAO_1);//ONDATE //OFFDATE
    List<SensorDao> sensorDaos = List.of(SENSOR_DAO_7);//ONDATE2 //ONDATE4
    List<WfdiscDao> wfdiscDaos = List.of();

    List<Channel> channels = channelAssembler.buildAllForTime(ONDATE3, siteDaos, siteChanDaos, sensorDaos, wfdiscDaos, List.of());

    channels.sort(Channel::compareTo);
    List<Channel> compareChannel = UtilsTestFixtures.getSingleChannelFromSensor();


    assertEquals(compareChannel, channels);
  }

  @Test
  void testGetListOfCssDaosPerChannelGroupMissingSensorDao() {
    List<SiteDao> siteDaos = CSSDaoTestFixtures.getTestSiteDaos();
    List<SiteChanDao> siteChanDaos = CSSDaoTestFixtures.getTestSiteChanDaos();
    List<SensorDao> sensorDaos = UtilsTestFixtures.getIncompleteSensorDaoList();
    List<WfdiscDao> wfdiscDaos = List.of();

    List<Channel> channels = channelAssembler.buildAllForTime(AssemblerUtils.effectiveAtNoonOffset.apply(ONDATE),
      siteDaos, siteChanDaos, sensorDaos, wfdiscDaos, List.of());

    List<Channel> compareChannel = UtilsTestFixtures.getListOfChannelsForIncompleteDaos();

    assertEquals(compareChannel, channels);
  }

  @Test
  void testGetListOfCssDaosPerChannelGroupMissingSiteChanDao() {
    List<SiteDao> siteDaos = CSSDaoTestFixtures.getTestSiteDaos();
    List<SiteChanDao> siteChanDaos = UtilsTestFixtures.getIncompleteSiteChanDaoList();
    List<SensorDao> sensorDaos = CSSDaoTestFixtures.getTestSensorDaos();
    List<WfdiscDao> wfdiscDaos = List.of();

    List<Channel> channels = channelAssembler.buildAllForTime(AssemblerUtils.effectiveAtNoonOffset.apply(ONDATE), siteDaos,
      siteChanDaos, sensorDaos, wfdiscDaos, List.of());

    List<Channel> compareChannel = UtilsTestFixtures.getListOfChannelsForIncompleteDaos();
    assertEquals(compareChannel, channels);
  }

  @Test
  void testGetListOfCssDaosPerChannelGroupMissingSiteDao() {
    List<SiteDao> siteDaos = UtilsTestFixtures.getIncompleteSiteDao();
    List<SiteChanDao> siteChanDaos = CSSDaoTestFixtures.getTestSiteChanDaos();
    List<SensorDao> sensorDaos = CSSDaoTestFixtures.getTestSensorDaos();
    List<WfdiscDao> wfdiscDaos = List.of();

    List<Channel> channels = channelAssembler.buildAllForTime(
      AssemblerUtils.effectiveAtNoonOffset.apply(ONDATE), siteDaos, siteChanDaos, sensorDaos,
      wfdiscDaos, List.of());

    List<Channel> compareChannel = UtilsTestFixtures.getListOfChannelsForIncompleteDaos();
    assertEquals(compareChannel, channels);
  }

  @Test
  void testChannelCreatedFromWfdisc() {
    List<SiteDao> siteDaos = CSSDaoTestFixtures.getTestSiteDaos();
    List<SiteChanDao> siteChanDaos = CSSDaoTestFixtures.getTestSiteChanDaos();
    List<SensorDao> sensorDaos = List.of();
    List<WfdiscDao> wfdiscDaos = CSSDaoTestFixtures.getTestWfdiscDaos();

    List<Channel> channels = channelAssembler.buildAllForTime(AssemblerUtils.effectiveAtNoonOffset.apply(ONDATE),
      siteDaos, siteChanDaos, sensorDaos, wfdiscDaos, List.of());

    List<Channel> compareChannel = UtilsTestFixtures.getListOfChannelsForDaos().stream()
      .sorted(Comparator.comparing(Functions.compose(Optional::get, Channel::getEffectiveAt)))
      .collect(Collectors.toList());

    assertEquals(compareChannel, channels);
  }

  @Test
  void testWfdiscAddedAfterChannelCreated() {
    List<SiteDao> siteDaos = List.of(SITE_DAO_1);
    List<SiteChanDao> siteChanDaos = List.of(SITE_CHAN_DAO_1);
    List<SensorDao> sensorDaos = List.of();
    List<WfdiscDao> wfdiscDaos = List.of(WFDISC_TEST_DAO_4);

    List<Channel> channels = channelAssembler.buildAllForTimeRange(SITE_DAO_1.getId().getOnDate(), Instant.MAX, siteDaos, siteChanDaos, sensorDaos,
      wfdiscDaos, List.of());

    assertEquals(1, channels.size(), "Channel version should be created when a wfdisc is added after the channel");
    assertEquals(WFDISC_TEST_DAO_4.getTime(), channels.get(0).getEffectiveAt().get());
  }

  @Test
  void testWfdiscAddedAfterChannelCreatedAndInstrumentSampleRateChanged() {
    List<SiteDao> siteDaos = List.of(SITE_DAO_1);
    List<SiteChanDao> siteChanDaos = List.of(SITE_CHAN_DAO_1);
    List<SensorDao> sensorDaos = List.of(SENSOR_DAO_1_3);
    List<WfdiscDao> wfdiscDaos = List.of(WFDISC_TEST_DAO_7);
    String id = WFDISC_TEST_DAO_7.getStationCode() + WFDISC_TEST_DAO_7.getChannelCode();
    Response response = Response.builder()
      .setId(UUID.nameUUIDFromBytes(id.getBytes()))
      .setEffectiveAt(ONDATE3)
      .setData(Response.Data.builder().setEffectiveUntil(ONDATE4).build()).build();

    List<Channel> channels = channelAssembler.buildAllForTimeRange(SITE_DAO_1.getId().getOnDate(), Instant.MAX, siteDaos, siteChanDaos, sensorDaos,
      wfdiscDaos, List.of(response));

    assertEquals(2, channels.size(), "Channel version should be created when a sensor is added after the channel");
    assertEquals(AssemblerUtils.effectiveAtStartOfDayOffset.apply(SENSOR_DAO_1_3.getSensorKey().getTime()), channels.get(0).getEffectiveAt().get());
    assertEquals(WFDISC_TEST_DAO_7.getTime().minusMillis(1), channels.get(0).getEffectiveUntil().get());
    assertEquals(WFDISC_TEST_DAO_7.getTime(), channels.get(1).getEffectiveAt().get());
  }

  @Test
  void testSensorAddedAfterChannelCreatedTimeRange() {
    List<SiteDao> siteDaos = List.of(SITE_DAO_1);
    List<SiteChanDao> siteChanDaos = List.of(SITE_CHAN_DAO_1);
    List<SensorDao> sensorDaos = List.of(SENSOR_DAO_7, SENSOR_DAO_8);
    List<WfdiscDao> wfdiscDaos = List.of();

    List<Channel> channels = channelAssembler.buildAllForTimeRange(SITE_DAO_1.getId().getOnDate(), Instant.MAX, siteDaos, siteChanDaos, sensorDaos,
      wfdiscDaos, List.of());

    assertEquals(2, channels.size(), "Channel version should be created when a sensor is added after the channel");
    assertEquals(SENSOR_DAO_7.getSensorKey().getTime(), channels.get(0).getEffectiveAt().get());
  }

  @Test
  void testSensorAddedAfterChannelCreated() {
    List<SiteDao> siteDaos = List.of(SITE_DAO_1);
    List<SiteChanDao> siteChanDaos = List.of(SITE_CHAN_DAO_1);
    List<SensorDao> sensorDaos = List.of(SENSOR_DAO_7);
    List<WfdiscDao> wfdiscDaos = List.of();

    List<Channel> channels = channelAssembler.buildAllForTime(ONDATE4,
      siteDaos, siteChanDaos, sensorDaos, wfdiscDaos, List.of());

    assertEquals(1, channels.size(), "Channel version should be created when a sensor is added after the channel");
    assertEquals(SENSOR_DAO_7.getSensorKey().getTime(), channels.get(0).getEffectiveAt().get());
  }

  @Test
  void testInstrumentSampleRateChangedDueToWfdisc() {
    List<SiteDao> siteDaos = List.of(SITE_DAO_1);
    List<SiteChanDao> siteChanDaos = List.of(SITE_CHAN_DAO_1);
    List<SensorDao> sensorDaos = List.of(SENSOR_DAO_1_3);
    List<WfdiscDao> wfdiscDaos = List.of(WFDISC_TEST_DAO_4);//ONDATE, END_TIME

    List<Channel> channels = channelAssembler.buildAllForTimeRange(SITE_DAO_1.getId().getOnDate(), Instant.MAX, siteDaos, siteChanDaos, sensorDaos,
      wfdiscDaos, List.of());

    assertEquals(1, channels.size(), "Channel version should be created when a instrument sample rate is changed");
    assertEquals(SENSOR_DAO_1_3.getSensorKey().getTime(), channels.get(0).getEffectiveAt().get());
  }

  @Test
  void testInstrumentSampleRateChangedDueToSensor() {
    List<SiteDao> siteDaos = List.of(SITE_DAO_1);
    List<SiteChanDao> siteChanDaos = List.of(SITE_CHAN_DAO_1);
    List<SensorDao> sensorDaos = List.of(SENSOR_DAO_8); //ONDATE3 = Instant.parse("2000-08-13T07:00:00Z");. OFFDATE
    List<WfdiscDao> wfdiscDaos = List.of(WFDISC_TEST_DAO_1);//ONDATE, END_TIME

    List<Channel> channels = channelAssembler.buildAllForTimeRange(SITE_DAO_1.getId().getOnDate(), Instant.MAX, siteDaos, siteChanDaos, sensorDaos,
      wfdiscDaos, List.of());

    assertEquals(2, channels.size(), "Channel version should be created when a instrument sample rate is changed");
    assertEquals(WFDISC_TEST_DAO_1.getTime(), channels.get(0).getEffectiveAt().get());
  }

  @Test
  void testNoValidSite() {
    List<SiteDao> siteDaos = List.of(SITE_DAO_1);//ONDATE - OFFDATE
    List<SiteChanDao> siteChanDaos = List.of(SITE_CHAN_DAO_4); //OFFDATE2, OFFDATE4
    List<SensorDao> sensorDaos = List.of(SENSOR_DAO_1); //START_TIME = Instant.parse("1970-02-13T02:48:04.486Z"); - Instant.MAX;
    List<WfdiscDao> wfdiscDaos = List.of();

    List<Channel> channels = channelAssembler.buildAllForTime(SITE_CHAN_DAO_4.getId().getOnDate(), siteDaos, siteChanDaos, sensorDaos,
      wfdiscDaos, List.of());

    assertEquals(0, channels.size(), "Site should not be valid for given SiteChan");
  }

  @Test
  void buildAllForTimeRange_wfdiscAddedThenSensor() {
    List<SiteDao> siteDaos = List.of(SITE_DAO_1);//ONDATE - OFFDATE
    List<SiteChanDao> siteChanDaos = List.of(SITE_CHAN_DAO_1); //ONDATE, OFFDATE
    List<SensorDao> sensorDaos = List.of(
      createSensorDao(STA1, CHAN1, CHANID_1, INSTRUMENT_DAO_1, SENSOR_PARAM_MAP, ONDATE4, OFFDATE));
    WfdiscDao wfdiscDao = createWfdiscDao(STA1, CHAN1, ONDATE2, END_TIME, WFID_1, CHANID_1, WFDISC_PARAM_MAP);
    List<WfdiscDao> wfdiscDaos = List.of(wfdiscDao);

    String id = wfdiscDao.getStationCode() + wfdiscDao.getChannelCode();
    Response response = Response.builder()
      .setId(UUID.nameUUIDFromBytes(id.getBytes()))
      .setEffectiveAt(ONDATE4)
      .setData(Response.Data.builder().setEffectiveUntil(OFFDATE).build()).build();

    List<Channel> channels = channelAssembler.buildAllForTimeRange(ONDATE, OFFDATE, siteDaos, siteChanDaos, sensorDaos,
      wfdiscDaos, List.of(response));

    assertEquals(2, channels.size(), "Channel Should be built when WfDisc is detected");
    assertEquals(ONDATE2, channels.get(0).getEffectiveAt().get());
    assertEquals(response.getEffectiveAt().get().minusMillis(1), channels.get(0).getEffectiveUntil().get());
    assertEquals(response.getEffectiveAt().get(), channels.get(1).getEffectiveAt().get());
    assertEquals(OFFDATE, channels.get(1).getEffectiveUntil().get());
  }

  @ParameterizedTest
  @MethodSource("getRawChannelNullArguments")
  void testBuildRawChannelNull(SiteDao site,
    WfdiscDao wfdisc,
    SiteChanDao siteChan,
    Optional<SensorDao> sensorDao,
    Instant channelEffectiveTime,
    Instant channelEndTime) {

    assertThrows(NullPointerException.class,
      () -> channelAssembler.buildRawChannel(site,
        wfdisc,
        siteChan,
        sensorDao,
        channelEffectiveTime,
        channelEndTime));
  }

  static Stream<Arguments> getRawChannelNullArguments() {
    return Stream.of(
      arguments(null,
        mock(WfdiscDao.class),
        mock(SiteChanDao.class),
        Optional.of(SENSOR_DAO_1),
        Instant.EPOCH,
        Instant.MAX),
      arguments(mock(SiteDao.class),
        null,
        mock(SiteChanDao.class),
        Optional.of(SENSOR_DAO_1),
        Instant.EPOCH,
        Instant.MAX),
      arguments(mock(SiteDao.class),
        mock(WfdiscDao.class),
        null,
        Optional.of(SENSOR_DAO_1),
        Instant.EPOCH,
        Instant.MAX),
      arguments(mock(SiteDao.class),
        mock(WfdiscDao.class),
        mock(SiteChanDao.class),
        null,
        Instant.EPOCH,
        Instant.MAX),
      arguments(mock(SiteDao.class),
        mock(WfdiscDao.class),
        mock(SiteChanDao.class),
        Optional.of(SENSOR_DAO_1),
        null,
        Instant.MAX),
      arguments(mock(SiteDao.class),
        mock(WfdiscDao.class),
        mock(SiteChanDao.class),
        Optional.of(SENSOR_DAO_1),
        Instant.EPOCH,
        null)
    );
  }

  @ParameterizedTest
  @MethodSource("getAssemblerArguments")
  void testBuildFromAssociatedRecordValidation(Map<ChannelProcessingMetadataType, Object> processingMetadataMap,
    Optional<BeamDao> beam,
    SiteDao site,
    WfdiscDao wfdisc,
    SiteChanDao siteChan,
    Optional<SensorDao> sensorDao,
    Instant channelEffectiveTime,
    Instant channelEndTime) {

    assertThrows(NullPointerException.class,
      () -> channelAssembler.buildFromAssociatedRecord(processingMetadataMap,
        beam,
        site,
        wfdisc,
        siteChan,
        sensorDao,
        channelEffectiveTime,
        channelEndTime));
  }

  static Stream<Arguments> getAssemblerArguments() {
    return Stream.of(
      arguments(null,
        Optional.of(beamDao),
        mock(SiteDao.class),
        mock(WfdiscDao.class),
        mock(SiteChanDao.class),
        Optional.of(SENSOR_DAO_1),
        Instant.EPOCH,
        Instant.MAX),
      arguments(mock(Map.class),
        null,
        mock(SiteDao.class),
        mock(WfdiscDao.class),
        mock(SiteChanDao.class),
        Optional.of(SENSOR_DAO_1),
        Instant.EPOCH,
        Instant.MAX),
      arguments(mock(Map.class),
        Optional.of(beamDao),
        null,
        mock(WfdiscDao.class),
        mock(SiteChanDao.class),
        Optional.of(SENSOR_DAO_1),
        Instant.EPOCH,
        Instant.MAX),
      arguments(mock(Map.class),
        Optional.of(beamDao),
        mock(SiteDao.class),
        null,
        mock(SiteChanDao.class),
        Optional.of(SENSOR_DAO_1),
        Instant.EPOCH,
        Instant.MAX),
      arguments(mock(Map.class),
        Optional.of(beamDao),
        mock(SiteDao.class),
        mock(WfdiscDao.class),
        null,
        Optional.of(SENSOR_DAO_1),
        Instant.EPOCH,
        Instant.MAX),
      arguments(mock(Map.class),
        Optional.of(beamDao),
        mock(SiteDao.class),
        mock(WfdiscDao.class),
        mock(SiteChanDao.class),
        null,
        Instant.EPOCH,
        Instant.MAX),
      arguments(mock(Map.class),
        Optional.of(beamDao),
        mock(SiteDao.class),
        mock(WfdiscDao.class),
        mock(SiteChanDao.class),
        Optional.of(SENSOR_DAO_1),
        null,
        Instant.MAX),
      arguments(mock(Map.class),
        Optional.of(beamDao),
        mock(SiteDao.class),
        mock(WfdiscDao.class),
        mock(SiteChanDao.class),
        Optional.of(SENSOR_DAO_1),
        Instant.EPOCH,
        null)
    );
  }

  @ParameterizedTest
  @MethodSource("getRawChannelArguments")
  void testBuildRawChannel(Consumer<ChannelConverter> channelSetup,
    SiteDao siteDao,
    WfdiscDao wfdiscDao,
    SiteChanDao siteChanDao,
    Optional<SensorDao> possibleSensor,
    Instant channelEffectiveTime,
    Instant channelEndTime,
    Channel expected,
    Consumer<ChannelConverter> channelVerification) {

    channelSetup.accept(mockChannelConverter);

    Channel actual = mockChannelAssembler.buildRawChannel(siteDao,
      wfdiscDao,
      siteChanDao,
      possibleSensor,
      channelEffectiveTime,
      channelEndTime);
    assertEquals(expected, actual);

    channelVerification.accept(mockChannelConverter);
    verifyNoMoreInteractions(mockChannelConverter, mockResponseConverter);
  }

  static Stream<Arguments> getRawChannelArguments() {
    Consumer<ChannelConverter> rawSetup = channelConverter -> doReturn(CHANNEL).when(channelConverter)
      .convert(any(SiteChanDao.class),
        any(SiteDao.class),
        any(SensorDao.class),
        any(InstrumentDao.class),
        any(WfdiscDao.class),
        any(Range.class),
        any(ResponseConverterTransform.class));

    Consumer<ChannelConverter> rawVerification = channelConverter -> verify(channelConverter)
      .convert(any(SiteChanDao.class),
        any(SiteDao.class),
        any(SensorDao.class),
        any(InstrumentDao.class),
        any(WfdiscDao.class),
        any(Range.class),
        any(ResponseConverterTransform.class));

    return Stream.of(
      arguments(rawSetup,
        siteDao,
        wfdiscDao,
        siteChanDao,
        Optional.of(sensorDao),
        Instant.EPOCH,
        Instant.MAX,
        CHANNEL,
        rawVerification));
  }

  @ParameterizedTest
  @MethodSource("getBuildFromAssociatedRecordArguments")
  void testBuildFromAssociatedRecord(Consumer<ChannelConverter> channelSetup,
    Map<ChannelProcessingMetadataType, Object> processingMetadataMap,
    Optional<BeamDao> beamDao,
    SiteDao siteDao,
    WfdiscDao wfdiscDao,
    SiteChanDao siteChanDao,
    Optional<SensorDao> possibleSensor,
    Instant channelEffectiveTime,
    Instant channelEndTime,
    Channel expected,
    Consumer<ChannelConverter> channelVerification) {

    channelSetup.accept(mockChannelConverter);

    Channel actual = mockChannelAssembler.buildFromAssociatedRecord(processingMetadataMap,
      beamDao,
      siteDao,
      wfdiscDao,
      siteChanDao,
      possibleSensor,
      channelEffectiveTime,
      channelEndTime);
    assertEquals(expected, actual);

    channelVerification.accept(mockChannelConverter);
    verifyNoMoreInteractions(mockChannelConverter, mockResponseConverter);
  }

  static Stream<Arguments> getBuildFromAssociatedRecordArguments() {
    Consumer<ChannelConverter> rawSetup = channelConverter -> doReturn(CHANNEL).when(channelConverter)
      .convert(any(SiteChanDao.class),
        any(SiteDao.class),
        any(SensorDao.class),
        any(InstrumentDao.class),
        any(WfdiscDao.class),
        any(Range.class),
        any(ResponseConverterTransform.class));

    Consumer<ChannelConverter> rawVerification = channelConverter -> verify(channelConverter)
      .convert(any(SiteChanDao.class),
        any(SiteDao.class),
        any(SensorDao.class),
        any(InstrumentDao.class),
        any(WfdiscDao.class),
        any(Range.class),
        any(ResponseConverterTransform.class));

    SiteDao arraySite = new SiteDao();
    arraySite.setReferenceStation("refSta");
    SiteKey siteKey = new SiteKey();
    siteKey.setStationCode("refSta");
    siteKey.setOnDate(Instant.EPOCH);
    arraySite.setId(siteKey);
    arraySite.setStaType(StaType.ARRAY_STATION);

    SiteChanDao arraySiteChan = new SiteChanDao();
    SiteChanKey siteChanKey = new SiteChanKey();
    siteChanKey.setStationCode("refSta");
    siteChanKey.setChannelCode("bhz");
    siteChanKey.setOnDate(Instant.EPOCH);
    arraySiteChan.setId(siteChanKey);

    Consumer<ChannelConverter> beamSetup = channelConverter -> when(channelConverter
      .convertToBeamDerived(eq(arraySite),
        eq(arraySiteChan),
        eq(WFDISC_DAO_1),
        eq(Instant.EPOCH),
        eq(Instant.MAX),
        eq(Optional.of(beamDao)),
        anyMap()))
      .thenReturn(CHANNEL);

    Consumer<ChannelConverter> beamVerification = channelConverter -> verify(channelConverter)
      .convertToBeamDerived(eq(arraySite),
        eq(arraySiteChan),
        eq(WFDISC_DAO_1),
        eq(Instant.EPOCH),
        eq(Instant.MAX),
        eq(Optional.of(beamDao)),
        anyMap());

    return Stream.of(
      arguments(rawSetup,
        Map.of(),
        Optional.empty(),
        siteDao,
        wfdiscDao,
        siteChanDao,
        Optional.of(sensorDao),
        Instant.EPOCH,
        Instant.MAX,
        CHANNEL,
        rawVerification),
      arguments(beamSetup,
        Map.of(),
        Optional.of(beamDao),
        arraySite,
        wfdiscDao,
        arraySiteChan,
        Optional.empty(),
        Instant.EPOCH,
        Instant.MAX,
        CHANNEL,
        beamVerification));
  }

  private List<Channel> updateEffectiveUntil(List<Channel> channels, UnaryOperator<Instant> func) {
    return channels.stream()
      .map(chan -> chan.toBuilder()
        .setData(chan.getData().get().toBuilder().setEffectiveUntil(
            Optional.of(func.apply(chan.getData().get().getEffectiveUntil().get())))
          .build())
        .build())
      .distinct()
      .sorted(new ChannelComparator())
      .collect(Collectors.toList());
  }

  private List<Channel> updateEffectiveAt(List<Channel> channels, UnaryOperator<Instant> func) {
    return channels.stream()
      .map(chan -> chan.toBuilder().setEffectiveAt(func.apply(chan.getEffectiveAt().get()))
        .build())
      .sorted()
      .collect(Collectors.toList());
  }

  private List<Response> getResponses(List<Channel> compareChannel){
    return compareChannel.stream()
      .map(Channel::getResponse)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toList());
  }
}
