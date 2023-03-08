package gms.shared.frameworks.cache.utils;

import org.apache.ignite.configuration.IgniteConfiguration;

import java.nio.file.Path;

public class IgniteTestUtility {

  private IgniteTestUtility() {
  }

  public static synchronized void initializeLocally(Path igniteHome, CacheInfo... cacheInfos) {
    IgniteConnectionManager.initialize(createLocalConfiguration(igniteHome, cacheInfos));
  }

  private static IgniteConfiguration createLocalConfiguration(Path igniteHome, CacheInfo... cacheInfos) {
    return new IgniteConfiguration()
      .setIgniteHome(igniteHome.toString())
      .setWorkDirectory(igniteHome.toString())
      .setUserAttributes(IgniteConnectionUtility.buildNodeAttributes(cacheInfos))
      .setCacheConfiguration(IgniteConnectionUtility.buildCacheConfigurations(cacheInfos));
  }


}
