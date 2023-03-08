package gms.shared.stationdefinition.dao.css.enums;

public enum TagName {
  ARID("arid"),
  EVID("evid"),
  ORID("orid"),
  STASSID("stassid"),
  MSGID("msig"),
  CLUSTAID("clustaid"),
  UNKNOWN("-");

  private final String name;

  TagName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return this.name();
  }
}
