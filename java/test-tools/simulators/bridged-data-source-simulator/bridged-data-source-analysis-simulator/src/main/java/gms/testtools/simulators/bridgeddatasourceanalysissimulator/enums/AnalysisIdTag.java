package gms.testtools.simulators.bridgeddatasourceanalysissimulator.enums;

/**
 * Analyis Id Tag - string that sets the type of id associated with tables related to analysis data simulator.
 * This enum can grow, as we add additional items that are being implemented.
 */
public enum AnalysisIdTag {
  AMPID("ampid"),
  ARID("arid"),
  ORID("orid"),
  PARID("parid"),
  EVID("evid"),
  WFID("wfid"),
  MBID("mbid"),
  MSID("msid"),
  MLID("mlid");

  private final String name;

  AnalysisIdTag(String name) {
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
