package gms.shared.frameworks.osd.repository.performancemonitoring.converter;

import com.google.common.base.Preconditions;
import gms.shared.frameworks.osd.coi.soh.ChannelSoh;
import gms.shared.frameworks.osd.coi.soh.SohMonitorValueAndStatus;
import gms.shared.frameworks.osd.dao.soh.ChannelSohDao;
import gms.shared.frameworks.utilities.jpa.EntityConverter;

import javax.persistence.EntityManager;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Converts to and from ChannelSoh to ChannelSohDao and vice versa
 */
public class ChannelSohDaoConverter implements EntityConverter<ChannelSohDao, ChannelSoh> {

  @Override
  public ChannelSohDao fromCoi(ChannelSoh coi, EntityManager entityManager) {

    Objects.requireNonNull(coi);
    Objects.requireNonNull(entityManager);
    Preconditions.checkState(entityManager.getTransaction().isActive(),
      "An active transaction is required to convert a ChannelSoh");

    var entity = new ChannelSohDao();
    entity.setSohStatus(coi.getSohStatusRollup());

    var monitorValueAndStatusConverter =
      new ChannelSohMonitorValueAndStatusDaoConverter();
    var allChannelSohMonitorValueAndStatusDao =
      coi.getAllSohMonitorValueAndStatuses()
        .stream()
        .map(smvs -> monitorValueAndStatusConverter.fromCoi(smvs, entityManager))
        .map(smvsDao -> {
          smvsDao.setChannelSoh(entity);
          smvsDao.setChannelName(coi.getChannelName());
          return smvsDao;
        })
        .collect(Collectors.toSet());

    entity.setChannelName(coi.getChannelName());
    entity.setAllMonitorValueAndStatuses(allChannelSohMonitorValueAndStatusDao);

    return entity;
  }

  @Override
  public ChannelSoh toCoi(ChannelSohDao entity) {
    Objects.requireNonNull(entity);

    var monitorValueStatusConverter =
      new ChannelSohMonitorValueAndStatusDaoConverter();
    Set<SohMonitorValueAndStatus<?>> allMonitorValueAndStatuses =
      entity.getAllMonitorValueAndStatuses()
        .stream()
        .map(monitorValueStatusConverter::toCoi)
        .collect(Collectors.toSet());

    return ChannelSoh.from(entity.getChannelName(),
      entity.getSohStatus(),
      allMonitorValueAndStatuses);
  }
}