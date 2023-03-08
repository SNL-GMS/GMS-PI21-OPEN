package gms.shared.event.repository.connector;

import com.google.common.collect.Lists;
import gms.shared.event.dao.GaTagDao;
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Manages querying {@link GaTagDao} from the database
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GaTagDatabaseConnector {

  private static final Logger logger = LoggerFactory.getLogger(GaTagDatabaseConnector.class);

  private final EntityManager entityManager;

  public GaTagDatabaseConnector(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  /**
   * Retrieves a list of {@link GaTagDao}s of provided object type, process state, and associated with the provided
   * evid
   *
   * @param objectType The desired object type of the returned {@link GaTagDao}s
   * @param processState The desired process state of the returned {@link GaTagDao}s
   * @param evid evid associated with the desired {@link GaTagDao}s
   * @return a list of {@link GaTagDao}s associated with the provided evid
   */
  public List<GaTagDao> findGaTagByObjectTypeProcessStateAndEvid(String objectType, String processState, long evid) {
    checkNotNull(objectType, "objectType must not be null");
    checkArgument(!objectType.isBlank(), "objectType must not be blank");
    checkNotNull(processState, "processState must not be null");
    checkArgument(!processState.isBlank(), "processState must not be blank");

    var criteriaBuilder = entityManager.getCriteriaBuilder();
    var gaTagCriteriaQuery = criteriaBuilder.createQuery(GaTagDao.class);
    var gaTagRoot = gaTagCriteriaQuery.from(GaTagDao.class);

    List<Predicate> predicates = new ArrayList<>();
    predicates.add(criteriaBuilder.equal(gaTagRoot.get("objectType"), objectType));
    predicates.add(criteriaBuilder.equal(gaTagRoot.get("processState"), processState));
    predicates.add(criteriaBuilder.equal(gaTagRoot.get("rejectedArrivalOriginEvid"), evid));

    gaTagCriteriaQuery.select(gaTagRoot).where(predicates.toArray(new Predicate[]{}));
    var gaTagQuery = entityManager.createQuery(gaTagCriteriaQuery);

    try {
      return gaTagQuery.getResultList();
    } catch (Exception e) {
      final var message = String.format(
        "Error retrieving GaTags with object type %s, process state %s, and rejected evid '%d'",
        objectType, processState, evid);
      throw new DatabaseConnectorException(message, e);
    }
  }

  /**
   * Retrieves a map of {@link GaTagDao}s of provided object types, process states, and associated with the provided
   * evids
   *
   * @param objectTypes The desired object type of the returned {@link GaTagDao}s
   * @param processStates The desired process state of the returned {@link GaTagDao}s
   * @param evids evids associated with the desired {@link GaTagDao}s
   * @return a map of {@link GaTagDao}s associated with the provided evids
   */
  public List<GaTagDao>
  findGaTagsByObjectTypesProcessStatesAndEvids(Collection<String> objectTypes, Collection<String> processStates,
    Collection<Long> evids) {
    checkNotNull(objectTypes, "objectTypes must not be null");
    checkArgument(!objectTypes.isEmpty(), "objectTypes must not be empty");
    checkArgument(objectTypes.stream().noneMatch(String::isBlank), "objectTypes must not be blank");
    checkNotNull(processStates, "processStates must not be null");
    checkArgument(!processStates.isEmpty(), "processStates must not be empty");
    checkArgument(processStates.stream().noneMatch(String::isBlank), "processStates must not be blank");

    return Lists.partition(new ArrayList<>(evids), 300).stream()
      .map(partitionedEvids -> {
        var criteriaBuilder = entityManager.getCriteriaBuilder();
        var gaTagCriteriaQuery = criteriaBuilder.createQuery(GaTagDao.class);
        var gaTagRoot = gaTagCriteriaQuery.from(GaTagDao.class);

        var predicates = objectTypes.stream().flatMap(objectType ->
          processStates.stream().flatMap(processState ->
            partitionedEvids.stream().map(evid ->
              criteriaBuilder.and(
                criteriaBuilder.equal(gaTagRoot.get("objectType"), objectType),
                criteriaBuilder.equal(gaTagRoot.get("processState"), processState),
                criteriaBuilder.equal(gaTagRoot.get("rejectedArrivalOriginEvid"), evid)
              )))).collect(Collectors.toList());

        gaTagCriteriaQuery.select(gaTagRoot).where(
          criteriaBuilder.or(predicates.toArray(Predicate[]::new)));

        try {
          return entityManager.createQuery(gaTagCriteriaQuery).getResultStream()
            .collect(Collectors.toList());
        } catch (Exception ex) {
          logger.warn("Could not find GaTagDaos from list of evids: {} with objectTypes: {} and processStates: {}",
            partitionedEvids, objectTypes, processStates);
          return Collections.<GaTagDao>emptyList();
        }

      })
      .flatMap(Collection::stream)
      .collect(Collectors.toList());
  }
}
