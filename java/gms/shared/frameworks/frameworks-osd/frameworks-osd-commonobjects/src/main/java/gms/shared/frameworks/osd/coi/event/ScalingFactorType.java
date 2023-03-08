package gms.shared.frameworks.osd.coi.event;

/**
 * Defines types for {@link Ellipse} and {@link Ellipsoid}
 *
 * @deprecated As of PI 17.5, the current model of this COI has been migrated into the event-coi package.
 * All usage of this COI outside the Frameworks area should be avoided and the alternative in event-coi used instead
 */
@Deprecated(since = "17.5", forRemoval = true)
public enum ScalingFactorType {
  CONFIDENCE,
  COVERAGE,
  K_WEIGHTED
}
