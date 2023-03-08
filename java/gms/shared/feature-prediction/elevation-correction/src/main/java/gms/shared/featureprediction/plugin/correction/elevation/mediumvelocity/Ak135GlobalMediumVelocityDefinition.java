package gms.shared.featureprediction.plugin.correction.elevation.mediumvelocity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Ak135GlobalMediumVelocityDefinition {

  public abstract String getDataDescriptor();

  @JsonCreator
  public static Ak135GlobalMediumVelocityDefinition create(String dataDescriptor) {
    return new AutoValue_Ak135GlobalMediumVelocityDefinition(dataDescriptor);
  }
}
