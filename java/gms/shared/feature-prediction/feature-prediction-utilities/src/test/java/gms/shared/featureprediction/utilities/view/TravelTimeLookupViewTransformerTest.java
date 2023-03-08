package gms.shared.featureprediction.utilities.view;

import gms.shared.featureprediction.utilities.data.EarthModelType;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.stationdefinition.coi.utils.Units;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;


class TravelTimeLookupViewTransformerTest {

  TravelTimeLookupViewTransformer view = new TravelTimeLookupViewTransformer();

  @Test
  void transformValidation() throws IOException {

    var data = Thread.currentThread().getContextClassLoader().getResourceAsStream(
      "gms.feature-prediction.data/travelTime_small.json");

    TravelTimeLookupView object = null;
    object = view.transform(data);

    assertNotNull(object);

    var deserialized = TravelTimeLookupView.builder()
      .setModel(EarthModelType.AK135)
      .setPhase(PhaseType.P)
      .setRawPhaseString("P")
      .setDepthUnits(Units.KILOMETERS)
      .setDistanceUnits(Units.DEGREES)
      .setTravelTimeUnits(Units.SECONDS)
      .setDepths(new double[]{0.0, 700.0})
      .setDistances(new double[]{0.0, 180.0})
      .setTravelTimes(new Duration[][]
        {
          {
            Duration.parse("PT0.0S"),
            Duration.parse("PT1212.5273S")
          },
          {
            Duration.parse("PT79.6958S"),
            Duration.parse("PT1132.8315S")
          }
        })
      .setModelingErrorDepths(new double[]{0.0, 200.0})
      .setModelingErrorDistances(new double[]{0.0, 180.0})
      .setModelingErrors(new Duration[][]
        {
          {
            Duration.parse("PT0.1S"),
            Duration.parse("PT1.8S")
          },
          {
            Duration.parse("PT0.7S"),
            Duration.parse("PT1.5S")
          }
        }
      )
      .build();

    Assertions.assertEquals(deserialized, object);
  }

}