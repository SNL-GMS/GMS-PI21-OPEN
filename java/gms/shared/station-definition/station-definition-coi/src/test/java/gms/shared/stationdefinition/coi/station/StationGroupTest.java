package gms.shared.stationdefinition.coi.station;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.id.VersionId;
import gms.shared.stationdefinition.coi.station.StationGroup.Data;
import gms.shared.stationdefinition.testfixtures.UtilsTestFixtures;
import gms.shared.utilities.javautilities.objectmapper.ObjectMapperFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.STATION_GROUP;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.createTestStationGroupData;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class StationGroupTest {
  private static final Object NULL_OBJECT = null;

  private static final Logger logger = LoggerFactory.getLogger(StationGroupTest.class);

  private final static ObjectMapper mapper = ObjectMapperFactory.getJsonObjectMapper();

  @Test
  void testFactoryMethodWithEmptyStationListThrowsException() {
    Data.Builder dataBuilder = Data.builder()
      .setDescription("test description")
      .setStations(List.of());

    Exception e = assertThrows(IllegalArgumentException.class,
      () -> dataBuilder.build());

  }

  @Test
  void testStationGroup_createEntityReference_serializeToAndFrom()
    throws JsonProcessingException {
    final StationGroup stationGroup = getNameFacetStationGroup("test");

    final String json = mapper.writeValueAsString(stationGroup);
    logger.info("json serialized stationGroup: {}", json);

    final StationGroup deserialized = mapper.readValue(json, StationGroup.class);
    assertEquals(stationGroup, deserialized);
    assertFalse(deserialized.isPresent());
  }

  @Test
  void testStationGroup_createEntityReference_present() {
    final StationGroup stationGroup = getNameFacetStationGroup("test");

    assertFalse(stationGroup.isPresent());
  }

  @Test
  void testStationGroup_createEntityReference_emptyName() {
    final var exception = assertThrows(IllegalArgumentException.class,
      () -> getNameFacetStationGroup(""));
    logger.info("EXPECTED ERROR: ", exception);
    assertEquals("Station group must be provided a name", exception.getMessage());
  }

  @Test
  void testStationGroup_from_serializeToAndFrom() throws JsonProcessingException {
    final StationGroup stationGroup = getFullStationGroup();
    assertThat("Stations in unserialized station group are out of order",
      stationGroup.getStations(), contains(UtilsTestFixtures.STATION));

    final String json = mapper.writeValueAsString(stationGroup);
    logger.info("json serialized stationGroup: {}", json);

    final StationGroup deserialized = mapper.readValue(json, StationGroup.class);
    assertEquals(stationGroup, deserialized);
    assertThat("Stations in deserialized station group are out of order",
      deserialized.getStations(), contains(stationGroup.getStations().toArray(new Station[]{})));
    assertTrue(deserialized.isPresent());
  }

  @Test
  void testStationGroup_CreateEntityReference_present() {
    final StationGroup stationGroup = getFullStationGroup();

    assertTrue(stationGroup.isPresent());
  }

  @ParameterizedTest
  @MethodSource("getVersionReferenceArguments")
  void testCreateVersionReferenceValidation(String name, Instant effectiveAt) {
    assertThrows(NullPointerException.class, () -> StationGroup.createVersionReference(name, effectiveAt));
  }

  static Stream<Arguments> getVersionReferenceArguments() {
    return Stream.of(arguments(NULL_OBJECT, Instant.EPOCH),
      arguments("test", NULL_OBJECT));
  }

  @Test
  void testCreateVersionReference() {
    StationGroup stationGroup = assertDoesNotThrow(() -> StationGroup.createVersionReference("test", Instant.EPOCH));
    assertNotNull(stationGroup);
  }

  @Test
  void testStationGroup_from_facetedStations_serializeToAndFrom()
    throws JsonProcessingException {
    final StationGroup stationGroup = getFullStationGroupWithFacetedStations();
    assertThat("Faceted stations in unserialized station group are out of order",
      stationGroup.getStations().stream().map(Station::getName).collect(Collectors.toList()),
      contains("station1", "station2"));

    final String json = mapper.writeValueAsString(stationGroup);
    logger.info("json serialized stationGroup: {}", json);

    final StationGroup deserialized = mapper.readValue(json, StationGroup.class);
    assertEquals(stationGroup, deserialized);
    assertThat("Faceted stations in deserialized station group are out of order",
      deserialized.getStations(), contains(stationGroup.getStations().toArray(new Station[]{})));
  }

  @Test
  void testStationGroup_from_facetedStationChannels_present() {
    final StationGroup stationGroup = getFullStationGroupWithFacetedStations();

    assertTrue(stationGroup.isPresent());
  }

  @Test
  void testStationGroup_from_facetedStationChannels_serializeToAndFrom()
    throws JsonProcessingException {
    final StationGroup stationGroup = getFullStationGroupWithStationsWithFacetedChannels();
    assertThat("Channels in unserialized station group are out of order",
      stationGroup.getStations().stream().flatMap(s -> s.getAllRawChannels().stream())
        .map(Channel::getName).collect(Collectors.toList()),
      contains(UtilsTestFixtures.CHANNEL.getName(), UtilsTestFixtures.CHANNEL_TWO.getName()));

    final String json = mapper.writeValueAsString(stationGroup);
    logger.info("json serialized stationGroup: {}", json);

    final StationGroup deserialized = mapper.readValue(json, StationGroup.class);
    assertEquals(stationGroup, deserialized);
    assertThat("Faceted stations in deserialized station group are out of order",
      deserialized.getStations(), contains(stationGroup.getStations().toArray(new Station[]{})));
  }

  @Test
  void testStationGroup_from_facetedStations_present() {
    final StationGroup stationGroup = getFullStationGroupWithStationsWithFacetedChannels();

    assertTrue(stationGroup.isPresent());
    assertTrue(stationGroup.getStations().stream().allMatch(Station::isPresent));
    assertTrue(stationGroup.getStations().stream().flatMap(s -> s.getAllRawChannels().stream())
      .noneMatch(Channel::isPresent));
  }

  @Test
  void testStationGroup_toEntityReference_fromEntityReference() {
    final StationGroup nameFacet = getNameFacetStationGroup("test");

    final StationGroup result = nameFacet.toEntityReference();

    assertEquals(nameFacet.getName(), result.getName());
    assertFalse(result.isPresent());
  }

  @Test
  void testStationGroup_toEntityReference_fromFullChannel() {
    final StationGroup fullEntity = getFullStationGroup();

    final StationGroup result = fullEntity.toEntityReference();

    assertEquals(fullEntity.getName(), result.getName());
    assertFalse(result.isPresent());
  }

  private StationGroup getNameFacetStationGroup(String name) {
    return StationGroup.createEntityReference(name);
  }

  private StationGroup getFullStationGroup() {
    return STATION_GROUP;
  }

  private StationGroup getFullStationGroupWithFacetedStations() {
    return StationGroup.builder()
      .setName("test")
      .setEffectiveAt(Instant.now())
      .setEffectiveUntil(Instant.now().plusSeconds(100))
      .setData(Data.builder()
        .setDescription("the is the group for testing!")
        .setStations(List.of(Station.createEntityReference("station1"),
          Station.createEntityReference("station2")))
        .build())
      .build();
  }

  private StationGroup getFullStationGroupWithStationsWithFacetedChannels() {
    final var channels = UtilsTestFixtures.STATION.getAllRawChannels().stream()
      .map(c -> Channel.createEntityReference(c.getName()))
      .collect(Collectors.toList());
    final var station = UtilsTestFixtures.STATION.toBuilder()
      .setEffectiveAt(Instant.EPOCH)
      .setData(UtilsTestFixtures.STATION.getData().orElseThrow().toBuilder()
        .setAllRawChannels(channels)
        .build())
      .build();
    return StationGroup.builder()
      .setName("test")
      .setEffectiveAt(Instant.now())
      .setEffectiveUntil(Optional.empty())
      .setData(Data.builder()
        .setDescription("the is the group for testing!")
        .setStations(List.of(station))
        .build())
      .build();
  }

  @Test
  void testToVersionIdValidation() {
    assertThrows(IllegalStateException.class, () -> getNameFacetStationGroup("test").toVersionId());
  }

  @Test
  void testToVersionId() {
    VersionId versionId = assertDoesNotThrow(() -> STATION_GROUP.toVersionId());
    assertEquals(STATION_GROUP.getName(), versionId.getEntityId());
    STATION_GROUP.getEffectiveAt().ifPresentOrElse(effectiveAt -> assertEquals(effectiveAt, versionId.getEffectiveAt()),
      () -> fail());
  }

  @ParameterizedTest
  @MethodSource("getBuildValidationArguments")
  void testBuildValidation(Class<? extends Exception> expectedException,
    String name,
    Instant effectiveAt,
    StationGroup.Data data) {
    StationGroup.Builder builder = StationGroup.builder()
      .setName(name)
      .setEffectiveAt(effectiveAt)
      .setData(data);
    assertThrows(expectedException, () -> builder.build());
  }

  static Stream<Arguments> getBuildValidationArguments() {
    return Stream.of(
      arguments(IllegalArgumentException.class, "", Instant.EPOCH, createTestStationGroupData()),
      arguments(IllegalStateException.class, "test", NULL_OBJECT, createTestStationGroupData()));
  }

  @Test
  void testBuild() {
    StationGroup stationGroup = assertDoesNotThrow(() -> StationGroup.builder()
      .setName("test")
      .setEffectiveAt(Instant.EPOCH)
      .setData(createTestStationGroupData())
      .build());
    assertNotNull(stationGroup);
  }

}