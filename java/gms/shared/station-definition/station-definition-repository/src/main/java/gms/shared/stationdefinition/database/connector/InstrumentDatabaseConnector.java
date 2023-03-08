package gms.shared.stationdefinition.database.connector;

import gms.shared.stationdefinition.dao.css.InstrumentDao;
import gms.shared.utilities.bridge.database.connector.DatabaseConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class InstrumentDatabaseConnector extends DatabaseConnector {

  private static final String INSTRUMENT_ID = "instrumentId";

  private static final Logger logger = LoggerFactory.getLogger(InstrumentDatabaseConnector.class);

  public InstrumentDatabaseConnector(@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {

    super(entityManagerFactory);
  }

  public List<InstrumentDao> findInstruments(Collection<Long> instrumentIds) {

    if (instrumentIds == null || instrumentIds.isEmpty()) {
      logger.debug("Request for Sensor by name was given an empty list of instrument ids");
      return new ArrayList<>();
    } else {
      return runWithEntityManager(entityManager ->
        runPartitionedQuery(instrumentIds, 950, partition -> {
          CriteriaBuilder cb = entityManager.getCriteriaBuilder();
          CriteriaQuery<InstrumentDao> query = cb.createQuery(InstrumentDao.class);
          Root<InstrumentDao> fromSensor = query.from(InstrumentDao.class);

          query.select(fromSensor);

          query.where(fromSensor.get(INSTRUMENT_ID).in(partition));

          return entityManager.createQuery(query).getResultList();
        })
      );
    }
  }
}
