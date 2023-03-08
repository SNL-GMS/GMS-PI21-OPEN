package gms.shared.stationdefinition.converter;

import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.coi.station.StationGroup;
import gms.shared.stationdefinition.dao.css.AffiliationDao;
import gms.shared.stationdefinition.dao.css.NetworkDao;
import gms.shared.stationdefinition.dao.css.SiteDao;
import gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures;
import gms.shared.stationdefinition.testfixtures.UtilsTestFixtures;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.AFFILIATION_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.NETWORK_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.NEXT_AFFILIATION_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_DAO_1;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.STATION;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.getListOfStationsForDaos;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class DaoStationGroupConverterTest {
  private static final Object NULL_OBJECT = null;
  private static List<AffiliationDao> affiliations;
  private static List<SiteDao> sites;
  private final DaoResponseConverter responseConverter = new DaoResponseConverter();
  private final DaoCalibrationConverter calibrationConverter = new DaoCalibrationConverter();
  private final FileFrequencyAmplitudePhaseConverter fapConverter = new FileFrequencyAmplitudePhaseConverter();
  private final DaoChannelConverter channelConverter = new DaoChannelConverter(calibrationConverter, fapConverter);
  private final DaoStationConverter stationConverter = new DaoStationConverter();

  private DaoStationGroupConverter stationGroupConverter;

  private static final Function<SiteDao, Station> stationFunction = (siteDao) -> STATION;

  @Mock
  Function<SiteDao, Station> stationFunctionMock;

  @BeforeEach
  void setUp() {
    stationGroupConverter = new DaoStationGroupConverter();
  }

  @Test
  void testConvertBiFunction() {
    List<AffiliationDao> affiliationList = List.of(AFFILIATION_DAO_1);
    Instant effectiveUntil = NEXT_AFFILIATION_DAO_1.getNetworkStationTimeKey().getTime();
    List<SiteDao> siteDaoList = List.of(SITE_DAO_1);
    List<Station> stationList = getListOfStationsForDaos();
    final StationGroup result = stationGroupConverter.convert(AFFILIATION_DAO_1.getNetworkStationTimeKey().getTime(), Optional.of(effectiveUntil),
      CSSDaoTestFixtures.NETWORK_DAO_1, stationList);

    assertNotNull(result);
    assertTrue(result.getEffectiveAt().isPresent());
    assertNotNull(result.getStations());
    assertEquals(SITE_DAO_1.getId().getOnDate(), result.getStations().first().getEffectiveAt().orElseThrow());
    assertEquals(SITE_DAO_1.getReferenceStation(), result.getStations().first().getName());
    assertEquals(NETWORK_DAO_1.getNet(), result.getName());
    assertEquals(AFFILIATION_DAO_1.getNetworkStationTimeKey().getTime(), result.getEffectiveAt().get());
    assertEquals(effectiveUntil, result.getEffectiveUntil().orElseThrow());
  }

  @Test
  void testConvert_no_effectiveUntil() {
    List<AffiliationDao> affiliationList = List.of(AFFILIATION_DAO_1);
    List<SiteDao> siteDaoList = List.of(SITE_DAO_1);
    List<Station> stationList = getListOfStationsForDaos();
    final StationGroup result = stationGroupConverter.convert(AFFILIATION_DAO_1.getNetworkStationTimeKey().getTime(),
      Optional.empty(), CSSDaoTestFixtures.NETWORK_DAO_1, stationList);

    assertNotNull(result);
    assertTrue(result.getEffectiveAt().isPresent());
    assertTrue(result.getEffectiveUntil().isEmpty());
  }

  @Test
  void testConvert_allStations() {
    List<Station> stationList = List.of(UtilsTestFixtures.STATION, UtilsTestFixtures.STATION_2);
    final Instant min = stationList.stream()
      .map(Station::getEffectiveAt)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .min(Instant::compareTo)
      .orElseThrow();
    final long count = stationList.stream()
      .map(Station::getEffectiveAt)
      .filter(Optional::isPresent)
      .filter(s -> !s.get().isBefore(min))
      .count();
    final String networkDescription = "test network";
    final String networkName = "testNetwork";

    final StationGroup result = stationGroupConverter
      .convert(Pair.of(networkName, min), networkDescription, stationList);

    assertNotNull(result);
    assertEquals(networkName, result.getName());
    assertEquals(Optional.of(min), result.getEffectiveAt());
    assertEquals(networkDescription, result.getDescription());
    assertNotNull(result.getStations());
    assertFalse(result.getStations().isEmpty());
    assertEquals(stationList.size(), result.getStations().size());
    assertEquals(count, result.getStations().size());
  }

  @Test
  void testConvert_facetedStations_entityReference() {
    List<Station> stationList = List.of(
      Station.createEntityReference("station1"),
      Station.createEntityReference("station2"));
    final Instant min = Instant.now();
    final String networkDescription = "test network";
    final String networkName = "testNetwork";

    final StationGroup result = stationGroupConverter
      .convert(Pair.of(networkName, min), networkDescription, stationList);

    assertNotNull(result);
    assertEquals(networkName, result.getName());
    assertEquals(Optional.of(min), result.getEffectiveAt());
    assertEquals(networkDescription, result.getDescription());
    assertNotNull(result.getStations());
    assertFalse(result.getStations().isEmpty());
    assertEquals(stationList.size(), result.getStations().size());
  }

  @Test
  void testConvert_facetedStations_versionReference() {
    final Instant now = Instant.now();
    List<Station> stationList = List.of(
      Station.createVersionReference("station1", now),
      Station.createVersionReference("station1", now.minusSeconds(10)),
      Station.createVersionReference("station2", now));
    final Instant min = stationList.stream()
      .map(Station::getEffectiveAt)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .min(Instant::compareTo)
      .orElseThrow();
    final long count = stationList.stream()
      .map(Station::getEffectiveAt)
      .filter(Optional::isPresent)
      .filter(s -> !s.get().isBefore(min))
      .count();
    final String networkDescription = "test network";
    final String networkName = "testNetwork";

    final StationGroup result = stationGroupConverter
      .convert(Pair.of(networkName, min), networkDescription, stationList);

    assertNotNull(result);
    assertEquals(networkName, result.getName());
    assertEquals(Optional.of(min), result.getEffectiveAt());
    assertEquals(networkDescription, result.getDescription());
    assertNotNull(result.getStations());
    assertFalse(result.getStations().isEmpty());
    assertEquals(stationList.size(), result.getStations().size());
    assertEquals(count, result.getStations().size());
  }

  @ParameterizedTest
  @MethodSource("convertValidationParameterBiFunctionSource")
  void testConvertFromDaosBiFunction_validationErrors(Class<Exception> errorType, Instant effectiveAt,
    Optional<Instant> effectiveUntil, NetworkDao networkDao, List<Station> stations) {

    assertThrows(errorType,
      () -> stationGroupConverter.convert(effectiveAt, effectiveUntil, networkDao, stations));
  }

  public static Stream<Arguments> convertValidationParameterBiFunctionSource() {
    List<Station> stationList = List.of(UtilsTestFixtures.STATION, UtilsTestFixtures.STATION_2);
    return Stream.of(
      arguments(IllegalStateException.class, null, Optional.of(Instant.EPOCH), NETWORK_DAO_1, stationList),
      arguments(NullPointerException.class, Instant.EPOCH, null, NETWORK_DAO_1, stationList),
      arguments(NullPointerException.class, Instant.EPOCH, Optional.of(Instant.EPOCH), null, stationList),
      arguments(NullPointerException.class, Instant.EPOCH, Optional.of(Instant.EPOCH), NETWORK_DAO_1, null)
    );
  }

  @ParameterizedTest
  @MethodSource("errorCaseSource")
  <T extends Exception> void testConvert_errorCases(Pair<String, Instant> networkKey,
    String networkDescription, List<Station> stationList, Class<T> errorClass) {
    assertThrows(errorClass, () -> stationGroupConverter
      .convert(networkKey, networkDescription, stationList));
  }

  private static Stream<Arguments> errorCaseSource() {

    List<Station> stationList = List.of(UtilsTestFixtures.STATION, UtilsTestFixtures.STATION_2);
    List<Station> emptyStationList = List.of();
    final Instant now = Instant.now();
    final String networkDescription = "test network";
    final String networkName = "testNetwork";
    return Stream.of(
      arguments(Pair.of(null, now), networkDescription, stationList,
        NullPointerException.class),
      arguments(Pair.of("", now), networkDescription, stationList,
        IllegalArgumentException.class),
      arguments(Pair.of(networkName, null), networkDescription, stationList,
        IllegalStateException.class),
      arguments(Pair.of(networkName, now), null, stationList,
        NullPointerException.class),
      arguments(Pair.of(networkName, now), "", stationList, IllegalArgumentException.class),
      arguments(Pair.of(networkName, now), networkDescription, null,
        NullPointerException.class),
      arguments(Pair.of(networkName, now), networkDescription, emptyStationList,
        IllegalArgumentException.class),
      arguments(NULL_OBJECT, networkDescription, stationList, NullPointerException.class)
    );
  }
}
