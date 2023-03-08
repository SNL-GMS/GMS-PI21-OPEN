package gms.shared.stationdefinition.coi.channel;

import java.util.Arrays;
import java.util.List;

/**
 * Represents the SEED / FDSN standard Channel Orientations.  Each orientation has a corresponding
 * single letter code.
 */
public enum ChannelOrientationType {

  // TODO: add orientations for other instrument types

  UNKNOWN('-', List.of('H', 'L', 'G', 'M', 'N', 'J', 'X', 'P', 'D')),

  // Seismometer, Rotational Sensor, or Derived/Generated Orientations.
  // These correspond to instrument codes H, L, G, M, N, J, and X.
  // These correspond to instrument codes H, L, G, M, N, J, X, and P.
  VERTICAL('Z', List.of('H', 'L', 'G', 'M', 'N', 'J', 'X', 'P')),
  NORTH_SOUTH('N', List.of('H', 'L', 'G', 'M', 'N', 'J', 'X', 'P')),
  EAST_WEST('E', List.of('H', 'L', 'G', 'M', 'N', 'J', 'X', 'P')),
  // These correspond to instrument codes H, L, G, M, N, J, and X.
  TRIAXIAL_A('A', List.of('H', 'L', 'G', 'M', 'N', 'J', 'X')),
  TRIAXIAL_B('B', List.of('H', 'L', 'G', 'M', 'N', 'J', 'X')),
  TRIAXIAL_C('C', List.of('H', 'L', 'G', 'M', 'N', 'J', 'X')),
  TRANSVERSE('T', List.of('H', 'L', 'G', 'M', 'N', 'J', 'X')),
  RADIAL('R', List.of('H', 'L', 'G', 'M', 'N', 'J', 'X')),
  ORTHOGONAL_1('1', List.of('H', 'L', 'G', 'M', 'N', 'J', 'X')),
  ORTHOGONAL_2('2', List.of('H', 'L', 'G', 'M', 'N', 'J', 'X')),
  ORTHOGONAL_3('3', List.of('H', 'L', 'G', 'M', 'N', 'J', 'X')),
  OPTIONAL_U('U', List.of('H', 'L', 'G', 'M', 'N', 'J', 'X')),
  OPTIONAL_V('V', List.of('H', 'L', 'G', 'M', 'N', 'J', 'X')),

  // These correspond to instrument code(s) D and K.
  OUTSIDE('O', List.of('D', 'K')),
  INSIDE('I', List.of('D', 'K')),
  DOWN_HOLE('D', List.of('D', 'K')),
  INFRASOUND('F', List.of('D')),
  HYDROPHONE('H', List.of('D')),
  UNDERGROUND('U', List.of('D')),
  CABINET_SOURCES_1('1', List.of('K')),
  CABINET_SOURCES_2('2', List.of('K')),
  CABINET_SOURCES_3('3', List.of('K')),
  CABINET_SOURCES_4('4', List.of('K')),

  //These correspond to instrument code W
  WIND_DIRECTION_VECTOR('D', List.of('W')),
  WIND_SPEED('S', List.of('W'));


  private final char code;
  private final List<Character> instrumentCodes;

  ChannelOrientationType(char code, List<Character> instrumentCodes) {
    this.code = code;
    this.instrumentCodes = instrumentCodes;
  }

  /**
   * Obtain the single character code associated with this ChannelOrientationType
   *
   * @return char containing the band code
   */
  public char getCode() {
    return this.code;
  }

  public List<Character> getInstrumentCodes() {
    return instrumentCodes;
  }

  //TODO with addition of codes that are identical between different instrument codes, but have different meanings, do we want this method - dbc 4/15/2021
  public static ChannelOrientationType fromCode(char code) {
    return Arrays.stream(values())
      .filter(t -> t.getCode() == Character.toUpperCase(code))
      .findFirst()
      .orElse(UNKNOWN);
  }

  public static ChannelOrientationType fromCode(char code, char instrumentCode) {
    return Arrays.stream(values())
      .filter(t -> t.getInstrumentCodes().contains(instrumentCode) && t.getCode() == code)
      .findFirst()
      .orElse(UNKNOWN);
  }
}
