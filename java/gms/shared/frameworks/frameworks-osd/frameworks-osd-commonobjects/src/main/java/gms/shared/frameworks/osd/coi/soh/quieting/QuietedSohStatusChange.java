package gms.shared.frameworks.osd.coi.soh.quieting;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@AutoValue
public abstract class QuietedSohStatusChange {

  public abstract Instant getQuietUntil();

  public abstract Duration getQuietDuration();

  public abstract SohMonitorType getSohMonitorType();

  public abstract String getChannelName();

  public abstract Optional<String> getComment();

  public abstract String getStationName();

  public static Builder builder() {
    return new AutoValue_QuietedSohStatusChange.Builder();
  }

  @JsonCreator
  public static QuietedSohStatusChange create(
    @JsonProperty("quietUntil") Instant quietUntil,
    @JsonProperty("quietDuration") Duration quietDuration,
    @JsonProperty("sohMonitorType") SohMonitorType sohMonitorType,
    @JsonProperty("channelName") String channelName,
    @JsonProperty("comment") Optional<String> comment,
    @JsonProperty("stationName") String stationName
  ) {


    return builder()
      .setQuietUntil(quietUntil)
      .setQuietDuration(quietDuration)
      .setSohMonitorType(sohMonitorType)
      .setChannelName(channelName)
      .setComment(comment)
      .setStationName(stationName)
      .build();
  }


  public abstract Builder toBuilder();


  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setQuietUntil(Instant quietUntil);

    public abstract Builder setQuietDuration(Duration quietDuration);

    public abstract Builder setSohMonitorType(SohMonitorType sohMonitorType);

    public abstract Builder setChannelName(String channelName);

    public abstract Builder setComment(Optional<String> comment);

    public abstract Builder setStationName(String stationName);

    public abstract QuietedSohStatusChange build();

  }
}
