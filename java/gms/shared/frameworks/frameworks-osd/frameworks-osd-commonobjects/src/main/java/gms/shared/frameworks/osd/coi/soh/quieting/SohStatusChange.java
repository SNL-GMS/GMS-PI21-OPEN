package gms.shared.frameworks.osd.coi.soh.quieting;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;

import java.time.Instant;
import java.util.Objects;

@AutoValue
public abstract class SohStatusChange {

  @JsonCreator
  public static SohStatusChange from(
    @JsonProperty("firstChangeTime") Instant firstChangeTime,
    @JsonProperty("sohMonitorType") SohMonitorType sohMonitorType,
    @JsonProperty("changedChannel") String changedChannel) {

    Objects.requireNonNull(firstChangeTime,
      "Cannot create SohStatusChange from null firstChangeTime");
    Objects.requireNonNull(sohMonitorType,
      "Cannot create SohStatusChange from null sohMonitorType");
    Objects.requireNonNull(changedChannel,
      "Cannot create SohStatusChange from null changedChannel");

    return new AutoValue_SohStatusChange(firstChangeTime,
      sohMonitorType,
      changedChannel);
  }

  public abstract Instant getFirstChangeTime();

  public abstract SohMonitorType getSohMonitorType();

  public abstract String getChangedChannel();
}
