package gms.shared.frameworks.osd.repository.util;

import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueId;
import gms.shared.frameworks.osd.dao.channelsoh.AcquiredChannelEnvironmentIssueDao;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Utility for building frequently used queries when testing repository/DB operations against a test database
 */
public class CriteriaQueryUtil {

  private static final String CHANNEL_NAME = "channelName";
  private static final String START_TIME = "startTime";
  private static final String ACEI_TYPE = "type";

  /**
   * Builds a {@link CriteriaQuery} for retrieval of {@link AcquiredChannelEnvironmentIssueDao}s by their associated COI's ID
   *
   * @param entityManager Entity manager used to build the query
   * @param entityType Entity type class used to bind the query's parameterized return type
   * @param coiId {@link AcquiredChannelEnvironmentIssue#getId()} used to construct the query parameters
   * @param <E> Entity type that parameterizes the returned {@link CriteriaQuery}
   * @return A {@link CriteriaQuery} used to retrieve relevant {@link AcquiredChannelEnvironmentIssueDao}s of a bound type E by their associated COI's ID
   */
  public static <E extends AcquiredChannelEnvironmentIssueDao> CriteriaQuery<E> aceiByCoiIdQuery(
    EntityManager entityManager, Class<E> entityType, AcquiredChannelEnvironmentIssueId coiId) {
    var builder = entityManager.getCriteriaBuilder();

    CriteriaQuery<E> aceiQuery =
      builder.createQuery(entityType);
    Root<E> fromAcei = aceiQuery.from(entityType);

    aceiQuery.where(builder.and(builder.equal(fromAcei.get(CHANNEL_NAME), coiId.getChannelName()),
      builder.equal(fromAcei.get(ACEI_TYPE), coiId.getType()),
      builder.equal(fromAcei.get(START_TIME), coiId.getStartTime())));

    return aceiQuery;
  }
}
