package gms.shared.event.manager;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.event.coi.EventTestFixtures;
import gms.shared.event.coi.LocationSolution;
import gms.shared.event.manager.config.EventManagerConfiguration;
import gms.shared.featureprediction.request.PredictForLocationSolutionAndChannelRequest;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.system.events.SystemEventPublisher;
import gms.shared.utilities.javautilities.objectmapper.ObjectMapperFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
class WebRequestsTest {

  WebRequestsTest() throws URISyntaxException {
  }

  private static final String POST_URI = "/feature-prediction-service/feature/predict-for-location-solution-and-channel";

  private WebRequests webRequests;

  @Mock
  private ConfigurationConsumerUtility configurationConsumerUtility;

  @Mock
  private SystemConfig sysconfig;

  @Mock
  private SystemEventPublisher systemEventPublisher;
  @Mock
  private EventManagerConfiguration eventManagerConfiguration;

  private final static LocationSolution LOCATION_SOLUTION = LocationSolution.builder()
    .setId(UUID.randomUUID())
    .setData(EventTestFixtures.LOCATION_SOLUTION_DATA)
    .build();

  private final ExchangeFunction exchangeFunction = Mockito.mock(ExchangeFunction.class);
  private final WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();

  private final URI PREDICT_URI = new URI("http://localhost:8080" + POST_URI);

  private final ObjectMapper mapper = ObjectMapperFactory.getJsonObjectMapper();


  @BeforeEach
  void setUp() {
    Mockito.when(eventManagerConfiguration.predictForLocationSolutionAndChannelUrl()).thenReturn(PREDICT_URI);
    webRequests = new WebRequests(eventManagerConfiguration, webClient.mutate());
  }

  @Test
  void testReturnsLocationSolutionWhenSuccessful() throws FeaturePredictionException {
    ClientResponse response = ClientResponse.create(HttpStatus.OK)
      .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .body(successBody()).build();
    Mockito.when(exchangeFunction.exchange(Mockito.any(ClientRequest.class))).thenReturn(Mono.just(response));

    var predictForLocationSolutionAndChannelRequest = PredictForLocationSolutionAndChannelRequest
      .from(List.of(), LOCATION_SOLUTION, List.of(), List.of(), "EarthModelTest", List.of());
    var actualLocationSolution = webRequests.fpsWebRequestPredictForLocationSolutionAndChannel(predictForLocationSolutionAndChannelRequest);

    assertEquals(LOCATION_SOLUTION, actualLocationSolution);
  }

  @Test
  void testThrowsFeaturePredictionBadString() {
    var locationSolutionString = "Somethings Wrong...";
    var expectedExceptionMsg = "Unable to convert to LocationSolution [{" + locationSolutionString + "}]";

    ClientResponse response = ClientResponse.create(HttpStatus.OK)
      .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .body(locationSolutionString).build();
    Mockito.when(exchangeFunction.exchange(Mockito.any(ClientRequest.class))).thenReturn(Mono.just(response));

    var predictForLocationSolutionAndChannelRequest = PredictForLocationSolutionAndChannelRequest
      .from(List.of(), LOCATION_SOLUTION, List.of(), List.of(), "EarthModelTest", List.of());
    FeaturePredictionException exception = assertThrows(FeaturePredictionException.class, () -> webRequests.fpsWebRequestPredictForLocationSolutionAndChannel(predictForLocationSolutionAndChannelRequest));
    assertEquals(expectedExceptionMsg, exception.getMessage());

  }

  @Test
  void testThrowsFeaturePredictionLongBadString() {
    var locationSolutionString = "Somethings Wrong. With the returned string response".repeat(3);
    var expectedExceptionMsg = "Unable to convert to LocationSolution [{" + locationSolutionString.substring(0, 100) + "..." + "}]";

    ClientResponse response = ClientResponse.create(HttpStatus.OK)
      .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .body(locationSolutionString).build();
    Mockito.when(exchangeFunction.exchange(Mockito.any(ClientRequest.class))).thenReturn(Mono.just(response));

    var predictForLocationSolutionAndChannelRequest = PredictForLocationSolutionAndChannelRequest
      .from(List.of(), LOCATION_SOLUTION, List.of(), List.of(), "EarthModelTest", List.of());
    FeaturePredictionException exception = assertThrows(FeaturePredictionException.class, () -> webRequests.fpsWebRequestPredictForLocationSolutionAndChannel(predictForLocationSolutionAndChannelRequest));
    assertEquals(expectedExceptionMsg, exception.getMessage());

  }

  @Test
  void testThrowsFeaturePredictionExceptionWhenClientErrorStatus() {

    ClientResponse response = ClientResponse.create(HttpStatus.BAD_REQUEST)
      .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .body("{}").build();
    Mockito.when(exchangeFunction.exchange(Mockito.any(ClientRequest.class))).thenReturn(Mono.just(response));

    var predictForLocationSolutionAndChannelRequest = PredictForLocationSolutionAndChannelRequest
      .from(List.of(), LOCATION_SOLUTION, List.of(), List.of(), "EarthModelTest", List.of());

    var monoCallable = Mono.fromCallable(() -> webRequests.fpsWebRequestPredictForLocationSolutionAndChannel(predictForLocationSolutionAndChannelRequest));

    StepVerifier.create(monoCallable)
      .expectErrorMatches(throwable -> throwable instanceof FeaturePredictionException &&
        "FeaturePredictionService failed with Client Error".equals(throwable.getMessage()))
      .verify();

  }

  @Test
  void testThrowsFeaturePredictionExceptionWhenServerErrorStatus() {
    ClientResponse response = ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR)
      .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .body("{}").build();
    Mockito.when(exchangeFunction.exchange(Mockito.any(ClientRequest.class))).thenReturn(Mono.just(response));

    var predictForLocationSolutionAndChannelRequest = PredictForLocationSolutionAndChannelRequest
      .from(List.of(), LOCATION_SOLUTION, List.of(), List.of(), "EarthModelTest", List.of());

    var monoCallable = Mono.fromCallable(() -> webRequests.fpsWebRequestPredictForLocationSolutionAndChannel(predictForLocationSolutionAndChannelRequest));

    StepVerifier.create(monoCallable)
      .expectErrorMatches(throwable -> throwable instanceof FeaturePredictionException &&
        "FeaturePredictionService failed with Server Error".equals(throwable.getMessage()))
      .verify();
  }

  private String successBody() {
    try {
      return mapper.writeValueAsString(LOCATION_SOLUTION);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }
}
