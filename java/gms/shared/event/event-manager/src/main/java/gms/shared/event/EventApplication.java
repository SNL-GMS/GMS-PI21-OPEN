package gms.shared.event;

import gms.shared.event.accessor.EventStatusInfoCache;
import gms.shared.event.accessor.SignalDetectionAccessorConfiguration;
import gms.shared.event.repository.util.id.EventIdUtility;
import gms.shared.frameworks.cache.utils.CacheInfo;
import gms.shared.frameworks.cache.utils.IgniteConnectionManager;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.signaldetection.cache.util.SignalDetectionCacheFactory;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.List;
import java.util.Optional;

@SpringBootApplication(exclude = {
  SqlInitializationAutoConfiguration.class,
  DataSourceAutoConfiguration.class,
  DataSourceTransactionManagerAutoConfiguration.class,
  HibernateJpaAutoConfiguration.class,
  JdbcTemplateAutoConfiguration.class
})
@EnableConfigurationProperties
@EntityScan(basePackages = {
  "gms.shared.event.dao",
  "gms.shared.signaldetection.dao"
})

/**
 * Entry point for the event-manager-service
 *
 * <p>Initializes ignite caches and starts the {@link EventManager}</p>
 */
public class EventApplication {

  private static final Logger logger = LoggerFactory.getLogger(EventApplication.class);

  private static final String CACHE_INITIALIZED = "IgniteCache successfully initialized";
  private static final String CACHE_DUPLICATE_INITIALIZED = "Cache already initialized: ";

  // EventIdUtility CacheInfo Objects
  public static final CacheInfo EVENT_ID_EVENT_RECORD_ID = EventIdUtility.EVENT_ID_EVENT_RECORD_ID;
  public static final CacheInfo EVENT_RECORD_ID_EVENT_ID = EventIdUtility.EVENT_RECORD_ID_EVENT_ID;
  public static final CacheInfo EVENT_HYPOTHESIS_ID_ORIGIN_UNIQUE_ID = EventIdUtility.EVENT_HYPOTHESIS_ID_ORIGIN_UNIQUE_ID;
  public static final CacheInfo ORIGIN_UNIQUE_ID_EVENT_HYPOTHESIS_ID = EventIdUtility.ORIGIN_UNIQUE_ID_EVENT_HYPOTHESIS_ID;

  // EventStatusInfo CacheInfo Objects
  public static final CacheInfo EVENT_STATUS_INFO_CACHE = EventStatusInfoCache.EVENT_STATUS_INFO_CACHE;

  // Signal Detection CacheInfo Objects
  public static final CacheInfo ARID_SIGNAL_DETECTION_ID_CACHE = SignalDetectionCacheFactory.ARID_SIGNAL_DETECTION_ID_CACHE;
  public static final CacheInfo SIGNAL_DETECTION_ID_ARID_CACHE = SignalDetectionCacheFactory.SIGNAL_DETECTION_ID_ARID_CACHE;
  public static final CacheInfo ARRIVAL_ID_SIGNAL_DETECTION_HYPOTHESIS_ID = SignalDetectionCacheFactory.ARRIVAL_ID_SIGNAL_DETECTION_HYPOTHESIS_ID;
  public static final CacheInfo SIGNAL_DETECTION_HYPOTHESIS_ID_ARRIVAL_ID = SignalDetectionCacheFactory.SIGNAL_DETECTION_HYPOTHESIS_ID_ARRIVAL_ID;
  public static final CacheInfo ASSOC_ID_SIGNAL_DETECTION_HYPOTHESIS_ID = SignalDetectionCacheFactory.ASSOC_ID_SIGNAL_DETECTION_HYPOTHESIS_ID;
  public static final CacheInfo SIGNAL_DETECTION_HYPOTHESIS_ID_ASSOC_ID = SignalDetectionCacheFactory.SIGNAL_DETECTION_HYPOTHESIS_ID_ASSOC_ID;
  public static final CacheInfo AMPLITUDE_ID_FEATURE_MEASUREMENT_ID = SignalDetectionCacheFactory.AMPLITUDE_ID_FEATURE_MEASUREMENT_ID;
  public static final CacheInfo FEATURE_MEASUREMENT_ID_AMPLITUDE_ID = SignalDetectionCacheFactory.FEATURE_MEASUREMENT_ID_AMPLITUDE_ID;
  public static final CacheInfo CHANNEL_SEGMENT_DESCRIPTOR_WFID_CACHE = SignalDetectionAccessorConfiguration.CHANNEL_SEGMENT_DESCRIPTOR_WFID_CACHE;
  public static final CacheInfo REQUEST_CACHE = new CacheInfo("signal-detection-request",
    CacheMode.REPLICATED, CacheAtomicityMode.ATOMIC, true, Optional.empty());

  private static final List<CacheInfo> CACHE_INFO_LIST = List.of(
    EVENT_ID_EVENT_RECORD_ID,
    EVENT_RECORD_ID_EVENT_ID,
    EVENT_HYPOTHESIS_ID_ORIGIN_UNIQUE_ID,
    EVENT_STATUS_INFO_CACHE,
    ORIGIN_UNIQUE_ID_EVENT_HYPOTHESIS_ID,
    ARID_SIGNAL_DETECTION_ID_CACHE,
    SIGNAL_DETECTION_ID_ARID_CACHE,
    ARRIVAL_ID_SIGNAL_DETECTION_HYPOTHESIS_ID,
    SIGNAL_DETECTION_HYPOTHESIS_ID_ARRIVAL_ID,
    ASSOC_ID_SIGNAL_DETECTION_HYPOTHESIS_ID,
    SIGNAL_DETECTION_HYPOTHESIS_ID_ASSOC_ID,
    AMPLITUDE_ID_FEATURE_MEASUREMENT_ID,
    FEATURE_MEASUREMENT_ID_AMPLITUDE_ID,
    CHANNEL_SEGMENT_DESCRIPTOR_WFID_CACHE,
    REQUEST_CACHE);

  public static void main(String[] args) {
    logger.info("Starting event manager");

    var systemConfig = SystemConfig.create("global");

    try {
      IgniteConnectionManager.initialize(systemConfig, CACHE_INFO_LIST);
    } catch (IllegalStateException e) {
      logger.warn(CACHE_DUPLICATE_INITIALIZED, e);
    }
    logger.info(CACHE_INITIALIZED);

    SpringApplication.run(EventApplication.class, args);
  }
}
