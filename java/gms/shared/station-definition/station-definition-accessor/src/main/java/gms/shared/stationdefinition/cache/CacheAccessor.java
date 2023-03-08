package gms.shared.stationdefinition.cache;

import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class CacheAccessor {

  private static final Logger logger = LoggerFactory.getLogger(CacheAccessor.class);

  // Operational time period config
  static final String OPERATIONAL_TIME_PERIOD_CONFIG = "global.operational-time-period";
  static final String OPERATIONAL_PERIOD_START = "operationalPeriodStart";
  static final String OPERATIONAL_PERIOD_END = "operationalPeriodEnd";

  // StationGroup names and configuration
  static final String STATION_GROUP_NAMES = "stationGroupNames";
  static final String STATION_GROUP_NAMES_CONFIG = "station-definition-manager.station-group-names";

  private final CachePopulator cachePopulator;
  private final RetryPolicy<Void> retryPolicy;
  private final List<String> stationGroupNames;
  private final Duration operationalStart;
  private final Duration operationalEnd;

  @Autowired
  public CacheAccessor(
    CachePopulator cachePopulator,
    ConfigurationConsumerUtility configurationConsumerUtility,
    @Qualifier("cache-retryPolicy") RetryPolicy<Void> retryPolicy) {
    this.retryPolicy = retryPolicy;
    this.cachePopulator = cachePopulator;

    // initialize objects needed for cache accessor
    var operationalTimeConfig = configurationConsumerUtility.resolve(OPERATIONAL_TIME_PERIOD_CONFIG,
      Collections.emptyList());
    Map<String, List<String>> stationGroupMap = configurationConsumerUtility.resolve(
      STATION_GROUP_NAMES_CONFIG, List.of(), Map.class);

    this.operationalStart = Duration.parse(operationalTimeConfig.get(OPERATIONAL_PERIOD_START).toString());
    this.operationalEnd = Duration.parse(operationalTimeConfig.get(OPERATIONAL_PERIOD_END).toString());
    this.stationGroupNames = stationGroupMap.get(STATION_GROUP_NAMES);
  }

  /**
   * Populate the cache using Spring scheduled cronjob
   */
  public void populateCache() {

    logger.info("Populating cache with interval {} to {}", operationalStart, operationalEnd);
    cachePopulator.populate(stationGroupNames, operationalStart, operationalEnd, retryPolicy);
  }
}
