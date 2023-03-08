package gms.shared.frameworks.osd.repository.performancemonitoring.converter;

import gms.shared.frameworks.osd.coi.soh.ChannelSoh;
import gms.shared.frameworks.osd.coi.soh.SohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.StationAggregate;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import gms.shared.frameworks.osd.dao.soh.ChannelSohDao;
import gms.shared.frameworks.osd.dao.soh.StationAggregateDao;
import gms.shared.frameworks.osd.dao.soh.StationSohDao;
import gms.shared.frameworks.utilities.jpa.EntityConverter;

import javax.persistence.EntityManager;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Converts to and from StationSoh to StationSohDao and vice versa
 */
public class StationSohDaoConverter implements EntityConverter<StationSohDao, StationSoh> {

  @Override
  public StationSohDao fromCoi(StationSoh coi, EntityManager entityManager) {
    Objects.requireNonNull(coi);
    Objects.requireNonNull(entityManager);

    var dao = new StationSohDao();

    dao.setCoiId(coi.getId());
    dao.setCreationTime(coi.getTime());

    dao.setStationName(coi.getStationName());
    dao.setSohStatus(coi.getSohStatusRollup());

    var smvsConverter =
      new StationSohMonitorValueAndStatusDaoConverter();
    var smvsDaos = coi
      .getSohMonitorValueAndStatuses()
      .stream()
      .map(smvs -> smvsConverter
        .fromCoi(smvs, entityManager))
      .map(smvsDao -> {
        smvsDao.setStationSoh(dao);
        smvsDao.setCreationTime(coi.getTime());
        return smvsDao;
      })
      .collect(Collectors.toSet());

    dao.setSohMonitorValueAndStatuses(smvsDaos);

    var channelSohDaoConverter = new ChannelSohDaoConverter();
    Set<ChannelSohDao> channelSohDaos = coi.getChannelSohs()
      .stream()
      .map(channelSoh -> channelSohDaoConverter.fromCoi(channelSoh, entityManager))
      .map(channelSohDao -> {
        channelSohDao.setStationSoh(dao);
        channelSohDao.setStationName(coi.getStationName());
        channelSohDao.setCreationTime(coi.getTime());
        channelSohDao.getAllMonitorValueAndStatuses().stream()
          .forEach(smvsDao -> {
              smvsDao.setCreationTime(coi.getTime());
              smvsDao.setStationName(coi.getStationName());
            });
        return channelSohDao;
      })
      .collect(Collectors.toSet());

    dao.setChannelSohs(channelSohDaos);

    var stationAgConverter = new StationAggregateDaoConverter();
    Set<StationAggregateDao> allStationAggregateDaos = coi.getAllStationAggregates()
      .stream()
      .map(stationAg -> stationAgConverter
        .fromCoi(stationAg, entityManager))
      .map(stationAgDao -> {
          stationAgDao.setStationSoh(dao);
          stationAgDao.setCreationTime(coi.getTime());
          return stationAgDao;
        })
      .collect(Collectors.toSet());
    dao.setAllStationAggregate(allStationAggregateDaos);

    return dao;
  }

  @Override
  public StationSoh toCoi(StationSohDao entity) {
    Set<SohMonitorValueAndStatus<?>> smvs = entity.getSohMonitorValueAndStatuses()
      .stream()
      .map(smvsDao -> new StationSohMonitorValueAndStatusDaoConverter().toCoi(smvsDao))
      .collect(Collectors.toSet());

    Set<ChannelSoh> channelSohs = entity.getChannelSohs()
      .stream()
      .map(channelSohDao -> new ChannelSohDaoConverter().toCoi(channelSohDao))
      .collect(Collectors.toSet());

    Set<StationAggregate<?>> allStationAggregates = entity.getAllStationAggregate()
      .stream()
      .map(stationAgDao -> new StationAggregateDaoConverter().toCoi(stationAgDao))
      .collect(Collectors.toSet());

    return StationSoh.from(entity.getCoiId(),
      entity.getCreationTime(),
      entity.getStationName(),
      smvs,
      entity.getSohStatus(),
      channelSohs,
      allStationAggregates);
  }
}
