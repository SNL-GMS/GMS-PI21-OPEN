package gms.shared.frameworks.osd.repository.systemmessage;

import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.osd.dao.systemmessage.SystemMessageDao;
import gms.shared.frameworks.osd.repository.SohPostgresTest;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
class SystemMessageRepositoryJpaTest extends SohPostgresTest {

  @Test
  void testStoreSystemMessagesValidation() {
    assertThrows(NullPointerException.class,
      () -> new SystemMessageRepositoryJpa(entityManagerFactory).storeSystemMessages(null));
  }

  @Test
  void testStoreSystemMessages() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();

    try {
      SystemMessageRepositoryJpa systemMessageRepositoryJpa = new SystemMessageRepositoryJpa(
        entityManagerFactory);

      systemMessageRepositoryJpa.storeSystemMessages(TestFixtures.msgs);

      for (SystemMessage msg : TestFixtures.msgs) {
        SystemMessageDao actual = entityManager.find(SystemMessageDao.class, msg.getId());
        assertNotNull(actual);

        SystemMessage expected = actual.toCoi();
        assertNotNull(expected);

        assertEquals(expected, actual.toCoi());
      }
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }
}
