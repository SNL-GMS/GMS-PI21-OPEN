package gms.shared.event.repository.connector;

import com.google.common.collect.Lists;
import gms.shared.event.dao.StaMagDao;
import gms.shared.signaldetection.dao.css.AridOridKey;
import gms.shared.signaldetection.dao.css.AssocDao;
import gms.shared.utilities.bridge.database.connector.DatabaseConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages querying {@link StaMagDao}s from the database
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class StaMagDatabaseConnector {

  private static final String ARRIVAL_ID = "arrivalId";
  private static final String ORIGIN_ID = "originId";
  private static final Logger logger = LoggerFactory.getLogger(StaMagDatabaseConnector.class);

  private final EntityManager entityManager;

  public StaMagDatabaseConnector(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  /**
   * Returns a list of {@link StaMagDao}s
   *
   * @param orid An orid to query for StagMags with
   * @return A list of {@link StaMagDao}s
   */
  public List<StaMagDao> findStaMagByOrid(long orid) {
    var criteriaBuilder = entityManager.getCriteriaBuilder();
    var cbQuery = criteriaBuilder.createQuery(StaMagDao.class);
    var fromStaMag = cbQuery.from(StaMagDao.class);

    cbQuery.select(fromStaMag).where(criteriaBuilder.equal(fromStaMag.get(ORIGIN_ID), orid));

    return entityManager.createQuery(cbQuery).getResultList();
  }

  /**
   * Returns a list of {@link StaMagDao}s corresponding to the provided orid and arid
   *
   * @param orid An orid to query for StagMags with
   * @param arid An arid to query for StagMags with
   * @return A list of {@link StaMagDao}s
   */
  public List<StaMagDao> findStaMagByOridAndArid(long orid, long arid) {

    var criteriaBuilder = entityManager.getCriteriaBuilder();
    var staMagCriteriaQuery = criteriaBuilder.createQuery(StaMagDao.class);
    var fromStaMag = staMagCriteriaQuery.from(StaMagDao.class);

    List<Predicate> predicates = new ArrayList<>();
    predicates.add(criteriaBuilder.equal(fromStaMag.get(ORIGIN_ID), orid));
    predicates.add(criteriaBuilder.equal(fromStaMag.get(ARRIVAL_ID), arid));

    staMagCriteriaQuery.select(fromStaMag).where(predicates.toArray(new Predicate[]{}));
    var gaTagQuery = entityManager.createQuery(staMagCriteriaQuery);

    try {
      return gaTagQuery.getResultList();
    } catch (Exception e) {
      final var message = String.format(
        "Error retrieving StaMagDaos with object orid: %d and arid: %d",
        orid, arid);
      throw new DatabaseConnectorException(message, e);
    }
  }

  /**
   * Returns a list of {@link StaMagDao}s corresponding to the provided list of {@link AssocDao}s
   *
   * @param assocs A list of {@link AssocDao} objects to retreive {@link StaMagDao}s for
   * @return A list of {@link StaMagDao}s relevant to the provided {@link AssocDao}s
   */
  public List<StaMagDao> findStaMagDaosByAssocs(Collection<AssocDao> assocs) {
    var assocDaoKeys = assocs.stream().map(AssocDao::assocDaoToAridOridKeyTransformer).collect(Collectors.toSet());

    return Lists.partition(new ArrayList<>(assocDaoKeys), 500).stream()
      .map(partitionedAssocDaoKeys -> {
        var criteriaBuilder = entityManager.getCriteriaBuilder();
        var staMagCriteriaQuery = criteriaBuilder.createQuery(StaMagDao.class);
        var fromStaMag = staMagCriteriaQuery.from(StaMagDao.class);

        staMagCriteriaQuery.select(fromStaMag)
          .where(
            criteriaBuilder.or(partitionedAssocDaoKeys.stream()
              .map(assocDaoKey -> criteriaBuilder.and(
                criteriaBuilder.equal(fromStaMag.get(ARRIVAL_ID), assocDaoKey.getArrivalId()),
                criteriaBuilder.equal(fromStaMag.get(ORIGIN_ID), assocDaoKey.getOriginId())
              ))
              .toArray(Predicate[]::new)));

        try {
          return entityManager.createQuery(staMagCriteriaQuery).getResultStream()
            .collect(Collectors.toList());
        } catch (Exception ex) {
          logger.warn("Could not find StaMagDaos from list of OriginIdArrivalIdKeys:{}", partitionedAssocDaoKeys, ex);
          return Collections.<StaMagDao>emptyList();
        }
      })
      .flatMap(Collection::stream)
      .collect(Collectors.toList());
  }

  /**
   * Maps a {@link StaMagDao} to an {@link AridOridKey}. Useful for backreferencing a StaMagDao to an {@link AssocDao}
   *
   * @param staMagDao the {@link StaMagDao} to map
   * @return the relevant {@link AridOridKey}
   */
  public static AridOridKey staMagDaoKeyTransformer(StaMagDao staMagDao) {
    var key = new AridOridKey();
    key.setOriginId(staMagDao.getOriginId());
    key.setArrivalId(staMagDao.getArrivalId());
    return key;
  }
}
