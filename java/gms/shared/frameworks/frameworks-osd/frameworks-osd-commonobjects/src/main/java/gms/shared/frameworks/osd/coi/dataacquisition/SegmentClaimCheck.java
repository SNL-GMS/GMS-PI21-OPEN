package gms.shared.frameworks.osd.coi.dataacquisition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.channel.ChannelSegment;

import java.time.Instant;
import java.util.UUID;

@AutoValue
public abstract class SegmentClaimCheck {

  public abstract UUID getSegmentId();

  public abstract Channel getChannel();

  public abstract String getSegmentName();

  public abstract Instant getStartTime();

  public abstract double getSampleRate();

  public abstract String getWaveformFile();

  public abstract int getSampleCount();

  public abstract int getfOff();

  public abstract String getDataType();

  public abstract ChannelSegment.Type getSegmentType();

  public abstract boolean isClipped();

  /**
   * Create a new a SegmentClaimCheck.
   *
   * @param segmentId the channel segment id
   * @param channel the channel id of the segment
   * @param segmentName the name of the segment
   * @param startTime start time of the segment
   * @param sampleRate sample rate of the segment
   * @param waveformFile .w file which contains the raw samples
   * @param sampleCount number of samples to read
   * @param fOff offset into the file
   * @param dataType type of the data (format of the waveform samples)
   * @param segmentType type of the segment
   * @return A SegmentClaimCheck object.
   */
  @JsonCreator
  public static SegmentClaimCheck from(
    @JsonProperty("segmentId") UUID segmentId,
    @JsonProperty("channel") Channel channel,
    @JsonProperty("segmentName") String segmentName,
    @JsonProperty("startTime") Instant startTime,
    @JsonProperty("sampleRate") double sampleRate,
    @JsonProperty("waveformFile") String waveformFile,
    @JsonProperty("sampleCount") int sampleCount,
    @JsonProperty("fOff") int fOff,
    @JsonProperty("dataType") String dataType,
    @JsonProperty("segmentType") ChannelSegment.Type segmentType,
    @JsonProperty("clipped") boolean clipped) {

    return new AutoValue_SegmentClaimCheck(segmentId, channel, segmentName, startTime,
      sampleRate, waveformFile, sampleCount, fOff, dataType, segmentType, clipped);
  }
}
