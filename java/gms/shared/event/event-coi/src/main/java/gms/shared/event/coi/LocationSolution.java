package gms.shared.event.coi;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import gms.shared.event.coi.featureprediction.FeaturePredictionContainer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;

/**
 * Define a LocationSolution class for process results. Describes an estimate of where an
 * {@link EventHypothesis} may be located, as well as additional location dependent information
 */
@AutoValue
@JsonSerialize(as = LocationSolution.class)
@JsonDeserialize(builder = AutoValue_LocationSolution.Builder.class)
public abstract class LocationSolution {

  public abstract UUID getId();

  @JsonUnwrapped
  public abstract Optional<LocationSolution.Data> getData();

  public static LocationSolution createEntityReference(UUID id) {
    return builder()
      .setId(id)
      .build();
  }

  public LocationSolution toEntityReference() {
    return builder()
      .setId(getId())
      .build();
  }

  /**
   * AutoValue builder for the main LocationSolution class
   */
  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {
    LocationSolution.Builder setId(UUID id);

    @JsonUnwrapped
    LocationSolution.Builder setData(@Nullable LocationSolution.Data data);

    LocationSolution autobuild();

    default LocationSolution build() {
      return autobuild();
    }

  }

  public static LocationSolution.Builder builder() {
    return new AutoValue_LocationSolution.Builder();
  }

  public abstract Builder toBuilder();

  /*********************************
   * DATA OBJECT
   *********************************/

  @AutoValue
  @JsonSerialize(as = LocationSolution.Data.class)
  @JsonDeserialize(builder = AutoValue_LocationSolution_Data.Builder.class)
  public abstract static class Data {

    public abstract LocationSolution.Data.Builder toBuilder();

    public static LocationSolution.Data.Builder builder() {
      return new AutoValue_LocationSolution_Data.Builder();
    }

    public abstract EventLocation getLocation();

    public abstract LocationRestraint getLocationRestraint();

    public abstract Optional<LocationUncertainty> getLocationUncertainty();

    public abstract FeaturePredictionContainer getFeaturePredictions();

    public abstract ImmutableSet<LocationBehavior> getLocationBehaviors();

    public Stream<LocationBehavior> locationBehaviors() {
      return getLocationBehaviors().stream();
    }

    public abstract ImmutableList<NetworkMagnitudeSolution> getNetworkMagnitudeSolutions();

    public Stream<NetworkMagnitudeSolution> networkMagnitudeSolutions() {
      return getNetworkMagnitudeSolutions().stream();
    }

    @AutoValue.Builder
    @JsonPOJOBuilder(withPrefix = "set")
    public abstract static class Builder {

      public abstract LocationSolution.Data.Builder setLocation(EventLocation location);

      abstract Optional<EventLocation> getLocation();

      public abstract LocationSolution.Data.Builder setLocationRestraint(LocationRestraint locationRestraint);

      abstract Optional<LocationRestraint> getLocationRestraint();

      abstract Optional<LocationUncertainty> getLocationUncertainty();

      public abstract LocationSolution.Data.Builder setLocationUncertainty(LocationUncertainty locationUncertainty);

      public abstract LocationSolution.Data.Builder setFeaturePredictions(
        FeaturePredictionContainer featurePredictions);

      abstract FeaturePredictionContainer getFeaturePredictions();

      abstract ImmutableSet.Builder<LocationBehavior> locationBehaviorsBuilder();

      abstract LocationSolution.Data.Builder setLocationBehaviors(ImmutableSet<LocationBehavior> locationBehaviors);

      public LocationSolution.Data.Builder setLocationBehaviors(Collection<LocationBehavior> locationBehaviors) {
        return setLocationBehaviors(ImmutableSet.copyOf(locationBehaviors));
      }

      public LocationSolution.Data.Builder addLocationBehavior(LocationBehavior locationBehavior) {
        locationBehaviorsBuilder().add(locationBehavior);
        return this;
      }

      abstract ImmutableSet<LocationBehavior> getLocationBehaviors();

      abstract ImmutableList.Builder<NetworkMagnitudeSolution> networkMagnitudeSolutionsBuilder();

      abstract LocationSolution.Data.Builder setNetworkMagnitudeSolutions(
        ImmutableList<NetworkMagnitudeSolution> networkMagnitudeSolutions);

      public LocationSolution.Data.Builder setNetworkMagnitudeSolutions(
        Collection<NetworkMagnitudeSolution> networkMagnitudeSolutions) {
        return setNetworkMagnitudeSolutions(ImmutableList.copyOf(networkMagnitudeSolutions));
      }

      public LocationSolution.Data.Builder addNetworkMagnitudeSolution(
        NetworkMagnitudeSolution networkMagnitudeSolution) {
        networkMagnitudeSolutionsBuilder().add(networkMagnitudeSolution);
        return this;
      }

      abstract ImmutableList<NetworkMagnitudeSolution> getNetworkMagnitudeSolutions();

      protected abstract LocationSolution.Data autoBuild();


      /**
       * Returns a LocationSolution.Data object only if all the fields are properly set.
       * This intentionally returns null if the Location and LocationRestraint are not set.
       * This is to support deserialization of a faceted Location Solution using Jackson.
       *
       * @return LocationSolution.Data
       */
      @Nullable
      public LocationSolution.Data build() {

        //This check is for allowing deserialization of a faceted LocationSolution to be created
        //where the data is empty. This build must return null in this case
        List<Optional<?>> allFields = List.of(getLocation(), getLocationRestraint());
        var numPresentFields = allFields.stream()
          .filter(Optional::isPresent)
          .count();
        if (numPresentFields == 0) {
          return null;
        }
        var data = autoBuild();
        var errorMessage = "Each location behavior's feature prediction must exist in the location solution's set of feature predictions";
        data.locationBehaviors().forEach(
          behavior ->
            behavior.getFeaturePrediction().ifPresent(fp -> checkState(data.getFeaturePredictions().contains(fp), errorMessage))
        );
        return data;
      }
    }
  }
}
