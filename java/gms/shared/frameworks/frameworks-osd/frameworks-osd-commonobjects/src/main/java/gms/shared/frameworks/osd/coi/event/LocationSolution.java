package gms.shared.frameworks.osd.coi.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

/**
 * Define a LocationSolution class for process results.
 */
@AutoValue
@JsonSerialize(as = LocationSolution.class)
@JsonDeserialize(builder = AutoValue_LocationSolution.Builder.class)
public abstract class LocationSolution {

  public abstract UUID getId();

  public abstract EventLocation getLocation();

  public abstract LocationRestraint getLocationRestraint();

  public abstract Optional<LocationUncertainty> getLocationUncertainty();

  public abstract ImmutableSet<FeaturePrediction<?>> getFeaturePredictions();

  public abstract ImmutableSet<LocationBehavior> getLocationBehaviors();

  public abstract ImmutableList<NetworkMagnitudeSolution> getNetworkMagnitudeSolutions();

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_LocationSolution.Builder();
  }

  /**
   * Create a new LocationSolution.
   *
   * @param location A Location object, not null.
   * @param locationRestraint A LocationRestraint object, not null.
   * @return A LocationSolution object.
   */
  public static LocationSolution withLocationAndRestraintOnly(EventLocation location,
    LocationRestraint locationRestraint) {

    return builder()
      .generateId()
      .setLocation(location)
      .setLocationRestraint(locationRestraint)
      .setLocationUncertainty(Optional.empty())
      .setFeaturePredictions(emptySet())
      .setLocationBehaviors(emptySet())
      .setNetworkMagnitudeSolutions(emptyList())
      .build();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public Builder generateId() {
      return setId(UUID.randomUUID());
    }

    public abstract Builder setId(UUID id);

    public abstract Builder setLocation(EventLocation location);

    public abstract Builder setLocationRestraint(LocationRestraint locationRestraint);

    public abstract Builder setLocationUncertainty(LocationUncertainty locationUncertainty);

    @JsonProperty
    public abstract Builder setLocationUncertainty(
      Optional<LocationUncertainty> locationUncertainty);

    abstract Builder setLocationBehaviors(
      ImmutableSet<LocationBehavior> locationBehaviors);

    public Builder setLocationBehaviors(
      Collection<LocationBehavior> locationBehaviors) {
      return setLocationBehaviors(ImmutableSet.copyOf(locationBehaviors));
    }

    abstract ImmutableSet.Builder<LocationBehavior> locationBehaviorsBuilder();

    public Builder addLocationBehavior(LocationBehavior locationBehavior) {
      locationBehaviorsBuilder().add(locationBehavior);
      return this;
    }

    abstract Builder setFeaturePredictions(
      ImmutableSet<FeaturePrediction<?>> featurePredictions);

    public Builder setFeaturePredictions(
      Collection<FeaturePrediction<?>> featurePredictions) {
      return setFeaturePredictions(ImmutableSet.copyOf(featurePredictions));
    }

    abstract ImmutableSet.Builder<FeaturePrediction<?>> featurePredictionsBuilder();

    public Builder addFeaturePrediction(FeaturePrediction<?> featurePrediction) {
      featurePredictionsBuilder().add(featurePrediction);
      return this;
    }

    abstract Builder setNetworkMagnitudeSolutions(
      ImmutableList<NetworkMagnitudeSolution> networkMagnitudeSolutions);

    public Builder setNetworkMagnitudeSolutions(
      Collection<NetworkMagnitudeSolution> networkMagnitudeSolutions) {
      return setNetworkMagnitudeSolutions(ImmutableList.copyOf(networkMagnitudeSolutions));
    }

    abstract ImmutableList.Builder<NetworkMagnitudeSolution> networkMagnitudeSolutionsBuilder();

    public Builder addNetworkMagnitudeSolution(NetworkMagnitudeSolution networkMagnitudeSolution) {
      networkMagnitudeSolutionsBuilder().add(networkMagnitudeSolution);
      return this;
    }

    abstract LocationSolution autoBuild();

    public LocationSolution build() {
      LocationSolution locationSolution = autoBuild();

      Preconditions.checkNotNull(locationSolution.getLocation(), "Location cannot be null");
      Preconditions.checkNotNull(locationSolution.getLocationRestraint(),
        "Location restraint cannot be null");

      return locationSolution;
    }
  }

}
