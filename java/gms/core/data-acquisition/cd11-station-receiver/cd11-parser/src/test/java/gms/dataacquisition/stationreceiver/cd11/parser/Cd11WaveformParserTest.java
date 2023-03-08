package gms.dataacquisition.stationreceiver.cd11.parser;

import gms.core.dataacquisition.receiver.DataFrameReceiverConfiguration;
import gms.dataacquisition.stationreceiver.cd11.parser.util.GmsObjectUtility;
import gms.shared.frameworks.osd.api.channel.ChannelRepositoryInterface;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.channel.ChannelSegment;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import gms.shared.frameworks.osd.coi.waveforms.Waveform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static gms.dataacquisition.stationreceiver.cd11.parser.util.MockUtility.configureMockRepository;
import static gms.dataacquisition.stationreceiver.cd11.parser.util.MockUtility.mockChannel;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;

@ExtendWith(MockitoExtension.class)
class Cd11WaveformParserTest {

  private static final String RSDF_FILE = "gms/data/rsdfs/LBTB.json";
  private static final Class<RawStationDataFrame> RSDF_CLASS = RawStationDataFrame.class;
  private static RawStationDataFrame testFrame;

  @Mock
  private DataFrameReceiverConfiguration mockReceiverConfiguration;

  @Mock
  private ChannelRepositoryInterface mockChannelRepository;

  private Cd11WaveformParser parser;

  @BeforeEach
  void setUp() throws IOException {
    parser = Cd11WaveformParser
      .create(mockReceiverConfiguration, mockChannelRepository);

    // create the test rsdf object
    Optional<?> rsdfOpt = GmsObjectUtility.getGmsObject(RSDF_FILE, RSDF_CLASS);
    assertTrue(rsdfOpt.isPresent());
    testFrame = (RawStationDataFrame) rsdfOpt.get();
    assertNotNull(testFrame);
  }

  @Test
  void updateCacheAllChannelsAvailableDoesNotThrow() {
    Channel mockLbtb1Z = mockChannel("LBTB.LBTB1.SHZ");

    given(mockReceiverConfiguration.channelNames())
      .willReturn(List.of("LBTB.LBTB1.SHZ").stream());
    given(mockChannelRepository.retrieveChannels(any()))
      .willReturn(List.of(mockLbtb1Z));

    assertDoesNotThrow(() -> parser.updateChannelCache());
  }

  @Test
  void updateCachePartialChannelsAvailableThrowsIllegalStateException() {
    Channel mockLbtb1Z = mockChannel("LBTB.LBTB1.SHZ");
    Channel mockLbtb2Z = mockChannel("LBTB.LBTB2.SHZ");

    List<String> channelNames = Stream.of(mockLbtb1Z, mockLbtb2Z).map(Channel::getName)
      .collect(toList());
    given(mockReceiverConfiguration.channelNames())
      .willReturn(channelNames.stream());
    given(mockChannelRepository.retrieveChannels(channelNames)).willReturn(List.of(mockLbtb1Z));

    IllegalStateException actualException = assertThrows(IllegalStateException.class,
      parser::updateChannelCache);

    assertTrue(actualException.getMessage().contains("Not all channels retrieved"));
    assertTrue(actualException.getMessage().contains(mockLbtb2Z.getName()));
  }

  @Test
  void updateCacheNoChannelsThrowsIllegalStateException() {
    Channel mockLbtb1Z = mockChannel("LBTB.LBTB1.SHZ");

    given(mockReceiverConfiguration.channelNames())
      .willReturn(Stream.of(mockLbtb1Z).map(Channel::getName));
    given(mockChannelRepository.retrieveChannels(any())).willReturn(List.of());

    IllegalStateException actualException = assertThrows(IllegalStateException.class,
      parser::updateChannelCache);

    assertTrue(actualException.getMessage().contains("Not all channels retrieved"));
    assertTrue(actualException.getMessage().contains(mockLbtb1Z.getName()));
  }

  @Test
  void parseWaveformMissingChannelNameProducesNoWaveforms() {
    assertEquals(0, assertDoesNotThrow(() -> parser.parseWaveform(testFrame)).size());
  }

  @Test
  void parseWaveformMissingChannelInCacheThrowsIllegalStateException() throws IOException {

    given(mockReceiverConfiguration.getChannelName("LBTB.LBTB1.SHZ"))
      .willReturn(Optional.of("LBTB.LBTB1.SHZ"));

    IllegalStateException actual = assertThrows(IllegalStateException.class,
      () -> parser.parseWaveform(testFrame));
    assertTrue(actual.getMessage().contains("No channel matching name"));
  }

  @Test
  void parseWaveform() {

    Channel mockLbtb1Z = mockChannel("LBTB.LBTB1.SHZ");
    Channel mockLbtbbZ = mockChannel("LBTB.LBTBB.BHZ");
    Channel mockLbtbbN = mockChannel("LBTB.LBTBB.BHN");
    Channel mockLbtbbE = mockChannel("LBTB.LBTBB.BHE");

    configureMockConfiguration(mockReceiverConfiguration, mockLbtb1Z, mockLbtbbZ, mockLbtbbN,
      mockLbtbbE);
    configureMockRepository(mockChannelRepository, mockLbtb1Z, mockLbtbbZ, mockLbtbbN, mockLbtbbE);

    parser.updateChannelCache();

    List<ChannelSegment<Waveform>> actualWaveforms = assertDoesNotThrow(
      () -> parser.parseWaveform(testFrame));

    assertEquals(4, actualWaveforms.size());

    assertTrue(actualWaveforms.stream().map(ChannelSegment::getChannel).collect(toSet())
      .containsAll(Set.of(mockLbtb1Z, mockLbtbbZ, mockLbtbbN, mockLbtbbE)));
  }

  private static void configureMockConfiguration(DataFrameReceiverConfiguration configuration,
    Channel... mockChannels) {
    for (Channel mockChannel : mockChannels) {
      String channelName = mockChannel.getName();
      willReturn(Optional.of(channelName)).given(configuration)
        .getChannelName(channelName);
    }

    given(configuration.channelNames())
      .willReturn(Arrays.stream(mockChannels).map(Channel::getName));
  }

}