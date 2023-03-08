package gms.shared.frameworks.cache.utils;

import gms.shared.frameworks.systemconfig.SystemConfig;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.events.EventType;
import org.apache.ignite.kubernetes.configuration.KubernetesConnectionConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.kubernetes.TcpDiscoveryKubernetesIpFinder;
import org.apache.ignite.spi.eventstorage.memory.MemoryEventStorageSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * The {@link IgniteConnectionManager} is used to create {@link Cache} instances from an Ignite node. This way a client just
 * need to adhere to accessing the cache via the JCache API rather than using an Ignite-specific query mechanism.
 * <p>
 * The class implements the {@link Closeable} interface in order to allow us to close the Ignite node upon shutdown of a
 * given application or use try-with-resources when scoping down the access to an Ignite node specifically.
 */
public class IgniteConnectionManager {

  private static final String CONFIG_IGNITE_INSTANCE_NAME = "ignite-instance-name";
  private static final int[] INCLUDED_EVENT_TYPES = {EventType.EVT_CACHE_OBJECT_PUT, EventType.EVT_CACHE_OBJECT_READ,
    EventType.EVT_CACHE_OBJECT_REMOVED, EventType.EVT_NODE_JOINED, EventType.EVT_NODE_LEFT};
  private static final String NAMESPACE_LOCATION = "/var/run/secrets/kubernetes.io/serviceaccount/namespace";

  private static Ignite ignite;

  private static final Logger logger = LoggerFactory.getLogger(IgniteConnectionManager.class);

  private IgniteConnectionManager() {
  } //private constructor for sonarlint

  /**
   * Create {@link IgniteConnectionManager} that controls configuration of cache and registers creator as a server node
   * server nodes participate in the cluster to store data
   * <p>
   * Clients can simply call IgniteConnectionManager.getInstance().getOrCreateCache(CacheName.{name}); to retrieve data from
   * the cluster
   *
   * @param systemConfig - system config to setup cache cluster
   * @param cacheList - caches to create in the cluster.  The node calling this will host (some of) the data
   */
  public static synchronized void initialize(SystemConfig systemConfig, Collection<CacheInfo> cacheList) {
    initialize(createConfiguration(systemConfig, cacheList));
  }

  static synchronized void initialize(IgniteConfiguration configuration) {
    logger.info("Initializing ignite cache");
    checkState(ignite == null, "IgniteConnectionManager should only be initialized once during application startup");
    ignite = Ignition.start(configuration);
  }

  private static IgniteConfiguration createConfiguration(SystemConfig systemConfig, Collection<CacheInfo> cacheList) {
    checkNotNull(systemConfig);
    checkNotNull(cacheList);

    long timeout = systemConfig.getValueAsLong("ignite-failure-detection-timeout");
    IgniteConfiguration cfg = new IgniteConfiguration()
      .setIgniteInstanceName(systemConfig.getValue(CONFIG_IGNITE_INSTANCE_NAME))
      .setUserAttributes(IgniteConnectionUtility.buildNodeAttributes(cacheList))
      .setNetworkTimeout(timeout)
      .setFailureDetectionTimeout(timeout)
      .setNetworkSendRetryDelay(timeout)
      .setGridLogger(new Slf4jLogger())
      .setIncludeEventTypes(INCLUDED_EVENT_TYPES)
      .setEventStorageSpi(new MemoryEventStorageSpi().setExpireAgeMs(60000))
      .setMetricsLogFrequency(0);

    if (systemConfig.getValueAsBoolean("ignite-kubernetes-ipfinder")) {
      KubernetesConnectionConfiguration kcc = new KubernetesConnectionConfiguration()
        .setNamespace(getKubernetesNamespace())
        .setServiceName("ignite");
      var kipFinder = new TcpDiscoveryKubernetesIpFinder(kcc);
      TcpDiscoverySpi spi = new TcpDiscoverySpi()
        .setIpFinder(kipFinder);
      cfg.setDiscoverySpi(spi)
        .setPeerClassLoadingEnabled(true);
    }

    if (cacheList.isEmpty()) {
      logger.info("Enabling Client Mode");
      cfg.setClientMode(true);
    } else {
      logger.info("Initializing Cache Configurations");
      cfg.setCacheConfiguration(IgniteConnectionUtility.buildCacheConfigurations(cacheList));
    }

    return cfg;
  }

  /**
   * retrieve a given {@link Cache} by the cacheName.
   *
   * @param cache name of the {@link Cache} instance to retrieve.
   * @param <K> the type of the key to retrieve a {@link Cache.Entry} instance.
   * @param <V> the type of the value of the retrieved {@link Cache.Entry} instance.
   * @return instance of {@link Cache} representing the requested Cache retrieved.  null if all server nodes are down
   */
  public static <K, V> IgniteCache<K, V> getOrCreateCache(CacheInfo cache) {
    Objects.requireNonNull(cache);
    Objects.requireNonNull(ignite, "Ignite has not been initialized.  Did you call create()?");
    return ignite.getOrCreateCache(IgniteConnectionUtility.createCacheFromCacheInfo(cache));
  }

  public static void close() {
    if (ignite != null)
      ignite.close();
    IgniteConnectionManager.ignite = null;
  }

  private static String getKubernetesNamespace() {
    var nameSpaceFile = Path.of(NAMESPACE_LOCATION);
    var namespace = "default";
    try {
      namespace = Files.readString(nameSpaceFile);
    } catch (IOException e) {
      logger.error("could not load namespace from " + NAMESPACE_LOCATION, e);
    }
    logger.info("Ignite kubernetesIpFinder using namespace " + namespace);
    return namespace;
  }

}

