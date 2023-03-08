package gms.dataacquisition.data.preloader.generator;

import gms.dataacquisition.data.preloader.GenerationSpec;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.channel.ChannelBandType;
import gms.shared.frameworks.osd.coi.channel.ChannelDataType;
import gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType;
import gms.shared.frameworks.osd.coi.channel.ChannelOrientationType;
import gms.shared.frameworks.osd.coi.channel.ChannelProcessingMetadataType;
import gms.shared.frameworks.osd.coi.channel.Orientation;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.signaldetection.Location;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Stream;


class StationSohDataGeneratorStateTest {

  @Test
  void instantiationAndPublicMethodsTest() {

    GenerationSpec generationSpecMock = Mockito.mock(GenerationSpec.class);
    Mockito.when(generationSpecMock.getStartTime())
      .thenReturn(Instant.parse("2021-01-10T12:00:00.00Z"));
    Mockito.when(generationSpecMock.getSampleDuration()).thenReturn(Duration.ofMillis(500L));
    Mockito.when(generationSpecMock.getDuration()).thenReturn(Duration.ofMillis(100L));

    Station stationMock = Mockito.mock(Station.class);
    NavigableSet<Channel> channels = new TreeSet<>(Comparator.comparing(Channel::getName));
    Channel channel = Channel.from(
      "name",
      "canon name",
      "description",
      "station",
      ChannelDataType.SEISMIC,
      ChannelBandType.ADMINISTRATIVE,
      ChannelInstrumentType.NON_SPECIFIC_INSTRUMENT,
      ChannelOrientationType.EAST_WEST,
      'E',
      Units.DEGREES,
      1.0,
      Location.from(1.0, 1.0, 1.0, 1.0),
      Orientation.from(45.0, 45.0),
      List.of("foo", "bar", "bif", "baz"),
      Map.of("foo", 1.0, "bar", 2.0, "bif", 3.0, "baz", 4.0),
      Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, 1.0)
    );
    channels.add(channel);
    Mockito.when(stationMock.getChannels()).thenReturn(channels);

    OsdRepositoryInterface osdRepositoryMock = Mockito.mock(OsdRepositoryInterface.class);

    StationSohDataGeneratorState stationSohDataGeneratorState = new StationSohDataGeneratorState(
      generationSpecMock,
      "seed name",
      stationMock,
      () -> Stream.of(AcquiredChannelEnvironmentIssueType.NUMBER_OF_CONSTANT_VALUES),
      osdRepositoryMock
    );

    Assertions.assertNotNull(stationSohDataGeneratorState.getStateSupplier());
    Assertions.assertNotNull(stationSohDataGeneratorState.getGenerator());
  }
}
