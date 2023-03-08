package gms.shared.stationdefinition.coi.channel;

/**
 * Represents the SEED / FDSN standard Channel Instruments.  Each instrument has a corresponding
 * single letter code.
 */
public enum ChannelInstrumentType {
  UNKNOWN('-'),
  HIGH_GAIN_SEISMOMETER('H'),
  LOW_GAIN_SEISMOMETER('L'),
  GRAVIMETER('G'),
  MASS_POSITION_SEISMOMETER('M'),
  ACCELEROMETER('N'), // Historic channels might use L or G for accelerometers
  ROTATIONAL_SENSOR('J'),
  TILT_METER('A'),
  CREEP_METER('B'),
  CALIBRATION_INPUT('C'),
  PRESSURE('D'),
  ELECTRONIC_TEST_POINT('E'),
  MAGNETOMETER('F'),
  HUMIDITY('I'),
  TEMPERATURE('K'),
  WATER_CURRENT('O'),
  GEOPHONE('P'),
  ELECTRIC_POTENTIAL('Q'),
  RAINFALL('R'),
  LINEAR_STRAIN('S'),
  TIDE('T'),
  BOLOMETER('U'),
  VOLUMETRIC_STRAIN('V'),
  WIND('W'),
  NON_SPECIFIC_INSTRUMENT('Y'),
  DERIVED('X'),
  SYNTHESIZED_BEAM('Z');

  private final char code;

  ChannelInstrumentType(char code) {
    this.code = code;
  }

  /**
   * Obtain the single character code associated with this ChannelInstrumentType
   *
   * @return char containing the instrument code
   */
  public char getCode() {
    return this.code;
  }

  public static ChannelInstrumentType fromCode(char code) {
    for (ChannelInstrumentType type : values()) {
      if (code == type.code) {
        return type;
      }
    }

    return UNKNOWN;
  }
}
