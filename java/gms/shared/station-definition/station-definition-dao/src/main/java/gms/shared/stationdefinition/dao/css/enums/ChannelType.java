package gms.shared.stationdefinition.dao.css.enums;

public enum ChannelType {
  B("b"),
  N("n"),
  I("i"),
  UNKNOWN("-");

  private final String name;

  ChannelType(String name) {
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
