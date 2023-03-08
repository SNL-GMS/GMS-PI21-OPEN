package gms.shared.stationdefinition.database.connector;

import gms.shared.stationdefinition.dao.css.NetworkDao;
import gms.shared.utilities.bridge.database.connector.DatabaseConnector;
import gms.shared.utilities.bridge.database.connector.DatabaseConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class NetworkDatabaseConnector extends DatabaseConnector {

  private static final String NET = "net";

  public static final String ERROR_RETRIEVING_NETWORK_FOR = "Error Retrieving Network For '%s'";

  private static final Logger logger = LoggerFactory.getLogger(NetworkDatabaseConnector.class);

  public NetworkDatabaseConnector(@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {

    super(entityManagerFactory);
  }

  public Optional<NetworkDao> findNetwork(String net) {

    if (net == null || net.isEmpty()) {
      final String message = "Request for Network was missing networkId input";
      logger.debug(message);
      return Optional.empty();
    } else {
      return runWithEntityManager(entityManager -> {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<NetworkDao> query = cb.createQuery(NetworkDao.class);
        Root<NetworkDao> fromNetwork = query.from(NetworkDao.class);

        query.select(fromNetwork);

        final Path<Object> channelId = fromNetwork.get(NET);
        query.where(cb.and(
          cb.equal(channelId, net)
        ));

        try {
          return Optional.of(entityManager.createQuery(query).getSingleResult());
        } catch (NoResultException e) {
          final String message = String.format("No Network Found for '%s'", net);
          logger.warn(message, e);
          return Optional.empty();
        } catch (NonUniqueResultException e) {
          final String message = String.format("No Unique Network Found for '%s'", net);
          logger.warn(message, e);
          return Optional.empty();
        } catch (Exception e) {
          final String message = String.format(ERROR_RETRIEVING_NETWORK_FOR, net);
          throw new DatabaseConnectorException(message, e);
        }
      });
    }
  }

  public List<NetworkDao> findNetworks(Collection<String> nets) {

    if (nets == null || nets.isEmpty()) {
      final String message = "Request for Network was missing networkId input";
      logger.debug(message);
      return List.of();
    } else {
      return runWithEntityManager(entityManager ->
        runPartitionedQuery(nets, 950, partition -> {

          CriteriaBuilder cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<NetworkDao> query = cb.createQuery(NetworkDao.class);
          Root<NetworkDao> fromNetwork = query.from(NetworkDao.class);

          query.select(fromNetwork);

          final Path<Object> channelId = fromNetwork.get(NET);
          query.where(cb.and(
            channelId.in(partition)
          ));

          try {
            return entityManager.createQuery(query).getResultList();
          } catch (Exception e) {
            final String message = String.format(ERROR_RETRIEVING_NETWORK_FOR, nets);
            throw new DatabaseConnectorException(message, e);
          }
        })
      );
    }
  }
}
