package gms.shared.frameworks.osd.repository.performancemonitoring;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import gms.shared.frameworks.osd.api.performancemonitoring.CapabilitySohRollupRepositoryInterface;
import gms.shared.frameworks.osd.api.util.RepositoryExceptionUtils;
import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.dao.soh.CapabilitySohRollupDao;
import gms.shared.frameworks.osd.repository.performancemonitoring.converter.CapabilitySohRollupConverter;

public class CapabilitySohRollupRepositoryJpa implements CapabilitySohRollupRepositoryInterface {

  private static final Logger logger = LoggerFactory.getLogger(CapabilitySohRollupRepositoryJpa.class);

  private static final String STATION_GROUP_NAME_ATTRIBUTE = "stationGroupName";
  private static final String CAPABILITY_ROLLUP_TIME_ATTRIBUTE = "capabilityRollupTime";

  private final EntityManagerFactory entityManagerFactory;

  public CapabilitySohRollupRepositoryJpa(EntityManagerFactory emf) {
    this.entityManagerFactory = emf;
  }

  @Override
  public List<CapabilitySohRollup> retrieveCapabilitySohRollupByStationGroup(
    Collection<String> stationGroups) {

    var entityManager = entityManagerFactory.createEntityManager();

    try {
      var cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<CapabilitySohRollupDao> capabilityQuery =
        cb.createQuery(CapabilitySohRollupDao.class);
      Root<CapabilitySohRollupDao> fromCapability =
        capabilityQuery.from(CapabilitySohRollupDao.class);

      if (!stationGroups.isEmpty()) {
        capabilityQuery.where(fromCapability.get(STATION_GROUP_NAME_ATTRIBUTE).in(stationGroups));
      }

      List<CapabilitySohRollupDao> capabilitySohRollupDaos =
        entityManager.createQuery(capabilityQuery).getResultList();

      List<CapabilitySohRollup> result;
      result = capabilitySohRollupDaos
        .stream()
        .map(capabilityDao -> new CapabilitySohRollupConverter().toCoi(capabilityDao))
        .collect(Collectors.toList());
      return result;
    } catch (PersistenceException e) {
      logger.error((e.getMessage()));
    } finally {
      entityManager.close();
    }
    return List.of();
  }

  @Override
  public void storeCapabilitySohRollup(Collection<CapabilitySohRollup> capabilitySohRollups) {

    Validate.notNull(capabilitySohRollups);
    var entityManager = this.entityManagerFactory.createEntityManager();

    entityManager.getTransaction().begin();
    try {
      TypedQuery<Integer> existsQuery = entityManager.createNamedQuery("CapabilitySohRollupDao.exists", Integer.class);
      for (CapabilitySohRollup capabilitySohRollup : capabilitySohRollups) {
        existsQuery.setParameter("id", capabilitySohRollup.getId());
        if (existsQuery.getSingleResult() == 0) {
          CapabilitySohRollupDao dao = new CapabilitySohRollupConverter().fromCoi(capabilitySohRollup, entityManager);
          entityManager.persist(dao);
        } else {
          logger.info("Duplicate CapabilitySohRollup {} found, dropping from batch store call",
            capabilitySohRollup.getId());
        }
      }
      entityManager.getTransaction().commit();
    } catch (PersistenceException e) {
      entityManager.getTransaction().rollback();
      throw RepositoryExceptionUtils.wrap(e);
    } finally {
      entityManager.close();
    }

  }

  @Override
  public List<CapabilitySohRollup> retrieveLatestCapabilitySohRollupByStationGroup(
    Collection<String> stationGroupNames) {
    Objects.requireNonNull(stationGroupNames);
    Preconditions.checkState(!stationGroupNames.isEmpty());
    var entityManager = entityManagerFactory.createEntityManager();

    try {
      var builder = entityManager.getCriteriaBuilder();
      CriteriaQuery<CapabilitySohRollupDao> capabilityRollupQuery =
        builder.createQuery(CapabilitySohRollupDao.class);
      Root<CapabilitySohRollupDao> fromCapabilityRollup =
        capabilityRollupQuery.from(CapabilitySohRollupDao.class);
      capabilityRollupQuery.select(fromCapabilityRollup);

      Expression<String> stationGroupName = fromCapabilityRollup.join(STATION_GROUP_NAME_ATTRIBUTE);

      Expression<Instant> time = fromCapabilityRollup.get(CAPABILITY_ROLLUP_TIME_ATTRIBUTE);

      Subquery<Instant> subQuery = capabilityRollupQuery.subquery(Instant.class);
      Root<CapabilitySohRollupDao> subRoot =
        subQuery.from(CapabilitySohRollupDao.class);

      Expression<String> subStationGroupName = subRoot.join(STATION_GROUP_NAME_ATTRIBUTE);
      Expression<Instant> subTime = subRoot.get(CAPABILITY_ROLLUP_TIME_ATTRIBUTE);

      subQuery
        .where(
          subStationGroupName.in(stationGroupNames),
          builder.equal(
            subStationGroupName, stationGroupName
          )
        )
        .groupBy(subStationGroupName)
        .select(builder.greatest(subTime));

      capabilityRollupQuery.where(builder.equal(time, subQuery));

      var converter = new CapabilitySohRollupConverter();
      return entityManager.createQuery(capabilityRollupQuery)
        .getResultStream()
        .map(converter::toCoi)
        .collect(Collectors.toList());
    } finally {
      entityManager.close();
    }
  }

}
