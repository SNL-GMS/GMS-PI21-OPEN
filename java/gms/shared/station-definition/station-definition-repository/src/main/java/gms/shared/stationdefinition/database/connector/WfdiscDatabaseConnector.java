package gms.shared.stationdefinition.database.connector;

import gms.shared.stationdefinition.dao.css.SiteChanKey;
import gms.shared.stationdefinition.dao.css.StationChannelTimeKey;
import gms.shared.stationdefinition.dao.css.WfdiscDao;
import gms.shared.stationdefinition.dao.css.enums.SegType;
import gms.shared.utilities.bridge.database.connector.DatabaseConnector;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class WfdiscDatabaseConnector extends DatabaseConnector {

  static final String MISSING_STATION_CHANNEL_TIME_KEY_ERROR = "Request by station channel time key must be provided a key";
  static final String EMPTY_CHANNEL_NAME_LIST_ERROR = "Request for Wfdisc by SiteChanKey must be given a list of keys";
  static final String MISSING_START_TIME_ERROR = "Request for Wfdisc by time range must be given a start time";
  static final String MISSING_END_TIME_ERROR = "Request for Wfdisc by time range must be given a end time";
  static final String START_NOT_BEFORE_END_TIME_ERROR = "Start time has to be before end time";
  static final String EMPTY_WFID_LIST_ERROR = "Request for Wftag by ids must be given a list of wfids";
  static final String EMPTY_LIST_OF_KEYS_ERROR = "Request for Wfdisc by name was given an empty list of keys";
  private static final String ID = "id";
  private static final String STATION_CODE = "stationCode";
  private static final String CHANNEL_CODE = "channelCode";
  private static final String SEG_TYPE = "segType";
  private static final String TIME = "time";
  private static final String END_TIME = "endTime";
  private static final String LOAD_DATE = "loadDate";
  private static final String CALIB = "calib";
  private static final String CALPER = "calper";
  private static final Logger logger = LoggerFactory.getLogger(WfdiscDatabaseConnector.class);

  //public for wiring into spring app (waveform manager service
  public WfdiscDatabaseConnector(@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
    super(entityManagerFactory);
  }

  /**
   * Find all {@link WfdiscDao}s associated with the input list of wfids
   *
   * @param wfids List of wfids to query
   * @return Collection of {@link WfdiscDao}s
   */
  public List<WfdiscDao> findWfdiscsByWfids(Collection<Long> wfids) {
    Validate.notNull(wfids, EMPTY_WFID_LIST_ERROR);

    if (wfids.isEmpty()) {
      logger.debug("Request for Wfdiscs by wfids was given an empty list of keys");
      return new ArrayList<>();
    } else {
      return runWithEntityManager(entityManager ->
        runPartitionedQuery(wfids, 250, partitionedWfids -> {
          var cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<WfdiscDao> query = cb.createQuery(WfdiscDao.class);
          Root<WfdiscDao> fromWfdisc = query.from(WfdiscDao.class);

          query.select(fromWfdisc);
          query.where(
            cb.or(
              partitionedWfids.stream()
                .map(wfid ->
                  cb.equal(fromWfdisc.get(ID), wfid)
                )
                .toArray(Predicate[]::new)
            ));

          return entityManager.createQuery(query).getResultList();
        }));
    }
  }

  /**
   * Finds all {@link WfdiscDao}s for siteChanKeys, starting at effectiveTime that have the same Response version info.
   * Response Versions look at calib and calper fields
   *
   * @param siteChanKeys
   * @param effectiveTime
   * @return List of all WfDisc for the supplied siteChanKeys that create a version starting at effectiveTime
   */
  public List<WfdiscDao> findWfdiscVersionsByNameAndTime(Collection<SiteChanKey> siteChanKeys,
    Instant effectiveTime) {

    Validate.notNull(siteChanKeys, EMPTY_CHANNEL_NAME_LIST_ERROR);
    Validate.notNull(effectiveTime, "Request for Wfdisc by time was must be given a time");

    if (siteChanKeys.isEmpty()) {
      logger.debug(EMPTY_LIST_OF_KEYS_ERROR);
      return new ArrayList<>();
    } else {
      return runWithEntityManager(entityManager ->
        runPartitionedQuery(siteChanKeys, 500, keySubList -> {
          var cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<WfdiscDao> query = cb.createQuery(WfdiscDao.class);

          Root<WfdiscDao> fromWfdisc = query.from(WfdiscDao.class);
          query.select(fromWfdisc);
          query.where(
            cb.or(
              keySubList.stream()
                .map(k -> cb.and(
                cb.equal(fromWfdisc.get(STATION_CODE), k.getStationCode()),
                cb.equal(fromWfdisc.get(CHANNEL_CODE), k.getChannelCode()),
                cb.lessThan(fromWfdisc.get(TIME), getNextVersionStartTime(cb, query, k, effectiveTime))
              ))
                .toArray(Predicate[]::new)
            ));
          return entityManager.createQuery(query).getResultList();
        })
      );
    }
  }

  /**
   * subquery to retreive the start time of the next version.  For Wfdisc, that is defined as a change in calper or calib values
   *
   * @param cb
   * @param query
   * @param k
   * @param effectiveTime
   * @return subquery to be used in parent query
   */
  private Subquery<Double> getNextVersionStartTime(CriteriaBuilder cb,
    CriteriaQuery<WfdiscDao> query, SiteChanKey k, Instant effectiveTime) {
    Subquery<Double> subquery = query.subquery(Double.class);
    Root<WfdiscDao> wfdisc = subquery.from(WfdiscDao.class);

    Predicate calibIn = wfdisc.get(CALIB).in(getVersionAttributes(cb, query, k, CALIB, effectiveTime));
    Predicate calperIn = wfdisc.get(CALPER).in(getVersionAttributes(cb, query, k, CALPER, effectiveTime));
    CriteriaBuilder.Coalesce<Double> coalesce = cb.coalesce();
    coalesce.value(cb.min(wfdisc.get(TIME)));
    coalesce.value(9999999999.999);

    subquery.select(coalesce)
      .where(
        cb.equal(wfdisc.get(STATION_CODE), k.getStationCode()),
        cb.equal(wfdisc.get(CHANNEL_CODE), k.getChannelCode()),
        cb.greaterThanOrEqualTo(wfdisc.get(TIME), effectiveTime),
        cb.or(cb.not(calibIn), cb.not(calperIn)));
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
    CriteriaQuery<WfdiscDao> query, SiteChanKey k, String property, Instant effectiveTime) {
    Subquery<Float> subquery = query.subquery(Float.class);
    Root<WfdiscDao> wfdisc = subquery.from(WfdiscDao.class);

    subquery.select(wfdisc.get(property))
      .where(
        cb.equal(wfdisc.get(STATION_CODE), k.getStationCode()),
        cb.equal(wfdisc.get(CHANNEL_CODE), k.getChannelCode()),
        cb.greaterThanOrEqualTo(wfdisc.get(END_TIME), effectiveTime),
        cb.lessThanOrEqualTo(wfdisc.get(TIME), effectiveTime));

    return subquery;
  }

  /**
   * Finds all {@link WfdiscDao}s for the given station code and channel code pairs at the specified
   * effective time.
   *
   * select * from wfdisc where wfdisc.endtime >= eff_time and wfdisc.time <= eff_time and segtype = 'o' and
   * ((sta = 'AS01' and chan='BHZ') or (sta = 'MK01' and chan = 'SHZ'))
   *
   * AS01/BHZ
   * MK01/SHZ
   * @param siteChanKeys
   * @param effectiveTime
   * @return
   */
  public List<WfdiscDao> findWfdiscsByNameAndTime(Collection<SiteChanKey> siteChanKeys,
    Instant effectiveTime) {

    Validate.notNull(siteChanKeys, EMPTY_CHANNEL_NAME_LIST_ERROR);
    Validate.notNull(effectiveTime, "Request for Wfdisc by time was must be given a time");

    if (siteChanKeys.isEmpty()) {
      logger.debug(EMPTY_LIST_OF_KEYS_ERROR);
      return new ArrayList<>();
    } else {
      return runWithEntityManager(entityManager ->
        runPartitionedQuery(siteChanKeys, 500, keySubList -> {
          var cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<WfdiscDao> query = cb.createQuery(WfdiscDao.class);
          Root<WfdiscDao> fromWfdisc = query.from(WfdiscDao.class);

          query.select(fromWfdisc);
          query.where(
            cb.or(
              keySubList.stream()
                .map(k -> cb.and(
                cb.equal(fromWfdisc.get(STATION_CODE), k.getStationCode()),
                cb.equal(fromWfdisc.get(CHANNEL_CODE), k.getChannelCode()),
                cb.lessThanOrEqualTo(fromWfdisc.get(TIME), effectiveTime),
                cb.greaterThanOrEqualTo(fromWfdisc.get(END_TIME), effectiveTime)
              ))
                .toArray(Predicate[]::new)
            ));

          return entityManager.createQuery(query).getResultList();
        })
      );
    }
  }

  /**
   * Finds all {@link WfdiscDao}s for the given station code and channel code pairs that have a
   * {@link StationChannelTimeKey#getTime()} and {@link WfdiscDao#getEndTime()} that falls within
   * the queried time range specified by the inputs such that:
   * <p>
   * {@link StationChannelTimeKey#getTime()} {@literal <=} endTime && {@link WfdiscDao#getEndTime()}
   * {@literal >=} startTime
   *
   * @param siteChanKeys
   * @param startTime
   * @param endTime
   * @return
   */
  public List<WfdiscDao> findWfdiscsByNameAndTimeRange(Collection<SiteChanKey> siteChanKeys,
    Instant startTime, Instant endTime) {

    Validate.notNull(siteChanKeys, EMPTY_CHANNEL_NAME_LIST_ERROR);
    Validate.notNull(startTime, MISSING_START_TIME_ERROR);
    Validate.notNull(endTime, MISSING_END_TIME_ERROR);

    if (siteChanKeys.isEmpty()) {
      logger.debug(EMPTY_LIST_OF_KEYS_ERROR);
      return new ArrayList<>();
    } else {
      return runWithEntityManager(entityManager ->
        runPartitionedQuery(siteChanKeys, 250, keySubList -> {
          var cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<WfdiscDao> query = cb.createQuery(WfdiscDao.class);
          Root<WfdiscDao> fromWfdisc = query.from(WfdiscDao.class);

          query.select(fromWfdisc);
          query.where(
            cb.and(
              cb.or(
                keySubList.stream()
                  .map(k -> cb.and(
                  cb.equal(fromWfdisc.get(STATION_CODE), k.getStationCode()),
                  cb.equal(fromWfdisc.get(CHANNEL_CODE), k.getChannelCode())
                )).toArray(Predicate[]::new)),
              cb.greaterThanOrEqualTo(fromWfdisc.get(END_TIME), startTime),
              cb.lessThanOrEqualTo(fromWfdisc.get(TIME), endTime)),
            cb.equal(fromWfdisc.get(SEG_TYPE), SegType.ORIGINAL)
          ).orderBy(cb.asc(fromWfdisc.get(TIME)));

          return entityManager.createQuery(query).getResultList();
        }));
    }
  }

  public List<WfdiscDao> findWfDiscVersionAfterEffectiveTime(Collection<SiteChanKey> siteChanKeys) {

    List<Tuple> resultSet = runWithEntityManager(entityManager ->
      runPartitionedQuery(siteChanKeys, 250, partitionedSiteChanKeys -> {
        var cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
        Root<WfdiscDao> fromWfdisc = query.from(WfdiscDao.class);
        query.multiselect(fromWfdisc.get(STATION_CODE), fromWfdisc.get(CHANNEL_CODE),
          cb.min(fromWfdisc.get(TIME)).alias(TIME))
          .where(
            cb.or(
              partitionedSiteChanKeys.stream()
                .map(k -> cb.and(
                cb.equal(fromWfdisc.get(STATION_CODE), k.getStationCode()),
                cb.equal(fromWfdisc.get(CHANNEL_CODE), k.getChannelCode()),
                cb.greaterThan(fromWfdisc.get(TIME), k.getOnDate())
              ))
                .toArray(Predicate[]::new)
            )).groupBy(fromWfdisc.get(STATION_CODE), fromWfdisc.get(CHANNEL_CODE));
        return entityManager.createQuery(query).getResultList();
      }));
    return runWithEntityManager(entityManager ->
      runPartitionedQuery(resultSet, 250, partitionedResultSet -> {
        var cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<WfdiscDao> query = cb.createQuery(WfdiscDao.class);
        Root<WfdiscDao> fromWfdisc = query.from(WfdiscDao.class);
        query.select(fromWfdisc)
          .where(
            cb.or(
              partitionedResultSet.stream()
                .map(k -> cb.and(
                cb.equal(fromWfdisc.get(STATION_CODE), k.get(0)),
                cb.equal(fromWfdisc.get(CHANNEL_CODE), k.get(1)),
                cb.equal(fromWfdisc.get(TIME), k.get(2))
              ))
                .toArray(Predicate[]::new)
            )).orderBy(cb.asc(fromWfdisc.get(TIME)));
        return entityManager.createQuery(query).getResultList();
      }));
  }

  /**
   * Retrieve wfdiscs for a provided time range
   *
   * @param startTime as the start of the desired time range
   * @param endTime as the end of the desired time range
   * @return a {@link List}{@literal <}{@link WfdiscDao}{@literal >}
   */
  public List<WfdiscDao> findWfdiscsByTimeRange(Instant startTime, Instant endTime) {

    Validate.notNull(startTime, MISSING_START_TIME_ERROR);
    Validate.notNull(endTime, MISSING_END_TIME_ERROR);
    Validate.isTrue(startTime.isBefore(endTime), START_NOT_BEFORE_END_TIME_ERROR);

    return runWithEntityManager(entityManager -> {

      var cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<WfdiscDao> query = cb.createQuery(WfdiscDao.class);
      Root<WfdiscDao> fromWfdisc = query.from(WfdiscDao.class);

      query.select(fromWfdisc);
      query.where(cb.and(
        cb.greaterThanOrEqualTo(fromWfdisc.get(END_TIME), startTime),
        cb.lessThanOrEqualTo(fromWfdisc.get(TIME), endTime)
      ));

      return entityManager.createQuery(query).getResultList();
    });
  }

  /**
   * Finds all {@link WfdiscDao}s for the given station code and channel code pairs that have a
   * {@link StationChannelTimeKey#getTime()} and {@link WfdiscDao#getEndTime()} that falls within
   * the queried time range and Segment type specified by the inputs such that:
   * <p>
   * {@link StationChannelTimeKey#getTime()} {@literal <=} endTime && {@link WfdiscDao#getEndTime()}
   * {@literal >=} && {@link WfdiscDao#getLoadDate()} {@literal <=} loadTime && {@link
   * WfdiscDao#getSegType()} {@literal <=} segType
   *
   * @param siteChanKeys siteChans to query related wfdiscs for
   * @param startTime begin time of query range
   * @param endTime end time of query range
   * @param creationTime the maximum creation time (load date) allowed for the wfdisc
   * @return list of matching wfdisc records
   */
  public List<WfdiscDao> findWfdiscsByNameTimeRangeAndCreationTime(
    Collection<SiteChanKey> siteChanKeys,
    Instant startTime,
    Instant endTime,
    Instant creationTime) {
    Validate.notNull(siteChanKeys,
      "Request for Wfdisc by time range must be given a valid siteChane List");
    Validate.notEmpty(siteChanKeys,
      "Request for Wfdisc by time range must be given a valid siteChane List");
    Validate.notNull(startTime, MISSING_START_TIME_ERROR);
    Validate.notNull(endTime, MISSING_END_TIME_ERROR);

    return runWithEntityManager(entityManager -> {
      var cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<WfdiscDao> query = cb.createQuery(WfdiscDao.class);
      Root<WfdiscDao> fromWfdisc = query.from(WfdiscDao.class);

      query.select(fromWfdisc);
      query.where(
        cb.and(
          cb.greaterThanOrEqualTo(fromWfdisc.get(END_TIME), startTime),
          cb.lessThanOrEqualTo(fromWfdisc.get(TIME), endTime),
          cb.lessThanOrEqualTo(fromWfdisc.get(LOAD_DATE), creationTime),
          cb.or(
            siteChanKeys.stream()
              .map(scList -> cb.and(
              cb.equal(fromWfdisc.get(STATION_CODE), scList.getStationCode()),
              cb.equal(fromWfdisc.get(CHANNEL_CODE), scList.getChannelCode())
            ))
              .toArray(Predicate[]::new)
          ))).orderBy(cb.asc(fromWfdisc.get(TIME))
      );
      return entityManager.createQuery(query).getResultList();
    });
  }
}
