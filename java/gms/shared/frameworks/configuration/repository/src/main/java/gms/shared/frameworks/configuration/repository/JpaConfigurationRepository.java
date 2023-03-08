package gms.shared.frameworks.configuration.repository;

import gms.shared.frameworks.configuration.Configuration;
import gms.shared.frameworks.configuration.ConfigurationRepository;
import gms.shared.frameworks.configuration.repository.dao.ConfigurationDao;
import gms.shared.frameworks.configuration.repository.dao.converter.ConfigurationDaoConverter;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.frameworks.utilities.jpa.GmsEntityManagerFactory;
import gms.shared.frameworks.utilities.jpa.JpaConstants.EntityGraphType;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * This class will read configuration from a postgres Database
 * <p>
 * It implements the ConfigurationRepository interface and so will replace the FileConfigurationRepository Until the database is ready, this
 * will just proxy request to the File Configuration Repository and consumers won't know the difference when the backing store is changed to
 * a DB
 */
public class JpaConfigurationRepository implements
  ConfigurationRepository {

  static final Logger logger = LoggerFactory.getLogger(JpaConfigurationRepository.class);

  private javax.persistence.EntityManagerFactory entityManagerFactory;

  public JpaConfigurationRepository() {
  }

  public JpaConfigurationRepository(SystemConfig config) {
    Objects.requireNonNull(config, "SystemConfig can't be null");
    this.entityManagerFactory = GmsEntityManagerFactory.create(config, "processing-cfg");
  }

  public void setEntityManagerFactory(javax.persistence.EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  @Override
  public Optional<Configuration> get(String key) {
    logger.info("/get method called");
    if (key == null) {
      throw new NullPointerException("key can't be null");
    }
    EntityManager entityManager = this.entityManagerFactory.createEntityManager();

    try {
      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<ConfigurationDao> query = cb
        .createQuery(ConfigurationDao.class);
      Root<ConfigurationDao> root = query.from(ConfigurationDao.class);

      CriteriaQuery<ConfigurationDao> select = query.select(root).where(cb.like(root.get("name"), key));
      TypedQuery<ConfigurationDao> typedQuery = entityManager.createQuery(select);
      EntityGraph graph = entityManager.getEntityGraph(ConfigurationDao.ENTITY_GRAPH_NAME);
      typedQuery.setHint(EntityGraphType.FETCH.getValue(), graph);

      //Adding retry loop around config resolution as it should not return empty,
      // so if it does initially come up empty, retry a few then fail
      final RetryPolicy<Object> retryPolicy = new RetryPolicy<>()
        .withBackoff(1, 60, ChronoUnit.SECONDS)
        .withMaxAttempts(6)
        .handle(List.of(NoResultException.class, ExecutionException.class, IllegalStateException.class,
          InterruptedException.class))
        .onFailedAttempt(e -> logger.warn(
          "Empty config found, trying again as may be in a race with loading",
          e));
      Optional<Configuration> config = Failsafe.with(retryPolicy).get(() -> resolveConfig(typedQuery));

      return config;
    } catch (NoResultException e) {
      JpaConfigurationRepository.logger.error("Configuration not found", e);
      entityManager.getTransaction().rollback();
      return Optional.empty();
    } finally {
      entityManager.close();
    }
  }

  @Override
  public Collection<Configuration> getKeyRange(String keyPrefix) {
    logger.info("/range method called");
    if (keyPrefix == null) {
      throw new NullPointerException("keyPrefix can't be null");
    }
    EntityManager entityManager = this.entityManagerFactory.createEntityManager();
    try {
      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<ConfigurationDao> query = cb
        .createQuery(ConfigurationDao.class);
      Root<ConfigurationDao> root = query.from(ConfigurationDao.class);
      CriteriaQuery<ConfigurationDao> select = query.select(root).where(cb.like(root.get("name"), keyPrefix + "%"));
      TypedQuery<ConfigurationDao> typedQuery = entityManager.createQuery(select);
      EntityGraph graph = entityManager.getEntityGraph(ConfigurationDao.ENTITY_GRAPH_NAME);
      typedQuery.setHint(EntityGraphType.FETCH.getValue(), graph);
      List<ConfigurationDao> configurationDaoList = typedQuery.getResultList();
      ConfigurationDaoConverter converter = new ConfigurationDaoConverter();

      Collection<Configuration> configurationList = new ArrayList<>();
      configurationDaoList.forEach(configurationDao ->
        configurationList.add(converter.toCoi(configurationDao))
      );
      return configurationList;
    } finally {
      entityManager.close();
    }
  }

  @Override
  public Optional<Configuration> put(Configuration configuration) {
    logger.info("/put method called");
    if (configuration == null) {
      throw new NullPointerException("Configuration can't be null");
    }
    EntityManager entityManager = this.entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();
    try {
      removeExistingConfig(entityManager, Arrays.asList(configuration.getName()));
      ConfigurationDaoConverter cdc = new ConfigurationDaoConverter();
      ConfigurationDao cfg = cdc.fromCoi(configuration);
      entityManager.persist(cfg);
      entityManager.getTransaction().commit();
    } catch (Exception e) {
      JpaConfigurationRepository.logger.error("Error committing configuration", e);
      entityManager.getTransaction().rollback();
      return Optional.empty();
    } finally {
      entityManager.close();
    }
    return Optional.of(configuration);
  }

  private void removeExistingConfig(EntityManager entityManager, List<String> configurationNames) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<ConfigurationDao> query = cb
      .createQuery(ConfigurationDao.class);
    Root<ConfigurationDao> root = query.from(ConfigurationDao.class);

    CriteriaQuery<ConfigurationDao> select = query.select(root).where(root.get("name").in(configurationNames));
    TypedQuery<ConfigurationDao> typedQuery = entityManager.createQuery(select);

    try {
      typedQuery.getResultStream().forEach(entityManager::remove);
      entityManager.flush();
    } catch (NoResultException e) {
      //not an error...cfg doesn't exist so nothing to do
    }
  }

  @Override
  public Collection<Configuration> putAll(Collection<Configuration> configurations) {
    logger.info("/putAll method called");
    if (configurations == null || configurations.isEmpty()) {
      throw new NullPointerException("Configurations can't be null or empty");
    }

    Collection<Configuration> retVal = new ArrayList<>();
    List<String> configurationNames = configurations.stream().map(
      Configuration::getName).collect(Collectors.toList());

    EntityManager entityManager = this.entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();
    try {
      removeExistingConfig(entityManager, configurationNames);
      configurations.forEach(configuration ->
        {
          ConfigurationDaoConverter cdc = new ConfigurationDaoConverter();
          entityManager.persist(cdc.fromCoi(configuration));
          retVal.add(configuration);
        }
      );
      entityManager.getTransaction().commit();
    } catch (Exception e) {
      JpaConfigurationRepository.logger.error("Error committing configuration", e);
      entityManager.getTransaction().rollback();
    } finally {
      entityManager.close();
    }
    return retVal;
  }

  public Optional<Configuration> resolveConfig(TypedQuery<ConfigurationDao> typedQuery) {
    ConfigurationDao c = typedQuery.getSingleResult();
    ConfigurationDaoConverter converter = new ConfigurationDaoConverter();
    Configuration configuration = converter.toCoi(c);
    return Optional.of(configuration);
  }
}
