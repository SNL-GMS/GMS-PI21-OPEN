package gms.shared.stationdefinition.database.connector;

import com.google.common.base.Preconditions;
import gms.shared.stationdefinition.dao.css.SiteAndSurroundingDates;
import gms.shared.stationdefinition.dao.css.SiteDao;
import gms.shared.utilities.bridge.database.connector.DatabaseConnector;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Subquery;

@Component
public class SiteDatabaseConnector extends DatabaseConnector {

  private static final String ID = "id";
  private static final String STATION_CODE = "stationCode";
  private static final String REF_STATION = "referenceStation";
  private static final String ON_DATE = "onDate";
  private static final String OFF_DATE = "offDate";

  private static final Logger logger = LoggerFactory.getLogger(SiteDatabaseConnector.class);

  static final String MISSING_STATION_NAME_LIST_ERROR = "Request for Site given empty list of station names.";
  static final String MISSING_START_TIME_ERROR = "Request for Site given empty start time.";
  private static final String MISSING_END_TIME_ERROR = "Request for Site given empty end time.";

  public SiteDatabaseConnector(@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
    super(entityManagerFactory);
  }

  public List<SiteDao> findSitesByStationCodes(Collection<String> stationNames) {

    Validate.notNull(stationNames, MISSING_STATION_NAME_LIST_ERROR);

    if (stationNames.isEmpty()) {
      logger.debug(MISSING_STATION_NAME_LIST_ERROR);
      return new ArrayList<>();
    } else {
      return runWithEntityManager(entityManager
        -> runPartitionedQuery(stationNames, 950, partition -> {

          CriteriaBuilder cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<SiteDao> query = cb.createQuery(SiteDao.class);
          Root<SiteDao> fromSite = query.from(SiteDao.class);
          query.select(fromSite);

          query.where(fromSite.get(ID).get(STATION_CODE).in(partition));
          return entityManager.createQuery(query).getResultList();
        })
      );
    }
  }

  public List<SiteDao> findSitesByRefStation(Collection<String> refStationNames) {

    Validate.notNull(refStationNames, MISSING_STATION_NAME_LIST_ERROR);

    if (refStationNames.isEmpty()) {
      logger.debug(MISSING_STATION_NAME_LIST_ERROR);
      return new ArrayList<>();
    } else {
      return runWithEntityManager(entityManager
        -> runPartitionedQuery(refStationNames, 950, partition -> {

          CriteriaBuilder cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<SiteDao> query = cb.createQuery(SiteDao.class);
          Root<SiteDao> fromSite = query.from(SiteDao.class);
          query.select(fromSite);

          query.where(fromSite.get(REF_STATION).in(partition));
          return entityManager.createQuery(query).getResultList();
        })
      );
    }
  }

  //this should validate/query for the refStation as well...ie: the refSta could be wrong, but we are only looking at the sta name
  public List<SiteDao> findSitesByStationCodesAndStartTime(Collection<String> stationNames,
    Instant start) {

    Validate.notNull(stationNames, MISSING_STATION_NAME_LIST_ERROR);
    Validate.notNull(start, MISSING_START_TIME_ERROR);

    if (stationNames.isEmpty()) {
      logger.debug(MISSING_STATION_NAME_LIST_ERROR);
      return new ArrayList<>();
    } else {
      return runWithEntityManager(entityManager
        -> runPartitionedQuery(stationNames, 950, partition -> {

          CriteriaBuilder cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<SiteDao> query = cb.createQuery(SiteDao.class);
          Root<SiteDao> fromSite = query.from(SiteDao.class);
          query.select(fromSite);

          query.where(cb.and(
            fromSite.get(ID).get(STATION_CODE).in(partition),
            cb.lessThanOrEqualTo(fromSite.get(ID).get(ON_DATE), start),
            cb.greaterThanOrEqualTo(fromSite.get(OFF_DATE), start)));
          return entityManager.createQuery(query).getResultList();
        })
      );
    }
  }

