package gms.shared.signaldetection.api.request;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import gms.shared.signaldetection.coi.detection.SignalDetection;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.utilities.javautilities.objectmapper.ObjectMapperFactory;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.stream.Stream;

import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.STATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class DetectionsWithSegmentsByStationsAndTimeRequestTest {

  private static final WorkflowDefinitionId STAGE_ID = WorkflowDefinitionId.from("test");

  @ParameterizedTest
  @MethodSource("getCreateValidationArguments")
  void testCreateValidation(Class<? extends Exception> expectedException,
    ImmutableList<Station> stations,
    Instant startTime,
    Instant endTime,
    WorkflowDefinitionId stageId,
    ImmutableList<SignalDetection> excludedSignalDetections) {
    assertThrows(expectedException,
      () -> DetectionsWithSegmentsByStationsAndTimeRequest.create(stations,
        startTime,
        endTime,
        stageId,
        excludedSignalDetections));
  }

  static Stream<Arguments> getCreateValidationArguments() {
    return Stream.of(arguments(IllegalStateException.class,
        ImmutableList.<Station>builder().build(),
        Instant.EPOCH,
        Instant.MAX,
        STAGE_ID,
        ImmutableList.builder().build()),
      arguments(IllegalStateException.class,
        ImmutableList.<Station>builder().add(STATION).build(),
        Instant.MAX,
        Instant.EPOCH,
        STAGE_ID,
        ImmutableList.builder().build()));
  }

  @Test
  void testSerialization() throws JsonProcessingException {
    DetectionsWithSegmentsByStationsAndTimeRequest request = DetectionsWithSegmentsByStationsAndTimeRequest.create(
      ImmutableList.<Station>builder().add(STATION).build(),
      Instant.EPOCH,
      Instant.MAX,
      STAGE_ID,
      ImmutableList.<SignalDetection>builder().build());

    ObjectMapper mapper = ObjectMapperFactory.getJsonObjectMapper();
    assertEquals(request, mapper.readValue(mapper.writeValueAsString(request),
      DetectionsWithSegmentsByStationsAndTimeRequest.class));
  }
}