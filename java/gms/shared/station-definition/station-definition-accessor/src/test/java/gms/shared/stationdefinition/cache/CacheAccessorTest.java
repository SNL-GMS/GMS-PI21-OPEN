package gms.shared.stationdefinition.cache;

import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import net.jodah.failsafe.RetryPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static gms.shared.stationdefinition.cache.CacheAccessor.OPERATIONAL_PERIOD_END;
import static gms.shared.stationdefinition.cache.CacheAccessor.OPERATIONAL_PERIOD_START;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.STATION_GROUP;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheAccessorTest {
  private static final List<String> STATION_GROUP_NAMES = List.of(STATION_GROUP.getName());
  private static final Duration PERIOD_START = Duration.ofMinutes(5);
  private static final Duration PERIOD_END = Duration.ofMinutes(0);

  @Mock
  ConfigurationConsumerUtility configurationConsumerUtility;

  @Mock
  RetryPolicy<Void> retryPolicy;

  @Mock
  CachePopulator cachePopulator;

  private static final Map<String, Object> operationalTimeConfig = Map.of(OPERATIONAL_PERIOD_START,
    PERIOD_START,
    OPERATIONAL_PERIOD_END,
    PERIOD_END);

  private static final Map<String, List<String>> stationGroupMap = Map.of(CacheAccessor.STATION_GROUP_NAMES,
    STATION_GROUP_NAMES);

  private static CacheAccessor cacheAccessor;

  @BeforeEach
  void initialize() {
    when(configurationConsumerUtility.resolve(CacheAccessor.OPERATIONAL_TIME_PERIOD_CONFIG,
      Collections.emptyList()))
      .thenReturn(operationalTimeConfig);

    when(configurationConsumerUtility.resolve(CacheAccessor.STATION_GROUP_NAMES_CONFIG,
      List.of(), Map.class))
      .thenReturn(stationGroupMap);

    cacheAccessor = new CacheAccessor(cachePopulator,
      configurationConsumerUtility, retryPolicy);
  }

  @Test
  void testPopulateCache() {
    try (MockedStatic<CachePopulator> utilities = Mockito.mockStatic(CachePopulator.class)) {
      cacheAccessor.populateCache();
      verify(cachePopulator, times(1)).populate(STATION_GROUP_NAMES,
        PERIOD_START, PERIOD_END, retryPolicy);
      verifyNoMoreInteractions(cachePopulator);
    }
  }
}
