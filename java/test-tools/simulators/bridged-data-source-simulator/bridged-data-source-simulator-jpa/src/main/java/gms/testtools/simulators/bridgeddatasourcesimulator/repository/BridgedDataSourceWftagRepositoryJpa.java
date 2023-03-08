package gms.testtools.simulators.bridgeddatasourcesimulator.repository;

import gms.shared.stationdefinition.dao.css.WfTagDao;
import org.apache.commons.lang3.Validate;

import javax.persistence.EntityManagerFactory;

public class BridgedDataSourceWftagRepositoryJpa extends BridgedDataSourceAnalysisRepositoryJpa
  implements BridgedDataSourceRepository {

  protected BridgedDataSourceWftagRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    super(entityManagerFactory);
  }

  public static BridgedDataSourceWftagRepositoryJpa create(
    EntityManagerFactory entityManagerFactory) {
    Validate.notNull(entityManagerFactory);
    return new BridgedDataSourceWftagRepositoryJpa(entityManagerFactory);
  }

  @Override
  public void cleanupData() {
    runWithEntityManager(entityManager -> {

      try {
        entityManager.getTransaction().begin();
        cleanupTable(WfTagDao.class, entityManager);
        entityManager.getTransaction().commit();
      } catch (Exception e) {
        entityManager.getTransaction().rollback();
        throw e;
      }
      return true;
    });
  }
}
