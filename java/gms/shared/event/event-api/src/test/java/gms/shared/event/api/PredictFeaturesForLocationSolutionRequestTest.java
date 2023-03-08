package gms.shared.event.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.event.coi.EventTestFixtures;
import gms.shared.event.coi.MagnitudeType;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.stationdefinition.testfixtures.UtilsTestFixtures;
import gms.shared.utilities.javautilities.objectmapper.ObjectMapperFactory;
import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PredictFeaturesForLocationSolutionRequestTest {

  private static final ObjectMapper MAPPER = ObjectMapperFactory.getJsonObjectMapper();

  @Test
  void testSerialization() {

    var eventHypothesis = EventTestFixtures.generateDummyEventHypothesis(UUID.randomUUID(), 3.3,
      Instant.EPOCH, MagnitudeType.MB, DoubleValue.from(3.3, Optional.empty(), Units.MAGNITUDE), List.of());
    assertTrue(eventHypothesis.getData().isPresent());
    assertTrue(eventHypothesis.getData().get().getLocationSolutions().size() > 0);
    var locationSolution = eventHypothesis.getData().get().getLocationSolutions().iterator().next();
    var request = PredictFeaturesForLocationSolutionRequest.from(locationSolution, List.of(UtilsTestFixtures.CHANNEL), List.of(PhaseType.P));
    TestUtilities.assertSerializes(request, PredictFeaturesForLocationSolutionRequest.class);

  }

}
