package gms.testtools.simulators.bridgeddatasourcesimulator.repository;

import gms.shared.stationdefinition.dao.css.BeamDao;
import gms.shared.stationdefinition.dao.css.WfdiscDao;
import org.apache.commons.lang3.Validate;

import javax.persistence.EntityManagerFactory;

public class BridgedDataSourceWaveformRepositoryJpa extends BridgedDataSourceAnalysisRepositoryJpa
  implements BridgedDataSourceRepository {

  protected BridgedDataSourceWaveformRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    super(entityManagerFactory);
  }

  public static BridgedDataSourceWaveformRepositoryJpa create(
    EntityManagerFactory entityManagerFactory) {
    Validate.notNull(entityManagerFactory);
    return new BridgedDataSourceWaveformRepositoryJpa(entityManagerFactory);
  }

  @Override
  public void cleanupData() {
    runWithEntityManager(entityManager -> {

      try {
        entityManager.getTransaction().begin();
        cleanupTable(WfdiscDao.class, entityManager);
        cleanupTable(BeamDao.class, entityManager);
        entityManager.getTransaction().commit();
      } catch (Exception e) {
        entityManager.getTransaction().rollback();
        throw e;
      }
      return true;
    });
  }
}
