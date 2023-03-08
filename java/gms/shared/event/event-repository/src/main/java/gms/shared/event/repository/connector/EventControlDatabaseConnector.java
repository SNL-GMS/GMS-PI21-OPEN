package gms.shared.event.repository.connector;

import gms.shared.event.dao.EventControlDao;
import gms.shared.event.dao.EventIdOriginIdKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.Optional;

/**
 * Manages querying {@link EventControlDao} from the database
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EventControlDatabaseConnector {

  private static final Logger logger = LoggerFactory.getLogger(EventControlDatabaseConnector.class);
  private static final String EVENT_ID_ORIGIN_ID_KEY = "eventIdOriginIdKey";

  private final EntityManager entityManager;

  public EventControlDatabaseConnector(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  /**
   * Retrieves {@link EventControlDao} that match the provided evid and orid
   *
   * @param evid to query for
   * @param orid to query for
   * @return {@link Optional} {@link EventControlDao} if found
   */
  public Optional<EventControlDao> findByEventIdOriginId(long evid, long orid) {

    var cb = entityManager.getCriteriaBuilder();
    var query = cb.createQuery(EventControlDao.class);
    var fromEventControl = query.from(EventControlDao.class);
    var inputKey = new EventIdOriginIdKey();
    inputKey.setEventId(evid);
    inputKey.setOriginId(orid);

    query.select(fromEventControl)
      .where(cb.equal(fromEventControl.get(EVENT_ID_ORIGIN_ID_KEY), inputKey));

    try {
      return Optional.of(entityManager.createQuery(query).getSingleResult());
    } catch (NoResultException ex) {
      logger.warn("No result found for EventControlDao with evid[{}], orid[{}]", evid, orid);
      return Optional.empty();
    } catch (Exception ex) {
      logger.warn("Failed to retrieve EventControlDao for evid[{}], orid[{}] caused by {}", evid, orid, ex);
      return Optional.empty();
    }
  }
}
