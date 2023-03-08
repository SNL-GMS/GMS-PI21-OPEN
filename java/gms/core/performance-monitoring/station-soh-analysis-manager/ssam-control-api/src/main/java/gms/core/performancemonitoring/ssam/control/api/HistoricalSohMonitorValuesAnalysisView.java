package gms.core.performancemonitoring.ssam.control.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.dto.soh.SohMonitorValues;

/**
 * Represents the HistoricalSohMonitorValues that are potentially decimated via the SSAM control decimation
 * algorithm
 */
@AutoValue
public abstract class HistoricalSohMonitorValuesAnalysisView {

  /**
   * The channel name
   *
   * @return a String of the channel name
   */
  public abstract String getChannelName();

  /**
   * The SohMonitorValue
   *
   * @return a SohMonitorValues object
   */
  public abstract SohMonitorValues getValues();

  /**
   * The average of the SohMonitorValues
   *
   * @return a Double value for the average
   */
  public abstract double getAverage();

  @JsonCreator
  public static HistoricalSohMonitorValuesAnalysisView create(
    @JsonProperty("channelName") String channelName,
    @JsonProperty("values") SohMonitorValues values,
    @JsonProperty("average") double average) {

    return new AutoValue_HistoricalSohMonitorValuesAnalysisView(channelName, values, average);
  }
}
