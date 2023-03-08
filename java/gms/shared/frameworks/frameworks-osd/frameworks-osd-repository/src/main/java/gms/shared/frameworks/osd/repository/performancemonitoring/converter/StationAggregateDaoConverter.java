package gms.shared.frameworks.osd.repository.performancemonitoring.converter;

import com.google.common.base.Preconditions;
import gms.shared.frameworks.osd.coi.soh.DurationStationAggregate;
import gms.shared.frameworks.osd.coi.soh.PercentStationAggregate;
import gms.shared.frameworks.osd.coi.soh.StationAggregate;
import gms.shared.frameworks.osd.dao.soh.DurationStationAggregateDao;
import gms.shared.frameworks.osd.dao.soh.PercentStationAggregateDao;
import gms.shared.frameworks.osd.dao.soh.StationAggregateDao;
import gms.shared.frameworks.utilities.jpa.EntityConverter;

import javax.persistence.EntityManager;
import java.time.Duration;
import java.util.Objects;

public class StationAggregateDaoConverter implements EntityConverter<StationAggregateDao, StationAggregate<?>> {
  @Override
  public StationAggregateDao fromCoi(StationAggregate<?> coi, EntityManager entityManager) {
    Objects.requireNonNull(coi);
    Objects.requireNonNull(entityManager);
    Preconditions.checkState(entityManager.getTransaction().isActive(),
      "An active transaction is required.");

    StationAggregateDao dao;
    if (coi instanceof PercentStationAggregate) {
      PercentStationAggregateDao percentDao = new PercentStationAggregateDao();
      percentDao.setValue((Double) coi.getValue().orElse(null));
      dao = percentDao;
    } else if (coi instanceof DurationStationAggregate) {
      DurationStationAggregateDao durationDao = new DurationStationAggregateDao();
      durationDao.setValue((Duration) coi.getValue().orElse(null));
      dao = durationDao;
    } else {
      throw new IllegalArgumentException(
        String.format("Unknown StationAggregateType: %s", coi.getClass().getTypeName()));
    }

    dao.setAggregateType(coi.getAggregateType());

    return dao;
  }

  @Override
  public StationAggregate<?> toCoi(StationAggregateDao entity) {
    Objects.requireNonNull(entity);

    if (entity instanceof PercentStationAggregateDao) {
      return PercentStationAggregate.from(
        ((PercentStationAggregateDao) entity).getValue(),
        entity.getAggregateType());
    } else if (entity instanceof DurationStationAggregateDao) {
      return DurationStationAggregate.from(
        ((DurationStationAggregateDao) entity).getValue(),
        entity.getAggregateType());
    } else {
      throw new IllegalArgumentException(
        String.format("Unknown StationAggregateDao type: %s", entity.getClass().getTypeName()));
    }
  }
}

