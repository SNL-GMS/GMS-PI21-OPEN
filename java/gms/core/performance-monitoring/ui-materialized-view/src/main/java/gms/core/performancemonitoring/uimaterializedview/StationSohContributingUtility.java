package gms.core.performancemonitoring.uimaterializedview;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import gms.core.performancemonitoring.soh.control.configuration.ChannelSohDefinition;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringUiClientParameters;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;

import java.util.HashMap;
import java.util.Map;

public class StationSohContributingUtility {

  // Make it a singleton
  // static variable single_instance of type Singleton
  private static StationSohContributingUtility instance = null;

  // Map of Maps concerning if channel/monitor are contributing to the Station's rollup status
  private static final Map<String, Table<String, SohMonitorType, Boolean>>
    stationContributingMaps = new HashMap<>();
  private boolean mapIsInitialized = false;

  // Singleton hide the default constructor
  private StationSohContributingUtility() {
  }

  // static method to create instance of class
  public static StationSohContributingUtility getInstance() {
    if (instance == null)
      instance = new StationSohContributingUtility();

    return instance;
  }

  /**
   * Initialize stationContributingMaps from the StationSohDefinitions in the stationSohParameters
   *
   * @param stationSohParameters
   */
  public void initialize(StationSohMonitoringUiClientParameters stationSohParameters) {
    // Build the map of maps which is a map of contributing channel/monitor types for each station
    stationSohParameters.getStationSohControlConfiguration()
      .getStationSohDefinitions().forEach(stationSohDefinition -> {
        // Add the station map to the stationContributingMaps map
        // the new table entries for the channel/monitor type and isContributing boolean value of true
        Table<String, SohMonitorType, Boolean> stationContributingTable = HashBasedTable.create();

        stationContributingMaps.put(stationSohDefinition.getStationName(), stationContributingTable);

        // Build a map of channel/monitor type key with a contributing flag
        // from which isContributing will be populated
        for (ChannelSohDefinition channelSohDefinition : stationSohDefinition.getChannelSohDefinitions()) {
          channelSohDefinition.getSohMonitorTypesForRollup().forEach(sohMonitorType ->
            stationContributingTable.put(channelSohDefinition.getChannelName(), sohMonitorType, true));
        }
      });
    mapIsInitialized = true;
  }

  /**
   * Look up in the static map if for the station the channel and monitor type were part of the rollup
   *
   * @param stationName
   * @param channelName
   * @param type
   * @return boolean isContributing to station rollup status
   * @throws IllegalStateException if the stationContributingMaps data structure is null
   */
  public boolean isChannelMonitorContributing(
    String stationName, String channelName, SohMonitorType type) {

    // Throw an illegal state exception if the map has not yet been initialized
    if (!mapIsInitialized) {
      throw new IllegalStateException("StationSohConfigUtility has not yet been initialized.");
    }
    // Should not happen, the map should have all stations created unless a station has no
    // channel/monitors that rollup
    if (!stationContributingMaps.containsKey(stationName)) {
      return false;
    }

    // Check if the table entry exists returning contents (always set to true) or false if not found
    Table<String, SohMonitorType, Boolean> table = stationContributingMaps.get(stationName);
    return table.contains(channelName, type);
  }
}
