package gms.shared.frameworks.osd.coi.channel;

/**
 * Represents the SEED / FDSN standard Channel Bands.  Each band has a corresponding single letter
 * code.
 */
public enum ChannelBandType {

  UNKNOWN('-'),
  WILD_CARD('*'),

  // Long Period Bands
  MID_PERIOD('M'),                // 1Hz - 10Hz
  LONG_PERIOD('L'),               // ~1Hz
  VERY_LONG_PERIOD('V'),          // ~0.1Hz
  ULTRA_LONG_PERIOD('U'),         // ~0.01Hz
  EXTREMELY_LONG_PERIOD('R'),     // 0.0001Hz - 0.001Hz
  PARTICULARLY_LONG_PERIOD('P'),  // 0.00001Hz - 0.0001Hz (new)
  TREMENDOUSLY_LONG_PERIOD('T'),  // 0.000001Hz - 0.00001Hz (new)
  IMMENSELY_LONG_PERIOD('Q'),     // < 0.000001Hz (new)

  // Short Period Bands
  TREMENDOUSLY_SHORT_PERIOD('G'), // 1000Hz - 5000Hz (new)
  PARTICULARLY_SHORT_PERIOD('D'), // 250Hz - 10000Hz (new)
  EXTREMELY_SHORT_PERIOD('E'),    // 80Hz - 250Hz
  SHORT_PERIOD('S'),              // 10Hz - 80Hz

  // Broadband (Corner Periods > 10 sec)
  ULTRA_HIGH_BROADBAND('F'),      // 1000Hz - 5000Hz (new)
  VERY_HIGH_BROADBAND('C'),       // 250Hz - 1000Hz (new)
  HIGH_BROADBAND('H'),            // 80Hz - 250Hz
  BROADBAND('B'),                 // 10Hz - 80Hz

  ADMINISTRATIVE('A'),
  OPAQUE('O');

  private final char code;

  ChannelBandType(char code) {
    this.code = code;
  }

  /**
   * Obtain the single character code associated with this ChannelBandType
   *
   * @return char containing the band code
   */
  public char getCode() {
    return this.code;
  }

  public static ChannelBandType fromCode(char code) {
    for (ChannelBandType type : values()) {
      if (code == type.code) {
        return type;
      }
    }

    return UNKNOWN;
  }
}