  public List<SiteDao> findSitesByRefStationAndStartTime(Collection<String> stationNames,
    Instant start) {

    Validate.notNull(stationNames, MISSING_STATION_NAME_LIST_ERROR);
    Validate.notNull(start, MISSING_START_TIME_ERROR);

    if (stationNames.isEmpty()) {
      logger.debug(MISSING_STATION_NAME_LIST_ERROR);
      return new ArrayList<>();
    } else {
      return runWithEntityManager(entityManager
        -> runPartitionedQuery(stationNames, 200, partition -> {

          CriteriaBuilder cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<SiteDao> query = cb.createQuery(SiteDao.class);
          Root<SiteDao> fromSite = query.from(SiteDao.class);
          query.select(fromSite);

          query.where(cb.and(
            fromSite.get(REF_STATION).in(partition),
            cb.lessThanOrEqualTo(fromSite.get(ID).get(ON_DATE), start),
            cb.greaterThanOrEqualTo(fromSite.get(OFF_DATE), start)
          ));
          return entityManager.createQuery(query).getResultList();
        })
      );
    }

  }

  public List<SiteDao> findMainSitesByRefStaAndTime(Collection<String> stationNames, Instant start) {
    Objects.requireNonNull(stationNames);
    Objects.requireNonNull(start);

    if (stationNames.isEmpty()) {
      logger.debug(MISSING_STATION_NAME_LIST_ERROR);
      return List.of();
    } else {
      return runWithEntityManager(entityManager
        -> runPartitionedQuery(stationNames, 200, partition -> {

          CriteriaBuilder cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<SiteDao> query = cb.createQuery(SiteDao.class);
          Root<SiteDao> fromSite = query.from(SiteDao.class);
          query.select(fromSite);

          query.where(cb.and(
            fromSite.get(REF_STATION).in(partition),
            fromSite.get(ID).get(STATION_CODE).in(partition),
            cb.lessThanOrEqualTo(fromSite.get(ID).get(ON_DATE), start),
            cb.greaterThanOrEqualTo(fromSite.get(OFF_DATE), start)
          ));
          return entityManager.createQuery(query).getResultList();
        }));
    }
  }

  public List<SiteDao> findSitesByReferenceStationAndTimeRange(List<String> referenceStations,
    Instant startTime,
    Instant endTime) {

    Validate.notNull(referenceStations, MISSING_STATION_NAME_LIST_ERROR);
    Validate.notNull(startTime, MISSING_START_TIME_ERROR);
    Validate.notNull(endTime, MISSING_END_TIME_ERROR);
    Preconditions.checkState(startTime.isBefore(endTime), "Start time cannot be after end time");

    if (referenceStations.isEmpty()) {
      logger.debug(MISSING_STATION_NAME_LIST_ERROR);
      return List.of();
    } else {
      return runWithEntityManager(entityManager
        -> runPartitionedQuery(referenceStations, 900, referenceStationsSubList -> {
          CriteriaBuilder builder = entityManager.getCriteriaBuilder();
          CriteriaQuery<SiteDao> query = builder.createQuery(SiteDao.class);
          Root<SiteDao> fromSite = query.from(SiteDao.class);

          query.select(fromSite)
            .where(builder.and(fromSite.get(REF_STATION).in(referenceStationsSubList),
              builder.lessThanOrEqualTo(fromSite.get(ID).get(ON_DATE), endTime),
              builder.greaterThanOrEqualTo(fromSite.get(OFF_DATE), startTime)));

          return entityManager.createQuery(query).getResultList();
        }));
    }
  }

  public List<SiteDao> findSitesByNamesAndTimeRange(Collection<String> stationNames,
    Instant startTime, Instant endTime) {

    Validate.notNull(stationNames, MISSING_STATION_NAME_LIST_ERROR);
    Validate.notNull(startTime, MISSING_START_TIME_ERROR);
    Validate.notNull(endTime, MISSING_END_TIME_ERROR);

    if (stationNames.isEmpty()) {
      logger.debug(MISSING_STATION_NAME_LIST_ERROR);
      return new ArrayList<>();
    } else {
      return runWithEntityManager(entityManager
        -> runPartitionedQuery(stationNames, 950, partition -> {

          CriteriaBuilder cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<SiteDao> query = cb.createQuery(SiteDao.class);
          Root<SiteDao> fromSite = query.from(SiteDao.class);
          query.select(fromSite);

          query.where(cb.and(
            fromSite.get(ID).get(STATION_CODE).in(partition),
            cb.greaterThanOrEqualTo(fromSite.get(OFF_DATE), startTime),
            cb.lessThanOrEqualTo(fromSite.get(ID).get(ON_DATE), endTime)
          ));
          return entityManager.createQuery(query).getResultList();
        })
      );
    }

  }

