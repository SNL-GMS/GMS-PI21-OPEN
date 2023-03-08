package gms.shared.frameworks.osd.repository.channel;

import gms.shared.frameworks.osd.api.channel.ChannelRepositoryInterface;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.dao.channel.ChannelDao;
import gms.shared.frameworks.osd.dao.channel.StationChannelInfoDao;
import gms.shared.frameworks.osd.dao.channel.StationChannelInfoKey;
import gms.shared.frameworks.osd.dao.channel.StationDao;
import gms.shared.frameworks.osd.repository.utils.ChannelUtils;
import gms.shared.metrics.CustomMetric;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChannelRepositoryJpa implements ChannelRepositoryInterface {

  private EntityManagerFactory entityManagerFactory;

  static final Logger logger = LoggerFactory.getLogger(ChannelRepositoryJpa.class);

  private static final CustomMetric<ChannelRepositoryJpa, Long> channelRepoRetrieveChannels =
    CustomMetric.create(CustomMetric::incrementer, "channelrepoRetrieveChannels_hits:type=Counter", 0L);

  private static final CustomMetric<ChannelRepositoryJpa, Long> channelRepoStoreChannels =
    CustomMetric.create(CustomMetric::incrementer, "channelRepoStoreChannels_hits:type=Counter", 0L);


  private static final CustomMetric<Long, Long> channelrepoRetrieveChannelsDuration =
    CustomMetric.create(CustomMetric::updateTimingData, "channelrepoRetrieveChannelsDuration:type=Value", 0L);

  private static final CustomMetric<Long, Long> channelRepoStoreChannelsDuration =
    CustomMetric.create(CustomMetric::updateTimingData, "channelRepoStoreChannelsDuration:type=Value", 0L);

  public ChannelRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  @Override
  public List<Channel> retrieveChannels(
    Collection<String> channelIds) {
    Validate.notNull(channelIds);
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    ChannelUtils channelUtils = new ChannelUtils(entityManager);

    List<Channel> result;

    channelRepoRetrieveChannels.updateMetric(this);
    Instant start = Instant.now();


    try {
      CriteriaBuilder cb = entityManager.getCriteriaBuilder();

      // this query will retrieve all the Channel info from database along with the station name
      // Looks like a fairly complex process but we will go through the steps one by one

      // 1. Create a CriteriaQuery of type <Type>. In this case, since we are creating a projection
      //    (grabbing a subset of columns from a table), we need to create a Tuple Query
      CriteriaQuery<Tuple> query = cb.createTupleQuery();

      // 2. Generate the from clause to indicate which table we are querying from. This will
      //    return a root object representing the entity in the clause (in the base situation, that
      //    in normal selects, this is equivalent to "SELECT id FROM" in SQL or "SELECT FROM" in
      //    JP QL). In the case shown here, we are using the Root of the StationChannelInfoDao class
      //    in order to create the Join statement on ChannelDao to grab those ChannelDao objects
      //    associated with the given StationDao based on the filter.
      Root<StationChannelInfoDao> fromStationChannelInfo = query.from(StationChannelInfoDao.class);
      fromStationChannelInfo.join("id");

      // 3. Generate the select (or multiselect). the difference between select and multiselect
      //    is that select will return the specific entity while multiselect will allow you to return
      //    projections. Specifically, a multiselect call is the equivalent of select(cb.tuple(...))
      //    where you would pass in the tuple object to generate from the select.
      final String channelColumnName = "channel";
      query.multiselect(fromStationChannelInfo.get("id").get("station"),
        fromStationChannelInfo.get("id").get(channelColumnName));

      if (!channelIds.isEmpty()) {
        // 4. if there are any constraints to add in the WHERE clause of JP QL (and subsequently in the
        //    generated SQL), you would add those by using the where function of the CriteriaQuery API.
        //    note that to create Predicates, you must use your already instantiated CriteriaBuilder in
        //    in order to create the constraints.
        query.where(cb.or(channelIds.stream()
          .map(id -> cb
            .equal(fromStationChannelInfo.get("id").get(channelColumnName).<String>get("name"),
              id))
          .toArray(Predicate[]::new)));
      }

      List<Tuple> queryResults = entityManager.createQuery(query).getResultList();

      // collect all the results
      result = new ArrayList<>();
      try {
        for (Tuple queryResult : queryResults) {
          StationDao stationDao = (StationDao) queryResult.get(0);
          ChannelDao channelDao = (ChannelDao) queryResult.get(1);
          result.addAll(channelUtils.constructChannels(List.of(channelDao), stationDao.getName()));
        }
      } catch (IOException e) {
        logger.error(e.getMessage());
      }
    } finally {
      entityManager.close();

      Instant finish = Instant.now();
      long timeElapsed = Duration.between(start, finish).toMillis();
      channelrepoRetrieveChannelsDuration.updateMetric(timeElapsed);
    }
    return result;
  }

  @Override
  public Set<String> storeChannels(Collection<Channel> channels) {
    Validate.notNull(channels);
    Set<String> result = new HashSet<>();
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    ChannelUtils channelUtils = new ChannelUtils(entityManager);

    channelRepoStoreChannels.updateMetric(this);
    Instant start = Instant.now();

    entityManager.getTransaction().begin();
    try {
      for (Channel channel : channels) {
        ChannelDao channelDao = ChannelDao.from(channel);
        entityManager.persist(channelDao);
        storeStationChannelInfo(entityManager, channelDao, channel.getStation());
        channelUtils.storeChannelConfiguredInputs(channelDao, channel);
        result.add(channelDao.getName());
      }
      entityManager.getTransaction().commit();
    } catch (Exception e) {
      entityManager.getTransaction().rollback();
      throw e;
    } finally {
      entityManager.close();

      Instant finish = Instant.now();
      long timeElapsed = Duration.between(start, finish).toMillis();
      channelRepoStoreChannelsDuration.updateMetric(timeElapsed);
    }
    return result;
  }

  private void storeStationChannelInfo(EntityManager entityManager, ChannelDao channelDao,
    String station) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<StationDao> stationQuery = cb.createQuery(StationDao.class);
    final Root<StationDao> fromStation = stationQuery.from(StationDao.class);
    stationQuery.select(fromStation);
    stationQuery.where(cb.equal(fromStation.get("name"), station));

    // Criteria API gives us the ability to either return a stream of results or, as shown in this case,
    // a single result.
    StationDao stationDao = entityManager.createQuery(stationQuery).getSingleResult();
    StationChannelInfoDao stationChannelInfo = new StationChannelInfoDao();
    stationChannelInfo.setId(new StationChannelInfoKey(stationDao, channelDao));
    // TODO: Need  tc compute relative position information between station and channel (in the case of derived channels)
    entityManager.persist(stationChannelInfo);
  }
}
