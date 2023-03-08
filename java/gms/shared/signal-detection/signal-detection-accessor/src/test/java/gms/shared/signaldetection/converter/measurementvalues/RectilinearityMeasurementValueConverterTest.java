package gms.shared.signaldetection.converter.measurementvalues;

import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.signaldetection.coi.values.NumericMeasurementValue;
import gms.shared.signaldetection.converter.measurementvalue.specs.MeasurementValueSpec;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.stationdefinition.coi.utils.Units;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_1;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RectilinearityMeasurementValueConverterTest
  extends SignalDetectionMeasurementValueConverterTest<RectilinearityMeasurementValueConverter> {

  private RectilinearityMeasurementValueConverter converter;

  private static final NumericMeasurementValue VALUE_1 = NumericMeasurementValue.from(Optional.empty(),
    DoubleValue.from(0.23, Optional.empty(), Units.UNITLESS));

  @BeforeEach
  void setup() {
    converter = RectilinearityMeasurementValueConverter.create();
  }

  @Test
  void testCreate() {
    RectilinearityMeasurementValueConverter converter =
      assertDoesNotThrow(RectilinearityMeasurementValueConverter::create);
    assertNotNull(converter);
  }

  @Test
  void testConvert() {
    MeasurementValueSpec<NumericMeasurementValue> measurementValueSpec1 = MeasurementValueSpec.<NumericMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_1)
      .setFeatureMeasurementType(FeatureMeasurementTypes.RECTILINEARITY)
      .setMeasuredValueExtractor(ArrivalDao::getRectilinearity)
      .setUnits(Units.UNITLESS)
      .build();

    Optional<NumericMeasurementValue> actualOptional = converter.convert(measurementValueSpec1);

    NumericMeasurementValue actual = actualOptional.orElseThrow();
    DoubleValue value = actual.getMeasuredValue();

    assertTrue(value.getStandardDeviation().isEmpty());
    assertEquals(VALUE_1, actualOptional.orElseThrow());
    assertEquals(Units.UNITLESS, value.getUnits());
  }

  @Test
  void testNullArguments() {
    testConvertNull(RectilinearityMeasurementValueConverter.create());
  }
}
