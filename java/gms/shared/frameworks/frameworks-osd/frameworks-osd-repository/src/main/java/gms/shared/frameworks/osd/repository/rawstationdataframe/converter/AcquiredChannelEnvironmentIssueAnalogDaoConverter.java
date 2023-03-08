package gms.shared.frameworks.osd.repository.rawstationdataframe.converter;

import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueAnalog;
import gms.shared.frameworks.osd.dao.channel.ChannelDao;
import gms.shared.frameworks.osd.dao.channelsoh.AcquiredChannelEnvironmentIssueAnalogDao;
import gms.shared.frameworks.utilities.jpa.EntityConverter;

import javax.persistence.EntityManager;
import java.util.Objects;

public class AcquiredChannelEnvironmentIssueAnalogDaoConverter
  implements EntityConverter<AcquiredChannelEnvironmentIssueAnalogDao, AcquiredChannelEnvironmentIssueAnalog> {

  @Override
  public AcquiredChannelEnvironmentIssueAnalogDao fromCoi(AcquiredChannelEnvironmentIssueAnalog aceiAnalog,
    EntityManager entityManager) {
    Objects.requireNonNull(aceiAnalog);
    Objects.requireNonNull(entityManager);

    var channelDao = entityManager.getReference(ChannelDao.class, aceiAnalog.getChannelName());
    var sohAnalogDao = new AcquiredChannelEnvironmentIssueAnalogDao();
    sohAnalogDao.setChannel(channelDao);
    sohAnalogDao.setChannelName(aceiAnalog.getChannelName());
    sohAnalogDao.setType(aceiAnalog.getType());
    sohAnalogDao.setStartTime(aceiAnalog.getStartTime());
    sohAnalogDao.setEndTime(aceiAnalog.getEndTime());
    sohAnalogDao.setStatus(aceiAnalog.getStatus());

    return sohAnalogDao;
  }

  @Override
  public AcquiredChannelEnvironmentIssueAnalog toCoi(
    AcquiredChannelEnvironmentIssueAnalogDao sohAnalogDao) {
    Objects.requireNonNull(sohAnalogDao);

    return AcquiredChannelEnvironmentIssueAnalog.from(
      sohAnalogDao.getChannelName(),
      sohAnalogDao.getType(),
      sohAnalogDao.getStartTime(),
      sohAnalogDao.getEndTime(),
      sohAnalogDao.getStatus());
  }
}
