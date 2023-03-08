package gms.shared.signaldetection.converter.measurementvalue.specs;

import gms.shared.signaldetection.coi.types.FeatureMeasurementType;
import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.signaldetection.coi.values.NumericMeasurementValue;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.signaldetection.dao.css.AssocDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static gms.shared.signaldetection.repository.utils.SignalDetectionAccessorTestFixtures.SLOWNESS_MEASUREMENT_SPEC;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_1;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlownessMeasurementValueSpecAcceptorTest extends MeasurementValueSpecAcceptorTest<NumericMeasurementValue> {
  protected SlownessMeasurementValueSpecAcceptor converterSpec;

  @BeforeEach
  void setup() {
    converterSpec = SlownessMeasurementValueSpecAcceptor.create();
  }

  @Test
  void testAcceptor() {
    testSpecAcceptor(SLOWNESS_MEASUREMENT_SPEC,
      FeatureMeasurementTypes.SLOWNESS,
      converterSpec,
      buildSpecVisitorConsumer(SLOWNESS_MEASUREMENT_SPEC, FeatureMeasurementTypes.SLOWNESS,
        ARRIVAL_1, Optional.empty()),
      ARRIVAL_1,
      Optional.empty());
  }

  @Override
  Consumer<MeasurementValueSpecVisitorInterface<NumericMeasurementValue>> buildSpecVisitorConsumer(
    MeasurementValueSpec<NumericMeasurementValue> measurementValueSpec,
    FeatureMeasurementType<NumericMeasurementValue> featureMeasurementType,
    ArrivalDao arrivalDao,
    Optional<AssocDao> assocDao) {
    // Measurement value spec visitor setup
    return visitor ->
      when(visitor.visit(converterSpec,
        featureMeasurementType,
        arrivalDao,
        assocDao))
        .thenReturn(Stream.of(measurementValueSpec));
  }
}