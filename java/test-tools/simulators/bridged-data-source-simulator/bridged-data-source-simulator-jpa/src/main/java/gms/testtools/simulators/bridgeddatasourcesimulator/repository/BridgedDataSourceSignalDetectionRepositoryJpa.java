package gms.testtools.simulators.bridgeddatasourcesimulator.repository;

import gms.shared.signaldetection.dao.css.AmplitudeDao;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import org.apache.commons.lang3.Validate;

import javax.persistence.EntityManagerFactory;

public class BridgedDataSourceSignalDetectionRepositoryJpa extends BridgedDataSourceAnalysisRepositoryJpa
  implements BridgedDataSourceRepository {

  protected BridgedDataSourceSignalDetectionRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    super(entityManagerFactory);
  }

  public static BridgedDataSourceSignalDetectionRepositoryJpa create(
    EntityManagerFactory entityManagerFactory) {
    Validate.notNull(entityManagerFactory);
    return new BridgedDataSourceSignalDetectionRepositoryJpa(entityManagerFactory);
  }

  @Override
  public void cleanupData() {
    runWithEntityManager(entityManager -> {

      try {
        entityManager.getTransaction().begin();
        cleanupTable(ArrivalDao.class, entityManager);
        cleanupTable(AmplitudeDao.class, entityManager);
        entityManager.getTransaction().commit();
      } catch (Exception e) {
        entityManager.getTransaction().rollback();
        throw e;
      }
      return true;
    });
  }
}
