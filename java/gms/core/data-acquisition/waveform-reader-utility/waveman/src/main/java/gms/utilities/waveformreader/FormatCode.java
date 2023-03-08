package gms.utilities.waveformreader;

public enum FormatCode {
  S4("s4"),
  S3("s3"),
  S2("s2"),
  I4("i4"),
  I2("i2"),
  CD("cd"),
  CC("cc"),
  F4("f4"),
  E1("e1"),
  CM6("cm6"),
  T4("t4");

  private String code;

  FormatCode(String code) {
    this.code = code;
  }

  protected String getCode() {
    return code;
  }

  public static FormatCode fcFromString(String s) {
    for (FormatCode fc : FormatCode.values()) {
      if (fc.code.equalsIgnoreCase(s)) {
        return fc;
      }
    }
    return null;
  }
}
