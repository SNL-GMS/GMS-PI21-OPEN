package gms.core.performancemonitoring.uimaterializedview;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.quieting.QuietedSohStatusChange;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@AutoValue
public abstract class QuietedSohStatusChangeUpdate {
  public abstract Instant getQuietUntil();

  public abstract Duration getQuietDuration();

  public abstract SohMonitorType getSohMonitorType();

  public abstract String getChannelName();

  public abstract Optional<String> getComment();

  public abstract String getStationName();

  public abstract String getQuietedBy();

  public static QuietedSohStatusChangeUpdate create(QuietedSohStatusChange coiQuietedChange) {
    return QuietedSohStatusChangeUpdate.create(
      coiQuietedChange.getQuietUntil(),
      coiQuietedChange.getQuietDuration(),
      coiQuietedChange.getSohMonitorType(),
      coiQuietedChange.getChannelName(),
      coiQuietedChange.getComment(),
      coiQuietedChange.getStationName(),
      "" // quietedBy
    );
  }

  @JsonCreator
  public static QuietedSohStatusChangeUpdate create(
    @JsonProperty("quietUntil") Instant quietUntil,
    @JsonProperty("quietDuration") Duration quietDuration,
    @JsonProperty("sohMonitorType") SohMonitorType sohMonitorType,
    @JsonProperty("channelName") String channelName,
    @JsonProperty("comment") Optional<String> comment,
    @JsonProperty("stationName") String stationName,
    @JsonProperty("quietedBy") String quietedBy
  ) {
    return new AutoValue_QuietedSohStatusChangeUpdate(
      quietUntil,
      quietDuration,
      sohMonitorType,
      channelName,
      comment,
      stationName,
      quietedBy
    );
  }
}
