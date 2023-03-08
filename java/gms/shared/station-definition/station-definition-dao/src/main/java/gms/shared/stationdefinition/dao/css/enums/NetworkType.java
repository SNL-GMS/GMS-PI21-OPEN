package gms.shared.stationdefinition.dao.css.enums;

public enum NetworkType {

  WORLD_WIDE("ww");

  private final String name;

  private NetworkType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }
}
