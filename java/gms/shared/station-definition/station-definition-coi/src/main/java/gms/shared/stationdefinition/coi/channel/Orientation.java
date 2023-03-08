package gms.shared.stationdefinition.coi.channel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.auto.value.AutoValue;

@AutoValue
@JsonPropertyOrder(alphabetic = true)
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
