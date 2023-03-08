package gms.shared.stationdefinition.accessor;

import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.stationdefinition.api.StationDefinitionAccessorInterface;
import gms.shared.stationdefinition.api.channel.util.ChannelGroupsTimeFacetRequest;
import gms.shared.stationdefinition.api.channel.util.ChannelGroupsTimeRangeRequest;
import gms.shared.stationdefinition.api.channel.util.ChannelGroupsTimeRequest;
import gms.shared.stationdefinition.api.channel.util.ChannelsTimeFacetRequest;
import gms.shared.stationdefinition.api.channel.util.ChannelsTimeRangeRequest;
import gms.shared.stationdefinition.api.channel.util.ChannelsTimeRequest;
import gms.shared.stationdefinition.api.channel.util.ResponseTimeFacetRequest;
import gms.shared.stationdefinition.api.channel.util.ResponseTimeRangeRequest;
import gms.shared.stationdefinition.api.station.util.StationChangeTimesRequest;
import gms.shared.stationdefinition.api.station.util.StationGroupsTimeFacetRequest;
import gms.shared.stationdefinition.api.station.util.StationGroupsTimeRangeRequest;
import gms.shared.stationdefinition.api.station.util.StationGroupsTimeRequest;
import gms.shared.stationdefinition.api.station.util.StationsTimeFacetRequest;
import gms.shared.stationdefinition.api.station.util.StationsTimeRangeRequest;
import gms.shared.stationdefinition.api.station.util.StationsTimeRequest;
import gms.shared.stationdefinition.api.util.Request;
import gms.shared.stationdefinition.api.util.TimeRangeRequest;
import gms.shared.stationdefinition.cache.RequestCache;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelGroup;
import gms.shared.stationdefinition.coi.channel.Response;
import gms.shared.stationdefinition.coi.facets.FacetingDefinition;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.coi.station.StationGroup;
import gms.shared.stationdefinition.facet.StationDefinitionFacetingUtility;
import gms.shared.stationdefinition.testfixtures.UtilsTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.RESPONSE_ONE;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.RESPONSE_ONE_FACET;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.STATION_CHANGE_TIMES_REQUEST_700s;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestCachingStationDefinitionAccessorTest {

  @Mock
  private SystemConfig systemConfig;
  @Mock
  private StationDefinitionAccessorInterface delegate;
  @Mock
  private RequestCache cache;
  @Captor
  private ArgumentCaptor<Request> requestCaptor;
  @Captor
  private ArgumentCaptor<Instant> startTimeCaptor;
  @Captor
  private ArgumentCaptor<Instant> endTimeCaptor;
  @Captor
  private ArgumentCaptor<List<UUID>> uuidsCaptor;
  @Captor
  private ArgumentCaptor<List<String>> namesCaptor;
  @Captor
  private ArgumentCaptor<List<UUID>> uuidCaptor;
  @Captor
  private ArgumentCaptor<FacetingDefinition> facetCaptor;
  @Captor
  private ArgumentCaptor<List<Object>> objectListCaptor;
  @Captor
  private ArgumentCaptor<List<StationGroup>> stationGroupListCaptor;
  @Captor
  private ArgumentCaptor<List<Station>> stationListCaptor;
  @Captor
  private ArgumentCaptor<List<ChannelGroup>> channelGroupListCaptor;
  @Captor
  private ArgumentCaptor<List<Channel>> channelListCaptor;
  @Captor
  private ArgumentCaptor<StationChangeTimesRequest> changeTimeRequestCaptor;
  @Captor
  private ArgumentCaptor<Station> stationCaptor;

  StationDefinitionAccessorInterface requestCacheAccessor;

  @BeforeEach
  public void beforeEach() {
    requestCacheAccessor = new RequestCachingStationDefinitionAccessor(mock(SystemConfig.class), delegate, cache);

    ReflectionTestUtils.setField(requestCacheAccessor, "stationDefinitionFacetingUtility",
      StationDefinitionFacetingUtility.create(requestCacheAccessor));
  }

  /* ------------------------------
   * StationGroup Caching Tests
   -------------------------------- */
  @Test
  void findStationGroupsByNameAndTimeHitTest() {
    when(cache.retrieve(any())).thenReturn(List.of(UtilsTestFixtures.STATION_GROUP));
    List<StationGroup> response = requestCacheAccessor.findStationGroupsByNameAndTime(List.of(CannedRequests.GENERIC_NAME),
      CannedRequests.START_TIME);
    verify(cache).retrieve(requestCaptor.capture());
    //check Request Key
    assertEquals(CannedRequests.STATION_GRP_TIME_REQ, requestCaptor.getValue());
    //check cache response gets Marshelled
    assertFalse(response.isEmpty());
    assertEquals(UtilsTestFixtures.STATION_GROUP, response.get(0));
  }

  @Test
  void findStationGroupsByNameAndTimeMissTest() {
    when(cache.retrieve(any())).thenReturn(List.of());
    when(delegate.findStationGroupsByNameAndTime(any(), any())).thenReturn(List.of(UtilsTestFixtures.STATION_GROUP));
    List<StationGroup> response = requestCacheAccessor.findStationGroupsByNameAndTime(List.of(CannedRequests.GENERIC_NAME),
      CannedRequests.START_TIME);
    //check correct delegate call
    verify(delegate, Mockito.only()).findStationGroupsByNameAndTime(namesCaptor.capture(),
      startTimeCaptor.capture());
    assertEquals(CannedRequests.START_TIME, startTimeCaptor.getValue());
    assertEquals(CannedRequests.GENERIC_NAME, namesCaptor.getValue().get(0));

    //check cache gets populated
    verify(cache).put(requestCaptor.capture(), objectListCaptor.capture());
    assertEquals(CannedRequests.STATION_GRP_TIME_REQ, requestCaptor.getValue());
    assertEquals(UtilsTestFixtures.STATION_GROUP, objectListCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(UtilsTestFixtures.STATION_GROUP, response.get(0));
  }

  @Test
  void storeResponsesTest() {
    requestCacheAccessor.storeResponses(List.of(UtilsTestFixtures.RESPONSE));
    verify(delegate, Mockito.times(1)).storeResponses(List.of(UtilsTestFixtures.RESPONSE));
  }

  @Test
  void findStationGroupsByNameAndTimeFacetHitTest() {
    when(cache.retrieve(any())).thenReturn(List.of(UtilsTestFixtures.STATION_GROUP));
    List<StationGroup> response = requestCacheAccessor.findStationGroupsByNameAndTime(List.of(CannedRequests.GENERIC_NAME),
      CannedRequests.START_TIME, CannedRequests.FACET_DEF);
    verify(cache).retrieve(requestCaptor.capture());
    //check Request Key
    assertEquals(CannedRequests.STATION_GRP_TIME_FACET_REQ, requestCaptor.getValue());
    //check cache response gets Marshelled
    assertFalse(response.isEmpty());
    assertEquals(UtilsTestFixtures.STATION_GROUP, response.get(0));
  }

  @Test
  void findStationGroupsByNameAndTimeFacetMissTest() {
    when(cache.retrieve(any())).thenReturn(List.of());
    when(delegate.findStationsByNameAndTime(any(), any(), any())).thenReturn(List.of(UtilsTestFixtures.STATION));
    List<Station> response = requestCacheAccessor.findStationsByNameAndTime(List.of(CannedRequests.GENERIC_NAME),
      CannedRequests.START_TIME, CannedRequests.FACET_DEF);
    //check correct delegate call
    verify(delegate, Mockito.only()).findStationsByNameAndTime(namesCaptor.capture(),
      startTimeCaptor.capture(), facetCaptor.capture());
    assertEquals(CannedRequests.START_TIME, startTimeCaptor.getValue());
    assertEquals(CannedRequests.GENERIC_NAME, namesCaptor.getValue().get(0));
    assertEquals(CannedRequests.FACET_DEF, facetCaptor.getValue());

    //check cache gets populated
    verify(cache).put(requestCaptor.capture(), objectListCaptor.capture());
    assertEquals(CannedRequests.STATION_TIME_FACET_REQ, requestCaptor.getValue());
    assertEquals(UtilsTestFixtures.STATION, objectListCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(UtilsTestFixtures.STATION, response.get(0));
  }

  @Test
  void findStationGroupsByNameAndTimeRangeHitTest() {
    when(cache.retrieve(any())).thenReturn(List.of(UtilsTestFixtures.STATION_GROUP));
    List<StationGroup> response = requestCacheAccessor.findStationGroupsByNameAndTimeRange(List.of(CannedRequests.GENERIC_NAME),
      CannedRequests.START_TIME, CannedRequests.END_TIME);
    verify(cache).retrieve(requestCaptor.capture());
    //check Request Key
    assertEquals(CannedRequests.STATION_GRP_TIME_RANGE_REQ, requestCaptor.getValue());
    //check cache response gets Marshelled
    assertEquals(1, response.size());
    assertEquals(UtilsTestFixtures.STATION_GROUP, response.get(0));
  }

  @Test
  void findStationGroupsByNameAndTimeRangeMissTest() {
    when(cache.retrieve(any())).thenReturn(List.of());
    when(delegate.findStationGroupsByNameAndTimeRange(any(), any(), any())).thenReturn(List.of(UtilsTestFixtures.STATION_GROUP));
    List<StationGroup> response = requestCacheAccessor.findStationGroupsByNameAndTimeRange(List.of(CannedRequests.GENERIC_NAME),
      CannedRequests.START_TIME, CannedRequests.END_TIME);
    //check correct delegate call
    verify(delegate, Mockito.only()).findStationGroupsByNameAndTimeRange(namesCaptor.capture(),
      startTimeCaptor.capture(), endTimeCaptor.capture());
    assertEquals(CannedRequests.START_TIME, startTimeCaptor.getValue());
    assertEquals(CannedRequests.GENERIC_NAME, namesCaptor.getValue().get(0));
    assertEquals(CannedRequests.END_TIME, endTimeCaptor.getValue());

    //check cache gets populated
    verify(cache).put(requestCaptor.capture(), objectListCaptor.capture());
    assertEquals(CannedRequests.STATION_GRP_TIME_RANGE_REQ, requestCaptor.getValue());
    assertEquals(UtilsTestFixtures.STATION_GROUP, objectListCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(UtilsTestFixtures.STATION_GROUP, response.get(0));
  }

//  /* ------------------------------
//   * Station Caching Tests
//   -------------------------------- */

  @Test
  void findStationsByNameAndTimeHitTest() {
    when(cache.retrieve(any())).thenReturn(List.of(UtilsTestFixtures.STATION));
    List<Station> response = requestCacheAccessor.findStationsByNameAndTime(List.of(CannedRequests.GENERIC_NAME),
      CannedRequests.START_TIME);
    verify(cache).retrieve(requestCaptor.capture());
    //check Request Key
    assertEquals(CannedRequests.STATION_TIME_REQ, requestCaptor.getValue());
    //check cache response gets Marshelled
    assertFalse(response.isEmpty());
    assertEquals(UtilsTestFixtures.STATION, response.get(0));
  }

  @Test
  void findStationsByNameAndTimeMissTest() {
    when(cache.retrieve(any())).thenReturn(List.of());
    when(delegate.findStationsByNameAndTime(any(), any())).thenReturn(List.of(UtilsTestFixtures.STATION));
    List<Station> response = requestCacheAccessor.findStationsByNameAndTime(List.of(CannedRequests.GENERIC_NAME),
      CannedRequests.START_TIME);
    //check correct delegate call
    verify(delegate, Mockito.only()).findStationsByNameAndTime(namesCaptor.capture(),
      startTimeCaptor.capture());
    assertEquals(CannedRequests.START_TIME, startTimeCaptor.getValue());
    assertEquals(CannedRequests.GENERIC_NAME, namesCaptor.getValue().get(0));

    //check cache gets populated
    verify(cache).put(requestCaptor.capture(), objectListCaptor.capture());
    assertEquals(CannedRequests.STATION_TIME_REQ, requestCaptor.getValue());
    assertEquals(UtilsTestFixtures.STATION, objectListCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(UtilsTestFixtures.STATION, response.get(0));
  }

  @Test
  void findStationsByNameAndTimeFacetHitTest() {
    when(cache.retrieve(any())).thenReturn(List.of(UtilsTestFixtures.STATION));
    List<Station> response = requestCacheAccessor.findStationsByNameAndTime(List.of(CannedRequests.GENERIC_NAME),
      CannedRequests.START_TIME, CannedRequests.FACET_DEF);
    verify(cache).retrieve(requestCaptor.capture());
    //check Request Key
    assertEquals(CannedRequests.STATION_TIME_FACET_REQ, requestCaptor.getValue());
    //check cache response gets Marshelled
    assertFalse(response.isEmpty());
    assertEquals(UtilsTestFixtures.STATION, response.get(0));
  }

  @Test
  void findStationsByNameAndTimeFacetMissTest() {
    when(cache.retrieve(any())).thenReturn(List.of());
    when(delegate.findStationsByNameAndTime(any(), any(), any())).thenReturn(List.of(UtilsTestFixtures.STATION));
    List<Station> response = requestCacheAccessor.findStationsByNameAndTime(List.of(CannedRequests.GENERIC_NAME),
      CannedRequests.START_TIME, CannedRequests.FACET_DEF);
    //check correct delegate call
    verify(delegate, Mockito.only()).findStationsByNameAndTime(namesCaptor.capture(),
      startTimeCaptor.capture(), facetCaptor.capture());
    assertEquals(CannedRequests.START_TIME, startTimeCaptor.getValue());
    assertEquals(CannedRequests.GENERIC_NAME, namesCaptor.getValue().get(0));
    assertEquals(CannedRequests.FACET_DEF, facetCaptor.getValue());

    //check cache gets populated
    verify(cache).put(requestCaptor.capture(), objectListCaptor.capture());
    assertEquals(CannedRequests.STATION_TIME_FACET_REQ, requestCaptor.getValue());
    assertEquals(UtilsTestFixtures.STATION, objectListCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(UtilsTestFixtures.STATION, response.get(0));
  }

  @Test
  void findStationsByNameAndTimeRangeHitTest() {
    when(cache.retrieve(any())).thenReturn(List.of(UtilsTestFixtures.STATION));
    List<Station> response = requestCacheAccessor.findStationsByNameAndTimeRange(List.of(CannedRequests.GENERIC_NAME),
      CannedRequests.START_TIME, CannedRequests.END_TIME);
    verify(cache).retrieve(requestCaptor.capture());
    //check Request Key
    assertEquals(CannedRequests.STATION_TIME_RANGE_REQ, requestCaptor.getValue());
    //check cache response gets Marshelled
    assertFalse(response.isEmpty());
    assertEquals(UtilsTestFixtures.STATION, response.get(0));
  }

  @Test
  void findStationsByNameAndTimeRangeMissTest() {
    when(cache.retrieve(any())).thenReturn(List.of());
    when(delegate.findStationsByNameAndTimeRange(any(), any(), any())).thenReturn(List.of(UtilsTestFixtures.STATION));
    List<Station> response = requestCacheAccessor.findStationsByNameAndTimeRange(List.of(CannedRequests.GENERIC_NAME),
      CannedRequests.START_TIME, CannedRequests.END_TIME);
    //check correct delegate call
    verify(delegate, Mockito.only()).findStationsByNameAndTimeRange(namesCaptor.capture(),
      startTimeCaptor.capture(), endTimeCaptor.capture());
    assertEquals(CannedRequests.START_TIME, startTimeCaptor.getValue());
    assertEquals(CannedRequests.GENERIC_NAME, namesCaptor.getValue().get(0));
    assertEquals(CannedRequests.END_TIME, endTimeCaptor.getValue());

    //check cache gets populated
    verify(cache).put(requestCaptor.capture(), objectListCaptor.capture());
    assertEquals(CannedRequests.STATION_TIME_RANGE_REQ, requestCaptor.getValue());
    assertEquals(UtilsTestFixtures.STATION, objectListCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(UtilsTestFixtures.STATION, response.get(0));
  }

  @Test
  void determineStationChangeTimesHitTest() {
    when(cache.retrieve(any())).thenReturn(List.of(Instant.EPOCH));
    List<Instant> response = requestCacheAccessor.determineStationChangeTimes(STATION_CHANGE_TIMES_REQUEST_700s.getStation(),
      STATION_CHANGE_TIMES_REQUEST_700s.getStartTime(),
      STATION_CHANGE_TIMES_REQUEST_700s.getEndTime());
    verify(cache).retrieve(requestCaptor.capture());
    assertEquals(STATION_CHANGE_TIMES_REQUEST_700s, requestCaptor.getValue());

    assertFalse(response.isEmpty());
    assertEquals(Instant.EPOCH, response.get(0));
  }

  @Test
  void determineStationChangeTimesMissTest() {
    when(cache.retrieve(any())).thenReturn(List.of());
    when(delegate.determineStationChangeTimes(STATION_CHANGE_TIMES_REQUEST_700s.getStation(),
      STATION_CHANGE_TIMES_REQUEST_700s.getStartTime(),
      STATION_CHANGE_TIMES_REQUEST_700s.getEndTime())).thenReturn(List.of(Instant.EPOCH));
    List<Instant> response = requestCacheAccessor.determineStationChangeTimes(STATION_CHANGE_TIMES_REQUEST_700s.getStation(),
      STATION_CHANGE_TIMES_REQUEST_700s.getStartTime(),
      STATION_CHANGE_TIMES_REQUEST_700s.getEndTime());
    verify(delegate, Mockito.only()).determineStationChangeTimes(stationCaptor.capture(),
      startTimeCaptor.capture(),
      endTimeCaptor.capture());

    assertEquals(STATION_CHANGE_TIMES_REQUEST_700s.getStation(), stationCaptor.getValue());
    assertEquals(STATION_CHANGE_TIMES_REQUEST_700s.getStartTime(), startTimeCaptor.getValue());
    assertEquals(STATION_CHANGE_TIMES_REQUEST_700s.getEndTime(), endTimeCaptor.getValue());
  }

  //  /* ------------------------------
//   * ChannelGroups Caching Tests
//   -------------------------------- */
  @Test
  void findChannelGroupsByNameAndTimeHitTest() {
    when(cache.retrieve(any())).thenReturn(List.of((Object) UtilsTestFixtures.CHANNEL_GROUP));
    List<ChannelGroup> response = requestCacheAccessor.findChannelGroupsByNameAndTime(List.of(CannedRequests.GENERIC_NAME),
      CannedRequests.START_TIME);
    verify(cache).retrieve(requestCaptor.capture());
    //check Request Key
    assertEquals(CannedRequests.CHAN_GRP_TIME_REQ, requestCaptor.getValue());
    //check cache response gets Marshelled
    assertFalse(response.isEmpty());
    assertEquals(UtilsTestFixtures.CHANNEL_GROUP, response.get(0));
  }

  @Test
  void findChannelGroupsByNameAndTimeMissTest() {
    when(cache.retrieve(any())).thenReturn(List.of());
    when(delegate.findChannelGroupsByNameAndTime(any(), any())).thenReturn(List.of(UtilsTestFixtures.CHANNEL_GROUP));
    List<ChannelGroup> response = requestCacheAccessor.findChannelGroupsByNameAndTime(List.of(CannedRequests.GENERIC_NAME),
      CannedRequests.START_TIME);
    //check correct delegate call
    verify(delegate, Mockito.only()).findChannelGroupsByNameAndTime(namesCaptor.capture(),
      startTimeCaptor.capture());
    assertEquals(CannedRequests.START_TIME, startTimeCaptor.getValue());
    assertEquals(CannedRequests.GENERIC_NAME, namesCaptor.getValue().get(0));

    //check cache gets populated
    verify(cache).put(requestCaptor.capture(), objectListCaptor.capture());
    assertEquals(CannedRequests.CHAN_GRP_TIME_REQ, requestCaptor.getValue());
    assertEquals(UtilsTestFixtures.CHANNEL_GROUP, objectListCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(UtilsTestFixtures.CHANNEL_GROUP, response.get(0));
  }

  @Test
  void findChannelGroupsByNameAndTimeFacetHitTest() {
    when(cache.retrieve(any())).thenReturn(List.of(UtilsTestFixtures.CHANNEL_GROUP));
    List<ChannelGroup> response = requestCacheAccessor.findChannelGroupsByNameAndTime(List.of(CannedRequests.GENERIC_NAME),
      CannedRequests.START_TIME, CannedRequests.FACET_DEF);
    verify(cache).retrieve(requestCaptor.capture());
    //check Request Key
    assertEquals(CannedRequests.CHAN_GRP_TIME_FACET_REQ, requestCaptor.getValue());
    //check cache response gets Marshelled
    assertFalse(response.isEmpty());
    assertEquals(UtilsTestFixtures.CHANNEL_GROUP, response.get(0));
  }

  @Test
  void findChannelGroupsByNameAndTimeFacetMissTest() {
    when(cache.retrieve(any())).thenReturn(List.of());
    when(delegate.findChannelGroupsByNameAndTime(any(), any(), any())).thenReturn(List.of(UtilsTestFixtures.CHANNEL_GROUP));
    List<ChannelGroup> response = requestCacheAccessor.findChannelGroupsByNameAndTime(List.of(CannedRequests.GENERIC_NAME),
      CannedRequests.START_TIME, CannedRequests.FACET_DEF);
    //check correct delegate call
    verify(delegate, Mockito.only()).findChannelGroupsByNameAndTime(namesCaptor.capture(),
      startTimeCaptor.capture(), facetCaptor.capture());
    assertEquals(CannedRequests.START_TIME, startTimeCaptor.getValue());
    assertEquals(CannedRequests.GENERIC_NAME, namesCaptor.getValue().get(0));
    assertEquals(CannedRequests.FACET_DEF, facetCaptor.getValue());

    //check cache gets populated
    verify(cache).put(requestCaptor.capture(), objectListCaptor.capture());
    assertEquals(CannedRequests.CHAN_GRP_TIME_FACET_REQ, requestCaptor.getValue());
    assertEquals(UtilsTestFixtures.CHANNEL_GROUP, objectListCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(UtilsTestFixtures.CHANNEL_GROUP, response.get(0));
  }

  @Test
  void findChannelGroupsByNameAndTimeRangeHitTest() {
    when(cache.retrieve(any())).thenReturn(List.of(UtilsTestFixtures.CHANNEL_GROUP));
    List<ChannelGroup> response = requestCacheAccessor.findChannelGroupsByNameAndTimeRange(List.of(CannedRequests.GENERIC_NAME),
      CannedRequests.START_TIME, CannedRequests.END_TIME);
    verify(cache).retrieve(requestCaptor.capture());
    //check Request Key
    assertEquals(CannedRequests.CHAN_GRP_TIME_RANGE_REQ, requestCaptor.getValue());
    //check cache response gets Marshelled
    assertFalse(response.isEmpty());
    assertEquals(UtilsTestFixtures.CHANNEL_GROUP, response.get(0));
  }

  @Test
  void findChannelGroupsByNameAndTimeRangeMissTest() {
    when(cache.retrieve(any())).thenReturn(List.of());
    when(delegate.findChannelGroupsByNameAndTimeRange(any(), any(), any())).thenReturn(List.of(UtilsTestFixtures.CHANNEL_GROUP));
    List<ChannelGroup> response = requestCacheAccessor.findChannelGroupsByNameAndTimeRange(List.of(CannedRequests.GENERIC_NAME),
      CannedRequests.START_TIME, CannedRequests.END_TIME);
    //check correct delegate call
    verify(delegate, Mockito.only()).findChannelGroupsByNameAndTimeRange(namesCaptor.capture(),
      startTimeCaptor.capture(), endTimeCaptor.capture());
    assertEquals(CannedRequests.START_TIME, startTimeCaptor.getValue());
    assertEquals(CannedRequests.GENERIC_NAME, namesCaptor.getValue().get(0));
    assertEquals(CannedRequests.END_TIME, endTimeCaptor.getValue());

    //check cache gets populated
    verify(cache).put(requestCaptor.capture(), objectListCaptor.capture());
    assertEquals(CannedRequests.CHAN_GRP_TIME_RANGE_REQ, requestCaptor.getValue());
    assertEquals(UtilsTestFixtures.CHANNEL_GROUP, objectListCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(UtilsTestFixtures.CHANNEL_GROUP, response.get(0));
  }

//  /* ---------------------------
//   * Channels Caching Tests
//   --------------------------- */

  @Test
  void findChannelsByNameAndTimeHitTest() {
    when(cache.retrieve(any())).thenReturn(List.of(UtilsTestFixtures.CHANNEL));
    List<Channel> response = requestCacheAccessor.findChannelsByNameAndTime(List.of(CannedRequests.GENERIC_NAME),
      CannedRequests.START_TIME);
    verify(cache).retrieve(requestCaptor.capture());
    //check Request Key
    assertEquals(CannedRequests.CHAN_TIME_REQ, requestCaptor.getValue());
    //check cache response gets Marshelled
    assertFalse(response.isEmpty());
    assertEquals(UtilsTestFixtures.CHANNEL, response.get(0));
  }

  @Test
  void findChannelsByNameAndTimeMissTest() {
    when(cache.retrieve(any())).thenReturn(List.of());
    when(delegate.findChannelsByNameAndTime(any(), any())).thenReturn(List.of(UtilsTestFixtures.CHANNEL));
    List<Channel> response = requestCacheAccessor.findChannelsByNameAndTime(List.of(CannedRequests.GENERIC_NAME),
      CannedRequests.START_TIME);
    //check correct delegate call
    verify(delegate, Mockito.only()).findChannelsByNameAndTime(namesCaptor.capture(),
      startTimeCaptor.capture());
    assertEquals(CannedRequests.START_TIME, startTimeCaptor.getValue());
    assertEquals(CannedRequests.GENERIC_NAME, namesCaptor.getValue().get(0));

    //check cache gets populated
    verify(cache).put(requestCaptor.capture(), objectListCaptor.capture());
    assertEquals(CannedRequests.CHAN_TIME_REQ, requestCaptor.getValue());
    assertEquals(UtilsTestFixtures.CHANNEL, objectListCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(UtilsTestFixtures.CHANNEL, response.get(0));
  }

  @Test
  void findChannelsByNameAndTimeFacetHitTest() {
    when(cache.retrieve(any())).thenReturn(List.of(UtilsTestFixtures.CHANNEL));
    List<Channel> response = requestCacheAccessor.findChannelsByNameAndTime(List.of(CannedRequests.GENERIC_NAME),
      CannedRequests.START_TIME, CannedRequests.FACET_DEF);
    verify(cache).retrieve(requestCaptor.capture());
    //check Request Key
    assertEquals(CannedRequests.CHAN_TIME_FACET_REQ, requestCaptor.getValue());
    //check cache response gets Marshelled
    assertFalse(response.isEmpty());
    assertEquals(UtilsTestFixtures.CHANNEL, response.get(0));

  }

  @Test
  void findChannelsByNameAndTimeFacetMissTest() {
    when(cache.retrieve(any())).thenReturn(List.of());
    when(delegate.findChannelsByNameAndTime(any(), any(), any())).thenReturn(List.of(UtilsTestFixtures.CHANNEL));
    List<Channel> response = requestCacheAccessor.findChannelsByNameAndTime(List.of(CannedRequests.GENERIC_NAME),
      CannedRequests.START_TIME, CannedRequests.FACET_DEF);
    //check correct delegate call
    verify(delegate, Mockito.only()).findChannelsByNameAndTime(namesCaptor.capture(),
      startTimeCaptor.capture(), facetCaptor.capture());
    assertEquals(CannedRequests.START_TIME, startTimeCaptor.getValue());
    assertEquals(CannedRequests.GENERIC_NAME, namesCaptor.getValue().get(0));
    assertEquals(CannedRequests.FACET_DEF, facetCaptor.getValue());

    //check cache gets populated
    verify(cache).put(requestCaptor.capture(), objectListCaptor.capture());
    assertEquals(CannedRequests.CHAN_TIME_FACET_REQ, requestCaptor.getValue());
    assertEquals(UtilsTestFixtures.CHANNEL, objectListCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(UtilsTestFixtures.CHANNEL, response.get(0));
  }

  @Test
  void findChannelsByNameAndTimeRangeHitTest() {
    when(cache.retrieve(any())).thenReturn(List.of(UtilsTestFixtures.CHANNEL));
    List<Channel> response = requestCacheAccessor.findChannelsByNameAndTimeRange(List.of(CannedRequests.GENERIC_NAME),
      CannedRequests.START_TIME, CannedRequests.END_TIME);
    verify(cache).retrieve(requestCaptor.capture());
    //check Request Key
    assertEquals(CannedRequests.CHAN_TIME_RANGE_REQ, requestCaptor.getValue());
    //check cache response gets Marshelled
    assertFalse(response.isEmpty());
    assertEquals(UtilsTestFixtures.CHANNEL, response.get(0));
  }

  @Test
  void findChannelsByNameAndTimeRangeMissTest() {
    when(cache.retrieve(any())).thenReturn(List.of());
    when(delegate.findChannelsByNameAndTimeRange(any(), any(), any())).thenReturn(List.of(UtilsTestFixtures.CHANNEL));
    List<Channel> response = requestCacheAccessor.findChannelsByNameAndTimeRange(List.of(CannedRequests.GENERIC_NAME),
      CannedRequests.START_TIME, CannedRequests.END_TIME);
    //check correct delegate call
    verify(delegate, Mockito.only()).findChannelsByNameAndTimeRange(namesCaptor.capture(),
      startTimeCaptor.capture(), endTimeCaptor.capture());
    assertEquals(CannedRequests.START_TIME, startTimeCaptor.getValue());
    assertEquals(CannedRequests.GENERIC_NAME, namesCaptor.getValue().get(0));
    assertEquals(CannedRequests.END_TIME, endTimeCaptor.getValue());

    //check cache gets populated
    verify(cache).put(requestCaptor.capture(), objectListCaptor.capture());
    assertEquals(CannedRequests.CHAN_TIME_RANGE_REQ, requestCaptor.getValue());
    assertEquals(UtilsTestFixtures.CHANNEL, objectListCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(UtilsTestFixtures.CHANNEL, response.get(0));
  }

  /* ---------------------------
   * Responses Caching Tests
   --------------------------- */

  @Test
  void findResponsesByIdHitTest() {
    when(cache.retrieve(any())).thenReturn(List.of(UtilsTestFixtures.RESPONSE));
    List<Response> response = requestCacheAccessor.findResponsesById(List.of(CannedRequests.GENERIC_UUID),
      CannedRequests.START_TIME);
    verify(cache).retrieve(requestCaptor.capture());
    //check Request Key
    assertEquals(CannedRequests.RESPONSE_NOFACET_TIME_REQ, requestCaptor.getValue());
    //check cache response gets Marshelled
    assertFalse(response.isEmpty());
    assertEquals(UtilsTestFixtures.RESPONSE, response.get(0));
  }

  @Test
  void findResponsesByIdMissTest() {
    when(cache.retrieve(any())).thenReturn(List.of());
    when(delegate.findResponsesById(List.of(CannedRequests.GENERIC_UUID),
      CannedRequests.START_TIME)).thenReturn(List.of(UtilsTestFixtures.RESPONSE));
    List<Response> response = requestCacheAccessor.findResponsesById(List.of(CannedRequests.GENERIC_UUID),
      CannedRequests.START_TIME);
    //check correct delegate call
    verify(delegate, Mockito.only()).findResponsesById(uuidsCaptor.capture(),
      startTimeCaptor.capture());
    assertEquals(CannedRequests.START_TIME, startTimeCaptor.getValue());
    assertEquals(CannedRequests.GENERIC_UUID, uuidsCaptor.getValue().get(0));

    //check cache gets populated
    verify(cache).put(requestCaptor.capture(), objectListCaptor.capture());
    assertEquals(CannedRequests.RESPONSE_NOFACET_TIME_REQ, requestCaptor.getValue());
    assertEquals(UtilsTestFixtures.RESPONSE, objectListCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(UtilsTestFixtures.RESPONSE, response.get(0));
  }

  @Test
  void findResponsesByIdFacetHit_populated_Test() {
    Response testResponse = RESPONSE_ONE;
    Request request = ResponseTimeFacetRequest.builder()
      .setResponseIds(List.of(testResponse.getId()))
      .setEffectiveTime(Optional.of(CannedRequests.START_TIME))
      .setFacetingDefinition(Optional.of(CannedRequests.RESP_FACET_DEF))
      .build();

    when(cache.retrieve(request)).thenReturn(List.of(testResponse));
    List<Response> response = requestCacheAccessor.findResponsesById(List.of(CannedRequests.GENERIC_UUID),
      CannedRequests.START_TIME, CannedRequests.RESP_FACET_DEF);

    verify(cache).retrieve(requestCaptor.capture());
    //check Request Key
    assertEquals(CannedRequests.RESPONSE_FACET_TIME_REQ, requestCaptor.getValue());
    //check cache response gets Marshelled
    assertFalse(response.isEmpty());
    assertEquals(RESPONSE_ONE, response.get(0));
  }

  @Test
  void findResponsesByIdFacetHit_unpopulated_Test() {
    Response testResponse = RESPONSE_ONE;
    Request request = ResponseTimeFacetRequest.builder()
      .setResponseIds(List.of(testResponse.getId()))
      .setEffectiveTime(Optional.of(CannedRequests.START_TIME))
      .setFacetingDefinition(Optional.of(CannedRequests.RESP_EMPTY_FACET_DEF))
      .build();
    when(cache.retrieve(request)).thenReturn(List.of(testResponse));
    List<Response> response = requestCacheAccessor.findResponsesById(List.of(CannedRequests.GENERIC_UUID),
      CannedRequests.START_TIME, CannedRequests.RESP_EMPTY_FACET_DEF);

    verify(cache).retrieve(requestCaptor.capture());
    //check Request Key
    assertEquals(CannedRequests.RESPONSE_EMPTY_FACET_TIME_REQ, requestCaptor.getValue());
    //check cache response gets Marshelled
    assertFalse(response.isEmpty());
    assertEquals(RESPONSE_ONE_FACET, response.get(0));
  }

  @Test
  void findResponsesByIdFacetMiss_populated_Test() {
    when(cache.retrieve(any())).thenReturn(List.of());
    when(delegate.findResponsesById(List.of(CannedRequests.GENERIC_UUID),
      CannedRequests.START_TIME, CannedRequests.RESP_FACET_DEF))
      .thenReturn(List.of(RESPONSE_ONE));
    List<Response> response = requestCacheAccessor.findResponsesById(List.of(CannedRequests.GENERIC_UUID),
      CannedRequests.START_TIME, CannedRequests.RESP_FACET_DEF);
    //check correct delegate call
    verify(delegate, Mockito.only()).findResponsesById(uuidsCaptor.capture(),
      startTimeCaptor.capture(), facetCaptor.capture());
    assertEquals(CannedRequests.START_TIME, startTimeCaptor.getValue());
    assertEquals(CannedRequests.GENERIC_UUID, uuidsCaptor.getValue().get(0));
    assertEquals(CannedRequests.RESP_FACET_DEF, facetCaptor.getValue());

    //check cache gets populated
    verify(cache).put(requestCaptor.capture(), objectListCaptor.capture());
    assertEquals(CannedRequests.RESPONSE_FACET_TIME_REQ, requestCaptor.getValue());
    assertEquals(RESPONSE_ONE, objectListCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(RESPONSE_ONE, response.get(0));
  }

  @Test
  void findResponsesByIdFacetMiss_unpopulated_Test() {
    when(cache.retrieve(any())).thenReturn(List.of());
    when(delegate.findResponsesById(List.of(CannedRequests.GENERIC_UUID),
      CannedRequests.START_TIME, CannedRequests.RESP_EMPTY_FACET_DEF))
      .thenReturn(List.of(RESPONSE_ONE_FACET));
    List<Response> response = requestCacheAccessor.findResponsesById(List.of(CannedRequests.GENERIC_UUID),
      CannedRequests.START_TIME, CannedRequests.RESP_EMPTY_FACET_DEF);

    //check correct delegate call
    verify(delegate, Mockito.only()).findResponsesById(uuidsCaptor.capture(),
      startTimeCaptor.capture(), facetCaptor.capture());
    assertEquals(CannedRequests.START_TIME, startTimeCaptor.getValue());
    assertEquals(CannedRequests.GENERIC_UUID, uuidsCaptor.getValue().get(0));
    assertEquals(CannedRequests.RESP_EMPTY_FACET_DEF, facetCaptor.getValue());

    //check cache gets populated
    verify(cache).put(requestCaptor.capture(), objectListCaptor.capture());
    assertEquals(CannedRequests.RESPONSE_EMPTY_FACET_TIME_REQ, requestCaptor.getValue());
    assertEquals(RESPONSE_ONE_FACET, objectListCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(RESPONSE_ONE_FACET, response.get(0));
  }

  @Test
  void findResponsesByIdAndTimeRangeHitTest() {
    when(cache.retrieve(any())).thenReturn(List.of(UtilsTestFixtures.RESPONSE));
    List<Response> response = requestCacheAccessor.findResponsesByIdAndTimeRange(List.of(CannedRequests.GENERIC_UUID),
      CannedRequests.START_TIME, CannedRequests.END_TIME);
    verify(cache).retrieve(requestCaptor.capture());
    //check Request Key
    assertEquals(CannedRequests.RESP_TIME_RANGE_REQ, requestCaptor.getValue());
    //check cache response gets Marshelled
    assertFalse(response.isEmpty());
    assertEquals(UtilsTestFixtures.RESPONSE, response.get(0));
  }

  @Test
  void findResponsesByIdAndTimeRangeMissTest() {
    when(cache.retrieve(any())).thenReturn(List.of());
    when(delegate.findResponsesByIdAndTimeRange(any(), any(), any())).thenReturn(List.of(UtilsTestFixtures.RESPONSE));
    List<Response> response = requestCacheAccessor.findResponsesByIdAndTimeRange(List.of(CannedRequests.GENERIC_UUID),
      CannedRequests.START_TIME, CannedRequests.END_TIME);
    //check correct delegate call
    verify(delegate, Mockito.only()).findResponsesByIdAndTimeRange(uuidCaptor.capture(),
      startTimeCaptor.capture(), endTimeCaptor.capture());
    assertEquals(CannedRequests.START_TIME, startTimeCaptor.getValue());
    assertEquals(CannedRequests.GENERIC_UUID, uuidCaptor.getValue().get(0));
    assertEquals(CannedRequests.END_TIME, endTimeCaptor.getValue());

    //check cache gets populated
    verify(cache).put(requestCaptor.capture(), objectListCaptor.capture());
    assertEquals(CannedRequests.RESP_TIME_RANGE_REQ, requestCaptor.getValue());
    assertEquals(UtilsTestFixtures.RESPONSE, objectListCaptor.getValue().get(0));

    //check returns delegate response
    assertEquals(UtilsTestFixtures.RESPONSE, response.get(0));
  }

  @Test
  void storeStationGroupTest() {
    requestCacheAccessor.storeStationGroups(List.of(UtilsTestFixtures.STATION_GROUP));
    verify(delegate).storeStationGroups(stationGroupListCaptor.capture());
  }

  @Test
  void storeStationTest() {
    requestCacheAccessor.storeStations(List.of(UtilsTestFixtures.STATION));
    verify(delegate).storeStations(stationListCaptor.capture());
  }

  @Test
  void storeChannelGroupTest() {
    requestCacheAccessor.storeChannelGroups(List.of(UtilsTestFixtures.CHANNEL_GROUP));
    verify(delegate).storeChannelGroups(channelGroupListCaptor.capture());
  }

  @Test
  void storeChannelTest() {
    requestCacheAccessor.storeChannels(List.of(UtilsTestFixtures.CHANNEL));
    verify(delegate).storeChannels(channelListCaptor.capture());
  }

  @Test
  void testCache() {
    requestCacheAccessor.cache(List.of("TEST"), CannedRequests.START_TIME, CannedRequests.END_TIME);
    verify(delegate).cache(List.of("TEST"), CannedRequests.START_TIME, CannedRequests.END_TIME);
    verifyNoMoreInteractions(delegate);
  }


  private static class CannedRequests {

    public static final UUID GENERIC_UUID = UtilsTestFixtures.CHANNEL.getResponse().orElseThrow().getId();
    public static final String GENERIC_NAME = UtilsTestFixtures.CHANNEL.getName();
    public static final Instant START_TIME = UtilsTestFixtures.CHANNEL.getEffectiveAt().orElseThrow();
    public static final Instant END_TIME = START_TIME.plusSeconds(10);

    public static final FacetingDefinition FACET_DEF = FacetingDefinition.builder()
      .setClassType("Channel")
      .setPopulated(true)
      .setFacetingDefinitions(Map.of())
      .build();
    public static final FacetingDefinition RESP_FACET_DEF = FacetingDefinition.builder()
      .setClassType("Response")
      .setPopulated(true)
      .build();
    public static final FacetingDefinition RESP_EMPTY_FACET_DEF = FacetingDefinition.builder()
      .setClassType("Response")
      .setPopulated(false)
      .build();
    public static final TimeRangeRequest TIME_RANGE_REQ = TimeRangeRequest.builder()
      .setStartTime(START_TIME)
      .setEndTime(END_TIME)
      .build();
    public static final ResponseTimeRangeRequest RESP_TIME_RANGE_REQ = ResponseTimeRangeRequest.builder()
      .setResponseIds(List.of(GENERIC_UUID))
      .setTimeRange(TIME_RANGE_REQ)
      .build();
    public static final Request RESPONSE_FACET_TIME_REQ = ResponseTimeFacetRequest.builder()
      .setResponseIds(List.of(GENERIC_UUID))
      .setEffectiveTime(Optional.of(START_TIME))
      .setFacetingDefinition(Optional.of(RESP_FACET_DEF))
      .build();
    public static final Request RESPONSE_EMPTY_FACET_TIME_REQ = ResponseTimeFacetRequest.builder()
      .setResponseIds(List.of(GENERIC_UUID))
      .setEffectiveTime(Optional.of(START_TIME))
      .setFacetingDefinition(Optional.of(RESP_EMPTY_FACET_DEF))
      .build();
    public static final Request RESPONSE_NOFACET_TIME_REQ = ResponseTimeFacetRequest.builder()
      .setResponseIds(List.of(GENERIC_UUID))
      .setEffectiveTime(Optional.of(START_TIME))
      .build();
    public static final Request CHAN_TIME_REQ = ChannelsTimeRequest.builder()
      .setChannelNames(List.of(GENERIC_NAME))
      .setEffectiveTime(START_TIME)
      .build();
    public static final Request CHAN_TIME_FACET_REQ = ChannelsTimeFacetRequest.builder()
      .setChannelNames(List.of(GENERIC_NAME))
      .setFacetingDefinition(FACET_DEF)
      .setEffectiveTime(START_TIME)
      .build();
    public static final Request STATION_GRP_TIME_REQ = StationGroupsTimeRequest.builder()
      .setStationGroupNames(List.of(GENERIC_NAME))
      .setEffectiveTime(START_TIME)
      .build();
    public static final Request STATION_GRP_TIME_FACET_REQ = StationGroupsTimeFacetRequest.builder()
      .setStationGroupNames(List.of(GENERIC_NAME))
      .setFacetingDefinition(FACET_DEF)
      .setEffectiveTime(START_TIME)
      .build();
    public static final Request STATION_GRP_TIME_RANGE_REQ = StationGroupsTimeRangeRequest.builder()
      .setStationGroupNames(List.of(GENERIC_NAME))
      .setTimeRange(TIME_RANGE_REQ)
      .build();
    public static final Request STATION_TIME_REQ = StationsTimeRequest.builder()
      .setStationNames(List.of(GENERIC_NAME))
      .setEffectiveTime(START_TIME)
      .build();
    public static final Request STATION_TIME_FACET_REQ = StationsTimeFacetRequest.builder()
      .setStationNames(List.of(GENERIC_NAME))
      .setFacetingDefinition(FACET_DEF)
      .setEffectiveTime(START_TIME)
      .build();
    public static final Request STATION_TIME_RANGE_REQ = StationsTimeRangeRequest.builder()
      .setStationNames(List.of(GENERIC_NAME))
      .setTimeRange(TIME_RANGE_REQ)
      .build();
    public static final Request CHAN_GRP_TIME_REQ = ChannelGroupsTimeRequest.builder()
      .setChannelGroupNames(List.of(GENERIC_NAME))
      .setEffectiveTime(START_TIME)
      .build();
    public static final Request CHAN_GRP_TIME_FACET_REQ = ChannelGroupsTimeFacetRequest.builder()
      .setChannelGroupNames(List.of(GENERIC_NAME))
      .setFacetingDefinition(FACET_DEF)
      .setEffectiveTime(START_TIME)
      .build();
    public static final Request CHAN_GRP_TIME_RANGE_REQ = ChannelGroupsTimeRangeRequest.builder()
      .setChannelGroupNames(List.of(GENERIC_NAME))
      .setTimeRange(TIME_RANGE_REQ)
      .build();
    public static final Request CHAN_TIME_RANGE_REQ = ChannelsTimeRangeRequest.builder()
      .setChannelNames(List.of(GENERIC_NAME))
      .setTimeRange(TIME_RANGE_REQ)
      .build();
  }
}
