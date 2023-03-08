package gms.shared.waveform.testfixture;


import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.waveform.coi.ChannelSegment;
import gms.shared.waveform.coi.Waveform;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL;

/**
 * Defines static objects used in unit tests
 */
public class WaveformTestFixtures {

  private static final SecureRandom random = new SecureRandom();
  public static final Instant SEGMENT_START = Instant.parse("1970-01-02T03:04:05.123Z");
  public static final Instant SEGMENT_END = SEGMENT_START.plusMillis(2000);

  public static final Channel CHANNEL_NO_DATA = Channel.builder()
    .setName("Real Channel Name One")
    .setEffectiveAt(Instant.EPOCH)
    .build();

  public static final ChannelSegment<Waveform> CHANNEL_SEGMENT = ChannelSegment
    .from(
      CHANNEL,
      CHANNEL.getUnits(),
      List.of(epochStart100RandomSamples(CHANNEL.getNominalSampleRateHz())),
      SEGMENT_START.minus(1, ChronoUnit.MINUTES));

  public static final ChannelSegment<Waveform> CHANNEL_SEGMENT_NO_CHANNEL_DATA = ChannelSegment
    .from(
      CHANNEL_NO_DATA,
      CHANNEL.getUnits(),
      List.of(epochStart100RandomSamples(CHANNEL.getNominalSampleRateHz())),
      SEGMENT_START.minus(1, ChronoUnit.MINUTES));

  // AcquiredChannelSohBoolean
  public static final String PROCESSING_CHANNEL_1_NAME = "PDAR.PD01.BHZ";
  // AcquiredChannelSohAnalog
  public static final String PROCESSING_CHANNEL_2_NAME = "PDAR.PD01.SHZ";

  // Waveform
  public static final double SAMPLE_RATE = 2.0;
  protected static final double[] WAVEFORM_POINTS = new double[]{1.1, 2.2, 3.3, 4.4, 5.5};
  public static final Waveform WAVEFORM_1 = Waveform.create(SEGMENT_START, SAMPLE_RATE,
    WAVEFORM_POINTS);

  // ChannelSegment
  public static final Collection<Waveform> waveforms = Collections.singleton(WAVEFORM_1);

  // RawStationDataFrame
  public static final UUID FRAME_ID = UUID.fromString("12347cc2-8c86-4fa1-a764-c9b9944614b7");
  public static final String STATION_NAME = "PDAR";

  public static Waveform randomSamples0To1(Instant start, Instant end, double sampleRate) {
    long size = (long) (Duration.between(start, end).toMillis() * sampleRate / 1e3) + 1;
    return randomSamples0To1(start, sampleRate, size);
  }

  public static Waveform randomSamples0To1(Instant start, double sampleRate, long size) {
    double[] values = random.doubles(-1.0, 1.0).limit(size).toArray();
    return Waveform.create(start, sampleRate, values);
  }

  public static Waveform epochStart100RandomSamples(double sampleRate) {
    return randomSamples0To1(Instant.EPOCH, sampleRate, 100);
  }

  public static ChannelSegment<Waveform> singleStationEpochStart100RandomSamples() {
    return CHANNEL_SEGMENT;
  }

  private WaveformTestFixtures() {
    // private constructor for static test fixtures class
  }
}
