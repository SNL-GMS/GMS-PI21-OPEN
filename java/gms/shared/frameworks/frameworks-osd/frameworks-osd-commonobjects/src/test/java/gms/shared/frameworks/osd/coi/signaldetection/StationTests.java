package gms.shared.frameworks.osd.coi.signaldetection;

import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.channel.ChannelGroup;
import gms.shared.frameworks.osd.coi.stationreference.RelativePosition;
import gms.shared.frameworks.osd.coi.stationreference.StationType;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StationTests {

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(UtilsTestFixtures.STATION, Station.class);
  }

  @Test
  void testEmptyNameThrowsException() {
    Exception exception = assertThrows(Exception.class, () -> Station.from(
      "",
      StationType.HYDROACOUSTIC,
      "",
      Map.of(UtilsTestFixtures.CHANNEL.getName(), RelativePosition.from(100.0, 50.0, 50.0)),
      Location.from(0.0, 100.0, 50.0, 10.0),
      List.of(),
      List.of(UtilsTestFixtures.CHANNEL)));
    assertEquals("Station must be provided a name", exception.getMessage());
  }

  @Test
  void testNullNameThrowsNullPointerException() {

    Map<String, RelativePosition> mixedMap = Map.of(
      UtilsTestFixtures.CHANNEL.getName(),
      RelativePosition.from(100.0, 50.0, 50.0));
    Location location = Location.from(0.0, 100.0, 50.0, 10.0);
    List<Channel> channelList = List.of(UtilsTestFixtures.CHANNEL);
    NullPointerException exception = Assertions
      .assertThrows(NullPointerException.class, () -> Station.from(
        null,
        StationType.HYDROACOUSTIC,
        "", mixedMap,
        location,
        Collections.EMPTY_LIST,
        channelList));
    assertEquals("Station must be provided a name", exception.getMessage());
  }

  @Test
  void testEmptyMapOfRelativePositionsThrowsException() {
    Exception exception = assertThrows(Exception.class, () -> Station.from(
      "Test Station",
      StationType.HYDROACOUSTIC,
      "",
      Map.of(),
      Location.from(0.0, 100.0, 50.0, 10.0),
      List.of(UtilsTestFixtures.channelGroup),
      List.of(UtilsTestFixtures.CHANNEL)));
    assertEquals(
      "Station being pass an empty or null map of relative positions for channels it manages",
      exception.getMessage());
  }

  @Test
  void testEmptyListOfChannelsThrowsException() {
    Exception exception = assertThrows(Exception.class, () -> Station.from(
      "Test Station",
      StationType.HYDROACOUSTIC,
      "",
      Map.of("Test Channel", RelativePosition.from(10.0, 5.0, 5.0)),
      Location.from(0.0, 100.0, 50.0, 10.0),
      List.of(),
      List.of()));
    assertEquals(
      "Station must have a non-empty list of channels",
      exception.getMessage());
  }

  @Test
  void testEmptyListOfChannelGroupsThrowsException() {
    Exception exception = assertThrows(Exception.class, () -> Station.from(
      "Test Station",
      StationType.HYDROACOUSTIC,
      "",
      Map.of("Test Channel", RelativePosition.from(10.0, 5.0, 5.0)),
      Location.from(0.0, 100.0, 50.0, 10.0),
      List.of(),
      List.of(UtilsTestFixtures.CHANNEL)));
    assertEquals(
      "Station must have a non-empty list of channel groups",
      exception.getMessage());
  }


  @Test
  void testMapOfRelativePositionsNotAssociatedWithChannelsStationManagesThrowsException() {
    Exception exception = assertThrows(Exception.class, () -> Station.from(
      "Test Station",
      StationType.HYDROACOUSTIC,
      "",
      Map.of("Invalid Channel Relative Position", RelativePosition.from(100.0, 55.0, 67.0)),
      Location.from(0.0, 100.0, 50.0, 10.0),
      List.of(UtilsTestFixtures.channelGroup),
      List.of(UtilsTestFixtures.CHANNEL)));
    assertEquals(
      "Station passed in a relative position for a channel it does not manage",
      exception.getMessage());
  }

  @Test
  void testChannelGroupContainsChannelNotInStationThrowsException() {

    assertNotEquals(UtilsTestFixtures.CHANNEL, UtilsTestFixtures.beamed);
    Location location = Location.from(0.0, 100.0, 50.0, 10.0);
    List<ChannelGroup> channelGroupList = List.of(UtilsTestFixtures.channelGroup);
    Map<String, RelativePosition> chanMap = Map.of(UtilsTestFixtures.beamed.getName(),
      RelativePosition.from(100.0, 55.0, 67.0));
    List<Channel> beamedList = List.of(UtilsTestFixtures.beamed);
    final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
      () -> Station.from(
        "Test Station",
        StationType.HYDROACOUSTIC,
        "", chanMap, location, channelGroupList, beamedList
      ));

    assertEquals(
      "Station cannot have ChannelGroups which groups Channels that are not part of the Station.",
      exception.getMessage());
  }
}
