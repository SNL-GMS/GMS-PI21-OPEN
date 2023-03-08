package gms.shared.featureprediction.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import gms.shared.event.coi.LocationSolution;
import gms.shared.event.coi.featureprediction.FeaturePredictionCorrectionDefinition;
import gms.shared.event.coi.featureprediction.type.FeaturePredictionType;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.stationdefinition.coi.channel.Channel;

import java.util.List;

@AutoValue
@JsonSerialize(as = PredictForLocationSolutionAndChannelRequest.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class PredictForLocationSolutionAndChannelRequest {


  @JsonCreator
  public static PredictForLocationSolutionAndChannelRequest from(
    @JsonProperty("predictionTypes") List<FeaturePredictionType<?>> predictionTypes,
    @JsonProperty("sourceLocationSolution") LocationSolution sourceLocationSolution,
    @JsonProperty("receivingChannels") List<Channel> receivingChannels,
    @JsonProperty("phases") List<PhaseType> phases,
    @JsonProperty("earthModel") String earthModel,
    @JsonProperty("correctionDefinitions") List<FeaturePredictionCorrectionDefinition> correctionDefinitions
  ) {

    return new AutoValue_PredictForLocationSolutionAndChannelRequest(predictionTypes,
      sourceLocationSolution,
      receivingChannels, phases, earthModel, correctionDefinitions);
  }

  public abstract List<FeaturePredictionType<?>> getPredictionTypes();

  public abstract LocationSolution getSourceLocationSolution();

  public abstract List<Channel> getReceivingChannels();

  public abstract List<PhaseType> getPhases();

  public abstract String getEarthModel();

  public abstract List<FeaturePredictionCorrectionDefinition> getCorrectionDefinitions();

}
