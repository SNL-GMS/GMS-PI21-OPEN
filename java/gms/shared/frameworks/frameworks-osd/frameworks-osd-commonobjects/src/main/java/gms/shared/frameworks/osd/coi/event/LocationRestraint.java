package gms.shared.frameworks.osd.coi.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.apache.commons.lang3.Validate;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Define a LocationRestraint class for the processing results location solution.  Use the Builder
 * class to create a new LocationRestraint object.
 *
 * @deprecated As of PI 17.5, the current model of this COI has been migrated into the event-coi package.
 * All usage of this COI outside the Frameworks area should be avoided and the alternative in event-coi used instead
 */
@Deprecated(since = "17.5", forRemoval = true)
@AutoValue
public abstract class LocationRestraint {

  /**
   * Constant instance indicating no restraints.
   */
  public static final LocationRestraint NO_RESTRAINTS = new Builder().build();


  public abstract RestraintType getLatitudeRestraintType();

  public abstract Optional<Double> getLatitudeRestraintDegrees();

  public abstract RestraintType getLongitudeRestraintType();

  public abstract Optional<Double> getLongitudeRestraintDegrees();

  public abstract DepthRestraintType getDepthRestraintType();

  public abstract Optional<Double> getDepthRestraintKm();

  public abstract RestraintType getTimeRestraintType();

  public abstract Optional<Instant> getTimeRestraint();

  /**
   * Create a LocationRestraint from known attributes.
   *
   * @return A LocationRestraint object.
   */
  @JsonCreator
  public static LocationRestraint from(
    @JsonProperty("latitudeRestraintType") RestraintType latitudeRestraintType,
    @JsonProperty("latitudeRestraintDegrees") Double latitudeRestraintDegrees,
    @JsonProperty("longitudeRestraintType") RestraintType longitudeRestraintType,
    @JsonProperty("longitudeRestraintDegrees") Double longitudeRestraintDegrees,
    @JsonProperty("depthRestraintType") DepthRestraintType depthRestraintType,
    @JsonProperty("depthRestraintKm") Double depthRestraintKm,
    @JsonProperty("timeRestraintType") RestraintType timeRestraintType,
    @JsonProperty("timeRestraint") Instant timeRestraint) {

    Objects.requireNonNull(latitudeRestraintType, "Null latitudeRestraintType");
    Objects.requireNonNull(longitudeRestraintType, "Null longitudeRestraintType");
    Objects.requireNonNull(depthRestraintType, "Null depthRestraintType");
    Objects.requireNonNull(timeRestraintType, "Null timeRestraintType");

    // If a latitude restraint is applied, validate corresponding restraint value
    if (!latitudeRestraintType.equals(RestraintType.UNRESTRAINED)) {

      // Validate that restraint value is not null
      Objects.requireNonNull(latitudeRestraintDegrees, String
        .format(
          "latitudeRestraintDegrees cannot be null when latitudeRestraintType is not \"%s\"",
          RestraintType.UNRESTRAINED));

      // Validate that restraint value is within required range
      Validate.isTrue(latitudeRestraintDegrees >= -90 && latitudeRestraintDegrees <= 90,
        "Expected latitude restraint to be in [-90,90] but was " + latitudeRestraintDegrees);
    } else {

      Validate
        .isTrue(Objects.isNull(latitudeRestraintDegrees),
          String.format(
            "latitudeRestraintType is set to RestraintType.UNRESTRAINED, expected latitudeRestraintDegrees to be null, but got: %f",
            latitudeRestraintDegrees));
    }

    // If a longitude restraint is applied, validate corresponding restraint value
    if (!longitudeRestraintType.equals(RestraintType.UNRESTRAINED)) {

      // Validate that restraint value is not null
      Objects.requireNonNull(longitudeRestraintDegrees, String
        .format(
          "longitudeRestraintDegrees cannot be null when longitudeRestraintType is not \"%s\"",
          RestraintType.UNRESTRAINED));

      // Validate that restraint value is within required range
      Validate.isTrue(longitudeRestraintDegrees >= -180 && longitudeRestraintDegrees <= 180,
        "Expected longitude restraint to be in [-180,180] but was " + longitudeRestraintDegrees);
    } else {

      Validate
        .isTrue(Objects.isNull(longitudeRestraintDegrees),
          String.format(
            "longitudeRestraintType is set to RestraintType.UNRESTRAINED, expected longitudeRestraintDegrees to be null, but got: %f",
            longitudeRestraintDegrees));
    }

    // If a depth restraint is applied, validate corresponding restraint value
    if (depthRestraintType.equals(DepthRestraintType.FIXED_AT_DEPTH)) {

      // Validate that restraint value is not null
      Objects.requireNonNull(depthRestraintKm, String
        .format(
          "depthRestraintKm cannot be null when depthRestraintType is \"%s\"",
          DepthRestraintType.FIXED_AT_DEPTH));
    } else if (depthRestraintType.equals(DepthRestraintType.FIXED_AT_SURFACE)) {

      // Validate that restraint value is null or zero
      Validate
        .isTrue(Objects.isNull(depthRestraintKm) || depthRestraintKm.equals(0.0), String
          .format("depthRestraintKm must be null or 0 when depthRestraintType is \"%s\"",
            DepthRestraintType.FIXED_AT_SURFACE));
    } else if (depthRestraintType.equals(DepthRestraintType.UNRESTRAINED)) {

      Validate
        .isTrue(Objects.isNull(depthRestraintKm),
          String.format(
            "depthRestraintType is set to DepthRestraintType.UNRESTRAINED, expected depthRestraintKm to be null, but got: %f",
            depthRestraintKm));
    }

    // If a time restraint is applied, validate corresponding restraint value
    if (!timeRestraintType.equals(RestraintType.UNRESTRAINED)) {

      // Validate that restraint value is not null
      Objects.requireNonNull(timeRestraint, String
        .format(
          "timeRestraint cannot be null when timeRestraintType is not \"%s\"",
          RestraintType.UNRESTRAINED));
    } else {

      Validate
        .isTrue(Objects.isNull(timeRestraint),
          String.format(
            "timeRestraintType is set to RestraintType.UNRESTRAINED, expected timeRestraint to be null, but got: %s",
            timeRestraint));
    }

    return new AutoValue_LocationRestraint(
      latitudeRestraintType, Optional.ofNullable(latitudeRestraintDegrees),
      longitudeRestraintType, Optional.ofNullable(longitudeRestraintDegrees),
      depthRestraintType, Optional.ofNullable(depthRestraintKm),
      timeRestraintType, Optional.ofNullable(timeRestraint));
  }

