package gms.shared.event.coi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

import java.time.Instant;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;

/**
 * Define a LocationRestraint class for the processing results location solution.  Use the Builder
 * class to create a new LocationRestraint object. Represent whether and how the location algorithm was constrained
 * when it was determined the {@link EventLocation}
 */
@AutoValue
@JsonSerialize(as = LocationRestraint.class)
@JsonDeserialize(builder = AutoValue_LocationRestraint.Builder.class)
public abstract class LocationRestraint {

  private static final LocationRestraint FREE = builder()
    .setDepthRestraintType(RestraintType.UNRESTRAINED)
    .setPositionRestraintType(RestraintType.UNRESTRAINED)
    .setTimeRestraintType(RestraintType.UNRESTRAINED)
    .build();

  private static final LocationRestraint SURFACE = builder()
    .setDepthRestraintType(RestraintType.FIXED)
    .setDepthRestraintReason(DepthRestraintReason.FIXED_AT_SURFACE)
    .setDepthRestraintKm(0.0)
    .setPositionRestraintType(RestraintType.UNRESTRAINED)
    .setTimeRestraintType(RestraintType.UNRESTRAINED)
    .build();

  public abstract RestraintType getDepthRestraintType();

  public abstract Optional<DepthRestraintReason> getDepthRestraintReason();

  public abstract Optional<Double> getDepthRestraintKm();

  public abstract RestraintType getPositionRestraintType();

  public abstract Optional<Double> getLatitudeRestraintDegrees();

  public abstract Optional<Double> getLongitudeRestraintDegrees();

  public abstract RestraintType getTimeRestraintType();

  public abstract Optional<Instant> getTimeRestraint();

  public static LocationRestraint free() {
    return FREE;
  }

  public static LocationRestraint surface() {
    return SURFACE;
  }

  public static Builder builder() {
    return new AutoValue_LocationRestraint.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {
    //TODO: Remove optional setter methods
    @JsonProperty
    public abstract Builder setDepthRestraintType(RestraintType depthRestraintType);

    public abstract Builder setDepthRestraintReason(DepthRestraintReason depthRestraintReason);

    @JsonProperty
    public abstract Builder setDepthRestraintReason(Optional<DepthRestraintReason> depthRestraintReason);

    public abstract Builder setDepthRestraintKm(double depthRestraintKm);

    @JsonProperty
    public abstract Builder setDepthRestraintKm(Optional<Double> depthRestraintKm);

    @JsonProperty
    public abstract Builder setPositionRestraintType(RestraintType positionRestraintType);

    public abstract Builder setLatitudeRestraintDegrees(double latitudeRestraintDegrees);

    @JsonProperty
    public abstract Builder setLatitudeRestraintDegrees(Optional<Double> latitudeRestraintDegrees);

    public abstract Builder setLongitudeRestraintDegrees(double longitudeRestraintDegrees);

    @JsonProperty
    public abstract Builder setLongitudeRestraintDegrees(Optional<Double> longitudeRestraintDegrees);

    @JsonProperty
    public abstract Builder setTimeRestraintType(RestraintType timeRestraintType);

    public abstract Builder setTimeRestraint(Instant timeRestraint);

    @JsonProperty
    public abstract Builder setTimeRestraint(Optional<Instant> timeRestraint);

    protected abstract LocationRestraint autoBuild();

    public LocationRestraint build() {
      var locationRestraint = autoBuild();


      validateDepthRestraintConstraints(locationRestraint.getDepthRestraintType(),
        locationRestraint.getDepthRestraintReason(),
        locationRestraint.getDepthRestraintKm());


      validatePositionRestraintConstraints(locationRestraint.getPositionRestraintType(),
        locationRestraint.getLatitudeRestraintDegrees(),
        locationRestraint.getLongitudeRestraintDegrees());


      validateTimeRestraintConstraints(locationRestraint.getTimeRestraintType(), locationRestraint.getTimeRestraint());

      return locationRestraint;
    }

    private void validateDepthRestraintConstraints(RestraintType depthRestraintType,
      Optional<DepthRestraintReason> depthRestraintReason, Optional<Double> depthRestraintKm) {

      // Depth Restraint Reason
      checkState(depthRestraintType.equals(RestraintType.UNRESTRAINED)
          || depthRestraintReason.isPresent(),
        "Depth restraint reason should be populated when depth is restrained");

      checkState(!depthRestraintType.equals(RestraintType.UNRESTRAINED)
          || depthRestraintReason.isEmpty(),
        "Depth restraint reason should not be populated when depth is unrestrained");

      // Depth Restraint (km)
      checkState(depthRestraintType.equals(RestraintType.UNRESTRAINED)
          || depthRestraintKm.isPresent(),
        "Depth restraint (km) should be populated when depth is restrained");

      checkState(!depthRestraintType.equals(RestraintType.UNRESTRAINED)
          || depthRestraintKm.isEmpty(),
        "Depth restraint (km) should not be populated when depth is unrestrained");
    }

    private void validatePositionRestraintConstraints(RestraintType positionRestraintType,
      Optional<Double> latitudeRestraintDegrees, Optional<Double> longitudeRestraintDegrees) {

      // Latitude Restraint (°)
      checkState(positionRestraintType.equals(RestraintType.UNRESTRAINED)
          || latitudeRestraintDegrees.isPresent(),
        "Latitude restraint (\u00B0) should be populated when position is restrained");

      checkState(!positionRestraintType.equals(RestraintType.UNRESTRAINED)
          || latitudeRestraintDegrees.isEmpty(),
        "Latitude restraint (\u00B0) should not be populated when position is unrestrained");

      // Longitude Restraint (°)
      checkState(positionRestraintType.equals(RestraintType.UNRESTRAINED)
          || longitudeRestraintDegrees.isPresent(),
        "Longitude restraint (\u00B0) should be populated when position is restrained");

      checkState(!positionRestraintType.equals(RestraintType.UNRESTRAINED)
          || longitudeRestraintDegrees.isEmpty(),
        "Longitude restraint (\u00B0) should not be populated when position is unrestrained");
    }

    private void validateTimeRestraintConstraints(RestraintType timeRestraintType, Optional<Instant> timeRestraint) {

      checkState(timeRestraintType.equals(RestraintType.UNRESTRAINED)
          || timeRestraint.isPresent(),
        "Time restraint should be populated when time is restrained");

      checkState(!timeRestraintType.equals(RestraintType.UNRESTRAINED)
          || timeRestraint.isEmpty(),
        "Time restraint should not be populated when time is unrestrained");
    }
  }
}
