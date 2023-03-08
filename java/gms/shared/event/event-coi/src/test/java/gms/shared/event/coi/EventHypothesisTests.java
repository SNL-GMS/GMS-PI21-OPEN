package gms.shared.event.coi;

import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class EventHypothesisTests {

  @Mock
  EventHypothesis.Data data;

  @Test
  void testSerializationFullyHydrated() {
    var eventHypothesis = EventTestFixtures.generateDummyEventHypothesis(UUID.randomUUID(), 3.3,
      Instant.EPOCH, MagnitudeType.MB, DoubleValue.from(3.3, Optional.empty(), Units.MAGNITUDE), List.of());

    TestUtilities.assertSerializes(eventHypothesis, EventHypothesis.class);

  }

  @Test
  void testSerializationFaceted() {
    var eventHypothesis = EventHypothesis.createEntityReference(EventHypothesis.Id.from(UUID.randomUUID(), UUID.randomUUID()));
    TestUtilities.assertSerializes(eventHypothesis, EventHypothesis.class);
  }

  @Test
  void testCreateEntityReference() {
    var id = EventHypothesis.Id.from(UUID.randomUUID(), UUID.randomUUID());
    var eventHypothesis = EventHypothesis.createEntityReference(id);
    assertEquals(id, eventHypothesis.getId());
    assertFalse(eventHypothesis.getData().isPresent());
  }

  @Test
  void testToEntityReference() {

    var eventHypothesis = EventHypothesis.builder()
      .setData(data)
      .setId(EventHypothesis.Id.from(UUID.randomUUID(), UUID.randomUUID()))
      .build();

    var entityRef = eventHypothesis.toEntityReference();

    assertTrue(eventHypothesis.getData().isPresent());
    assertEquals(eventHypothesis.getId(), entityRef.getId());
    assertFalse(entityRef.getData().isPresent());
  }

  @Test
  void testCreateRejectedEventHypothesis() {
    var eventUUID = UUID.randomUUID();
    var rejectedEhUUID = UUID.randomUUID();
    var rejectedParentEh = UUID.randomUUID();
    var actualRejectedEventHypothesis = EventHypothesis.createRejectedEventHypothesis(eventUUID, rejectedEhUUID, rejectedParentEh);

    var expectedEventHypothesis = EventHypothesis.builder()
      .setId(EventHypothesis.Id.from(eventUUID, rejectedEhUUID))
      .setData(EventHypothesis.Data.builder()
        .setParentEventHypotheses(List.of(EventHypothesis.builder()
          .setId(EventHypothesis.Id.from(eventUUID, rejectedParentEh))
          .build()))
        .setRejected(true)
        .build())
      .build();
    assertEquals(expectedEventHypothesis, actualRejectedEventHypothesis);
  }

  @ParameterizedTest
  @MethodSource("eventHypothesisDataBuildSource")
  void testEventHypothesisDataBuild(boolean shouldThrow, Class<Throwable> exception,
    EventHypothesis.Data.Builder dataBuilder) {

    if (shouldThrow) {
      assertThrows(exception, dataBuilder::build);
    } else {
      assertDoesNotThrow(dataBuilder::build);
    }
  }

  private static Stream<Arguments> eventHypothesisDataBuildSource() {
    var locationSolution = mock(LocationSolution.class);
    return Stream.of(
      Arguments.arguments(false, null, EventHypothesis.Data.builder()),
      Arguments.arguments(true, IllegalStateException.class, EventHypothesis.Data.builder()
        .setRejected(true)
        .setLocationSolutions(List.of(locationSolution))),
      Arguments.arguments(true, IllegalStateException.class, EventHypothesis.Data.builder()
        .setRejected(true)
        .setPreferredLocationSolution(locationSolution)),
      Arguments.arguments(false, null, EventHypothesis.Data.builder()
        .setRejected(true)),
      Arguments.arguments(true, IllegalStateException.class, EventHypothesis.Data.builder()
        .setRejected(false)
        .setLocationSolutions(List.of(locationSolution))),
      Arguments.arguments(true, IllegalStateException.class, EventHypothesis.Data.builder()
        .setRejected(false)
        .setPreferredLocationSolution(locationSolution)
        .setLocationSolutions(Collections.emptyList())),
      Arguments.arguments(false, null, EventHypothesis.Data.builder()
        .setRejected(false)
        .setPreferredLocationSolution(locationSolution)
        .setLocationSolutions(List.of(locationSolution)))
    );
  }
}