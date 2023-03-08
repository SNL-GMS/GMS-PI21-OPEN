package gms.shared.frameworks.osd.coi.waveforms;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.time.Instant;

import static com.google.common.base.Preconditions.checkArgument;

@AutoValue
public abstract class WaveformSummary implements Comparable<WaveformSummary> {

  public abstract String getChannelName();

  public abstract Instant getStartTime();

  public abstract Instant getEndTime();

  @JsonCreator
  public static WaveformSummary from(
    @JsonProperty("channelName") String channelName,
    @JsonProperty("startTime") Instant startTime,
    @JsonProperty("endTime") Instant endTime) {
    checkArgument(!endTime.isBefore(startTime), "End time cannot be before Start time");
    return new AutoValue_WaveformSummary(channelName, startTime, endTime);
  }

  //Just looks at startTime
  @Override
  public int compareTo(WaveformSummary otherSummary) {
    if (this.getStartTime().isAfter(otherSummary.getStartTime())) {
      return 1;
    } else if (this.getEndTime().isBefore(otherSummary.getStartTime())) {
      return -1;
    } else {
      return 0;
    }
  }
}

