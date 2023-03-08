package gms.shared.signaldetection.database.connector;

import gms.shared.signaldetection.dao.css.AssocDao;
import gms.shared.utilities.bridge.database.connector.DatabaseConnector;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.Predicate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Component
public class AssocDatabaseConnector extends DatabaseConnector {

  private static final String ARRIVAL_ID = "arrivalId";
  private static final String ORIGIN_ID = "originId";
  private static final String ID = "id";

  public AssocDatabaseConnector(EntityManagerFactory entityManagerFactory) {
    super(entityManagerFactory);
  }

  public List<AssocDao> findAssocsByArids(Collection<Long> arids) {
    Objects.requireNonNull(arids, "Arids cannot be null");

    if (arids.isEmpty()) {
      return List.of();
    } else {
      return runPartitionedQuery(arids, 1000, partitionedArids ->
        runWithEntityManager(entityManager -> {
          var criteriaBuilder = entityManager.getCriteriaBuilder();
          var query = criteriaBuilder.createQuery(AssocDao.class);
          var fromAssoc = query.from(AssocDao.class);
          var idPath = fromAssoc.get(ID);
          query.select(fromAssoc)
            .where(idPath.get(ARRIVAL_ID).in(partitionedArids));

          return entityManager.createQuery(query).getResultList();
        }));
    }
  }

  /**
   * Retrieves a list of AssocDaos that match the passed in list of ORIDs
   *
   * @param orids the ORIDs to find
   * @return a list of AssocDaos matching the ORIDs passed in
   */
  public List<AssocDao> findAssocsByOrids(Collection<Long> orids) {
    Validate.notNull(orids, "OriginIds cannot be null!");

    if (orids.isEmpty()) {
      return List.of();
    } else {
      return runPartitionedQuery(orids, 1000, partitionedOrids ->
        runWithEntityManager(entityManager -> {
          var criteriaBuilder = entityManager.getCriteriaBuilder();
          var query = criteriaBuilder.createQuery(AssocDao.class);
          var fromAssoc = query.from(AssocDao.class);
          var idPath = fromAssoc.get(ID);
          query.select(fromAssoc)
            .where(idPath.get(ORIGIN_ID).in(partitionedOrids));

          return entityManager.createQuery(query).getResultList();
        }));
    }
  }

  /**
   * Retrieves a list of AssocDaos that match the passed in list of
   * pairs of arids and orids
   *
   * @param aridOridList the list of pairs of arids and orids
   * @return a list of AssocDaos matching the arids and orids passed in
   */
  public List<AssocDao> findAssocsByAridsAndOrids(Collection<Pair<Long, Long>> aridOridList) {
    Validate.notNull(aridOridList, "Arids and Orids cannot be null!");

    if (aridOridList.isEmpty()) {
      return List.of();
    } else {
      return runPartitionedQuery(aridOridList, 1000, partitionedAridsOrids ->
        runWithEntityManager(entityManager -> {
          var cb = entityManager.getCriteriaBuilder();
          var query = cb.createQuery(AssocDao.class);
          var fromAssoc = query.from(AssocDao.class);
          var idPath = fromAssoc.get(ID);

          // stream through the list of arid/orid pairs and create predicate for primary keys
          query.select(fromAssoc)
            .where(
              cb.or(partitionedAridsOrids.stream()
                .map(aridOridPair -> cb.and(
                  cb.equal(idPath.get(ARRIVAL_ID), aridOridPair.getLeft()),
                  cb.equal(idPath.get(ORIGIN_ID), aridOridPair.getRight())
                ))
                .toArray(Predicate[]::new)));

          return entityManager.createQuery(query).getResultList();
        }));
    }
  }
}
