package gms.shared.stationdefinition.dao.css.enums;


/**
 * Numeric data storage. This column specifies the format of the data series in the file system.
 * Data types: i4, f4 and s4 are typical values. Datatype i4 denotes a 4-byte integer and f4 denotes
 * a 32-bit real number. Datatype s4 is an integer where the most significant byte is in the low
 * address position in memory and is opposite to the i4 order. Machine-dependent formats are
 * supported for common hardware to allow data transfer in native machine binary formats. American
 * Standard Code for Information Interchange (ASCII) formats have also been defined to retain full
 * precision of any binary data type. ASCII may be used when exchanging data between computer
 * systems with incompatible binary types. Datatype can also describe single values or arrays of one
 * data type.
 */
public enum DataType {
  NA("-"),
  A0("a0"),
  B0("b0"),
  C0("c0"),
  AN("a#"),
  BN("b#"),
  CN("c#"),
  T4("t4"),
  T8("t8"),
  S4("s4"),
  S2("s2"),
  S3("s3"),
  F4("f4"),
  F8("f8"),
  I4("i4"),
  I2("i2"),
  E1("e1"),
  EN("e#"),
  G2("g2");

  private final String name;

  DataType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return this.getName();
  }
}
