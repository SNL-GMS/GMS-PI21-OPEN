package gms.shared.featureprediction.service;

import gms.shared.event.coi.LocationSolution;
import gms.shared.event.coi.featureprediction.FeaturePredictionContainer;
import gms.shared.featureprediction.framework.FeaturePredictor;
import gms.shared.featureprediction.request.PredictForLocationRequest;
import gms.shared.featureprediction.request.PredictForLocationSolutionAndChannelRequest;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static gms.shared.frameworks.common.ContentType.MSGPACK_NAME;

/**
 * Predict endpoint for FeaturePrediction results,
 */
@RestController
@RequestMapping(value = "/feature",
  consumes = MediaType.APPLICATION_JSON_VALUE,
  produces = {MediaType.APPLICATION_JSON_VALUE, MSGPACK_NAME})
public class FeaturePredictorService {

  private static final Logger logger = LoggerFactory.getLogger(FeaturePredictorService.class);

  private final FeaturePredictor featurePredictorUtility;

  public FeaturePredictorService(
    @Autowired FeaturePredictor predictor) {

    featurePredictorUtility = predictor;
  }

  @PostMapping(value = "/predict-for-location")
  @Operation(summary = "Predict using a location request")
  public FeaturePredictionContainer predict(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "PredictForLocationRequest request endpoint.")
    @RequestBody PredictForLocationRequest lrpRequest) {

    logger.info("FeaturePredictorService predict endpoint for PredictForLocationRequest starting");

    // unpack the PredictForLocationRequest and call featurePredictorUtility.predict
    var predictions = featurePredictorUtility.predict(
      lrpRequest.getPredictionTypes(),
      lrpRequest.getSourceLocation(),
      lrpRequest.getReceiverLocations(),
      lrpRequest.getPhases(),
      lrpRequest.getEarthModel(),
      lrpRequest.getCorrectionDefinitions()
    );

    logger.info("FeaturePredictorService predict processing of PredictForLocationRequest complete");

    return predictions;
  }

  @Operation(summary = "Predict using a location solution and channel request")
  @PostMapping(value = "/predict-for-location-solution-and-channel")
  public LocationSolution predict(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "PredictForLocationSolutionAndChannelRequest request endpoint.")
    @RequestBody PredictForLocationSolutionAndChannelRequest lscRequest) {

    logger.info(
      "FeaturePredictorService predict processing of PredictForLocationSolutionAndChannelRequest starting");

    // unpack the PredictForLocationSolutionAndChannelRequest and call featurePredictorUtility.predict
    var locationSolution = featurePredictorUtility.predict(
      lscRequest.getPredictionTypes(),
      lscRequest.getSourceLocationSolution(),
      lscRequest.getReceivingChannels(),
      lscRequest.getPhases(),
      lscRequest.getEarthModel(),
      lscRequest.getCorrectionDefinitions()
    );

    logger.info(
      "FeaturePredictorService predict processing of PredictForLocationSolutionAndChannelRequest complete");

    return locationSolution;
  }

}
