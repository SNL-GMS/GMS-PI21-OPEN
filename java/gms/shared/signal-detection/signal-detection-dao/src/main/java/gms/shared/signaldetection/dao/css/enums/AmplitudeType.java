package gms.shared.signaldetection.dao.css.enums;

//since there are no constraints on AmplitudeType, these enums can't be used inside AmplitudeDao
// these enums can be used to check the type of an amplitude dao
public enum AmplitudeType {

  AMPLITUDE_A5_OVER_2("A5/2");

  private final String name;

  AmplitudeType(String type) {
    this.name = type;
  }

  public String getName() {
    return name;
  }

}
