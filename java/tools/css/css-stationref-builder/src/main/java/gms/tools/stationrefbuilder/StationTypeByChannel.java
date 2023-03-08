package gms.tools.stationrefbuilder;

import java.util.Arrays;

public enum StationTypeByChannel {
  SEISMIC(
    "BH1",
    "BH2",
    "BHE",
    "BHN",
    "BHZ",
    "BLE",
    "BLN",
    "BLZ",
    "BNE",
    "BNN",
    "BNZ",
    "EHE",
    "EHN",
    "EHZ",
    "EN1",
    "EN2",
    "ENE",
    "ENN",
    "ENZ",
    "HH1",
    "HH2",
    "HHE",
    "HHN",
    "HHZ",
    "HLE",
    "HLN",
    "HLZ",
    "HN1",
    "HN2",
    "HNE",
    "HNN",
    "HNZ",
    "LH1",
    "LH2",
    "LHE",
    "LHN",
    "LHZ",
    "LLE",
    "LLN",
    "LLZ",
    "LN1",
    "LN2",
    "LNZ",
    "MH1",
    "MH2",
    "MHE",
    "MHN",
    "MHZ",
    "SH1",
    "SH2",
    "SHE",
    "SHN",
    "SHZ",
    "SLE",
    "SLN",
    "SLZ",
    "VH1",
    "VH2",
    "VHE",
    "VHN",
    "VHZ",
    "VM1",
    "VM2",
    "VMU",
    "VMV",
    "VMW",
    "VMZ",
    "VY1",
    "VY2",
    "VYZ"),
  HYDRO("EDH",
    "EHE",
    "EHN",
    "EHZ",
    "HHE",
    "HHN",
    "HHZ",
    "LEA",
    "LEV"
  ),
  INFRASOUND("BDF", "LDF"),
  WEATHER(
    "LDA",
    "LDO",
    "LEA",
    "LEV",
    "LH1",
    "LH2",
    "LHE",
    "LHN",
    "LHZ",
    "LIO",
    "LKO",
    "LLE",
    "LLN",
    "LLZ",
    "LN1",
    "LN2",
    "LNZ",
    "LWD",
    "LWS"
  );

  private final String[] values;

  StationTypeByChannel(String... values) {
    this.values = values;
  }

  public static String[] getValuesByEnum(StationTypeByChannel stationType) {
    return stationType.values;
  }

  public static StationTypeByChannel getEnumByValue(String value) {
    // default to SEISMIC for now
    return Arrays.stream(StationTypeByChannel.values()).filter(stationType ->
      Arrays.asList(stationType.values).contains(value)).findFirst().orElse(SEISMIC);
  }

}

