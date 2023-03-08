package gms.shared.utilities.bridge.database.enums;

/**
 * Id Tag - string that sets the type of id associated with tables wftag and lastid.
 * This enum can grow, as we add additional items that are being implemented.
 */
public enum IdTag {
  ARID("arid"),
  ORID("orid"),
  EVID("evid");

  private final String name;

  IdTag(String name) {
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
