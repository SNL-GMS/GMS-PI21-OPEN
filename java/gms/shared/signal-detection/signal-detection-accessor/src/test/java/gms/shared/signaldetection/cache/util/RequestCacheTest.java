package gms.shared.signaldetection.cache.util;

import gms.shared.frameworks.cache.utils.IgniteConnectionManager;
import gms.shared.frameworks.test.utils.containers.ZookeeperTest;
import gms.shared.signaldetection.api.response.SignalDetectionsWithChannelSegments;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static gms.shared.signaldetection.cache.util.SignalDetectionCacheFactory.REQUEST_CACHE;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.DETECTIONS_WITH_CHANNEL_SEGMENTS;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.REQUEST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Disabled //zookeeper has been removed 8/5/2021
class RequestCacheTest extends ZookeeperTest {

  private RequestCache cache;

  @BeforeAll
  static void setup() {
    try {
      Path tempIgniteDirectory = Files.createTempDirectory("ignite-work");
      System.setProperty("IGNITE_HOME", tempIgniteDirectory.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }

    IgniteConnectionManager.initialize(systemConfig, List.of(REQUEST_CACHE));
  }

  @BeforeEach
  void initializeCache() {
    cache = new RequestCache();
  }

  @Test
  void testCreate() {
    RequestCache cache = new RequestCache();
    assertNotNull(cache);
  }

  @Test
  void testCacheMiss() {
    Optional<SignalDetectionsWithChannelSegments> possibleResult = cache.retrieve(REQUEST);
    assertTrue(possibleResult.isEmpty());
  }

  @Test
  void testCacheHit() {
    cache.cache(REQUEST, DETECTIONS_WITH_CHANNEL_SEGMENTS);
    Optional<SignalDetectionsWithChannelSegments> cached = cache.retrieve(REQUEST);
    cached.ifPresentOrElse(cachedResult -> assertEquals(DETECTIONS_WITH_CHANNEL_SEGMENTS, cachedResult),
      () -> fail("Could not retrieve cached item"));
  }

}