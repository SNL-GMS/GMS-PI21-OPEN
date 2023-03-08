package gms.shared.event.coi;

import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.stream.Stream;

import static gms.shared.event.coi.EventTestFixtures.CONFIDENCE_LEVEL;
import static gms.shared.event.coi.EventTestFixtures.DEPTH_UNCERTAINTY;
import static gms.shared.event.coi.EventTestFixtures.K_WEIGHT;
import static gms.shared.event.coi.EventTestFixtures.MAJOR_AXIS_LENGTH;
import static gms.shared.event.coi.EventTestFixtures.MAJOR_AXIS_TREND;
import static gms.shared.event.coi.EventTestFixtures.MINOR_AXIS_LENGTH;
import static gms.shared.event.coi.EventTestFixtures.TIME_UNCERTAINTY;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EllipseTests {
  private static final ScalingFactorType SCALING_FACTOR_TYPE = ScalingFactorType.CONFIDENCE;
  public static final Ellipse ELLIPSE = Ellipse.builder()
    .setScalingFactorType(SCALING_FACTOR_TYPE)
    .setkWeight(K_WEIGHT)
    .setConfidenceLevel(CONFIDENCE_LEVEL)
    .setSemiMajorAxisLengthKm(MAJOR_AXIS_LENGTH)
    .setSemiMajorAxisTrendDeg(MAJOR_AXIS_TREND)
    .setSemiMinorAxisLengthKm(MINOR_AXIS_LENGTH)
    .setDepthUncertaintyKm(DEPTH_UNCERTAINTY)
    .setTimeUncertainty(TIME_UNCERTAINTY)
    .build();

  @ParameterizedTest
  @MethodSource("ellipseValidationSource")
  void testEllipseValidation(ScalingFactorType scalingFactorType, double confidenceLevel, double kWeight,
    double depthUncertainty, Duration timeUncertainty, boolean shouldFail) {
    var testEllipseBuilder = ELLIPSE.toBuilder()
      .setScalingFactorType(scalingFactorType)
      .setConfidenceLevel(confidenceLevel)
      .setkWeight(kWeight)
      .setDepthUncertaintyKm(depthUncertainty)
      .setTimeUncertainty(timeUncertainty);

    if (shouldFail) {
      assertThrows(IllegalStateException.class, testEllipseBuilder::build);
    } else {
      assertDoesNotThrow(testEllipseBuilder::build);
    }
  }

  private static Stream<Arguments> ellipseValidationSource() {

    return Stream.of(
      // Confidence level within expected bounds [0.5, 1]
      Arguments.arguments(SCALING_FACTOR_TYPE, 0.4, K_WEIGHT, DEPTH_UNCERTAINTY, TIME_UNCERTAINTY, true),
      Arguments.arguments(SCALING_FACTOR_TYPE, 1.1, K_WEIGHT, DEPTH_UNCERTAINTY, TIME_UNCERTAINTY, true),
      Arguments.arguments(SCALING_FACTOR_TYPE, 0.5, K_WEIGHT, DEPTH_UNCERTAINTY, TIME_UNCERTAINTY, false),
      Arguments.arguments(SCALING_FACTOR_TYPE, 0.7, K_WEIGHT, DEPTH_UNCERTAINTY, TIME_UNCERTAINTY, false),
      Arguments.arguments(SCALING_FACTOR_TYPE, 1.0, K_WEIGHT, DEPTH_UNCERTAINTY, TIME_UNCERTAINTY, false),
      // Coverage infinity kWeight
      Arguments.arguments(ScalingFactorType.COVERAGE, CONFIDENCE_LEVEL, K_WEIGHT, DEPTH_UNCERTAINTY, TIME_UNCERTAINTY, true),
      Arguments.arguments(ScalingFactorType.COVERAGE, CONFIDENCE_LEVEL, Double.POSITIVE_INFINITY, DEPTH_UNCERTAINTY, TIME_UNCERTAINTY, false),
      // K-Weighted positive kWeight
      Arguments.arguments(ScalingFactorType.K_WEIGHTED, CONFIDENCE_LEVEL, -1.0, DEPTH_UNCERTAINTY, TIME_UNCERTAINTY, true),
      Arguments.arguments(ScalingFactorType.K_WEIGHTED, CONFIDENCE_LEVEL, 0.0, DEPTH_UNCERTAINTY, TIME_UNCERTAINTY, false),
      Arguments.arguments(ScalingFactorType.K_WEIGHTED, CONFIDENCE_LEVEL, 0.0, DEPTH_UNCERTAINTY, TIME_UNCERTAINTY, false),
      // Confidence kWeight is 0.0
      Arguments.arguments(ScalingFactorType.CONFIDENCE, CONFIDENCE_LEVEL, 0.1, DEPTH_UNCERTAINTY, TIME_UNCERTAINTY, true),
      Arguments.arguments(ScalingFactorType.CONFIDENCE, CONFIDENCE_LEVEL, -0.1, DEPTH_UNCERTAINTY, TIME_UNCERTAINTY, true),
      Arguments.arguments(ScalingFactorType.CONFIDENCE, CONFIDENCE_LEVEL, 0.0, DEPTH_UNCERTAINTY, TIME_UNCERTAINTY, false),
      // Depth uncertainty >= 0.0
      Arguments.arguments(SCALING_FACTOR_TYPE, CONFIDENCE_LEVEL, K_WEIGHT, 0.0, TIME_UNCERTAINTY, false),
      Arguments.arguments(SCALING_FACTOR_TYPE, CONFIDENCE_LEVEL, K_WEIGHT, 0.1, TIME_UNCERTAINTY, false),
      // Non-negative time uncertainty
      Arguments.arguments(SCALING_FACTOR_TYPE, CONFIDENCE_LEVEL, K_WEIGHT, DEPTH_UNCERTAINTY, Duration.ofNanos(1).negated(), true),
      Arguments.arguments(SCALING_FACTOR_TYPE, CONFIDENCE_LEVEL, K_WEIGHT, DEPTH_UNCERTAINTY, Duration.ZERO, false),
      Arguments.arguments(SCALING_FACTOR_TYPE, CONFIDENCE_LEVEL, K_WEIGHT, DEPTH_UNCERTAINTY, Duration.ofNanos(1), false)
    );
  }

  @Test
  void testSerialization() {
    TestUtilities.assertSerializes(ELLIPSE, Ellipse.class);
  }
}