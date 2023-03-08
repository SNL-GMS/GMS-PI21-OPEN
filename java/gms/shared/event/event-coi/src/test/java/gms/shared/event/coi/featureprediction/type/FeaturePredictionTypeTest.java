package gms.shared.event.coi.featureprediction.type;

import gms.shared.event.coi.featureprediction.value.ArrivalTimeFeaturePredictionValue;
import gms.shared.event.coi.featureprediction.value.NumericFeaturePredictionValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FeaturePredictionTypeTest {

  @Test
  void getTypeValueClass() {
    Assertions.assertEquals(
      ArrivalTimeFeaturePredictionValue.class,
      FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE.getTypeValueClass()
    );

    Assertions.assertEquals(
      NumericFeaturePredictionValue.class,
      FeaturePredictionType.SLOWNESS_PREDICTION_TYPE.getTypeValueClass()
    );
  }
}