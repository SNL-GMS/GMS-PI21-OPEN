package gms.core.performancemonitoring.soh.control.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;

import java.util.Map;
import java.util.Set;


/**
 * Contains configuration values for a {@link gms.shared.frameworks.osd.coi.signaldetection.Station}'s
 * channelsByMonitorType configuration value.
 */
@AutoValue
public abstract class ChannelsByMonitorType {


  public abstract Map<SohMonitorType, Set<String>> getChannelsByMonitorType();


  @JsonCreator
  public static ChannelsByMonitorType create(
    @JsonProperty("channelsByMonitorType") Map<SohMonitorType, Set<String>> channelsByMonitorType) {

    return new AutoValue_ChannelsByMonitorType(channelsByMonitorType);
  }
}
