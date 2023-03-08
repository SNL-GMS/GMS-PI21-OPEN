package gms.dataacquisition.css.stationrefconverter;

import com.google.common.collect.Multimap;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.channel.ChannelGroup;
import gms.shared.frameworks.osd.coi.channel.ReferenceChannel;
import gms.shared.frameworks.osd.coi.signaldetection.Response;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class StationGroupBuilderTests {

  private static StationGroupBuilder stationGroupBuilder;

  @BeforeAll
  static void setup() throws Exception {
    final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    CssReferenceReader.process(classLoader.getResource("data").getPath(),
      "test_config.network");

    stationGroupBuilder = new StationGroupBuilder(
      CssReferenceReader.getReferenceNetworkMemberships(),
      CssReferenceReader.getReferenceStationMemberships(),
      CssReferenceReader.getReferenceSiteMemberships(),
      CssReferenceReader.getReferenceNetworksByName().values(),
      CssReferenceReader.getReferenceStationsByName().values(),
      CssReferenceReader.getReferenceSitesByName().values(),
      CssReferenceReader.getReferenceChannelsByName().values(),
      CssReferenceReader.getReferenceResponses());
  }

  @Test
  void testBuildStationGroupsAndResponses() {
    // Get the station groups and assert the "test" station group is properly populated
    Pair<Set<StationGroup>, Set<Response>> stationGroupsAndResponses = stationGroupBuilder
      .createStationGroupsAndResponses();
    Set<StationGroup> stationGroups = stationGroupsAndResponses.getLeft();
    Set<Response> responses = stationGroupsAndResponses.getRight();
    assertNotNull(stationGroups);

    Optional<StationGroup> testStationGroup = stationGroups.stream()
      .filter(s -> "test".equals(s.getName())).findFirst();
    testStationGroup.ifPresentOrElse(tsg -> {
      // Check the test station group structure
      assertEquals(7, tsg.getStations().size(),
        "Number of station groups is incorrect, looking for 7 stations found " +
          tsg.getStations().size());
      // Walk thru the stations and confirm each station has channel groups and channels
      for (Station station : tsg.getStations()) {
        assertTrue(station.getChannels().size() > 0, "Station " + station.getName() +
          " channels should not be empty.");
        assertTrue(station.getChannelGroups().size() > 0, "Station " + station.getName() +
          " channel groups should not be empty.");
        // Check each channel group and confirm the channels within each are not empty
        for (ChannelGroup channelGroup : station.getChannelGroups()) {
          assertTrue(channelGroup.getChannels().size() > 0, "ChannelGroup " +
            channelGroup.getName() + " channels should not be empty.");
        }
      }
    }, () -> fail("Failed to find test station group."));

    Multimap<String, ReferenceChannel> referenceChannelsByName = CssReferenceReader
      .getReferenceChannelsByName();
    List<String> uniqueChannelNames = referenceChannelsByName.values().stream()
      .collect(Collectors.groupingBy(ReferenceChannel::getName))
      .values()
      .stream()
      .map(List::stream)
      .map(channelStream -> channelStream
        .max(Comparator.comparing(ReferenceChannel::getActualTime)))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .filter(ReferenceChannel::isActive)
      .map(ReferenceChannel::getName)
      .distinct()
      .collect(Collectors.toList());

    assertEquals(uniqueChannelNames.size(), responses.size(),
      "Expected one response for each ref channel");
    final Map<String, Response> responsesByChanName = responses.stream()
      .collect(toMap(Response::getChannelName, Function.identity()));
    final Set<String> channelsNames = allChannelNames(stationGroups);
    channelsNames.forEach(name -> assertTrue(responsesByChanName.containsKey(name),
      "Response doesn't exist for channel " + name));
  }

  private static Set<String> allChannelNames(Collection<StationGroup> groups) {
    return groups.stream()
      .map(StationGroup::getStations)
      .flatMap(Set::stream)
      .map(Station::getChannelGroups)
      .flatMap(Set::stream)
      .map(ChannelGroup::getChannels)
      .flatMap(Set::stream)
      .map(Channel::getName)
      .collect(toSet());
  }
}