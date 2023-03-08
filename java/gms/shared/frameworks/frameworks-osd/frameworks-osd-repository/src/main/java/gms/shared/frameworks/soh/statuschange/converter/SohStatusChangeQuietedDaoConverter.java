package gms.shared.frameworks.soh.statuschange.converter;

import gms.shared.frameworks.osd.coi.soh.quieting.QuietedSohStatusChange;
import gms.shared.frameworks.osd.dao.channel.ChannelDao;
import gms.shared.frameworks.osd.dao.channel.StationDao;
import gms.shared.frameworks.osd.dao.soh.statuschange.SohStatusChangeQuietedDao;
import gms.shared.frameworks.utilities.jpa.EntityConverter;

import javax.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;


public class SohStatusChangeQuietedDaoConverter implements EntityConverter<SohStatusChangeQuietedDao, QuietedSohStatusChange> {

  @Override
  public SohStatusChangeQuietedDao fromCoi(QuietedSohStatusChange coi, EntityManager entityManager) {


    SohStatusChangeQuietedDao dao = new SohStatusChangeQuietedDao();

    dao.setQuietedSohStatusChangeId(UUID.randomUUID());
    dao.setQuietUntil(coi.getQuietUntil());
    dao.setQuietDuration(coi.getQuietDuration());
    dao.setSohMonitorType(coi.getSohMonitorType());
    dao.setChannel(entityManager.getReference(ChannelDao.class, coi.getChannelName()));
    dao.setStation(entityManager.getReference(StationDao.class, coi.getStationName()));
    dao.setComment(coi.getComment().orElse(null));


    return dao;
  }

  @Override
  public QuietedSohStatusChange toCoi(SohStatusChangeQuietedDao dao) {

    QuietedSohStatusChange coi = QuietedSohStatusChange.create(dao.getQuietUntil(),
      dao.getQuietDuration(), dao.getSohMonitorType(),
      dao.getChannelName(), Optional.ofNullable(dao.getComment()), dao.getStationName());

    return coi;

  }


}
