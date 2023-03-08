package gms.shared.frameworks.osd.coi.event;

/**
 * Define an enumeration of restraint types for location solutions.
 *
 * @deprecated As of PI 17.5, the current model of this COI has been migrated into the event-coi package.
 * All usage of this COI outside the Frameworks area should be avoided and the alternative in event-coi used instead
 */
@Deprecated(since = "17.5", forRemoval = true)
public enum RestraintType {
  UNRESTRAINED,
  FIXED
}
