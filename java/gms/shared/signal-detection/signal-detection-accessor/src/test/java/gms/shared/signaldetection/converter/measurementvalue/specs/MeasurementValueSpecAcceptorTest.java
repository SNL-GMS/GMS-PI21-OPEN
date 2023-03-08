package gms.shared.signaldetection.converter.measurementvalue.specs;

import gms.shared.signaldetection.coi.types.FeatureMeasurementType;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.signaldetection.dao.css.AssocDao;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
abstract class MeasurementValueSpecAcceptorTest<V> {

  @Mock
  private MeasurementValueSpecVisitorInterface<V> specVisitor;

  abstract Consumer<MeasurementValueSpecVisitorInterface<V>> buildSpecVisitorConsumer(
    MeasurementValueSpec<V> expectedMeasurementValueSpec,
    FeatureMeasurementType<V> featureMeasurementType,
    ArrivalDao arrivalDao,
    Optional<AssocDao> assocDao);

  void testSpecAcceptor(MeasurementValueSpec<V> expectedSpec,
    FeatureMeasurementType<V> featureMeasurementType,
    MeasurementValueSpecAcceptorInterface<V> specAcceptor,
    Consumer<MeasurementValueSpecVisitorInterface<V>> specVisitorSetup,
    ArrivalDao arrivalDao,
    Optional<AssocDao> assocDao) {

    specVisitorSetup.accept(specVisitor);

    Stream<MeasurementValueSpec<V>> valueSpec = specAcceptor.accept(specVisitor,
      featureMeasurementType, arrivalDao, assocDao, Optional.empty());

    assertEquals(expectedSpec, valueSpec.findFirst().orElseThrow());
  }

  void testAssocSpecAcceptorNull(FeatureMeasurementType<V> featureMeasurementType,
    MeasurementValueSpecAcceptorInterface<V> specAcceptor,
    Consumer<MeasurementValueSpecVisitorInterface<V>> specVisitorSetup,
    AssocDao assocDao) {

    specVisitorSetup.accept(specVisitor);

    Stream<MeasurementValueSpec<V>> valueSpec = specAcceptor.accept(specVisitor,
      featureMeasurementType, null, Optional.of(assocDao), Optional.empty());

    Integer size = (int) valueSpec.filter(Objects::nonNull).count();
    assertEquals(0, size);
  }

}
