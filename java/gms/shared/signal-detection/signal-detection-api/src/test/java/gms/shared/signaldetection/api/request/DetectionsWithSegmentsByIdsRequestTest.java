package gms.shared.signaldetection.api.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import gms.shared.utilities.javautilities.objectmapper.ObjectMapperFactory;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class DetectionsWithSegmentsByIdsRequestTest {

  private static final WorkflowDefinitionId STAGE_ID = WorkflowDefinitionId.from("test");

  @ParameterizedTest
  @MethodSource("getCreateValidationArguments")
  void testCreateValidation(Class<? extends Exception> expectedException,
    ImmutableList<UUID> detectionIds,
    WorkflowDefinitionId stageId) {

    assertThrows(expectedException,
      () -> DetectionsWithSegmentsByIdsRequest.create(detectionIds,
        stageId));
  }

  static Stream<Arguments> getCreateValidationArguments() {
    return Stream.of(arguments(IllegalStateException.class,
      ImmutableList.<UUID>builder().build(),
      STAGE_ID));
  }

  @Test
  void testSerialization() throws JsonProcessingException {
    DetectionsWithSegmentsByIdsRequest request = DetectionsWithSegmentsByIdsRequest.create(
      ImmutableList.<UUID>builder().add(UUID.randomUUID()).build(),
      STAGE_ID);

    ObjectMapper mapper = ObjectMapperFactory.getJsonObjectMapper();
    assertEquals(request, mapper.readValue(mapper.writeValueAsString(request),
      DetectionsWithSegmentsByIdsRequest.class));
  }

}