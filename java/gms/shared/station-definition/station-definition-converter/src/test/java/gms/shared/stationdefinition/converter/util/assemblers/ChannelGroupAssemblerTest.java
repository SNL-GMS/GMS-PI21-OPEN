package gms.shared.stationdefinition.converter.util.assemblers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.RangeMap;
import com.google.common.collect.Table;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelGroup;
import gms.shared.stationdefinition.coi.utils.comparator.ChannelGroupComparator;
import gms.shared.stationdefinition.converter.DaoCalibrationConverter;
import gms.shared.stationdefinition.converter.DaoChannelConverter;
import gms.shared.stationdefinition.converter.DaoChannelGroupConverter;
import gms.shared.stationdefinition.converter.FileFrequencyAmplitudePhaseConverter;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteChanKey;
import gms.shared.stationdefinition.dao.css.SiteDao;
import gms.shared.stationdefinition.dao.css.SiteKey;
import gms.shared.stationdefinition.dao.css.enums.ChannelType;
import gms.shared.stationdefinition.testfixtures.DefaultCoiTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_CHAN_DAO_REF_11;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_DAO_REF_11;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_OVERLAPPING_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_OVERLAPPING_2;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.createSiteChanDao;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.createSiteDao;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHAN1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHAN5;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHANID_1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHANNEL_DESCRIPTION_1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHAN_PARAM_MAP;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.LDATE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.OFFDATE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.OFFDATE4;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE3;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE4;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.REFERENCE_STATION;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STA1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STA1_PARAM_MAP;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STA2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STATION_TYPE_1;
import static gms.shared.stationdefinition.testfixtures.DefaultCoiTestFixtures.END_2;
import static gms.shared.stationdefinition.testfixtures.DefaultCoiTestFixtures.START_2;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.STA01_STA01_BHE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelGroupAssemblerTest {
  private static final DaoCalibrationConverter calibrationConverter = new DaoCalibrationConverter();
  private static final FileFrequencyAmplitudePhaseConverter fapConverter = new FileFrequencyAmplitudePhaseConverter();
  private static final DaoChannelConverter channelConverter = new DaoChannelConverter(calibrationConverter, fapConverter);
  private static final DaoChannelGroupConverter channelGroupConverter = new DaoChannelGroupConverter();
  private static final Function<String, String> parseChannelCode = channelName -> {
    String[] parsedNames = channelName.split("/")[0].split("\\.");
    Preconditions.checkState(parsedNames.length > 2, "Cannot parse channel code from " + channelName);
    return parsedNames[2];
  };
  private static final Function<String, String> parseStationCode = channelName -> {
    String channelDef = channelName.split("/")[0];
    String[] parsedNames = channelName.split("\\.");
    Preconditions.checkState(parsedNames.length > 2, "Cannot parse channel code from " + channelName);
    return channelDef.contains("beam") ?
      parsedNames[0] :
      parsedNames[1];
  };
  private final Instant now = Instant.now();
  private ChannelGroupAssembler channelGroupAssembler;

  @Mock
  DaoChannelGroupConverter mockConverter;

  static Stream<Arguments> getBuildAllValidationArguments() {
    return Stream.of(arguments(null, List.of(), Instant.EPOCH, HashBasedTable.create()),
      arguments(List.of(), null, Instant.EPOCH, HashBasedTable.create()),
      arguments(List.of(), List.of(), null, HashBasedTable.create()),
      arguments(List.of(), List.of(), Instant.EPOCH, null));
  }

  static Stream<Arguments> getBuildAllArguments() {
    var defaultSiteDao = getUpdatedDefaultSiteDao();
    var defaultSiteChanDao = getUpdatedDefaultSiteChanDao(defaultSiteDao);

    Table<String, String, NavigableMap<Instant, Channel>> channelsByStaChan =
      AssemblerUtils.buildVersionTable(
        Functions.compose(ChannelGroupAssemblerTest::getCssKeyFromName, Channel::getName).andThen(SiteChanKey::getStationCode),
        Functions.compose(ChannelGroupAssemblerTest::getCssKeyFromName, Channel::getName).andThen(SiteChanKey::getChannelCode),
        Functions.compose(Optional::get, Channel::getEffectiveAt),
        List.of(CHANNEL.toBuilder()
          .setName(STA01_STA01_BHE)
          .setEffectiveAt(defaultSiteDao.getId().getOnDate()).build()));

    var defaultOffdate = defaultSiteDao.getOffDate();

    Instant effectiveUntil = defaultOffdate.atZone(ZoneOffset.UTC)
      .withHour(11)
      .withMinute(59)
      .withSecond(59)
      .withNano(999999000)
      .toInstant();

    return Stream.of(
      arguments(List.of(), List.of(), List.of(defaultSiteChanDao), HashBasedTable.create()),
      arguments(List.of(), List.of(defaultSiteDao), List.of(), HashBasedTable.create()),
      arguments(
        List.of(),
        List.of(defaultSiteDao), List.of(defaultSiteChanDao), channelsByStaChan)
    );
  }

  static Stream<Arguments> getBuildAllForTimeRangeValidationArguments() {
    return Stream.of(
      arguments(null, List.of(), Instant.EPOCH, Instant.EPOCH.plusSeconds(10), HashBasedTable.create()),
      arguments(List.of(), null, Instant.EPOCH, Instant.EPOCH.plusSeconds(10), HashBasedTable.create()),
      arguments(List.of(), List.of(), null, Instant.EPOCH.plusSeconds(10), HashBasedTable.create()),
      arguments(List.of(), List.of(), Instant.EPOCH, null, HashBasedTable.create()),
      arguments(List.of(), List.of(), Instant.EPOCH, Instant.EPOCH.plusSeconds(10), null));
  }

  public static SiteChanKey getCssKeyFromName(String channelName) {
    final String stationCode = parseStationCode.apply(channelName);
    final String channelCode = parseChannelCode.apply(channelName);

    return new SiteChanKey(stationCode, channelCode, Instant.now());
  }

  @BeforeEach
  void setUp() {
    channelGroupAssembler = new ChannelGroupAssembler(channelGroupConverter);
  }

  @ParameterizedTest
  @MethodSource("getBuildAllValidationArguments")
  void testBuildAllValidation(List<SiteDao> sites, List<SiteChanDao> siteChans, Instant effectiveAt,
    Table<String, String, NavigableMap<Instant, Channel>> channelsByStaChan) {
    assertThrows(NullPointerException.class, () -> channelGroupAssembler.buildAllForTime(
      sites, siteChans, effectiveAt, channelsByStaChan));
  }

  @ParameterizedTest
  @MethodSource("getBuildAllArguments")
  void testBuildAll(List<ChannelGroup> expected,
    List<SiteDao> sites,
    List<SiteChanDao> siteChans,
    Table<String, String, NavigableMap<Instant, Channel>> channelsByStaChan) {

    List<ChannelGroup> actual = channelGroupAssembler.buildAllForTime(sites, siteChans, now, channelsByStaChan);
    expected = updateEffectiveUntil(
      expected, AssemblerUtils.effectiveUntilNoonOffset);
    expected = updateEffectiveAt(expected, AssemblerUtils.effectiveAtNoonOffset);

    assertEquals(expected.size(), actual.size());
    assertEquals(expected, actual);
  }

  @ParameterizedTest
  @MethodSource("getBuildAllForTimeRangeValidationArguments")
  void testBuildAllForTimeRangeValidation(List<SiteDao> sites, List<SiteChanDao> siteChans,
    Instant startTime, Instant endTime, Table<String, String, NavigableMap<Instant, Channel>> channelsByStaChan) {

    assertThrows(NullPointerException.class,
      () -> channelGroupAssembler.buildAllForTimeRange(sites, siteChans, startTime, endTime, channelsByStaChan));
  }

  @Test
  void testBuildAllForTimeRange() {

    var assembler = new ChannelGroupAssembler(mockConverter);

    var testChan = DefaultCoiTestFixtures.getDefaultChannel();
    testChan = testChan.toBuilder()
      .setName(STA01_STA01_BHE)
      .setEffectiveAt(SITE_DAO_REF_11.getId().getOnDate()).build();
    testChan = (Channel) testChan.setEffectiveUntil(SITE_DAO_REF_11.getOffDate());
    var expectedChannelGroup = DefaultCoiTestFixtures.getDefaultChannelGroupFromSiteAndChannel(SITE_DAO_REF_11, testChan);
    
    when(mockConverter.convert(eq(SITE_DAO_REF_11), eq(List.of(SITE_CHAN_DAO_REF_11)), any(), any(), any(), any())).thenReturn(expectedChannelGroup);

    List<Channel> channels = List.of(testChan);
    Table<String, String, NavigableMap<Instant, Channel>> channelsByStaChan =
      AssemblerUtils.buildVersionTable(
        Functions.compose(ChannelGroupAssemblerTest::getCssKeyFromName, Channel::getName).andThen(SiteChanKey::getStationCode),
        Functions.compose(ChannelGroupAssemblerTest::getCssKeyFromName, Channel::getName).andThen(SiteChanKey::getChannelCode),
        Functions.compose(Optional::get, Channel::getEffectiveAt),
        channels);

    List<ChannelGroup> expected = List.of(
      expectedChannelGroup);

    List<ChannelGroup> actual = assembler.buildAllForTimeRange(List.of(SITE_DAO_REF_11),
      List.of(SITE_CHAN_DAO_REF_11),
      ONDATE, ONDATE.plusSeconds(10),
      channelsByStaChan);

    assertEquals(expected.size(), actual.size());
    assertEquals(expected, actual);
  }

  @Test
  void testBuildAllForTimeRange_channelEffectiveAtUpdatedByResponse() {
    var defaultSiteDao = getUpdatedDefaultSiteDao();
    var defaultSiteChanDao = getUpdatedDefaultSiteChanDao(defaultSiteDao);

    Channel channel = CHANNEL.toBuilder()
      .setName(STA01_STA01_BHE)
      .setEffectiveAt(defaultSiteDao.getId().getOnDate()).build();
    channel = (Channel)channel.setEffectiveAtUpdatedByResponse(true);
    List<Channel> channels = List.of(channel);

    Table<String, String, NavigableMap<Instant, Channel>> channelsByStaChan =
      AssemblerUtils.buildVersionTable(
        Functions.compose(ChannelGroupAssemblerTest::getCssKeyFromName, Channel::getName).andThen(SiteChanKey::getStationCode),
        Functions.compose(ChannelGroupAssemblerTest::getCssKeyFromName, Channel::getName).andThen(SiteChanKey::getChannelCode),
        Functions.compose(Optional::get, Channel::getEffectiveAt),
        channels);

    ChannelGroup expectedChannelGroup = channelGroupConverter.convert(
      defaultSiteDao,
      List.of(defaultSiteChanDao),
      Channel::createEntityReference,
      defaultSiteDao.getId().getOnDate(),
      defaultSiteDao.getOffDate(),
      channels);

    List<ChannelGroup> expected = List.of(
      expectedChannelGroup);

    List<ChannelGroup> actual = channelGroupAssembler.buildAllForTimeRange(List.of(defaultSiteDao),
      List.of(defaultSiteChanDao),
      ONDATE, ONDATE.plusSeconds(10),
      channelsByStaChan);

    assertEquals(expected.size(), actual.size());
    assertEquals(expected, actual);
  }

  @Test
  void testBuildAllForTimeRange_channelEffectiveUntilUpdatedByResponse() {
    var defaultSiteDao = getUpdatedDefaultSiteDao();
    var defaultSiteChanDao = getUpdatedDefaultSiteChanDao(defaultSiteDao);

    Channel channel = CHANNEL.toBuilder()
      .setName(STA01_STA01_BHE)
      .setEffectiveAt(defaultSiteDao.getId().getOnDate()).build();
    channel = (Channel)channel.setEffectiveUntilUpdatedByResponse(true);
    channel = (Channel)channel.setEffectiveUntil(defaultSiteDao.getOffDate());
    List<Channel> channels = List.of(channel);

    Table<String, String, NavigableMap<Instant, Channel>> channelsByStaChan =
      AssemblerUtils.buildVersionTable(
        Functions.compose(ChannelGroupAssemblerTest::getCssKeyFromName, Channel::getName).andThen(SiteChanKey::getStationCode),
        Functions.compose(ChannelGroupAssemblerTest::getCssKeyFromName, Channel::getName).andThen(SiteChanKey::getChannelCode),
        Functions.compose(Optional::get, Channel::getEffectiveAt),
        channels);

    ChannelGroup expectedChannelGroup = channelGroupConverter.convert(
      defaultSiteDao,
      List.of(defaultSiteChanDao),
      Channel::createEntityReference,
      defaultSiteDao.getId().getOnDate(),
      defaultSiteDao.getOffDate(),
      channels);

    List<ChannelGroup> expected = List.of(
      expectedChannelGroup);

    List<ChannelGroup> actual = channelGroupAssembler.buildAllForTimeRange(List.of(defaultSiteDao),
      List.of(defaultSiteChanDao),
      ONDATE, ONDATE.plusSeconds(10),
      channelsByStaChan);

    assertEquals(expected.size(), actual.size());
    assertEquals(expected, actual);
  }

  @Test
  void testBuildAllForTime_updateActiveChannels() throws JsonProcessingException {
    var defaultSiteDao = getUpdatedDefaultSiteDao();
    var defaultSiteChanDao = getUpdatedDefaultSiteChanDao(defaultSiteDao);

    List<Channel> channels = List.of(
      CHANNEL.toBuilder()
        .setName(STA01_STA01_BHE)
        .setEffectiveAt(defaultSiteDao.getId().getOnDate())
        .setData(CHANNEL.getData().get().toBuilder().setEffectiveUntil(ONDATE2.minusSeconds(1)).build())
        .build(),
      CHANNEL.toBuilder()
        .setName(STA01_STA01_BHE)
        .setEffectiveAt(ONDATE2)
        .build());

    Table<String, String, NavigableMap<Instant, Channel>> channelsByStaChan =
      AssemblerUtils.buildVersionTable(
        Functions.compose(ChannelGroupAssemblerTest::getCssKeyFromName, Channel::getName).andThen(SiteChanKey::getStationCode),
        Functions.compose(ChannelGroupAssemblerTest::getCssKeyFromName, Channel::getName).andThen(SiteChanKey::getChannelCode),
        Functions.compose(Optional::get, Channel::getEffectiveAt),
        channels);

    Instant effectiveUntil = defaultSiteDao.getOffDate();

    List<ChannelGroup> expected = List.of(channelGroupConverter
      .convert(defaultSiteDao, List.of(defaultSiteChanDao), defaultSiteDao.getId().getOnDate(),
        effectiveUntil, List.of(CHANNEL.toBuilder()
          .setName(STA01_STA01_BHE)
          .setEffectiveAt(defaultSiteDao.getId().getOnDate())
          .build())));

    List<ChannelGroup> actual = channelGroupAssembler.buildAllForTime(
      List.of(defaultSiteDao), List.of(defaultSiteChanDao), ONDATE2, channelsByStaChan);

    assertEquals(expected.size(), actual.size());
    assertEquals(expected, actual);
  }

  @Test
  void testBuildAllForTimeRange_PositionChanges() throws JsonProcessingException {
    var channel5 = CHANNEL.toBuilder()
      .setName("STA01.STA01.sz")
      .setEffectiveAt(ONDATE).build();

    List<Channel> channels = List.of(channel5);
    Table<String, String, NavigableMap<Instant, Channel>> channelsByStaChan =
      AssemblerUtils.buildVersionTable(
        Functions.compose(ChannelGroupAssemblerTest::getCssKeyFromName, Channel::getName).andThen(SiteChanKey::getStationCode),
        Functions.compose(ChannelGroupAssemblerTest::getCssKeyFromName, Channel::getName).andThen(SiteChanKey::getChannelCode),
        Functions.compose(Optional::get, Channel::getEffectiveAt),
        channels);

    SiteChanDao siteChanDao = createNewSiteChanDao(STA1, CHAN5, ONDATE, OFFDATE, 1L,
      ChannelType.N, 0.03, -1.0, 0.0, CHANNEL_DESCRIPTION_1, LDATE);

    List<ChannelGroup> expected1 = List.of(channelGroupConverter.convert(SITE_OVERLAPPING_1,
      List.of(siteChanDao),
      Channel::createEntityReference, ONDATE, ONDATE2, channels));
    List<ChannelGroup> expected2 = List.of(channelGroupConverter.convert(SITE_OVERLAPPING_2,
      List.of(siteChanDao),
      Channel::createEntityReference, ONDATE2, OFFDATE, channels));

    expected1 = updateEffectiveUntil(expected1, AssemblerUtils.effectiveUntilNoonOffset);
    expected2 = updateEffectiveAt(expected2, AssemblerUtils.effectiveAtNoonOffset);

    List<ChannelGroup> expected = Stream.concat(expected1.stream(), expected2.stream()).collect(Collectors.toList());

    List<SiteDao> siteList = List.of(SITE_OVERLAPPING_1, SITE_OVERLAPPING_2);
    List<ChannelGroup> actual = channelGroupAssembler.buildAllForTimeRange(siteList,
      List.of(siteChanDao),
      Instant.EPOCH, OFFDATE4, channelsByStaChan);

    assertEquals(expected.size(), actual.size());
    assertEquals(expected, actual);
  }

  @Test
  void testMultipleChannelGroups() {
    var channel1 = CHANNEL.toBuilder()
      .setName("STA01.STA01.BHE")
      .setEffectiveAt(ONDATE).build();
    var channel2 = CHANNEL.toBuilder()
      .setName("STA02.STA02.BHE")
      .setEffectiveAt(ONDATE2).build();

    List<Channel> channels = List.of(channel1, channel2);
    Table<String, String, NavigableMap<Instant, Channel>> channelsByStaChan =
      AssemblerUtils.buildVersionTable(
        Functions.compose(ChannelGroupAssemblerTest::getCssKeyFromName, Channel::getName).andThen(SiteChanKey::getStationCode),
        Functions.compose(ChannelGroupAssemblerTest::getCssKeyFromName, Channel::getName).andThen(SiteChanKey::getChannelCode),
        Functions.compose(Optional::get, Channel::getEffectiveAt),
        channels);

    var siteDao1 = createSiteDao(STA1, ONDATE4, OFFDATE,
      STA1_PARAM_MAP, STATION_TYPE_1, REFERENCE_STATION);
    var siteDao2 = createSiteDao(STA2, ONDATE3, OFFDATE,
      STA1_PARAM_MAP, STATION_TYPE_1, REFERENCE_STATION);
    var siteChanDao1 = createSiteChanDao(STA1, CHAN1, ONDATE, OFFDATE, CHAN_PARAM_MAP, CHANID_1);
    var siteChanDao2 = createSiteChanDao(STA2, CHAN1, ONDATE2, OFFDATE, CHAN_PARAM_MAP, CHANID_1);

    List<SiteDao> siteDaos = List.of(siteDao1, siteDao2);
    List<SiteChanDao> siteChanDaos = List.of(siteChanDao1, siteChanDao2);

    List<ChannelGroup> results = channelGroupAssembler.buildAllForTimeRange(
      siteDaos, siteChanDaos, ONDATE, OFFDATE, channelsByStaChan);
    assertEquals(2, results.size());
  }

  private static SiteDao getUpdatedDefaultSiteDao() {
    // update the default site dao
    var defaultSiteDao = DefaultCoiTestFixtures.getDefaultSiteDao();
    var siteKey = new SiteKey(STA1, START_2);
    defaultSiteDao.setId(siteKey);
    defaultSiteDao.setOffDate(END_2);
    defaultSiteDao.setLoadDate(LDATE);
    return defaultSiteDao;
  }

  private static SiteChanDao getUpdatedDefaultSiteChanDao(SiteDao defaultSiteDao) {
    // update the default site chan dao
    var defaultSiteChanDao = DefaultCoiTestFixtures.getDefaultSiteChanDaoFromSiteDao(defaultSiteDao);
    var oldSiteChanKey = defaultSiteChanDao.getId();
    var siteChanKey = new SiteChanKey(oldSiteChanKey.getStationCode(),
      oldSiteChanKey.getChannelCode(), oldSiteChanKey.getOnDate());
    defaultSiteChanDao.setId(siteChanKey);
    return defaultSiteChanDao;
  }

  private SiteChanDao copySiteChanDao(SiteChanDao siteChanDao) {
    SiteChanKey siteChanKey = siteChanDao.getId();
    SiteChanDao siteChanDaoCopy = new SiteChanDao();
    SiteChanKey siteChanKeyCopy =
      new SiteChanKey(siteChanKey.getStationCode(), siteChanKey.getChannelCode(), siteChanKey.getOnDate());
    siteChanDaoCopy.setId(siteChanKeyCopy);
    siteChanDaoCopy.setChannelId(siteChanDao.getChannelId());
    siteChanDaoCopy.setOffDate(siteChanDao.getOffDate());
    siteChanDaoCopy.setChannelType(siteChanDao.getChannelType());
    siteChanDaoCopy.setEmplacementDepth(siteChanDao.getEmplacementDepth());
    siteChanDaoCopy.setHorizontalAngle(siteChanDao.getHorizontalAngle());
    siteChanDaoCopy.setVerticalAngle(siteChanDao.getVerticalAngle());
    siteChanDaoCopy.setChannelDescription(siteChanDao.getChannelDescription());
    siteChanDaoCopy.setLoadDate(siteChanDao.getLoadDate());

    return siteChanDaoCopy;
  }

  private SiteChanDao createNewSiteChanDao(String stationCode, String channelCode, Instant onDate,
    Instant offDate, long channelId, ChannelType channelType, double depth, double horizontalAngle,
    double verticleAngle, String description, Instant loadDate) {
    SiteChanKey siteChanKey = new SiteChanKey(stationCode, channelCode, onDate);
    SiteChanDao newSiteChanDao = new SiteChanDao();
    newSiteChanDao.setId(siteChanKey);
    newSiteChanDao.setChannelId(channelId);
    newSiteChanDao.setOffDate(offDate);
    newSiteChanDao.setChannelType(channelType);
    newSiteChanDao.setEmplacementDepth(depth);
    newSiteChanDao.setHorizontalAngle(horizontalAngle);
    newSiteChanDao.setVerticalAngle(verticleAngle);
    newSiteChanDao.setChannelDescription(description);
    newSiteChanDao.setLoadDate(loadDate);

    return newSiteChanDao;
  }

  private void compareInstantResults(Map<SiteKey, NavigableMap<Instant, List<SiteChanDao>>> expected,
    Map<SiteKey, NavigableMap<Instant, List<SiteChanDao>>> actual) {
    Set<SiteKey> expectedKeySet = expected.keySet();
    Set<SiteKey> actualKeySet = actual.keySet();
    assertEquals(expectedKeySet, actualKeySet);

    for (SiteKey key1 : expectedKeySet) {
      for (SiteKey key2 : actualKeySet) {
        if (key1.equals(key2)) {
          List<SiteChanDao> expectedSiteChanDaos = expected.get(key1)
            .values()
            .stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
          List<SiteChanDao> actualSiteChanDaos = expected.get(key2)
            .values()
            .stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
          assertTrue(expectedSiteChanDaos.containsAll(actualSiteChanDaos));
          assertTrue(actualSiteChanDaos.containsAll(expectedSiteChanDaos));
        }
      }
    }
  }

  private void compareRangeResults(Map<SiteKey, RangeMap<Instant, List<SiteChanDao>>> expected,
    Map<SiteKey, RangeMap<Instant, List<SiteChanDao>>> actual) {
    Set<SiteKey> expectedKeySet = expected.keySet();
    Set<SiteKey> actualKeySet = actual.keySet();
    assertEquals(expectedKeySet, actualKeySet);

    for (SiteKey key1 : expectedKeySet) {
      for (SiteKey key2 : actualKeySet) {
        if (key1.equals(key2)) {
          List<SiteChanDao> expectedSiteChanDaos = expected.get(key1)
            .asDescendingMapOfRanges()
            .values()
            .stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
          List<SiteChanDao> actualSiteChanDaos = expected.get(key2)
            .asDescendingMapOfRanges()
            .values()
            .stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
          assertTrue(expectedSiteChanDaos.containsAll(actualSiteChanDaos));
          assertTrue(actualSiteChanDaos.containsAll(expectedSiteChanDaos));
        }
      }
    }
  }

  private List<ChannelGroup> updateEffectiveUntil(List<ChannelGroup> channelGroups, UnaryOperator<Instant> func){
    return channelGroups.stream()
      .map(chanGroup -> chanGroup.toBuilder()
        .setData(chanGroup.getData().get().toBuilder().setEffectiveUntil(
            Optional.of(func.apply(chanGroup.getData().get().getEffectiveUntil().get())))
          .build())
        .build())
      .distinct()
      .sorted(new ChannelGroupComparator())
      .collect(Collectors.toList());
  }
  private List<ChannelGroup> updateEffectiveAt(List<ChannelGroup> channelGroups, UnaryOperator<Instant> func){
    return channelGroups.stream()
      .map(chan -> chan.toBuilder().setEffectiveAt(func.apply(chan.getEffectiveAt().get()))
        .build())
      .sorted()
      .collect(Collectors.toList());
  }
}
