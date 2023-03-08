package gms.shared.frameworks.osd.repository.stationreference;

import gms.shared.frameworks.coi.exceptions.DataExistsException;
import gms.shared.frameworks.osd.api.stationreference.ReferenceSensorRepositoryInterface;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSensor;
import gms.shared.frameworks.osd.dao.stationreference.ReferenceSensorDao;
import gms.shared.frameworks.osd.repository.utils.RepositoryUtility;
import org.apache.commons.lang3.Validate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReferenceSensorRepositoryJpa implements ReferenceSensorRepositoryInterface {

  private final EntityManagerFactory entityManagerFactory;
  private final RepositoryUtility<ReferenceSensor, ReferenceSensorDao> repositoryUtility;

  public ReferenceSensorRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
    this.repositoryUtility = RepositoryUtility.create(ReferenceSensorDao.class,
      ReferenceSensorDao::new,
      ReferenceSensorDao::toCoi);
  }

  @Override
  public List<ReferenceSensor> retrieveReferenceSensorsById(Collection<UUID> sensorIds) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<ReferenceSensorDao> query = cb.createQuery(ReferenceSensorDao.class);
      Root<ReferenceSensorDao> from = query.from(ReferenceSensorDao.class);
      if (!sensorIds.isEmpty()) {
        query.select(from).where(from.get("id").in(sensorIds));
      }
      return entityManager.createQuery(query).getResultStream().map(ReferenceSensorDao::toCoi)
        .collect(Collectors.toList());
    } finally {
      entityManager.close();
    }
  }

  @Override
  public Map<String, List<ReferenceSensor>> retrieveSensorsByChannelName(
    Collection<String> channelNames) {
    Validate.notEmpty(channelNames);
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<ReferenceSensorDao> query = cb.createQuery(ReferenceSensorDao.class);
      Root<ReferenceSensorDao> from = query.from(ReferenceSensorDao.class);
      query.select(from).where(from.get("channelName").in(channelNames));
      return entityManager.createQuery(query).getResultStream().map(ReferenceSensorDao::toCoi)
        .collect(Collectors.groupingBy(ReferenceSensor::getChannelName));
    } finally {
      entityManager.close();
    }
  }

  @Override
  public void storeReferenceSensors(Collection<ReferenceSensor> sensors) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      for (ReferenceSensor sensor : sensors) {
        if (referenceSensorExists(sensor, entityManager)) {
          throw new DataExistsException(
            String.format("ReferenceSensor %s with comment %s", sensor.getId(),
              sensor.getComment()));
        }
      }
      this.repositoryUtility.persist(sensors, entityManager);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    } finally {
      entityManager.close();
    }
  }

  private boolean referenceSensorExists(ReferenceSensor sensor, EntityManager em) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Long> query = cb.createQuery(Long.class);
    Root<ReferenceSensorDao> from = query.from(ReferenceSensorDao.class);
    query.select(cb.count(from)).where(cb.equal(from.get("id"), sensor.getId()));
    return em.createQuery(query).getSingleResult() == 1;
  }

}
