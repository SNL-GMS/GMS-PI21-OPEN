package gms.shared.stationdefinition.converter;

import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelGroup;
import gms.shared.stationdefinition.coi.channel.Location;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteChanKey;
import gms.shared.stationdefinition.dao.css.SiteDao;
import gms.shared.stationdefinition.dao.css.SiteKey;
import gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures;
import gms.shared.stationdefinition.testfixtures.DefaultCoiTestFixtures;
import gms.shared.stationdefinition.testfixtures.UtilsTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.TreeSet;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.LDATE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STA1;
import static gms.shared.stationdefinition.testfixtures.DefaultCoiTestFixtures.END_2;
import static gms.shared.stationdefinition.testfixtures.DefaultCoiTestFixtures.START_2;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL_TWO;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.STA01_STA01_BHE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class DaoChannelGroupConverterTest {
  private final DaoResponseConverter responseConverter = new DaoResponseConverter();
  private final DaoCalibrationConverter calibrationConverter = new DaoCalibrationConverter();
  private final FileFrequencyAmplitudePhaseConverter fapConverter = new FileFrequencyAmplitudePhaseConverter();
  private final DaoChannelConverter channelConverter = new DaoChannelConverter(calibrationConverter, fapConverter);

  private DaoChannelGroupConverter daoChannelGroupConverter;
  private NavigableSet<Channel> channels;
  private NavigableSet<Channel> invalidChannels;

  private ChannelGroup channelGroup;
  private final Channel STA01_CHANNEL = CHANNEL.toBuilder().setName(STA01_STA01_BHE).build();

  private SiteDao siteDao1;
  private SiteDao invalidSiteDao;

  public static UnaryOperator<Channel> channelFunction = (channel) -> CHANNEL;

  @Mock
  private UnaryOperator<Channel> channelFunctionMock;

  @BeforeEach
  void setUp() {
    daoChannelGroupConverter = new DaoChannelGroupConverter();
  }


  @BeforeEach
  void createReferencedObjects() {
    channels = new TreeSet<>();
    channels.add(CHANNEL);

    invalidChannels = new TreeSet<>();
    invalidSiteDao = new SiteDao();

    channelGroup = UtilsTestFixtures.CHANNEL_GROUP1;

    siteDao1 = CSSDaoTestFixtures.SITE_DAO_1;
  }

  @Test
  void testConvertBiFunction() {
    final SiteDao mainSiteDao = CSSDaoTestFixtures.SITE_DAO_1;
    final List<SiteChanDao> siteChanDaos = List
      .of(CSSDaoTestFixtures.SITE_CHAN_DAO_1, CSSDaoTestFixtures.SITE_CHAN_DAO_2);
    doReturn(CHANNEL, CHANNEL_TWO).when(channelFunctionMock).apply(any());
    final ChannelGroup result = daoChannelGroupConverter.convert(mainSiteDao, siteChanDaos, channelFunctionMock,
      CSSDaoTestFixtures.SITE_DAO_1.getId().getOnDate(), CSSDaoTestFixtures.SITE_DAO_1.getOffDate(),
      List.of(CHANNEL, CHANNEL_TWO));

    final OptionalDouble possibleChannelGroupAverageDepth = siteChanDaos
      .stream()
      .mapToDouble(SiteChanDao::getEmplacementDepth)
      .average();

    final double channelGroupAverageDepth = possibleChannelGroupAverageDepth.orElse(0);

    assertNotNull(result);

    assertEquals(mainSiteDao.getId().getStationCode(), result.getName());
    assertEquals(mainSiteDao.getId().getOnDate(), result.getEffectiveAt().get());
    assertEquals(mainSiteDao.getStationName(), result.getDescription());
    assertTrue(result.getLocation().isPresent());
    assertEquals(
      Location.from(mainSiteDao.getLatitude(), mainSiteDao.getLongitude(), channelGroupAverageDepth,
        mainSiteDao.getElevation()),
      result.getLocation().get());
    assertEquals(ChannelGroup.ChannelGroupType.PHYSICAL_SITE, result.getType());
    assertNotNull(result.getChannels());
    assertTrue(result.getChannels().stream().allMatch(c -> c.isPresent() && c.getName() != null));
  }

  @Test
  void testConvertToVersionReference() {
    SiteDao mainSiteDao = CSSDaoTestFixtures.getMainSiteForFirstChannelGroup();

    final ChannelGroup channelGroup = daoChannelGroupConverter
      .convertToVersionReference(mainSiteDao);

    assertNotNull(channelGroup);
    assertNotNull(channelGroup.getName());
    assertEquals(mainSiteDao.getId().getStationCode(), channelGroup.getName());
    assertNotNull(channelGroup.getEffectiveAt());
    assertEquals(mainSiteDao.getId().getOnDate(), channelGroup.getEffectiveAt().orElseThrow());
    assertFalse(channelGroup.isPresent());
  }

  @Test
  void testConvertToVersionReference_nullSite() {
    assertThrows(NullPointerException.class,
      () -> daoChannelGroupConverter.convertToVersionReference(null));
  }

  @Test
  void testConvertFromDaos() {
    final SiteDao defaultSiteDao = getUpdatedDefaultSiteDao();
    final SiteChanDao defaultSiteChanDao = getUpdatedDefaultSiteChanDao(defaultSiteDao);

    final List<SiteChanDao> siteChanDaos = List.of(defaultSiteChanDao);
    final List<Channel> channels = siteChanDaos.stream()
      .map(siteChanDao -> channelConverter.convertToVersionReference(defaultSiteDao, siteChanDao))
      .collect(Collectors.toList());

    final ChannelGroup result = daoChannelGroupConverter.convert(defaultSiteDao,
      siteChanDaos,
      defaultSiteDao.getId().getOnDate(),
      defaultSiteDao.getOffDate(),
      channels);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertNotNull(result.getName());
    assertEquals(defaultSiteDao.getId().getStationCode(), result.getName());
    assertNotNull(result.getEffectiveAt());
    assertTrue(result.getEffectiveAt().isPresent());
    assertEquals(defaultSiteDao.getId().getOnDate(), result.getEffectiveAt().orElseThrow());
    assertEquals(siteChanDaos.size(), result.getChannels().size());
    assertEquals(result.getEffectiveUntil(), Optional.of(defaultSiteDao.getOffDate()));
    result.getChannels().forEach(channel -> {
      assertNotNull(channel);
      assertFalse(channel.isPresent());
      assertNotNull(channel.getName());
      assertNotNull(channel.getEffectiveAt());
      assertTrue(channel.getEffectiveAt().isPresent());
      assertTrue(channels.stream().anyMatch(c -> c.equals(channel)));
    });
  }

  @Test
  void testConvertWithMaxEffectiveTime() {
    SiteDao site = CSSDaoTestFixtures.SITE_DAO_1;
    SiteChanDao siteChanDao = copySiteChanDao(CSSDaoTestFixtures.SITE_CHAN_DAO_1);
    siteChanDao.setOffDate(Instant.MAX);
    final SiteDao mainSiteDao = new SiteDao(site.getId(), Instant.MAX, site.getLatitude(), site.getLongitude(),
      site.getElevation(), site.getStationName(), site.getStaType(), site.getReferenceStation(),
      site.getDegreesNorth(), site.getDegreesEast(), site.getLoadDate());

    final ChannelGroup result = daoChannelGroupConverter.convert(mainSiteDao,
      List.of(siteChanDao), CSSDaoTestFixtures.SITE_DAO_1.getId().getOnDate(), mainSiteDao.getOffDate(),
      List.of(STA01_CHANNEL));

    assertEquals(result.getEffectiveUntil(), Optional.empty());
  }

  @ParameterizedTest
  @MethodSource("convertValidationParameterBiFunctionSource")
  void testConvertFromDaosBiFunction_validationErrors(Class<Exception> errorType, SiteDao siteDao,
    Collection<SiteChanDao> siteChanDaos,
    UnaryOperator<Channel> channelBiFunction, Instant effectiveAt, Instant effectiveUntil) {

    assertThrows(errorType,
      () -> daoChannelGroupConverter.convert(
        siteDao, siteChanDaos, channelBiFunction,
        effectiveAt, effectiveUntil, List.of(STA01_CHANNEL)));
  }

  public static Stream<Arguments> convertValidationParameterBiFunctionSource() {
    Instant effectiveAt = Instant.EPOCH;
    Instant effectiveUntil = effectiveAt.plus(Duration.ofDays(30));
    return Stream.of(
      arguments(NullPointerException.class, null, List.of(CSSDaoTestFixtures.SITE_CHAN_DAO_1), channelFunction, effectiveAt, effectiveUntil),
      arguments(NullPointerException.class, CSSDaoTestFixtures.SITE_DAO_1, null, channelFunction, effectiveAt, effectiveUntil),
      arguments(NullPointerException.class, CSSDaoTestFixtures.SITE_DAO_1, List.of(CSSDaoTestFixtures.SITE_CHAN_DAO_1), null, effectiveAt, effectiveUntil),
      arguments(NullPointerException.class, CSSDaoTestFixtures.SITE_DAO_1, List.of(CSSDaoTestFixtures.SITE_CHAN_DAO_1), channelFunction, null, effectiveUntil),
      arguments(NullPointerException.class, CSSDaoTestFixtures.SITE_DAO_1, List.of(CSSDaoTestFixtures.SITE_CHAN_DAO_1), channelFunction, effectiveAt, null),
      arguments(IllegalStateException.class, CSSDaoTestFixtures.SITE_DAO_1, List.of(), channelFunction, effectiveAt, effectiveUntil),
      arguments(IllegalStateException.class, CSSDaoTestFixtures.SITE_DAO_1, List.of(), channelFunction, effectiveUntil, effectiveAt)
    );
  }

  @ParameterizedTest
  @MethodSource("convertValidationParameterSource")
  void testConvertFromDaos_validationErrors(Class<Exception> errorType, SiteDao siteDao,
    Collection<SiteChanDao> siteChanDaos, Instant effectiveAt, Instant effectiveUntil) {
    assertThrows(errorType,
      () -> daoChannelGroupConverter.convert(siteDao, siteChanDaos, effectiveAt, effectiveUntil, List.of(STA01_CHANNEL)));
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

  private static Stream<Arguments> convertValidationParameterSource() {
    Instant effectiveAt = Instant.EPOCH;
    Instant effectiveUntil = effectiveAt.plus(Duration.ofDays(30));
    return Stream.of(
      arguments(NullPointerException.class, null, List.of(CSSDaoTestFixtures.SITE_CHAN_DAO_1), effectiveAt, effectiveUntil),
      arguments(NullPointerException.class, CSSDaoTestFixtures.SITE_DAO_1, null, effectiveAt, effectiveUntil),
      arguments(IllegalStateException.class, CSSDaoTestFixtures.SITE_DAO_1, List.of(), effectiveAt, effectiveUntil),
      arguments(NullPointerException.class, CSSDaoTestFixtures.SITE_DAO_1, List.of(CSSDaoTestFixtures.SITE_CHAN_DAO_1), null, effectiveUntil),
      arguments(NullPointerException.class, CSSDaoTestFixtures.SITE_DAO_1, List.of(CSSDaoTestFixtures.SITE_CHAN_DAO_1), effectiveAt, null),
      arguments(IllegalStateException.class, CSSDaoTestFixtures.SITE_DAO_1, List.of(CSSDaoTestFixtures.SITE_CHAN_DAO_1), effectiveUntil, effectiveAt)
    );
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
}
