package gms.shared.stationdefinition.factory;

import gms.shared.frameworks.test.utils.containers.ZookeeperTest;
import gms.shared.stationdefinition.api.StationDefinitionAccessorInterface;
import gms.shared.stationdefinition.cache.CacheTestInitialization;
import gms.shared.stationdefinition.database.connector.factory.StationDefinitionDatabaseConnectorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@Disabled
  //zookeeper has been removed 8/5/2021
class StationDefinitionAccessorFactoryTest extends ZookeeperTest {
  private static final Logger logger = LoggerFactory.getLogger(StationDefinitionAccessorFactoryTest.class);
  static final String OPERATIONAL_TIME_PERIOD_CONFIG = "global.operational-time-period";
  static final String OPERATIONAL_PERIOD_START = "operationalPeriodStart";
  static final String OPERATIONAL_PERIOD_END = "operationalPeriodEnd";

  @Mock
  private EntityManagerFactory entityManagerFactory;
  @Mock
  private StationDefinitionDatabaseConnectorFactory stationDefinitionDatabaseConnectorFactory;
  @Mock
  private StationDefinitionAccessorInterface delegate;

  private StationDefinitionAccessorFactory stationDefinitionAccessorFactory;

  @BeforeAll
  protected static void fixtureSetUp() {
    setUpContainer();

    try {
      CacheTestInitialization.setup(systemConfig);
    } catch (IllegalStateException e) {
      logger.info("IgniteCache already initialized.");
    }
  }

  @BeforeEach
  public void testSetup() {
    stationDefinitionDatabaseConnectorFactory = StationDefinitionDatabaseConnectorFactory
      .create(entityManagerFactory);
    stationDefinitionAccessorFactory =
      StationDefinitionAccessorFactory.create(stationDefinitionDatabaseConnectorFactory);
    assertNotNull(stationDefinitionAccessorFactory);
  }

  @Test
  void testCreate() {
    assertThrows(NullPointerException.class, () -> StationDefinitionAccessorFactory
      .create(null));
  }
}
