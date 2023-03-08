package gms.shared.frameworks.osd.repository.rawstationdataframe;

import gms.shared.frameworks.osd.api.rawstationdataframe.RawStationDataFrameRepositoryInterface;
import gms.shared.frameworks.osd.api.util.RepositoryExceptionUtils;
import gms.shared.frameworks.osd.api.util.StationTimeRangeRequest;
import gms.shared.frameworks.osd.api.util.TimeRangeRequest;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import gms.shared.frameworks.osd.dao.transferredfile.RawStationDataFrameDao;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class RawStationDataFrameRepositoryJpa implements RawStationDataFrameRepositoryInterface {

  private static final Logger logger = LoggerFactory.getLogger(RawStationDataFrameRepositoryJpa.class);

  public static final String PAYLOAD_DATA_END_TIME = "payloadDataEndTime";
  public static final String PAYLOAD_DATA_START_TIME = "payloadDataStartTime";
  public static final String STATION_NAME = "stationName";

  private final EntityManagerFactory entityManagerFactory;

  /**
   * Default constructor.
   */
  public RawStationDataFrameRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  /**
   * Stores {@link RawStationDataFrame}s
   *
   * @param frames Collection of {@link RawStationDataFrame}s to store
   * @throws gms.shared.frameworks.coi.exceptions.RepositoryException if there was an issue interacting with the repository
   */
  @Override
  public void storeRawStationDataFrames(Collection<RawStationDataFrame> frames) {
    Validate.notNull(frames, "Cannot store null RawStationDataFrames");

    var entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();
    try {
      TypedQuery<Integer> query = entityManager.createNamedQuery("RawStationDataFrameDao.exists", Integer.class);
      for (RawStationDataFrame rsdf : frames) {
        query.setParameter("id", rsdf.getId());
        var daoExists = query.getSingleResult();
        if (daoExists == 0) {
          var rsdfDao = new RawStationDataFrameDao(rsdf);
          entityManager.persist(rsdfDao);
        } else {
          logger.info("Duplicate rsdf {} found, dropping from batch store call", rsdf.getId());
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

  /**
   * Retrieve RawStationDataFrames that are within the specified time range and station name
   *
   * @param stationTimeRangeRequest request containing time range and station name to query by
   * @return List of RawStationDataFrames for specified time range and station name
   * @throws gms.shared.frameworks.coi.exceptions.RepositoryException if there was an issue interacting with the repository
   */
  @Override
  public List<RawStationDataFrame> retrieveRawStationDataFramesByStationAndTime(
    StationTimeRangeRequest stationTimeRangeRequest) {

    var entityManager = entityManagerFactory.createEntityManager();
    var criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<RawStationDataFrameDao> criteriaQuery =
      criteriaBuilder.createQuery(RawStationDataFrameDao.class);
    Root<RawStationDataFrameDao> rsdfRoot = criteriaQuery.from(RawStationDataFrameDao.class);

    var predicate = criteriaBuilder.and(
      criteriaBuilder.lessThanOrEqualTo(rsdfRoot.get(PAYLOAD_DATA_START_TIME),
        stationTimeRangeRequest.getTimeRange().getEndTime()),
      criteriaBuilder.greaterThanOrEqualTo(rsdfRoot.get(PAYLOAD_DATA_END_TIME),
        stationTimeRangeRequest.getTimeRange().getStartTime()),
      criteriaBuilder.equal(rsdfRoot.get(STATION_NAME),
        stationTimeRangeRequest.getStationName()));
    criteriaQuery.where(predicate).orderBy(
      criteriaBuilder.asc(rsdfRoot.get(STATION_NAME)),
      criteriaBuilder.asc(rsdfRoot.get(PAYLOAD_DATA_START_TIME)));

    try {
      return entityManager.createQuery(criteriaQuery).getResultStream()
        .map(RawStationDataFrameDao::toCoi).collect(Collectors.toList());
    } catch (PersistenceException e) {
      throw RepositoryExceptionUtils.wrap(e);
    } finally {
      entityManager.close();
    }
  }

  /**
   * Retrieve RawStationDataFrames that are within the specified time range
   *
   * @param timeRangeRequest request containing time range and station name to query by
   * @return List of RawStationDataFrames for specified time range
   * @throws gms.shared.frameworks.coi.exceptions.RepositoryException if there was an issue interacting with the repository
   */
  @Override
  public List<RawStationDataFrame> retrieveRawStationDataFramesByTime(
    TimeRangeRequest timeRangeRequest) {
    var entityManager = entityManagerFactory.createEntityManager();
    var criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<RawStationDataFrameDao> criteriaQuery =
      criteriaBuilder.createQuery(RawStationDataFrameDao.class);
    Root<RawStationDataFrameDao> rsdfRoot = criteriaQuery.from(RawStationDataFrameDao.class);

    var predicate = criteriaBuilder.and(
      criteriaBuilder.lessThanOrEqualTo(
        rsdfRoot.get(PAYLOAD_DATA_START_TIME), timeRangeRequest.getEndTime()),
      criteriaBuilder.greaterThanOrEqualTo(
        rsdfRoot.get(PAYLOAD_DATA_END_TIME), timeRangeRequest.getStartTime()));

    criteriaQuery.where(predicate).orderBy(
      criteriaBuilder.asc(rsdfRoot.get(STATION_NAME)),
      criteriaBuilder.asc(rsdfRoot.get(PAYLOAD_DATA_START_TIME)));

    try {
      return entityManager.createQuery(criteriaQuery).getResultStream()
        .map(RawStationDataFrameDao::toCoi).collect(Collectors.toList());
    } catch (PersistenceException e) {
      throw RepositoryExceptionUtils.wrap(e);
    } finally {
      entityManager.close();
    }
  }

}
