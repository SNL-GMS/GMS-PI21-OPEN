package gms.core.performancemonitoring.ssam.processor;

import gms.core.performancemonitoring.soh.control.configuration.StationSohDefinition;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringDefinition;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringUiClientParameters;
import gms.core.performancemonitoring.ssam.control.processor.AcknowledgeSohStatusChangeMaterializedViewProcessor;
import gms.core.performancemonitoring.uimaterializedview.AcknowledgedSohStatusChange;
import gms.core.performancemonitoring.uimaterializedview.QuietedSohStatusChangeUpdate;
import gms.core.performancemonitoring.uimaterializedview.SohQuietAndUnacknowledgedCacheManager;
import gms.core.performancemonitoring.uimaterializedview.UiStationAndStationGroups;
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

import static org.mockito.Mockito.mock;


class AcknowledgeSohStatusChangeMaterializedViewProcessorTests {

  public static final String STATION_A_NAME = "Station A";

  public static final Channel CHANNEL_A = Channel.from(
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

  public static final ChannelGroup CHANNEL_GROUP_A = ChannelGroup.from(
    "channelGroupWithALocation",
    "Sample channel group containing all test suite channels",
    Location.from(134.23, 33.32, 50, 0),
    Type.SITE_GROUP,
    List.of(CHANNEL_A)
  );

  public static final Station STATION_A = Station.from(
    STATION_A_NAME,
    StationType.SEISMIC_ARRAY,
    "Station that does have a channel with known location",
    Map.of("Channel A", RelativePosition.from(30, 55, 120)),
    Location.from(135.75, 65.75, 50.0, 0.0),
    List.of(CHANNEL_GROUP_A),
    List.of(CHANNEL_A)
  );

  public static final StationGroup STATION_GROUP = StationGroup
    .from("Station Group A", "Description of station group A", List.of(STATION_A));


  @Test
  void testCreate() {

    StationSohDefinition mockedStationSohDefinition = mock(StationSohDefinition.class);
    Mockito.when(mockedStationSohDefinition.getStationName()).thenReturn(STATION_A.getName());

    StationSohMonitoringDefinition mockedStationSohMonitoringDefinition =
      mock(StationSohMonitoringDefinition.class);
    Mockito.when(mockedStationSohMonitoringDefinition.getStationSohDefinitions())
      .thenReturn(Set.of(mockedStationSohDefinition));

    StationSohMonitoringUiClientParameters mockedStationSohMonitoringUiClientParameters =
      mock(StationSohMonitoringUiClientParameters.class);
    Mockito.when(mockedStationSohMonitoringUiClientParameters.getStationSohControlConfiguration())
      .thenReturn(mockedStationSohMonitoringDefinition);

    // Add one station,monitor type, channel to the unacknowledged list.
    // This will create a station that needs attention message.
    SohQuietAndUnacknowledgedCacheManager mockedSohQuietAndUnacknowledgedCacheManager =
      mock(SohQuietAndUnacknowledgedCacheManager.class);
    Mockito.when(mockedSohQuietAndUnacknowledgedCacheManager.getUnacknowledgedList())
      .thenReturn(
        List.of(
          UnacknowledgedSohStatusChange.from(
            STATION_A_NAME,
            Set.of(
              SohStatusChange.from(
                Instant.now(),
                SohMonitorType.MISSING,
                CHANNEL_A.getName()
              )
            )
          )
        )
      );

    // Quiet a fake channel.  Should not effect the above system message from being created.
    Mockito.when(mockedSohQuietAndUnacknowledgedCacheManager.getQuietedSohStatusChanges())
      .thenReturn(
        List.of(
          QuietedSohStatusChangeUpdate.create(
            Instant.now(),
            Duration.ofDays(10),
            SohMonitorType.MISSING,
            "Fake Channel B",
            Optional.empty(),
            "Fake Channel",
            "Bartholomew T. McGillicuddy"
          )
        )
      );

    UUID stationSohUUID = UUID.randomUUID();
    Instant now = Instant.now();

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

    CapabilitySohRollup capabilitySohRollup = CapabilitySohRollup.create(UUID.randomUUID(), now,
      SohStatus.GOOD,
      STATION_GROUP.getName(),
      Set.of(stationSohUUID),
      Map.of(STATION_A_NAME, SohStatus.GOOD));

    Sinks.Many<SystemMessage> systemMessagesSink = Sinks.many().multicast().onBackpressureBuffer();

    AcknowledgeSohStatusChangeMaterializedViewProcessor acknowledgeSohStatusChangeMaterializedViewProcessor =
      AcknowledgeSohStatusChangeMaterializedViewProcessor.create(
        mockedStationSohMonitoringUiClientParameters,
        mockedSohQuietAndUnacknowledgedCacheManager,
        Map.of(STATION_A_NAME, stationSoh),
        Map.of(STATION_A_NAME, capabilitySohRollup),
        systemMessagesSink,
        List.of(STATION_GROUP)
      );

    AcknowledgedSohStatusChange acknowledgedSohStatusChange = AcknowledgedSohStatusChange.from(
      UUID.randomUUID(),
      "The Great And Powerful Oz",
      now,
      Optional.empty(),
      List.of(SohStatusChange.from(now, SohMonitorType.MISSING, CHANNEL_A.getName())),
      STATION_A_NAME
    );

    List<UiStationAndStationGroups> uiStationAndStationGroupsList = acknowledgeSohStatusChangeMaterializedViewProcessor
      .apply(acknowledgedSohStatusChange);

    systemMessagesSink.tryEmitComplete();

    StepVerifier.create(systemMessagesSink.asFlux())
      .assertNext(systemMessage -> Assertions
        .assertEquals("Station Station A needs attention", systemMessage.getMessage()))
      .verifyComplete();
    Assertions.assertEquals(1, uiStationAndStationGroupsList.size());
    Assertions.assertTrue(uiStationAndStationGroupsList.get(0).getIsUpdateResponse());
  }

}
