package gms.shared.frameworks.osd.repository.systemmessage;

import gms.shared.frameworks.osd.api.systemmessage.SystemMessageRepositoryInterface;
import gms.shared.frameworks.osd.api.util.RepositoryExceptionUtils;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.osd.dao.systemmessage.SystemMessageDao;
import gms.shared.metrics.CustomMetric;
import org.apache.commons.lang3.Validate;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;

public class SystemMessageRepositoryJpa implements SystemMessageRepositoryInterface {

  private static final CustomMetric<SystemMessageRepositoryJpa, Long> systemMessageRepositoryStoreSystemMessages =
    CustomMetric.create(CustomMetric::incrementer,
      "systemMessageRepositoryStoreSystemMessages_hits:type=Counter", 0L);

  private static final CustomMetric<Long, Long> systemMessageRepositoryStoreSystemMessagesDuration =
    CustomMetric.create(CustomMetric::updateTimingData,
      "systemMessageRepositoryStoreSystemMessagesDuration_duration:type=Value", 0L);

  private final EntityManagerFactory entityManagerFactory;

  public SystemMessageRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  @Override
  public void storeSystemMessages(Collection<SystemMessage> systemMessages) {
    Validate.notEmpty(systemMessages);

    systemMessageRepositoryStoreSystemMessages.updateMetric(this);
    var start = Instant.now();

    var entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();

    try {
      for (SystemMessage msg : systemMessages) {
        SystemMessageDao dao = entityManager.find(SystemMessageDao.class, msg.getId());

        if (dao == null) {
          entityManager.persist(SystemMessageDao.from(msg));
        }
      }

      entityManager.getTransaction().commit();
    } catch (PersistenceException e) {
      entityManager.getTransaction().rollback();
      throw RepositoryExceptionUtils.wrap(e);
    } finally {
      entityManager.close();

      var finish = Instant.now();
      long timeElapsed = Duration.between(start, finish).toMillis();
      systemMessageRepositoryStoreSystemMessagesDuration.updateMetric(timeElapsed);
    }
  }
}
