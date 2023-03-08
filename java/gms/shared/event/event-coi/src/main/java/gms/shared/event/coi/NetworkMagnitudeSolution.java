package gms.shared.event.coi;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import gms.shared.stationdefinition.coi.utils.DoubleValue;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkState;

/**
 * Defines the NetworkMagnitudeSolution - measures the size of an {@link Event} occuring at a {@link LocationSolution}
 */
@AutoValue
@JsonSerialize(as = NetworkMagnitudeSolution.class)
@JsonDeserialize(builder = AutoValue_NetworkMagnitudeSolution.Builder.class)
public abstract class NetworkMagnitudeSolution {

  public abstract MagnitudeType getType();

  public abstract DoubleValue getMagnitude();

  public abstract Builder toBuilder();

  public abstract ImmutableList<NetworkMagnitudeBehavior> getNetworkMagnitudeBehaviors();

  public static Builder builder() {
    return new AutoValue_NetworkMagnitudeSolution.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setType(MagnitudeType type);

    public abstract Builder setMagnitude(DoubleValue magnitude);

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

      checkState(solution.getMagnitude().getValue() <= 10,
        "Error creating NetworkMagnitudeSolution: magnitude must be <= 10, but was " + solution
          .getMagnitude());

      return solution;
    }
  }

}
