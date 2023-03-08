package gms.shared.frameworks.osd.coi.channel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Orientation {

  public abstract double getHorizontalAngleDeg();

  public abstract double getVerticalAngleDeg();

  @JsonCreator
  public static Orientation from(
    @JsonProperty("horizontalAngleDeg") double horizontalAngleDeg,
    @JsonProperty("verticalAngleDeg") double verticalAngleDeg) {
    return new AutoValue_Orientation(horizontalAngleDeg, verticalAngleDeg);
  }
}
