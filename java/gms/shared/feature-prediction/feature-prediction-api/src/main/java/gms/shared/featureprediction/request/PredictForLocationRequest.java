package gms.shared.featureprediction.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import gms.shared.event.coi.EventLocation;
import gms.shared.event.coi.featureprediction.FeaturePredictionCorrectionDefinition;
import gms.shared.event.coi.featureprediction.type.FeaturePredictionType;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.stationdefinition.coi.channel.Location;

import java.util.List;

@AutoValue
@JsonSerialize(as = PredictForLocationRequest.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class PredictForLocationRequest {

  @JsonCreator
  public static PredictForLocationRequest from(
    @JsonProperty("predictionTypes") List<FeaturePredictionType<?>> predictionTypes,
    @JsonProperty("sourceLocation") EventLocation sourceLocation,
    @JsonProperty("receiverLocations") List<Location> receiverLocations,
    @JsonProperty("phases") List<PhaseType> phases,
    @JsonProperty("earthModel") String earthModel,
    @JsonProperty("correctionDefinitions") List<FeaturePredictionCorrectionDefinition> correctionDefinitions
  ) {
    return new AutoValue_PredictForLocationRequest(predictionTypes,
      sourceLocation,
      receiverLocations, phases, earthModel, correctionDefinitions);
  }

  public abstract List<FeaturePredictionType<?>> getPredictionTypes();

  public abstract EventLocation getSourceLocation();

  public abstract List<Location> getReceiverLocations();

  public abstract List<PhaseType> getPhases();

  public abstract String getEarthModel();

  public abstract List<FeaturePredictionCorrectionDefinition> getCorrectionDefinitions();
}
