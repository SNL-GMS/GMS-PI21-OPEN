package gms.shared.event.coi;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

import static com.google.common.base.Preconditions.checkState;

/**
 * Defines the NetworkMagnitudeBehavior class - represents the relationship between a {@link NetworkMagnitudeSolution}
 * and each {@link StationMagnitudeSolution}
 */
@AutoValue
@JsonSerialize(as = NetworkMagnitudeBehavior.class)
@JsonDeserialize(builder = AutoValue_NetworkMagnitudeBehavior.Builder.class)
public abstract class NetworkMagnitudeBehavior {

  public abstract boolean isDefining();

  public abstract double getResidual();

  public abstract double getWeight();

  public abstract StationMagnitudeSolution getStationMagnitudeSolution();

  public static Builder builder() {
    return new AutoValue_NetworkMagnitudeBehavior.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setDefining(boolean defining);

    public abstract Builder setResidual(double residual);

    public abstract Builder setWeight(double weight);

    public abstract Builder setStationMagnitudeSolution(StationMagnitudeSolution stationMagnitudeSolution);

    protected abstract NetworkMagnitudeBehavior autoBuild();

    public NetworkMagnitudeBehavior build() {
      NetworkMagnitudeBehavior behavior = autoBuild();

      checkState(behavior.getResidual() >= -10 && behavior.getResidual() <= 10,
        "Error creating NetworkMagnitudeBehavior: residual must be >= -10 and <= 10, but was "
          + behavior.getResidual());

      checkState(behavior.getWeight() >= 0,
        "Error creating NetworkMagnitudeBehavior: weight must be >= 0, but was "
          + behavior.getWeight());

      return behavior;
    }
  }
}
