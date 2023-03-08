package gms.shared.event.repository.factory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManagerFactory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class OriginSimulatorDatabaseConnectorFactoryTest {

  @Mock
  EntityManagerFactory entityManagerFactory;
  OriginSimulatorDatabaseConnectorFactory factory;

  @Test
  void initializationErrors() {
    assertThrows(NullPointerException.class,
      () -> OriginSimulatorDatabaseConnectorFactory.create(null));
  }

  @Test
  void initialize_requiresEntityManagerFactory() {
    assertDoesNotThrow(
      () -> OriginSimulatorDatabaseConnectorFactory.create(entityManagerFactory));
  }

  @Test
  void geEventDatabaseConnectorInstance() {

    factory = OriginSimulatorDatabaseConnectorFactory.create(entityManagerFactory);
    assertDoesNotThrow(() -> factory.getEventDatabaseConnectorInstance());
  }

  @Test
  void getOrigErrDatabaseConnectorInstance() {

    factory = OriginSimulatorDatabaseConnectorFactory.create(entityManagerFactory);
    assertDoesNotThrow(() -> factory.getOrigErrDatabaseConnectorInstance());
  }

  @Test
  void getOriginDatabaseConnectorInstance() {

    factory = OriginSimulatorDatabaseConnectorFactory.create(entityManagerFactory);
    assertDoesNotThrow(() -> factory.getOriginDatabaseConnectorInstance());
  }
}