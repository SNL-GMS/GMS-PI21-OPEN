package gms.shared.signaldetection.coi.detection;

import com.fasterxml.jackson.core.JsonProcessingException;
import gms.shared.utilities.javautilities.objectmapper.ObjectMapperFactory;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SignalDetectionHypothesisReferenceTests {

  @Test
  void testSerialization() throws JsonProcessingException {
    final var id1 = UUID.randomUUID();
    final var id2 = UUID.randomUUID();
    final var signalDetectionHypothesisId = SignalDetectionHypothesisId.from(id1, id2);
    final var signalDetectionHypothesisReference = SignalDetectionHypothesisReference.builder()
      .setId(signalDetectionHypothesisId)
      .build();
    final var mapper = ObjectMapperFactory.getJsonObjectMapper();
    assertEquals(signalDetectionHypothesisReference,
      mapper.readValue(mapper.writeValueAsString(signalDetectionHypothesisReference),
        SignalDetectionHypothesisReference.class));
  }
}
