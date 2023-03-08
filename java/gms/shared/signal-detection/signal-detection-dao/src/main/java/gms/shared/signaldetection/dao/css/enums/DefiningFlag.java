package gms.shared.signaldetection.dao.css.enums;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;

import java.util.Arrays;

public enum DefiningFlag {

  DEFAULT_NON_DEFINING("n"),
  DEFAULT_DEFINING("d"),
  OVERRIDDEN_NON_DEFINING("N"),
  OVERRIDDEN_DEFINING("D"),
  NON_OVERRIDABLE_NON_DEFINING("X"),
  OVERRIDABLE_DEFINING("x"),
  NA("-");

  private static final ImmutableMap<String, DefiningFlag> flagsByCode;

  static {
    flagsByCode = Arrays.stream(values())
      .collect(ImmutableMap.toImmutableMap(DefiningFlag::getCode, Functions.identity()));
  }

  private final String code;

  DefiningFlag(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public static DefiningFlag fromString(String code) {
    if (code == null) {
      return NA;
    } else {
      return flagsByCode.get(code);
    }
  }

  public static boolean isDefining(DefiningFlag definingFlag) {
    return (DEFAULT_DEFINING.equals(definingFlag) || OVERRIDDEN_DEFINING.equals(definingFlag) ||
      OVERRIDABLE_DEFINING.equals(definingFlag));
  }
}
