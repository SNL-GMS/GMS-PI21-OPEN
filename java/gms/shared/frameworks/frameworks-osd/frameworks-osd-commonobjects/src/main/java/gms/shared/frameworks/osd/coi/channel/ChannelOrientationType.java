package gms.shared.frameworks.osd.coi.channel;

/**
 * Represents the SEED / FDSN standard Channel Orientations.  Each orientation has a corresponding
 * single letter code.
 */
public enum ChannelOrientationType {

  UNKNOWN('-'),
  WILD_CARD('*'),

  // Seismometer, Rotational Sensor, or Derived/Generated Orientations.
  // These correspond to instrument codes H, L, G, M, N, J, and X.
  VERTICAL('Z'),
  NORTH_SOUTH('N'),
  EAST_WEST('E'),
  TRIAXIAL_A('A'),
  TRIAXIAL_B('B'),
  TRIAXIAL_C('C'),
  TRANSVERSE('T'),
  RADIAL('R'),
  ORTHOGONAL_1('1'),
  ORTHOGONAL_2('2'),
  ORTHOGONAL_3('3'),
  OPTIONAL_U('U'),
  OPTIONAL_V('V'),
  OPTIONAL_W('W');

  private final char code;

  ChannelOrientationType(char code) {
    this.code = code;
  }

  /**
   * Obtain the single character code associated with this ChannelOrientationType
   *
   * @return char containing the band code
   */
  public char getCode() {
    return this.code;
  }

  public static ChannelOrientationType fromCode(char code) {
    for (ChannelOrientationType type : values()) {
      if (code == type.code) {
        return type;
      }
    }

    return UNKNOWN;
  }
}
