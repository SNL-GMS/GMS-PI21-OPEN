package gms.shared.stationdefinition.dao.css.enums;

public enum StaType {
  SINGLE_STATION("ss"),
  ARRAY_STATION("ar"),
  UNKNOWN("-");

  private String name;

  StaType(String name) {
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
