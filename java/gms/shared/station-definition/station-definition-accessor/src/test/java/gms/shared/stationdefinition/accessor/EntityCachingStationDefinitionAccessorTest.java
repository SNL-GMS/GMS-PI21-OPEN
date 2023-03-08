package gms.shared.stationdefinition.accessor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.stationdefinition.api.StationDefinitionAccessorInterface;
import gms.shared.stationdefinition.api.station.util.StationChangeTimesRequest;
import gms.shared.stationdefinition.cache.VersionCache;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelGroup;
import gms.shared.stationdefinition.coi.channel.Response;
import gms.shared.stationdefinition.coi.facets.FacetingDefinition;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.coi.station.StationGroup;
import gms.shared.stationdefinition.dao.css.enums.TagName;
import gms.shared.stationdefinition.facet.StationDefinitionFacetingUtility;
import gms.shared.stationdefinition.repository.util.StationDefinitionIdUtility;
import gms.shared.stationdefinition.testfixtures.UtilsTestFixtures;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static gms.shared.stationdefinition.facet.FacetingTypes.CHANNEL_GROUP_TYPE;
import static gms.shared.stationdefinition.facet.FacetingTypes.CHANNEL_TYPE;
import static gms.shared.stationdefinition.facet.FacetingTypes.RESPONSE_TYPE;
import static gms.shared.stationdefinition.facet.FacetingTypes.STATION_GROUP_TYPE;
import static gms.shared.stationdefinition.facet.FacetingTypes.STATION_TYPE;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL_FACET;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL_GROUP;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL_GROUP_TEST;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL_TWO;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.RESPONSE;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.RESPONSE_FACET;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.RESPONSE_FULL;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.RESPONSE_ONE;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.RESPONSE_TWO;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.STATION;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.STATION_2;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.STATION_CHANGE_TIMES_REQUEST_200s;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.STATION_GROUP;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.STATION_GROUP2;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntityCachingStationDefinitionAccessorTest {
  public static final String TEST_NAME = "COI Test Name";
  public static final UUID TEST_UUID = UUID.nameUUIDFromBytes("COI_UUID".getBytes());
  public static final Instant START_TIME = Instant.EPOCH;
  public static final Instant END_TIME = Instant.EPOCH.plusSeconds(30);
  public static final Instant END_TIME2 = Instant.EPOCH.plusSeconds(15);

  @Mock
  private StationDefinitionAccessorInterface delegate;
  @Mock
  private SystemConfig systemConfig;
  @Mock
  private ConfigurationConsumerUtility configurationConsumerUtility;
  @Mock
  private StationDefinitionIdUtility stationDefinitionIdUtility;
  @Mock
  private VersionCache cache;
  @Captor
  private ArgumentCaptor<NavigableSet<Instant>> rangeSetInstantCaptor;
  @Captor
  private ArgumentCaptor<RangeMap<Instant, Object>> rangeMapInstantObjectCaptor;
  @Captor
  private ArgumentCaptor<Instant> startTimeCaptor;
  @Captor
  private ArgumentCaptor<Instant> endTimeCaptor;
  @Captor
  private ArgumentCaptor<Range<Instant>> timeRangeCaptor;
  @Captor
  private ArgumentCaptor<String> entityIdCaptor;
  @Captor
  private ArgumentCaptor<List<String>> namesCaptor;
  @Captor
  private ArgumentCaptor<List<UUID>> uuidCaptor;
  @Captor
  private ArgumentCaptor<FacetingDefinition> facetCaptor;
  @Captor
  private ArgumentCaptor<List<StationGroup>> stationGroupListCaptor;
  @Captor
  private ArgumentCaptor<List<Station>> stationListCaptor;
  @Captor
  private ArgumentCaptor<List<ChannelGroup>> channelGroupListCaptor;
  @Captor
  private ArgumentCaptor<List<Channel>> channelListCaptor;
  NavigableSet<Instant> rangeSet = new TreeSet<>();

  StationDefinitionAccessorInterface entityCacheAccessor;

  @BeforeEach
  public void beforeEach() {
    entityCacheAccessor = new EntityCachingStationDefinitionAccessor(
      mock(SystemConfig.class),
      configurationConsumerUtility,
      delegate,
      cache,
      stationDefinitionIdUtility);
    ReflectionTestUtils.setField(entityCacheAccessor, "operationalRange",
      new AtomicReference(Range.closed(Instant.MIN, Instant.MAX)));
    ReflectionTestUtils.setField(entityCacheAccessor, "stationDefinitionFacetingUtility",
      StationDefinitionFacetingUtility.create(entityCacheAccessor));
  }

  /* ------------------------------
   * StationGroup Caching Tests
   -------------------------------- */
  @Test
  void findStationGroupsByNameAndTimeHitTest() {

    String stationGroupKey = StationGroup.class.getSimpleName().concat(TEST_NAME);
    when(cache.versionsByEntityIdAndTimeHasKey(stationGroupKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(stationGroupKey), any())).thenReturn(STATION_GROUP);

    // mocking station retrieval
    // TODO: sgk 4/30/2021 extract to helper method?
    String stationKey = Station.class.getSimpleName().concat(STATION.getName());
    when(cache.versionsByEntityIdAndTimeHasKey(stationKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(stationKey), any()))
      .thenReturn(STATION);

    // mocking channel group retrieval
    String channelGroupKey = ChannelGroup.class.getSimpleName().concat(CHANNEL_GROUP.getName());
    when(cache.versionsByEntityIdAndTimeHasKey(channelGroupKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(channelGroupKey), any())).thenReturn(CHANNEL_GROUP);

    // mocking channel retrieval
    String channel1Key = Channel.class.getSimpleName().concat(CHANNEL.getName());
    when(cache.versionsByEntityIdAndTimeHasKey(channel1Key)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(channel1Key), any())).thenReturn(CHANNEL);

    String channel2Key = Channel.class.getSimpleName().concat(CHANNEL_TWO.getName());
    when(cache.versionsByEntityIdAndTimeHasKey(channel2Key)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(channel2Key), any())).thenReturn(CHANNEL_TWO);

    // mock responses
    Response response1 = CHANNEL.getResponse().orElseThrow();
    String response1Key = Response.class.getSimpleName().concat(response1.getId().toString());
    when(cache.versionsByEntityIdAndTimeHasKey(response1Key)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(response1Key), any())).thenReturn(response1);

    Response response2 = CHANNEL_TWO.getResponse().orElseThrow();
    String response2Key = Response.class.getSimpleName().concat(response2.getId().toString());
    when(cache.versionsByEntityIdAndTimeHasKey(response2Key)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(response2Key), any())).thenReturn(response2);
    //Instant.EPOCH, Instant.EPOCH.plusSeconds(60 * 5)
    List<StationGroup> stationGroups = entityCacheAccessor.findStationGroupsByNameAndTime(List.of(TEST_NAME),
      START_TIME);

    // verify cache was called and response is correct
    verify(cache, times(11)).retrieveVersionsByEntityIdAndTime(entityIdCaptor.capture(), startTimeCaptor.capture());
    assertFalse(stationGroups.isEmpty());
    assertEquals(STATION_GROUP, stationGroups.get(0));
  }

  @Test
  void findStationGroupsByNameAndTimeMissTest() {
    when(delegate.findStationGroupsByNameAndTime(any(), any())).thenReturn(List.of(STATION_GROUP));
    when(delegate.findStationsByNameAndTime(any(), any())).thenReturn(List.of(STATION));
    when(delegate.findChannelGroupsByNameAndTime(any(), any())).thenReturn(List.of(CHANNEL_GROUP));
    when(delegate.findChannelsByNameAndTime(any(), any())).thenReturn(List.of(CHANNEL, CHANNEL_TWO));

    List<StationGroup> response = entityCacheAccessor.findStationGroupsByNameAndTime(List.of(TEST_NAME),
      START_TIME);

    //check the delegate call is correct
    verify(delegate).findStationGroupsByNameAndTime(namesCaptor.capture(),
      startTimeCaptor.capture());
    assertEquals(START_TIME, startTimeCaptor.getValue());
    assertEquals(TEST_NAME, namesCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(STATION_GROUP, response.get(0));
  }

  @Test
  void findStationGroupsByNameAndTimeFacetHitTest() {

    final FacetingDefinition facetingDefinition = getFacetingDefinition(STATION_GROUP_TYPE.getValue(), true);

    String stationGroupKey = StationGroup.class.getSimpleName().concat(TEST_NAME);
    when(cache.versionsByEntityIdAndTimeHasKey(stationGroupKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(stationGroupKey), any())).thenReturn(STATION_GROUP);

    // mock stations
    String stationKey = Station.class.getSimpleName().concat(STATION.getName());
    when(cache.versionsByEntityIdAndTimeHasKey(stationKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(stationKey), any())).thenReturn(STATION);

    // mock channel groups
    String channelGroupKey = ChannelGroup.class.getSimpleName().concat(CHANNEL_GROUP.getName());
    when(cache.versionsByEntityIdAndTimeHasKey(channelGroupKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(channelGroupKey), any())).thenReturn(CHANNEL_GROUP);

    // mock channels
    String channel1Key = Channel.class.getSimpleName().concat(CHANNEL.getName());
    when(cache.versionsByEntityIdAndTimeHasKey(channel1Key)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(channel1Key), any())).thenReturn(CHANNEL);

    String channel2Key = Channel.class.getSimpleName().concat(CHANNEL_TWO.getName());
    when(cache.versionsByEntityIdAndTimeHasKey(channel2Key)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(channel2Key), any())).thenReturn(CHANNEL_TWO);

    // mock responses
    Response response1 = CHANNEL.getResponse().orElseThrow();
    String response1Key = Response.class.getSimpleName().concat(response1.getId().toString());
    when(cache.versionsByEntityIdAndTimeHasKey(response1Key)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(response1Key), any())).thenReturn(response1);

    Response response2 = CHANNEL_TWO.getResponse().orElseThrow();
    String response2Key = Response.class.getSimpleName().concat(response2.getId().toString());
    when(cache.versionsByEntityIdAndTimeHasKey(response2Key)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(response2Key), any())).thenReturn(response2);

    List<StationGroup> stationGroups = entityCacheAccessor.findStationGroupsByNameAndTime(List.of(TEST_NAME),
      START_TIME, facetingDefinition);

    // verify cache was called and response is correct
    verify(cache, times(11)).retrieveVersionsByEntityIdAndTime(entityIdCaptor.capture(), startTimeCaptor.capture());
    assertFalse(stationGroups.isEmpty());
    assertEquals(STATION_GROUP, stationGroups.get(0));
  }

  @Test
  void findStationGroupsByNameAndTimeFacetMissTest() {
    final FacetingDefinition facetingDefinition = getFacetingDefinition(STATION_GROUP_TYPE.getValue(), false);

    when(delegate.findStationGroupsByNameAndTime(any(), any())).thenReturn(List.of(STATION_GROUP));
    when(delegate.findStationsByNameAndTime(any(), any())).thenReturn(List.of(STATION));
    when(delegate.findChannelGroupsByNameAndTime(any(), any())).thenReturn(List.of(CHANNEL_GROUP));
    when(delegate.findChannelsByNameAndTime(any(), any())).thenReturn(List.of(CHANNEL, CHANNEL_TWO));

    List<StationGroup> response = entityCacheAccessor.findStationGroupsByNameAndTime(List.of(TEST_NAME),
      START_TIME, facetingDefinition);

    //check the delegate call is correct
    verify(delegate).findStationGroupsByNameAndTime(namesCaptor.capture(),
      startTimeCaptor.capture());
    assertEquals(START_TIME, startTimeCaptor.getValue());
    assertEquals(TEST_NAME, namesCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(STATION_GROUP.toBuilder().setData(Optional.empty()).build(), response.get(0));
  }

  @Test
  void findStationGroupsByNameAndTimeRangeHitTest() {
    rangeSet.addAll(List.of(START_TIME, END_TIME));

    RangeMap<Instant, Object> rangeMap = TreeRangeMap.create();
    rangeMap.put(Range.closed(START_TIME, END_TIME), STATION_GROUP);
    when(cache.retrieveVersionsByEntityIdAndTimeRangeMap(any())).thenReturn(rangeMap);

    List<StationGroup> response = entityCacheAccessor.findStationGroupsByNameAndTimeRange(List.of(TEST_NAME),
      START_TIME, END_TIME);

    // verify cache was called and response is correct
    verify(cache).retrieveVersionsByEntityIdAndTimeRangeMap(entityIdCaptor.capture());
    assertFalse(response.isEmpty());
    assertEquals(STATION_GROUP, response.get(0));
  }

  @Test
  void findStationGroupsByNameAndTimeRangeMissTest() {
    rangeSet.addAll(List.of(START_TIME, END_TIME2));

    RangeMap<Instant, Object> rangeMap = TreeRangeMap.create();
    rangeMap.put(Range.closed(START_TIME, END_TIME2), STATION_GROUP);
    when(cache.retrieveVersionsByEntityIdAndTimeRangeMap(any())).thenReturn(rangeMap);
    when(delegate.findStationGroupsByNameAndTimeRange(any(), any(), any())).thenReturn(List.of(STATION_GROUP2));

    List<StationGroup> response = entityCacheAccessor.findStationGroupsByNameAndTimeRange(List.of(TEST_NAME),
      START_TIME, END_TIME);

    // verify cache was called and response is correct
    verify(cache, times(1)).retrieveVersionsByEntityIdAndTimeRangeMap(entityIdCaptor.capture());
    assertFalse(response.isEmpty());
    assertTrue(response.contains(STATION_GROUP));
    assertTrue(response.contains(STATION_GROUP2));
  }

  /* ------------------------------
   * Station Caching Tests
   -------------------------------- */

  @Test
  void findStationsByNameAndTimeHitTest() {

    String stationKey = Station.class.getSimpleName().concat(TEST_NAME);
    when(cache.versionsByEntityIdAndTimeHasKey(stationKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(stationKey), any())).thenReturn(STATION);

    String channelGroupKey = ChannelGroup.class.getSimpleName().concat(CHANNEL_GROUP.getName());
    when(cache.versionsByEntityIdAndTimeHasKey(channelGroupKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(channelGroupKey), any())).thenReturn(CHANNEL_GROUP);

    String channel1Key = Channel.class.getSimpleName().concat(CHANNEL.getName());
    when(cache.versionsByEntityIdAndTimeHasKey(channel1Key)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(channel1Key), any())).thenReturn(CHANNEL);

    String channel2Key = Channel.class.getSimpleName().concat(CHANNEL_TWO.getName());
    when(cache.versionsByEntityIdAndTimeHasKey(channel2Key)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(channel2Key), any())).thenReturn(CHANNEL_TWO);

