package gms.shared.event.repository.connector;

import com.google.common.collect.Lists;
import gms.shared.event.dao.OriginDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Manages querying {@link OriginDao}s from the database
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OriginDatabaseConnector {

  private static final Logger logger = LoggerFactory.getLogger(OriginDatabaseConnector.class);

  public static final String ORIGIN_ID = "originId";
  public static final String EVENT_ID = "eventId";

  private final EntityManager entityManager;

  public OriginDatabaseConnector(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  /**
   * Retrieves a list of OriginDaos associated with the passed in eventIds
   *
   * @param eventIds to retrieve
   * @return a list of OriginDaos
   */
  public List<OriginDao> findByEventIds(List<Long> eventIds) {
    checkNotNull(eventIds, "The collection of EventIds cannot be null!");
    checkArgument(!eventIds.isEmpty(), "The collection of EventIds cannot be empty!");

    return Lists.partition(new ArrayList<>(eventIds), 500).stream()
      .map(partitionedEventIds -> {
        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createQuery(OriginDao.class);
        var fromOrigin = query.from(OriginDao.class);

        query.select(fromOrigin)
          .where(
            cb.or(partitionedEventIds.stream()
              .map(eventId -> cb.and(
                cb.equal(fromOrigin.get(EVENT_ID), eventId)
              ))
              .toArray(Predicate[]::new)));

        try {
          return entityManager.createQuery(query).getResultStream()
            .collect(Collectors.toList());
        } catch (Exception ex) {
          logger.warn("Could not find OriginDaos from list of EventIds: {}", partitionedEventIds);
          return Collections.<OriginDao>emptyList();
        }
      })
      .flatMap(Collection::stream)
      .collect(Collectors.toList());
  }

  /**
   * Retrieves an {@link OriginDao} associated with the passed in originId
   *
   * @param originId to retrieve
   * @return a OriginDao if found, otherwise an empty {@link Optional}
   */
  public Optional<OriginDao> findById(long originId) {


    var cb = entityManager.getCriteriaBuilder();
    var query = cb.createQuery(OriginDao.class);
    var fromOrigin = query.from(OriginDao.class);

    query.select(fromOrigin)
      .where(fromOrigin.get(ORIGIN_ID).in(originId));


    try {
      return Optional.of(entityManager.createQuery(query).getSingleResult());
    } catch (Exception ex) {
      logger.warn("Failed to retrieve OriginDao for ID {}", originId, ex);
      return Optional.empty();
    }
  }

  /**
   * Retrieves a list of OriginDaos based on the range [startTime, endTime] +- the Origerr.stime (bounds are inclusive)
   *
   * @param startTime the startTime
   * @param endTime the endTime
   * @return a list of OriginDaos
   */
  public List<OriginDao> findByTime(Instant startTime, Instant endTime) {

    checkNotNull(startTime, "startTime cannot be null!");
    checkNotNull(endTime, "endTIme cannot be null");
    checkArgument(startTime.isBefore(endTime), "startTime must be before endTime");

    var query = entityManager.createNamedQuery("origin.findByTime", OriginDao.class);
    query.setParameter("startTime", startTime.toEpochMilli() / 1000.0);
    query.setParameter("endTime", endTime.toEpochMilli() / 1000.0);
    logger.debug("OriginDaos found between startTime: {} EndTime: {}: {}",
      startTime.toEpochMilli() / 1000.0,
      endTime.toEpochMilli() / 1000.0,
      query.getResultList().size());
    return query.getResultList();
  }

}
