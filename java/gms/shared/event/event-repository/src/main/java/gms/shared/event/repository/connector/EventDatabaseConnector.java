package gms.shared.event.repository.connector;

import gms.shared.event.dao.EventDao;
import gms.shared.utilities.bridge.database.connector.DatabaseConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Manages querying {@link EventDao} from the database
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EventDatabaseConnector {

  private static final Logger logger = LoggerFactory.getLogger(EventDatabaseConnector.class);

  private final EntityManager entityManager;

  private static final String PERSISTENCE_UNIT_NAME_STRING = "Persistence unit name: {}";
  private static final String HIBERNATE_GET_PERSISTENCE_UNIT_NAME_STRING = "hibernate.ejb.persistenceUnitName";

  public EventDatabaseConnector(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  /**
   * Returns the EventDao from the database with the specified id
   *
   * @param eventId The id of the EventDao to query
   * @return Returns an Optional populated with the retrieved EventDao. If there was no unique
   * EventDao found with the specified id, an empty Optional is returned.
   */
  public Optional<EventDao> findEventById(long eventId) {

    logger.info(PERSISTENCE_UNIT_NAME_STRING, entityManager.getEntityManagerFactory().getProperties().get(HIBERNATE_GET_PERSISTENCE_UNIT_NAME_STRING).toString());
    var criteriaBuilder = entityManager.getCriteriaBuilder();
    var eventCriteriaQuery = criteriaBuilder.createQuery(EventDao.class);
    var eventRoot = eventCriteriaQuery.from(EventDao.class);

    eventCriteriaQuery.select(eventRoot);

    eventCriteriaQuery.where(criteriaBuilder.equal(eventRoot.get("eventId"), eventId));

    var eventQuery = entityManager.createQuery(eventCriteriaQuery);
    return executeFindEventById(eventQuery, eventId);
  }

  /**
   * Returns EventDaos that occur in the time range provided. An EventDao lies within the time range if the time of its
   * preferredOrigin +/- its OrigerrDao's stime lies within the time range. Both ends of the range are inclusive.
   *
   * @param startTime Beginning of the time range
   * @param endTime End of the time range
   * @return List of EventDaos
   */
  public List<EventDao> findEventsByTime(Instant startTime, Instant endTime) {

    checkNotNull(startTime);
    checkNotNull(endTime);

    logger.info(PERSISTENCE_UNIT_NAME_STRING, entityManager.getEntityManagerFactory().getProperties().get(HIBERNATE_GET_PERSISTENCE_UNIT_NAME_STRING).toString());

    var query = entityManager.createNamedQuery("event.findByTime", EventDao.class);
    query.setParameter("startTime", startTime.toEpochMilli() / 1000.0);
    query.setParameter("endTime", endTime.toEpochMilli() / 1000.0);
    logger.debug("EventDaos found between startTime: {} EndTime: {}: {}",
      startTime.toEpochMilli() / 1000.0,
      endTime.toEpochMilli() / 1000.0,
      query.getResultList().size());
    return query.getResultList();
  }

  public List<Long> findEventIdsByArids(List<Long> arids) {
    checkNotNull(arids, "arids must not be null");
    checkArgument(!arids.isEmpty(), "arids must not be empty");

    logger.info(PERSISTENCE_UNIT_NAME_STRING, entityManager.getEntityManagerFactory().getProperties().get(HIBERNATE_GET_PERSISTENCE_UNIT_NAME_STRING).toString());

    var query = entityManager.createQuery(
      "SELECT DISTINCT event.eventId " +
        "FROM EventDao as event " +
        "JOIN OriginDao as origin " +
        "  WITH origin.eventId = event.eventId " +
        "JOIN AssocDao as assoc " +
        "  WITH assoc.id.originId = origin.originId " +
        "WHERE assoc.id.arrivalId IN :arrivalIds", Long.class);
    query.setParameter("arrivalIds", arids);

    return query.getResultList();
  }

  /**
   * Handles the various exceptions thrown by executing the findEventById query
   */
  private Optional<EventDao> executeFindEventById(TypedQuery<EventDao> eventQuery, long eventId) {

    try {
      return Optional.of(eventQuery.getSingleResult());
    } catch (NoResultException e) {
      final var message = String.format("No Event found with id '%d'", eventId);
      logger.warn(message, e);
      return Optional.empty();
    } catch (NonUniqueResultException e) {
      final var message = String.format("Non-unique events found with id '%d'", eventId);
      logger.warn(message, e);
      return Optional.empty();
    } catch (Exception e) {
      final var message = String.format("Error retrieving Event with id '%d'", eventId);
      throw new DatabaseConnectorException(message, e);
    }
  }

}
