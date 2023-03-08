package gms.testtools.simulators.bridgeddatasourcesimulator.repository;

import gms.shared.workflow.dao.ClassEndTimeNameTimeKey;
import gms.shared.workflow.dao.IntervalDao;
import org.apache.commons.lang3.Validate;

import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaUpdate;
import java.util.List;

public class BridgedDataSourceIntervalRepositoryJpa extends BridgedDataSourceRepositoryJpa
  implements BridgedDataSourceRepository {

  protected BridgedDataSourceIntervalRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    super(entityManagerFactory);
  }

  public static BridgedDataSourceIntervalRepositoryJpa create(
    EntityManagerFactory entityManagerFactory
  ) {
    Validate.notNull(entityManagerFactory);
    return new BridgedDataSourceIntervalRepositoryJpa(entityManagerFactory);
  }


  @Override
  public void cleanupData() {

    runWithEntityManager(entityManager -> {
      try {
        entityManager.getTransaction().begin();
        cleanupTable(IntervalDao.class, entityManager);
        entityManager.getTransaction().commit();
      } catch (Exception e) {
        entityManager.getTransaction().rollback();
        throw e;
      }
      return true;
    });
  }


  /**
   * For each of the given {@link IntervalDao}s, if a record with a matching {@link
   * ClassEndTimeNameTimeKey} exists, update it with the values from the {@code simulationData}.
   * Otherwise, insert the {@code simulationData} into the database.
   *
   * @param simulationData a list of {@link IntervalDao}s to update or insert into the database
   */
  public void storeOrUpdate(List<IntervalDao> simulationData) {

    runWithEntityManager(entityManager -> {

      try {
        entityManager.getTransaction().begin();

        simulationData.forEach(intervalDao -> {

          var criteriaBuilder = entityManager.getCriteriaBuilder();
          CriteriaUpdate<IntervalDao> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(IntervalDao.class);

          var intervalDaoRt = criteriaUpdate.from(IntervalDao.class);
          var primaryKeyPath = intervalDaoRt.get("classEndTimeNameTimeKey");

          criteriaUpdate.set(
            primaryKeyPath.get("type"),
            intervalDao.getType()
          );
          criteriaUpdate.set(
            primaryKeyPath.get("name"),
            intervalDao.getName()
          );
          criteriaUpdate.set(
            primaryKeyPath.get("time"),
            intervalDao.getTime()
          );
          criteriaUpdate.set(
            primaryKeyPath.get("endTime"),
            intervalDao.getEndTime()
          );

          criteriaUpdate.set("state", intervalDao.getState());
          criteriaUpdate.set("author", intervalDao.getAuthor());
          criteriaUpdate.set("percentAvailable", intervalDao.getPercentAvailable());
          criteriaUpdate.set("processStartDate", intervalDao.getProcessStartDate());
          criteriaUpdate.set("processEndDate", intervalDao.getProcessEndDate());
          criteriaUpdate.set("lastModificationDate", intervalDao.getLastModificationDate());
          criteriaUpdate.set("loadDate", intervalDao.getLoadDate());
          criteriaUpdate.where(
            criteriaBuilder.equal(intervalDaoRt.get("intervalIdentifier"),
              intervalDao.getIntervalIdentifier()));

          int updatedCount = entityManager.createQuery(criteriaUpdate).executeUpdate();

          if (updatedCount == 0) {
            entityManager.persist(intervalDao);
          }
        });

        entityManager.getTransaction().commit();
      } catch (Exception e) {
        entityManager.getTransaction().rollback();
        throw e;
      }

      return true;
    });

  }

}
