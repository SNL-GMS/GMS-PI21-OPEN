package gms.shared.stationdefinition.database.connector;

import com.google.common.base.Preconditions;
import gms.shared.stationdefinition.dao.css.SensorDao;
import gms.shared.stationdefinition.dao.css.SensorKey;
import gms.shared.stationdefinition.dao.css.SiteChanKey;
import gms.shared.utilities.bridge.database.connector.DatabaseConnector;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
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

@Component
public class SensorDatabaseConnector extends DatabaseConnector {

  private static final String SENSOR_KEY = "sensorKey";
  private static final String STATION = "station";
  private static final String CHANNEL = "channel";
  private static final String CHANNEL_ID = "channelId";
  private static final String TIME = "time";
  private static final String END_TIME = "endTime";
  private static final String T_SHIFT = "tShift";

  private static final Logger logger = LoggerFactory.getLogger(SensorDatabaseConnector.class);

  public SensorDatabaseConnector(@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {

    super(entityManagerFactory);
  }

  public Optional<SensorDao> findSensorByKeyInRange(String station,
    String channel,
    Instant startTime,
    Instant endTime) {

    Objects.requireNonNull(station, "Station cannot be null");
    Objects.requireNonNull(channel, "Channel cannot be null");
    Objects.requireNonNull(startTime, "Start time cannot be null");
    Objects.requireNonNull(endTime, "End time cannot be null");
    Preconditions.checkState(!startTime.isAfter(endTime), "Start time cannot be after end time");

    return runWithEntityManager(entityManager -> {
      var builder = entityManager.getCriteriaBuilder();
      var query = builder.createQuery(SensorDao.class);
      var fromSensor = query.from(SensorDao.class);
      var sensorKey = fromSensor.get(SENSOR_KEY);
      query.select(fromSensor)
        .where(builder.and(
          builder.equal(sensorKey.get(STATION), station),
          builder.equal(sensorKey.get(CHANNEL), channel),
          builder.or(builder.lessThanOrEqualTo(sensorKey.get(TIME), endTime),
            builder.greaterThanOrEqualTo(sensorKey.get(END_TIME), startTime))));
      query.orderBy(builder.desc(sensorKey.get(TIME)));

      try {
        return Optional.ofNullable(entityManager.createQuery(query).setMaxResults(1).getSingleResult());
      } catch (NoResultException ex) {
        return Optional.empty();
      }
    });
  }

  public List<SensorDao> findSensorsByChannelIdAndTimeRange(Collection<Long> channelIds, Instant startTime,
    Instant endTime) {
    Objects.requireNonNull(channelIds);
    Objects.requireNonNull(startTime);
    Objects.requireNonNull(endTime);
    Preconditions.checkState(startTime.isBefore(endTime));

    if (channelIds.isEmpty()) {
      logger.debug("Request for Sensor by name was given an empty list of channel ids");
      return List.of();
    } else {
      return runWithEntityManager(entityManager ->
        runPartitionedQuery(channelIds, 950, channelIdSublist -> {
          var builder = entityManager.getCriteriaBuilder();
          CriteriaQuery<SensorDao> query = builder.createQuery(SensorDao.class);
          Root<SensorDao> fromSensor = query.from(SensorDao.class);
          query.select(fromSensor)
            .where(builder.and(fromSensor.get(CHANNEL_ID).in(channelIdSublist)),
              builder.lessThanOrEqualTo(fromSensor.get(SENSOR_KEY).get("time"), endTime),
              builder.greaterThanOrEqualTo(fromSensor.get(SENSOR_KEY).get(END_TIME), startTime));

          return entityManager.createQuery(query).getResultList();
        }));
    }
  }

  /**
   * Finds all {@link SensorDao}s for siteChanKeys, starting at effectiveTime that have the same Response version info.
   * Response Versions look at calib and calper fields
   *
   * @param siteChanKeys
   * @param effectiveTime
   * @return List of all Sensor for the supplied siteChanKeys that create a version starting at effectiveTime
   */
  public List<SensorDao> findSensorVersionsByNameAndTime(List<SiteChanKey> siteChanKeys,
    Instant effectiveTime) {
    Validate.notNull(siteChanKeys);
    Validate.notNull(effectiveTime);

    if (siteChanKeys.isEmpty()) {
      logger.debug("findNextSensorVersionByNameAndTime was given an empty list of SiteChanKeys");
      return List.of();
    } else {
      return runWithEntityManager(entityManager ->
        runPartitionedQuery(siteChanKeys, 500, keySubList -> {
          var cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<SensorDao> query = cb.createQuery(SensorDao.class);

          Root<SensorDao> rootSensor = query.from(SensorDao.class);
          query.select(rootSensor);
          query.where(
            cb.or(
              keySubList.stream()
                .map(k -> cb.and(
                  cb.equal(rootSensor.get(SENSOR_KEY).get(STATION), k.getStationCode()),
                  cb.equal(rootSensor.get(SENSOR_KEY).get(CHANNEL), k.getChannelCode()),
                  cb.greaterThanOrEqualTo(rootSensor.get(SENSOR_KEY).get(END_TIME), effectiveTime),
                  cb.lessThan(rootSensor.get(SENSOR_KEY).get(TIME), getNextVersionStartTime(cb, query, k, effectiveTime))
                ))
                .toArray(Predicate[]::new)
            ));
          return entityManager.createQuery(query).getResultList();
        })
      );
    }
  }

  /**
   * subquery to retreive the start time of the next version.  For Sensor, that is defined as a change in tShift value
   *
   * @param cb
   * @param query
   * @param k
   * @param effectiveTime
   * @return subquery to be used in parent query
   */
  private Subquery<Double> getNextVersionStartTime(CriteriaBuilder cb,
    CriteriaQuery<SensorDao> query, SiteChanKey k, Instant effectiveTime) {
    Subquery<Double> subquery = query.subquery(Double.class);
    Root<SensorDao> sensor = subquery.from(SensorDao.class);

    Predicate tShiftIn = sensor.get(T_SHIFT).in(getVersionAttributes(cb, query, k, T_SHIFT, effectiveTime));
    CriteriaBuilder.Coalesce<Double> coalesce = cb.coalesce();
    coalesce.value(cb.min(sensor.get(SENSOR_KEY).get(TIME)));
    coalesce.value(9999999999.999);

    subquery.select(coalesce)
      .where(
        cb.equal(sensor.get(SENSOR_KEY).get(STATION), k.getStationCode()),
        cb.equal(sensor.get(SENSOR_KEY).get(CHANNEL), k.getChannelCode()),
        cb.greaterThanOrEqualTo(sensor.get(SENSOR_KEY).get(TIME), effectiveTime),
        cb.not(tShiftIn));
    return subquery;
  }

  /**
   * subquery to compare specific attributes (columns) to find changes
   *
   * @param cb
   * @param query
   * @param k
   * @param property
   * @param effectiveTime
   * @return subquery to be used in parent query
   */
  private Subquery<Float> getVersionAttributes(CriteriaBuilder cb,
    CriteriaQuery<SensorDao> query, SiteChanKey k, String property, Instant effectiveTime) {
    Subquery<Float> subquery = query.subquery(Float.class);
    Root<SensorDao> sensor = subquery.from(SensorDao.class);

    subquery.select(sensor.get(property))
      .where(
        cb.equal(sensor.get(SENSOR_KEY).get(STATION), k.getStationCode()),
        cb.equal(sensor.get(SENSOR_KEY).get(CHANNEL), k.getChannelCode()),
        cb.greaterThanOrEqualTo(sensor.get(SENSOR_KEY).get(END_TIME), effectiveTime),
        cb.lessThanOrEqualTo(sensor.get(SENSOR_KEY).get(TIME), effectiveTime));

    return subquery;
  }


  public List<SensorDao> findSensorsByKeyAndTime(List<SiteChanKey> siteChanKeys, Instant effectiveAt) {

    Validate.notNull(siteChanKeys);
    Validate.notNull(effectiveAt);

    if (siteChanKeys.isEmpty()) {
      logger.debug("Request for Sensor by SiteChanKeys and effective at time "
        + "was given an empty list of SiteChanKeys");
      return List.of();
    }

    return runWithEntityManager(entityManager ->
      runPartitionedQuery(siteChanKeys, 500, keySublist -> {
        var builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SensorDao> sensorQuery = builder.createQuery(SensorDao.class);
        Root<SensorDao> fromSensorDao = sensorQuery.from(SensorDao.class);
        Path<SensorKey> id = fromSensorDao.get(SENSOR_KEY);
        sensorQuery.select(fromSensorDao)
          .where(builder.and(
              builder.or(keySublist.stream()
                .map(key -> builder.and(builder.equal(id.get(STATION), key.getStationCode()),
                  builder.equal(id.get(CHANNEL), key.getChannelCode())))
                .toArray(Predicate[]::new))),
            builder.lessThanOrEqualTo(id.get(TIME), effectiveAt),
            builder.greaterThanOrEqualTo(id.get(END_TIME), effectiveAt));

        return entityManager.createQuery(sensorQuery).getResultList();
      })
    );
  }

  public List<SensorDao> findSensorsByKeyAndTimeRange(List<SiteChanKey> siteChanKeys,
    Instant startTime, Instant endTime) {

    Validate.notNull(siteChanKeys, "Request for Sensors by time range must be given list of SiteChanKeys");
    Validate.notNull(startTime, "Request for Sensors by time range was must be given a start time");
    Validate.notNull(endTime, "Request for Sensors by time range was must be given a end time");

    if (siteChanKeys.isEmpty()) {
      logger.debug("Request for Sensors by key and timer range was given an empty list of keys");
      return new ArrayList<>();
    } else {
      return runWithEntityManager(entityManager ->
        runPartitionedQuery(siteChanKeys, 250, keySubList -> {
          var cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<SensorDao> query = cb.createQuery(SensorDao.class);
          Root<SensorDao> fromSensor = query.from(SensorDao.class);

          final Path<Object> id = fromSensor.get(SENSOR_KEY);
          query.select(fromSensor);
          query.where(
            cb.and(
              cb.greaterThanOrEqualTo(id.get(END_TIME), startTime),
              cb.lessThanOrEqualTo(id.get(TIME), endTime),
              cb.or(
                keySubList.stream()
                  .map(k -> cb.and(
                    cb.equal(id.get(STATION), k.getStationCode()),
                    cb.equal(id.get(CHANNEL), k.getChannelCode())
                  ))
                  .toArray(Predicate[]::new)
              ))).orderBy(cb.asc(fromSensor.get(SENSOR_KEY).get(TIME)));

          return entityManager.createQuery(query).getResultList();
        }));
    }
  }

}
