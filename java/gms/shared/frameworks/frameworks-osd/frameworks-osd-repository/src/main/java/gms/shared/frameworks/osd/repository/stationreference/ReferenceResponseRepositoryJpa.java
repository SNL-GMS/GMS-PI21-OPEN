package gms.shared.frameworks.osd.repository.stationreference;

import gms.shared.frameworks.coi.exceptions.DataExistsException;
import gms.shared.frameworks.osd.api.stationreference.ReferenceResponseRepositoryInterface;
import gms.shared.frameworks.osd.api.util.RepositoryExceptionUtils;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceResponse;
import gms.shared.frameworks.osd.dao.stationreference.ReferenceResponseDao;
import gms.shared.frameworks.osd.repository.utils.RepositoryUtility;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ReferenceResponseRepositoryJpa implements ReferenceResponseRepositoryInterface {

  private static final Logger logger = LoggerFactory
    .getLogger(ReferenceResponseRepositoryJpa.class);

  private EntityManagerFactory entityManagerFactory;

  private final RepositoryUtility<ReferenceResponse, ReferenceResponseDao> responseRepoUtility;

  public ReferenceResponseRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
    this.responseRepoUtility = RepositoryUtility
      .create(ReferenceResponseDao.class, ReferenceResponseDao::new, ReferenceResponseDao::toCoi);
  }

  /**
   * Retrieves {@link ReferenceResponse}s by the channel names those responses are associated with.
   *
   * @param channelNames the names of the channels to retrieve reference response data for
   * @return list of reference responses; may be empty.
   */
  public List<ReferenceResponse> retrieveReferenceResponses(Collection<String> channelNames) {
    Validate.notNull(channelNames);
    // This prevents querying for all reference response data.
    Validate.notEmpty(channelNames);
    logger.info("Retrieving ReferenceResponses");
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<ReferenceResponseDao> query = cb
        .createQuery(ReferenceResponseDao.class);
      Root<ReferenceResponseDao> from = query.from(ReferenceResponseDao.class);
      query.select(from).where(from.get("channelName").in(channelNames));
      return entityManager.createQuery(query).getResultStream()
        .map(ReferenceResponseDao::toCoi)
        .collect(Collectors.toList());
    } finally {
      entityManager.close();
    }
  }

  /**
   * Stores {@link ReferenceResponse}s
   */
  public void storeReferenceResponses(Collection<ReferenceResponse> referenceResponses) {
    Validate.notNull(referenceResponses);
    logger.info("Storing ReferenceResponses");
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      for (ReferenceResponse response : referenceResponses) {
        if (responseExists(entityManager, response)) {
          throw new DataExistsException("Attempt to store ReferenceResponse, already present: "
            + response);
        }
      }
      this.responseRepoUtility.persist(referenceResponses, entityManager);
    } catch (Exception e) {
      throw RepositoryExceptionUtils.wrapWithContext("Error storing ReferenceResponse: ", e);
    } finally {
      entityManager.close();
    }
  }

  private static boolean responseExists(EntityManager entityManager, ReferenceResponse response) {
    ReferenceResponseDao stored = entityManager.find(ReferenceResponseDao.class, response.getReferenceResponseId());

    // If stored != null, the response already exists.
    return (stored != null);
  }
}
