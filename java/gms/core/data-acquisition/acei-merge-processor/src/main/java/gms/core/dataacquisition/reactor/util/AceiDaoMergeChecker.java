package gms.core.dataacquisition.reactor.util;

import com.google.common.collect.Range;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.osd.dao.channelsoh.AcquiredChannelEnvironmentIssueBooleanDao;

import java.time.Duration;
import java.time.Instant;

/**
 * Implementation of {@link MergeChecker} to determine if two {@link
 * AcquiredChannelEnvironmentIssueBooleanDao}s share enough common
 * metadata and are close enough in time that they can be merged together.
 */
public class AceiDaoMergeChecker implements MergeChecker<AcquiredChannelEnvironmentIssueBooleanDao> {

  final MergeChecker<AcquiredChannelEnvironmentIssueBooleanDao> statusChecker = (acei1, acei2) ->
    acei1.isStatus() == acei2.isStatus();

  final MergeChecker<AcquiredChannelEnvironmentIssueBooleanDao> metadataChecker = (acei1, acei2) ->
    acei1.getChannelName().equals(acei2.getChannelName()) && acei1.getType()
      .equals(acei2.getType());

  private final ToleranceResolver toleranceResolver;

  AceiDaoMergeChecker(ToleranceResolver toleranceResolver) {
    this.toleranceResolver = toleranceResolver;
  }

  public static AceiDaoMergeChecker create(ConfigurationConsumerUtility config) {
    return new AceiDaoMergeChecker(ConfigurationToleranceResolver.create(config));
  }

  public static AceiDaoMergeChecker create(ToleranceResolver toleranceForChannelResolver) {
    return new AceiDaoMergeChecker(toleranceForChannelResolver);
  }

  @Override
  public boolean canMerge(AcquiredChannelEnvironmentIssueBooleanDao t1,
    AcquiredChannelEnvironmentIssueBooleanDao t2) {
    return (metadataChecker.and(statusChecker).canMerge(t1, t2)) && checkTimeRange(t1, t2);
  }

  private boolean checkTimeRange(AcquiredChannelEnvironmentIssueBooleanDao t1,
    AcquiredChannelEnvironmentIssueBooleanDao t2) {
    Duration mergeTolerance = toleranceResolver.apply(t1.getChannelName());

    Range<Instant> acei1Range = Range.closed(t1.getStartTime(), t1.getEndTime());

    Range<Instant> acei2RangeTolerancePadded = Range.closed(
      t2.getStartTime().minus(mergeTolerance),
      t2.getEndTime().plus(mergeTolerance));

    return acei1Range.isConnected(acei2RangeTolerancePadded);
  }

}