  /**
   * Define a builder for the LocationRestraint class.  Attributes not specified will default to the
   * following values:
   * <ul>
   * <li>depthRestraintType = FIXED_AT_SURFACE </li>
   * <li>depthRestraintKm = 0.0</li>
   * <li>positionRestraintType = UNRESTRAINED</li>
   * <li>latitudeRestraintDegrees = 0.0</li>
   * <li>longitudeRestraintDegrees = 0.0</li>
   * <li>timeRestraintType = UNRESTRAINED</li>
   * <li>timeRestraint = Instant.now()</li>
   * </ul>
   * <p>
   * Example:
   * <code>
   * LocationRestraint.Builder builder = new Builder() LocationRestraint lr =
   * builder.setLatitudeRestraintDegrees(34.5).setLongitudeRestraintDegrees(56.0).build();
   * </code>
   * </p>
   */
  public static class Builder {

    private RestraintType latitudeRestraintType;
    private Double latitudeRestraintDegrees;
    private RestraintType longitudeRestraintType;
    private Double longitudeRestraintDegrees;
    private DepthRestraintType depthRestraintType;
    private Double depthRestraintKm;
    private RestraintType timeRestraintType;
    private Instant timeRestraint;

    /**
     * Constructor.
     */
    public Builder() {
      setDefaults();
    }

    private void setDefaults() {
      this.latitudeRestraintType = RestraintType.UNRESTRAINED;
      this.longitudeRestraintType = RestraintType.UNRESTRAINED;
      this.depthRestraintType = DepthRestraintType.UNRESTRAINED;
      this.timeRestraintType = RestraintType.UNRESTRAINED;
    }

    /**
     * Build a new LocationRestraint object.
     *
     * @return A LocationRestraint object.
     */
    public LocationRestraint build() {
      return LocationRestraint.from(
        this.latitudeRestraintType,
        this.latitudeRestraintDegrees,
        this.longitudeRestraintType,
        this.longitudeRestraintDegrees,
        this.depthRestraintType,
        this.depthRestraintKm,
        this.timeRestraintType,
        this.timeRestraint);
    }

    public Builder setLatitudeRestraint(double latitudeRestraintDegrees) {
      this.latitudeRestraintType = RestraintType.FIXED;
      this.latitudeRestraintDegrees = latitudeRestraintDegrees;
      return this;
    }

    public Builder setLongitudeRestraint(double longitudeRestraintDegrees) {
      this.longitudeRestraintType = RestraintType.FIXED;
      this.longitudeRestraintDegrees = longitudeRestraintDegrees;
      return this;
    }

    public Builder setPositionRestraint(double latitudeRestraintDegrees,
      double longitudeRestraintDegrees) {
      this.latitudeRestraintType = RestraintType.FIXED;
      this.latitudeRestraintDegrees = latitudeRestraintDegrees;
      this.longitudeRestraintType = RestraintType.FIXED;
      this.longitudeRestraintDegrees = longitudeRestraintDegrees;
      return this;
    }

    public Builder setDepthRestraint(double depthRestraintKm) {
      this.depthRestraintType = DepthRestraintType.FIXED_AT_DEPTH;
      this.depthRestraintKm = depthRestraintKm;
      return this;
    }

    public Builder setDepthRestraintAtSurface() {
      this.depthRestraintType = DepthRestraintType.FIXED_AT_SURFACE;
      this.depthRestraintKm = 0.0;
      return this;
    }

    public Builder setTimeRestraint(Instant timeRestraint) {
      this.timeRestraintType = RestraintType.FIXED;
      this.timeRestraint = timeRestraint;
      return this;
    }
  }

}
