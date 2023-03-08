package gms.shared.frameworks.injector;

import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueAnalog;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AceiIdModifier implements
  Modifier<Iterable<? extends AcquiredChannelEnvironmentIssue<?>>> {

  private static final Logger logger = LoggerFactory.getLogger(AceiIdModifier.class);

  private Instant startTime = null;
  private Instant endTime = null;

  private final Duration interval;

  public AceiIdModifier() {
    this(Duration.ofSeconds(20));
  }

  public AceiIdModifier(Duration interval) {
    this.interval = interval;
  }

  @Override
  public List<? extends AcquiredChannelEnvironmentIssue<?>> apply(
    Iterable<? extends AcquiredChannelEnvironmentIssue<?>> aceiList) {

    List<AcquiredChannelEnvironmentIssue<?>> newAceiList = new ArrayList<>();

    for (AcquiredChannelEnvironmentIssue<?> acei : aceiList) {

      if (startTime == null) {
        startTime = acei.getStartTime();
      }
      if (endTime == null) {
        endTime = acei.getEndTime();
      }

      if (acei instanceof AcquiredChannelEnvironmentIssueBoolean) {
        AcquiredChannelEnvironmentIssueBoolean booleanAcei = (AcquiredChannelEnvironmentIssueBoolean) acei;

        AcquiredChannelEnvironmentIssueBoolean newBooleanAcei =
          AcquiredChannelEnvironmentIssueBoolean.from(
            booleanAcei.getChannelName(),
            booleanAcei.getType(),
            startTime,
            endTime,
            booleanAcei.getStatus()
          );
        newAceiList.add(newBooleanAcei);
      } else if (acei instanceof AcquiredChannelEnvironmentIssueAnalog) {
        AcquiredChannelEnvironmentIssueAnalog analogAcei = (AcquiredChannelEnvironmentIssueAnalog) acei;

        AcquiredChannelEnvironmentIssueAnalog newAnalogAcei =
          AcquiredChannelEnvironmentIssueAnalog.from(
            analogAcei.getChannelName(),
            analogAcei.getType(),
            startTime,
            endTime,
            analogAcei.getStatus()
          );
        newAceiList.add(newAnalogAcei);
      }

      startTime = startTime.plus(interval);
      endTime = endTime.plus(interval);
    }
    if (logger.isDebugEnabled()) {
      logger.debug("\n");
      logger.debug("ACEI Boolean Final Index Times:");
      for (AcquiredChannelEnvironmentIssue<?> acei : newAceiList) {
        logger.debug("{} : {} - {}", acei.getChannelName(), acei.getStartTime(),
          acei.getEndTime());
      }
    }
    return newAceiList;
  }
}
