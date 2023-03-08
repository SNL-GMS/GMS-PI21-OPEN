package gms.shared.stationdefinition.database.connector;

import gms.shared.stationdefinition.dao.css.WfTagDao;
import gms.shared.utilities.bridge.database.connector.DatabaseConnector;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class WftagDatabaseConnector extends DatabaseConnector {
  private static final Logger logger = LoggerFactory.getLogger(WftagDatabaseConnector.class);

  private static final String WFTAG_KEY = "wfTagKey";
  private static final String TAGID = "id";

  public static final String EMPTY_TAGID_LIST_ERROR = "Request for Wftag by ids must be given a list of tagIds";

  public WftagDatabaseConnector(@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
    super(entityManagerFactory);
  }

  /**
   * Find all {@link WfTagDao}s for the given tagIds
   *
   * @param tagIds Collection of tagIds
   * @return list of {@link WfTagDao}s
   */
  public List<WfTagDao> findWftagsByTagIds(Collection<Long> tagIds) {
    Validate.notNull(tagIds, EMPTY_TAGID_LIST_ERROR);

    if (tagIds.isEmpty()) {
      logger.debug("Request for Wftag by tagIds was given an empty list of keys");
      return new ArrayList<>();
    } else {
      return runWithEntityManager(entityManager ->
        runPartitionedQuery(tagIds, 250, partitionedTagIds -> {

          var cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<WfTagDao> query = cb.createQuery(WfTagDao.class);
          Root<WfTagDao> fromWftag = query.from(WfTagDao.class);

          final Path<Object> idPath = fromWftag.get(WFTAG_KEY);
          query.select(fromWftag);
          query.where(
            cb.or(
              partitionedTagIds.stream()
                .map(tagId ->
                  cb.equal(idPath.get(TAGID), tagId)
                )
                .toArray(Predicate[]::new)
            ));

          return entityManager.createQuery(query).getResultList();
        }));
    }
  }
}
