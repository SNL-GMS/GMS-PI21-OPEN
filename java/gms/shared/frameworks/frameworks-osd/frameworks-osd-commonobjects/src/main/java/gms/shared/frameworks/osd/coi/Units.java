package gms.shared.frameworks.osd.coi;


/**
 * An enumeration of units used in any given context
 */
@Deprecated
public enum Units {
  DEGREES,
  RADIANS,
  SECONDS,
  HERTZ,
  SECONDS_PER_DEGREE,
  SECONDS_PER_RADIAN,
  SECONDS_PER_DEGREE_SQUARED,
  SECONDS_PER_KILOMETER_SQUARED,
  SECONDS_PER_KILOMETER,
  SECONDS_PER_KILOMETER_PER_DEGREE,
  ONE_OVER_KM,
  NANOMETERS,
  NANOMETERS_PER_SECOND,
  NANOMETERS_PER_COUNT,
  UNITLESS,
  MAGNITUDE,   // TODO - make this go away.  magnitude is not a unit.
  COUNTS_PER_NANOMETER,
  COUNTS_PER_PASCAL,
  PASCALS_PER_COUNT
}