//     mock responses
    Response response1 = CHANNEL.getResponse().orElseThrow();
    String response1Key = Response.class.getSimpleName().concat(response1.getId().toString());
    when(cache.versionsByEntityIdAndTimeHasKey(response1Key)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(response1Key), any())).thenReturn(response1);

    Response response2 = CHANNEL_TWO.getResponse().orElseThrow();
    String response2Key = Response.class.getSimpleName().concat(response2.getId().toString());
    when(cache.versionsByEntityIdAndTimeHasKey(response2Key)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(response2Key), any())).thenReturn(response2);

    List<Station> stations = entityCacheAccessor.findStationsByNameAndTime(List.of(TEST_NAME),
      START_TIME);

    // verify cache was called and response is correct
    verify(cache, times(10)).retrieveVersionsByEntityIdAndTime(entityIdCaptor.capture(), startTimeCaptor.capture());
    assertFalse(stations.isEmpty());
    assertEquals(STATION, stations.get(0));
  }

  @Test
  void findStationsByNameAndTimeMissTest() {
    when(delegate.findStationsByNameAndTime(any(), any())).thenReturn(List.of(STATION));
    when(delegate.findChannelGroupsByNameAndTime(any(), any())).thenReturn(List.of(CHANNEL_GROUP));
    when(delegate.findChannelsByNameAndTime(any(), any())).thenReturn(List.of(CHANNEL, CHANNEL_TWO));

    List<Station> response = entityCacheAccessor.findStationsByNameAndTime(List.of(TEST_NAME),
      START_TIME);

    //check the delegate call is correct
    verify(delegate).findStationsByNameAndTime(namesCaptor.capture(),
      startTimeCaptor.capture());
    assertEquals(START_TIME, startTimeCaptor.getValue());
    assertEquals(TEST_NAME, namesCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(STATION, response.get(0));
  }

  @Test
  void findStationsByNameAndTimeFacetHitTest() throws JsonProcessingException {
    final FacetingDefinition facetingDefinition = getFacetingDefinition(STATION_TYPE.getValue(), true);

    String stationKey = Station.class.getSimpleName().concat(TEST_NAME);
    when(cache.versionsByEntityIdAndTimeHasKey(stationKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(stationKey), any())).thenReturn(STATION);

    // mock channel groups
    String channelGroupKey = ChannelGroup.class.getSimpleName().concat(CHANNEL_GROUP.getName());
    when(cache.versionsByEntityIdAndTimeHasKey(channelGroupKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(channelGroupKey), any())).thenReturn(CHANNEL_GROUP);

    // mock channels
    String channel1Key = Channel.class.getSimpleName().concat(CHANNEL.getName());
    when(cache.versionsByEntityIdAndTimeHasKey(channel1Key)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(channel1Key), any())).thenReturn(CHANNEL);

    String channel2Key = Channel.class.getSimpleName().concat(CHANNEL_TWO.getName());
    when(cache.versionsByEntityIdAndTimeHasKey(channel2Key)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(channel2Key), any())).thenReturn(CHANNEL_TWO);

