package gms.shared.signaldetection.cache.util;

import gms.shared.frameworks.cache.utils.CacheInfo;
import gms.shared.frameworks.cache.utils.IgniteConnectionManager;
import gms.shared.signaldetection.api.request.Request;
import gms.shared.signaldetection.api.response.SignalDetectionsWithChannelSegments;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("signalDetectionRequestCache")
public class RequestCache {

  private IgniteCache<Request, SignalDetectionsWithChannelSegments> detectionsWithSegmentsByRequest;

  public static final CacheInfo REQUEST_CACHE = new CacheInfo("signal-detection-request",
    CacheMode.REPLICATED, CacheAtomicityMode.ATOMIC, true, Optional.empty());

  @Autowired
  public RequestCache() {

    detectionsWithSegmentsByRequest = IgniteConnectionManager.getOrCreateCache(REQUEST_CACHE);
  }


  /**
   * Retrieves the {@link SignalDetectionsWithChannelSegments} associated with the {@link Request}, if it has been
   * cached
   *
   * @param request the {@link Request} to find the cached result for
   * @return
   */
  public Optional<SignalDetectionsWithChannelSegments> retrieve(Request request) {
    return Optional.ofNullable(detectionsWithSegmentsByRequest.get(request));
  }

  /**
   * Stores the provided {@link Request} with it's result
   *
   * @param key the {@link Request} used to retrieve the result
   * @param result the {@link SignalDetectionsWithChannelSegments} from the {@link Request} to cache
   */
  public void cache(Request key, SignalDetectionsWithChannelSegments result) {
    detectionsWithSegmentsByRequest.put(key, result);
  }
}
