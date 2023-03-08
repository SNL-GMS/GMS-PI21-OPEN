package gms.shared.stationdefinition.coi.utils;


import gms.shared.stationdefinition.coi.channel.ChannelDataType;

/**
 * An enumeration of units used in any given context
 */
public enum Units {
  DEGREES,
  DECIBELS,
  RADIANS,
  SECONDS,
  HERTZ,
  KILOMETERS,
  KILOMETERS_PER_SECOND,
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
  LOG_NM,
  UNITLESS,
  MAGNITUDE,   // TODO - make this go away.  magnitude is not a unit.
  COUNTS_PER_NANOMETER,
  COUNTS_PER_PASCAL,
  PASCALS_PER_COUNT,
  PASCALS,
  MICROPASCALS;

  /**
   * Given a reference channel, return the correct units for that channel's data type
   * (e.g. seimsic units are counts/nm).
   *
   * @param referenceChannelDataType data type for channel whose units are returned
   * @return units for specified channel
   */
  public static Units determineUnits(ChannelDataType referenceChannelDataType) {
    Units units;
    switch (referenceChannelDataType) {
      case SEISMIC:
        units = Units.NANOMETERS;
        break;
      case HYDROACOUSTIC:
        units = Units.MICROPASCALS;
        break;
      case INFRASOUND:
        units = Units.PASCALS;
        break;
      default:
        units = Units.UNITLESS;
    }
    return units;
  }

}