  public List<SiteDao> findSitesByTimeRange(Instant startTime, Instant endTime) {
    Validate.notNull(startTime, MISSING_START_TIME_ERROR);
    Validate.notNull(endTime, MISSING_END_TIME_ERROR);

    return runWithEntityManager(entityManager -> {

      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<SiteDao> query = cb.createQuery(SiteDao.class);
      Root<SiteDao> fromSite = query.from(SiteDao.class);
      query.select(fromSite);

      query.where(cb.and(
        cb.greaterThanOrEqualTo(fromSite.get(OFF_DATE), startTime),
        cb.lessThanOrEqualTo(fromSite.get(ID).get(ON_DATE), endTime)
      ));
      return entityManager.createQuery(query).getResultList();
    });

  }



  public List<SiteAndSurroundingDates> findSitesAndSurroundingDatesByRefStaAndTime(
    Collection<String> refStas, Instant effectiveAt) {

    Validate.notNull(refStas, MISSING_STATION_NAME_LIST_ERROR);
    Validate.notNull(effectiveAt, MISSING_START_TIME_ERROR);

    if (refStas.isEmpty()) {
      logger.debug(MISSING_STATION_NAME_LIST_ERROR);
      return new ArrayList<>();
    } else {
      return runWithEntityManager(entityManager
        -> runPartitionedQuery(refStas, 950, partition -> {
          var cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<SiteAndSurroundingDates> siteQuery = cb
            .createQuery(SiteAndSurroundingDates.class);

          // site dao query from the main site and surrounding dates
          Root<SiteDao> fromSite = siteQuery.from(SiteDao.class);
          Path<String> staCodePath = fromSite.get(REF_STATION);

          var predicate = cb.and(staCodePath.in(partition),
            cb.lessThanOrEqualTo(fromSite.get(ID).get(ON_DATE), effectiveAt),
            cb.greaterThanOrEqualTo(fromSite.get(OFF_DATE), effectiveAt));

          var finalQuery = getSiteAndSurroundingDatesQuery(predicate, cb, siteQuery, fromSite);

          return entityManager.createQuery(finalQuery).getResultList();
        }));
    }
  }
  
    public List<SiteAndSurroundingDates> findSitesAndSurroundingDatesByRefStaAndTimeRange(
    Collection<String> refStas, Instant startTime, Instant endTime) {

    Validate.notNull(refStas, MISSING_STATION_NAME_LIST_ERROR);
    Validate.notNull(startTime, MISSING_START_TIME_ERROR);
    Validate.notNull(endTime, MISSING_END_TIME_ERROR);

    if (refStas.isEmpty()) {
      logger.debug(MISSING_STATION_NAME_LIST_ERROR);
      return new ArrayList<>();
    } else {
      return runWithEntityManager(entityManager
        -> runPartitionedQuery(refStas, 950, partition -> {
          var cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<SiteAndSurroundingDates> siteQuery = cb
            .createQuery(SiteAndSurroundingDates.class);

          // site dao query from the main site and surrounding dates
          Root<SiteDao> fromSite = siteQuery.from(SiteDao.class);
          Path<String> staCodePath = fromSite.get(REF_STATION);

          var predicate = cb.and(staCodePath.in(partition),
            cb.greaterThanOrEqualTo(fromSite.get(OFF_DATE), startTime),
            cb.lessThanOrEqualTo(fromSite.get(ID).get(ON_DATE), endTime));

          var finalQuery = getSiteAndSurroundingDatesQuery(predicate, cb, siteQuery, fromSite);

          return entityManager.createQuery(finalQuery).getResultList();
        }));
    }
  }

  public List<SiteAndSurroundingDates> findSitesAndSurroundingDatesByStaCodeAndTime(
    Collection<String> stationCodes, Instant effectiveAt) {

    Validate.notNull(stationCodes, MISSING_STATION_NAME_LIST_ERROR);
    Validate.notNull(effectiveAt, MISSING_START_TIME_ERROR);

    if (stationCodes.isEmpty()) {
      logger.debug(MISSING_STATION_NAME_LIST_ERROR);
      return new ArrayList<>();
    } else {
      return runWithEntityManager(entityManager
        -> runPartitionedQuery(stationCodes, 950, partition -> {
          var cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<SiteAndSurroundingDates> siteQuery = cb
            .createQuery(SiteAndSurroundingDates.class);

          // site dao query from the main site and surrounding dates
          Root<SiteDao> fromSite = siteQuery.from(SiteDao.class);
          Path<String> staCodePath = fromSite.get(ID).get(STATION_CODE);

          var predicate = cb.and(staCodePath.in(partition),
            cb.lessThanOrEqualTo(fromSite.get(ID).get(ON_DATE), effectiveAt),
            cb.greaterThanOrEqualTo(fromSite.get(OFF_DATE), effectiveAt));

          var finalQuery = getSiteAndSurroundingDatesQuery(predicate, cb, siteQuery, fromSite);

          return entityManager.createQuery(finalQuery).getResultList();
        }));
    }
  }
  
