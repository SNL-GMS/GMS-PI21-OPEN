package gms.shared.featureprediction.utilities.view;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.primitives.ImmutableDoubleArray;
import gms.shared.featureprediction.utilities.data.EarthModelType;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.stationdefinition.coi.utils.Units;

import java.time.Duration;
import java.util.Locale;


/**
 * A class representing a travel time lookup table for a specific earth model and wave phase.
 */
@AutoValue
public abstract class TravelTimeLookupView {

  /**
   * Creates a TravelTimeLookupView object.
   *
   * @return new TravelTimeLookupView object
   */
  @JsonCreator
  public static TravelTimeLookupView from(
    @JsonProperty("model") String model,
    @JsonProperty("phase") String phase,
    @JsonProperty("depthUnits") String depthUnits,
    @JsonProperty("distanceUnits") String distanceUnits,
    @JsonProperty("travelTimeUnits") String travelTimeUnits,
    @JsonProperty("depths") double[] depths,
    @JsonProperty("distances") double[] distances,
    @JsonProperty("travelTimes") Duration[][] travelTimes,
    @JsonProperty("modelingErrorDepths") double[] modelingErrorDepths,
    @JsonProperty("modelingErrorDistances") double[] modelingErrorDistances,
    @JsonProperty("modelingErrors") Duration[][] modelingErrors
  ) {
    PhaseType phaseType;

    try {
      phaseType = PhaseType.valueOf(phase);
    } catch (IllegalArgumentException e) {
      phaseType = PhaseType.UNKNOWN;
    }

    return TravelTimeLookupView.builder()
      .setModel(EarthModelType.valueOf(model.toUpperCase(Locale.US)))
      .setPhase(phaseType)
      .setRawPhaseString(phase)
      .setDepthUnits(Units.valueOf(depthUnits.toUpperCase(Locale.US)))
      .setDistanceUnits(Units.valueOf(distanceUnits.toUpperCase(Locale.US)))
      .setTravelTimeUnits(Units.valueOf(travelTimeUnits.toUpperCase(Locale.US)))
      .setDepths(depths)
      .setDistances(distances)
      .setTravelTimes(travelTimes)
      .setModelingErrorDepths(modelingErrorDepths)
      .setModelingErrorDistances(modelingErrorDistances)
      .setModelingErrors(modelingErrors)
      .build();
  }

  public abstract EarthModelType getModel();

  public abstract PhaseType getPhase();

  public abstract String getRawPhaseString();

  public abstract Units getDepthUnits();

  public abstract Units getDistanceUnits();

  public abstract Units getTravelTimeUnits();

  public abstract ImmutableDoubleArray getDepths();

  public abstract ImmutableDoubleArray getDistances();

  public abstract Immutable2dArray<Duration> getTravelTimes();

  public abstract ImmutableDoubleArray getModelingErrorDepths();

  public abstract ImmutableDoubleArray getModelingErrorDistances();

  public abstract Immutable2dArray<Duration> getModelingErrors();

  public static Builder builder() {
    return new AutoValue_TravelTimeLookupView.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setModel(EarthModelType model);

    public abstract Builder setPhase(PhaseType phase);

    public abstract Builder setRawPhaseString(String phaseString);

    public abstract Builder setDepthUnits(Units depthUnits);

    public abstract Builder setDistanceUnits(Units distanceUnits);

    public abstract Builder setTravelTimeUnits(Units travelTimeUnits);

    public abstract Builder setDepths(ImmutableDoubleArray depths);

    public Builder setDepths(double[] values) {
      return setDepths(ImmutableDoubleArray.copyOf(values));
    }

    public abstract Builder setDistances(ImmutableDoubleArray distances);

    public Builder setDistances(double[] values) {
      return setDistances(ImmutableDoubleArray.copyOf(values));
    }

    public abstract Builder setTravelTimes(Immutable2dArray<Duration> travelTimes);

    public Builder setTravelTimes(Duration[][] values) {
      return setTravelTimes(Immutable2dArray.from(Duration.class, values));
    }

    public abstract Builder setModelingErrorDepths(ImmutableDoubleArray modelingErrorDepths);

    public Builder setModelingErrorDepths(double[] values) {
      return setModelingErrorDepths(ImmutableDoubleArray.copyOf(values));
    }

    public abstract Builder setModelingErrorDistances(ImmutableDoubleArray modelingErrorDistances);

    public Builder setModelingErrorDistances(double[] values) {
      return setModelingErrorDistances(ImmutableDoubleArray.copyOf(values));
    }

    public abstract Builder setModelingErrors(Immutable2dArray<Duration> modelingErrors);

    public Builder setModelingErrors(Duration[][] values) {
      return setModelingErrors(Immutable2dArray.from(Duration.class, values));
    }

    public abstract TravelTimeLookupView autoBuild();

    public TravelTimeLookupView build() {
      return autoBuild();
    }
  }
}
