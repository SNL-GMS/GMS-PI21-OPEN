package gms.shared.frameworks.osd.coi.waveforms;

import gms.shared.frameworks.osd.coi.channel.ChannelSegment;
import gms.shared.frameworks.osd.coi.channel.ChannelSegment.Type;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueAnalog;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame.AuthenticationStatus;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static gms.shared.frameworks.osd.coi.channel.ChannelTestFixtures.asarAs01Bhz;

/**
 * Defines static objects used in unit tests
 */
public class WaveformTestFixtures {

  private static final SecureRandom random = new SecureRandom();

  private static final ChannelSegment<Waveform> asarAs01BhzEpochStart100RandomSamples = ChannelSegment
    .create(
      asarAs01Bhz(),
      asarAs01Bhz().getName(),
      Type.RAW,
      List.of(epochStart100RandomSamples(asarAs01Bhz().getNominalSampleRateHz())));

  public static final Instant SEGMENT_START = Instant.parse("1970-01-02T03:04:05.123Z");
  public static final Instant SEGMENT_END = SEGMENT_START.plusMillis(2000);

  // AcquiredChannelSohBoolean
  public static final String PROCESSING_CHANNEL_1_NAME = "PDAR.PD01.BHZ";
  public static final AcquiredChannelEnvironmentIssueBoolean channelSohBoolean = AcquiredChannelEnvironmentIssueBoolean
    .from(
      PROCESSING_CHANNEL_1_NAME,
      AcquiredChannelEnvironmentIssueType.DEAD_SENSOR_CHANNEL, SEGMENT_START, SEGMENT_END,
      true);

  // AcquiredChannelSohAnalog
  public static final String PROCESSING_CHANNEL_2_NAME = "PDAR.PD01.SHZ";
  public static final AcquiredChannelEnvironmentIssueAnalog channelSohAnalog = AcquiredChannelEnvironmentIssueAnalog
    .from(
      PROCESSING_CHANNEL_2_NAME,
      AcquiredChannelEnvironmentIssueType.STATION_POWER_VOLTAGE, SEGMENT_START, SEGMENT_END,
      1.5);

  // Waveform
  public static final double SAMPLE_RATE = 2.0;
  protected static final double[] WAVEFORM_POINTS = new double[]{1.1, 2.2, 3.3, 4.4, 5.5};
  public static final Waveform waveform1 = Waveform.from(SEGMENT_START, SAMPLE_RATE,
    WAVEFORM_POINTS);

  // ChannelSegment
  public static final Collection<Waveform> waveforms = Collections.singleton(waveform1);

  // RawStationDataFrame
  public static final UUID FRAME_ID = UUID.fromString("12347cc2-8c86-4fa1-a764-c9b9944614b7");
  public static final String STATION_NAME = "PDAR";
  protected static final Set<String> CHANNEL_NAMES = Set
    .of(PROCESSING_CHANNEL_1_NAME);
  protected static final Map<String, WaveformSummary> waveformSummaries =
    Map.of(PROCESSING_CHANNEL_1_NAME, WaveformSummary
      .from(PROCESSING_CHANNEL_1_NAME, Instant.now(), Instant.now().plusSeconds(20)));

  public static final RawStationDataFrame RAW_STATION_DATA_FRAME = RawStationDataFrame
    .builder()
    .setId(FRAME_ID)
    .setMetadata(RawStationDataFrameMetadata.builder()
      .setPayloadFormat(RawStationDataFramePayloadFormat.CD11)
      .setStationName(STATION_NAME)
      .setChannelNames(CHANNEL_NAMES)
      .setReceptionTime(SEGMENT_END.plusSeconds(10))
      .setAuthenticationStatus(AuthenticationStatus.AUTHENTICATION_SUCCEEDED)
      .setWaveformSummaries(waveformSummaries)
      .setPayloadStartTime(SEGMENT_START)
      .setPayloadEndTime(SEGMENT_END)
      .build())
    .setRawPayload(new byte[50])
    .build();

  public static Waveform randomSamples0To1(Instant start, Instant end, double sampleRate) {
    long size = (long) (Duration.between(start, end).toMillis() * sampleRate / 1e3) + 1;
    return randomSamples0To1(start, sampleRate, size);
  }

  public static Waveform randomSamples0To1(Instant start, double sampleRate, long size) {
    double[] values = random.doubles(-1.0, 1.0).limit(size).toArray();
    return Waveform.from(start, sampleRate, values);
  }

  public static ChannelSegment<Waveform> asarAs01BhzEpochStart100RandomSamples() {
    return asarAs01BhzEpochStart100RandomSamples;
  }

  public static Waveform epochStart100RandomSamples(double sampleRate) {
    return randomSamples0To1(Instant.EPOCH, sampleRate, 100);
  }

  public static ChannelSegment<Waveform> singleStationEpochStart100RandomSamples() {
    return asarAs01BhzEpochStart100RandomSamples;
  }

  private WaveformTestFixtures() {
    // private constructor for static test fixtures class
  }
}
