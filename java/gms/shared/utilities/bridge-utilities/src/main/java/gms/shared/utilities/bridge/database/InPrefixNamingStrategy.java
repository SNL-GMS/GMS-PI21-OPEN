package gms.shared.utilities.bridge.database;

public class InPrefixNamingStrategy extends PrefixNamingStrategy {
  static final String IN_PREFIX = "IN_";

  public InPrefixNamingStrategy() {
    super(IN_PREFIX);
  }
}
