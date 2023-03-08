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

class ReceiverToSourceAzimuthMeasurementValueConverterTest
  extends SignalDetectionMeasurementValueConverterTest<ReceiverToSourceAzimuthMeasurementValueConverter> {

  private ReceiverToSourceAzimuthMeasurementValueConverter converter;

  private static final NumericMeasurementValue VALUE_1 = NumericMeasurementValue.from(Optional.empty(),
    DoubleValue.from(180, Optional.of(0.5), Units.DEGREES));

  @BeforeEach
  void setup() {
    converter = ReceiverToSourceAzimuthMeasurementValueConverter.create();
  }

  @Test
  void testCreate() {
    ReceiverToSourceAzimuthMeasurementValueConverter converter =
      assertDoesNotThrow(ReceiverToSourceAzimuthMeasurementValueConverter::create);
    assertNotNull(converter);
  }

  @Test
  void testConvert() {
    MeasurementValueSpec<NumericMeasurementValue> measurementValueSpec1 = MeasurementValueSpec.<NumericMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_1)
      .setFeatureMeasurementType(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH)
      .setMeasuredValueExtractor(ArrivalDao::getAzimuth)
      .setUncertaintyValueExtractor(ArrivalDao::getAzimuthUncertainty)
      .setUnits(Units.DEGREES)
      .build();

    Optional<NumericMeasurementValue> actualOptional1 = converter.convert(measurementValueSpec1);

    assertTrue(actualOptional1.isPresent());
    assertEquals(VALUE_1, actualOptional1.orElseThrow());
    assertEquals(VALUE_1.getMeasuredValue().getValue(), actualOptional1.orElseThrow().getMeasuredValue().getValue());
    assertEquals(VALUE_1.getMeasuredValue().getStandardDeviation().orElseThrow(), actualOptional1.orElseThrow().getMeasuredValue().getStandardDeviation().orElseThrow());
  }

  @Test
  void testNullArguments() {
    testConvertNull(ReceiverToSourceAzimuthMeasurementValueConverter.create());
  }
}
