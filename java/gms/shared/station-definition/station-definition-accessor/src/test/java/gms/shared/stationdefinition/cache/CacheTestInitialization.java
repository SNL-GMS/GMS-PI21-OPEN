package gms.shared.stationdefinition.cache;

import gms.shared.frameworks.cache.utils.IgniteConnectionManager;
import gms.shared.frameworks.systemconfig.SystemConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static gms.shared.stationdefinition.cache.util.StationDefinitionCacheFactory.REQUEST_CACHE;
import static gms.shared.stationdefinition.cache.util.StationDefinitionCacheFactory.VERSION_EFFECTIVE_TIME_CACHE;
import static gms.shared.stationdefinition.cache.util.StationDefinitionCacheFactory.VERSION_ENTITY_TIME_CACHE;

public class CacheTestInitialization {
  public CacheTestInitialization() {
  }

  public static void setup(SystemConfig systemConfig) {
    try {
      Path tempIgniteDirectory = Files.createTempDirectory("ignite-work");
      System.setProperty("IGNITE_HOME", tempIgniteDirectory.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }

    IgniteConnectionManager.initialize(systemConfig, List.of(REQUEST_CACHE,
      VERSION_EFFECTIVE_TIME_CACHE, VERSION_ENTITY_TIME_CACHE));
  }
}
