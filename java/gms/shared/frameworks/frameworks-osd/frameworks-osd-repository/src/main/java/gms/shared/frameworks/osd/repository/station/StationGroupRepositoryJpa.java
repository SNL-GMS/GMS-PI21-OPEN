package gms.shared.frameworks.osd.repository.station;

import gms.shared.frameworks.osd.api.station.StationGroupRepositoryInterface;
import gms.shared.frameworks.osd.api.util.RepositoryExceptionUtils;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroupDefinition;
import gms.shared.frameworks.osd.dao.channel.StationDao;
import gms.shared.frameworks.osd.dao.channel.StationGroupDao;
import gms.shared.frameworks.osd.repository.utils.StationUtils;
import gms.shared.frameworks.utilities.jpa.JpaConstants.EntityGraphType;
import gms.shared.metrics.CustomMetric;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class StationGroupRepositoryJpa implements
  StationGroupRepositoryInterface {

  private EntityManagerFactory entityManagerFactory;
  static final Logger logger = LoggerFactory.getLogger(StationGroupRepositoryJpa.class);

  private static final CustomMetric<StationGroupRepositoryJpa, Long> stationGroupRepositoryRetrieveStationGroups =
    CustomMetric.create(CustomMetric::incrementer,
      "stationGroupRepositoryRetrieveStationGroups_hits:type=Counter", 0L);

  private static final CustomMetric<StationGroupRepositoryJpa, Long> stationGroupRepositoryStoreStationGroups =
    CustomMetric.create(CustomMetric::incrementer,
      "stationGroupRepositoryStoreStationGroups_hits:type=Counter", 0L);

  private static final CustomMetric<StationGroupRepositoryJpa, Long> stationGroupRepositoryUpdateStationGroups =
    CustomMetric.create(CustomMetric::incrementer,
      "stationGroupRepositoryUpdateStationGroups_hits:type=Counter", 0L);

  private static final CustomMetric<Long, Long> stationGroupRepositoryRetrieveStationGroupsDuration =
    CustomMetric.create(CustomMetric::updateTimingData,
      "stationGroupRepositoryRetrieveStationGroups_duration:type=Value", 0L);
  private static final CustomMetric<Long, Long> stationGroupRepositoryStoreStationGroupsDuration =
    CustomMetric.create(CustomMetric::updateTimingData,
      "stationGroupRepositoryStoreStationGroups_duration:type=Value", 0L);
  private static final CustomMetric<Long, Long> stationGroupRepositoryUpdateStationGroupsDuration =
    CustomMetric.create(CustomMetric::updateTimingData,
      "stationGroupRepositoryUpdateStationGroups_duration:type=Value", 0L);

  public StationGroupRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  @Override
  public List<StationGroup> retrieveStationGroups(
    Collection<String> stationGroupNames) {
    Validate.notEmpty(stationGroupNames);

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    StationUtils stationUtils = new StationUtils(entityManager);
    List<StationGroup> result = new ArrayList<>();

    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<StationGroupDao> stationGroupDaoCriteria = cb
      .createQuery(StationGroupDao.class);
    Root<StationGroupDao> fromStationGroup = stationGroupDaoCriteria
      .from(StationGroupDao.class);
    stationGroupDaoCriteria.select(fromStationGroup).distinct(true);
    stationGroupDaoCriteria.where(fromStationGroup.get("name").in(stationGroupNames));
    List<StationGroupDao> stationGroupDaos = entityManager.createQuery(stationGroupDaoCriteria)
      .setHint(EntityGraphType.LOAD.getValue(),
        entityManager.getEntityGraph("station-group-graph"))
      .getResultList();

    stationGroupRepositoryRetrieveStationGroups.updateMetric(this);
    Instant start = Instant.now();

    try {
      for (StationGroupDao dao : stationGroupDaos) {
        List<Station> stations = new ArrayList<>();
        final var stationDaos = dao.getStations();
        for (StationDao stationDao : stationDaos) {
          stations.add(stationUtils.generateStation(stationDao, false));
        }
        result.add(StationGroup.from(
          dao.getName(),
          dao.getDescription(),
          stations
        ));
      }
    } catch (IOException e) {
      logger.error("error retrieving station groups", e);
    } finally {
      entityManager.close();

      Instant finish = Instant.now();
      long timeElapsed = Duration.between(start, finish).toMillis();
      stationGroupRepositoryRetrieveStationGroupsDuration.updateMetric(timeElapsed);
    }
    return result;
  }

  @Override
  public void storeStationGroups(
    Collection<StationGroup> stationGroups) {
    Validate.notEmpty(stationGroups);
    EntityManager entityManager = entityManagerFactory.createEntityManager();

    entityManager.getTransaction().begin();

    stationGroupRepositoryStoreStationGroups.updateMetric(this);
    Instant start = Instant.now();

    try {
      for (StationGroup stationGroup : stationGroups) {
        List<StationDao> stationDaos = new ArrayList<>();
        for (Station station : stationGroup.getStations()) {
          StationDao stationDao = storeStation(entityManager, station);
          stationDaos.add(stationDao);
        }
        StationGroupDao stationGroupDao = StationGroupDao.from(
          stationGroup.getName(), stationGroup.getDescription(), stationDaos);
        entityManager.persist(stationGroupDao);
      }
      entityManager.getTransaction().commit();
    } catch (Exception e) {
      entityManager.getTransaction().rollback();
      throw RepositoryExceptionUtils.wrapWithContext("Error committing station groups: ", e);
    } finally {
      entityManager.close();

      Instant finish = Instant.now();
      long timeElapsed = Duration.between(start, finish).toMillis();
      stationGroupRepositoryStoreStationGroupsDuration.updateMetric(timeElapsed);
    }
  }

  @Override
  public void updateStationGroups(
    Collection<StationGroupDefinition> stationGroupDefinitions) {
    Validate.notEmpty(stationGroupDefinitions);
    EntityManager entityManager = entityManagerFactory.createEntityManager();

    entityManager.getTransaction().begin();

    stationGroupRepositoryUpdateStationGroups.updateMetric(this);
    Instant start = Instant.now();

    try {
      for (StationGroupDefinition stationGroupDefinition : stationGroupDefinitions) {

        StationGroupDao stationGroupDao = entityManager
          .find(StationGroupDao.class, stationGroupDefinition.getName());

        List<StationDao> stationDaos = new ArrayList<>();
        for (String stationName : stationGroupDefinition.getStationNames()) {
          StationDao stationDao = entityManager.find(StationDao.class, stationName);
          checkArgument(stationDao != null, "Station %s not found for group %s",
            stationName, stationGroupDefinition.getName());
          stationDaos.add(stationDao);
        }

        if (stationGroupDao != null) {
          logger.info("Updating existing station group {}", stationGroupDefinition.getName());
          stationGroupDao.setDescription(stationGroupDefinition.getDescription());
          stationGroupDao.setStations(stationDaos);
        } else {
          logger.info("Creating new station group {}", stationGroupDefinition.getName());
          stationGroupDao = StationGroupDao
            .from(stationGroupDefinition.getName(), stationGroupDefinition.getDescription(),
              stationDaos);
        }

        entityManager.persist(stationGroupDao);
      }
      entityManager.getTransaction().commit();
    } catch (Exception e) {
      entityManager.getTransaction().rollback();
      throw RepositoryExceptionUtils.wrapWithContext("Error updating station groups: ", e);
    } finally {
      entityManager.close();

      Instant finish = Instant.now();
      long timeElapsed = Duration.between(start, finish).toMillis();
      stationGroupRepositoryUpdateStationGroupsDuration.updateMetric(timeElapsed);
    }
  }

  private StationDao storeStation(EntityManager entityManager, Station station) {
    StationUtils stationUtils = new StationUtils(entityManager);
    StationDao stationDao;
    stationDao = entityManager.find(StationDao.class, station.getName());
    if (stationDao == null) {
      logger.info("Station {} does not currently exist in OSD. Attempting to create new entry...",
        station.getName());
      return stationUtils.storeStation(station);
    }
    return stationDao;
  }
}
