package gms.shared.stationdefinition.factory;

import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.test.utils.containers.ZookeeperTest;
import gms.shared.stationdefinition.database.connector.factory.StationDefinitionDatabaseConnectorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManagerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class StationDefinitionManagerFactoryTest extends ZookeeperTest {

  @Mock
  private EntityManagerFactory entityManagerFactory;
  @Mock
  private ConfigurationConsumerUtility configurationConsumerUtility;
  private StationDefinitionDatabaseConnectorFactory stationDefinitionDatabaseConnectorFactory;

  @BeforeAll
  protected static void fixtureSetUp() {
    setUpContainer();
  }

  @BeforeEach
  public void testSetup() {
    stationDefinitionDatabaseConnectorFactory = StationDefinitionDatabaseConnectorFactory
      .create(entityManagerFactory);
  }

  @Test
  void initializationErrors_missingFactory() {
    final NullPointerException error = assertThrows(NullPointerException.class,
      () -> StationDefinitionAccessorFactory.create(null));
    assertEquals("A StationDefinitionJpaRepositoryFactory must be provided.", error.getMessage());
  }

  @Test
  void initialize_requiresEntityManagerFactory() {
    Assertions.assertDoesNotThrow(
      () -> StationDefinitionAccessorFactory
        .create(stationDefinitionDatabaseConnectorFactory));
  }

}