//     mock responses
    Response response1 = CHANNEL.getResponse().orElseThrow();
    String response1Key = Response.class.getSimpleName().concat(response1.getId().toString());
    when(cache.versionsByEntityIdAndTimeHasKey(response1Key)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(response1Key), any())).thenReturn(response1);

    Response response2 = CHANNEL_TWO.getResponse().orElseThrow();
    String response2Key = Response.class.getSimpleName().concat(response2.getId().toString());
    when(cache.versionsByEntityIdAndTimeHasKey(response2Key)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(response2Key), any())).thenReturn(response2);

    List<Station> stations = entityCacheAccessor.findStationsByNameAndTime(List.of(TEST_NAME),
      START_TIME, facetingDefinition);

    // verify cache was called and response is correct
    verify(cache, times(10)).retrieveVersionsByEntityIdAndTime(entityIdCaptor.capture(), startTimeCaptor.capture());
    assertFalse(stations.isEmpty());
    assertEquals(STATION, stations.get(0));
  }

  @Test
  void findStationsByNameAndTimeFacetMissTest() {
    final FacetingDefinition facetingDefinition = getFacetingDefinition(STATION_TYPE.getValue(), false);

    when(delegate.findStationsByNameAndTime(eq(List.of(TEST_NAME)), any())).thenReturn(List.of(STATION));
    when(delegate.findChannelGroupsByNameAndTime(any(), any())).thenReturn(List.of(CHANNEL_GROUP));
    when(delegate.findChannelsByNameAndTime(any(), any())).thenReturn(List.of(CHANNEL, CHANNEL_TWO));

    List<Station> response = entityCacheAccessor.findStationsByNameAndTime(List.of(TEST_NAME),
      START_TIME, facetingDefinition);

    //check the delegate call is correct
    verify(delegate).findStationsByNameAndTime(namesCaptor.capture(),
      startTimeCaptor.capture());
    assertEquals(START_TIME, startTimeCaptor.getValue());
    assertEquals(TEST_NAME, namesCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(STATION.toBuilder().setData(Optional.empty()).build(), response.get(0));
  }

  @Test
  void findStationsByNameAndTimeRangeHitTest() {
    rangeSet.addAll(List.of(START_TIME, END_TIME));

    RangeMap<Instant, Object> rangeMap = TreeRangeMap.create();
    rangeMap.put(Range.closed(START_TIME, END_TIME), STATION);

    String stationKey = Station.class.getSimpleName().concat(TEST_NAME);
    when(cache.retrieveVersionsByEntityIdAndTimeRangeMap(stationKey)).thenReturn(rangeMap);


    // mock channel groups
    String channelGroupKey = ChannelGroup.class.getSimpleName().concat(CHANNEL_GROUP.getName());
    when(cache.versionsByEntityIdAndTimeHasKey(channelGroupKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(channelGroupKey), any())).thenReturn(CHANNEL_GROUP);

    // mock channels
    String channelKey = Channel.class.getSimpleName().concat(CHANNEL.getName());
    when(cache.versionsByEntityIdAndTimeHasKey(channelKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(channelKey), any())).thenReturn(CHANNEL);

    String channelKey2 = Channel.class.getSimpleName().concat(CHANNEL_TWO.getName());
    when(cache.versionsByEntityIdAndTimeHasKey(channelKey2)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(channelKey2), any())).thenReturn(CHANNEL_TWO);

    // mock responses
    Response response = CHANNEL.getResponse().orElseThrow();
    String responseKey = Response.class.getSimpleName().concat(response.getId().toString());
    when(cache.versionsByEntityIdAndTimeHasKey(responseKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(responseKey), any())).thenReturn(response);

    Response response2 = CHANNEL_TWO.getResponse().orElseThrow();
    String responseKey2 = Response.class.getSimpleName().concat(response2.getId().toString());
    when(cache.versionsByEntityIdAndTimeHasKey(responseKey2)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(responseKey2), any())).thenReturn(response2);

    List<Station> stations = entityCacheAccessor.findStationsByNameAndTimeRange(List.of(TEST_NAME),
      START_TIME, END_TIME);

    // verify cache was called and response is correct
    verify(cache, times(1)).retrieveVersionsByEntityIdAndTimeRangeMap(entityIdCaptor.capture());
    assertFalse(stations.isEmpty());

    Station expectedStation = STATION.toBuilder()
      .setData(STATION.getData().orElseThrow().toBuilder()
        .setChannelGroups(List.of(CHANNEL_GROUP.toBuilder()
          .setData(CHANNEL_GROUP.getData().orElseThrow().toBuilder()
            .setChannels(List.of(CHANNEL.toEntityReference(), CHANNEL_TWO.toEntityReference()))
            .build())
          .build()))
        .setAllRawChannels(List.of(CHANNEL.toEntityReference(), CHANNEL_TWO.toEntityReference()))
        .build())
      .build();
    assertEquals(expectedStation, stations.get(0));
  }

  @Test
  void findStationsByNameAndTimeRangeMissTest() {

    rangeSet.addAll(List.of(START_TIME, END_TIME2));

    when(delegate.findStationsByNameAndTimeRange(List.of(TEST_NAME), START_TIME, END_TIME)).thenReturn(List.of(STATION, STATION_2));

    List<Station> response = entityCacheAccessor.findStationsByNameAndTimeRange(List.of(TEST_NAME),
      START_TIME, END_TIME);

    // verify cache was called and response is correct
    verify(cache, times(1)).retrieveVersionsByEntityIdAndTimeRangeMap(entityIdCaptor.capture());
    assertFalse(response.isEmpty());

    List<Channel> entityReferenceChannels = List.of(CHANNEL.toEntityReference(), CHANNEL_TWO.toEntityReference());
    List<ChannelGroup> facetedChannelGroup = List.of(CHANNEL_GROUP.toBuilder()
      .setData(CHANNEL_GROUP.getData().orElseThrow().toBuilder()
        .setChannels(entityReferenceChannels)
        .build())
      .build());

    assertTrue(response.contains(STATION));
    assertTrue(response.contains(STATION_2));
  }

  @Test
  void determineStationChangeTimesTest() {
    StationChangeTimesRequest changeRequest = UtilsTestFixtures.STATION_CHANGE_TIMES_REQUEST_700s;
    when(delegate.findStationsByNameAndTimeRange(List.of(changeRequest.getStation().getName()),
      changeRequest.getStartTime(), changeRequest.getEndTime()))
      .thenReturn(List.of(STATION.toBuilder().setEffectiveAt(Instant.EPOCH.plusSeconds(100)).build()));

    when(delegate.findChannelGroupsByNameAndTimeRange(List.of(CHANNEL_GROUP.getName()),
      changeRequest.getStartTime(), changeRequest.getEndTime()))
      .thenReturn(List.of(CHANNEL_GROUP.toBuilder().setEffectiveAt(Instant.EPOCH.plusSeconds(200)).build()));

    when(delegate.findChannelsByNameAndTimeRange(List.of(CHANNEL.getName()),
      changeRequest.getStartTime(), changeRequest.getEndTime()))
      .thenReturn(List.of(CHANNEL.toBuilder().setEffectiveAt(Instant.EPOCH.plusSeconds(300)).build()));

    when(delegate.findChannelsByNameAndTimeRange(List.of(CHANNEL_TWO.getName()),
      changeRequest.getStartTime(), changeRequest.getEndTime()))
      .thenReturn(List.of(CHANNEL_TWO.toBuilder().setEffectiveAt(Instant.EPOCH.plusSeconds(400)).build()));

    when(delegate.findResponsesByIdAndTimeRange(List.of(UtilsTestFixtures.getResponse(CHANNEL.getName()).getId()),
      changeRequest.getStartTime(), changeRequest.getEndTime()))
      .thenReturn(List.of(UtilsTestFixtures.getResponse(CHANNEL.getName()).toBuilder().setEffectiveAt(Instant.EPOCH.plusSeconds(600)).build()));

    when(delegate.findResponsesByIdAndTimeRange(List.of(UtilsTestFixtures.getResponse(CHANNEL_TWO.getName()).getId()),
      changeRequest.getStartTime(), changeRequest.getEndTime()))
      .thenReturn(List.of(UtilsTestFixtures.getResponse(CHANNEL_TWO.getName()).toBuilder().setEffectiveAt(Instant.EPOCH.plusSeconds(500)).build()));

    List<Instant> changeTimes = entityCacheAccessor.determineStationChangeTimes(changeRequest.getStation(),
      changeRequest.getStartTime(),
      changeRequest.getEndTime());
    assertEquals(6, changeTimes.size());
    assertTrue(changeTimes.contains(Instant.EPOCH.plusSeconds(100)));
    assertTrue(changeTimes.contains(Instant.EPOCH.plusSeconds(200)));
    assertTrue(changeTimes.contains(Instant.EPOCH.plusSeconds(300)));
    assertTrue(changeTimes.contains(Instant.EPOCH.plusSeconds(400)));
    assertTrue(changeTimes.contains(Instant.EPOCH.plusSeconds(500)));
    assertTrue(changeTimes.contains(Instant.EPOCH.plusSeconds(600)));
  }

  @Test
  void determineStationChangeTimesOutOfRangeTest() {
    StationChangeTimesRequest changeRequest = STATION_CHANGE_TIMES_REQUEST_200s;
    when(delegate.findStationsByNameAndTimeRange(List.of(changeRequest.getStation().getName()),
      changeRequest.getStartTime(), changeRequest.getEndTime()))
      .thenReturn(List.of(STATION.toBuilder().setEffectiveAt(Instant.EPOCH.plusSeconds(100)).build()));

    when(delegate.findChannelGroupsByNameAndTimeRange(List.of(CHANNEL_GROUP.getName()),
      changeRequest.getStartTime(), changeRequest.getEndTime()))
      .thenReturn(List.of(CHANNEL_GROUP.toBuilder().setEffectiveAt(Instant.EPOCH.plusSeconds(200)).build()));

    when(delegate.findChannelsByNameAndTimeRange(List.of(CHANNEL.getName()),
      changeRequest.getStartTime(), changeRequest.getEndTime()))
      .thenReturn(List.of(CHANNEL.toBuilder().setEffectiveAt(Instant.EPOCH.plusSeconds(300)).build()));

    when(delegate.findChannelsByNameAndTimeRange(List.of(CHANNEL_TWO.getName()),
      changeRequest.getStartTime(), changeRequest.getEndTime()))
      .thenReturn(List.of(CHANNEL_TWO.toBuilder().setEffectiveAt(Instant.EPOCH.plusSeconds(400)).build()));

    when(delegate.findResponsesByIdAndTimeRange(List.of(UtilsTestFixtures.getResponse(CHANNEL.getName()).getId()),
      changeRequest.getStartTime(), changeRequest.getEndTime()))
      .thenReturn(List.of(UtilsTestFixtures.getResponse(CHANNEL.getName()).toBuilder().setEffectiveAt(Instant.EPOCH.plusSeconds(600)).build()));

    when(delegate.findResponsesByIdAndTimeRange(List.of(UtilsTestFixtures.getResponse(CHANNEL_TWO.getName()).getId()),
      changeRequest.getStartTime(), changeRequest.getEndTime()))
      .thenReturn(List.of(UtilsTestFixtures.getResponse(CHANNEL_TWO.getName()).toBuilder().setEffectiveAt(Instant.EPOCH.plusSeconds(500)).build()));

    List<Instant> changeTimes = entityCacheAccessor.determineStationChangeTimes(changeRequest.getStation(),
      changeRequest.getStartTime(),
      changeRequest.getEndTime());
    assertEquals(2, changeTimes.size());
    assertTrue(changeTimes.contains(Instant.EPOCH.plusSeconds(100)));
    assertTrue(changeTimes.contains(Instant.EPOCH.plusSeconds(200)));

  }

  //  /* ------------------------------
