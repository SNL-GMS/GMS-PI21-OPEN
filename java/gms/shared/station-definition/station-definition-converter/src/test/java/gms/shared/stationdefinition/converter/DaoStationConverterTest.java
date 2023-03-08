package gms.shared.stationdefinition.converter;


import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelGroup;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.converter.util.assemblers.AssemblerData;
import gms.shared.stationdefinition.dao.css.InstrumentDao;
import gms.shared.stationdefinition.dao.css.SensorDao;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteDao;
import gms.shared.stationdefinition.dao.css.WfdiscDao;
import gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SINGLE_SITE;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SINGLE_SITE_CHAN;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_CHAN_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_CHAN_DAO_2;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_CHAN_DAO_3;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_DAO_2;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_DAO_3;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL_GROUP;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL_GROUP_STA01;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL_STA01_STA01_BHE;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.STA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class DaoStationConverterTest {

  static final List<SiteDao> SITE_DAOS = List.of(SITE_DAO_1,
    SITE_DAO_2, SITE_DAO_3);
  static final List<SiteChanDao> SITE_CHAN_DAOS = List.of(SITE_CHAN_DAO_1,
    SITE_CHAN_DAO_2, SITE_CHAN_DAO_3);
  static final List<SensorDao> SENSOR_DAOS = List.of(CSSDaoTestFixtures.SENSOR_DAO_1,
    CSSDaoTestFixtures.SENSOR_DAO_2, CSSDaoTestFixtures.SENSOR_DAO_3);
  static final List<InstrumentDao> INSTRUMENT_DAOS = List.of(CSSDaoTestFixtures.INSTRUMENT_DAO_1,
    CSSDaoTestFixtures.INSTRUMENT_DAO_2, CSSDaoTestFixtures.INSTRUMENT_DAO_3);
  static final List<WfdiscDao> WFDISC_DAOS = List.of(CSSDaoTestFixtures.WFDISC_DAO_1,
    CSSDaoTestFixtures.WFDISC_DAO_2, CSSDaoTestFixtures.WFDISC_DAO_3);
  private static final BiFunction<SiteDao, SiteChanDao, Channel> channelBiFunction = (siteDao, siteChanDao) -> CHANNEL;
  private static final Function<AssemblerData, ChannelGroup> channelGroupFunction = data -> CHANNEL_GROUP;
  private final DaoResponseConverter responseConverter = new DaoResponseConverter();
  private final DaoCalibrationConverter calibrationConverter = new DaoCalibrationConverter();
  private final FileFrequencyAmplitudePhaseConverter fapConverter = new FileFrequencyAmplitudePhaseConverter();
  private final DaoChannelConverter channelConverter = new DaoChannelConverter(calibrationConverter, fapConverter);
  private final DaoStationConverter stationConverter = new DaoStationConverter();

  private static Stream<Arguments> convertValidationParameterSource() {
    return Stream.of(
      arguments(NullPointerException.class, null, SITE_DAO_1.getOffDate(), List.of(SITE_DAO_1), List.of(SITE_CHAN_DAO_1),
        List.of(CHANNEL_GROUP_STA01), List.of(CHANNEL_STA01_STA01_BHE)),
      arguments(NullPointerException.class, SITE_DAO_1.getId().getOnDate(), null, List.of(SITE_DAO_1), List.of(SITE_CHAN_DAO_1),
        List.of(CHANNEL_GROUP_STA01), List.of(CHANNEL_STA01_STA01_BHE)),
      arguments(NullPointerException.class, SITE_DAO_1.getId().getOnDate(), SITE_DAO_1.getOffDate(), null, List.of(SITE_CHAN_DAO_1),
        List.of(CHANNEL_GROUP_STA01), List.of(CHANNEL_STA01_STA01_BHE)),
      arguments(NullPointerException.class, SITE_DAO_1.getId().getOnDate(), SITE_DAO_1.getOffDate(), List.of(SITE_DAO_1), null,
        List.of(CHANNEL_GROUP_STA01), List.of(CHANNEL_STA01_STA01_BHE)),
      arguments(NullPointerException.class, SITE_DAO_1.getId().getOnDate(), SITE_DAO_1.getOffDate(), List.of(SITE_DAO_1), List.of(SITE_CHAN_DAO_1),
        null, List.of(CHANNEL_STA01_STA01_BHE)),
      arguments(NullPointerException.class, SITE_DAO_1.getId().getOnDate(), SITE_DAO_1.getOffDate(), List.of(SITE_DAO_1), List.of(SITE_CHAN_DAO_1),
        List.of(CHANNEL_GROUP_STA01), null),
      arguments(IllegalStateException.class, SITE_DAO_1.getId().getOnDate(), SITE_DAO_1.getOffDate(), List.of(SITE_DAO_1), List.of(),
        List.of(CHANNEL_GROUP_STA01), List.of(CHANNEL_STA01_STA01_BHE)),
      arguments(IllegalStateException.class, SITE_DAO_1.getId().getOnDate(), SITE_DAO_1.getOffDate(), List.of(), List.of(SITE_CHAN_DAO_1),
        List.of(CHANNEL_GROUP_STA01), List.of(CHANNEL_STA01_STA01_BHE)),
      arguments(IllegalStateException.class, SITE_DAO_1.getId().getOnDate(), SITE_DAO_1.getOffDate(), List.of(SITE_DAO_1), List.of(SITE_CHAN_DAO_1),
        List.of(), List.of(CHANNEL_STA01_STA01_BHE)),
      arguments(IllegalStateException.class, SITE_DAO_1.getId().getOnDate(), SITE_DAO_1.getOffDate(), List.of(SITE_DAO_1), List.of(SITE_CHAN_DAO_1),
        List.of(CHANNEL_GROUP_STA01), List.of())
    );
  }

  @Test
  void testConvertSingleStation() {
    List<SiteDao> siteList = List.of(SINGLE_SITE);
    List<SiteChanDao> siteChanList = List.of(SINGLE_SITE_CHAN);
    final Station result = stationConverter.convert(SITE_DAO_1.getId().getOnDate(), SITE_DAO_1.getOffDate(),
      siteList, siteChanList, List.of(CHANNEL_GROUP_STA01.toBuilder().setName(STA).build()), List.of(CHANNEL_STA01_STA01_BHE));

    assertNotNull(result);
    assertNotNull(result.getName());
    assertNotNull(result.getEffectiveAt());
    assertTrue(result.isPresent());
  }

  @Test
  void testConvertToVersionReference_mainSiteDao() {
    // tests a single main SiteDao
    SiteDao mainSiteDao = SITE_DAO_1;
    Station testStation = stationConverter.convertToVersionReference(mainSiteDao);

    assertNotNull(testStation);
    assertNotNull(testStation.getName());
    assertEquals(mainSiteDao.getReferenceStation(), testStation.getName());
    assertNotNull(testStation.getEffectiveAt());
    assertEquals(mainSiteDao.getId().getOnDate(), testStation.getEffectiveAt().orElseThrow());
    assertFalse(testStation.isPresent());
  }

  @ParameterizedTest
  @MethodSource("convertValidationParameterSource")
  void testConvertFromDaos_validationErrors(Class<Exception> errorType,
    Instant versionStartTime, Instant versionEndTime, Collection<SiteDao> siteDaos,
    Collection<SiteChanDao> siteChanDaos, Collection<ChannelGroup> channelGroups, Collection<Channel> channel) {
    assertThrows(errorType,
      () -> stationConverter.convert(versionStartTime, versionEndTime, siteDaos, siteChanDaos,
        channelGroups, channel));
  }

  @Test
  void testConvertToVersionReference_nullSiteDao() {
    assertThrows(NullPointerException.class,
      () -> stationConverter.convertToVersionReference(null));
  }
}
