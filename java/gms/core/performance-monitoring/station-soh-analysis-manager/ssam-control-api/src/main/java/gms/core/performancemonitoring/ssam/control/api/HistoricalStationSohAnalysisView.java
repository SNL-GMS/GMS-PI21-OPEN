package gms.core.performancemonitoring.ssam.control.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.apache.commons.lang3.Validate;

import java.util.List;

/**
 * Represents the results of a SSAM control decimation endpoint query.  This data will be returned
 * directly to the UI to be displayed after such a request is made.
 */
@AutoValue
public abstract class HistoricalStationSohAnalysisView {

  /**
   * The station name
   *
   * @return A String representing the station name
   */
  public abstract String getStationName();

  /**
   * Calculation times matching each value entry in each associated {@link
   * gms.shared.frameworks.osd.dto.soh.HistoricalSohMonitorValues}, in epoch milliseconds
   *
   * @return An array of time values in epoch milliseconds, ordered by time
   */
  public abstract long[] getCalculationTimes();

  /**
   * List of {@link gms.shared.frameworks.osd.dto.soh.HistoricalSohMonitorValues} associated with a station for the span of related
   * calculation times
   *
   * @return An unordered {@link List} of {@link gms.shared.frameworks.osd.dto.soh.HistoricalSohMonitorValues}
   */
  public abstract List<HistoricalSohMonitorValuesAnalysisView> getMonitorValues();

  /**
   * The percentage of data points actually sent back after decimation runs
   *
   * @return A double value representing the percent of data sent back
   */
  public abstract double getPercentageSent();

  @JsonCreator
  public static HistoricalStationSohAnalysisView create(
    @JsonProperty("stationName") String stationName,
    @JsonProperty("calculationTimes") long[] calculationTimes,
    @JsonProperty("monitorValues") List<HistoricalSohMonitorValuesAnalysisView> monitorValues,
    @JsonProperty("percentageSent") double percentageSent) {

    Validate.notEmpty(stationName);
    Validate.inclusiveBetween(0, 100, percentageSent);

    monitorValues.forEach(
      historicalSohMonitorValuesAnalysisView -> Validate.isTrue(
        historicalSohMonitorValuesAnalysisView.getValues().size() == calculationTimes.length,
        "Size of historical array for channel " + historicalSohMonitorValuesAnalysisView.getChannelName()
          + "does not match the calculationTimes array"
      )
    );

    return new AutoValue_HistoricalStationSohAnalysisView(stationName, calculationTimes,
      monitorValues,
      percentageSent
    );
  }

}
