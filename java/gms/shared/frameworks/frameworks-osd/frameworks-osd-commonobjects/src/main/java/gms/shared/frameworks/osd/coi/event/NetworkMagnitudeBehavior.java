package gms.shared.frameworks.osd.coi.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;

/**
 * @deprecated As of PI 17.5, the current model of this COI has been migrated into the event-coi package.
 * All usage of this COI outside the Frameworks area should be avoided and the alternative in event-coi used instead
 */
@Deprecated(since = "17.5", forRemoval = true)
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

  /**
   * @deprecated As of PI 17.5, the current model of this builder has been migrated into the event-coi package.
   * All usage of this builder outside the Frameworks area should be avoided and the alternative in event-coi used instead
   */
  @Deprecated(since = "17.5", forRemoval = true)
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

      Preconditions.checkState(behavior.getResidual() >= -10 && behavior.getResidual() <= 10,
        "Error creating NetworkMagnitudeBehavior: residual must be >= -10 and <= 10, but was "
          + behavior.getResidual());

      Preconditions.checkState(behavior.getWeight() >= 0,
        "Error creating NetworkMagnitudeBehavior: weight must be >= 0, but was "
          + behavior.getWeight());

      return behavior;
    }
  }
}
