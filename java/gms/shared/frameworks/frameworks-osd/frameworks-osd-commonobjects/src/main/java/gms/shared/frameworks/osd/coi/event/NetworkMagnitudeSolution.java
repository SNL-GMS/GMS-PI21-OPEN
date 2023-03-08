package gms.shared.frameworks.osd.coi.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.Collection;

/**
 * @deprecated As of PI 17.5, the current model of this COI has been migrated into the event-coi package.
 * All usage of this COI outside the Frameworks area should be avoided and the alternative in event-coi used instead
 */
@Deprecated(since = "17.5", forRemoval = true)
@AutoValue
@JsonSerialize(as = NetworkMagnitudeSolution.class)
@JsonDeserialize(builder = AutoValue_NetworkMagnitudeSolution.Builder.class)
public abstract class NetworkMagnitudeSolution {

  public abstract MagnitudeType getMagnitudeType();

  public abstract double getMagnitude();

  public abstract double getUncertainty();

  public abstract Builder toBuilder();

  public abstract ImmutableList<NetworkMagnitudeBehavior> getNetworkMagnitudeBehaviors();

  public static Builder builder() {
    return new AutoValue_NetworkMagnitudeSolution.Builder();
  }

  /**
   * @deprecated As of PI 17.5, the current model of this builder has been migrated into the event-coi package.
   * All usage of this builder outside the Frameworks area should be avoided and the alternative in event-coi used instead
   */
  @Deprecated(since = "17.5", forRemoval = true)
  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setMagnitudeType(MagnitudeType magnitudeType);

    public abstract Builder setMagnitude(double magnitude);

    public abstract Builder setUncertainty(double uncertainty);

    abstract Builder setNetworkMagnitudeBehaviors(
      ImmutableList<NetworkMagnitudeBehavior> networkMagnitudeBehaviors);

    public Builder setNetworkMagnitudeBehaviors(
      Collection<NetworkMagnitudeBehavior> networkMagnitudeBehaviors) {
      return setNetworkMagnitudeBehaviors(ImmutableList.copyOf(networkMagnitudeBehaviors));
    }

    abstract ImmutableList.Builder<NetworkMagnitudeBehavior> networkMagnitudeBehaviorsBuilder();

    public Builder addNetworkMagnitudeBehavior(NetworkMagnitudeBehavior networkMagnitudeBehavior) {
      networkMagnitudeBehaviorsBuilder().add(networkMagnitudeBehavior);
      return this;
    }

    protected abstract NetworkMagnitudeSolution autoBuild();

    public NetworkMagnitudeSolution build() {
      NetworkMagnitudeSolution solution = autoBuild();

      Preconditions.checkState(solution.getMagnitude() <= 10,
        "Error creating NetworkMagnitudeSolution: magnitude must be <= 10, but was " + solution
          .getMagnitude());

      Preconditions.checkState(
        0 <= solution.getUncertainty() &&
          solution.getUncertainty() <= 10,
        "Error creating NetworkMagnitudeSolution: uncertainty must be >= 0 and <= 10, but was "
          + solution.getUncertainty());

      return solution;
    }
  }

}
