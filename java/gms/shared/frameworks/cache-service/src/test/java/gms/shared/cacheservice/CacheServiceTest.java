package gms.shared.cacheservice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheServiceTest {

  @Test
  void testRunVisor() {
    assertTrue(CacheService.parseArgs(new String[]{"visor"}), "Visor Service was not started");
  }

  @Test
  void testRunService() {
    assertFalse(CacheService.parseArgs(new String[]{}), "Cache Service was not started");
  }

  @Test
  void testRunServiceNullArgument() {
    assertFalse(CacheService.parseArgs(null), "Cache Service was not started");
  }

  @Test
  void testRunServiceNullInput() {
    assertFalse(CacheService.parseArgs(new String[]{null}), "Cache Service was not started");
  }
}