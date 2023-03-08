package gms.core.performancemonitoring.ssam.processor;

import gms.core.performancemonitoring.soh.control.configuration.StationSohDefinition;
import gms.core.performancemonitoring.ssam.control.SohPackage;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringDefinition;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringUiClientParameters;
import gms.core.performancemonitoring.ssam.control.processor.MaterializedViewProcessor;
import gms.core.performancemonitoring.uimaterializedview.QuietedSohStatusChangeUpdate;
import gms.core.performancemonitoring.uimaterializedview.SohQuietAndUnacknowledgedCacheManager;
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
import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.coi.soh.ChannelSoh;
import gms.shared.frameworks.osd.coi.soh.PercentSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import gms.shared.frameworks.osd.coi.soh.quieting.SohStatusChange;
import gms.shared.frameworks.osd.coi.soh.quieting.UnacknowledgedSohStatusChange;
import gms.shared.frameworks.osd.coi.stationreference.RelativePosition;
import gms.shared.frameworks.osd.coi.stationreference.StationType;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

class MaterializedViewProcessorTest {

  //This station name must be unique while testing with the UiStationGenerator
  //due to its static nature
  private static final String STATION_A_NAME = "Station AB";
  private static final Channel CHANNEL_A = Channel.from(
    "Channel A",
    "Test Channel A",
    "This is a description of the channel A",
    STATION_A_NAME,
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
    Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, "channelGroupA")
  );

  private static final ChannelGroup CHANNEL_GROUP_A = ChannelGroup.from(
    "channelGroupWithALocation",
    "Sample channel group containing all test suite channels",
    Location.from(134.23, 33.32, 50, 0),
    Type.SITE_GROUP,
    List.of(CHANNEL_A));

  private static final Station STATION_A = Station.from(
    STATION_A_NAME,
    StationType.SEISMIC_ARRAY,
    "Station that does have a channel with known location",
    Map.of(
      "Channel A", RelativePosition.from(30, 55, 120)
    ),
    Location.from(135.75, 65.75, 50.0, 0.0),
    List.of(CHANNEL_GROUP_A),
    List.of(CHANNEL_A));

  private static final StationGroup STATION_GROUP = StationGroup.from("Station Group A",
    "Description of station group A", List.of(STATION_A));


  @Test
  void testMaterializedViewProcessor() {

    //Add one station,monitor type, channel to the unacknowledged list, this will create
    //station needs attention message for Station A
    var quietAndUnackListsManager = Mockito.mock(SohQuietAndUnacknowledgedCacheManager.class);
    Mockito.when(quietAndUnackListsManager.getUnacknowledgedList()).thenReturn(List.of(
      UnacknowledgedSohStatusChange.from(
        STATION_A_NAME, Set.of(SohStatusChange.from(Instant.now()
          , SohMonitorType.MISSING, CHANNEL_A.getName()))
      )
    ));

    //quiet a fake channel, should not effect the above system message from being created
    Mockito.when(quietAndUnackListsManager.getQuietedSohStatusChanges()).thenReturn(List.of(
      QuietedSohStatusChangeUpdate.create(Instant.now(), Duration.ofDays(10),
        SohMonitorType.MISSING, "Fake Channel B", Optional.empty(),
        "Fake Channel", "a person")
    ));

    var stationSohDefinition = Mockito.mock(StationSohDefinition.class);
    Mockito.when(stationSohDefinition.getStationName())
      .thenReturn(STATION_A.getName());

    var stationSohMonitoringUiClientParameters = Mockito
      .mock(StationSohMonitoringUiClientParameters.class);
    var stationSohMonitoringDefinition = Mockito.mock(StationSohMonitoringDefinition.class);
    Mockito.when(stationSohMonitoringUiClientParameters.getStationSohControlConfiguration())
      .thenReturn(stationSohMonitoringDefinition);
    Mockito.when(stationSohMonitoringDefinition.getStationSohDefinitions())
      .thenReturn(Set.of(stationSohDefinition));

    Sinks.Many<SystemMessage> systemMessagesSink = Sinks.many().multicast().onBackpressureBuffer(Integer.MAX_VALUE, false);

    var materializedViewProcessor = MaterializedViewProcessor.create(quietAndUnackListsManager,
      stationSohMonitoringUiClientParameters, List.of(STATION_GROUP),
      systemMessagesSink);

    var stationSohUUID = UUID.randomUUID();
    var now = Instant.now();

    var capabilitySohRollup = CapabilitySohRollup.create(UUID.randomUUID(), now,
      SohStatus.GOOD,
      STATION_GROUP.getName(),
      Set.of(stationSohUUID),
      Map.of(STATION_A_NAME, SohStatus.GOOD));

    StationSoh stationSoh = StationSoh.from(
      stationSohUUID,
      now,
      STATION_A_NAME,
      Set.of(PercentSohMonitorValueAndStatus.from(5.0, SohStatus.GOOD, SohMonitorType.MISSING)),
      SohStatus.GOOD,
      Set.of(
        ChannelSoh.from(
          CHANNEL_A.getName(),
          SohStatus.GOOD,
          Set.of(
            PercentSohMonitorValueAndStatus
              .from(5.0, SohStatus.GOOD, SohMonitorType.MISSING)
          )
        )
      ),
      Set.of() //empty set of station aggregate
    );

    var uiStationAndStationGroupsList = materializedViewProcessor
      .apply(SohPackage.create(Set.of(capabilitySohRollup), Set.of(stationSoh)));

    systemMessagesSink.tryEmitComplete();

    StepVerifier.create(systemMessagesSink.asFlux())
      .assertNext(systemMessage -> Assertions
        .assertEquals("Station Station AB needs attention", systemMessage.getMessage()))
      .verifyComplete();
    Assertions.assertEquals(1, uiStationAndStationGroupsList.size());
    Assertions.assertFalse(uiStationAndStationGroupsList.get(0).getIsUpdateResponse());
  }

}
