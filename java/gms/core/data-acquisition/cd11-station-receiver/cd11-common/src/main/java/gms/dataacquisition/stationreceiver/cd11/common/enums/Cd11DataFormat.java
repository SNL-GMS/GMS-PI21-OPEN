package gms.dataacquisition.stationreceiver.cd11.common.enums;

import java.util.Arrays;

public enum Cd11DataFormat {

  S4("s4", (byte) 4),
  S3("s3", (byte) 3),
  S2("s2", (byte) 2),
  I4("i4", (byte) 4),
  I2("i2", (byte) 2),
  CD("CD", (byte) -1), // Per CD1.1 Spec, size is not applicable to CD-1 Encapsulated Data
  CA("ca", (byte) -1); // This is a compressed data format, so size is not applicable

  private final String dataFormatName;
  public final byte size;

  Cd11DataFormat(String dataFormatName, byte size) {
    this.dataFormatName = dataFormatName;
    this.size = size;
  }

  public static Cd11DataFormat fromString(String s) {
    return Arrays.stream(Cd11DataFormat.values())
      .filter(v -> v.dataFormatName.equalsIgnoreCase(s))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException(String.format("Unsupported CD1.1 Data Format: %s", s)));
  }

  @Override
  public String toString() {
    return dataFormatName;
  }

  public byte[] toBytes() {
    return this.toString().getBytes();
  }
}
