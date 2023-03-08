package gms.shared.frameworks.osd.dto.soh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A grouping of {@link SohMonitorValues} for a specific Channel over a span of time
 */
@AutoValue
public abstract class HistoricalSohMonitorValues {

  public abstract String getChannelName();

  public abstract ImmutableMap<SohMonitorType, SohMonitorValues> getValuesByType();

  @JsonCreator
  public static HistoricalSohMonitorValues create(
    @JsonProperty("channelName") String channelName,
    @JsonProperty("valuesByType") Map<SohMonitorType, SohMonitorValues> values) {

    values.forEach(
      (type, sohValues) -> checkArgument(type.getSohValueType().equals(sohValues.getType()),
        "The value type of every SohMonitorValue contained within a "
          + "HistoricalSohMonitorValues must match the HistoricalSohMonitorValues' "
          + "SohMonitorType's value type"));

    return new AutoValue_HistoricalSohMonitorValues(channelName, ImmutableMap.copyOf(values));
  }
}
