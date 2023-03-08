package gms.dataacquisition.css.stationrefconverter;

import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.coi.channel.ChannelDataType;

class UnitsUtility {

  /**
   * Default private constructor to hide implicit public constructor
   */
  private UnitsUtility() {
  }

  /**
   * Given a reference channel, return the correct units for that channel's data type
   * (e.g. seimsic units are counts/nm).
   *
   * @param referenceChannelDataType data type for channel whose units are returned
   * @return units for specified channel
   */
  static Units determineUnits(ChannelDataType referenceChannelDataType) {
    Units units;
    switch (referenceChannelDataType) {
      case SEISMIC:
        units = Units.COUNTS_PER_NANOMETER;
        break;
      case HYDROACOUSTIC:
      case INFRASOUND:
        units = Units.COUNTS_PER_PASCAL;
        break;
      default:
        units = Units.UNITLESS;
    }
    return units;
  }
}
