package gms.shared.stationdefinition.accessor;

import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.stationdefinition.api.channel.ChannelGroupRepositoryInterface;
import gms.shared.stationdefinition.api.channel.ChannelRepositoryInterface;
import gms.shared.stationdefinition.api.channel.ResponseRepositoryInterface;
import gms.shared.stationdefinition.api.channel.util.ChannelsTimeRangeRequest;
import gms.shared.stationdefinition.api.channel.util.ChannelsTimeRequest;
import gms.shared.stationdefinition.api.station.StationGroupRepositoryInterface;
import gms.shared.stationdefinition.api.station.StationRepositoryInterface;
import gms.shared.stationdefinition.api.station.util.StationGroupsTimeRangeRequest;
import gms.shared.stationdefinition.api.station.util.StationGroupsTimeRequest;
import gms.shared.stationdefinition.api.station.util.StationsTimeRangeRequest;
import gms.shared.stationdefinition.api.station.util.StationsTimeRequest;
import gms.shared.stationdefinition.api.util.TimeRangeRequest;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelGroup;
import gms.shared.stationdefinition.coi.channel.Response;
import gms.shared.stationdefinition.coi.facets.FacetingDefinition;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.coi.station.StationGroup;
import gms.shared.stationdefinition.dao.css.enums.TagName;
import gms.shared.stationdefinition.facet.StationDefinitionFacetingUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gms.shared.stationdefinition.accessor.BridgedStationDefinitionAccessor.END_BEFORE_START_MESSAGE;
import static gms.shared.stationdefinition.accessor.BridgedStationDefinitionAccessor.NULL_END_TIME_MESSAGE;
import static gms.shared.stationdefinition.accessor.BridgedStationDefinitionAccessor.NULL_START_TIME_MESSAGE;
import static gms.shared.stationdefinition.accessor.BridgedStationDefinitionAccessor.NULL_STATION_MESSAGE;
import static gms.shared.stationdefinition.facet.FacetingTypes.CHANNELS_KEY;
import static gms.shared.stationdefinition.facet.FacetingTypes.CHANNEL_GROUPS_KEY;
import static gms.shared.stationdefinition.facet.FacetingTypes.CHANNEL_GROUP_TYPE;
import static gms.shared.stationdefinition.facet.FacetingTypes.CHANNEL_TYPE;
import static gms.shared.stationdefinition.facet.FacetingTypes.STATIONS_KEY;
import static gms.shared.stationdefinition.facet.FacetingTypes.STATION_GROUP_TYPE;
import static gms.shared.stationdefinition.facet.FacetingTypes.STATION_TYPE;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL_GROUP;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL_GROUP1;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL_TWO;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.RESPONSE_1;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.STATION;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.STATION_GROUP;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.STATION_GROUP1;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BridgedStationDefinitionAccessorTest {

  public static final Instant startTime = Instant.EPOCH;
  public static final Instant endTime = Instant.EPOCH.plusSeconds(10);
  private BridgedStationDefinitionAccessor stationDefinitionAccessor;

  private final Instant now = Instant.now();

  // --------------------------------
  // StationGroup COI test variables
  // --------------------------------

  private final StationGroup stationGroup1_v1 = STATION_GROUP;
  private final StationGroup stationGroup1_v2 = STATION_GROUP.toBuilder()
    .setName(STATION_GROUP.getName())
    .setEffectiveAt(now)
    .setData(STATION_GROUP.getData().orElseThrow().toBuilder()
      .build())
    .build();
  final List<StationGroup> stationGroup1Versions = List.of(stationGroup1_v1, stationGroup1_v2);
  private final String stationGroup1Name = stationGroup1_v1.getName();

  private final StationGroup stationGroup2_v1 = STATION_GROUP1.toBuilder()
    .setName(STATION_GROUP1.getName() + " 2")
    .build();
  private final StationGroup stationGroup2_v2 = STATION_GROUP1.toBuilder()
    .setName(STATION_GROUP1.getName() + " 2")
    .setEffectiveAt(now)
    .setData(STATION_GROUP1.getData().orElseThrow().toBuilder()
      .build())
    .build();
  private final String stationGroup2Name = stationGroup2_v1.getName();

  // --------------------------------
  // Station COI test variables
  // --------------------------------

  private final Station station1_v1 = STATION;
  private final Station station1_v2 = STATION.toBuilder()
    .setEffectiveAt(now)
    .build();
  final List<Station> station1Versions = List.of(station1_v1, station1_v2);
  private final String station1Name = station1_v1.getName();

  private final Station station2_v1 = STATION.toBuilder()
    .setName(station1Name + " 2")
    .build();
  private final String station2Name = station2_v1.getName();
  final List<Station> station2Versions = List.of(STATION);

  // --------------------------------
  // ChannelGroup COI test variables
  // --------------------------------

  private final ChannelGroup channelGroup1_v1 = CHANNEL_GROUP;
  private final ChannelGroup channelGroup1_v2 = CHANNEL_GROUP.toBuilder()
    .setEffectiveAt(now)
    .build();
  final List<ChannelGroup> channelGroup1Versions = List.of(channelGroup1_v1, channelGroup1_v2);
  private final String channelGroup1Name = channelGroup1_v1.getName();

  private final ChannelGroup channelGroup2_v1 = CHANNEL_GROUP1;
  private final ChannelGroup channelGroup2_v2 = CHANNEL_GROUP1.toBuilder()
    .setEffectiveAt(now)
    .build();
  final List<ChannelGroup> channelGroup2Versions = List.of(channelGroup2_v1, channelGroup2_v2);
  private final String channelGroup2Name = channelGroup2_v1.getName();

  // --------------------------------
  // Channel COI test variables
  // --------------------------------

  private final Channel channel1_v1 = CHANNEL;
  private final Channel channel1_v2 = CHANNEL.toBuilder()
    .setEffectiveAt(now)
    .build();
  final List<Channel> channel1Versions = List.of(channel1_v1, channel1_v2);
  private String channel1Name = channel1_v1.getName();

  private final Channel channel2_v1 = CHANNEL_TWO;
  private final Channel channel2_v2 = CHANNEL_TWO.toBuilder()
    .setEffectiveAt(now)
    .build();
  final List<Channel> channel2Versions = List.of(channel2_v1, channel2_v2);
  private String channel2Name = channel2_v1.getName();


  // -------------------------------------
  // StationGroup Request test variables
  // -------------------------------------

  private final StationGroupsTimeRequest stationGroupsTimeRequest = StationGroupsTimeRequest
    .builder()
    .setStationGroupNames(List.of(stationGroup1Name, stationGroup2Name))
    .setEffectiveTime(now)
    .build();

  private final StationGroupsTimeRangeRequest stationGroupsTimeRangeRequest = StationGroupsTimeRangeRequest
    .builder()
    .setStationGroupNames(List.of(stationGroup1Name, stationGroup2Name))
    .setTimeRange(TimeRangeRequest.builder()
      .setStartTime(Instant.EPOCH)
      .setEndTime(now)
      .build())
    .build();

  private final FacetingDefinition stationGroupFacetingDefinition = FacetingDefinition.builder()
    .setPopulated(true)
    .setClassType(STATION_GROUP_TYPE.getValue())
    .setFacetingDefinitions(Map.of(
      STATIONS_KEY.getValue(),
      FacetingDefinition.builder()
        .setPopulated(true)
        .setClassType(STATION_TYPE.getValue())
        .setFacetingDefinitions(Map.of(
          CHANNELS_KEY.getValue(),
          FacetingDefinition.builder()
            .setPopulated(true)
            .setClassType(CHANNEL_TYPE.getValue())
            .build(),
          CHANNEL_GROUPS_KEY.getValue(),
          FacetingDefinition.builder()
            .setPopulated(true)
            .setClassType(CHANNEL_GROUP_TYPE.getValue())
            .setFacetingDefinitions(Map.of(
              CHANNELS_KEY.getValue(),
              FacetingDefinition.builder()
                .setPopulated(true)
                .setClassType(CHANNEL_TYPE.getValue())
                .build()))
            .build()
        ))
        .build()))
    .build();

  // --------------------------------
  // Station Request test variables
  // --------------------------------

  private final StationsTimeRequest stationsTimeRequest = StationsTimeRequest.builder()
    .setStationNames(List.of(station1Name, station2Name))
    .setEffectiveTime(now)
    .build();

  private final StationsTimeRangeRequest stationsTimeRangeRequest = StationsTimeRangeRequest.builder()
    .setStationNames(List.of(station1Name, station2Name))
    .setTimeRange(TimeRangeRequest.builder()
      .setStartTime(Instant.EPOCH)
      .setEndTime(now)
      .build())
    .build();

  private final FacetingDefinition stationFacetingDefinition = FacetingDefinition.builder()
    .setPopulated(true)
    .setClassType(STATION_TYPE.getValue())
    .setFacetingDefinitions(Map.of(
      CHANNELS_KEY.getValue(),
      FacetingDefinition.builder()
        .setPopulated(true)
        .setClassType(CHANNEL_TYPE.getValue())
        .build(),
      CHANNEL_GROUPS_KEY.getValue(),
      FacetingDefinition.builder()
        .setPopulated(true)
        .setClassType(CHANNEL_GROUP_TYPE.getValue())
        .setFacetingDefinitions(Map.of(
          CHANNELS_KEY.getValue(),
          FacetingDefinition.builder()
            .setPopulated(true)
            .setClassType(CHANNEL_TYPE.getValue())
            .build()))
        .build()))
    .build();

  // ------------------------------------
  // ChannelGroup Request test variables
  // ------------------------------------

  private final FacetingDefinition channelGroupFacetingDefinition = FacetingDefinition.builder()
    .setPopulated(true)
    .setClassType(CHANNEL_GROUP_TYPE.getValue())
    .setFacetingDefinitions(Map.of(CHANNELS_KEY.getValue(),
      FacetingDefinition.builder()
        .setPopulated(true)
        .setClassType(CHANNEL_TYPE.getValue())
        .build()))
    .build();

  // --------------------------------
  // Channel Request test variables
  // --------------------------------

  private final ChannelsTimeRequest channelsTimeRequest = ChannelsTimeRequest.builder()
    .setChannelNames(List.of(channel1Name, channel2Name))
    .setEffectiveTime(now)
    .build();

  private final ChannelsTimeRangeRequest channelsTimeRangeRequest = ChannelsTimeRangeRequest
    .builder()
    .setChannelNames(List.of(channel1Name, channel2Name))
    .setTimeRange(TimeRangeRequest.builder()
      .setStartTime(Instant.EPOCH)
      .setEndTime(now)
      .build())
    .build();

  private final FacetingDefinition channelsFacetingDefinition = FacetingDefinition.builder()
    .setClassType("Channel")
    .setPopulated(false)
    .setFacetingDefinitions(Map.of())
    .build();

  private static final FacetingDefinition RESPONSE_FACETING_DEFINITION = FacetingDefinition.builder()
    .setClassType("Response")
    .setPopulated(true)
    .setFacetingDefinitions(Map.of())
    .build();

  @Mock
  private SystemConfig systemConfig;
  @Mock
  private StationGroupRepositoryInterface stationGroupRepository;
  @Mock
  private StationRepositoryInterface stationRepository;
  @Mock
  private ChannelGroupRepositoryInterface channelGroupRepository;
  @Mock
  private ChannelRepositoryInterface channelRepository;
  @Mock
  private ResponseRepositoryInterface responseRepository;

  @BeforeEach
  public void testSetup() {
    stationDefinitionAccessor = new BridgedStationDefinitionAccessor(
      systemConfig,
      stationGroupRepository,
      stationRepository,
      channelGroupRepository,
      channelRepository,
      responseRepository);
    assertNotNull(stationDefinitionAccessor);
    ReflectionTestUtils.setField(stationDefinitionAccessor, "stationDefinitionFacetingUtility",
      StationDefinitionFacetingUtility.create(stationDefinitionAccessor));
  }

  @Test
  void testFindStationGroupsByNameAndTime() {
    List<String> stationGroupNames = stationGroupsTimeRequest.getStationGroupNames();
    Instant effectiveTime = stationGroupsTimeRequest.getEffectiveTime();
    when(stationGroupRepository.findStationGroupsByNameAndTime(stationGroupNames, effectiveTime))
      .thenReturn(stationGroup1Versions);

    final List<StationGroup> result = stationDefinitionAccessor
      .findStationGroupsByNameAndTime(stationGroupNames, effectiveTime);

    assertEquals(stationGroup1Versions, result);
    verify(stationGroupRepository, times(1)).findStationGroupsByNameAndTime(stationGroupNames, effectiveTime);
    verifyNoMoreInteractions(stationGroupRepository);
  }

  @Test
  void testFindStationGroupsByNameAndTimeRange() {
    List<String> stationGroupNames = stationGroupsTimeRangeRequest.getStationGroupNames();
    Instant startTime = stationsTimeRangeRequest.getTimeRange().getStartTime();
    Instant endTime = stationsTimeRangeRequest.getTimeRange().getEndTime();
    when(stationGroupRepository.findStationGroupsByNameAndTimeRange(stationGroupNames, startTime, endTime))
      .thenReturn(stationGroup1Versions);

    final List<StationGroup> result = stationDefinitionAccessor
      .findStationGroupsByNameAndTimeRange(stationGroupNames, startTime, endTime);

    assertEquals(stationGroup1Versions, result);
    verify(stationGroupRepository, times(1)).findStationGroupsByNameAndTimeRange(stationGroupNames,
      startTime, endTime);
    verifyNoMoreInteractions(stationGroupRepository);
  }

  @Test
  void testFindStationGroupsByName() {
    final List<String> stationGroupNames = List.of(stationGroup1Name);
    when(stationDefinitionAccessor.findStationGroupsByNameAndTime(stationGroupNames, now)).thenReturn(
      stationGroup1Versions);

    final List<StationGroup> result = stationDefinitionAccessor.findStationGroupsByNameAndTime(stationGroupNames, now);

    assertEquals(stationGroup1Versions, result);
    verify(stationGroupRepository, times(1)).findStationGroupsByNameAndTime(stationGroupNames, now);
    verifyNoMoreInteractions(stationGroupRepository);
  }

  @Test
  void testFindStationGroupsByNameAndTimeFacet() {
    List<String> stationGroupNames = stationGroupsTimeRequest.getStationGroupNames();
    Instant effectiveTime = stationGroupsTimeRequest.getEffectiveTime();
    when(stationGroupRepository.findStationGroupsByNameAndTime(stationGroupNames, effectiveTime))
      .thenReturn(stationGroup1Versions);

    final List<StationGroup> result = stationDefinitionAccessor
      .findStationGroupsByNameAndTime(stationGroupNames, effectiveTime, stationGroupFacetingDefinition);

    result.forEach(stationGroup -> {
      assertTrue(stationGroup.isPresent());

      stationGroup.getStations().forEach(stat -> {
        assertTrue(stat.isPresent());
        stat.getChannelGroups().forEach(channelGroup -> {
          assertTrue(channelGroup.isPresent());
          channelGroup.getChannels().forEach(channel -> assertTrue(channel.isPresent()));
        });
      });

    });
    verify(stationGroupRepository, times(1)).findStationGroupsByNameAndTime(stationGroupNames, effectiveTime);
    verifyNoMoreInteractions(stationGroupRepository);
  }

  @Test
  void testFindStationsByNameAndTime() {
    List<String> stationNames = stationsTimeRequest.getStationNames();
    Instant effectiveTime = stationsTimeRequest.getEffectiveTime();
    when(stationRepository.findStationsByNameAndTime(stationNames, effectiveTime)).thenReturn(station2Versions);

    final List<Station> result = stationDefinitionAccessor.findStationsByNameAndTime(stationNames, effectiveTime);

    assertEquals(station2Versions, result);
    verify(stationRepository, times(1)).findStationsByNameAndTime(stationNames, effectiveTime);
    verifyNoMoreInteractions(stationRepository);
  }

  @Test
  void testFindStationsByNameAndTimeRange() {
    List<String> stationNames = stationsTimeRangeRequest.getStationNames();
    Instant startTime = stationsTimeRangeRequest.getTimeRange().getStartTime();
    Instant endTime = stationsTimeRangeRequest.getTimeRange().getEndTime();
    when(stationRepository.findStationsByNameAndTimeRange(stationNames, startTime, endTime))
      .thenReturn(station2Versions);

    final List<Station> result = stationDefinitionAccessor.findStationsByNameAndTimeRange(stationNames, startTime, endTime);

    assertEquals(station2Versions, result);
    verify(stationRepository, times(1)).findStationsByNameAndTimeRange(stationNames, startTime, endTime);
    verifyNoMoreInteractions(stationRepository);
  }

  @Test
  void testFindStationsByName() {
    final List<String> stationNames = List.of(station1Name);
    when(stationRepository.findStationsByNameAndTime(stationNames, now)).thenReturn(station1Versions);

    final List<Station> result = stationDefinitionAccessor.findStationsByNameAndTime(stationNames, now);

    assertEquals(station1Versions, result);
    verify(stationRepository, times(1)).findStationsByNameAndTime(stationNames, now);
    verifyNoMoreInteractions(stationRepository);
  }

  @Test
  void testFindStationsByNameAndTimeFacet() {
    List<String> stationNames = stationsTimeRequest.getStationNames();
    Instant effectiveTime = stationsTimeRequest.getEffectiveTime();
    when(stationRepository.findStationsByNameAndTime(stationNames, effectiveTime)).thenReturn(station2Versions);

    final List<Station> result = stationDefinitionAccessor.findStationsByNameAndTime(stationNames, effectiveTime,
      stationFacetingDefinition);

    result.forEach(stat -> {
      assertTrue(stat.isPresent());
      stat.getChannelGroups().forEach(channelGroup -> {
        assertTrue(channelGroup.isPresent());
        channelGroup.getChannels().forEach(channel -> assertTrue(channel.isPresent()));
      });
    });
    verify(stationRepository, times(1)).findStationsByNameAndTime(stationNames, effectiveTime);
    verifyNoMoreInteractions(stationRepository);
  }

  @ParameterizedTest
  @MethodSource("getDetermineStationChangeTimesArguments")
  void testDetermineStationChangeTimesValidation(Class<? extends Exception> expectedException,
    String expectedMessage,
    Station station,
    Instant startTime,
    Instant endTime) {

    Exception exception = assertThrows(expectedException,
      () -> stationDefinitionAccessor.determineStationChangeTimes(station, startTime, endTime));
    assertEquals(expectedMessage, exception.getMessage());
  }

  static Stream<Arguments> getDetermineStationChangeTimesArguments() {
    return Stream.of(
      arguments(NullPointerException.class,
        NULL_STATION_MESSAGE,
        null,
        startTime,
        endTime),
      arguments(NullPointerException.class,
        NULL_START_TIME_MESSAGE,
        STATION,
        null,
        endTime),
      arguments(NullPointerException.class,
        NULL_END_TIME_MESSAGE,
        STATION,
        startTime,
        null),
      arguments(IllegalStateException.class,
        END_BEFORE_START_MESSAGE,
        STATION,
        endTime,
        startTime));
  }

  @Test
  void testDetermineStationChangeTimes() {
    when(stationRepository.findStationsByNameAndTimeRange(List.of(STATION.getName()), startTime, endTime))
      .thenReturn(List.of(STATION));
    List<String> channelGroupNames = STATION.getChannelGroups().stream()
      .map(ChannelGroup::getName)
      .collect(Collectors.toList());
    when(channelGroupRepository.findChannelGroupsByNameAndTimeRange(channelGroupNames, startTime, endTime))
      .thenReturn(List.copyOf(STATION.getChannelGroups()));
    List<String> channelNames = STATION.getAllRawChannels().stream()
      .map(Channel::getName)
      .collect(Collectors.toList());
    when(channelRepository.findChannelsByNameAndTimeRange(channelNames, startTime, endTime))
      .thenReturn(List.copyOf(STATION.getAllRawChannels()));

    List<Response> responses = STATION.getAllRawChannels().stream()
      .map(Channel::getResponse)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toList());
    List<UUID> responseIds = responses.stream()
      .map(Response::getId)
      .collect(Collectors.toList());
    when(responseRepository.findResponsesByIdAndTimeRange(responseIds, startTime, endTime))
      .thenReturn(responses);

    List<Instant> expectedChangeTimes = Stream.concat(Stream.concat(Stream.concat(Stream.of(STATION.getEffectiveAt()),
            STATION.getChannelGroups().stream().map(ChannelGroup::getEffectiveAt)),
          STATION.getAllRawChannels().stream().map(Channel::getEffectiveAt)),
        responses.stream().map(Response::getEffectiveAt))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .filter(instant -> !instant.isAfter(endTime))
      .sorted(Comparator.reverseOrder())
      .distinct()
      .collect(Collectors.toList());

    List<Instant> actualChangeTimes = stationDefinitionAccessor.determineStationChangeTimes(STATION,
      startTime,
      endTime);

    assertEquals(expectedChangeTimes, actualChangeTimes);

    verify(stationRepository).findStationsByNameAndTimeRange(List.of(STATION.getName()), startTime, endTime);
    verify(channelGroupRepository).findChannelGroupsByNameAndTimeRange(channelGroupNames, startTime, endTime);
    verify(channelRepository).findChannelsByNameAndTimeRange(channelNames, startTime, endTime);
    verify(responseRepository).findResponsesByIdAndTimeRange(responseIds, startTime, endTime);
    verifyNoMoreInteractions(stationGroupRepository,
      stationRepository,
      channelGroupRepository,
      channelRepository,
      responseRepository);
  }

  @Test
  void testFindChannelGroupsByNameAndTime() {
    final List<String> channelGroupNames = List.of(channelGroup2Name, channelGroup1Name);
    when(channelGroupRepository.findChannelGroupsByNameAndTime(channelGroupNames, now))
      .thenReturn(channelGroup1Versions);

    final List<ChannelGroup> result = stationDefinitionAccessor
      .findChannelGroupsByNameAndTime(channelGroupNames, now);

    assertEquals(channelGroup1Versions, result);
    verify(channelGroupRepository, times(1)).findChannelGroupsByNameAndTime(channelGroupNames, now);
    verifyNoMoreInteractions(channelGroupRepository);
  }

  @Test
  void testFindChannelGroupsByName() {
    final List<String> channelGroupNames = List.of(channelGroup1Name);
    when(channelGroupRepository.findChannelGroupsByNameAndTime(eq(channelGroupNames), any()))
      .thenReturn(channelGroup1Versions);

    final List<ChannelGroup> result = stationDefinitionAccessor
      .findChannelGroupsByNameAndTime(channelGroupNames, now);

    assertEquals(channelGroup1Versions, result);
    verify(channelGroupRepository, times(1)).findChannelGroupsByNameAndTime(eq(channelGroupNames), any());
    verifyNoMoreInteractions(channelGroupRepository);
  }

  @Test
  void testFindChannelGroupsByNameAndTimeRange() {
    final List<String> channelGroupNames = List.of(channelGroup1Name);
    when(channelGroupRepository.findChannelGroupsByNameAndTimeRange(channelGroupNames, Instant.EPOCH, now))
      .thenReturn(channelGroup1Versions);

    final List<ChannelGroup> result = stationDefinitionAccessor
      .findChannelGroupsByNameAndTimeRange(channelGroupNames, Instant.EPOCH, now);

    assertEquals(channelGroup1Versions, result);
    verify(channelGroupRepository, times(1)).findChannelGroupsByNameAndTimeRange(channelGroupNames,
      Instant.EPOCH, now);
    verifyNoMoreInteractions(channelGroupRepository);
  }

  @Test
  void testFindChannelGroupsByNameAndTimeFacet() {
    final List<String> channelGroupNames = List.of(channelGroup2Name, channelGroup1Name);
    when(channelGroupRepository.findChannelGroupsByNameAndTime(channelGroupNames, now))
      .thenReturn(channelGroup1Versions);

    final List<ChannelGroup> result = stationDefinitionAccessor
      .findChannelGroupsByNameAndTime(channelGroupNames, now, channelGroupFacetingDefinition);

    result.forEach(chanGroup -> {
      assertTrue(chanGroup.isPresent());
      chanGroup.getChannels().forEach(chan -> assertTrue(chan.isPresent()));
    });
    verify(channelGroupRepository, times(1)).findChannelGroupsByNameAndTime(channelGroupNames, now);
    verifyNoMoreInteractions(channelGroupRepository);
  }

  @Test
  void testFindChannelsByNameAndTime() {
    List<String> channelNames = channelsTimeRequest.getChannelNames();
    Instant effectiveTime = channelsTimeRequest.getEffectiveTime();
    when(channelRepository.findChannelsByNameAndTime(channelNames, effectiveTime)).thenReturn(channel1Versions);

    final List<Channel> result = stationDefinitionAccessor.findChannelsByNameAndTime(channelNames, effectiveTime);

    assertEquals(channel1Versions, result);
    verify(channelRepository, times(1)).findChannelsByNameAndTime(channelNames, effectiveTime);
    verifyNoMoreInteractions(channelRepository);
  }

  @Test
  void testFindChannelsByNameAndTimeRange() {
    List<String> channelNames = channelsTimeRequest.getChannelNames();
    Instant startTime = channelsTimeRangeRequest.getTimeRange().getStartTime();
    Instant endTime = channelsTimeRangeRequest.getTimeRange().getEndTime();
    when(channelRepository.findChannelsByNameAndTimeRange(channelNames, startTime, endTime)).thenReturn(channel1Versions);

    final List<Channel> result = stationDefinitionAccessor.findChannelsByNameAndTimeRange(channelNames, startTime, endTime);

    assertEquals(channel1Versions, result);
    verify(channelRepository, times(1))
      .findChannelsByNameAndTimeRange(channelNames, startTime, endTime);
    verifyNoMoreInteractions(channelRepository);
  }

  @Test
  void testLoadChannelFromWfdisc() {
    List<Long> wfids = Collections.singletonList(1L);
    Optional<Long> filterId = Optional.of(1L);
    Optional<Long> associatedRecordId = Optional.of(1L);
    Optional<TagName> associatedRecordType = Optional.of(TagName.ARID);
    Instant channelEffectiveTime = Instant.EPOCH;
    Instant channelEndTime = channelEffectiveTime.plus(Duration.ofSeconds(5));
    when(channelRepository.loadChannelFromWfdisc(wfids, associatedRecordType,
      associatedRecordId, filterId, channelEffectiveTime, channelEndTime))
      .thenReturn(channel1_v1);

    final Channel result = stationDefinitionAccessor.loadChannelFromWfdisc(wfids, associatedRecordType,
      associatedRecordId, filterId, channelEffectiveTime, channelEndTime);

    assertEquals(channel1_v1, result);
    verify(channelRepository, times(1))
      .loadChannelFromWfdisc(wfids, associatedRecordType,
        associatedRecordId, filterId, channelEffectiveTime, channelEndTime);
    verifyNoMoreInteractions(channelRepository);

  }

  @Test
  void testFindChannelsByNameAndTimeFacet() {
    final List<String> channelNames = List.of(channel1Name);
    when(channelRepository.findChannelsByNameAndTime(channelNames, now)).thenReturn(channel1Versions);

    final List<Channel> result = stationDefinitionAccessor.findChannelsByNameAndTime(channelNames, now,
      channelsFacetingDefinition);

    List<Channel> expected = channel1Versions.stream()
      .map(channel -> channel.toBuilder().setData(Optional.empty()).build())
      .collect(Collectors.toList());
    assertEquals(expected, result);
    verify(channelRepository, times(1)).findChannelsByNameAndTime(eq(channelNames), any());
    // TODO: update when we can mock static methods.
    verifyNoMoreInteractions(channelRepository);
  }

  @ParameterizedTest
  @MethodSource("getFindResponsesByIdArguments")
  void testFindResponsesByIdValidation(List<UUID> responseIds, Instant effectiveTime) {
    assertThrows(NullPointerException.class,
      () -> stationDefinitionAccessor.findResponsesById(responseIds, effectiveTime));
  }

  static Stream<Arguments> getFindResponsesByIdArguments() {
    return Stream.of(arguments(null, Instant.EPOCH),
      arguments(List.of(UUID.randomUUID()), null));
  }

  @Test
  void testFindResponsesById() {
    List<UUID> uuids = List.of(RESPONSE_1.getId());
    when(responseRepository.findResponsesById(uuids, now)).thenReturn(List.of(RESPONSE_1));

    List<Response> actual = stationDefinitionAccessor.findResponsesById(uuids, now);
    assertEquals(List.of(RESPONSE_1), actual);
    verify(responseRepository, times(1)).findResponsesById(uuids, now);
    verifyNoMoreInteractions(responseRepository);
  }

  @ParameterizedTest
  @MethodSource("getFindResponsesByIdFacetArguments")
  void testFindResponsesByIdFacetValidation(List<UUID> responseIds,
    Instant effectiveTime,
    FacetingDefinition facetingDefinition) {

    assertThrows(NullPointerException.class,
      () -> stationDefinitionAccessor.findResponsesById(responseIds, effectiveTime, facetingDefinition));
  }

  static Stream<Arguments> getFindResponsesByIdFacetArguments() {
    return Stream.of(arguments(null, Instant.EPOCH, RESPONSE_FACETING_DEFINITION),
      arguments(List.of(UUID.randomUUID()), null, RESPONSE_FACETING_DEFINITION),
      arguments(List.of(UUID.randomUUID()), Instant.EPOCH, null));
  }

  @Test
  void testFindResponseByIdFacet() {
    List<UUID> uuids = List.of(RESPONSE_1.getId());
    when(responseRepository.findResponsesById(uuids, now)).thenReturn(List.of(RESPONSE_1));

    List<Response> actual = stationDefinitionAccessor.findResponsesById(uuids, now, RESPONSE_FACETING_DEFINITION);
    assertEquals(List.of(RESPONSE_1), actual);
    verify(responseRepository, times(1)).findResponsesById(uuids, now);
    verifyNoMoreInteractions(responseRepository);
  }

  @ParameterizedTest
  @MethodSource("getFindResponsesByIdAndTimeRangeArguments")
  void testFindResponsesByIdAndTimeRangeValidation(Class<? extends Exception> expectedException,
    List<UUID> responseIds,
    Instant startTime,
    Instant endTime) {

    assertThrows(expectedException,
      () -> stationDefinitionAccessor.findResponsesByIdAndTimeRange(responseIds, startTime, endTime));
  }

  static Stream<Arguments> getFindResponsesByIdAndTimeRangeArguments() {
    Instant startTime = Instant.EPOCH;
    Instant endTime = Instant.EPOCH.plusSeconds(300);
    return Stream.of(
      arguments(NullPointerException.class,
        null,
        startTime,
        endTime),
      arguments(NullPointerException.class,
        List.of(UUID.randomUUID()),
        null,
        endTime),
      arguments(NullPointerException.class,
        List.of(UUID.randomUUID()),
        startTime,
        null),
      arguments(IllegalStateException.class,
        List.of(UUID.randomUUID()),
        endTime,
        startTime));
  }

  @Test
  void testFindResponsesByIdAndTimeRange() {
    List<UUID> responseIds = List.of(RESPONSE_1.getId());
    Instant startTime = Instant.EPOCH;
    Instant endTime = Instant.EPOCH.plusSeconds(300);

    when(responseRepository.findResponsesByIdAndTimeRange(responseIds, startTime, endTime))
      .thenReturn(List.of(RESPONSE_1));

    List<Response> actual = stationDefinitionAccessor.findResponsesByIdAndTimeRange(responseIds, startTime, endTime);
    assertEquals(List.of(RESPONSE_1), actual);

    verify(responseRepository, times(1))
      .findResponsesByIdAndTimeRange(responseIds, startTime, endTime);
    verifyNoMoreInteractions(responseRepository);
  }
}