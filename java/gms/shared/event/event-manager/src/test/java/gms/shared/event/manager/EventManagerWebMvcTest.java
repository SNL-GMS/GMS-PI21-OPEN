package gms.shared.event.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.event.api.EventAccessorInterface;
import gms.shared.event.api.EventsByAssociatedSignalDetectionHypothesesRequest;
import gms.shared.event.api.EventsByTimeRequest;
import gms.shared.event.api.EventsWithDetectionsAndSegmentsByTimeRequest;
import gms.shared.event.api.PredictFeaturesForLocationSolutionRequest;
import gms.shared.event.coi.EventTestFixtures;
import gms.shared.event.coi.LocationSolution;
import gms.shared.event.manager.config.EventManagerConfiguration;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.spring.utilities.framework.SpringTestBase;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.Location;
import gms.shared.stationdefinition.testfixtures.UtilsTestFixtures;
import gms.shared.system.events.SystemEventPublisher;
import gms.shared.utilities.javautilities.objectmapper.ObjectMapperFactory;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.times;

@Import({EventManagerConfiguration.class, EventManagerTestConfiguration.class})
@WebMvcTest
class EventManagerWebMvcTest extends SpringTestBase {

  private ObjectMapper objectMapper;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private EventManagerConfiguration eventManagerConfiguration;

  @MockBean
  private EventAccessorInterface eventAccessor;

  @MockBean
  private SystemConfig systemConfig;

  @MockBean
  private SystemEventPublisher systemEventPublisher;

  @MockBean
  private WebRequests webRequests;

  @BeforeEach
  void init() {
    objectMapper = ObjectMapperFactory.getJsonObjectMapper();
  }

  @Test
  void testFindEventsById() throws Exception {
    final var stageId = "Auto Network";
    final var eventId = "02e74f10-e032-3ad8-a8d1-38f2b4fdd6f0";

    final var urlPath = String.format("/event/%s/%s", stageId, eventId);

    final var response = getResult(
      urlPath,
      HttpStatus.OK
    );

    Mockito.verify(eventAccessor, times(1)).findByIds(
      List.of(UUID.fromString(eventId)),
      WorkflowDefinitionId.from(stageId));
  }

  @Test
  void testFindEventsByTime_FacetingDefinition() throws Exception {
    final var eventsByTimeRequest = EventsByTimeRequest.create(
      Instant.MIN,
      Instant.MAX,
      WorkflowDefinitionId.from("test"),
      EventTestFixtures.DEFAULT_EVENT_FACETING_DEFINITION
    );

    final var response = postResult(
      "/event/time",
      eventsByTimeRequest,
      HttpStatus.OK
    );

    Mockito.verify(eventAccessor, times(1)).findByTime(
      eventsByTimeRequest.getStartTime(),
      eventsByTimeRequest.getEndTime(),
      eventsByTimeRequest.getStageId(),
      eventsByTimeRequest.getFacetingDefinition()
    );
  }

  @Test
  void testFindEventsByTime_NoFacetingDefinition() throws Exception {
    final var eventsByTimeRequest = EventsByTimeRequest.create(
      Instant.MIN,
      Instant.MAX,
      WorkflowDefinitionId.from("test"),
      null
    );

    final var response = postResult(
      "/event/time",
      eventsByTimeRequest,
      HttpStatus.OK
    );

    Mockito.verify(eventAccessor, times(1)).findByTime(
      eventsByTimeRequest.getStartTime(),
      eventsByTimeRequest.getEndTime(),
      eventsByTimeRequest.getStageId(),
      eventsByTimeRequest.getFacetingDefinition()
    );
  }

  @Test
  void testFindEventsWithDetectionsAndSegmentsByTime() throws Exception {
    final var eventsWithDetectionsAndSegmentsByTimeRequest =
      EventsWithDetectionsAndSegmentsByTimeRequest.create(
        Instant.MIN,
        Instant.MAX,
        WorkflowDefinitionId.from("test")
      );

    final var response = postResult(
      "/event/detections-and-segments/time",
      eventsWithDetectionsAndSegmentsByTimeRequest,
      HttpStatus.OK
    );

    Mockito.verify(eventAccessor, times(1)).findEventsWithDetectionsAndSegmentsByTime(
      eventsWithDetectionsAndSegmentsByTimeRequest.getStartTime(),
      eventsWithDetectionsAndSegmentsByTimeRequest.getEndTime(),
      eventsWithDetectionsAndSegmentsByTimeRequest.getStageId()
    );
  }

  @Test
  void testFindAssociatedSignalDetectionHypothesis() throws Exception {
    final var eventsByAssociatedSignalDetectionHypothesisRequest = EventsByAssociatedSignalDetectionHypothesesRequest.create(
      List.of(EventTestFixtures.SIGNAL_DETECTION_HYPOTHESIS),
      WorkflowDefinitionId.from("test")
    );

    final var response = postResult(
      "/event/associated-signal-detection-hypotheses",
      eventsByAssociatedSignalDetectionHypothesisRequest,
      HttpStatus.OK
    );

    Mockito.verify(eventAccessor, times(1)).findByAssociatedDetectionHypotheses(
      eventsByAssociatedSignalDetectionHypothesisRequest.getSignalDetectionHypotheses(),
      eventsByAssociatedSignalDetectionHypothesisRequest.getStageId()
    );
  }

  @Test
  void testPredictFeaturesForLocationSolution_EmptyLocationSolution() throws Exception {
    // Tests that EventManager handles an EventRequestException correctly and returns a 400 with an error message

    // Create request body for eventManager.predictFeaturesForLocationSolution
    var locationSolution = LocationSolution.builder().setId(UUID.randomUUID()).build();

    var channelName = "channelOne";
    var channelLocation = Location.from(0.0, 0.0, 0.0, 0.0);
    var channel = createTestChannel(channelName, channelLocation);
    var channels = List.of(channel);

    var phaseTypes = List.of(PhaseType.P);

    var predictFeaturesForLocationSolutionRequestBody = PredictFeaturesForLocationSolutionRequest.from(
      locationSolution,
      channels,
      phaseTypes
    );

    // Peform request and expect 400
    var mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/event/predict")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(predictFeaturesForLocationSolutionRequestBody)))
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andReturn();

    // Assert service returns correct error message
    var responseBody = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
    var errorMessage = responseBody.get("errorMessage").asText();
    Assertions.assertEquals("Error processing request body: Provided LocationSolution has no data", errorMessage);
  }

  // Creates a test Channel with the provided channelName at the provided Location
  private Channel createTestChannel(String channelName, Location channelLocation) {
    return UtilsTestFixtures.CHANNEL.toBuilder()
      .setName(channelName)
      .setData(
        UtilsTestFixtures.CHANNEL.getData().orElseThrow().toBuilder()
          .setLocation(channelLocation)
          .build()
      )
      .build();
  }
}
