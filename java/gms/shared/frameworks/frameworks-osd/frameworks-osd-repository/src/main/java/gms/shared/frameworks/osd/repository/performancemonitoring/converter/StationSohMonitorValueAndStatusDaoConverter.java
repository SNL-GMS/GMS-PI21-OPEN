package gms.shared.frameworks.osd.repository.performancemonitoring.converter;

import com.google.common.base.Preconditions;
import gms.shared.frameworks.osd.coi.soh.DurationSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.PercentSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorValueAndStatus;
import gms.shared.frameworks.osd.dao.soh.StationSohMonitorValueAndStatusDao;
import gms.shared.frameworks.utilities.jpa.EntityConverter;

import javax.persistence.EntityManager;
import java.time.Duration;
import java.util.Objects;

public class StationSohMonitorValueAndStatusDaoConverter implements EntityConverter<StationSohMonitorValueAndStatusDao, SohMonitorValueAndStatus<?>> {

  @Override
  public StationSohMonitorValueAndStatusDao fromCoi(SohMonitorValueAndStatus coi, EntityManager entityManager) {
    Objects.requireNonNull(coi);
    Objects.requireNonNull(entityManager);
    Preconditions.checkState(entityManager.getTransaction().isActive());

    var dao = new StationSohMonitorValueAndStatusDao();
    if (coi instanceof PercentSohMonitorValueAndStatus) {
      Double val = (Double) coi.getValue().orElse(null);
      if (val != null) {
        dao.setPercent(val.floatValue());
      }
    } else if (coi instanceof DurationSohMonitorValueAndStatus) {
      var duration = (Duration) coi.getValue().orElse(null);
      //prefer int to long in order to save DB storage space but int won't store nanosec precision, but does store sec
      //and don't need nano-sec precision
      if (duration != null) {
        dao.setDuration((int) duration.getSeconds());
      }
    } else {
      throw new IllegalArgumentException("Unknown SohMonitorValueAndStatusType: " + coi.getClass().getTypeName());
    }

    dao.setMonitorType(coi.getMonitorType());
    dao.setStatus(coi.getStatus());

    return dao;
  }

  @Override
  public SohMonitorValueAndStatus<?> toCoi(StationSohMonitorValueAndStatusDao entity) {
    Objects.requireNonNull(entity);

    var sohMonitorType = entity.getMonitorType();
    if (sohMonitorType.getSohValueType() == SohMonitorType.SohValueType.PERCENT) {
      return PercentSohMonitorValueAndStatus.from(
        entity.getPercent() == null ? null : entity.getPercent().doubleValue(),
        entity.getStatus(),
        entity.getMonitorType());
    } else if (sohMonitorType.getSohValueType() == SohMonitorType.SohValueType.DURATION) {

      //convert to secs...don't need more precision and allows us to store in smaller columns
      return DurationSohMonitorValueAndStatus.from(
        entity.getDuration() == null ? null : Duration.ofSeconds(entity.getDuration()),
        entity.getStatus(),
        entity.getMonitorType());
    } else {
      throw new IllegalArgumentException("Unknown SohMonitorValueAndStatusDao type: " + entity.getClass().getTypeName());
    }
  }
}
