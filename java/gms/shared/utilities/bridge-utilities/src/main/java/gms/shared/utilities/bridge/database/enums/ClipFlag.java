package gms.shared.utilities.bridge.database.enums;


/**
 * Clipped data flag. This value is a single-character flag to indicate whether (c) or not (n) the
 * data was clipped
 */
public enum ClipFlag {

  NA("-"),
  CLIPPED("c"),
  NOT_CLIPPED("n");

  private final String name;

  ClipFlag(String name) {
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
