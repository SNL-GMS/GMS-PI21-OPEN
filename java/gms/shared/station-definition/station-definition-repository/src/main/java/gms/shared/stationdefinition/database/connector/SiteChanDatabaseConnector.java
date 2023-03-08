package gms.shared.stationdefinition.database.connector;

import com.google.common.base.Preconditions;
import gms.shared.stationdefinition.dao.css.SiteChanAndSurroundingDates;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteChanKey;
import gms.shared.utilities.bridge.database.connector.DatabaseConnector;
import gms.shared.utilities.bridge.database.connector.DatabaseConnectorException;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SiteChanDatabaseConnector extends DatabaseConnector {

  private static final String ID = "id";
  private static final String STATION_CODE = "stationCode";
  private static final String CHANNEL_CODE = "channelCode";
  private static final String ON_DATE = "onDate";
  private static final String OFF_DATE = "offDate";

  private static final Logger logger = LoggerFactory.getLogger(SiteChanDatabaseConnector.class);

  private static final String MISSING_KEY_SET_ERROR = "Request for SiteChan by SiteChanKey was must be given a list of keys";
  private static final String MISSING_END_TIME_ERROR = "Request for SiteChan by time range was must be given a end time";
  static final String MISSING_START_TIME_ERROR = "Request for SiteChan by time range was must be given a start time";
  static final String MISSING_STATION_CODES_ERROR = "Request for SiteChan by station codes was must be given a list of station codes";

  public SiteChanDatabaseConnector(@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {

    super(entityManagerFactory);
  }

  public Optional<SiteChanDao> findSiteChan(SiteChanKey siteChanKey) {
    Validate.notNull(siteChanKey, "Request for SiteChan by SiteChanKey was must be given a key");

    final var stationCode = siteChanKey.getStationCode();
    final var channelCode = siteChanKey.getChannelCode();
    final var onDate = siteChanKey.getOnDate();

    return runWithEntityManager(entityManager -> {

      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<SiteChanDao> query = cb.createQuery(SiteChanDao.class);
      Root<SiteChanDao> fromSiteChan = query.from(SiteChanDao.class);

      query.select(fromSiteChan);

      final Path<Object> channelId = fromSiteChan.get(ID);
      query.where(cb.and(
        cb.equal(channelId.get(STATION_CODE), stationCode),
        cb.equal(channelId.get(CHANNEL_CODE), channelCode),
        cb.equal(channelId.get(ON_DATE), onDate)
      ));

      try {
        return Optional.of(entityManager.createQuery(query).getSingleResult());
      }
      catch (NoResultException e) {
        final String message = String
          .format("No SiteChan Found for '%s, %s, %s'", stationCode, channelCode, onDate);
        logger.warn(message, e);
        return Optional.empty();
      }
      catch (NonUniqueResultException e) {
        final String message = String
          .format("No Unique SiteChan Found for '%s, %s, %s'", stationCode, channelCode,
            onDate);
        logger.warn(message, e);
        return Optional.empty();
      }
      catch (Exception e) {
        final String message = String
          .format("Error Retrieving SiteChan For '%s, %s, %s'", stationCode, channelCode,
            onDate);
        throw new DatabaseConnectorException(message, e);
      }
    });
  }

  public List<SiteChanDao> findSiteChansByStationCodeAndTime(Collection<String> stationCodes,
    Instant effectiveTime) {
    Objects.requireNonNull(stationCodes);
    Objects.requireNonNull(effectiveTime);

    if (stationCodes.isEmpty()) {
      logger.debug("Request for SiteChan by name was given an empty list of station codes");
      return new ArrayList<>();
    } else {
      return runWithEntityManager(entityManager
        -> runPartitionedQuery(stationCodes, 950, partition -> {
          CriteriaBuilder builder = entityManager.getCriteriaBuilder();
          CriteriaQuery<SiteChanDao> query = builder.createQuery(SiteChanDao.class
          );
          Root<SiteChanDao> fromSiteChan = query.from(SiteChanDao.class
          );
          query.select(fromSiteChan)
            .where(builder.and(fromSiteChan.get(ID).get(STATION_CODE).in(partition),
              builder.lessThanOrEqualTo(fromSiteChan.get(ID).get(ON_DATE), effectiveTime),
              builder.greaterThanOrEqualTo(fromSiteChan.get(OFF_DATE), effectiveTime)));

          return entityManager.createQuery(query).getResultList();
        })
      );
    }
  }

  public List<SiteChanDao> findSiteChansByStationCodeAndTimeRange(Collection<String> stationCodes,
    Instant startTime,
    Instant endTime) {

    Objects.requireNonNull(stationCodes, MISSING_STATION_CODES_ERROR);
    Objects.requireNonNull(startTime);
    Objects.requireNonNull(endTime);
    Preconditions.checkState(endTime.isAfter(startTime));

    if (stationCodes.isEmpty()) {
      logger.debug(MISSING_STATION_CODES_ERROR);
      return new ArrayList<>();
    } else {
      return runWithEntityManager(entityManager
        -> runPartitionedQuery(stationCodes, 400, partition -> {

          CriteriaBuilder cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<SiteChanDao> query = cb.createQuery(SiteChanDao.class
          );
          Root<SiteChanDao> fromSiteChan = query.from(SiteChanDao.class
          );

          final Path<Object> idPath = fromSiteChan.get(ID);
          query.select(fromSiteChan);
          query.where(cb.and(idPath.get(STATION_CODE).in(partition),
            cb.greaterThanOrEqualTo(fromSiteChan.get(OFF_DATE), startTime),
            cb.lessThanOrEqualTo(idPath.get(ON_DATE), endTime)));

          return entityManager.createQuery(query).getResultList();
        }));
    }
  }

  public List<SiteChanDao> findSiteChansByNameAndTimeRange(Collection<SiteChanKey> siteChanKeys,
    Instant startTime, Instant endTime) {

    Validate.notNull(siteChanKeys, MISSING_KEY_SET_ERROR);
    Validate.notNull(startTime, MISSING_START_TIME_ERROR);
    Validate.notNull(endTime, MISSING_END_TIME_ERROR);

    if (siteChanKeys.isEmpty()) {
      logger.debug(MISSING_KEY_SET_ERROR);
      return new ArrayList<>();
    } else {
      return runWithEntityManager(entityManager
        -> runPartitionedQuery(siteChanKeys, 250, keySubList -> {

          CriteriaBuilder cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<SiteChanDao> query = cb.createQuery(SiteChanDao.class
          );
          Root<SiteChanDao> fromSiteChan = query.from(SiteChanDao.class
          );

          final Path<Object> idPath = fromSiteChan.get(ID);
          query.select(fromSiteChan);
          query.where(
            cb.or(
              keySubList.stream()
                .map(k -> cb.and(
                cb.equal(idPath.get(STATION_CODE), k.getStationCode()),
                cb.equal(idPath.get(CHANNEL_CODE), k.getChannelCode()),
                cb.greaterThanOrEqualTo(fromSiteChan.get(OFF_DATE), startTime),
                cb.lessThanOrEqualTo(idPath.get(ON_DATE), endTime)
              ))
                .toArray(Predicate[]::new)
            ));

          return entityManager.createQuery(query).getResultList();
        }));
    }
  }

  public List<SiteChanDao> findSiteChansByTimeRange(Instant startTime, Instant endTime) {

    Validate.notNull(startTime, MISSING_START_TIME_ERROR);
    Validate.notNull(endTime, MISSING_END_TIME_ERROR);

    return runWithEntityManager(entityManager -> {

      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<SiteChanDao> query = cb.createQuery(SiteChanDao.class
      );
      Root<SiteChanDao> fromSiteChan = query.from(SiteChanDao.class
      );

      final Path<Object> idPath = fromSiteChan.get(ID);
      query.select(fromSiteChan);
      query.where(cb.and(
        cb.greaterThanOrEqualTo(fromSiteChan.get(OFF_DATE), startTime),
        cb.lessThanOrEqualTo(idPath.get(ON_DATE), endTime)
      ));

      return entityManager.createQuery(query).getResultList();
    });
  }

  public List<SiteChanDao> findSiteChansByKeyAndTime(List<SiteChanKey> siteChanKeys, Instant effectiveAt) {
    Validate.notNull(siteChanKeys);
    Validate.notNull(effectiveAt);

    if (siteChanKeys.isEmpty()) {
      logger.debug(MISSING_KEY_SET_ERROR);
      return List.of();
    }

    return runWithEntityManager(entityManager
      -> runPartitionedQuery(siteChanKeys, 500, keySublist -> {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SiteChanDao> siteChanQuery = builder.createQuery(SiteChanDao.class
        );
        Root<SiteChanDao> fromSiteChan = siteChanQuery.from(SiteChanDao.class
        );
        Path<SiteChanKey> id = fromSiteChan.get(ID);
        siteChanQuery.select(fromSiteChan)
          .where(builder.and(
            builder.or(keySublist.stream()
              .map(key -> builder.and(builder.equal(id.get(STATION_CODE), key.getStationCode()),
              builder.equal(id.get(CHANNEL_CODE), key.getChannelCode())))
              .toArray(Predicate[]::new))),
            builder.lessThanOrEqualTo(id.get(ON_DATE), effectiveAt),
            builder.greaterThanOrEqualTo(fromSiteChan.get(OFF_DATE), effectiveAt));

        return entityManager.createQuery(siteChanQuery).getResultList();
      })
    );
  }

  //SiteChanAndSurroundingDates queries
  public List<SiteChanAndSurroundingDates> findSiteChansAndSurroundingDatesByStationCodeAndTime(
    Collection<String> stationCodes, Instant effectiveAt) {
    Validate.notNull(stationCodes, MISSING_STATION_CODES_ERROR);
    Validate.notNull(effectiveAt, MISSING_START_TIME_ERROR);

    if (stationCodes.isEmpty()) {
      logger.debug(MISSING_STATION_CODES_ERROR);
      return new ArrayList<>();
    } else {
      return runWithEntityManager(entityManager
        -> runPartitionedQuery(stationCodes, 950, partition -> {

          var cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<SiteChanAndSurroundingDates> siteChanQuery = cb
            .createQuery(SiteChanAndSurroundingDates.class);

          // site chan dao query from main site chan and surrounding dates
          Root<SiteChanDao> fromSiteChan = siteChanQuery.from(SiteChanDao.class);

          var predicate = cb.and(fromSiteChan.get(ID).get(STATION_CODE).in(partition),
            cb.lessThanOrEqualTo(fromSiteChan.get(ID).get(ON_DATE), effectiveAt),
            cb.greaterThanOrEqualTo(fromSiteChan.get(OFF_DATE), effectiveAt));

          var finalQuery = getStationCodeQuery(predicate, cb, siteChanQuery, fromSiteChan);

          return entityManager.createQuery(finalQuery).getResultList();
        }));
    }
  }

  public List<SiteChanAndSurroundingDates> findSiteChansAndSurroundingDatesByStationCodeAndTimeRange(
    Collection<String> stationCodes, Instant startTime, Instant endTime) {
    Validate.notNull(stationCodes, MISSING_STATION_CODES_ERROR);
    Validate.notNull(startTime, MISSING_START_TIME_ERROR);
    Validate.notNull(endTime, MISSING_END_TIME_ERROR);

    if (stationCodes.isEmpty()) {
      logger.debug(MISSING_STATION_CODES_ERROR);
      return new ArrayList<>();
    } else {
      return runWithEntityManager(entityManager
        -> runPartitionedQuery(stationCodes, 950, partition -> {

          var cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<SiteChanAndSurroundingDates> siteChanQuery = cb
            .createQuery(SiteChanAndSurroundingDates.class);

          // site chan dao query from main site chan and surrounding dates
          Root<SiteChanDao> fromSiteChan = siteChanQuery.from(SiteChanDao.class);

          var predicate = cb.and(fromSiteChan.get(ID).get(STATION_CODE).in(partition),
          cb.greaterThanOrEqualTo(fromSiteChan.get(OFF_DATE), startTime),
          cb.lessThanOrEqualTo(fromSiteChan.get(ID).get(ON_DATE), endTime));

          var finalQuery = getStationCodeQuery(predicate, cb, siteChanQuery, fromSiteChan);

          return entityManager.createQuery(finalQuery).getResultList();
        }));
    }
  }

  public List<SiteChanAndSurroundingDates> findSiteChansAndSurroundingDatesByKeysAndTime(
    Collection<SiteChanKey> siteChanKeys, Instant effectiveAt) {

    Validate.notNull(siteChanKeys, MISSING_KEY_SET_ERROR);
    Validate.notNull(effectiveAt, MISSING_START_TIME_ERROR);

    if (siteChanKeys.isEmpty()) {
      logger.debug(MISSING_KEY_SET_ERROR);
      return new ArrayList<>();
    }

    return runWithEntityManager(entityManager
      -> runPartitionedQuery(siteChanKeys, 950, partition -> {

        var cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SiteChanAndSurroundingDates> siteChanQuery = cb
          .createQuery(SiteChanAndSurroundingDates.class);
        Root<SiteChanDao> fromSiteChan = siteChanQuery.from(SiteChanDao.class);

        List<String> staCodes = partition.stream()
          .map(SiteChanKey::getStationCode)
          .collect(Collectors.toList());

        List<String> chanCodes = partition.stream()
          .map(SiteChanKey::getChannelCode)
          .collect(Collectors.toList());

        var predicate = cb.and(fromSiteChan.get(ID).get(STATION_CODE).in(staCodes),
          fromSiteChan.get(ID).get(CHANNEL_CODE).in(chanCodes),
          cb.lessThanOrEqualTo(fromSiteChan.get(ID).get(ON_DATE), effectiveAt),
          cb.greaterThanOrEqualTo(fromSiteChan.get(OFF_DATE), effectiveAt));

        // main sitechan query select that implements all sub queries
        var finalQuery = getStationCodeQuery(predicate, cb, siteChanQuery, fromSiteChan);

        return entityManager.createQuery(finalQuery).getResultList();
      }));
  }

  public List<SiteChanAndSurroundingDates> findSiteChansAndSurroundingDatesByKeysAndTimeRange(
    Collection<SiteChanKey> siteChanKeys, Instant startTime, Instant endTime) {

    Validate.notNull(siteChanKeys, MISSING_KEY_SET_ERROR);
    Validate.notNull(startTime, MISSING_START_TIME_ERROR);
    Validate.notNull(endTime, MISSING_END_TIME_ERROR);

    if (siteChanKeys.isEmpty()) {
      logger.debug(MISSING_KEY_SET_ERROR);
      return new ArrayList<>();
    }

    return runWithEntityManager(entityManager
      -> runPartitionedQuery(siteChanKeys, 950, partition -> {

        var cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SiteChanAndSurroundingDates> siteChanQuery = cb
          .createQuery(SiteChanAndSurroundingDates.class);
        // site chan dao query from main site chan and surrounding dates
        Root<SiteChanDao> fromSiteChan = siteChanQuery.from(SiteChanDao.class);

        List<String> staCodes = partition.stream()
          .map(SiteChanKey::getStationCode)
          .collect(Collectors.toList());

        List<String> chanCodes = partition.stream()
          .map(SiteChanKey::getChannelCode)
          .collect(Collectors.toList());

        var predicate = cb.and(fromSiteChan.get(ID).get(STATION_CODE).in(staCodes),
          fromSiteChan.get(ID).get(CHANNEL_CODE).in(chanCodes),
          cb.greaterThanOrEqualTo(fromSiteChan.get(OFF_DATE), startTime),
          cb.lessThanOrEqualTo(fromSiteChan.get(ID).get(ON_DATE), endTime));

        var finalQuery = getStationCodeQuery(predicate, cb, siteChanQuery, fromSiteChan);

        return entityManager.createQuery(finalQuery).getResultList();
      }));
  }

  private CriteriaQuery<SiteChanAndSurroundingDates> getStationCodeQuery(
    Predicate predicate,
    CriteriaBuilder cb,
    CriteriaQuery<SiteChanAndSurroundingDates> siteChanQuery,
    Root<SiteChanDao> fromSiteChan) {

    // previous off date query to return greatest previous off date
    Subquery<Instant> previousOffDateQuery = siteChanQuery.subquery(Instant.class);
    Root<SiteChanDao> subFromSiteChanOffDate = previousOffDateQuery.from(SiteChanDao.class);
    previousOffDateQuery.select(subFromSiteChanOffDate.<Instant>get(OFF_DATE))
      .where(cb.and(
        cb.equal(subFromSiteChanOffDate.get(ID).get(STATION_CODE), fromSiteChan.get(ID).get(STATION_CODE)),
        cb.equal(subFromSiteChanOffDate.get(ID).get(CHANNEL_CODE), fromSiteChan.get(ID).get(CHANNEL_CODE)),
        cb.equal(subFromSiteChanOffDate.get(OFF_DATE), fromSiteChan.get(ID).get(ON_DATE))));

    // next on date query to return the least next on date
    Subquery<Instant> nextOnDateQuery = siteChanQuery.subquery(Instant.class);
    Root<SiteChanDao> subFromSiteChanOnDate = nextOnDateQuery.from(SiteChanDao.class);
    nextOnDateQuery.select(subFromSiteChanOnDate.get(ID).<Instant>get(ON_DATE))
      .where(cb.and(
        cb.equal(subFromSiteChanOnDate.get(ID).get(STATION_CODE), fromSiteChan.get(ID).get(STATION_CODE)),
        cb.equal(subFromSiteChanOnDate.get(ID).get(CHANNEL_CODE), fromSiteChan.get(ID).get(CHANNEL_CODE)),
        cb.equal(subFromSiteChanOnDate.get(ID).get(ON_DATE), fromSiteChan.get(OFF_DATE))));

    // main sitechan query select that implements all sub queries
    return siteChanQuery
      .select(cb.construct(SiteChanAndSurroundingDates.class,
        fromSiteChan.as(SiteChanDao.class),
        previousOffDateQuery.getSelection(),
        nextOnDateQuery.getSelection()))
      .where(predicate);
  }

}
