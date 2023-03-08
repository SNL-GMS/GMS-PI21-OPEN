package gms.shared.event.coi;

import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.stream.Stream;

import static gms.shared.event.coi.EventTestFixtures.CONFIDENCE_LEVEL;
import static gms.shared.event.coi.EventTestFixtures.INTERMEDIATE_AXIS_LENGTH;
import static gms.shared.event.coi.EventTestFixtures.INTERMEDIATE_AXIS_PLUNGE;
import static gms.shared.event.coi.EventTestFixtures.INTERMEDIATE_AXIS_TREND;
import static gms.shared.event.coi.EventTestFixtures.K_WEIGHT;
import static gms.shared.event.coi.EventTestFixtures.MAJOR_AXIS_LENGTH;
import static gms.shared.event.coi.EventTestFixtures.MAJOR_AXIS_PLUNGE;
import static gms.shared.event.coi.EventTestFixtures.MAJOR_AXIS_TREND;
import static gms.shared.event.coi.EventTestFixtures.MINOR_AXIS_LENGTH;
import static gms.shared.event.coi.EventTestFixtures.MINOR_AXIS_PLUNGE;
import static gms.shared.event.coi.EventTestFixtures.MINOR_AXIS_TREND;
import static gms.shared.event.coi.EventTestFixtures.TIME_UNCERTAINTY;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EllipsoidTests {
  private static final ScalingFactorType SCALING_FACTOR_TYPE = ScalingFactorType.CONFIDENCE;
  private static final Ellipsoid ELLIPSOID = Ellipsoid.builder()
    .setScalingFactorType(SCALING_FACTOR_TYPE)
    .setkWeight(K_WEIGHT)
    .setConfidenceLevel(CONFIDENCE_LEVEL)
    .setSemiMajorAxisLengthKm(MAJOR_AXIS_LENGTH)
    .setSemiMajorAxisTrendDeg(MAJOR_AXIS_TREND)
    .setSemiMajorAxisPlungeDeg(MAJOR_AXIS_PLUNGE)
    .setSemiIntermediateAxisLengthKm(INTERMEDIATE_AXIS_LENGTH)
    .setSemiIntermediateAxisTrendDeg(INTERMEDIATE_AXIS_TREND)
    .setSemiIntermediateAxisPlungeDeg(INTERMEDIATE_AXIS_PLUNGE)
    .setSemiMinorAxisLengthKm(MINOR_AXIS_LENGTH)
    .setSemiMinorAxisTrendDeg(MINOR_AXIS_TREND)
    .setSemiMinorAxisPlungeDeg(MINOR_AXIS_PLUNGE)
    .setTimeUncertainty(TIME_UNCERTAINTY)
    .build();

  private static Stream<Arguments> ellipsoidValidationSource() {

    return Stream.of(
      // Confidence level within expected bounds [0.5, 1]
      Arguments.arguments(SCALING_FACTOR_TYPE, 0.4, K_WEIGHT, TIME_UNCERTAINTY, true),
      Arguments.arguments(SCALING_FACTOR_TYPE, 1.1, K_WEIGHT, TIME_UNCERTAINTY, true),
      Arguments.arguments(SCALING_FACTOR_TYPE, 0.5, K_WEIGHT, TIME_UNCERTAINTY, false),
      Arguments.arguments(SCALING_FACTOR_TYPE, 0.7, K_WEIGHT, TIME_UNCERTAINTY, false),
      Arguments.arguments(SCALING_FACTOR_TYPE, 1.0, K_WEIGHT, TIME_UNCERTAINTY, false),
      // Coverage infinity kWeight
      Arguments.arguments(ScalingFactorType.COVERAGE, CONFIDENCE_LEVEL, K_WEIGHT, TIME_UNCERTAINTY, true),
      Arguments.arguments(ScalingFactorType.COVERAGE, CONFIDENCE_LEVEL, Double.POSITIVE_INFINITY, TIME_UNCERTAINTY, false),
      // K-Weighted positive kWeight
      Arguments.arguments(ScalingFactorType.K_WEIGHTED, CONFIDENCE_LEVEL, -1.0, TIME_UNCERTAINTY, true),
      Arguments.arguments(ScalingFactorType.K_WEIGHTED, CONFIDENCE_LEVEL, 0.0, TIME_UNCERTAINTY, false),
      Arguments.arguments(ScalingFactorType.K_WEIGHTED, CONFIDENCE_LEVEL, 0.1, TIME_UNCERTAINTY, false),
      // Confidence kWeight is 0.0
      Arguments.arguments(ScalingFactorType.CONFIDENCE, CONFIDENCE_LEVEL, 0.1, TIME_UNCERTAINTY, true),
      Arguments.arguments(ScalingFactorType.CONFIDENCE, CONFIDENCE_LEVEL, -0.1, TIME_UNCERTAINTY, true),
      Arguments.arguments(ScalingFactorType.CONFIDENCE, CONFIDENCE_LEVEL, 0.0, TIME_UNCERTAINTY, false),
      // Non-negative time uncertainty
      Arguments.arguments(SCALING_FACTOR_TYPE, CONFIDENCE_LEVEL, K_WEIGHT, Duration.ofNanos(1).negated(), true),
      Arguments.arguments(SCALING_FACTOR_TYPE, CONFIDENCE_LEVEL, K_WEIGHT, Duration.ZERO, false),
      Arguments.arguments(SCALING_FACTOR_TYPE, CONFIDENCE_LEVEL, K_WEIGHT, Duration.ofNanos(1), false)
    );
  }

  @ParameterizedTest
  @MethodSource("ellipsoidValidationSource")
  void testEllipsoidValidation(ScalingFactorType scalingFactorType, double confidenceLevel, double kWeight,
    Duration TIME_UNCERTAINTY, boolean shouldFail) {
    var testEllipseBuilder = ELLIPSOID.toBuilder()
      .setScalingFactorType(scalingFactorType)
      .setConfidenceLevel(confidenceLevel)
      .setkWeight(kWeight)
      .setTimeUncertainty(TIME_UNCERTAINTY);

    if (shouldFail) {
      assertThrows(IllegalStateException.class, testEllipseBuilder::build);
    } else {
      assertDoesNotThrow(testEllipseBuilder::build);
    }
  }

  @Test
  void testSerialization() {
    TestUtilities.assertSerializes(ELLIPSOID, Ellipsoid.class);
  }
}