package gms.shared.waveform.testfixture;

import gms.shared.stationdefinition.api.channel.util.ChannelsTimeFacetRequest;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.facets.FacetingDefinition;
import gms.shared.waveform.api.util.ChannelSegmentDescriptorRequest;
import gms.shared.waveform.api.util.ChannelTimeRangeRequest;
import gms.shared.waveform.coi.ChannelSegmentDescriptor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static gms.shared.stationdefinition.facet.FacetingTypes.CHANNEL_TYPE;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.WFDISC_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.WFDISC_TEST_DAO_4;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.createTestChannelData;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.getResponse;

public class WaveformRequestTestFixtures {
  private static final String STA_01_STA_01_BHE = "STA01.STA01.BHE";
  private static final String STA_02_STA_02_SHZ = "STA02.STA02.SHZ";
  private static final String CANONICAL_NAME_ONE = "Canonical Name One";

  private WaveformRequestTestFixtures() {
  }

  public static final Channel WAVEFORM_CHANNEL = Channel.builder()
    .setName(STA_01_STA_01_BHE)
    .setEffectiveAt(Instant.now())
    .setData(createTestChannelData(CANONICAL_NAME_ONE, getResponse(STA_01_STA_01_BHE)))
    .build();

  public static final Channel WAVEFORM_CHANNEL_LATER_ON_DATE = Channel.builder()
    .setName(STA_01_STA_01_BHE)
    .setEffectiveAt(Instant.now().plus(10, ChronoUnit.MINUTES))
    .setData(createTestChannelData(CANONICAL_NAME_ONE, getResponse(STA_01_STA_01_BHE)))
    .build();

  public static final Channel WAVEFORM_CHANNEL_LATER_ON_DATE_2 = Channel.builder()
    .setName(STA_01_STA_01_BHE)
    .setEffectiveAt(Instant.now().plus(20, ChronoUnit.MINUTES))
    .setData(createTestChannelData(CANONICAL_NAME_ONE, getResponse(STA_01_STA_01_BHE)))
    .build();

  public static final Channel WAVEFORM_CHANNEL_EARLY_ON_DATE = Channel.builder()
    .setName(STA_01_STA_01_BHE)
    .setEffectiveAt(Instant.EPOCH)
    .setData(createTestChannelData(CANONICAL_NAME_ONE, getResponse(STA_01_STA_01_BHE)))
    .build();

  public static final Channel WAVEFORM_CHANNEL_2 = Channel.builder()
    .setName(STA_02_STA_02_SHZ)
    .setEffectiveAt(Instant.EPOCH)
    .setData(createTestChannelData("Canonical Name Two", getResponse(STA_02_STA_02_SHZ)))
    .build();

  public static final Channel WAVEFORM_CHANNEL_2_LATER_ON_DATE = Channel.builder()
    .setName(STA_02_STA_02_SHZ)
    .setEffectiveAt(Instant.EPOCH.plus(10, ChronoUnit.MINUTES))
    .setData(createTestChannelData("Canonical Name Two", getResponse(STA_02_STA_02_SHZ)))
    .build();

  public static final ChannelTimeRangeRequest channelTimeRangeRequest = ChannelTimeRangeRequest.builder()
    .setChannels(Set.of(WAVEFORM_CHANNEL))
    .setStartTime(Instant.EPOCH)
    .setEndTime(Instant.EPOCH.plus(10, ChronoUnit.MINUTES))
    .build();

  public static final ChannelTimeRangeRequest channelTimeRangeRequest2Channels = ChannelTimeRangeRequest.builder()
    .setChannels(Set.of(WAVEFORM_CHANNEL, WAVEFORM_CHANNEL_2))
    .setStartTime(Instant.now())
    .setEndTime(Instant.now())
    .build();

  public static final FacetingDefinition channelSegmentFacetingDefinition = FacetingDefinition
    .builder()
    .setClassType("ChannelSegment")
    .setPopulated(true)
    .setFacetingDefinitions(
      Map.of("id.Channel", FacetingDefinition.builder()
        .setClassType("Channel")
        .setPopulated(true)
        .build())
    )
    .build();

  public static final FacetingDefinition channelSegmentFacetingDefinition2 = FacetingDefinition
    .builder()
    .setClassType("ChannelSegment")
    .setPopulated(true)
    .build();

