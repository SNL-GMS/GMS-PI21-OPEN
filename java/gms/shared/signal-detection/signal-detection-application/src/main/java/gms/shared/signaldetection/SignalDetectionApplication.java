package gms.shared.signaldetection;

import gms.shared.frameworks.cache.utils.CacheInfo;
import gms.shared.frameworks.cache.utils.IgniteConnectionManager;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class SignalDetectionApplication {
  private static final Logger logger = LoggerFactory.getLogger(SignalDetectionApplication.class);

  public static final CacheInfo ARID_SIGNAL_DETECTION_ID_CACHE = new CacheInfo("arid-signal-detection-id-cache",
    CacheMode.REPLICATED, CacheAtomicityMode.ATOMIC, true, Optional.empty());
  public static final CacheInfo SIGNAL_DETECTION_ID_ARID_CACHE = new CacheInfo("signal-detection-id-arid-cache",
    CacheMode.REPLICATED, CacheAtomicityMode.ATOMIC, true, Optional.empty());
  public static final CacheInfo ARRIVAL_ID_SIGNAL_DETECTION_HYPOTHESIS_ID =
    new CacheInfo("arrival-id-signal-detection-hypothesis-id", CacheMode.REPLICATED,
      CacheAtomicityMode.ATOMIC, true, Optional.empty());
  public static final CacheInfo SIGNAL_DETECTION_HYPOTHESIS_ID_ARRIVAL_ID =
    new CacheInfo("signal-detection-hypothesis-id-arrival-id-cache", CacheMode.REPLICATED,
      CacheAtomicityMode.ATOMIC, true, Optional.empty());
  public static final CacheInfo ASSOC_ID_SIGNAL_DETECTION_HYPOTHESIS_ID =
    new CacheInfo("assoc-id-signal-detection-hypothesis-id", CacheMode.REPLICATED,
      CacheAtomicityMode.ATOMIC, true, Optional.empty());
  public static final CacheInfo SIGNAL_DETECTION_HYPOTHESIS_ID_ASSOC_ID =
    new CacheInfo("signal-detection-hypothesis-id-assoc-id-cache", CacheMode.REPLICATED,
      CacheAtomicityMode.ATOMIC, true, Optional.empty());
  public static final CacheInfo AMPLITUDE_ID_FEATURE_MEASUREMENT_ID =
    new CacheInfo("amplitude-id-feature-measurement-id", CacheMode.REPLICATED,
      CacheAtomicityMode.ATOMIC, true, Optional.empty());
  public static final CacheInfo FEATURE_MEASUREMENT_ID_AMPLITUDE_ID =
    new CacheInfo("feature-measurement-id-amplitude-id", CacheMode.REPLICATED,
      CacheAtomicityMode.ATOMIC, true, Optional.empty());
  public static final CacheInfo CHANNEL_SEGMENT_DESCRIPTOR_WFID_CACHE = new CacheInfo("channel-segment-descriptor-wfid-cache",
    CacheMode.REPLICATED, CacheAtomicityMode.ATOMIC, true, Optional.empty());
  public static final CacheInfo REQUEST_CACHE = new CacheInfo("signal-detection-request",
    CacheMode.REPLICATED, CacheAtomicityMode.ATOMIC, true, Optional.empty());
  public static final CacheInfo VERSION_EFFECTIVE_TIME_CACHE = new CacheInfo("version-effective-time-cache",
    CacheMode.REPLICATED, CacheAtomicityMode.ATOMIC, true, Optional.empty());
  public static final CacheInfo VERSION_ENTITY_TIME_CACHE = new CacheInfo("version-entity-time-cache",
    CacheMode.REPLICATED, CacheAtomicityMode.ATOMIC, true, Optional.empty());
  public static final CacheInfo RECORD_ID_WFID_CHANNEL_CACHE = new CacheInfo("arid-wfid-channel-cache",
    CacheMode.REPLICATED, CacheAtomicityMode.ATOMIC, true, Optional.empty());
  public static final CacheInfo CHANNEL_RECORD_ID_WFID_CACHE = new CacheInfo("channel-arid-wfid-cache",
    CacheMode.REPLICATED, CacheAtomicityMode.ATOMIC, true, Optional.empty());
  public static final CacheInfo WFID_RESPONSE_CACHE = new CacheInfo("wfid-response-cache",
    CacheMode.REPLICATED, CacheAtomicityMode.ATOMIC, true, Optional.empty());
  public static final CacheInfo CHANNEL_RESPONSE_CACHE = new CacheInfo("channel-response-cache",
    CacheMode.REPLICATED, CacheAtomicityMode.ATOMIC, true, Optional.empty());

  private static final List<CacheInfo> CACHE_INFO_LIST = List.of(
    ARID_SIGNAL_DETECTION_ID_CACHE,
    SIGNAL_DETECTION_ID_ARID_CACHE,
    ARRIVAL_ID_SIGNAL_DETECTION_HYPOTHESIS_ID,
    SIGNAL_DETECTION_HYPOTHESIS_ID_ARRIVAL_ID,
    ASSOC_ID_SIGNAL_DETECTION_HYPOTHESIS_ID,
    SIGNAL_DETECTION_HYPOTHESIS_ID_ASSOC_ID,
    AMPLITUDE_ID_FEATURE_MEASUREMENT_ID,
    FEATURE_MEASUREMENT_ID_AMPLITUDE_ID,
    CHANNEL_SEGMENT_DESCRIPTOR_WFID_CACHE,
    REQUEST_CACHE,
    VERSION_EFFECTIVE_TIME_CACHE,
    VERSION_ENTITY_TIME_CACHE,
    RECORD_ID_WFID_CHANNEL_CACHE,
    CHANNEL_RECORD_ID_WFID_CACHE,
    WFID_RESPONSE_CACHE,
    CHANNEL_RESPONSE_CACHE);

  public static void main(String[] args) {
    logger.info("Starting signal detection manager");

    var systemConfig = SystemConfig.create("global");

    try {
      IgniteConnectionManager.initialize(systemConfig, CACHE_INFO_LIST);
    } catch (IllegalStateException e) {
      logger.error("Failed to initialize Ignite");
    }

    new SpringApplicationBuilder(SignalDetectionApplication.class)
      .run(args);
  }
}
