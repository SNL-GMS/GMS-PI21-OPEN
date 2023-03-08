package gms.shared.featureprediction.request;

import gms.shared.event.coi.EventTestFixtures;
import gms.shared.event.coi.LocationSolution;
import gms.shared.event.coi.featureprediction.type.FeaturePredictionType;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

class PredictForLocationSolutionAndChannelRequestTest {

  @Test
  void testObjectSerialization() {

    PredictForLocationSolutionAndChannelRequest request =
      PredictForLocationSolutionAndChannelRequest.from(
        List.of(FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE),
        LocationSolution.builder()
          .setId(UUID.randomUUID())
          .setData(EventTestFixtures.LOCATION_SOLUTION_DATA.toBuilder().build())
          .build(),
        List.of(Channel.builder()
          .setName("TestChannel")
          .build()),
        List.of(PhaseType.P),
        "Iaspei", List.of());

    TestUtilities.assertSerializes(request, PredictForLocationSolutionAndChannelRequest.class);

  }

}