  public static final ChannelTimeRangeRequest facetedChannelTimeRangeRequest = ChannelTimeRangeRequest.builder()
    .setChannels(Set.of(WAVEFORM_CHANNEL))
    .setStartTime(Instant.now())
    .setEndTime(Instant.now())
    .setFacetingDefinition(Optional.of(channelSegmentFacetingDefinition))
    .build();

  public static final ChannelTimeRangeRequest facetedChannelTimeRangeRequest2 = ChannelTimeRangeRequest.builder()
    .setChannels(Set.of(WAVEFORM_CHANNEL))
    .setStartTime(Instant.now())
    .setEndTime(Instant.now())
    .setFacetingDefinition(Optional.of(channelSegmentFacetingDefinition))
    .build();

  public static final gms.shared.waveform.coi.ChannelSegmentDescriptor channelSegmentDescriptor =
    ChannelSegmentDescriptor.from(
      WAVEFORM_CHANNEL,
      Instant.now(),
      Instant.now().plus(20, ChronoUnit.MINUTES),
      Instant.now()
    );
  public static final gms.shared.waveform.coi.ChannelSegmentDescriptor channelSegmentDescriptor2 =
    ChannelSegmentDescriptor.from(
      WAVEFORM_CHANNEL_2,
      Instant.EPOCH,
      Instant.EPOCH.plus(10, ChronoUnit.MINUTES),
      Instant.EPOCH.plus(10, ChronoUnit.MINUTES)
    );
  public static final gms.shared.waveform.coi.ChannelSegmentDescriptor channelSegmentDescriptorLater =

    ChannelSegmentDescriptor.from(
      WAVEFORM_CHANNEL_2,
      Instant.now(),
      Instant.now().plus(10, ChronoUnit.MINUTES),
      Instant.now()
    );

  public static final ChannelSegmentDescriptorRequest channelSegmentDescriptorRequest =
    ChannelSegmentDescriptorRequest.builder()
      .setChannelSegmentDescriptors(Arrays.asList(channelSegmentDescriptor))
      .build();

  public static final ChannelSegmentDescriptorRequest channelSegmentDescriptorRequest2 =
    ChannelSegmentDescriptorRequest.builder()
      .setChannelSegmentDescriptors(Arrays.asList(channelSegmentDescriptor, channelSegmentDescriptor2))
      .build();

  public static final ChannelSegmentDescriptorRequest facetedChannelSegmentDescriptorRequest =
    ChannelSegmentDescriptorRequest.builder()
      .setChannelSegmentDescriptors(Arrays.asList(channelSegmentDescriptor))
      .setFacetingDefinition(Optional.of(channelSegmentFacetingDefinition))
      .build();

  public static final ChannelsTimeFacetRequest channelsTimeFacetRequest = ChannelsTimeFacetRequest.builder()
    .setChannelNames(List.of(WAVEFORM_CHANNEL.getName()))
    .setEffectiveTime(WFDISC_DAO_1.getTime())
    .setFacetingDefinition(
      FacetingDefinition.builder()
        .setPopulated(true)
        .setClassType(CHANNEL_TYPE.getValue())
        .build())
    .build();

  public static final ChannelsTimeFacetRequest channelsTimeFacetRequest2 = ChannelsTimeFacetRequest.builder()
    .setChannelNames(List.of(WAVEFORM_CHANNEL.getName(), WAVEFORM_CHANNEL_2.getName()))
    .setEffectiveTime(WFDISC_DAO_1.getTime())
    .setFacetingDefinition(
      FacetingDefinition.builder()
        .setPopulated(true)
        .setClassType(CHANNEL_TYPE.getValue())
        .build())
    .build();

  public static final ChannelsTimeFacetRequest channelsTimeFacetRequest3 = ChannelsTimeFacetRequest.builder().
    setChannelNames(List.of(WAVEFORM_CHANNEL.getName()))
    .setEffectiveTime(WFDISC_TEST_DAO_4.getTime())
    .setFacetingDefinition(
      FacetingDefinition.builder()
        .setPopulated(true)
        .setClassType(CHANNEL_TYPE.getValue())
        .build())
    .build();
}
