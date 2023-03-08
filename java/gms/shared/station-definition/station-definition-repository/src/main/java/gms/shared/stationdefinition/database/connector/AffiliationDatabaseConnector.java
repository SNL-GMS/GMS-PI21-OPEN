package gms.shared.stationdefinition.database.connector;

import gms.shared.stationdefinition.dao.css.AffiliationDao;
import gms.shared.utilities.bridge.database.connector.DatabaseConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class AffiliationDatabaseConnector extends DatabaseConnector {

  private static final String NETWORK_STATION_TIME_KEY = "networkStationTimeKey";
  private static final String NETWORK = "network";
  private static final String TIME = "time";
  private static final String END_TIME = "endTime";

  private static final Logger logger = LoggerFactory.getLogger(AffiliationDatabaseConnector.class);

  public AffiliationDatabaseConnector(@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {

    super(entityManagerFactory);
  }

  public List<AffiliationDao> findNextAffiliationByNameAfterTime(Collection<String> networkNames,
    Instant effectiveTime) {
    if (effectiveTime == null || networkNames == null || networkNames.isEmpty()) {
      logger.debug(
        "Request for Affiliation by name was given an empty list of network names or invalid effective time");
      return new ArrayList<>();
    } else {
      return runWithEntityManager(entityManager ->
        runPartitionedQuery(networkNames, 950, partition -> {

          CriteriaBuilder cb = entityManager.getCriteriaBuilder();

          CriteriaQuery<AffiliationDao> query = cb.createQuery(AffiliationDao.class);
          Root<AffiliationDao> fromAffiliation = query.from(AffiliationDao.class);
          query.select(fromAffiliation);

          Subquery<Float> subQuery = query.subquery(Float.class);
          Root<AffiliationDao> subRoot = subQuery.from(AffiliationDao.class);
          subQuery.where(
            cb.and(
              cb.equal(
                fromAffiliation.get(NETWORK_STATION_TIME_KEY).get(NETWORK),
                subRoot.get(NETWORK_STATION_TIME_KEY).get(NETWORK)
              ),
              subRoot.get(NETWORK_STATION_TIME_KEY).get(NETWORK).in(partition),
              cb.greaterThan(subRoot.get(NETWORK_STATION_TIME_KEY).get(TIME), effectiveTime))
          );

          Subquery<Float> subSelect = subQuery
            .select(cb.min(subRoot.get(NETWORK_STATION_TIME_KEY).get(TIME)));
          query.where(
            cb.equal(fromAffiliation.get(NETWORK_STATION_TIME_KEY).get(TIME), subSelect.getSelection())
          );

          return entityManager.createQuery(query).getResultList();
        })
      );
    }
  }

  public List<AffiliationDao> findAffiliationsByNameAndTime(Collection<String> networkNames,
    Instant effectiveTime) {

    if (effectiveTime == null || networkNames == null || networkNames.isEmpty()) {
      logger.debug(
        "Request for Affiliation by name was given an empty list of network names or invalid effective time");
      return new ArrayList<>();
    } else {
      return runWithEntityManager(entityManager ->
        runPartitionedQuery(networkNames, 950, partition -> {

          CriteriaBuilder cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<AffiliationDao> query = cb.createQuery(AffiliationDao.class);
          Root<AffiliationDao> fromAffiliation = query.from(AffiliationDao.class);

          query.select(fromAffiliation);

          query.where(cb.and(
            fromAffiliation.get(NETWORK_STATION_TIME_KEY).get(NETWORK).in(partition),
            cb.lessThanOrEqualTo(fromAffiliation.get(NETWORK_STATION_TIME_KEY).get(TIME), effectiveTime),
            cb.greaterThanOrEqualTo(fromAffiliation.get(END_TIME), effectiveTime)
          ));

          return entityManager.createQuery(query).getResultList();
        })
      );
    }
  }

  public List<AffiliationDao> findAffiliationsByNameAndTimeRange(Collection<String> networkNames,
    Instant startTime, Instant endTime) {

    if (startTime == null || endTime == null || networkNames == null || networkNames.isEmpty()) {
      logger.debug(
        "Request for Affiliation by name was given an empty list of network names or invalid time range parameters");
      return new ArrayList<>();
    } else {

      return runWithEntityManager(entityManager ->
        runPartitionedQuery(networkNames, 950, partition -> {

          CriteriaBuilder cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<AffiliationDao> query = cb.createQuery(AffiliationDao.class);
          Root<AffiliationDao> fromAffiliation = query.from(AffiliationDao.class);

          query.select(fromAffiliation);

          query.where(
            cb.and(
              fromAffiliation.get(NETWORK_STATION_TIME_KEY).get(NETWORK).in(partition),
              cb.greaterThanOrEqualTo(fromAffiliation.get(END_TIME), startTime),
              cb.lessThanOrEqualTo(fromAffiliation.get(NETWORK_STATION_TIME_KEY).get(TIME),
                endTime)
            )
          );

          return entityManager.createQuery(query).getResultList();
        })
      );
    }
  }

  public Collection<AffiliationDao> findAffiliationsByTimeRange(Instant startTime, Instant endTime) {
    return runWithEntityManager(entityManager -> {
      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<AffiliationDao> query = cb.createQuery(AffiliationDao.class);
      Root<AffiliationDao> fromAffiliation = query.from(AffiliationDao.class);

      query.select(fromAffiliation);

      query.where(
        cb.and(
          cb.greaterThanOrEqualTo(fromAffiliation.get(END_TIME), startTime),
          cb.lessThanOrEqualTo(fromAffiliation.get(NETWORK_STATION_TIME_KEY).get(TIME), endTime)
        )
      );

      return entityManager.createQuery(query).getResultList();
    });

  }
}
