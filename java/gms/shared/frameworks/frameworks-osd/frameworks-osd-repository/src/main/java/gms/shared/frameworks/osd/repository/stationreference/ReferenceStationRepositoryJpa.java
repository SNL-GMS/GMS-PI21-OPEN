package gms.shared.frameworks.osd.repository.stationreference;

import gms.shared.frameworks.coi.exceptions.DataExistsException;
import gms.shared.frameworks.osd.api.stationreference.ReferenceStationRepositoryInterface;
import gms.shared.frameworks.osd.api.stationreference.util.ReferenceStationMembershipRequest;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceStation;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceStationMembership;
import gms.shared.frameworks.osd.dao.stationreference.ReferenceStationDao;
import gms.shared.frameworks.osd.dao.stationreference.ReferenceStationMembershipDao;
import gms.shared.frameworks.osd.repository.utils.RepositoryUtility;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReferenceStationRepositoryJpa implements ReferenceStationRepositoryInterface {

  private final EntityManagerFactory entityManagerFactory;
  private final RepositoryUtility<ReferenceStation, ReferenceStationDao> referenceStationRepositoryUtility;
  private final RepositoryUtility<ReferenceStationMembership, ReferenceStationMembershipDao> referenceStationMembershipRepositoryUtility;

  public ReferenceStationRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
    this.referenceStationRepositoryUtility = RepositoryUtility
      .create(ReferenceStationDao.class,
        ReferenceStationDao::new,
        ReferenceStationDao::toCoi);
    this.referenceStationMembershipRepositoryUtility = RepositoryUtility.create(
      ReferenceStationMembershipDao.class,
      ReferenceStationMembershipDao::new,
      ReferenceStationMembershipDao::toCoi);
  }

  @Override
  public List<ReferenceStation> retrieveStations(List<UUID> entityIds) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<ReferenceStationDao> query = cb.createQuery(ReferenceStationDao.class);
      Root<ReferenceStationDao> from = query.from(ReferenceStationDao.class);
      query.select(from);
      if (!entityIds.isEmpty()) {
        query.where(cb.or(entityIds.stream()
          .map(id -> cb.equal(from.get("entityId"), id))
          .toArray(Predicate[]::new)));
      }
      return entityManager.createQuery(query).getResultStream().map(ReferenceStationDao::toCoi)
        .collect(Collectors.toList());
    } finally {
      entityManager.close();
    }
  }

  @Override
  public List<ReferenceStation> retrieveStationsByVersionIds(Collection<UUID> stationVersionIds) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<ReferenceStationDao> query = cb.createQuery(ReferenceStationDao.class);
      Root<ReferenceStationDao> from = query.from(ReferenceStationDao.class);
      query.select(from);
      if (!stationVersionIds.isEmpty()) {
        query.where(cb.or(stationVersionIds.stream()
          .map(id -> cb.equal(from.get("versionId"), id))
          .toArray(Predicate[]::new)));
      }
      return entityManager.createQuery(query).getResultStream().map(ReferenceStationDao::toCoi)
        .collect(Collectors.toList());
    } finally {
      entityManager.close();
    }
  }

  @Override
  public List<ReferenceStation> retrieveStationsByName(List<String> names) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<ReferenceStationDao> query = cb.createQuery(ReferenceStationDao.class);
      Root<ReferenceStationDao> from = query.from(ReferenceStationDao.class);
      query.select(from);
      if (!names.isEmpty()) {
        query.where(cb.or(names.stream()
          .map(id -> cb.equal(from.get("name"), id))
          .toArray(Predicate[]::new)));
      }
      return entityManager.createQuery(query).getResultStream().map(ReferenceStationDao::toCoi)
        .collect(Collectors.toList());
    } finally {
      entityManager.close();
    }
  }

  @Override
  public void storeReferenceStation(Collection<ReferenceStation> stations) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      for (ReferenceStation station : stations) {
        if (referenceStationExists(station, entityManager)) {
          throw new DataExistsException(String
            .format("ReferenceStation name %s version %s", station.getName(),
              station.getVersionId()));
        }
      }
      referenceStationRepositoryUtility.persist(stations, entityManager);
    } catch (Exception ex) {
      throw new RuntimeException("Error storing stations", ex);
    } finally {
      entityManager.close();
    }
  }

  @Override
  public Map<UUID, List<ReferenceStationMembership>> retrieveStationMemberships(List<UUID> ids) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<ReferenceStationMembershipDao> query = cb
        .createQuery(ReferenceStationMembershipDao.class);
      Root<ReferenceStationMembershipDao> from = query.from(ReferenceStationMembershipDao.class);
      query.select(from);
      if (!ids.isEmpty()) {
        query.where(from.get("id").in(ids));
      }
      return entityManager.createQuery(query).getResultStream()
        .map(ReferenceStationMembershipDao::toCoi)
        .collect(Collectors.groupingBy(ReferenceStationMembership::getId));
    } finally {
      entityManager.close();
    }
  }

  @Override
  public Map<UUID, List<ReferenceStationMembership>> retrieveStationMembershipsByStationId(
    List<UUID> stationIds) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<ReferenceStationMembershipDao> query = cb
        .createQuery(ReferenceStationMembershipDao.class);
      Root<ReferenceStationMembershipDao> from = query.from(ReferenceStationMembershipDao.class);
      query.select(from);
      if (!stationIds.isEmpty()) {
        query.where(from.get("stationId").in(stationIds));
      }
      return entityManager.createQuery(query).getResultStream()
        .map(ReferenceStationMembershipDao::toCoi)
        .collect(Collectors.groupingBy(ReferenceStationMembership::getStationId));
    } finally {
      entityManager.close();
    }
  }

  @Override
  public Map<UUID, List<ReferenceStationMembership>> retrieveStationMembershipsBySiteId(
    List<UUID> siteIds) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<ReferenceStationMembershipDao> query = cb
        .createQuery(ReferenceStationMembershipDao.class);
      Root<ReferenceStationMembershipDao> from = query.from(ReferenceStationMembershipDao.class);
      query.select(from);
      if (!siteIds.isEmpty()) {
        query.where(from.get("siteId").in(siteIds));
      }
      return entityManager.createQuery(query).getResultStream()
        .map(ReferenceStationMembershipDao::toCoi)
        .collect(Collectors.groupingBy(ReferenceStationMembership::getSiteId));
    } finally {
      entityManager.close();
    }
  }

  @Override
  public List<ReferenceStationMembership> retrieveStationMembershipsByStationAndSiteId(
    ReferenceStationMembershipRequest request) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<ReferenceStationMembershipDao> query = cb
        .createQuery(ReferenceStationMembershipDao.class);
      Root<ReferenceStationMembershipDao> from = query.from(ReferenceStationMembershipDao.class);
      query.select(from).where(cb.and(
        cb.equal(from.get("siteId"), request.getSiteId()),
        cb.equal(from.get("stationId"), request.getStationId())));
      return entityManager.createQuery(query).getResultStream()
        .map(ReferenceStationMembershipDao::toCoi)
        .collect(Collectors.toList());
    } finally {
      entityManager.close();
    }
  }

  @Override
  public void storeStationMemberships(Collection<ReferenceStationMembership> memberships) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      for (ReferenceStationMembership stationMembership : memberships) {
        if (referenceStationMembershipExists(stationMembership, entityManager)) {
          throw new DataExistsException(String
            .format("ReferenceStationMembership Id %s", stationMembership.getId()));
        }
      }
      referenceStationMembershipRepositoryUtility.persist(memberships, entityManager);
    } catch (Exception ex) {
      throw new RuntimeException("Error storing stations", ex);
    } finally {
      entityManager.close();
    }

  }

  private boolean referenceStationExists(ReferenceStation station, EntityManager em) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<ReferenceStationDao> query = cb.createQuery(ReferenceStationDao.class);
    Root<ReferenceStationDao> from = query.from(ReferenceStationDao.class);
    query.select(from).where(cb.equal(from.get("versionId"), station.getVersionId()));
    List<ReferenceStationDao> res = em.createQuery(query).getResultList();
    return !res.isEmpty() && res.size() == 1;
  }

  private boolean referenceStationMembershipExists(ReferenceStationMembership stationMembership,
    EntityManager em) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<ReferenceStationMembershipDao> query = cb
      .createQuery(ReferenceStationMembershipDao.class);
    Root<ReferenceStationMembershipDao> from = query.from(ReferenceStationMembershipDao.class);
    query.select(from).where(cb.equal(from.get("id"), stationMembership.getId()));
    List<ReferenceStationMembershipDao> res = em.createQuery(query).getResultList();
    return !res.isEmpty() && res.size() == 1;
  }
}