//   * ChannelGroups Caching Tests
//   -------------------------------- */
//
  @Test
  void findChannelGroupsByNameAndTimeHitTest() {

    rangeSet.addAll(List.of(Instant.now(), Instant.now().plusSeconds(30)));

    String channelGroupKey = ChannelGroup.class.getSimpleName().concat(TEST_NAME);
    when(cache.versionsByEntityIdAndTimeHasKey(channelGroupKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(channelGroupKey), any())).thenReturn(CHANNEL_GROUP);

    // mock channels
    String channelKey = Channel.class.getSimpleName().concat(CHANNEL.getName());
    when(cache.versionsByEntityIdAndTimeHasKey(channelKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(channelKey), any())).thenReturn(CHANNEL);

    String channelKey2 = Channel.class.getSimpleName().concat(CHANNEL_TWO.getName());
    when(cache.versionsByEntityIdAndTimeHasKey(channelKey2)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(channelKey2), any())).thenReturn(CHANNEL_TWO);

    // mock responses
    Response response = CHANNEL.getResponse().orElseThrow();
    String responseKey = Response.class.getSimpleName().concat(response.getId().toString());
    when(cache.versionsByEntityIdAndTimeHasKey(responseKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(responseKey), any())).thenReturn(response);

    Response response2 = CHANNEL_TWO.getResponse().orElseThrow();
    String responseKey2 = Response.class.getSimpleName().concat(response2.getId().toString());
    when(cache.versionsByEntityIdAndTimeHasKey(responseKey2)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(responseKey2), any())).thenReturn(response2);

    List<ChannelGroup> channelGroups = entityCacheAccessor.findChannelGroupsByNameAndTime(List.of(TEST_NAME),
      START_TIME);

    // verify cache was called and response is correct
    verify(cache, times(5)).retrieveVersionsByEntityIdAndTime(entityIdCaptor.capture(), startTimeCaptor.capture());
    assertFalse(channelGroups.isEmpty());
    assertEquals(CHANNEL_GROUP, channelGroups.get(0));
  }

  @Test
  void findChannelGroupsByNameAndTimeMissTest() {

    when(delegate.findChannelGroupsByNameAndTime(any(), any())).thenReturn(List.of(CHANNEL_GROUP));
    when(delegate.findChannelsByNameAndTime(any(), any())).thenReturn(List.of(CHANNEL, CHANNEL_TWO));

    List<ChannelGroup> response = entityCacheAccessor.findChannelGroupsByNameAndTime(List.of(TEST_NAME),
      START_TIME);

    //check the delegate call is correct
    verify(delegate).findChannelGroupsByNameAndTime(namesCaptor.capture(),
      startTimeCaptor.capture());
    assertEquals(START_TIME, startTimeCaptor.getValue());
    assertEquals(TEST_NAME, namesCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(CHANNEL_GROUP, response.get(0));
  }

  @Test
  void findChannelGroupsByNameAndTimeFacetHitTest() {

    rangeSet.addAll(List.of(Instant.now(), Instant.now().plusSeconds(30)));
    final FacetingDefinition facetingDefinition = getFacetingDefinition(CHANNEL_GROUP_TYPE.getValue(), true);

    String channelGroupKey = ChannelGroup.class.getSimpleName().concat(TEST_NAME);
    when(cache.versionsByEntityIdAndTimeHasKey(channelGroupKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(channelGroupKey), any())).thenReturn(CHANNEL_GROUP);

    // mock channels
    String channelKey = Channel.class.getSimpleName().concat(CHANNEL.getName());
    when(cache.versionsByEntityIdAndTimeHasKey(channelKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(channelKey), any())).thenReturn(CHANNEL);

    String channelKey2 = Channel.class.getSimpleName().concat(CHANNEL_TWO.getName());
    when(cache.versionsByEntityIdAndTimeHasKey(channelKey2)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(channelKey2), any())).thenReturn(CHANNEL_TWO);

    // mock response
    Response response = CHANNEL.getResponse().get();
    String responseKey = Response.class.getSimpleName().concat(response.getId().toString());
    when(cache.versionsByEntityIdAndTimeHasKey(responseKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(responseKey), any())).thenReturn(response);

    Response response2 = CHANNEL_TWO.getResponse().get();
    String responseKey2 = Response.class.getSimpleName().concat(response2.getId().toString());
    when(cache.versionsByEntityIdAndTimeHasKey(responseKey2)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(responseKey2), any())).thenReturn(response2);

    List<ChannelGroup> channelGroups = entityCacheAccessor.findChannelGroupsByNameAndTime(List.of(TEST_NAME),
      START_TIME, facetingDefinition);

    // verify cache was called and response is correct
    verify(cache, times(5)).retrieveVersionsByEntityIdAndTime(entityIdCaptor.capture(), startTimeCaptor.capture());
    assertFalse(channelGroups.isEmpty());
    assertEquals(CHANNEL_GROUP, channelGroups.get(0));
  }

  @Test
  void findChannelGroupsByNameAndTimeFacetMissTest() {
    rangeSet.addAll(List.of(Instant.now(), Instant.now().plusSeconds(30)));
    final FacetingDefinition facetingDefinition = getFacetingDefinition(CHANNEL_GROUP_TYPE.getValue(), false);

    when(delegate.findChannelGroupsByNameAndTime(any(), any())).thenReturn(List.of(CHANNEL_GROUP));
    when(delegate.findChannelsByNameAndTime(any(), any())).thenReturn(List.of(CHANNEL, CHANNEL_TWO));

    List<ChannelGroup> response = entityCacheAccessor.findChannelGroupsByNameAndTime(List.of(TEST_NAME),
      START_TIME, facetingDefinition);

    //check the delegate call is correct
    verify(delegate).findChannelGroupsByNameAndTime(namesCaptor.capture(), startTimeCaptor.capture());
    assertEquals(START_TIME, startTimeCaptor.getValue());
    assertEquals(TEST_NAME, namesCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(CHANNEL_GROUP.toBuilder().setData(Optional.empty()).build(), response.get(0));
  }

  @Test
  void findChannelGroupsByNameAndTimeRangeHitTest() {
    rangeSet.addAll(List.of(START_TIME, END_TIME));
    RangeMap<Instant, Object> rangeMap = TreeRangeMap.create();
    rangeMap.put(Range.closed(START_TIME, END_TIME), CHANNEL_GROUP);
    when(cache.retrieveVersionsByEntityIdAndTimeRangeMap(any())).thenReturn(rangeMap);
    List<ChannelGroup> response = entityCacheAccessor.findChannelGroupsByNameAndTimeRange(List.of(CHANNEL_GROUP.getName()),
      START_TIME, END_TIME);

    // verify cache was called and response is correct
    verify(cache).retrieveVersionsByEntityIdAndTimeRangeMap(entityIdCaptor.capture());
    assertFalse(response.isEmpty());
    assertEquals(CHANNEL_GROUP, response.get(0));
  }

  @Test
  void findChannelGroupsByNameAndTimeRangeMissTest() {
    rangeSet.addAll(List.of(START_TIME, END_TIME2));

    RangeMap<Instant, Object> rangeMap = TreeRangeMap.create();
    rangeMap.put(Range.closed(START_TIME, END_TIME2), CHANNEL_GROUP);
    when(cache.retrieveVersionsByEntityIdAndTimeRangeMap(any())).thenReturn(rangeMap);
    when(delegate.findChannelGroupsByNameAndTimeRange(any(), any(), any())).thenReturn(List.of(CHANNEL_GROUP_TEST));
    List<ChannelGroup> response = entityCacheAccessor.findChannelGroupsByNameAndTimeRange(List.of(TEST_NAME),
      START_TIME, END_TIME);

    // verify cache was called and response is correct
    verify(cache).retrieveVersionsByEntityIdAndTimeRangeMap(entityIdCaptor.capture());
    assertFalse(response.isEmpty());
    assertTrue(response.contains(CHANNEL_GROUP));
    assertTrue(response.contains(CHANNEL_GROUP_TEST));
  }

  //  /* ---------------------------
//   * Channels Caching Tests
//   --------------------------- */
//
  @Test
  void findChannelsByNameAndTimeHitTest() {
    // verify cache was called and response is correct
    String channelKey = Channel.class.getSimpleName().concat(TEST_NAME);
    when(cache.versionsByEntityIdAndTimeHasKey(channelKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(channelKey), any())).thenReturn(CHANNEL);

    // mock response
    String responseKey = Response.class.getSimpleName().concat(CHANNEL.getResponse().get().getId().toString());
    when(cache.versionsByEntityIdAndTimeHasKey(responseKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(responseKey), any())).thenReturn(CHANNEL.getResponse().get());
    List<Channel> response = entityCacheAccessor.findChannelsByNameAndTime(List.of(TEST_NAME),
      START_TIME);

    // verify cache was called and response is correct
    verify(cache, times(2)).retrieveVersionsByEntityIdAndTime(entityIdCaptor.capture(), startTimeCaptor.capture());
    assertFalse(response.isEmpty());
    assertEquals(CHANNEL, response.get(0));
  }

  @Test
  void findChannelsByNameAndTimeMissTest() {
    when(delegate.findChannelsByNameAndTime(eq(List.of(TEST_NAME)), any())).thenReturn(List.of(CHANNEL));
    List<Channel> response = entityCacheAccessor.findChannelsByNameAndTime(List.of(TEST_NAME),
      START_TIME);

    //check the delegate call is correct
    verify(delegate).findChannelsByNameAndTime(namesCaptor.capture(),
      startTimeCaptor.capture());
    assertEquals(START_TIME, startTimeCaptor.getValue());
    assertEquals(TEST_NAME, namesCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(CHANNEL, response.get(0));
  }

  @Test
  void findChannelsByNameAndTimeFacet_populated_HitTest() {
    final FacetingDefinition facetingDefinition = getFacetingDefinition(CHANNEL_TYPE.getValue(), true);

    // verify cache was called and response is correct
    String channelKey = Channel.class.getSimpleName().concat(TEST_NAME);
    when(cache.versionsByEntityIdAndTimeHasKey(channelKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(channelKey), any())).thenReturn(CHANNEL);

    Response response = CHANNEL.getResponse().orElseThrow();
    String responseKey = Response.class.getSimpleName().concat(response.getId().toString());
    when(cache.versionsByEntityIdAndTimeHasKey(responseKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(responseKey), any())).thenReturn(response);

    List<Channel> channels = entityCacheAccessor.findChannelsByNameAndTime(List.of(TEST_NAME),
      START_TIME, facetingDefinition);

    // verify cache was called and response is correct
    verify(cache, times(2)).retrieveVersionsByEntityIdAndTime(entityIdCaptor.capture(),
      startTimeCaptor.capture());
    assertFalse(channels.isEmpty());
    assertEquals(CHANNEL, channels.get(0));
  }

  @Test
  void findChannelsByNameAndTimeFacet_unpopulated_HitTest() {
    final FacetingDefinition facetingDefinition = getFacetingDefinition(CHANNEL_TYPE.getValue(), false);

    String channelKey = Channel.class.getSimpleName().concat(TEST_NAME);
    when(cache.versionsByEntityIdAndTimeHasKey(channelKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(channelKey), any())).thenReturn(CHANNEL);

    Response response = CHANNEL.getResponse().orElseThrow();
    String responseKey = Response.class.getSimpleName().concat(response.getId().toString());
    when(cache.versionsByEntityIdAndTimeHasKey(responseKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(responseKey), any())).thenReturn(response);

    List<Channel> channels = entityCacheAccessor.findChannelsByNameAndTime(List.of(TEST_NAME),
      START_TIME, facetingDefinition);

    // verify cache was called and response is correct
    verify(cache, times(2)).retrieveVersionsByEntityIdAndTime(entityIdCaptor.capture(), startTimeCaptor.capture());
    assertFalse(channels.isEmpty());
    assertEquals(CHANNEL_FACET, channels.get(0));
  }

  @Test
  void findChannelsByNameAndTimeFacet_populated_MissTest() {
    final FacetingDefinition facetingDefinition = getFacetingDefinition(CHANNEL_TYPE.getValue(), true);

    when(delegate.findChannelsByNameAndTime(any(), any())).thenReturn(List.of(CHANNEL));
    List<Channel> response = entityCacheAccessor.findChannelsByNameAndTime(List.of(TEST_NAME),
      START_TIME, facetingDefinition);

    //check the delegate call is correct
    verify(delegate).findChannelsByNameAndTime(namesCaptor.capture(), startTimeCaptor.capture());
    assertEquals(START_TIME, startTimeCaptor.getValue());
    assertEquals(TEST_NAME, namesCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(CHANNEL, response.get(0));
  }

  @Test
  void findChannelsByNameAndTimeFacet_unpopulated_MissTest() {
    final FacetingDefinition facetingDefinition = getFacetingDefinition(CHANNEL_TYPE.getValue(), false);

    when(delegate.findChannelsByNameAndTime(any(), any())).thenReturn(List.of(CHANNEL));
    List<Channel> channels = entityCacheAccessor.findChannelsByNameAndTime(List.of(TEST_NAME),
      START_TIME, facetingDefinition);

    //check the delegate call is correct
    verify(delegate).findChannelsByNameAndTime(namesCaptor.capture(), startTimeCaptor.capture());
    assertEquals(START_TIME, startTimeCaptor.getValue());
    assertEquals(TEST_NAME, namesCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(CHANNEL_FACET, channels.get(0));
  }

  @Test
  void findChannelsByNameAndTimeRangeHitTest() {
    rangeSet.addAll(List.of(START_TIME, END_TIME));

    RangeMap<Instant, Object> rangeMap = TreeRangeMap.create();
    rangeMap.put(Range.closed(START_TIME, END_TIME), CHANNEL);
    when(cache.retrieveVersionsByEntityIdAndTimeRangeMap(any())).thenReturn(rangeMap);
    when(cache.retrieveVersionsByEntityIdAndTimeRangeMap(any())).thenReturn(rangeMap);
    List<Channel> response = entityCacheAccessor.findChannelsByNameAndTimeRange(List.of(TEST_NAME),
      START_TIME, END_TIME);

    // verify cache was called and response is correct
    verify(cache, times(1)).retrieveVersionsByEntityIdAndTimeRangeMap(entityIdCaptor.capture());
    assertFalse(response.isEmpty());
    assertEquals(CHANNEL, response.get(0));
  }

  @Test
  void findChannelsByNameAndTimeRangeMissTest() {
    rangeSet.addAll(List.of(START_TIME, END_TIME2));

    RangeMap<Instant, Object> rangeMap = TreeRangeMap.create();
    rangeMap.put(Range.closed(START_TIME, END_TIME2), CHANNEL);
    when(cache.retrieveVersionsByEntityIdAndTimeRangeMap(any())).thenReturn(rangeMap);
    when(delegate.findChannelsByNameAndTimeRange(any(), any(), any())).thenReturn(List.of(CHANNEL_TWO));
    List<Channel> response = entityCacheAccessor.findChannelsByNameAndTimeRange(List.of(TEST_NAME),
      START_TIME, END_TIME);

    // verify cache was called and response is correct
    verify(cache, times(1)).retrieveVersionsByEntityIdAndTimeRangeMap(entityIdCaptor.capture());
    assertFalse(response.isEmpty());
    assertTrue(response.contains(CHANNEL));
    assertTrue(response.contains(CHANNEL_TWO));
  }

  @Test
  void testLoadChannelFromWfdiscMiss() {
    final long WFID = 314;
    final long RECORD_ID = 217;
    final TagName RECORD_TYPE = TagName.ARID;
    final Instant START_TIME_CHAN = END_TIME2;
    final Instant END_TIME_CHAN = END_TIME;

    when(stationDefinitionIdUtility.getDerivedChannelForWfidRecordId(RECORD_TYPE, RECORD_ID, WFID)).thenReturn(null);
    when(delegate.loadChannelFromWfdisc(List.of(WFID),
      Optional.of(RECORD_TYPE), Optional.of(RECORD_ID), Optional.empty(),
      START_TIME_CHAN, END_TIME_CHAN)).thenReturn(CHANNEL_TWO);

    var result = entityCacheAccessor.loadChannelFromWfdisc(List.of(WFID),
      Optional.of(RECORD_TYPE), Optional.of(RECORD_ID), Optional.empty(),
      START_TIME_CHAN, END_TIME_CHAN);
    assertEquals(CHANNEL_TWO, result);
  }

  @Test
  void testLoadChannelFromWfdiscHit() {
    final long WFID = 314;
    final long RECORD_ID = 217;
    final TagName RECORD_TYPE = TagName.ARID;
    final Instant START_TIME_CHAN = END_TIME2;
    final Instant END_TIME_CHAN = END_TIME;

    when(stationDefinitionIdUtility.getDerivedChannelForWfidRecordId(RECORD_TYPE, RECORD_ID, WFID))
      .thenReturn(CHANNEL);

    String channelKey = Channel.class.getSimpleName().concat(CHANNEL.getName());
    Instant effectiveAt = CHANNEL.getEffectiveAt().orElseThrow();

    when(cache.retrieveVersionsByEntityIdAndTime(channelKey, effectiveAt))
      .thenReturn(CHANNEL);

    // mock response
    String responseKey = Response.class.getSimpleName().concat(CHANNEL.getResponse().get().getId().toString());
    when(cache.versionsByEntityIdAndTimeHasKey(responseKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(responseKey), any())).thenReturn(CHANNEL.getResponse().get());

    Channel result = entityCacheAccessor.loadChannelFromWfdisc(List.of(WFID),
      Optional.of(RECORD_TYPE), Optional.of(RECORD_ID), Optional.empty(),
      START_TIME_CHAN, END_TIME_CHAN);
    assertEquals(CHANNEL, result);

  }

//  /* ---------------------------
//   * Responses Caching Tests
//   --------------------------- */

  @Test
  void findResponsesByIdHitTest() {
    var responseKey = Response.class.getSimpleName().concat(TEST_UUID.toString());
    when(cache.versionsByEntityIdAndTimeHasKey(responseKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(eq(responseKey), any())).thenReturn(RESPONSE);
    List<Response> response = entityCacheAccessor.findResponsesById(List.of(TEST_UUID),
      START_TIME);

    // verify cache was called and response is correct
    verify(cache).retrieveVersionsByEntityIdAndTime(entityIdCaptor.capture(), startTimeCaptor.capture());
    assertFalse(response.isEmpty());
    assertEquals(RESPONSE, response.get(0));
  }

  @Test
  void findResponseByIdMissTest() {
    rangeSet.addAll(List.of(Instant.now(), Instant.now().plusSeconds(30)));

    when(delegate.findResponsesById(any(), any())).thenReturn(List.of(RESPONSE));
    List<Response> response = entityCacheAccessor.findResponsesById(List.of(TEST_UUID),
      START_TIME);

    //check the delegate call is correct
    verify(delegate).findResponsesById(uuidCaptor.capture(),
      startTimeCaptor.capture());
    assertEquals(START_TIME, startTimeCaptor.getValue());
    assertEquals(TEST_UUID, uuidCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(RESPONSE, response.get(0));
  }

  @Test
  void findResponsesByIdFacetHit_populated_Test() {
    final FacetingDefinition facetingDefinition = getFacetingDefinition(RESPONSE_TYPE.getValue(), true);

    String responseKey = Response.class.getSimpleName().concat(TEST_UUID.toString());
    when(cache.versionsByEntityIdAndTimeHasKey(responseKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(any(), any())).thenReturn(RESPONSE_FULL);
    List<Response> response = entityCacheAccessor.findResponsesById(List.of(TEST_UUID),
      START_TIME, facetingDefinition);

    // verify cache was called and response is correct
    verify(cache).retrieveVersionsByEntityIdAndTime(entityIdCaptor.capture(), startTimeCaptor.capture());
    assertFalse(response.isEmpty());
    assertEquals(RESPONSE_FULL, response.get(0));
  }

  @Test
  void findResponsesByIdFacetHit_unpopulated_Test() {
    rangeSet.addAll(List.of(Instant.now(), Instant.now().plusSeconds(30)));
    final FacetingDefinition facetingDefinition = getFacetingDefinition(RESPONSE_TYPE.getValue(), false);

    String responseKey = Response.class.getSimpleName().concat(TEST_UUID.toString());
    when(cache.versionsByEntityIdAndTimeHasKey(responseKey)).thenReturn(true);
    when(cache.retrieveVersionsByEntityIdAndTime(any(), any())).thenReturn(RESPONSE_FULL);
    List<Response> response = entityCacheAccessor.findResponsesById(List.of(TEST_UUID),
      START_TIME, facetingDefinition);

    // verify cache was called and response is correct
    verify(cache).retrieveVersionsByEntityIdAndTime(entityIdCaptor.capture(), startTimeCaptor.capture());
    assertFalse(response.isEmpty());
    assertEquals(RESPONSE_FACET, response.get(0));
  }

  @Test
  void findResponsesByIdFacetMiss_populated_Test() {
    rangeSet.addAll(List.of(Instant.now(), Instant.now().plusSeconds(30)));
    final FacetingDefinition facetingDefinition = getFacetingDefinition(RESPONSE_TYPE.getValue(), true);

    when(delegate.findResponsesById(any(), any(), any())).thenReturn(List.of(RESPONSE_FULL));
    List<Response> response = entityCacheAccessor.findResponsesById(List.of(TEST_UUID),
      START_TIME, facetingDefinition);

    //check the delegate call is correct
    verify(delegate).findResponsesById(uuidCaptor.capture(),
      startTimeCaptor.capture(), facetCaptor.capture());
    assertEquals(START_TIME, startTimeCaptor.getValue());
    assertEquals(TEST_UUID, uuidCaptor.getValue().get(0));
    assertEquals(facetingDefinition, facetCaptor.getValue());

    //check returns delegate response
    assertEquals(RESPONSE_FULL, response.get(0));
  }

  @Test
  void findResponsesByIdFacetMiss_unpopulated_Test() {
    rangeSet.addAll(List.of(Instant.now(), Instant.now().plusSeconds(30)));
    final FacetingDefinition facetingDefinition = getFacetingDefinition(RESPONSE_TYPE.getValue(), false);

    when(delegate.findResponsesById(any(), any(), any())).thenReturn(List.of(RESPONSE_FULL));
    List<Response> response = entityCacheAccessor.findResponsesById(List.of(TEST_UUID),
      START_TIME, facetingDefinition);

    //check the delegate call is correct
    verify(delegate).findResponsesById(uuidCaptor.capture(),
      startTimeCaptor.capture(), facetCaptor.capture());
    assertEquals(START_TIME, startTimeCaptor.getValue());
    assertEquals(TEST_UUID, uuidCaptor.getValue().get(0));
    assertEquals(facetingDefinition, facetCaptor.getValue());

    //check returns delegate response
    assertEquals(RESPONSE_FACET, response.get(0));
  }

  @Test
  void findResponsesByIdAndTimeRangeHitTest() {
    rangeSet.addAll(List.of(START_TIME, END_TIME));

    RangeMap<Instant, Object> rangeMap = TreeRangeMap.create();
    rangeMap.put(Range.closed(START_TIME, END_TIME), RESPONSE);
    when(cache.retrieveVersionsByEntityIdAndTimeRangeMap(any())).thenReturn(rangeMap);

    List<Response> response = entityCacheAccessor.findResponsesByIdAndTimeRange(List.of(TEST_UUID),
      START_TIME, END_TIME);

    // verify cache was called and response is correct
    verify(cache, times(1)).retrieveVersionsByEntityIdAndTimeRangeMap(entityIdCaptor.capture());
    assertFalse(response.isEmpty());
    assertEquals(RESPONSE, response.get(0));
  }

  @Test
  void findResponsesByIdAndTimeRangeMissTest() {
    rangeSet.addAll(List.of(START_TIME, END_TIME2));

    RangeMap<Instant, Object> rangeMap = TreeRangeMap.create();
    rangeMap.put(Range.closed(START_TIME, END_TIME2), RESPONSE);
    when(cache.retrieveVersionsByEntityIdAndTimeRangeMap(any())).thenReturn(rangeMap);
    when(delegate.findResponsesByIdAndTimeRange(any(), any(), any())).thenReturn(List.of(RESPONSE_TWO));
    List<Response> response = entityCacheAccessor.findResponsesByIdAndTimeRange(List.of(TEST_UUID),
      START_TIME, END_TIME);

    // verify cache was called and response is correct
    verify(cache, times(1)).retrieveVersionsByEntityIdAndTimeRangeMap(entityIdCaptor.capture());
    assertFalse(response.isEmpty());
    assertTrue(response.contains(RESPONSE));
    assertTrue(response.contains(RESPONSE_TWO));
  }

  @Test
  void loadResponseFromWfdiscHitTest() {
    when(stationDefinitionIdUtility.getResponseForWfid(anyLong())).thenReturn(Optional.of(RESPONSE_ONE));
    when(cache.retrieveVersionsByEntityIdAndTime(any(), any())).thenReturn(RESPONSE_ONE);

    var actual = entityCacheAccessor.loadResponseFromWfdisc(1L);

    verify(stationDefinitionIdUtility, times(1)).getResponseForWfid(anyLong());
    verify(cache, times(1)).retrieveVersionsByEntityIdAndTime(any(), any());

    Assertions.assertEquals(RESPONSE_ONE, actual);

    verifyNoMoreInteractions(stationDefinitionIdUtility, cache);
  }

  @Test
  void loadResponseFromWfdiscHitTestNoEffectiveAt() {
    RangeMap entityMap = TreeRangeMap.create();

    entityMap.put(Range.closed(Instant.EPOCH, Instant.MAX), RESPONSE);

    when(stationDefinitionIdUtility.getResponseForWfid(anyLong())).thenReturn(Optional.of(RESPONSE));
    when(cache.retrieveVersionsByEntityIdAndTimeRangeMap(any())).thenReturn(entityMap);

    var actual = entityCacheAccessor.loadResponseFromWfdisc(1L);

    verify(stationDefinitionIdUtility, times(1)).getResponseForWfid(anyLong());
    verify(cache, times(1)).retrieveVersionsByEntityIdAndTimeRangeMap(any());

    Assertions.assertEquals(RESPONSE, actual);

    verifyNoMoreInteractions(stationDefinitionIdUtility, cache);
  }

  @Test
  void loadResponseFromWfdiscMissTest() {
    when(stationDefinitionIdUtility.getResponseForWfid(anyLong())).thenReturn(Optional.empty());
    when(delegate.loadResponseFromWfdisc(anyLong())).thenReturn(RESPONSE_ONE);

    var actual = entityCacheAccessor.loadResponseFromWfdisc(1L);

    verify(stationDefinitionIdUtility, times(1)).getResponseForWfid(anyLong());
    verify(delegate, times(1)).loadResponseFromWfdisc(anyLong());
    verify(delegate, times(1)).storeResponses(any());

    Assertions.assertEquals(RESPONSE_ONE, actual);

    verifyNoMoreInteractions(stationDefinitionIdUtility, delegate);
  }

  @Test
  void storeStationGroupTest() {
    StationGroup stationGroup = STATION_GROUP.toBuilder().setEffectiveAt(END_TIME2).build();
    String key = "StationGroup" + stationGroup.getName();
    entityCacheAccessor.storeStationGroups(List.of(stationGroup));
    verify(cache).cacheVersionsByEntityIdAndTime(eq(key), any());
    verify(delegate).storeStationGroups(stationGroupListCaptor.capture());
  }

  @Test
  void storeStationGroupTestExistingTimes() {
    RangeMap<Instant, Object> rangeMap = TreeRangeMap.create();
    StationGroup existngStationGroup = STATION_GROUP.toBuilder()
      .setEffectiveAt(END_TIME2.plusSeconds(5))
      .setEffectiveUntil(END_TIME2.plusSeconds(10))
      .build();
    String key = StationGroup.class.getSimpleName().concat(existngStationGroup.getName());
    rangeMap.put(Range.closed(existngStationGroup.getEffectiveAt().orElseThrow(),
      existngStationGroup.getEffectiveUntil().orElseThrow()), existngStationGroup.getName());
    StationGroup stationGroup = STATION_GROUP.toBuilder().setEffectiveAt(END_TIME2).build();
    entityCacheAccessor.storeStationGroups(List.of(stationGroup));
    verify(cache).cacheVersionsByEntityIdAndTime(eq(key), any());
    verify(delegate).storeStationGroups(stationGroupListCaptor.capture());
  }

  @Test
  void storeStationTest() {
    Station station = STATION.toBuilder().setEffectiveAt(END_TIME2).build();
    String key = Station.class.getSimpleName().concat(station.getName());
    entityCacheAccessor.storeStations(List.of(station));
    verify(cache).cacheVersionsByEntityIdAndTime(eq(key), any());
    verify(delegate).storeStations(stationListCaptor.capture());
  }

  @Test
  void storeStationTestExistingTimes() {
    Station station = STATION.toBuilder().setEffectiveAt(END_TIME2).build();
    String key = Station.class.getSimpleName().concat(station.getName());
    entityCacheAccessor.storeStations(List.of(station));
    verify(cache).cacheVersionsByEntityIdAndTime(eq(key), any());
    verify(delegate).storeStations(stationListCaptor.capture());
  }

  @Test
  void storeChannelGroupTest() {
    ChannelGroup channelGroup = CHANNEL_GROUP.toBuilder().setEffectiveAt(END_TIME2).build();
    String key = ChannelGroup.class.getSimpleName().concat(channelGroup.getName());
    entityCacheAccessor.storeChannelGroups(List.of(channelGroup));
    verify(cache).cacheVersionsByEntityIdAndTime(eq(key), any());
    verify(delegate).storeChannelGroups(channelGroupListCaptor.capture());
  }

  @Test
  void storeChannelGroupTestExistingTimes() {
    ChannelGroup channelGroup = CHANNEL_GROUP.toBuilder().setEffectiveAt(END_TIME2).build();
    String key = ChannelGroup.class.getSimpleName().concat(channelGroup.getName());
    entityCacheAccessor.storeChannelGroups(List.of(channelGroup));
    verify(cache).cacheVersionsByEntityIdAndTime(eq(key), any());
    verify(delegate).storeChannelGroups(channelGroupListCaptor.capture());
  }

  @Test
  void storeChannelTest() {
    Channel channel = CHANNEL.toBuilder().setEffectiveAt(END_TIME2).build();
    String key = Channel.class.getSimpleName().concat(channel.getName());
    entityCacheAccessor.storeChannels(List.of(channel));
    verify(cache).cacheVersionsByEntityIdAndTime(eq(key), any());
    verify(delegate).storeChannels(channelListCaptor.capture());
  }

  @Test
  void storeChannelTestExistingTimes() {
    RangeMap<Instant, Object> rangeMap = TreeRangeMap.create();
    Channel existngChannel = CHANNEL.toBuilder().setEffectiveAt(END_TIME2.plusSeconds(5))
      .build();
    String key = Channel.class.getSimpleName().concat(existngChannel.getName());
    rangeMap.put(Range.closed(existngChannel.getEffectiveAt().orElseThrow(), existngChannel.getEffectiveUntil().orElseThrow()),
      existngChannel.getName());
    Channel channel = CHANNEL.toBuilder().setEffectiveAt(END_TIME2).build();
    entityCacheAccessor.storeChannels(List.of(channel));
    verify(cache).cacheVersionsByEntityIdAndTime(eq(key), any());
    verify(delegate).storeChannels(channelListCaptor.capture());
  }

  @Test
  void testStoreResponseValidation() {

    assertThrows(NullPointerException.class, () -> entityCacheAccessor.storeResponses(null));
  }

  @Test
  void testStoreResponses() {
    Response timeAdjResponse = RESPONSE.toBuilder().setEffectiveAt(START_TIME).build();
    List<Response> responses = List.of(timeAdjResponse);
    entityCacheAccessor.storeResponses(responses);
    verify(cache).cacheVersionsByEntityIdAndTime(entityIdCaptor.capture(), rangeMapInstantObjectCaptor.capture());
    assertEquals(entityIdCaptor.getValue(), Response.class.getSimpleName().concat(responses.get(0).getId().toString()));
    assertEquals(rangeMapInstantObjectCaptor.getValue().get(START_TIME), (Object) timeAdjResponse);
  }

  @Test
  void testStoreResponsesExistingTimes() {
    Response existingResponse = RESPONSE_FULL.toBuilder()
      .setId(UUID.nameUUIDFromBytes("NEWID".getBytes()))
      .setEffectiveAt(Instant.EPOCH.plusSeconds(5))
      .build();

    RangeMap<Instant, Object> rangeMap = TreeRangeMap.create();
    rangeMap.put(Range.atLeast(existingResponse.getEffectiveAt().orElseThrow()),
      existingResponse);
    Response timeAdjResponse = RESPONSE.toBuilder().setEffectiveAt(START_TIME).build();
    List<Response> responses = List.of(timeAdjResponse);
    entityCacheAccessor.storeResponses(responses);
    verify(cache).cacheVersionsByEntityIdAndTime(entityIdCaptor.capture(), rangeMapInstantObjectCaptor.capture());
    assertEquals(entityIdCaptor.getValue(), Response.class.getSimpleName().concat(responses.get(0).getId().toString()));
    assertEquals(rangeMapInstantObjectCaptor.getValue().get(START_TIME), timeAdjResponse);
  }

  @ParameterizedTest
  @MethodSource("getCacheArguments")
  void testCachingValidation(Class<? extends Exception> expectedException,
    List<String> stationGroupNames,
    Instant startTime,
    Instant endTime) {

    assertThrows(expectedException, () -> entityCacheAccessor.cache(stationGroupNames, startTime, endTime));
  }

  static Stream<Arguments> getCacheArguments() {
    return Stream.of(arguments(NullPointerException.class, null, START_TIME, END_TIME),
      arguments(NullPointerException.class, List.of("Test"), null, END_TIME),
      arguments(NullPointerException.class, List.of("Test"), START_TIME, null),
      arguments(IllegalStateException.class, List.of("Test"), END_TIME, START_TIME));
  }

  @Test
  void testCache() {
    when(delegate.findStationGroupsByNameAndTimeRange(List.of(STATION_GROUP.getName()), START_TIME, END_TIME))
      .thenReturn(List.of(STATION_GROUP));
    when(delegate.findStationsByNameAndTimeRange(List.of(STATION.getName()), START_TIME, END_TIME))
      .thenReturn(List.of(STATION));
    when(delegate.findChannelGroupsByNameAndTimeRange(List.of(CHANNEL_GROUP.getName()), START_TIME, END_TIME))
      .thenReturn(List.of(CHANNEL_GROUP));
    when(delegate.findChannelsByNameAndTimeRange(List.of(CHANNEL.getName()), START_TIME, END_TIME))
      .thenReturn(List.of(CHANNEL));
    when(delegate.findResponsesByIdAndTimeRange(List.of(CHANNEL.getResponse().map(Response::getId).orElseThrow()),
      START_TIME,
      END_TIME))
      .thenReturn(List.of(CHANNEL.getResponse().orElseThrow()));

    when(delegate.findChannelsByNameAndTimeRange(List.of(CHANNEL_TWO.getName()), START_TIME, END_TIME))
      .thenReturn(List.of(CHANNEL_TWO));
    when(delegate.findResponsesByIdAndTimeRange(List.of(CHANNEL_TWO.getResponse().map(Response::getId).orElseThrow()),
      START_TIME,
      END_TIME))
      .thenReturn(List.of(CHANNEL_TWO.getResponse().orElseThrow()));

    entityCacheAccessor.cache(List.of(STATION_GROUP.getName()), START_TIME, END_TIME);

    verify(cache, times(1)).clear();
    verify(delegate, times(1))
      .findStationGroupsByNameAndTimeRange(List.of(STATION_GROUP.getName()), START_TIME, END_TIME);
    verify(delegate, times(1))
      .storeStationGroups(List.of(STATION_GROUP));
    verify(delegate, times(1))
      .findStationsByNameAndTimeRange(List.of(STATION.getName()), START_TIME, END_TIME);
    verify(delegate, times(1))
      .storeStations(List.of(STATION));
    verify(delegate, times(1))
      .findChannelGroupsByNameAndTimeRange(List.of(CHANNEL_GROUP.getName()), START_TIME, END_TIME);
    verify(delegate, times(1))
      .storeChannelGroups(List.of(CHANNEL_GROUP));

    verify(delegate, times(1))
      .findChannelsByNameAndTimeRange(List.of(CHANNEL.getName()), START_TIME, END_TIME);
    verify(delegate, times(1))
      .findChannelsByNameAndTimeRange(List.of(CHANNEL_TWO.getName()), START_TIME, END_TIME);
    verify(delegate, times(1))
      .storeChannels(List.of(CHANNEL));
    verify(delegate, times(1))
      .storeChannels(List.of(CHANNEL_TWO));
    verify(delegate, times(1)).storeResponses(List.of(CHANNEL.getResponse().orElseThrow(),
      CHANNEL_TWO.getResponse().orElseThrow()));

    verify(delegate, times(1)).findResponsesByIdAndTimeRange(
      List.of(CHANNEL.getResponse().map(Response::getId).orElseThrow()),
      START_TIME,
      END_TIME);
    verify(delegate, times(1)).findResponsesByIdAndTimeRange(
      List.of(CHANNEL_TWO.getResponse().map(Response::getId).orElseThrow()),
      START_TIME,
      END_TIME);
    verify(delegate, times(1)).storeResponses(List.of(CHANNEL.getResponse().orElseThrow()));
    verify(delegate, times(1)).storeResponses(List.of(CHANNEL_TWO.getResponse().orElseThrow()));

    verifyNoMoreInteractions(delegate);
  }

  public FacetingDefinition getFacetingDefinition(String classType, boolean populated) {
    return FacetingDefinition.builder()
      .setClassType(classType)
      .setPopulated(populated)
      .build();
  }
}
