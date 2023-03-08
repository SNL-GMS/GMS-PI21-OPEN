package gms.shared.event.repository.connector;

import gms.shared.event.dao.OrigerrDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Executes {@link OrigerrDao} queries against the database
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OriginErrDatabaseConnector {

  private static final Logger logger = LoggerFactory.getLogger(OriginErrDatabaseConnector.class);

  public static final String ORIGIN_ID = "originId";

  private final EntityManager entityManager;

  public OriginErrDatabaseConnector(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  /**
   * Retrieves all {@link OrigerrDao}s that contain the particular origin IDs
   *
   * @param orids The Collection of Origin IDs to query for
   * @return all the {@link OrigerrDao}s that contain the particular origin IDs
   */
  public Set<OrigerrDao> findByIds(Collection<Long> orids) {
    checkNotNull(orids, "The collection of OriginIds cannot be null!");
    checkArgument(!orids.isEmpty(), "The collection of OriginIds cannot be empty!");

    var cb = entityManager.getCriteriaBuilder();
    var query = cb.createQuery(OrigerrDao.class);
    var fromOriginErr = query.from(OrigerrDao.class);

    query.select(fromOriginErr);
    query.where(fromOriginErr.get(ORIGIN_ID).in(orids));

    return Set.copyOf(entityManager.createQuery(query).getResultList());
  }

  /**
   * Retrieves an {@link OrigerrDao} that contains the particular origin ID, if found
   *
   * @param orid The Origin IDs to query for
   * @return the {@link OrigerrDao} that contains the particular origin IDs, if found
   */
  public Optional<OrigerrDao> findById(long orid) {

    var cb = entityManager.getCriteriaBuilder();
    var query = cb.createQuery(OrigerrDao.class);
    var fromOriginErr = query.from(OrigerrDao.class);

    query.select(fromOriginErr)
      .where(fromOriginErr.get(ORIGIN_ID).in(orid));

    try {
      return Optional.of(entityManager.createQuery(query).getSingleResult());
    } catch (Exception ex) {
      logger.warn("Failed to retrieve OriginDao for ID {}", orid, ex);
      return Optional.empty();
    }
  }
}