package gms.shared.signaldetection.converter.measurementvalue.specs;

import gms.shared.signaldetection.coi.types.FeatureMeasurementType;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.stationdefinition.coi.utils.Units;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.function.ToDoubleFunction;

import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.AMPLITUDE_DAO_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ASSOC_DAO_1;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class MeasurementValueSpecTest<V> {
  private MeasurementValueSpec.Builder<V> builder;

  @Mock
  private FeatureMeasurementType<V> featureMeasurementType;

  @BeforeEach
  public void testSetup() {
    builder = MeasurementValueSpec.builder();
  }

  @Test
  void testSerializationDeserialization() {
    Optional<String> typeIndex = Optional.of("c");
    Optional<ToDoubleFunction<ArrivalDao>> measuredExtractor = Optional.of(ArrivalDao::getSlowness);
    Optional<ToDoubleFunction<ArrivalDao>> uncertaintyExtractor = Optional.of(ArrivalDao::getSlownessUncertainty);
    Optional<Units> units = Optional.of(Units.SECONDS_PER_DEGREE);
    MeasurementValueSpec<?> measurementValueSpec = builder
      .setFeatureMeasurementType(featureMeasurementType)
      .setArrivalDao(ARRIVAL_1)
      .setMeasuredValueExtractor(measuredExtractor)
      .setUncertaintyValueExtractor(uncertaintyExtractor)
      .setFeatureMeasurementTypeCode("c")
      .setUnits(units)
      .build();
    assertEquals(ARRIVAL_1, measurementValueSpec.getArrivalDao());
    assertEquals(featureMeasurementType, measurementValueSpec.getFeatureMeasurementType());
    assertEquals(typeIndex, measurementValueSpec.getFeatureMeasurementTypeCode());
    assertEquals(measuredExtractor, measurementValueSpec.getMeasuredValueExtractor());
    assertEquals(uncertaintyExtractor, measurementValueSpec.getUncertaintyValueExtractor());
    assertEquals(units, measurementValueSpec.getUnits());
  }


  @Test
  void testSerializationDeserializationWithOptionalDaos() {
    Optional<String> typeIndex = Optional.of("c");
    Optional<ToDoubleFunction<ArrivalDao>> measuredExtractor = Optional.of(ArrivalDao::getSlowness);
    Optional<ToDoubleFunction<ArrivalDao>> uncertaintyExtractor = Optional.of(ArrivalDao::getSlownessUncertainty);
    Optional<Units> units = Optional.of(Units.SECONDS_PER_DEGREE);
    MeasurementValueSpec<?> measurementValueSpec = builder
      .setFeatureMeasurementType(featureMeasurementType)
      .setArrivalDao(ARRIVAL_1)
      .setAmplitudeDao(AMPLITUDE_DAO_1)
      .setAssocDao(ASSOC_DAO_1)
      .setMeasuredValueExtractor(measuredExtractor)
      .setUncertaintyValueExtractor(uncertaintyExtractor)
      .setFeatureMeasurementTypeCode("c")
      .setUnits(units)
      .build();
    assertEquals(ARRIVAL_1, measurementValueSpec.getArrivalDao());
    assertEquals(AMPLITUDE_DAO_1, measurementValueSpec.getAmplitudeDao().get());
    assertEquals(ASSOC_DAO_1, measurementValueSpec.getAssocDao().get());
    assertEquals(featureMeasurementType, measurementValueSpec.getFeatureMeasurementType());
    assertEquals(typeIndex, measurementValueSpec.getFeatureMeasurementTypeCode());
    assertEquals(measuredExtractor, measurementValueSpec.getMeasuredValueExtractor());
    assertEquals(uncertaintyExtractor, measurementValueSpec.getUncertaintyValueExtractor());
    assertEquals(units, measurementValueSpec.getUnits());
  }
}
