package gms.shared.featureprediction.service;

import gms.shared.event.coi.EventLocation;
import gms.shared.event.coi.LocationSolution;
import gms.shared.event.coi.featureprediction.type.FeaturePredictionType;
import gms.shared.featureprediction.configuration.FeaturePredictorConfiguration;
import gms.shared.featureprediction.framework.FeaturePredictor;
import gms.shared.featureprediction.request.PredictForLocationRequest;
import gms.shared.featureprediction.request.PredictForLocationSolutionAndChannelRequest;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.plugin.PluginRegistry;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.spring.utilities.framework.SpringTestBase;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.Location;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static gms.shared.event.coi.EventTestFixtures.LOCATION_SOLUTION_DATA;

@WebMvcTest(FeaturePredictorService.class)
class FeaturePredictorServiceTest extends SpringTestBase {

  @MockBean
  private SystemConfig systemConfig;

  @MockBean
  private FeaturePredictor featurePredictor;

  @MockBean
  PluginRegistry registry;

  @MockBean
  FeaturePredictorConfiguration configuration;

  /*
  JSON for postman test

  {
    "predictionTypes": ["ARRIVAL_TIME"],
    "sourceLocation": {
        "latitudeDegrees": 0.0,
        "longitudeDegrees": 0.0,
        "depthKm": 0.0,
        "time": "1970-01-01T00:00:00Z"
    },
    "receiverLocations": [{
        "latitudeDegrees": 100.0,
        "longitudeDegrees": 150.0,
        "depthKm": 30.0,
        "elevationKm": 20.0
    }],
    "phases": ["P"],
    "earthModel": "String"
}
   */

  @Test
  void testPredictLocation() throws Exception {

    PredictForLocationRequest request = PredictForLocationRequest.from(
      List.of(FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE),
      EventLocation
        .from(0.0, 0.0, 0.0, Instant.EPOCH),
      List.of(Location
        .from(100.0, 150.0, 30, 20)),
      List.of(PhaseType.P),
      "String",
      List.of());

    MockHttpServletResponse response = postResult(
      "/feature//predict-for-location",
      request,
      HttpStatus.OK);

    Assertions.assertEquals(response.getStatus(), HttpStatus.OK.value());

  }

  @Test
  void testPredictLocationSolutionAndChannel() throws Exception {

    PredictForLocationSolutionAndChannelRequest request = PredictForLocationSolutionAndChannelRequest.from(
      List.of(FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE),
      LocationSolution.builder()
        .setId(UUID.randomUUID())
        .setData(LOCATION_SOLUTION_DATA)
        .build(),
      List.of(Channel.builder()
        .setName("fakeName")
        .autoBuild()),
      List.of(PhaseType.P),
      "String",
      List.of());

    MockHttpServletResponse response = postResult(
      "/feature/predict-for-location-solution-and-channel",
      request,
      HttpStatus.OK);

    Assertions.assertEquals(response.getStatus(), HttpStatus.OK.value());

  }

}