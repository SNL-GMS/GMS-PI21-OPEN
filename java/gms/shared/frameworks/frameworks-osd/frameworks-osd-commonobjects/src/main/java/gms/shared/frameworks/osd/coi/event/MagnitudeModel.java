package gms.shared.frameworks.osd.coi.event;

/**
 * @deprecated As of PI 17.5, the current model of this COI has been migrated into the event-coi package.
 * All usage of this COI outside the Frameworks area should be avoided and the alternative in event-coi used instead
 */
@Deprecated(since = "17.5", forRemoval = true)
public enum MagnitudeModel {
  RICHTER("Richter"),
  VEITH_CLAWSON("VeithClawson72"),
  REZAPOUR_PEARCE("Reazpour-Pearce"),
  NUTTLI("Nuttli"),
  UNKNOWN("Unknown");

  private final String earthModel;

  MagnitudeModel(String earthModel) {
    this.earthModel = earthModel;
  }

  public String getEarthModel() {
    return earthModel;
  }

}
