package gms.shared.frameworks.osd.repository.performancemonitoring.converter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.dao.soh.CapabilitySohRollupDao;
import gms.shared.frameworks.osd.dao.soh.CapabilityStationStatusDao;
import gms.shared.frameworks.utilities.jpa.EntityConverter;

public class CapabilitySohRollupConverter
  implements EntityConverter<CapabilitySohRollupDao, CapabilitySohRollup> {

  @Override
  public CapabilitySohRollupDao fromCoi(CapabilitySohRollup coi, EntityManager entityManager) {
    Objects.requireNonNull(coi);
    Objects.requireNonNull(entityManager);

    CapabilitySohRollupDao dao = new CapabilitySohRollupDao();


    dao.setId(coi.getId());
    dao.setTime(coi.getTime());
    dao.setGroupRollupstatus(coi.getGroupRollupSohStatus());
    dao.setStationGroupName(coi.getForStationGroup());
    dao.setStationSohUUIDS(coi.getBasedOnStationSohs());


    List<CapabilityStationStatusDao> capabilityStationStatusDaos = new LinkedList<>();

    for (Map.Entry<String, SohStatus> entry : coi.getRollupSohStatusByStation().entrySet()) {
      CapabilityStationStatusDao capStatDao = new CapabilityStationStatusDao();
      capStatDao.setStationSohStatus(entry.getValue());
      capStatDao.setStationName(entry.getKey());
      capabilityStationStatusDaos.add(capStatDao);

    }
    dao.setRollupSohStatusByStation(capabilityStationStatusDaos);
    return dao;
  }

  @Override
  public CapabilitySohRollup toCoi(CapabilitySohRollupDao dao) {

    Set<UUID> basedOnStationSohs = dao.getStationSohUUIDs();

    Set<UUID> stationSohUUIDset = basedOnStationSohs
      .stream()
      .collect(Collectors.toSet());


    Map<String, SohStatus> rollupSohStatusByStation = new HashMap<>();

    for (CapabilityStationStatusDao capStatDao : dao.getRollupSohStatusByStation()) {
      rollupSohStatusByStation.put(capStatDao.getStationName(), capStatDao.getStationSohStatus());
    }

    return CapabilitySohRollup.create(dao.getId(), dao.getTime(), dao.getGroupRollupstatus(),
      dao.getStationGroupName(), stationSohUUIDset, rollupSohStatusByStation);
  }

}
