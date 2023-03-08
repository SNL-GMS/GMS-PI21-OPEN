package gms.shared.signaldetection.coi.types;

import com.google.common.collect.ImmutableMap;

import java.util.List;

/**
 * Enum of first motion types
 */
public enum FirstMotionType {

  COMPRESSION(List.of("c", "u")),
  DILATION(List.of("d", "r")),
  INDETERMINATE(List.of(".", "-"));

  private static final ImmutableMap<String, FirstMotionType> FIRST_MOTION_TYPE_BY_CODE;
  private final List<String> codes;

  FirstMotionType(List<String> codes) {
    this.codes = codes;
  }

  static {
    ImmutableMap.Builder<String, FirstMotionType> builder = ImmutableMap.builder();
    for (FirstMotionType firstMotionType : values()) {
      for (String code : firstMotionType.getCodes()) {
        builder.put(code, firstMotionType);
      }
    }

    FIRST_MOTION_TYPE_BY_CODE = builder.build();
  }

  public List<String> getCodes() {
    return codes;
  }

  public static FirstMotionType fromCode(String code) {
    return FIRST_MOTION_TYPE_BY_CODE.get(code);
  }
}
