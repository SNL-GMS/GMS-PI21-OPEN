package gms.shared.event.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.event.coi.LocationSolution;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.stationdefinition.coi.channel.Channel;

import java.util.List;

/**
 * Defines the request body for EventManager.EventLocationSolutionFeaturePredictionRequest()
 */
@AutoValue
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class PredictFeaturesForLocationSolutionRequest {

  public abstract LocationSolution getLocationSolution();

  public abstract List<Channel> getChannels();

  public abstract List<PhaseType> getPhases();


  @JsonCreator
  public static PredictFeaturesForLocationSolutionRequest from(
    @JsonProperty("locationSolution") LocationSolution locationSolution,
    @JsonProperty("channels") List<Channel> channels,
    @JsonProperty("phases") java.util.List<PhaseType> phases) {

    return new AutoValue_PredictFeaturesForLocationSolutionRequest(locationSolution, channels, phases);
  }

}
