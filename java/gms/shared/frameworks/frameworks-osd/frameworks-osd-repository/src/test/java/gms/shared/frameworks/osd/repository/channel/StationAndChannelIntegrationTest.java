package gms.shared.frameworks.osd.repository.channel;

import gms.shared.frameworks.coi.exceptions.RepositoryException;
import gms.shared.frameworks.osd.api.channel.ChannelRepositoryInterface;
import gms.shared.frameworks.osd.api.station.StationGroupRepositoryInterface;
import gms.shared.frameworks.osd.api.station.StationRepositoryInterface;
import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.channel.ChannelBandType;
import gms.shared.frameworks.osd.coi.channel.ChannelDataType;
import gms.shared.frameworks.osd.coi.channel.ChannelGroup;
import gms.shared.frameworks.osd.coi.channel.ChannelGroup.Type;
import gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType;
import gms.shared.frameworks.osd.coi.channel.ChannelOrientationType;
import gms.shared.frameworks.osd.coi.channel.ChannelProcessingMetadataType;
import gms.shared.frameworks.osd.coi.channel.Orientation;
import gms.shared.frameworks.osd.coi.signaldetection.Location;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroupDefinition;
import gms.shared.frameworks.osd.coi.station.StationTestFixtures;
import gms.shared.frameworks.osd.coi.stationreference.RelativePosition;
import gms.shared.frameworks.osd.coi.stationreference.StationType;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import gms.shared.frameworks.osd.repository.SohPostgresTest;
import gms.shared.frameworks.osd.repository.station.StationGroupRepositoryJpa;
import gms.shared.frameworks.osd.repository.station.StationRepositoryJpa;
import gms.shared.utilities.db.test.utils.TestFixtures;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class StationAndChannelIntegrationTest extends SohPostgresTest {

  private static final ChannelRepositoryInterface channelRepository =
    new ChannelRepositoryJpa(entityManagerFactory);
  private static final StationRepositoryInterface stationRepository = new StationRepositoryJpa(
    entityManagerFactory);
  private static final StationGroupRepositoryInterface stationGroupRepository =
    new StationGroupRepositoryJpa(entityManagerFactory);
  private static final List<Channel> CHANNELS = List.of(
    TestFixtures.channel1,
    TestFixtures.channel2,
    TestFixtures.channel3,
    TestFixtures.channel4,
    TestFixtures.channel5,
    TestFixtures.channel6);
  private static final List<Station> STATIONS = List.of(TestFixtures.station);

  @BeforeAll
  static void beforeAll() {
    stationRepository.storeStations(List.of(UtilsTestFixtures.STATION, TestFixtures.station));
    stationGroupRepository.storeStationGroups(
      List.of(UtilsTestFixtures.STATION_GROUP, StationTestFixtures.getStationGroup()));
  }

  @Test
  void testRetrieveChannels() {
    List<String> channelIds = CHANNELS.stream().map(Channel::getName).collect(Collectors.toList());
    List<Channel> storedChannels = channelRepository.retrieveChannels(channelIds);
    for (Channel channel : CHANNELS) {
      assertTrue(storedChannels.contains(channel));
    }
  }

  @Test
  void testStoreStation() {
    assertDoesNotThrow(() -> stationRepository.storeStations(List.of(TestFixtures.stationTwo)));
  }

  @Test
  void testStoreChannels() {
    channelRepository.storeChannels(List.of(TestFixtures.derivedChannelTwo));
    List<Channel> storedChannels = channelRepository.retrieveChannels(List.of("derivedChannelTwo"));
    assertAll(
      () -> assertFalse(storedChannels.isEmpty()),
      () -> assertEquals(TestFixtures.derivedChannelTwo, storedChannels.get(0))
    );
  }

  @Test
  void testStoringChannelOfNonexistentStationWillThrowException() {
    final Channel invalidChannel = Channel.from(
      "testChannelEight",
      "Test Channel Eight",
      "This is a description of the channel",
      "stationThree",
      ChannelDataType.DIAGNOSTIC_SOH,
      ChannelBandType.BROADBAND,
      ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
      ChannelOrientationType.EAST_WEST,
      'E',
      Units.HERTZ,
      50.0,
      Location.from(100.0, 10.0, 50.0, 100),
      Orientation.from(10.0, 35.0),
      List.of(),
      Map.of(),
      Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, "")
    );
    assertThrows(Exception.class, () -> channelRepository.storeChannels(List.of(invalidChannel)));
  }

  @Test
  void testEmptyListPassedToRetrieveChannelsWillReturnAllChannels() {
    List<Channel> storedChannels = channelRepository.retrieveChannels(List.of());
    for (Channel channel : CHANNELS) {
      assertTrue(storedChannels.contains(channel));
    }
  }

  @Test
  void testStoringChannelWithStationThatDoesNotExistThrowsException() {
    final List<Channel> channels = List.of(
      TestFixtures.channelWithNonExistentStation
    );
    assertThrows(Exception.class, () -> channelRepository.storeChannels(channels));
  }

  @Test
  void testStoringChannelThatExistsAlreadyThrowsException() {
    final List<Channel> channels = List.of(
      TestFixtures.channel1
    );
    assertThrows(Exception.class, () -> channelRepository.storeChannels(channels));
  }

  @Test
  void testRetrieveStations() {
    List<Station> storedStations = stationRepository
      .retrieveAllStations(List.of(TestFixtures.station.getName()));
    Station stored = storedStations.get(0);
    assertAll(
      () -> assertEquals(TestFixtures.station.getName(), stored.getName()),
      () -> assertEquals(TestFixtures.station.getDescription(), stored.getDescription()),
      () -> assertEquals(TestFixtures.station.getLocation(), stored.getLocation()),
      () -> assertEquals(TestFixtures.station.getType(), stored.getType()),
      () -> assertEquals(TestFixtures.station.getChannelGroups(), stored.getChannelGroups()),
      () -> assertEquals(TestFixtures.station.getChannels(), stored.getChannels()),
      () -> assertEquals(TestFixtures.station.getRelativePositionsByChannel(),
        stored.getRelativePositionsByChannel())
    );
  }

  @Test
  void testNoStationsPassedWillRetrieveAllStations() {
    List<Station> storedStations = stationRepository.retrieveAllStations(List.of());

    storedStations.forEach(stored -> {
      if (stored.getName().equalsIgnoreCase("stationOne")) {
        assertAll(
          () -> assertEquals(TestFixtures.station.getName(), stored.getName()),
          () -> assertEquals(TestFixtures.station.getDescription(), stored.getDescription()),
          () -> assertEquals(TestFixtures.station.getLocation(), stored.getLocation()),
          () -> assertEquals(TestFixtures.station.getType(), stored.getType()),
          () -> assertEquals(TestFixtures.station.getChannelGroups(), stored.getChannelGroups()),
          () -> assertEquals(TestFixtures.station.getChannels(), stored.getChannels()),
          () -> assertEquals(TestFixtures.station.getRelativePositionsByChannel(),
            stored.getRelativePositionsByChannel())
        );
      }
    });
  }

  @Test
  void testNullStationPassedWillThrowException() {
    assertThrows(NullPointerException.class, () -> stationRepository.storeStations(null));
  }

  @Test
  void testNullChannelIdListPassedWillThrowException() {
    assertThrows(NullPointerException.class, () -> channelRepository.storeChannels(null));
  }

  @Test
  void testNullStationIdCollectionToRetrieveAllStationsWillThrowException() {
    assertThrows(NullPointerException.class, () -> stationRepository.storeStations(null));
  }

  @Test
  void testPassingNullToRetrieveStationGroupsThrowsException() {
    assertThrows(NullPointerException.class,
      () -> stationGroupRepository.retrieveStationGroups(null));
  }

  @Test
  void testPassingEmptyCollectionToRetrieveStationGroupThrowsException() {
    assertThrows(IllegalArgumentException.class,
      () -> stationGroupRepository.retrieveStationGroups(List.of()));
  }

  @Test
  void testPassingNullToStoreStationGroupsThrowsException() {
    assertThrows(NullPointerException.class, () -> stationGroupRepository.storeStationGroups(null));
  }

  @Test
  void testPassingEmptyCollectionToStoreStationGroupsThrowsException() {
    assertThrows(IllegalArgumentException.class,
      () -> stationGroupRepository.storeStationGroups(List.of()));
  }

  @Test
  void testRetrieveStationGroups() {
    final Channel channel = Channel.from(
      "yetiChannelOne",
      "New Channel One",
      "This is a description of the channel",
      "yetiStation",
      ChannelDataType.DIAGNOSTIC_SOH,
      ChannelBandType.BROADBAND,
      ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
      ChannelOrientationType.EAST_WEST,
      'E',
      Units.HERTZ,
      50.0,
      Location.from(100.0, 150.0, 30, 20),
      Orientation.from(10.0, 35.0),
      List.of(),
      Map.of(),
      Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, "channelGroupOne")
    );

    final ChannelGroup newChannelGroup = ChannelGroup.from(
      "yetiChannelGroup",
      "Another Sample channel group containing all test suite channels",
      null,
      Type.SITE_GROUP,
      List.of(channel));

    final Station station = Station.from(
      "yetiStation",
      StationType.SEISMIC_3_COMPONENT,
      "Sample 3-component station",
      Map.of(
        "yetiChannelOne", RelativePosition.from(25, 35, 35)
      ),
      Location.from(65.75, 135.50, 100.0, 50),
      List.of(newChannelGroup),
      List.of(channel)
    );

    final StationGroup stationGroup = StationGroup.from(
      "YetiPSG",
      "This is a PSG with a station that has not yet been stored",
      List.of(station)
    );
    stationGroupRepository.storeStationGroups(List.of(stationGroup));
    List<StationGroup> storedPsgs = stationGroupRepository
      .retrieveStationGroups(List.of("YetiPSG"));
    assertAll(
      () -> assertEquals(stationGroup.getName(), storedPsgs.get(0).getName()),
      () -> assertEquals(stationGroup.getDescription(),
        storedPsgs.get(0).getDescription())
    );

    assertFalse(storedPsgs.get(0).getStations().isEmpty());
    assertEquals(storedPsgs.get(0).getStations().first(), station);
  }

  @Test
  void testStoreStationGroupWithStationNotCurrentlyInDatabase() {
    final Station station = Station.from(
      "testStationTwo",
      StationType.SEISMIC_3_COMPONENT,
      "Sample 3-component station",
      Map.of(
        "testChannelEight", RelativePosition.from(25, 35, 35)
      ),
      Location.from(65.75, 135.50, 100.0, 50),
      List.of(ChannelGroup.from(
        "testChannelGroupThree",
        "Another Channel Group",
        Location.from(136.76, 65.75, 105.0, 55.0),
        Type.SITE_GROUP,
        List.of(TestFixtures.channel8)
      )),
      List.of(TestFixtures.channel8)
    );
    final StationGroup stationGroup = StationGroup.from(
      "AnotherPSG",
      "This is a PSG with a station that has not yet been stored",
      List.of(station)
    );

    assertDoesNotThrow(() -> stationGroupRepository.storeStationGroups(List.of(stationGroup)));
  }

  @Test
  void testStoreStationWithChannelGroupThatHasNullLocation() {
    final Channel channel = Channel.from(
      "newChannel",
      "Test Channel One",
      "This is a description of the channel",
      TestFixtures.station.getName(),
      ChannelDataType.DIAGNOSTIC_SOH,
      ChannelBandType.BROADBAND,
      ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
      ChannelOrientationType.EAST_WEST,
      'E',
      Units.HERTZ,
      50.0,
      Location.from(100.0, 150.0, 30, 20),
      Orientation.from(10.0, 35.0),
      List.of(),
      Map.of(),
      Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, "channelGroupOne")
    );

    final ChannelGroup newChannelGroup = ChannelGroup.from(
      "channelGroupWithNullLocation",
      "Sample channel group containing all test suite channels",
      null,
      Type.SITE_GROUP,
      List.of(channel));

    final Station station = Station.from(
      "stationWithChannelWithUnknownLocation",
      StationType.SEISMIC_ARRAY,
      "Station that does has a channel with unknown location",
      Map.of(
        "newChannel", RelativePosition.from(30, 55, 120)
      ),
      Location.from(135.75, 65.75, 50.0, 0.0),
      List.of(newChannelGroup),
      List.of(channel));

    assertDoesNotThrow(() -> stationRepository.storeStations(List.of(station)));
  }

  @Test
  void testStoringStationGroupsWithTheSameStations() {
    final Channel channel = Channel.from(
      "newChannel",
      "Test Channel One",
      "This is a description of the channel",
      TestFixtures.station.getName(),
      ChannelDataType.DIAGNOSTIC_SOH,
      ChannelBandType.BROADBAND,
      ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
      ChannelOrientationType.EAST_WEST,
      'E',
      Units.HERTZ,
      50.0,
      Location.from(100.0, 150.0, 30, 20),
      Orientation.from(10.0, 35.0),
      List.of(),
      Map.of(),
      Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, "channelGroupOne")
    );

    final ChannelGroup newChannelGroup = ChannelGroup.from(
      "channelGroupWithNullLocation",
      "Sample channel group containing all test suite channels",
      null,
      Type.SITE_GROUP,
      List.of(channel));

    final Station station = Station.from(
      "stationWithChannelWithUnknownLocation",
      StationType.SEISMIC_ARRAY,
      "Station that does has a channel with unknown location",
      Map.of(
        "newChannel", RelativePosition.from(30, 55, 120)
      ),
      Location.from(135.75, 65.75, 50.0, 0.0),
      List.of(newChannelGroup),
      List.of(channel));

    final StationGroup stationGroup = StationGroup.from(
      "Yet Another PSG",
      "This is a PSG with a station that has not yet been stored",
      List.of(station)
    );

    final StationGroup stationGroupTwo = StationGroup.from(
      "Different PSG",
      "This is a PSG with a station that has been stored",
      List.of(station)
    );

    assertDoesNotThrow(
      () -> stationGroupRepository.storeStationGroups(List.of(stationGroup, stationGroupTwo)));
  }

  @Test
  void testUpdateStationGroupsNewGroup() {
    final StationGroupDefinition newDefinition = StationGroupDefinition.from(
      "updateNewStationGroup",
      "This is a completely new station group that we are creating via StationGroupDefinition. "
        + "This should store a new station group containing all STATIONS successfully",
      STATIONS.stream().map(Station::getName).collect(Collectors.toList()));

    stationGroupRepository.updateStationGroups(List.of(newDefinition));

    final List<StationGroup> stationGroups = stationGroupRepository
      .retrieveStationGroups(List.of(newDefinition.getName()));

    assertEquals(1, stationGroups.size());

    StationGroup newStationGroup = stationGroups.get(0);
    assertEquals(newDefinition.getName(), newStationGroup.getName());
    assertEquals(newDefinition.getDescription(), newStationGroup.getDescription());
    assertTrue(STATIONS.containsAll(newStationGroup.getStations()));
  }

  @Test
  void testUpdateStationGroupsUpdateExisting() {
    stationRepository.storeStations(List.of(TestFixtures.stationTwo));

    final StationGroupDefinition updatedGroupDefinition = StationGroupDefinition.from(
      TestFixtures.STATION_GROUP.getName(),
      "This is an update to an existing station group that we are updating via StationGroupDefinition. "
        + "This should update the existing station group containing all STATIONS (plus one new station) successfully",
      List.of(TestFixtures.station.getName(), TestFixtures.stationTwo.getName()));

    stationGroupRepository.updateStationGroups(List.of(updatedGroupDefinition));

    final List<StationGroup> stationGroups = stationGroupRepository
      .retrieveStationGroups(List.of(updatedGroupDefinition.getName()));

    assertEquals(1, stationGroups.size());

    StationGroup updatedStationGroup = stationGroups.get(0);
    assertEquals(TestFixtures.STATION_GROUP.getName(), updatedStationGroup.getName());
    assertEquals(updatedGroupDefinition.getDescription(), updatedStationGroup.getDescription());
    assertFalse(STATIONS.containsAll(updatedStationGroup.getStations()));
    assertTrue(List.of(TestFixtures.station, TestFixtures.stationTwo)
      .containsAll(updatedStationGroup.getStations()));
  }

  @Test
  void testUpdateStationGroupsReplaceStation() {
    stationRepository.storeStations(List.of(TestFixtures.stationTwo));

    final StationGroupDefinition updatedGroupDefinition = StationGroupDefinition.from(
      TestFixtures.STATION_GROUP.getName(),
      "This is an update to an existing station group that we are updating via StationGroupDefinition. "
        + "This should update the existing station group to replace the one station associated with it with another successfully",
      List.of(TestFixtures.stationTwo.getName()));

    stationGroupRepository.updateStationGroups(List.of(updatedGroupDefinition));

    final List<StationGroup> stationGroups = stationGroupRepository
      .retrieveStationGroups(List.of(updatedGroupDefinition.getName()));

    assertEquals(1, stationGroups.size());

    StationGroup updatedStationGroup = stationGroups.get(0);
    assertEquals(TestFixtures.STATION_GROUP.getName(), updatedStationGroup.getName());
    assertEquals(updatedGroupDefinition.getDescription(), updatedStationGroup.getDescription());
    assertThat(updatedStationGroup.getStations()).containsOnly(TestFixtures.stationTwo);
    assertArrayEquals(List.of(TestFixtures.stationTwo).toArray(), updatedStationGroup.getStations().toArray());
  }

  @Test
  void testUpdateStationGroupsMissingStation() {
    final StationGroupDefinition newDefinitionMissingStation = StationGroupDefinition.from(
      "updateNewStationGroupMissingStation",
      "This is a completely new station group that we are creating via StationGroupDefinition. "
        + "This should fail to store a station group and throw an exception due to missing stations in its name list",
      List.of("thisStationDoesNotExist"));
    var missingStationList = List.of(newDefinitionMissingStation);
    assertThrows(RepositoryException.class,
      () -> stationGroupRepository.updateStationGroups(missingStationList));
  }
}
