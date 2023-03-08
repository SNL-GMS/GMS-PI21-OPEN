package gms.shared.event.coi;

import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocationRestraintTests {

  private static final LocationRestraint DEFAULT_UNRESTRAINED = LocationRestraint.free();

  @ParameterizedTest
  @MethodSource("locationRestraintValidationProvider")
  void testLocationRestraintValidation(RestraintType depthRestraintType,
    Optional<DepthRestraintReason> depthRestraintReason, Optional<Double> depthRestraintKm,
    RestraintType positionRestraintType, Optional<Double> latitudeRestraintDegrees,
    Optional<Double> longitudeRestraintDegrees, RestraintType timeRestraintType, Optional<Instant> timeRestraint,
    boolean shouldFail) {

    var locationRestraintBuilder = LocationRestraint.builder()
      .setDepthRestraintType(depthRestraintType)
      .setDepthRestraintReason(depthRestraintReason)
      .setDepthRestraintKm(depthRestraintKm)
      .setPositionRestraintType(positionRestraintType)
      .setLatitudeRestraintDegrees(latitudeRestraintDegrees)
      .setLongitudeRestraintDegrees(longitudeRestraintDegrees)
      .setTimeRestraintType(timeRestraintType)
      .setTimeRestraint(timeRestraint);

    if (shouldFail) {
      assertThrows(IllegalStateException.class, locationRestraintBuilder::build);
    } else {
      assertDoesNotThrow(locationRestraintBuilder::build);
    }
  }

  private static Stream<Arguments> locationRestraintValidationProvider() {
    return Stream.of(
      // Depth Restraint
      Arguments.arguments(RestraintType.FIXED, Optional.of(DepthRestraintReason.FIXED_AT_SURFACE), Optional.of(1.0),
        DEFAULT_UNRESTRAINED.getPositionRestraintType(), Optional.empty(), Optional.empty(),
        DEFAULT_UNRESTRAINED.getTimeRestraintType(), Optional.empty(), false),
      Arguments.arguments(RestraintType.FIXED, Optional.empty(), Optional.of(1.0),
        DEFAULT_UNRESTRAINED.getPositionRestraintType(), Optional.empty(), Optional.empty(),
        DEFAULT_UNRESTRAINED.getTimeRestraintType(), Optional.empty(), true),
      Arguments.arguments(RestraintType.FIXED, Optional.of(DepthRestraintReason.FIXED_AT_SURFACE), Optional.empty(),
        DEFAULT_UNRESTRAINED.getPositionRestraintType(), Optional.empty(), Optional.empty(),
        DEFAULT_UNRESTRAINED.getTimeRestraintType(), Optional.empty(), true),
      Arguments.arguments(RestraintType.UNRESTRAINED, Optional.empty(), Optional.empty(),
        DEFAULT_UNRESTRAINED.getPositionRestraintType(), Optional.empty(), Optional.empty(),
        DEFAULT_UNRESTRAINED.getTimeRestraintType(), Optional.empty(), false),
      Arguments.arguments(RestraintType.UNRESTRAINED, Optional.of(DepthRestraintReason.FIXED_AT_SURFACE),
        Optional.empty(), DEFAULT_UNRESTRAINED.getPositionRestraintType(), Optional.empty(), Optional.empty(),
        DEFAULT_UNRESTRAINED.getTimeRestraintType(), Optional.empty(), true),
      Arguments.arguments(RestraintType.UNRESTRAINED, Optional.empty(), Optional.of(1.0),
        DEFAULT_UNRESTRAINED.getPositionRestraintType(), Optional.empty(), Optional.empty(),
        DEFAULT_UNRESTRAINED.getTimeRestraintType(), Optional.empty(), true),

      // Position Restraint
      Arguments.arguments(DEFAULT_UNRESTRAINED.getDepthRestraintType(), Optional.empty(), Optional.empty(),
        RestraintType.FIXED, Optional.of(1.0), Optional.of(1.0), DEFAULT_UNRESTRAINED.getTimeRestraintType(),
        Optional.empty(), false),
      Arguments.arguments(DEFAULT_UNRESTRAINED.getDepthRestraintType(), Optional.empty(), Optional.empty(),
        RestraintType.FIXED, Optional.empty(), Optional.of(1.0), DEFAULT_UNRESTRAINED.getTimeRestraintType(),
        Optional.empty(), true),
      Arguments.arguments(DEFAULT_UNRESTRAINED.getDepthRestraintType(), Optional.empty(), Optional.empty(),
        RestraintType.FIXED, Optional.of(1.0), Optional.empty(), DEFAULT_UNRESTRAINED.getTimeRestraintType(),
        Optional.empty(), true),
      Arguments.arguments(DEFAULT_UNRESTRAINED.getDepthRestraintType(), Optional.empty(), Optional.empty(),
        RestraintType.UNRESTRAINED, Optional.empty(), Optional.empty(), DEFAULT_UNRESTRAINED.getTimeRestraintType(),
        Optional.empty(), false),
      Arguments.arguments(DEFAULT_UNRESTRAINED.getDepthRestraintType(), Optional.empty(), Optional.empty(),
        RestraintType.UNRESTRAINED, Optional.of(1.0), Optional.empty(), DEFAULT_UNRESTRAINED.getTimeRestraintType(),
        Optional.empty(), true),
      Arguments.arguments(DEFAULT_UNRESTRAINED.getDepthRestraintType(), Optional.empty(), Optional.empty(),
        RestraintType.UNRESTRAINED, Optional.empty(), Optional.of(1.0), DEFAULT_UNRESTRAINED.getTimeRestraintType(),
        Optional.empty(), true),

      // Time Restraint
      Arguments.arguments(DEFAULT_UNRESTRAINED.getDepthRestraintType(), Optional.empty(), Optional.empty(),
        DEFAULT_UNRESTRAINED.getPositionRestraintType(), Optional.empty(), Optional.empty(), RestraintType.FIXED,
        Optional.of(Instant.EPOCH), false),
      Arguments.arguments(DEFAULT_UNRESTRAINED.getDepthRestraintType(), Optional.empty(), Optional.empty(),
        DEFAULT_UNRESTRAINED.getPositionRestraintType(), Optional.empty(), Optional.empty(), RestraintType.FIXED,
        Optional.empty(), true),
      Arguments.arguments(DEFAULT_UNRESTRAINED.getDepthRestraintType(), Optional.empty(), Optional.empty(),
        DEFAULT_UNRESTRAINED.getPositionRestraintType(), Optional.empty(), Optional.empty(), RestraintType.UNRESTRAINED,
        Optional.empty(), false),
      Arguments.arguments(DEFAULT_UNRESTRAINED.getDepthRestraintType(), Optional.empty(), Optional.empty(),
        DEFAULT_UNRESTRAINED.getPositionRestraintType(), Optional.empty(), Optional.empty(), RestraintType.UNRESTRAINED,
        Optional.of(Instant.EPOCH), true)
    );
  }

  @Test
  void testBuilderWithDefaults() {
    assertEquals(RestraintType.UNRESTRAINED, DEFAULT_UNRESTRAINED.getDepthRestraintType());
    assertTrue(DEFAULT_UNRESTRAINED.getDepthRestraintReason().isEmpty());
    assertTrue(DEFAULT_UNRESTRAINED.getDepthRestraintKm().isEmpty());

    assertEquals(RestraintType.UNRESTRAINED, DEFAULT_UNRESTRAINED.getPositionRestraintType());
    assertTrue(DEFAULT_UNRESTRAINED.getLatitudeRestraintDegrees().isEmpty());
    assertTrue(DEFAULT_UNRESTRAINED.getLongitudeRestraintDegrees().isEmpty());

    assertEquals(RestraintType.UNRESTRAINED, DEFAULT_UNRESTRAINED.getTimeRestraintType());
    assertTrue(DEFAULT_UNRESTRAINED.getTimeRestraint().isEmpty());
  }

  @Test
  void testSerialization() {
    TestUtilities.assertSerializes(DEFAULT_UNRESTRAINED, LocationRestraint.class);
  }
}