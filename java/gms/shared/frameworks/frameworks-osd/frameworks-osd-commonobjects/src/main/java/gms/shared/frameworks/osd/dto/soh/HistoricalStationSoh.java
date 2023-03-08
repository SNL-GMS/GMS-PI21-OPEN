package gms.shared.frameworks.osd.dto.soh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.apache.commons.lang3.Validate;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents the results of a historical StationSoh query for one or more SohMonitorTypes computed
 * for a single Station, containing historical values for each Channel in the Station.
 */
@AutoValue
public abstract class HistoricalStationSoh {

  public abstract String getStationName();

  /**
   * Calculation times matching each value entry in each associated {@link
   * HistoricalSohMonitorValues}, in epoch milliseconds
   *
   * @return An array of time values in epoch milliseconds, ordered by time
   */
  public abstract long[] getCalculationTimes();

  /**
   * List of {@link HistoricalSohMonitorValues} associated with a station for the span of related
   * calculation times
   *
   * @return An unordered {@link List} of {@link HistoricalSohMonitorValues}
   */
  public abstract List<HistoricalSohMonitorValues> getMonitorValues();

  @JsonCreator
  public static HistoricalStationSoh create(
    @JsonProperty("stationName") String stationName,
    @JsonProperty("calculationTimes") long[] calculationTimes,
    @JsonProperty("monitorValues") List<HistoricalSohMonitorValues> monitorValues) {

    Validate.notEmpty(stationName);

    monitorValues.forEach(historicalValues ->
      historicalValues.getValuesByType().forEach((key, value) -> checkArgument(calculationTimes.length == value.size(),
        String.format("Monitor type:%s has a array of values of size:%s which is not the same length as the "
          + "calculation times:%s on channel:%s.", value.getType(), value.size(), calculationTimes.length, key))));


    return new AutoValue_HistoricalStationSoh(stationName, calculationTimes, monitorValues);
  }
}
