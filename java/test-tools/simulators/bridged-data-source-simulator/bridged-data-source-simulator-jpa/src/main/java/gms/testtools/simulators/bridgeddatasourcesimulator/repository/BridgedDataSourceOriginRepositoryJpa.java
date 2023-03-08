package gms.testtools.simulators.bridgeddatasourcesimulator.repository;

import gms.shared.event.dao.ArInfoDao;
import gms.shared.event.dao.EventControlDao;
import gms.shared.event.dao.EventDao;
import gms.shared.event.dao.NetMagDao;
import gms.shared.event.dao.OrigerrDao;
import gms.shared.event.dao.OriginDao;
import gms.shared.event.dao.StaMagDao;
import gms.shared.signaldetection.dao.css.AssocDao;
import org.apache.commons.lang3.Validate;

import javax.persistence.EntityManagerFactory;

public class BridgedDataSourceOriginRepositoryJpa extends BridgedDataSourceRepositoryJpa
  implements BridgedDataSourceRepository {

  private BridgedDataSourceOriginRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    super(entityManagerFactory);
  }

  public static BridgedDataSourceOriginRepositoryJpa create(
    EntityManagerFactory entityManagerFactory
  ) {
    Validate.notNull(entityManagerFactory,
      "BridgedDataSourceOriginRepositoryJpa.create: entityManagerFactory is null");
    return new BridgedDataSourceOriginRepositoryJpa(entityManagerFactory);
  }

  @Override
  public void cleanupData() {

    runWithEntityManager(entityManager -> {

      try {
        entityManager.getTransaction().begin();

        cleanupTable(EventDao.class, entityManager);
        cleanupTable(OrigerrDao.class, entityManager);
        cleanupTable(EventControlDao.class, entityManager);
        cleanupTable(AssocDao.class, entityManager);
        cleanupTable(ArInfoDao.class, entityManager);
        cleanupTable(NetMagDao.class, entityManager);
        cleanupTable(StaMagDao.class, entityManager);

        // Note, this must be deleted last!
        cleanupTable(OriginDao.class, entityManager);
        entityManager.getTransaction().commit();
      } catch (Exception e) {
        entityManager.getTransaction().rollback();
        throw e;
      }
      return true;
    });

  }
}
