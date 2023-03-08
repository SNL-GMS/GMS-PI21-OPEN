package gms.shared.utilities.bridge.database.connector;

import com.google.common.collect.Lists;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class DatabaseConnector {

  private final EntityManagerFactory entityManagerFactory;

  protected DatabaseConnector(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  protected <T> T runWithEntityManager(Function<EntityManager, T> entityManagerFunction) {
    final var entityManager = entityManagerFactory.createEntityManager();
    try {
      return entityManagerFunction.apply(entityManager);
    } finally {
      entityManager.close();
    }
  }

  /**
   * Breaks the query into partitionSize batches and makes individual Oracle queries for each partition
   *
   * @param queryParams - query params to partition
   * @param partitionSize - number of params in each partition
   * @param partitionQueryFunction - function to query for each partition
   * @return list of DAOs
   */
  protected <P, T> List<T> runPartitionedQuery(Collection<P> queryParams,
    int partitionSize, Function<List<P>, List<T>> partitionQueryFunction) {
    return Lists.partition(new ArrayList<>(queryParams), partitionSize).stream()
      .map(partitionQueryFunction)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());
  }
}
