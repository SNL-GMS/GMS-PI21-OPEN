package gms.core.performancemonitoring.uimaterializedview;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.soh.StationAggregate;
import gms.shared.frameworks.osd.coi.soh.StationAggregateType;
import gms.shared.frameworks.osd.coi.soh.StationAggregateType.StationValueType;

import java.time.Duration;
import java.util.Optional;

@AutoValue
public abstract class UiStationAggregate {

  /**
   * @return Aggregate value
   */
  public abstract Double getValue();

  /**
   * is value present (was the value set in the StationAggregate)
   */
  public abstract boolean getValuePresent();

  /**
   * @return station aggregate type
   */
  public abstract StationAggregateType getAggregateType();


  @JsonCreator
  public static UiStationAggregate create(
    @JsonProperty("value") Double value,
    @JsonProperty("valuePresent") boolean valuePresent,
    @JsonProperty("aggregateType") StationAggregateType aggregateType
  ) {

    return new AutoValue_UiStationAggregate(
      value,
      valuePresent,
      aggregateType
    );
  }

  public static UiStationAggregate from(StationAggregate aggregate) {
    //
    // Convert the value to a Double. For now the only types defined are Percent and Duration
    // If a value is set and is not an instanceof a know type log a warning.
    //
    Double value = -1.0;

    if (aggregate.getAggregateType().getStationValueType() == StationValueType.PERCENT) {
      value = UiMaterializedViewUtility.setDecimalPrecisionAsNumber(
        (Double) aggregate.getValue().orElse(value), 2);
    } else if (aggregate.getAggregateType().getStationValueType() == StationValueType.DURATION) {
      Optional<Duration> optionalDuration = aggregate.getValue();
      double doubleValue = optionalDuration.isPresent() ?
        optionalDuration.get().toMillis() / 1000.0 : value;
      value = UiMaterializedViewUtility.setDecimalPrecisionAsNumber(doubleValue, 2);
    }
    return new AutoValue_UiStationAggregate(
      value,
      value != -1.0,
      aggregate.getAggregateType());
  }
}
