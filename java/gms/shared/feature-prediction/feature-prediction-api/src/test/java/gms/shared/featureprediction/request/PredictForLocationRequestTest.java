package gms.shared.featureprediction.request;


import gms.shared.event.coi.EventLocation;
import gms.shared.event.coi.featureprediction.type.FeaturePredictionType;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.stationdefinition.coi.channel.Location;
import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

class PredictForLocationRequestTest {

  @Test
  void testObjectSerialization() {

    PredictForLocationRequest request = PredictForLocationRequest.from(
      List.of(FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE),
      EventLocation
        .from(0.0, 0.0, 0.0, Instant.EPOCH),
      List.of(Location
        .from(100.0, 150.0, 30, 20)),
      List.of(PhaseType.P),
      "Iaspei", List.of());

    TestUtilities.assertSerializes(request, PredictForLocationRequest.class);

  }
}