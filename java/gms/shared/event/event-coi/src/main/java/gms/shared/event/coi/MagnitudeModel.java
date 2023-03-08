package gms.shared.event.coi;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;

import java.util.Arrays;

public enum MagnitudeModel {
  NUTTLI("Nuttli"),
  P_FACTOR("P Factor"),
  REZAPOUR_PEARCE("Reazpour-Pearce"),
  RICHTER("Richter"),
  UNKNOWN("Unknown"),
  VEITH_CLAWSON("VeithClawson72");

  private static final ImmutableMap<String, MagnitudeModel> flagsByModel;

  static {
    flagsByModel = Arrays.stream(values())
      .collect(ImmutableMap.toImmutableMap(MagnitudeModel::getEarthModel, Functions.identity()));
  }

  private final String earthModel;


  MagnitudeModel(String earthModel) {
    this.earthModel = earthModel;
  }

  public static MagnitudeModel fromString(String earthModel) {
    if (earthModel == null) {
      return UNKNOWN;
    } else {
      return flagsByModel.get(earthModel);
    }
  }

  public String getEarthModel() {
    return earthModel;
  }

}
