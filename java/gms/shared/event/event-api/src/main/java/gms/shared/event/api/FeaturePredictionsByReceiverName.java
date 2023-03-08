package gms.shared.event.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.event.coi.featureprediction.FeaturePredictionContainer;

import java.util.Map;

@AutoValue
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class FeaturePredictionsByReceiverName {
  public abstract Map<String, FeaturePredictionContainer> getReceiverLocationsByName();

  @JsonCreator
  public static FeaturePredictionsByReceiverName from(
    @JsonProperty("receiverLocationsByName") Map<String, FeaturePredictionContainer> receiverLocationsByNames) {

    return new AutoValue_FeaturePredictionsByReceiverName(receiverLocationsByNames);
  }
}
