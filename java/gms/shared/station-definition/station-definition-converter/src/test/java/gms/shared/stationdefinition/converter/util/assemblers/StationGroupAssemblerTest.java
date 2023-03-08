package gms.shared.stationdefinition.converter.util.assemblers;

import com.google.common.collect.Range;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.coi.station.StationGroup;
import gms.shared.stationdefinition.converter.DaoStationGroupConverter;
import gms.shared.stationdefinition.dao.css.AffiliationDao;
import gms.shared.stationdefinition.dao.css.NetworkDao;
import gms.shared.stationdefinition.testfixtures.DefaultCoiTestFixtures;
import gms.shared.stationdefinition.testfixtures.UtilsTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.AFFILIATION_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.AFFILIATION_DAO_1_2;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.AFFILIATION_DAO_2;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.AFFILIATION_DAO_6;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.AFFILIATION_DAO_7;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.AFFILIATION_DAO_SAME_START1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.NETWORK_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.NETWORK_DAO_1_NO_OFFDATE;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.NETWORK_DAO_4;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.NETWORK_DAO_6;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.NETWORK_DAO_7;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.NETWORK_DAO_9;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.NEXT_AFFILIATION_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE2;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class StationGroupAssemblerTest {

  private static final Object NULL_OBJECT = null;
  private final DaoStationGroupConverter stationGroupConverter = new DaoStationGroupConverter();
  private StationGroupAssembler stationGroupAssembler;

  @BeforeEach
  void setUp() {
    stationGroupAssembler = new StationGroupAssembler(stationGroupConverter);
  }

  @ParameterizedTest
  @MethodSource("getBuildAllForTimeValidationArguments")
  void testBuildAllForTimeValidation(Instant effectiveAt,
    List<NetworkDao> networks,
    List<AffiliationDao> affiliations,
    List<AffiliationDao> nextAffiliations) {
    assertThrows(NullPointerException.class,
      () -> stationGroupAssembler.buildAllForTime(effectiveAt, networks, affiliations, nextAffiliations, null));
  }

  static Stream<Arguments> getBuildAllForTimeValidationArguments() {
    List<Station> stations = List.of(UtilsTestFixtures.STATION);
    return Stream.of(arguments(NULL_OBJECT, List.of(NETWORK_DAO_1), List.of(AFFILIATION_DAO_1),
        List.of(AFFILIATION_DAO_2), stations),
      arguments(ONDATE, NULL_OBJECT, List.of(AFFILIATION_DAO_1), List.of(AFFILIATION_DAO_2), stations),
      arguments(ONDATE, List.of(NETWORK_DAO_1), NULL_OBJECT, List.of(AFFILIATION_DAO_2), stations),
      arguments(ONDATE, List.of(NETWORK_DAO_1), List.of(AFFILIATION_DAO_1), List.of(AFFILIATION_DAO_2), NULL_OBJECT));
  }

  @ParameterizedTest
  @MethodSource("getBuildAllForTimeArguments")
  void testBuildAllForTime(List<StationGroup> expected,
    Instant effectiveAt,
    List<NetworkDao> networks,
    List<AffiliationDao> affiliations,
    List<AffiliationDao> nextAffiliations,
    List<Station> stations) {
    List<StationGroup> actual = assertDoesNotThrow(() -> stationGroupAssembler.buildAllForTime(effectiveAt,
      networks,
      affiliations,
      nextAffiliations,
      stations));
    assertNotNull(actual);
    assertEquals(expected.size(), actual.size());
    if (!actual.isEmpty()) {
      assertTrue(expected.containsAll(actual));
    }
  }

  static Stream<Arguments> getBuildAllForTimeArguments() {
    Station station = DefaultCoiTestFixtures.getDefaultStation(AFFILIATION_DAO_1.getNetworkStationTimeKey().getStation(),
      Instant.parse("1990-02-13T02:48:04.486Z"));
    List<Station> stations = List.of(station);
    StationGroup expected = StationGroup.builder()
      .setName(NETWORK_DAO_1.getNet())
      .setEffectiveAt(AFFILIATION_DAO_1.getNetworkStationTimeKey().getTime())
      .setEffectiveUntil(Optional.of(NEXT_AFFILIATION_DAO_1.getNetworkStationTimeKey().getTime()))
      .setData(StationGroup.Data.builder()
        .setDescription(NETWORK_DAO_1.getDescription())
        .setStations(stations)
        .build())
      .build();
    StationGroup expectedNoEffectiveUntil = StationGroup.builder()
      .setName(NETWORK_DAO_1.getNet())
      .setEffectiveAt(AFFILIATION_DAO_1.getNetworkStationTimeKey().getTime())
      .setData(StationGroup.Data.builder()
        .setDescription(NETWORK_DAO_1.getDescription())
        .setStations(stations)
        .build())
      .build();
    return Stream.of(
      arguments(List.of(expected),
        ONDATE2,
        List.of(NETWORK_DAO_1),
        List.of(AFFILIATION_DAO_1),
        List.of(NEXT_AFFILIATION_DAO_1),
        stations),
      arguments(List.of(expected),
        ONDATE2.plus(1, ChronoUnit.DAYS),
        List.of(NETWORK_DAO_1),
        List.of(AFFILIATION_DAO_1),
        List.of(NEXT_AFFILIATION_DAO_1),
        stations),
      arguments(List.of(expected),
        ONDATE2.plus(1, ChronoUnit.DAYS),
        List.of(NETWORK_DAO_1),
        List.of(AFFILIATION_DAO_1),
        List.of(NEXT_AFFILIATION_DAO_1, AFFILIATION_DAO_SAME_START1),
        stations),
      arguments(List.of(),
        ONDATE,
        List.of(NETWORK_DAO_1),
        List.of(AFFILIATION_DAO_1),
        List.of(NEXT_AFFILIATION_DAO_1),
        stations),
      arguments(List.of(),
        ONDATE2,
        List.of(),
        List.of(AFFILIATION_DAO_1),
        List.of(NEXT_AFFILIATION_DAO_1),
        List.of()),
      arguments(List.of(),
        ONDATE2,
        List.of(NETWORK_DAO_1),
        List.of(),
        List.of(NEXT_AFFILIATION_DAO_1),
        stations)
    );
  }

  @ParameterizedTest
  @MethodSource("getBuildAllForTimeRangeArguments")
  void testBuildAllForTimeRange(List<StationGroup> expected,
    Range<Instant> range,
    List<NetworkDao> networks,
    List<AffiliationDao> affiliations,
    List<AffiliationDao> nextAffiliations,
    List<Station> stations) {
    List<StationGroup> actual = assertDoesNotThrow(() -> stationGroupAssembler.buildAllForTimeRange(range,
      networks,
      affiliations,
      nextAffiliations,
      stations));

    assertNotNull(actual);
    assertEquals(expected.size(), actual.size());
    if (!actual.isEmpty()) {

      if(!expected.containsAll(actual)){
        System.out.println("oh no");
      }
      assertTrue(expected.containsAll(actual));
    }
  }

  static Stream<Arguments> getBuildAllForTimeRangeArguments() {

    Station station1 = DefaultCoiTestFixtures.getDefaultStation(AFFILIATION_DAO_1.getNetworkStationTimeKey().getStation(),
      Instant.parse("1990-02-13T02:48:04.486Z"));
    List<Station> stations = List.of(station1);
    List<Station> entityStations = stations.stream()
      .map(station -> station.toEntityReference())
      .collect(Collectors.toList());


    StationGroup expectedNextAffiliationEffectiveUntil = StationGroup.builder()
      .setName(NETWORK_DAO_1.getNet())
      .setEffectiveAt(AFFILIATION_DAO_1.getNetworkStationTimeKey().getTime())
      .setEffectiveUntil(Optional.of(NEXT_AFFILIATION_DAO_1.getNetworkStationTimeKey().getTime()))
      .setData(StationGroup.Data.builder()
        .setDescription(NETWORK_DAO_1.getDescription())
        .setStations(entityStations)
        .build())
      .build();
    StationGroup expectedUseNetworkEffectiveUntil = StationGroup.builder()
      .setName(NETWORK_DAO_1.getNet())
      .setEffectiveAt(AFFILIATION_DAO_1.getNetworkStationTimeKey().getTime())
      .setEffectiveUntil(Optional.of(NETWORK_DAO_1.getOffDate()))
      .setData(StationGroup.Data.builder()
        .setDescription(NETWORK_DAO_1.getDescription())
        .setStations(entityStations)
        .build())
      .build();
    StationGroup expectedNoEffectiveUntil = StationGroup.builder()
      .setName(NETWORK_DAO_1_NO_OFFDATE.getNet())
      .setEffectiveAt(AFFILIATION_DAO_1.getNetworkStationTimeKey().getTime())
      .setData(StationGroup.Data.builder()
        .setDescription(NETWORK_DAO_1_NO_OFFDATE.getDescription())
        .setStations(entityStations)
        .build())
      .build();
    // The following station groups expected1 - expected9 should be regularly generated station groups.
    // A station group should use either the affiliation or network effectiveAt and the next affiliation or network
    // effectiveUntil, whichever is more restrictive. The station group description should match the network
    // description.
    StationGroup expected1 = StationGroup.builder()
      .setName(NETWORK_DAO_6.getNet())
      .setEffectiveAt(AFFILIATION_DAO_6.getNetworkStationTimeKey().getTime())
      .setEffectiveUntil(NETWORK_DAO_7.getOnDate().minusMillis(1))
      .setData(StationGroup.Data.builder()
        .setDescription(NETWORK_DAO_6.getDescription())
        .setStations(entityStations)
        .build())
      .build();
    StationGroup expected3 = StationGroup.builder()
      .setName(NETWORK_DAO_7.getNet())
      .setEffectiveAt(NETWORK_DAO_7.getOnDate())
      .setEffectiveUntil(AFFILIATION_DAO_7.getNetworkStationTimeKey().getTime())
      .setData(StationGroup.Data.builder()
        .setDescription(NETWORK_DAO_7.getDescription())
        .setStations(entityStations)
        .build())
      .build();
    StationGroup expected4 = StationGroup.builder()
      .setName(NETWORK_DAO_9.getNet())
      .setEffectiveAt(AFFILIATION_DAO_1.getNetworkStationTimeKey().getTime())
      .setEffectiveUntil(AFFILIATION_DAO_1_2.getNetworkStationTimeKey().getTime())
      .setData(StationGroup.Data.builder()
        .setDescription(NETWORK_DAO_9.getDescription())
        .setStations(entityStations)
        .build())
      .build();
    StationGroup expected5 = StationGroup.builder()
      .setName(NETWORK_DAO_9.getNet())
      .setEffectiveAt(AFFILIATION_DAO_1_2.getNetworkStationTimeKey().getTime())
      .setEffectiveUntil(AFFILIATION_DAO_7.getNetworkStationTimeKey().getTime())
      .setData(StationGroup.Data.builder()
        .setDescription(NETWORK_DAO_9.getDescription())
        .setStations(entityStations)
        .build())
      .build();

    StationGroup expected8 = StationGroup.builder()
      .setName(NETWORK_DAO_4.getNet())
      .setEffectiveAt(NETWORK_DAO_4.getOnDate())
      .setData(StationGroup.Data.builder()
        .setDescription(NETWORK_DAO_4.getDescription())
        .setStations(entityStations)
        .build())
      .build();
    StationGroup expected9 = StationGroup.builder()
      .setName(NETWORK_DAO_7.getNet())
      .setEffectiveAt(NETWORK_DAO_7.getOnDate())
      .setData(StationGroup.Data.builder()
        .setDescription(NETWORK_DAO_7.getDescription())
        .setStations(entityStations)
        .build())
      .build();
    return Stream.of(
      arguments(List.of(expectedNextAffiliationEffectiveUntil),
        Range.closed(ONDATE, ONDATE2),
        List.of(NETWORK_DAO_1),
        List.of(AFFILIATION_DAO_1),
        List.of(NEXT_AFFILIATION_DAO_1),
        stations),
      arguments(List.of(),
        Range.closed(ONDATE, ONDATE2),
        List.of(),
        List.of(AFFILIATION_DAO_1),
        List.of(NEXT_AFFILIATION_DAO_1),
        stations),
      arguments(List.of(),
        Range.closed(ONDATE, ONDATE2),
        List.of(NETWORK_DAO_1),
        List.of(),
        List.of(NEXT_AFFILIATION_DAO_1),
        stations),
      arguments(List.of(expectedNoEffectiveUntil),
        Range.closed(ONDATE, ONDATE2),
        List.of(NETWORK_DAO_1_NO_OFFDATE),
        List.of(AFFILIATION_DAO_1),
        List.of(),
        stations),
      arguments(List.of(expectedUseNetworkEffectiveUntil),
        Range.closed(ONDATE, ONDATE2),
        List.of(NETWORK_DAO_1),
        List.of(AFFILIATION_DAO_1),
        List.of(),
        stations),
      arguments(List.of(),
        Range.closed(ONDATE, ONDATE2),
        List.of(NETWORK_DAO_1),
        List.of(AFFILIATION_DAO_1),
        List.of(NEXT_AFFILIATION_DAO_1),
        List.of()),
      arguments(List.of(expected1, expected9),
        Range.closed(ONDATE, Instant.parse("2003-09-19T00:00:00Z")),
        List.of(NETWORK_DAO_6, NETWORK_DAO_7),
        List.of(AFFILIATION_DAO_1, AFFILIATION_DAO_2),
        List.of(),
        stations)
    );
  }
}
