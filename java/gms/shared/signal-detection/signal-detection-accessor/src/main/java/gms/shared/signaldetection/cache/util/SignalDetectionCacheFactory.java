package gms.shared.signaldetection.cache.util;

import gms.shared.frameworks.cache.utils.CacheInfo;
import gms.shared.frameworks.cache.utils.IgniteConnectionManager;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.apache.commons.lang3.Validate;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SignalDetectionCacheFactory {

  public static final CacheInfo REQUEST_CACHE = new CacheInfo("signal-detection-request",
    CacheMode.REPLICATED, CacheAtomicityMode.ATOMIC, true, Optional.empty());
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
  public static final CacheInfo CHANNEL_SEGMENT_DESCRIPTOR_WFID_CACHE = new CacheInfo("channel-segment-descriptor-wfid-cache",
    CacheMode.REPLICATED, CacheAtomicityMode.ATOMIC, true, Optional.empty());

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

  private static final List<CacheInfo> CACHE_INFO_LIST = List.of(REQUEST_CACHE,
    ARID_SIGNAL_DETECTION_ID_CACHE,
    SIGNAL_DETECTION_ID_ARID_CACHE,
    ARRIVAL_ID_SIGNAL_DETECTION_HYPOTHESIS_ID,
    SIGNAL_DETECTION_HYPOTHESIS_ID_ARRIVAL_ID,
    CHANNEL_SEGMENT_DESCRIPTOR_WFID_CACHE,
    ASSOC_ID_SIGNAL_DETECTION_HYPOTHESIS_ID,
    SIGNAL_DETECTION_HYPOTHESIS_ID_ASSOC_ID,
    AMPLITUDE_ID_FEATURE_MEASUREMENT_ID,
    FEATURE_MEASUREMENT_ID_AMPLITUDE_ID);

  private SignalDetectionCacheFactory() {
    // prevent instantiation
  }

  public static void setUpCache(SystemConfig systemConfig) {
    Validate.notNull(systemConfig, "SystemConfig is required");
    IgniteConnectionManager.initialize(systemConfig, CACHE_INFO_LIST);
  }

  public static void setUpIdCache(SystemConfig systemConfig) {
    Validate.notNull(systemConfig, "SystemConfig is required");
    IgniteConnectionManager.initialize(systemConfig, CACHE_INFO_LIST.stream()
      .filter(cacheInfo -> cacheInfo.getCacheName().contains("-id"))
      .collect(Collectors.toList()));
  }
}