    public List<SiteAndSurroundingDates> findSitesAndSurroundingDatesByStaCodeAndTimeRange(
    Collection<String> stationCodes, Instant startTime, Instant endTime) {

    Validate.notNull(stationCodes, MISSING_STATION_NAME_LIST_ERROR);
    Validate.notNull(startTime, MISSING_START_TIME_ERROR);
    Validate.notNull(endTime, MISSING_END_TIME_ERROR);

    if (stationCodes.isEmpty()) {
      logger.debug(MISSING_STATION_NAME_LIST_ERROR);
      return new ArrayList<>();
    } else {
      return runWithEntityManager(entityManager
        -> runPartitionedQuery(stationCodes, 950, partition -> {
          var cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<SiteAndSurroundingDates> siteQuery = cb
            .createQuery(SiteAndSurroundingDates.class);

          // site dao query from the main site and surrounding dates
          Root<SiteDao> fromSite = siteQuery.from(SiteDao.class);
          Path<String> staCodePath = fromSite.get(ID).get(STATION_CODE);

          var predicate = cb.and(staCodePath.in(partition),
            cb.greaterThanOrEqualTo(fromSite.get(OFF_DATE), startTime),
            cb.lessThanOrEqualTo(fromSite.get(ID).get(ON_DATE), endTime));

          var finalQuery = getSiteAndSurroundingDatesQuery(predicate, cb, siteQuery, fromSite);

          return entityManager.createQuery(finalQuery).getResultList();
        }));
    }
  }

  private CriteriaQuery<SiteAndSurroundingDates> getSiteAndSurroundingDatesQuery(
    Predicate predicate,
    CriteriaBuilder cb,
    CriteriaQuery<SiteAndSurroundingDates> siteQuery,
    Root<SiteDao> fromSite) {

    // previous off date query to return the greatest previous off date
    Subquery<Instant> previousOffDateQuery = siteQuery.subquery(Instant.class);
    Root<SiteDao> subFromSiteOffDate = previousOffDateQuery.from(SiteDao.class);
    previousOffDateQuery.select(cb.greatest(subFromSiteOffDate.<Instant>get(OFF_DATE)))
      .where(cb.and(
        cb.equal(subFromSiteOffDate.get(REF_STATION), fromSite.get(REF_STATION)),
        cb.equal(subFromSiteOffDate.get(ID).get(STATION_CODE), fromSite.get(ID).get(STATION_CODE)),
        cb.lessThanOrEqualTo(subFromSiteOffDate.get(OFF_DATE), fromSite.get(ID).get(ON_DATE))));

    // next on date query to return the least next on date 
    Subquery<Instant> nextOnDateQuery = siteQuery.subquery(Instant.class);
    Root<SiteDao> subFromSiteOnDate = nextOnDateQuery.from(SiteDao.class);
    nextOnDateQuery.select(cb.least(subFromSiteOnDate.get(ID).<Instant>get(ON_DATE)))
      .where(cb.and(
        cb.equal(subFromSiteOnDate.get(REF_STATION), fromSite.get(REF_STATION)),
        cb.equal(subFromSiteOnDate.get(ID).get(STATION_CODE), fromSite.get(ID).get(STATION_CODE)),
        cb.greaterThanOrEqualTo(subFromSiteOnDate.get(ID).get(ON_DATE), fromSite.get(OFF_DATE))));

    // main site query select that implements all sub queries 
    return siteQuery.select(cb.construct(SiteAndSurroundingDates.class,
      fromSite.as(SiteDao.class),
      previousOffDateQuery.getSelection(),
      nextOnDateQuery.getSelection()))
      .where(predicate);
  }

}
