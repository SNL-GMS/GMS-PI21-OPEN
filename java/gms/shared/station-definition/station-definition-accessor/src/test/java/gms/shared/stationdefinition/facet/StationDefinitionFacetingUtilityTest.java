package gms.shared.stationdefinition.facet;

import gms.shared.stationdefinition.api.StationDefinitionAccessorInterface;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelGroup;
import gms.shared.stationdefinition.coi.channel.Location;
import gms.shared.stationdefinition.coi.channel.RelativePosition;
import gms.shared.stationdefinition.coi.channel.RelativePositionChannelPair;
import gms.shared.stationdefinition.coi.channel.Response;
import gms.shared.stationdefinition.coi.facets.FacetingDefinition;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.coi.station.StationGroup;
import gms.shared.stationdefinition.testfixtures.FacetingDefintionsTestFixtures;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gms.shared.stationdefinition.facet.FacetingTypes.CHANNELS_KEY;
import static gms.shared.stationdefinition.facet.FacetingTypes.CHANNEL_GROUPS_KEY;
import static gms.shared.stationdefinition.facet.FacetingTypes.CHANNEL_GROUP_TYPE;
import static gms.shared.stationdefinition.facet.FacetingTypes.CHANNEL_TYPE;
import static gms.shared.stationdefinition.facet.FacetingTypes.RESPONSES_KEY;
import static gms.shared.stationdefinition.facet.FacetingTypes.RESPONSE_TYPE;
import static gms.shared.stationdefinition.facet.FacetingTypes.STATIONS_KEY;
import static gms.shared.stationdefinition.facet.FacetingTypes.STATION_GROUP_TYPE;
import static gms.shared.stationdefinition.facet.FacetingTypes.STATION_TYPE;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL_TWO;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.RESPONSE;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.RESPONSE_FULL;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.STATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationDefinitionFacetingUtilityTest {

  private static final Instant EFFECTIVE_TIME = Instant.parse("2021-01-04T19:57:02.457440Z");

  @Mock
  private StationDefinitionAccessorInterface stationDefinitionAccessor;
  private StationDefinitionFacetingUtility stationDefinitionFacetingUtility;

  @BeforeEach
  void setup() {
    this.stationDefinitionFacetingUtility = StationDefinitionFacetingUtility
      .create(stationDefinitionAccessor);
  }

  @Test
  void testCreate_nullAccessor_throwsError() {
    assertThrows(NullPointerException.class, () -> StationDefinitionFacetingUtility.create(null));
  }

  //STATION GROUP
  @Test
  void testStationGroup_populateFacets_null_throwsError() {
    StationGroup data = null;
    final FacetingDefinition facetingDefinition = getFacetingDefinition(STATION_GROUP_TYPE.getValue(), true);

    assertThrows(NullPointerException.class,
      () -> stationDefinitionFacetingUtility.populateFacets(data, facetingDefinition, EFFECTIVE_TIME));
  }

  @Test
  void testStationGroup_populateFacets_fromPopulatedEntity_toEntityReference() {
    StationGroup data = UtilsTestFixtures.STATION_GROUP_FACET;
    final FacetingDefinition facetingDefinition = getFacetingDefinition(STATION_GROUP_TYPE.getValue(), false);

    final StationGroup result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertFalse(result.isPresent());
    assertNotEquals(data, result);
    assertEquals(data.getName(), result.getName());
  }

  @Test
  void testStationGroup_populateFacets_fromEntityReference_toEntityReference() {
    StationGroup data = UtilsTestFixtures.STATION_GROUP_FACET.toEntityReference();
    final FacetingDefinition facetingDefinition = getFacetingDefinition(STATION_GROUP_TYPE.getValue(), false);

    final StationGroup result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertFalse(result.isPresent());
    assertEquals(data, result);
    assertEquals(data.getName(), result.getName());
  }

  @Test
  void testStationGroup_populateFacets_fromEntityReference_toPopulatedStationGroup() {
    StationGroup data = UtilsTestFixtures.STATION_GROUP_FACET;
    final FacetingDefinition facetingDefinition = getFacetingDefinition(STATION_GROUP_TYPE.getValue(), true);

    final StationGroup result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(data.getName(), result.getName());
    assertTrue(result.getStations().stream().noneMatch(Station::isPresent));
  }

  @Test
  void testStationGroup_populateFacets_fromPopulatedEntity_toSelf_populatedFacetDefinitionForStations() {
    StationGroup data = UtilsTestFixtures.STATION_GROUP_FACET;
    final FacetingDefinition facetingDefinition = FacetingDefinition.builder()
      .setClassType(STATION_GROUP_TYPE.getValue())
      .setPopulated(true)
      .setFacetingDefinitions(Map.of(
        STATIONS_KEY.getValue(), getFacetingDefinition(
          STATION_TYPE.getValue(), true
        )))
      .build();
    mockBridgedStationRepositoryTimeRequestInteraction();
    final StationGroup result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);
    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(data.getName(), result.getName());
    assertTrue(result.getStations().stream().allMatch(Station::isPresent));
    assertStationGroupAndStationsPopulated(result);
  }

  @Test
  void testStationGroup_populateFacets_fromPopulatedEntity_toSelf_notPopulatedFacetDefinitionForStations() {
    StationGroup data = UtilsTestFixtures.STATION_GROUP_FACET;
    final FacetingDefinition facetingDefinition = FacetingDefinition.builder()
      .setClassType(STATION_GROUP_TYPE.getValue())
      .setPopulated(true)
      .setFacetingDefinitions(Map.of(
        STATIONS_KEY.getValue(), getFacetingDefinition(
          STATION_TYPE.getValue(), false
        )))
      .build();

    final StationGroup result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(data.getName(), result.getName());
    assertTrue(result.getStations().stream().noneMatch(Station::isPresent));
  }

  @Test
  void testStationGroup_populateFacets_fromPopulatedEntity_toSelf_populatedFacetDefinitionForChannelGroups() {
    StationGroup data = UtilsTestFixtures.STATION_GROUP_FACET;
    final FacetingDefinition stationGroupFacetingDefinition = FacetingDefinition.builder()
      .setClassType(STATION_GROUP_TYPE.getValue())
      .setPopulated(true)
      .setFacetingDefinitions(Map.of(
        STATIONS_KEY.getValue(), getFacetingDefinition(
          STATION_TYPE.getValue(), true
        )))
      .build();
    stationGroupFacetingDefinition.toBuilder().setFacetingDefinitions(Map.of(ChannelGroup.class.getName(),
      FacetingDefinition.builder()
        .setClassType(Station.class.getName())
        .setPopulated(true)
        .setFacetingDefinitions(Map.of(
          CHANNEL_GROUPS_KEY.getValue(), getFacetingDefinition(
            ChannelGroup.class.getName(), true)))
        .build()));

    mockBridgedStationRepositoryTimeRequestInteraction();
    final StationGroup result = stationDefinitionFacetingUtility
      .populateFacets(data, stationGroupFacetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(data.getName(), result.getName());
    assertTrue(result.getStations().stream().allMatch(Station::isPresent));
    assertStationGroupAndStationsPopulated(result);
  }

  @Test
  void testStationGroup_populateFacets_fromPopulatedEntity_toSelf_notPopulatedFacetDefinitionForChannelGroups() {
    StationGroup data = UtilsTestFixtures.STATION_GROUP_FACET;
    final FacetingDefinition stationGroupFacetingDefinition = FacetingDefinition.builder()
      .setClassType(STATION_GROUP_TYPE.getValue())
      .setPopulated(true)
      .setFacetingDefinitions(Map.of(
        STATIONS_KEY.getValue(),
        getFacetingDefinition(STATION_TYPE.getValue(), true).toBuilder()
          .setFacetingDefinitions(Map.of(
            CHANNEL_GROUPS_KEY.getValue(),
            getFacetingDefinition(CHANNEL_GROUP_TYPE.getValue(), false)
          ))
          .build()
      ))
      .build();

    mockBridgedStationRepositoryTimeRequestInteraction();
    final StationGroup result = stationDefinitionFacetingUtility
      .populateFacets(data, stationGroupFacetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(data.getName(), result.getName());
    assertTrue(result.getStations().stream().allMatch(Station::isPresent));
    result.getStations().forEach(stat ->
      assertTrue(stat.getChannelGroups().stream().noneMatch(ChannelGroup::isPresent)));
  }

  @Test
  void testStationGroup_populateFacets_fromPopulatedEntity_toSelf_populatedFacetDefinitionForChannels() {
    StationGroup data = UtilsTestFixtures.STATION_GROUP_FACET;
    final FacetingDefinition stationGroupFacetingDefinition = FacetingDefintionsTestFixtures.STATIONGROUP_CHANNELS_POPULATED;

    mockBridgedStationRepositoryTimeRequestInteraction();
    mockBridgedChannelRepositoryTimeRequestInteraction();
    final StationGroup result = stationDefinitionFacetingUtility
      .populateFacets(data, stationGroupFacetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(data.getName(), result.getName());
    assertTrue(result.getStations().stream().allMatch(Station::isPresent));
    result.getStations().forEach(stat ->
      assertTrue(stat.getAllRawChannels().stream().allMatch(Channel::isPresent)));
  }

  @Test
  void testStationGroup_populateFacets_fromPopulatedEntity_toSelf_nullFacetDefinition() {
    StationGroup data = UtilsTestFixtures.STATION_GROUP_FACET;

    assertThrows(NullPointerException.class,
      () -> stationDefinitionFacetingUtility.populateFacets(data, null, EFFECTIVE_TIME));
  }

  //STATION
  @Test
  void testStation_populateFacets_null_throwsError() {
    Station data = null;
    final FacetingDefinition facetingDefinition = getFacetingDefinition(STATION_TYPE.getValue(), true);

    assertThrows(NullPointerException.class,
      () -> stationDefinitionFacetingUtility.populateFacets(data, facetingDefinition,
        EFFECTIVE_TIME));
  }

  @Test
  void testStation_populateFacets_fromEntityReference_toPopulatedEntity() {
    Station data = UtilsTestFixtures.STATION_FACET
      .toEntityReference()
      .toBuilder()
      .setEffectiveAt(Instant.now())
      .build();
    final FacetingDefinition facetingDefinition = getFacetingDefinition(STATION_TYPE.getValue(), true);

    mockBridgedStationRepositoryTimeRequestInteraction();
    final Station result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(data.getName(), result.getName());
    assertTrue(result.getChannelGroups().stream().allMatch(ChannelGroup::isPresent));
  }

  @Test
  void testStation_populateFacets_fromPopulatedEntity_toEntityReference() {
    Station data = UtilsTestFixtures.STATION_FACET;
    final FacetingDefinition facetingDefinition = getFacetingDefinition(STATION_TYPE.getValue(), false);

    final Station result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertFalse(result.isPresent());
    assertEquals(data.getName(), result.getName());
  }

  @Test
  void testStation_populateFacets_fromEntityReference_toEntityReference() {
    Station data = UtilsTestFixtures.STATION_FACET.toEntityReference();
    final FacetingDefinition facetingDefinition = getFacetingDefinition(STATION_TYPE.getValue(), false);

    final Station result = stationDefinitionFacetingUtility.populateFacets(data,
      facetingDefinition,
      EFFECTIVE_TIME);

    assertNotNull(result);
    assertFalse(result.isPresent());
    assertEquals(data.getName(), result.getName());
  }

  @Test
  void testStation_populateFacets_fromPopulatedEntity_toSelf_noFacetDefinitionForChannelGroupsOrChannels() {
    Station data = UtilsTestFixtures.STATION_FACET;
    final FacetingDefinition facetingDefinition = getFacetingDefinition(STATION_TYPE.getValue(), true);

    final Station result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition,
        EFFECTIVE_TIME);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(data.getName(), result.getName());
    assertTrue(result.getChannelGroups().stream().allMatch(ChannelGroup::isPresent));
    assertTrue(result.getAllRawChannels().stream().noneMatch(Channel::isPresent));
  }

  @Test
  void testStation_populateFacets_fromPopulatedEntity_toSelf_populatedFacetDefinitionForChannelGroups() {
    Station data = UtilsTestFixtures.STATION_FACET;
    final FacetingDefinition facetingDefinition = FacetingDefinition.builder()
      .setClassType(STATION_TYPE.getValue())
      .setPopulated(true)
      .setFacetingDefinitions(Map.of(
        CHANNEL_GROUPS_KEY.getValue(),
        getFacetingDefinition(CHANNEL_GROUP_TYPE.getValue(), true))
      )
      .build();

    final Station result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(data.getName(), result.getName());
    assertTrue(result.getChannelGroups().stream().allMatch(ChannelGroup::isPresent));
    result.getChannelGroups()
      .forEach(g -> assertTrue(g.getChannels().stream().noneMatch(Channel::isPresent)));
  }

  @Test
  void testStation_populateFacets_fromPopulatedEntity_toSelf_populatedFacetDefinitionForChannelGroups_unpopulatedChannels() {
    Station data = UtilsTestFixtures.STATION_FACET;
    final FacetingDefinition facetingDefinition = FacetingDefinition.builder()
      .setClassType(STATION_TYPE.getValue())
      .setPopulated(true)
      .setFacetingDefinitions(Map.of(
        CHANNEL_GROUPS_KEY.getValue(),
        getFacetingDefinition(CHANNEL_GROUP_TYPE.getValue(), true).toBuilder()
          .setFacetingDefinitions(Map.of(
            CHANNELS_KEY.getValue(),
            getFacetingDefinition(CHANNEL_TYPE.getValue(), false)))
          .build()
      ))
      .build();

    final Station result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(data.getName(), result.getName());
    assertTrue(result.getChannelGroups().stream().allMatch(ChannelGroup::isPresent));
    result.getChannelGroups()
      .forEach(g -> assertTrue(g.getChannels().stream().noneMatch(Channel::isPresent)));
  }

  @Test
  void testStation_populateFacets_fromPopulatedEntity_toSelf_populatedFacetDefinitionForChannelGroups_populatedChannels() {
    Station data = UtilsTestFixtures.STATION_FACET;
    final FacetingDefinition facetingDefinition = FacetingDefinition.builder()
      .setClassType(STATION_TYPE.getValue())
      .setPopulated(true)
      .setFacetingDefinitions(Map.of(
        CHANNEL_GROUPS_KEY.getValue(),
        getFacetingDefinition(CHANNEL_GROUP_TYPE.getValue(), true).toBuilder()
          .setFacetingDefinitions(Map.of(
            CHANNELS_KEY.getValue(),
            getFacetingDefinition(CHANNEL_TYPE.getValue(), true)))
          .build()
      ))
      .build();

    mockBridgedChannelRepositoryTimeRequestInteraction();
    final Station result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(data.getName(), result.getName());
    assertTrue(result.getChannelGroups().stream().allMatch(ChannelGroup::isPresent));
    result.getChannelGroups()
      .forEach(g -> assertTrue(g.getChannels().stream().allMatch(Channel::isPresent)));
  }

  @Test
  void testStation_populateFacets_fromPopulatedEntity_toSelf_notPopulatedFacetDefinitionForChannelGroups() {
    Station data = UtilsTestFixtures.STATION_FACET;

    final FacetingDefinition facetingDefinition = FacetingDefinition.builder()
      .setClassType(STATION_TYPE.getValue())
      .setPopulated(true)
      .setFacetingDefinitions(Map.of(
        CHANNEL_GROUPS_KEY.getValue(), getFacetingDefinition(
          CHANNEL_GROUP_TYPE.getValue(), false
        )))
      .build();

    final Station result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(data.getName(), result.getName());
    assertTrue(result.getChannelGroups().stream().noneMatch(ChannelGroup::isPresent));
    assertTrue(result.getAllRawChannels().stream().noneMatch(Channel::isPresent));
  }

  @Test
  void testStation_populateFacets_fromPopulatedEntity_toSelf_populatedFacetDefinition_SingleChannel() {
    Station data = UtilsTestFixtures.STATION_CHANNEL_GROUP_SINGLE_CHANNEL_FACET;
    final FacetingDefinition facetingDefinition = FacetingDefinition.builder()
      .setClassType(STATION_TYPE.getValue())
      .setPopulated(true)
      .setFacetingDefinitions(Map.of(
        CHANNELS_KEY.getValue(),
        getFacetingDefinition(CHANNEL_TYPE.getValue(), true)))
      .build();

    mockBridgedChannelRepositorySingleTimeRequestInteraction();
    final Station result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNull(result);
  }

  @Test
  void testStation_populateFacets_fromPopulatedEntity_toSelf_populatedFacetDefinitionForChannels() {
    Station data = UtilsTestFixtures.STATION_FACET;
    final FacetingDefinition facetingDefinition = FacetingDefinition.builder()
      .setClassType(STATION_TYPE.getValue())
      .setPopulated(true)
      .setFacetingDefinitions(Map.of(
        CHANNELS_KEY.getValue(),
        getFacetingDefinition(CHANNEL_TYPE.getValue(), true)))
      .build();

    mockBridgedChannelRepositoryTimeRequestInteraction();
    final Station result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(data.getName(), result.getName());
    assertTrue(result.getAllRawChannels().stream().allMatch(Channel::isPresent));
  }


  @Test
  void testStation_populateFacets_fromPopulatedEntity_toSelf_populatedFacetDefinitionForChannelsAndChannelGroups() {
    Station data = UtilsTestFixtures.STATION_FACET;
    final FacetingDefinition facetingDefinition = FacetingDefinition.builder()
      .setClassType(STATION_TYPE.getValue())
      .setPopulated(true)
      .setFacetingDefinitions(Map.of(
        CHANNELS_KEY.getValue(),
        getFacetingDefinition(CHANNEL_TYPE.getValue(), true),
        CHANNEL_GROUPS_KEY.getValue(),
        getFacetingDefinition(CHANNEL_GROUP_TYPE.getValue(), true)))
      .build();

    mockBridgedChannelRepositoryTimeRequestInteraction();
    final Station result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(data.getName(), result.getName());
    assertTrue(result.getAllRawChannels().stream().allMatch(Channel::isPresent));
    assertTrue(result.getChannelGroups().stream().allMatch(ChannelGroup::isPresent));
  }

  @Test
  void testStation_populateFacets_fromPopulatedEntity_toSelf_populatedFacetDefinitionForChannelsAndUnpopulatedChannelGroups() {
    Station data = UtilsTestFixtures.STATION_FACET;
    final FacetingDefinition facetingDefinition = FacetingDefinition.builder()
      .setClassType(STATION_TYPE.getValue())
      .setPopulated(true)
      .setFacetingDefinitions(Map.of(
        CHANNELS_KEY.getValue(),
        getFacetingDefinition(CHANNEL_TYPE.getValue(), true),
        CHANNEL_GROUPS_KEY.getValue(),
        getFacetingDefinition(CHANNEL_GROUP_TYPE.getValue(), false)))
      .build();

    mockBridgedChannelRepositoryTimeRequestInteraction();
    final Station result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(data.getName(), result.getName());
    assertTrue(result.getAllRawChannels().stream().allMatch(Channel::isPresent));
    assertTrue(result.getChannelGroups().stream().noneMatch(ChannelGroup::isPresent));
  }

  @Test
  void testStation_populateFacets_fromPopulatedEntity_toSelf_populatedFacetDefinitionForChannelGroupsAndUnpopulatedChannels() {
    Station data = UtilsTestFixtures.STATION_FACET;
    final FacetingDefinition facetingDefinition = FacetingDefinition.builder()
      .setClassType(STATION_TYPE.getValue())
      .setPopulated(true)
      .setFacetingDefinitions(Map.of(
        CHANNELS_KEY.getValue(),
        getFacetingDefinition(CHANNEL_TYPE.getValue(), false),
        CHANNEL_GROUPS_KEY.getValue(),
        getFacetingDefinition(CHANNEL_GROUP_TYPE.getValue(), true)))
      .build();

    final Station result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(data.getName(), result.getName());
    assertTrue(result.getAllRawChannels().stream().noneMatch(Channel::isPresent));
    assertTrue(result.getChannelGroups().stream().allMatch(ChannelGroup::isPresent));
  }

  @Test
  void testStation_populateFacets_fromPopulatedEntity_toSelf_unpopulatedFacetDefinitionForChannelGroupsAndChannels() {
    Station data = UtilsTestFixtures.STATION_FACET;
    final FacetingDefinition facetingDefinition = FacetingDefinition.builder()
      .setClassType(STATION_TYPE.getValue())
      .setPopulated(true)
      .setFacetingDefinitions(Map.of(
        CHANNELS_KEY.getValue(),
        getFacetingDefinition(CHANNEL_TYPE.getValue(), false),
        CHANNEL_GROUPS_KEY.getValue(),
        getFacetingDefinition(CHANNEL_GROUP_TYPE.getValue(), false)))
      .build();

    final Station result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(data.getName(), result.getName());
    assertTrue(result.getAllRawChannels().stream().noneMatch(Channel::isPresent));
    assertTrue(result.getChannelGroups().stream().noneMatch(ChannelGroup::isPresent));
  }

  @Test
  void testStation_populateFacets_fromPopulatedEntity_toSelf_notPopulatedFacetDefinitionForChannels() {
    Station data = UtilsTestFixtures.STATION_FACET;
    final FacetingDefinition facetingDefinition = FacetingDefinition.builder()
      .setClassType(STATION_TYPE.getValue())
      .setPopulated(true)
      .setFacetingDefinitions(Map.of(
        CHANNELS_KEY.getValue(),
        getFacetingDefinition(CHANNEL_TYPE.getValue(), false)))
      .build();

    final Station result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(data.getName(), result.getName());
    assertTrue(result.getAllRawChannels().stream().noneMatch(Channel::isPresent));
  }

  @Test
  void testStation_populateFacets_fromPopulatedEntity_toSelf_nullFacetDefinition() {
    Station data = UtilsTestFixtures.STATION_FACET;

    assertThrows(NullPointerException.class,
      () -> stationDefinitionFacetingUtility
        .populateFacets(data, null, EFFECTIVE_TIME));
  }

  @Test
  void testStation_populateFacets_fromEntityReference_toSelf_nullFacetDefinition() {
    Station data = UtilsTestFixtures.STATION.toEntityReference();

    assertThrows(NullPointerException.class,
      () -> stationDefinitionFacetingUtility
        .populateFacets(data, null, EFFECTIVE_TIME));
  }

  @Test
  void testStation_populateFacets_removeChannelGroup() {
    ChannelGroup channelGroup1 = ChannelGroup.builder()
      .setName("channelGroup1")
      .setEffectiveAt(Instant.EPOCH)
      .setData(ChannelGroup.Data.builder()
        .setDescription("Channel Group1")
        .setType(ChannelGroup.ChannelGroupType.PHYSICAL_SITE)
        .setLocation(Location.from(1.0, 1.0, 1.0, 1.0))
        .setChannels(List.of(CHANNEL.toEntityReference()))
        .build())
      .build();

    ChannelGroup channelGroup2 = ChannelGroup.builder()
      .setName("channelGroup2")
      .setEffectiveAt(Instant.EPOCH)
      .setData(ChannelGroup.Data.builder()
        .setDescription("Channel Group 2")
        .setType(ChannelGroup.ChannelGroupType.PHYSICAL_SITE)
        .setLocation(Location.from(2.0, 2.0, 2.0, 2.0))
        .setChannels(List.of(CHANNEL_TWO.toEntityReference()))
        .build())
      .build();
    Station station = STATION.toBuilder()
      .setData(STATION.getData().orElseThrow().toBuilder()
        .setChannelGroups(List.of(channelGroup1, channelGroup2))
        .setAllRawChannels(List.of(CHANNEL.toEntityReference(), CHANNEL_TWO.toEntityReference()))
        .build())
      .build();

    FacetingDefinition facetingDefinition = FacetingDefinition.builder()
      .setClassType(STATION_TYPE.getValue())
      .setPopulated(true)
      .addFacetingDefinitions(CHANNELS_KEY.getValue(),
        FacetingDefinition.builder()
          .setClassType(CHANNEL_TYPE.getValue())
          .setPopulated(false)
          .build())
      .addFacetingDefinitions(CHANNEL_GROUPS_KEY.getValue(),
        FacetingDefinition.builder()
          .setClassType(CHANNEL_GROUP_TYPE.getValue())
          .setPopulated(true)
          .addFacetingDefinitions(CHANNELS_KEY.getValue(),
            FacetingDefinition.builder()
              .setClassType(CHANNEL_TYPE.getValue())
              .setPopulated(true)
              .addFacetingDefinitions(RESPONSES_KEY.getValue(),
                FacetingDefinition.builder()
                  .setClassType(RESPONSE_TYPE.getValue())
                  .setPopulated(false)
                  .build())
              .build())
          .build())
      .build();

    doReturn(List.of())
      .when(stationDefinitionAccessor).findChannelsByNameAndTime(List.of(CHANNEL.getName()), Instant.EPOCH);
    doReturn(List.of(CHANNEL_TWO))
      .when(stationDefinitionAccessor).findChannelsByNameAndTime(List.of(CHANNEL_TWO.getName()), Instant.EPOCH);

    Map<Channel, RelativePosition> expectedRelativePositionsByChannel = new HashMap<>(station.getRelativePositionsByChannel());
    expectedRelativePositionsByChannel.remove(CHANNEL.toEntityReference());

    List<RelativePositionChannelPair> expectedRelativePositionChannelPairs = station.getRelativePositionChannelPairs()
      .stream()
      .filter(pair -> !pair.getChannel().equals(CHANNEL))
      .collect(Collectors.toList());

    Station expected = station.toBuilder()
      .setData(station.getData().orElseThrow().toBuilder()
        .setChannelGroups(List.of(channelGroup2))
        .setAllRawChannels(List.of(CHANNEL_TWO.toEntityReference()))
        .setRelativePositionChannelPairs(expectedRelativePositionChannelPairs)
        .build())
      .build();

    Station actual = stationDefinitionFacetingUtility.populateFacets(station, facetingDefinition, Instant.EPOCH);
    assertEquals(expected, actual);
  }

  @Test
  void testStation_populateFacets_removeChannel() {
    ChannelGroup channelGroup1 = ChannelGroup.builder()
      .setName("channelGroup1")
      .setEffectiveAt(Instant.EPOCH)
      .setData(ChannelGroup.Data.builder()
        .setDescription("Channel Group1")
        .setType(ChannelGroup.ChannelGroupType.PHYSICAL_SITE)
        .setLocation(Location.from(1.0, 1.0, 1.0, 1.0))
        .setChannels(List.of(CHANNEL.toEntityReference()))
        .build())
      .build();

    ChannelGroup channelGroup2 = ChannelGroup.builder()
      .setName("channelGroup2")
      .setEffectiveAt(Instant.EPOCH)
      .setData(ChannelGroup.Data.builder()
        .setDescription("Channel Group 2")
        .setType(ChannelGroup.ChannelGroupType.PHYSICAL_SITE)
        .setLocation(Location.from(2.0, 2.0, 2.0, 2.0))
        .setChannels(List.of(CHANNEL_TWO.toEntityReference()))
        .build())
      .build();
    Station station = STATION.toBuilder()
      .setData(STATION.getData().orElseThrow().toBuilder()
        .setChannelGroups(List.of(channelGroup1, channelGroup2))
        .setAllRawChannels(List.of(CHANNEL.toEntityReference(), CHANNEL_TWO.toEntityReference()))
        .build())
      .build();

    FacetingDefinition facetingDefinition = FacetingDefinition.builder()
      .setClassType(STATION_TYPE.getValue())
      .setPopulated(true)
      .addFacetingDefinitions(CHANNELS_KEY.getValue(),
        FacetingDefinition.builder()
          .setClassType(CHANNEL_TYPE.getValue())
          .setPopulated(true)
          .addFacetingDefinitions(RESPONSES_KEY.getValue(),
            FacetingDefinition.builder()
              .setClassType(RESPONSE_TYPE.getValue())
              .setPopulated(false)
              .build())
          .build())
      .addFacetingDefinitions(CHANNEL_GROUPS_KEY.getValue(),
        FacetingDefinition.builder()
          .setClassType(CHANNEL_GROUP_TYPE.getValue())
          .setPopulated(true)
          .addFacetingDefinitions(CHANNELS_KEY.getValue(),
            FacetingDefinition.builder()
              .setClassType(CHANNEL_TYPE.getValue())
              .setPopulated(false)
              .build())
          .build())
      .build();

    doReturn(List.of())
      .when(stationDefinitionAccessor).findChannelsByNameAndTime(List.of(CHANNEL.getName()), Instant.EPOCH);
    doReturn(List.of(CHANNEL_TWO))
      .when(stationDefinitionAccessor).findChannelsByNameAndTime(List.of(CHANNEL_TWO.getName()), Instant.EPOCH);

    Map<Channel, RelativePosition> expectedRelativePositionsByChannel = new HashMap<>(station.getRelativePositionsByChannel());
    expectedRelativePositionsByChannel.remove(CHANNEL.toEntityReference());

    List<RelativePositionChannelPair> expectedRelativePositionChannelPairs = station.getRelativePositionChannelPairs()
      .stream()
      .filter(pair -> !pair.getChannel().equals(CHANNEL))
      .collect(Collectors.toList());

    Station expected = station.toBuilder()
      .setData(station.getData().orElseThrow().toBuilder()
        .setChannelGroups(List.of(channelGroup2))
        .setAllRawChannels(List.of(CHANNEL_TWO))
        .setRelativePositionChannelPairs(expectedRelativePositionChannelPairs)
        .build())
      .build();

    Station actual = stationDefinitionFacetingUtility.populateFacets(station, facetingDefinition, Instant.EPOCH);
    assertEquals(expected, actual);
  }

  //CHANNEL GROUP
  @Test
  void testChannelGroup_populateFacets_null_throwsError() {
    final FacetingDefinition facetingDefinition = getFacetingDefinition("channelGroups", true);

    assertThrows(NullPointerException.class,
      () -> stationDefinitionFacetingUtility.populateFacets((ChannelGroup) null,
        facetingDefinition,
        EFFECTIVE_TIME));
  }

  @Test
  void testChannelGroup_populateFacets_fromPopulatedEntity_toEntityReference() {
    ChannelGroup data = UtilsTestFixtures.CHANNEL_GROUP_FACET;
    final FacetingDefinition facetingDefinition = getFacetingDefinition(CHANNEL_GROUP_TYPE.getValue(), false);

    final ChannelGroup result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertFalse(result.isPresent());
    assertEquals(data.getName(), result.getName());
  }

  @Test
  void testChannelGroup_populateFacets_fromEntityReference_toEntityReference() {
    ChannelGroup data = UtilsTestFixtures.CHANNEL_GROUP_FACET.toEntityReference();
    final FacetingDefinition facetingDefinition = getFacetingDefinition(CHANNEL_GROUP_TYPE.getValue(), false);

    final ChannelGroup result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertFalse(result.isPresent());
    assertEquals(data.getName(), result.getName());
  }

  @Test
  void testChannelGroup_populateFaceets_fromEntityReferece_toPopulatedChannelGroup() {
    ChannelGroup data = UtilsTestFixtures.CHANNEL_GROUP_FACET
      .toEntityReference()
      .toBuilder()
      .setEffectiveAt(Instant.now())
      .build();

    mockBridgedChannelGroupRepositoryTimeRequestInteraction();
    final FacetingDefinition facetingDefinition = getFacetingDefinition(CHANNEL_GROUP_TYPE.getValue(), true);
    final ChannelGroup result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition,
        EFFECTIVE_TIME);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(data.getName(), result.getName());
    assertTrue(result.getChannels().stream().noneMatch(Channel::isPresent));
  }

  @Test
  void testChannelGroup_populateFacets_fromPopulatedEntity_toSelf_noFacetDefinitionForChannels() {
    ChannelGroup data = UtilsTestFixtures.CHANNEL_GROUP_FACET;
    final FacetingDefinition facetingDefinition = getFacetingDefinition(CHANNEL_GROUP_TYPE.getValue(), true);

    final ChannelGroup result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(data.getName(), result.getName());
    assertTrue(result.getChannels().stream().noneMatch(Channel::isPresent));
  }

  @Test
  void testChannelGroup_populateFacets_fromPopulatedEntity_toSelf_populatedFacetDefinitionForChannels() {
    ChannelGroup data = UtilsTestFixtures.CHANNEL_GROUP_FACET;
    final FacetingDefinition facetingDefinition = FacetingDefinition.builder()
      .setClassType(CHANNEL_GROUP_TYPE.getValue())
      .setPopulated(true)
      .setFacetingDefinitions(Map.of(
        CHANNELS_KEY.getValue(),
        getFacetingDefinition(CHANNEL_TYPE.getValue(), true)))
      .build();

    mockBridgedChannelRepositoryTimeRequestInteraction();
    final ChannelGroup result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(data.getName(), result.getName());
    assertTrue(result.getChannels().stream().allMatch(Channel::isPresent));
  }

  @Test
  void testChannelGroup_populateFacets_fromPopulatedEntity_toSelf_notPopulatedFacetDefinitionForChannels() {
    ChannelGroup data = UtilsTestFixtures.CHANNEL_GROUP_FACET;
    final FacetingDefinition facetingDefinition = FacetingDefinition.builder()
      .setClassType(CHANNEL_GROUP_TYPE.getValue())
      .setPopulated(true)
      .setFacetingDefinitions(Map.of(
        CHANNELS_KEY.getValue(),
        getFacetingDefinition(CHANNEL_TYPE.getValue(), false)))
      .build();

    final ChannelGroup result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(data.getName(), result.getName());
    assertTrue(result.getChannels().stream().noneMatch(Channel::isPresent));
  }

  @Test
  void testChannelGroup_populateFacets_fromPopulatedEntity_toSelf_nullFacetDefinition() {
    ChannelGroup data = UtilsTestFixtures.CHANNEL_GROUP;

    assertThrows(NullPointerException.class,
      () -> stationDefinitionFacetingUtility.populateFacets(data, null, EFFECTIVE_TIME));
  }

  @Test
  void testChannelGroup_populateFacets_fromEntityReference_toSelf_nullFacetDefinition() {
    ChannelGroup data = UtilsTestFixtures.CHANNEL_GROUP.toEntityReference();
    final FacetingDefinition facetingDefinition = null;

    assertThrows(NullPointerException.class,
      () -> stationDefinitionFacetingUtility.populateFacets(data, null, EFFECTIVE_TIME));
  }

  //CHANNEL
  @Test
  void testChannel_populateFacets_null_throwsError() {
    final FacetingDefinition facetingDefinition = getFacetingDefinition(CHANNEL_TYPE.getValue(), true);

    assertThrows(NullPointerException.class,
      () -> stationDefinitionFacetingUtility.populateFacets((Channel) null,
        facetingDefinition,
        EFFECTIVE_TIME));
  }

  @Test
  void testChannel_populateFacets_fromPopulatedEntity_toVersionReference() {
    Channel data = UtilsTestFixtures.CHANNEL_FACET;
    final FacetingDefinition facetingDefinition = getFacetingDefinition(CHANNEL_TYPE.getValue(), false);

    final Channel result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertFalse(result.isPresent());
    assertEquals(data.getName(), result.getName());
  }

  @Test
  void testChannel_populateFacets_fromEntityReference_toVersionReference() {
    Channel data = Channel.createVersionReference(UtilsTestFixtures.CHANNEL_FACET.getName(), UtilsTestFixtures
      .CHANNEL_FACET.getEffectiveAt().orElseThrow());
    final FacetingDefinition facetingDefinition = getFacetingDefinition(CHANNEL_TYPE.getValue(), false);

    final Channel result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertFalse(result.isPresent());
    assertEquals(data.getName(), result.getName());
  }

  @Test
  void testChannel_populateFacets_fromEntityReferece_toPopulatedChannel() {
    Channel data = Channel.createVersionReference(UtilsTestFixtures.CHANNEL_FACET.getName(), UtilsTestFixtures
        .CHANNEL_FACET.getEffectiveAt().orElseThrow())
      .toEntityReference()
      .toBuilder()
      .setEffectiveAt(Instant.now())
      .build();

    mockBridgedChannelRepositoryTimeRequestInteraction();
    final FacetingDefinition facetingDefinition = getFacetingDefinition(CHANNEL_TYPE.getValue(), true);
    final Channel result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);
    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(UtilsTestFixtures.CHANNEL, result);
    assertEquals(data.getName(), result.getName());
  }

  @Test
  void testChannel_populateFacets_fromPopulatedEntity_toSelf() {
    Channel data = UtilsTestFixtures.CHANNEL_FACET;
    final FacetingDefinition facetingDefinition = getFacetingDefinition(CHANNEL_TYPE.getValue(), true);

    mockBridgedChannelRepositoryTimeRequestInteraction();
    final Channel result = stationDefinitionFacetingUtility
      .populateFacets(data, facetingDefinition, EFFECTIVE_TIME);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(data.getName(), result.getName());
  }

  @Test
  void testChannel_populateFacets_fromPopulatedEntity_toSelf_nullFacetDefinition() {
    Channel data = UtilsTestFixtures.CHANNEL;

    assertThrows(NullPointerException.class, () -> stationDefinitionFacetingUtility
      .populateFacets(data, null, EFFECTIVE_TIME));
  }

  @Test
  void testChannel_populateFacets_fromEntityReference_toSelf_nullFacetDefinition() {
    Channel data = UtilsTestFixtures.CHANNEL.toEntityReference();

    assertThrows(NullPointerException.class,
      () -> stationDefinitionFacetingUtility.populateFacets(data, null, EFFECTIVE_TIME));
  }

  // RESPONSE

  @ParameterizedTest
  @MethodSource("getPopulateResponseFacetValidationArguments")
  void testPopulateResponseFacetValidation(Class<? extends Exception> expectedException,
    Response initial,
    FacetingDefinition facetingDefinition,
    Instant effectiveTime) {

    assertThrows(expectedException,
      () -> stationDefinitionFacetingUtility.populateFacets(initial,
        facetingDefinition,
        effectiveTime));
  }

  static Stream<Arguments> getPopulateResponseFacetValidationArguments() {
    return Stream.of(
      arguments(NullPointerException.class,
        null,
        getFacetingDefinition(RESPONSE_TYPE.getValue(), true),
        EFFECTIVE_TIME),
      arguments(NullPointerException.class,
        RESPONSE,
        null,
        EFFECTIVE_TIME),
      arguments(NullPointerException.class,
        RESPONSE,
        getFacetingDefinition(RESPONSE_TYPE.getValue(), true),
        null),
      arguments(IllegalStateException.class,
        RESPONSE,
        getFacetingDefinition(STATION_TYPE.getValue(), true),
        EFFECTIVE_TIME),
      arguments(NoSuchElementException.class,
        RESPONSE.toBuilder().setEffectiveAt(Optional.empty()).setData(Optional.empty()).build(),
        getFacetingDefinition(RESPONSE_TYPE.getValue(), false),
        EFFECTIVE_TIME));
  }

  @ParameterizedTest
  @MethodSource("getPopulateResponseFacetsArguments")
  void testPopulateResponseFacets(Consumer<StationDefinitionAccessorInterface> setUpMocks,
    Response initial,
    FacetingDefinition facetingDefinition,
    Response expected,
    Consumer<StationDefinitionAccessorInterface> validateMocks) {
    setUpMocks.accept(stationDefinitionAccessor);
    Response actual = stationDefinitionFacetingUtility.populateFacets(initial,
      facetingDefinition,
      EFFECTIVE_TIME);
    assertEquals(expected, actual);
    validateMocks.accept(stationDefinitionAccessor);
  }

  static Stream<Arguments> getPopulateResponseFacetsArguments() {
    Consumer<StationDefinitionAccessorInterface> emptyToFullSetup = stationDefinitionAccessorInterface ->
      when(stationDefinitionAccessorInterface.findResponsesById(List.of(RESPONSE.getId()), EFFECTIVE_TIME))
        .thenReturn(List.of(RESPONSE));
    Consumer<StationDefinitionAccessorInterface> emptyToFullValidation = stationDefinitionAccessorInterface ->
      verify(stationDefinitionAccessorInterface, times(1))
        .findResponsesById(List.of(RESPONSE.getId()), EFFECTIVE_TIME);

    Consumer<StationDefinitionAccessorInterface> noOpSetupValidation = stationDefinitionAccessorInterface -> {
    };

    return Stream.of(
      arguments(emptyToFullSetup,
        RESPONSE.toBuilder().setData(Optional.empty()).build(),
        getFacetingDefinition(RESPONSE_TYPE.getValue(), true),
        RESPONSE,
        emptyToFullValidation),
      arguments(noOpSetupValidation,
        RESPONSE_FULL,
        getFacetingDefinition(RESPONSE_TYPE.getValue(), true),
        RESPONSE_FULL,
        noOpSetupValidation),
      arguments(noOpSetupValidation,
        RESPONSE_FULL,
        getFacetingDefinition(RESPONSE_TYPE.getValue(), false),
        RESPONSE_FULL.toBuilder().setData(Optional.empty()).build(),
        noOpSetupValidation));
  }

  private void assertStationGroupAndStationsPopulated(StationGroup stationGroup) {
    stationGroup.getStations().forEach(station -> {
      assertTrue(station.getChannelGroups().stream().allMatch(ChannelGroup::isPresent));
      station.getChannelGroups().forEach(channelGroup ->
        assertTrue(channelGroup.getChannels().stream().noneMatch(Channel::isPresent))
      );
      assertTrue(station.getAllRawChannels().stream().noneMatch(Channel::isPresent));
    });

  }

  public static FacetingDefinition getFacetingDefinition(String classType, boolean populated) {
    return FacetingDefinition.builder()
      .setClassType(classType)
      .setPopulated(populated)
      .build();
  }

  public void mockBridgedStationRepositoryTimeRequestInteraction() {
    when(stationDefinitionAccessor.findStationsByNameAndTime(any(), any())).thenReturn(
      List.of(UtilsTestFixtures.STATION_FACET)
    );
  }

  public void mockBridgedChannelRepositoryTimeRequestInteraction() {
    when(stationDefinitionAccessor.findChannelsByNameAndTime(any(), any()))
      .thenReturn(List.of(UtilsTestFixtures.CHANNEL)).thenReturn(List.of(CHANNEL_TWO));
  }

  public void mockBridgedChannelRepositorySingleTimeRequestInteraction() {
    when(stationDefinitionAccessor.findChannelsByNameAndTime(any(), any()))
      .thenReturn(List.of(UtilsTestFixtures.CHANNEL)).thenReturn(List.of());
  }

  public void mockBridgedChannelGroupRepositoryTimeRequestInteraction() {
    when(stationDefinitionAccessor.findChannelGroupsByNameAndTime(any(), any())).thenReturn(
      List.of(UtilsTestFixtures.CHANNEL_GROUP_FACET)
    );
  }
}
