package gms.shared.event.repository.connector;

import com.google.common.collect.Lists;
import gms.shared.event.dao.ArInfoDao;
import gms.shared.event.dao.OriginIdArrivalIdKey;
import gms.shared.signaldetection.dao.css.AridOridKey;
import gms.shared.signaldetection.dao.css.AssocDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Manages querying {@link gms.shared.event.dao.ArInfoDao} from the database
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ArInfoDatabaseConnector {

  private static final String ORIGIN_ID_ARRIVAL_ID_KEY = "originIdArrivalIdKey";
  private static final Logger logger = LoggerFactory.getLogger(ArInfoDatabaseConnector.class);

  private final EntityManager entityManager;

  public ArInfoDatabaseConnector(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  /**
   * Returns an {@link ArInfoDao} from the database
   *
   * @param orid the oridId of the query
   * @param arid the artidId of the query
   * @return a populated {@link ArInfoDao} if found, otherwise an empty Optional
   */
  public Optional<ArInfoDao> findArInfoByOridAndArid(long orid, long arid) {

    var criteriaBuilder = entityManager.getCriteriaBuilder();
    var arInfoCriteriaQuery = criteriaBuilder.createQuery(ArInfoDao.class);
    var fromArInfo = arInfoCriteriaQuery.from(ArInfoDao.class);
    var inputKey = new OriginIdArrivalIdKey();
    inputKey.setOriginId(orid);
    inputKey.setArrivalId(arid);

    arInfoCriteriaQuery.select(fromArInfo)
      .where(criteriaBuilder.equal(fromArInfo.get(ORIGIN_ID_ARRIVAL_ID_KEY), inputKey));

    try {
      return Optional.of(entityManager.createQuery(arInfoCriteriaQuery).getSingleResult());
    } catch (Exception ex) {
      logger.warn("Failed to retrieve ArInfo for orid:{} and arid:{}", inputKey.getOriginId(),
        inputKey.getArrivalId(), ex);
      return Optional.empty();
    }
  }

  /**
   * Returns a {@link Map} with {@link AridOridKey} keys and {@link ArInfoDao} values from the database
   *
   * @param assocs A list of {@link AssocDao} objects to retreive {@link ArInfoDao}s for
   * @return a populated Map of {@link ArInfoDao}s
   */
  public Map<AridOridKey, ArInfoDao> findArInfosByAssocs(Collection<AssocDao> assocs) {

    var assocDaoKeys = assocs.stream().map(this::assocDaoKeyTransformer).collect(Collectors.toSet());
    return Lists.partition(new ArrayList<>(assocDaoKeys), 500).stream()
      .map(partitionedAssocDaoKeys -> {
        var criteriaBuilder = entityManager.getCriteriaBuilder();
        var arInfoCriteriaQuery = criteriaBuilder.createQuery(ArInfoDao.class);
        var fromArInfo = arInfoCriteriaQuery.from(ArInfoDao.class);

        arInfoCriteriaQuery.select(fromArInfo)
          .where(fromArInfo.get(ORIGIN_ID_ARRIVAL_ID_KEY).in(partitionedAssocDaoKeys));

        try {
          return entityManager.createQuery(arInfoCriteriaQuery).getResultStream()
            .collect(Collectors.toList());
        } catch (Exception ex) {
          logger.warn("Could not find ArInfoDaos from list of OriginIdArrivalIdKeys:{}", partitionedAssocDaoKeys, ex);
          return Collections.<ArInfoDao>emptyList();
        }
      })
      .flatMap(Collection::stream)
      .collect(Collectors.toMap(this::arInfoDaoKeyTransformer, Function.identity()));

  }

  /**
   * Maps a {@link AssocDao} to an {@link OriginIdArrivalIdKey}.
   *
   * @param assocDao the {@link AssocDao} to map
   * @return the relevant {@link OriginIdArrivalIdKey}
   */
  private OriginIdArrivalIdKey assocDaoKeyTransformer(AssocDao assocDao) {
    var key = new OriginIdArrivalIdKey();
    key.setOriginId(assocDao.getId().getOriginId());
    key.setArrivalId(assocDao.getId().getArrivalId());
    return key;
  }

  /**
   * Maps an {@link ArInfoDao} to an {@link AridOridKey}. Useful for backreferencing a ArInfoDao to an {@link AssocDao}
   *
   * @param arInfoDao the {@link ArInfoDao} to map
   * @return the relevant {@link AridOridKey}
   */
  private AridOridKey arInfoDaoKeyTransformer(ArInfoDao arInfoDao) {
    var key = new AridOridKey();
    key.setOriginId(arInfoDao.getOriginId());
    key.setArrivalId(arInfoDao.getArrivalId());
    return key;
  }


}
