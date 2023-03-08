package gms.dataacquisition.stationreceiver.cd11.datamanipulator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;

@JsonSerialize(as = ReadStationDataFrame.class)
class ReadStationDataFrame {

  public Cd11DataFrameSoh cd11DataFrameSoh; // NOSONAR needs to be public to serialize correctly

  public RawStationDataFrame rsdf; // NOSONAR needs to be public to serialize correctly

  @JsonCreator
  ReadStationDataFrame(@JsonProperty("cd11DataFrameSoh") Cd11DataFrameSoh cd11DataFrameSoh,
    @JsonProperty("rsdf") RawStationDataFrame rsdf) {
    this.cd11DataFrameSoh = cd11DataFrameSoh;
    this.rsdf = rsdf;
  }

  public Cd11DataFrameSoh getCd11DataFrameSoh() {
    return cd11DataFrameSoh;
  }

  public RawStationDataFrame getRsdf() {
    return rsdf;
  }
}
