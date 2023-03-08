package gms.shared.frameworks.soh.statuschange.converter;

import gms.shared.frameworks.osd.coi.soh.quieting.SohStatusChange;
import gms.shared.frameworks.osd.coi.soh.quieting.UnacknowledgedSohStatusChange;
import gms.shared.frameworks.osd.dao.channel.StationDao;
import gms.shared.frameworks.osd.dao.soh.statuschange.SohStatusChangeDao;
import gms.shared.frameworks.osd.dao.soh.statuschange.SohStatusChangeEventDao;
import gms.shared.frameworks.utilities.jpa.EntityConverter;

import javax.persistence.EntityManager;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SohStatusChangeEventDaoConverter
  implements EntityConverter<SohStatusChangeEventDao, UnacknowledgedSohStatusChange> {

  @Override
  public SohStatusChangeEventDao fromCoi(UnacknowledgedSohStatusChange coi, EntityManager entityManager) {
    Objects.requireNonNull(coi);
    Objects.requireNonNull(entityManager);

    SohStatusChangeEventDao dao = new SohStatusChangeEventDao();

    dao.setId(UUID.randomUUID());

    dao.setStationDao(entityManager.getReference(StationDao.class, coi.getStation()));

    Set<SohStatusChangeDao> sohStatusChangeDaos = coi.getSohStatusChanges()
      .stream()
      .map(sohStatusChange -> new SohStatusChangeDaoConverter().fromCoi(sohStatusChange, entityManager))
      .collect(Collectors.toSet());

    dao.setSohStatusChangeDaos(sohStatusChangeDaos);
    return dao;
  }

  @Override
  public UnacknowledgedSohStatusChange toCoi(SohStatusChangeEventDao dao) {
    Set<SohStatusChange> sohStatusChangeSet = dao.getSohStatusChangeDaos()
      .stream()
      .map(sohStatusChangeDao -> new SohStatusChangeDaoConverter().toCoi(sohStatusChangeDao))
      .collect(Collectors.toSet());

    return UnacknowledgedSohStatusChange.from(dao.getStationDao().getName(), sohStatusChangeSet);
  }

}
