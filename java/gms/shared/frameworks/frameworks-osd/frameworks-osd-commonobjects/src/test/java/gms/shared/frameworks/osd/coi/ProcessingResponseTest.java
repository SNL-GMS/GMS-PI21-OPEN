package gms.shared.frameworks.osd.coi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.event.EventTestFixtures;
import gms.shared.frameworks.osd.coi.signaldetection.SignalDetectionHypothesis;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcessingResponseTest {

  @Test
  void testSerialization() throws Exception {
    ProcessingResponse<SignalDetectionHypothesis> expected = ProcessingResponse
      .from(
        List.of(SignalDetectionHypothesis
          .from(UUID.randomUUID(), UUID.randomUUID(), "test",
            UtilsTestFixtures.STATION.getName(), UUID.randomUUID(), false,
            List.of(EventTestFixtures.ARRIVAL_TIME_FEATURE_MEASUREMENT,
              EventTestFixtures.PHASE_FEATURE_MEASUREMENT))),
        List.of(SignalDetectionHypothesis
          .from(UUID.randomUUID(), UUID.randomUUID(), "test",
            UtilsTestFixtures.STATION.getName(), UUID.randomUUID(), false,
            List.of(EventTestFixtures.ARRIVAL_TIME_FEATURE_MEASUREMENT,
              EventTestFixtures.PHASE_FEATURE_MEASUREMENT))),
        List.of(SignalDetectionHypothesis
          .from(UUID.randomUUID(), UUID.randomUUID(), "test",
            UtilsTestFixtures.STATION.getName(), UUID.randomUUID(), false,
            List.of(EventTestFixtures.ARRIVAL_TIME_FEATURE_MEASUREMENT,
              EventTestFixtures.PHASE_FEATURE_MEASUREMENT))));

    ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

    ProcessingResponse<SignalDetectionHypothesis> actual = objectMapper
      .readValue(objectMapper.writeValueAsString(expected),
        new TypeReference<ProcessingResponse<SignalDetectionHypothesis>>() {
        });

    assertEquals(expected, actual);
  }
}
