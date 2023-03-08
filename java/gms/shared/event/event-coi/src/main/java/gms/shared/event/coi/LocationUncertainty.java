package gms.shared.event.coi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;

/**
 * Defines the LocationUncertainty class
 */
@AutoValue
@JsonSerialize(as = LocationUncertainty.class)
@JsonDeserialize(builder = AutoValue_LocationUncertainty.Builder.class)
public abstract class LocationUncertainty {

  public abstract double getXx();

  public abstract double getXy();

  public abstract double getXz();

  public abstract double getXt();

  public abstract double getYy();

  public abstract double getYz();

  public abstract double getYt();

  public abstract double getZz();

  public abstract double getZt();

  public abstract double getTt();

  public abstract double getStDevOneObservation();

  public abstract ImmutableSet<Ellipse> getEllipses();

  public abstract ImmutableSet<Ellipsoid> getEllipsoids();

  public static Builder builder() {
    return new AutoValue_LocationUncertainty.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {
    public abstract Builder setXx(double xx);

    public abstract Builder setXy(double xy);

    public abstract Builder setXz(double xz);

    public abstract Builder setXt(double xt);

    public abstract Builder setYy(double yy);

    public abstract Builder setYz(double yz);

    public abstract Builder setYt(double yt);

    public abstract Builder setZz(double zz);

    public abstract Builder setZt(double zt);

    public abstract Builder setTt(double tt);

    public abstract Builder setStDevOneObservation(double stDevOneObservation);

    public Builder setEllipses(Collection<Ellipse> ellipses) {
      setEllipses(ImmutableSet.copyOf(ellipses));
      return this;
    }

    public abstract Builder setEllipses(ImmutableSet<Ellipse> ellipses);

    abstract ImmutableSet.Builder<Ellipse> ellipsesBuilder();

    public LocationUncertainty.Builder addEllipse(Ellipse ellipse) {
      ellipsesBuilder().add(ellipse);
      return this;
    }

    public Builder setEllipsoids(Collection<Ellipsoid> ellipsoids) {
      setEllipsoids(ImmutableSet.copyOf(ellipsoids));
      return this;
    }

    public abstract Builder setEllipsoids(ImmutableSet<Ellipsoid> ellipsoids);

    abstract ImmutableSet.Builder<Ellipsoid> ellipsoidsBuilder();

    public LocationUncertainty.Builder addEllipsoid(Ellipsoid ellipsoid) {
      ellipsoidsBuilder().add(ellipsoid);
      return this;
    }

    protected abstract LocationUncertainty autoBuild();

    public LocationUncertainty build() {
      var locationUncertainty = autoBuild();

      checkState(!Double.isNaN(locationUncertainty.getXx()),
        "The validated xx is not a number");
      checkState(!Double.isNaN(locationUncertainty.getXy()),
        "The validated xy is not a number");
      checkState(!Double.isNaN(locationUncertainty.getXz()),
        "The validated xz is not a number");
      checkState(!Double.isNaN(locationUncertainty.getXt()),
        "The validated xt is not a number");
      checkState(!Double.isNaN(locationUncertainty.getYy()),
        "The validated yy is not a number");
      checkState(!Double.isNaN(locationUncertainty.getYz()),
        "The validated yz is not a number");
      checkState(!Double.isNaN(locationUncertainty.getYt()),
        "The validated yt is not a number");
      checkState(!Double.isNaN(locationUncertainty.getZz()),
        "The validated zz is not a number");
      checkState(!Double.isNaN(locationUncertainty.getZt()),
        "The validated zt is not a number");
      checkState(!Double.isNaN(locationUncertainty.getTt()),
        "The validated tt is not a number");
      checkState(!Double.isNaN(locationUncertainty.getStDevOneObservation()),
        "The validated stDevOneObservation is not a number");

      var uniqueEllipsesConstraintSet = locationUncertainty.getEllipses().stream()
        .map(ellipseConstraint ->
          Pair.of(ellipseConstraint.getConfidenceLevel(), ellipseConstraint.getScalingFactorType()))
        .collect(Collectors.toSet());
      checkState(locationUncertainty.getEllipses().size() ==
          uniqueEllipsesConstraintSet.size(),
        "Ellipses have a duplicate(s) with the same Confidence Level and Scaling Factor Type.");

      var uniqueEllipsoidsConstraintSet = locationUncertainty.getEllipsoids().stream()
        .map(ellipsoidConstraint ->
          Pair.of(ellipsoidConstraint.getConfidenceLevel(), ellipsoidConstraint.getScalingFactorType()))
        .collect(Collectors.toSet());
      checkState(locationUncertainty.getEllipsoids().size() ==
          uniqueEllipsoidsConstraintSet.size(),
        "Ellipsoids have a duplicate(s) with the same Confidence Level and Scaling Factor Type.");

      return locationUncertainty;
    }
  }

  /**
   * Build the covariance matrix.
   *
   * @return the covariance matrix with shape: xx xy xz xt xy yy yz yt xz yz zz zt xt yt zt tt
   */
  @JsonIgnore
  public List<List<Double>> getCovarianceMatrix() {
    return List.of(
      List.of(getXx(), getXy(), getXz(), getXt()),
      List.of(getXy(), getYy(), getYz(), getYt()),
      List.of(getXz(), getYz(), getZz(), getZt()),
      List.of(getXt(), getYt(), getZt(), getTt()));
  }

}
