package gms.shared.frameworks.soh.statuschange.converter;

import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.quieting.SohStatusChange;
import gms.shared.frameworks.osd.dao.channel.ChannelDao;
import gms.shared.frameworks.osd.dao.soh.statuschange.SohStatusChangeDao;
import gms.shared.frameworks.utilities.jpa.EntityConverter;

import javax.persistence.EntityManager;
import java.util.Objects;

public class SohStatusChangeDaoConverter implements
  EntityConverter<SohStatusChangeDao, SohStatusChange> {

  @Override
  public SohStatusChangeDao fromCoi(SohStatusChange coi, EntityManager entityManager) {
    Objects.requireNonNull(coi);
    Objects.requireNonNull(entityManager);

    SohStatusChangeDao dao = new SohStatusChangeDao();
    dao.setFirstChangeTime(coi.getFirstChangeTime());
    dao.setSohMonitorType(coi.getSohMonitorType());
    dao.setChangedChannel(entityManager.getReference(ChannelDao.class, coi.getChangedChannel()));
    return dao;
  }

  @Override
  public SohStatusChange toCoi(SohStatusChangeDao dao) {

    SohMonitorType sohMonitorType = dao.getSohMonitorType();
    return SohStatusChange
      .from(dao.getFirstChangeTime(), sohMonitorType, dao.getChangedChannel().getName());

  }

}
