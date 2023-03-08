package gms.shared.event.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import gms.shared.event.coi.LocationSolution;
import gms.shared.event.coi.featureprediction.FeaturePredictionContainer;
import gms.shared.event.manager.config.EventManagerConfiguration;
import gms.shared.featureprediction.request.PredictForLocationRequest;
import gms.shared.featureprediction.request.PredictForLocationSolutionAndChannelRequest;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.utilities.javautilities.objectmapper.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.stream.Collectors;

/**
 * A utility for constructing and sending a request to the {@link gms.shared.event.coi.featureprediction.FeaturePrediction} service
 */
@Component
class WebRequests {
  private final WebClient.Builder webClientBuilder;
  private static final Logger logger = LoggerFactory.getLogger(WebRequests.class);

  private final URI predictForLocationUri;
  private final URI predictForLocationSolutionAndChannelUri;

  @Autowired
  WebRequests(EventManagerConfiguration eventManagerConfiguration, WebClient.Builder webClientBuilder) {
    this.predictForLocationUri = eventManagerConfiguration.predictForLocationUrl();
    this.predictForLocationSolutionAndChannelUri = eventManagerConfiguration.predictForLocationSolutionAndChannelUrl();
    this.webClientBuilder = webClientBuilder;
  }

  /**
   * Communicates with Feature Prediction Service
   *
   * @param predictForLocationSolutionAndChannelRequest {@link PredictForLocationSolutionAndChannelRequest} request body to sent to Feature Prediction Service
   * @return {@link LocationSolution} processed by Feature Prediction Service
   * @throws FeaturePredictionException When not able to communicate or parse data from Feature Prediction Service
   */
  LocationSolution fpsWebRequestPredictForLocationSolutionAndChannel(
    PredictForLocationSolutionAndChannelRequest predictForLocationSolutionAndChannelRequest) throws FeaturePredictionException {

    logger.info("Querying FeaturePredictorService endpoint {} with request... " +
        "FeaturePredictionTypes: {}" +
        ", Channels: {}" +
        ", Phases: {}" +
        ", EarthModel: {}" +
        ", EventLocation: {}",
      predictForLocationSolutionAndChannelUri,
      predictForLocationSolutionAndChannelRequest.getPredictionTypes(),
      predictForLocationSolutionAndChannelRequest.getReceivingChannels().stream().map(Channel::getName).collect(Collectors.toSet()),
      predictForLocationSolutionAndChannelRequest.getPhases(),
      predictForLocationSolutionAndChannelRequest.getEarthModel(),
      predictForLocationSolutionAndChannelRequest.getSourceLocationSolution().getData().orElseThrow().getLocation());

    var locationSolutionJson = this.webClientBuilder.build().post()
      .uri(predictForLocationSolutionAndChannelUri)
      .bodyValue(predictForLocationSolutionAndChannelRequest).retrieve()
      .onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new FeaturePredictionException("FeaturePredictionService failed with Client Error")))
      .onStatus(HttpStatus::is5xxServerError, response -> Mono.error(new FeaturePredictionException("FeaturePredictionService failed with Server Error")))
      .bodyToMono(String.class).blockOptional().orElseThrow(() -> new FeaturePredictionException("Unable to process empty response from FeaturePredictionService"));

    try {

      return ObjectMapperFactory.getJsonObjectMapper().readValue(locationSolutionJson, LocationSolution.class);
    } catch (JsonProcessingException e) {
      var locationSolutionString = locationSolutionJson.length() > 100 ? locationSolutionJson.substring(0, 100) + "..." : locationSolutionJson;
      var errorMessage = "Unable to convert to LocationSolution [{" + locationSolutionString + "}]";
      throw new FeaturePredictionException(errorMessage);
    }
  }

  FeaturePredictionContainer fpsWebRequestPredictForLocation(
    PredictForLocationRequest predictForLocationRequest) throws FeaturePredictionException {

    logger.info("Querying FeaturePredictorService endpoint {} with request... " +
        "FeaturePredictionTypes: {}" +
        ", ReceiverLocations: {}" +
        ", SourceLocation: {}" +
        ", Phases: {}" +
        ", EarthModel: {}",
      predictForLocationUri,
      predictForLocationRequest.getPredictionTypes(),
      predictForLocationRequest.getReceiverLocations(),
      predictForLocationRequest.getSourceLocation(),
      predictForLocationRequest.getPhases(),
      predictForLocationRequest.getEarthModel());

    var locationJson = this.webClientBuilder.build().post()
      .uri(predictForLocationUri)
      .bodyValue(predictForLocationRequest).retrieve()
      .onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new FeaturePredictionException("FeaturePredictionService failed with Client Error")))
      .onStatus(HttpStatus::is5xxServerError, response -> Mono.error(new FeaturePredictionException("FeaturePredictionService failed with Server Error")))
      .bodyToMono(String.class).blockOptional().orElseThrow(() -> new FeaturePredictionException("Unable to process empty response from FeaturePredictionService"));

    try {

      return ObjectMapperFactory.getJsonObjectMapper().readValue(locationJson, FeaturePredictionContainer.class);
    } catch (JsonProcessingException e) {
      var locationString = locationJson.length() > 100 ? locationJson.substring(0, 100) + "..." : locationJson;
      var errorMessage = "Unable to convert to FeaturePredictionContainer [{" + locationString + "}]";
      throw new FeaturePredictionException(errorMessage);
    }
  }
}
