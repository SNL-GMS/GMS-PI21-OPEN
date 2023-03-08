package gms.shared.signaldetection.dao.css.enums;

import com.google.common.collect.ImmutableMap;

import java.util.List;

public enum SignalType {

  NA(List.of('-')),
  LOCAL_EVENT(List.of('l')),
  REGIONAL_EVENT(List.of('r')),
  TELESEISMID_EVENT(List.of('t')),
  MIXED_EVENT(List.of('m')),
  GLITCH(List.of('g')),
  CALIBRATION_ACTIVITY_OBFUSCATED(List.of('e'));

  private static final ImmutableMap<Character, SignalType> SIGNAL_TYPE_BY_CODE;
  private final List<Character> codes;

  SignalType(List<Character> codes) {
    this.codes = codes;
  }

  static {
    ImmutableMap.Builder<Character, SignalType> builder = ImmutableMap.builder();
    for (SignalType type : values()) {
      for (Character code : type.getCodes()) {
        builder.put(code, type);
      }
    }

    SIGNAL_TYPE_BY_CODE = builder.build();
  }

  public List<Character> getCodes() {
    return codes;
  }

  public static SignalType fromCode(char code) {
    return SIGNAL_TYPE_BY_CODE.get(code);
  }
}
