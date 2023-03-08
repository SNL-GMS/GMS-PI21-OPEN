package gms.shared.frameworks.osd.coi.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import gms.shared.frameworks.osd.coi.DoubleValue;
import gms.shared.frameworks.osd.coi.PhaseType;
import gms.shared.frameworks.osd.coi.signaldetection.FeatureMeasurementType;
import gms.shared.frameworks.osd.coi.signaldetection.FeatureMeasurementTypesChecking;
import gms.shared.frameworks.osd.coi.signaldetection.Location;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @deprecated being moved to gms/shared/feature-prediction project
 */
@Deprecated(forRemoval = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.EXISTING_PROPERTY, property =
  "predictionType", visible = true)
@JsonTypeIdResolver(FeaturePredictionIdResolver.class)
@AutoValue
public abstract class FeaturePrediction<T> {

  public abstract PhaseType getPhase();

  public abstract Optional<T> getPredictedValue();

  public abstract ImmutableSet<FeaturePredictionComponent> getFeaturePredictionComponents();

  public abstract boolean isExtrapolated();

  @JsonIgnore
  public abstract FeatureMeasurementType<T> getPredictionType();

  public abstract EventLocation getSourceLocation();

  public abstract Location getReceiverLocation();

  public abstract Optional<String> getChannelName();

  public abstract ImmutableMap<FeaturePredictionDerivativeType, DoubleValue> getFeaturePredictionDerivativeMap();

  public static <T> Builder<T> builder() {
    return new AutoValue_FeaturePrediction.Builder<>();
  }

  @JsonCreator
  public static <T> FeaturePrediction<T> from(
    @JsonProperty("phase") PhaseType phase,
    @JsonProperty("predictedValue") Optional<T> predictedValue,
    @JsonProperty("featurePredictionComponents") Set<FeaturePredictionComponent> featurePredictionComponents,
    @JsonProperty("extrapolated") boolean extrapolated,
    @JsonProperty("predictionType") String predictionType,
    @JsonProperty("sourceLocation") EventLocation sourceLocation,
    @JsonProperty("receiverLocation") Location receiverLocation,
    @JsonProperty("channelName") Optional<String> channelName,
    @JsonProperty("featurePredictionDerivativeMap") Map<FeaturePredictionDerivativeType,
      DoubleValue> featurePredictionDerivativeMap) {

    FeatureMeasurementType<T> featureMeasurementType = FeatureMeasurementTypesChecking
      .featureMeasurementTypeFromMeasurementTypeString(predictionType);

    return FeaturePrediction.<T>builder()
      .setPhase(phase)
      .setPredictedValue(predictedValue)
      .setFeaturePredictionComponents(featurePredictionComponents)
      .setExtrapolated(extrapolated)
      .setPredictionType(featureMeasurementType)
      .setSourceLocation(sourceLocation)
      .setReceiverLocation(receiverLocation)
      .setChannelName(channelName)
      .setFeaturePredictionDerivativeMap(featurePredictionDerivativeMap)
      .build();
  }

  /**
   * The name of the feature measurement type. Only needed for serialization.
   */
  @JsonProperty("predictionType")
  public String getPredictionTypeName() {
    return getPredictionType().getFeatureMeasurementTypeName();
  }

  @AutoValue.Builder
  public abstract static class Builder<T> {

    public abstract Builder<T> setPhase(PhaseType phase);

    public abstract Builder<T> setPredictedValue(T predictedValue);

    public abstract Builder<T> setPredictedValue(Optional<T> predictedValue);

    abstract Builder<T> setFeaturePredictionComponents(
      ImmutableSet<FeaturePredictionComponent> featurePredictionComponents);

    public Builder<T> setFeaturePredictionComponents(
      Collection<FeaturePredictionComponent> featurePredictionComponents) {
      return setFeaturePredictionComponents(ImmutableSet.copyOf(featurePredictionComponents));
    }

    abstract ImmutableSet.Builder<FeaturePredictionComponent> featurePredictionComponentsBuilder();

    public Builder<T> addFeaturePredictionComponent(FeaturePredictionComponent component) {
      featurePredictionComponentsBuilder().add(component);
      return this;
    }

    public abstract Builder<T> setExtrapolated(boolean extrapolated);

    public abstract Builder<T> setPredictionType(FeatureMeasurementType<T> predictionType);

    public abstract Builder<T> setSourceLocation(EventLocation sourceLocation);

    public abstract Builder<T> setReceiverLocation(Location location);

    public abstract Builder<T> setChannelName(String channelName);

    public abstract Builder<T> setChannelName(Optional<String> channelName);

    abstract Builder<T> setFeaturePredictionDerivativeMap(
      ImmutableMap<FeaturePredictionDerivativeType, DoubleValue> featurePredictionDerivativeMap);

    public Builder<T> setFeaturePredictionDerivativeMap(
      Map<FeaturePredictionDerivativeType, DoubleValue> featurePredictionDerivateMap) {
      return setFeaturePredictionDerivativeMap(ImmutableMap.copyOf(featurePredictionDerivateMap));
    }

    abstract ImmutableMap.Builder<FeaturePredictionDerivativeType, DoubleValue> featurePredictionDerivativeMapBuilder();

    public Builder addFeaturePredictionDerivative(FeaturePredictionDerivativeType derivativeType,
      DoubleValue derivative) {
      featurePredictionDerivativeMapBuilder().put(derivativeType, derivative);
      return this;
    }

    public abstract FeaturePrediction<T> build();
  }
}
