package gms.shared.waveform.accessor;

import gms.shared.frameworks.cache.utils.IgniteConnectionManager;
import gms.shared.frameworks.test.utils.containers.ZookeeperTest;
import gms.shared.stationdefinition.api.StationDefinitionAccessorInterface;
import gms.shared.waveform.api.WaveformRepositoryInterface;
import gms.shared.waveform.api.util.ChannelTimeRangeRequest;
import gms.shared.waveform.coi.ChannelSegment;
import gms.shared.waveform.coi.Waveform;
import gms.shared.waveform.testfixture.WaveformRequestTestFixtures;
import gms.shared.waveform.testfixture.WaveformTestFixtures;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
@Disabled
//zookeeper has been removed 8/5/2021
class WaveformAccessorTest extends ZookeeperTest {
  public static final String CHANNELS = "channels";

  @Mock
  private StationDefinitionAccessorInterface stationDefinitionAccessorImpl;

  @Mock
  private WaveformRepositoryInterface waveformRepositoryInterface;

  @InjectMocks
  private WaveformAccessor waveformAccessor;

  @BeforeAll
  protected static void fixtureSetUp() {
    setUpContainer();
  }

  @AfterEach
  protected void testTeardown() {
    IgniteConnectionManager.close();
  }

  @AfterAll
  protected static void fixtureTeardown() {
    IgniteConnectionManager.close();
  }

  @Test
  void findByChannelsAndTimeRange() {
    var request = WaveformRequestTestFixtures.channelTimeRangeRequest;

    Mockito.when(waveformRepositoryInterface.findByChannelsAndTimeRange(request.getChannels(), request.getStartTime(), request.getEndTime()))
      .thenReturn(List.of(WaveformTestFixtures.singleStationEpochStart100RandomSamples()));

    Collection<ChannelSegment<Waveform>> returnChannelSegments =
      waveformAccessor.findByChannelsAndTimeRange(request.getChannels(), request.getStartTime(), request.getEndTime());

    assertEquals(1, returnChannelSegments.size(), "Incorrect number of channelSegments returned");
    assertTrue(returnChannelSegments.contains(WaveformTestFixtures.singleStationEpochStart100RandomSamples()),
      "Return list did not contain expected ChannelSegment");
  }

  @ParameterizedTest
  @MethodSource("getFindByChannelsTimeRangeAndFacetingDefinitionArguments")
  void findByChannelsTimeRangeAndFacetingDefinition(ChannelTimeRangeRequest request) {

    Mockito.when(waveformRepositoryInterface.findByChannelsAndTimeRange(
        request.getChannels(), request.getStartTime(), request.getEndTime()))
      .thenReturn(List.of(WaveformTestFixtures.singleStationEpochStart100RandomSamples()));

    Collection<ChannelSegment<Waveform>> returnChannelSegments =
      waveformAccessor.findByChannelsAndTimeRange(
        request.getChannels(), request.getStartTime(), request.getEndTime(), request.getFacetingDefinition().get());

    assertEquals(1, returnChannelSegments.size(), "Incorrect number of channelSegments returned");
    assertTrue(returnChannelSegments.contains(WaveformTestFixtures.singleStationEpochStart100RandomSamples()),
      "Return list did not contain expected ChannelSegment");
  }

  static Stream<Arguments> getFindByChannelsTimeRangeAndFacetingDefinitionArguments() {
    return Stream.of(
      arguments(WaveformRequestTestFixtures.facetedChannelTimeRangeRequest),
      arguments(WaveformRequestTestFixtures.facetedChannelTimeRangeRequest2));
  }

  @Test
  void findByChannelNamesAndSegmentDescriptor() {
    var request = WaveformRequestTestFixtures.channelSegmentDescriptorRequest;

    Mockito.when(waveformRepositoryInterface.findByChannelSegmentDescriptors(
        request.getChannelSegmentDescriptors()))
      .thenReturn(List.of(WaveformTestFixtures.singleStationEpochStart100RandomSamples()));

    Collection<ChannelSegment<Waveform>> returnChannelSegments =
      waveformAccessor.findByChannelSegmentDescriptors(
        request.getChannelSegmentDescriptors());

    assertEquals(1, returnChannelSegments.size(), "Incorrect number of channelSegments returned");
    assertTrue(returnChannelSegments.contains(WaveformTestFixtures.singleStationEpochStart100RandomSamples()),
      "Return list did not contain expected ChannelSegment");
  }

  @Test
  void findByChannelNamesAndSegmentDescriptorAndFacetingDefinition() {
    var request = WaveformRequestTestFixtures.facetedChannelSegmentDescriptorRequest;

    Mockito.when(waveformRepositoryInterface.findByChannelSegmentDescriptors(
        request.getChannelSegmentDescriptors()))
      .thenReturn(List.of(WaveformTestFixtures.singleStationEpochStart100RandomSamples()));

    Collection<ChannelSegment<Waveform>> returnChannelSegments =
      waveformAccessor.findByChannelNamesAndSegmentDescriptor(
        request.getChannelSegmentDescriptors(),
        request.getFacetingDefinition().get());

    assertEquals(1, returnChannelSegments.size(), "Incorrect number of channelSegments returned");
    assertTrue(returnChannelSegments.contains(WaveformTestFixtures.singleStationEpochStart100RandomSamples()),
      "Return list did not contain expected ChannelSegment");
  }
}