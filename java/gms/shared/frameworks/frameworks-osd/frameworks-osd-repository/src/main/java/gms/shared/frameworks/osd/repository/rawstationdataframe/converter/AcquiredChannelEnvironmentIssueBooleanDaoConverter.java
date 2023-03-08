package gms.shared.frameworks.osd.repository.rawstationdataframe.converter;

import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import gms.shared.frameworks.osd.dao.channel.ChannelDao;
import gms.shared.frameworks.osd.dao.channelsoh.AcquiredChannelEnvironmentIssueBooleanDao;
import gms.shared.frameworks.utilities.jpa.EntityConverter;

import javax.persistence.EntityManager;
import java.util.Objects;

public class AcquiredChannelEnvironmentIssueBooleanDaoConverter
  implements EntityConverter<AcquiredChannelEnvironmentIssueBooleanDao, AcquiredChannelEnvironmentIssueBoolean> {

  @Override
  public AcquiredChannelEnvironmentIssueBooleanDao fromCoi(AcquiredChannelEnvironmentIssueBoolean aceiBoolean,
    EntityManager entityManager) {
    Objects.requireNonNull(aceiBoolean);
    Objects.requireNonNull(entityManager);

    var channelDao = entityManager.getReference(ChannelDao.class, aceiBoolean.getChannelName());

    var sohBooleanDao = new AcquiredChannelEnvironmentIssueBooleanDao();
    sohBooleanDao.setChannel(channelDao);
    sohBooleanDao.setChannelName(aceiBoolean.getChannelName());
    sohBooleanDao.setType(aceiBoolean.getType());
    sohBooleanDao.setStartTime(aceiBoolean.getStartTime());
    sohBooleanDao.setEndTime(aceiBoolean.getEndTime());
    sohBooleanDao.setStatus(aceiBoolean.getStatus());

    return sohBooleanDao;
  }

  @Override
  public AcquiredChannelEnvironmentIssueBoolean toCoi(AcquiredChannelEnvironmentIssueBooleanDao aceiBooleanDao) {
    Objects.requireNonNull(aceiBooleanDao);

    return AcquiredChannelEnvironmentIssueBoolean.from(
      aceiBooleanDao.getChannelName(),
      aceiBooleanDao.getType(),
      aceiBooleanDao.getStartTime(),
      aceiBooleanDao.getEndTime(),
      aceiBooleanDao.isStatus());
  }
}
