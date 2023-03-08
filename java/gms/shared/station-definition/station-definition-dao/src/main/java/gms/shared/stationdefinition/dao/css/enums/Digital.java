package gms.shared.stationdefinition.dao.css.enums;

public enum Digital {

  ANALOG("a"),
  DIGITAL("d");

  private String name;

  Digital(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
