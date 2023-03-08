package gms.shared.frameworks.cache.utils;

import com.google.common.base.Preconditions;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.eviction.AbstractEvictionPolicyFactory;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.lang.IgnitePredicate;
import org.apache.ignite.util.AttributeNodeFilter;

import java.util.Optional;

public class CacheInfo {

  private static final String NODE_EXT = ".node";

  private final String nodeAttr;
  private final String cacheName;
  private final CacheMode cacheMode;
  private final CacheAtomicityMode cacheAtomicityMode;
  private final boolean onHeap;
  private final Optional<AbstractEvictionPolicyFactory> evictionPolicy;
  private final IgnitePredicate<ClusterNode> nodeFilter;

  public CacheInfo(String cacheName, CacheMode cacheMode,
    CacheAtomicityMode cacheAtomicityMode,
    boolean onHeap, Optional<AbstractEvictionPolicyFactory> evictionPolicy) {

    if (evictionPolicy.isPresent()) {
      Preconditions.checkState(onHeap,
        "If eviction policy is set, onHeap memory must be set to true");
    }
    this.cacheName = cacheName;
    this.nodeAttr = cacheName + NODE_EXT;
    this.cacheMode = cacheMode;
    this.cacheAtomicityMode = cacheAtomicityMode;
    this.onHeap = onHeap;
    this.evictionPolicy = evictionPolicy;
    this.nodeFilter = new AttributeNodeFilter(this.nodeAttr, true);
  }

  public String getNodeAttr() {
    return this.getCacheName() + NODE_EXT;
  }

  public String getCacheName() {
    return this.cacheName;
  }

  public CacheMode getCacheMode() {
    return this.cacheMode;
  }

  public CacheAtomicityMode getCacheAtomicityMode() {
    return this.cacheAtomicityMode;
  }

  public boolean isOnHeap() {
    return this.onHeap;
  }

  public Optional<AbstractEvictionPolicyFactory> getEvictionPolicy() {
    return this.evictionPolicy;
  }

  public IgnitePredicate<ClusterNode> getNodeFilter() {
    return this.nodeFilter;
  }
}
