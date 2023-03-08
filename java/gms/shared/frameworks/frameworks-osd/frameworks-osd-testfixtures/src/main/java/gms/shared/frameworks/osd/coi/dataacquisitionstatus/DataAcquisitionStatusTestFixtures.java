package gms.shared.frameworks.osd.coi.dataacquisitionstatus;

import gms.shared.frameworks.osd.api.rawstationdataframe.AceiUpdates;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueAnalog;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import gms.shared.frameworks.osd.coi.soh.AcquiredStationSohExtract;
import gms.shared.frameworks.osd.coi.soh.StationSohIssue;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame.AuthenticationStatus;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrameMetadata;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFramePayloadFormat;
import gms.shared.frameworks.osd.coi.waveforms.WaveformSummary;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singleton;

public class DataAcquisitionStatusTestFixtures {

  private DataAcquisitionStatusTestFixtures() {

  }

  public static final Instant NOW = Instant.now();  // used as transferTime
  static final Instant receptionTime = NOW.plusSeconds(5);
  public static final Instant NOW_MINUS_FIVE_MINUTES = NOW.minusSeconds(300);
  public static final Instant NOW_MINUS_TEN_MINUTES = NOW_MINUS_FIVE_MINUTES.minusSeconds(300);
  public static final Instant NOW_MINUS_FIFTEEN_MINUTES = NOW_MINUS_TEN_MINUTES.minusSeconds(300);

  private static final WaveformSummary waveformSummary = WaveformSummary
    .from(UtilsTestFixtures.CHANNEL.getName(), Instant.EPOCH, Instant.EPOCH.plusSeconds(10));

// StationGroupSohStatus test fixtures

  // acknowledged status
  static final StationSohIssue acknowledged = StationSohIssue.from(false, Instant.EPOCH);
  static final StationSohIssue notAcknowledged = StationSohIssue.from(true, NOW);

  public static final AcquiredChannelEnvironmentIssueAnalog ACQUIRED_CHANNEL_SOH_ANALOG =
    AcquiredChannelEnvironmentIssueAnalog.from(
      UtilsTestFixtures.CHANNEL.getName(),
      AcquiredChannelEnvironmentIssueType.CLIPPED,
      NOW_MINUS_TEN_MINUTES,
      NOW_MINUS_FIVE_MINUTES,
      1.0);

  public static final AcquiredChannelEnvironmentIssueAnalog ACQUIRED_CHANNEL_SOH_ANALOG_TWO =
    AcquiredChannelEnvironmentIssueAnalog.from(
      UtilsTestFixtures.CHANNEL_TWO.getName(),
      AcquiredChannelEnvironmentIssueType.CLIPPED,
      NOW_MINUS_TEN_MINUTES,
      NOW_MINUS_FIVE_MINUTES,
      1.0);

  public static final AcquiredChannelEnvironmentIssueBoolean ACQUIRED_CHANNEL_SOH_BOOLEAN =
    AcquiredChannelEnvironmentIssueBoolean.from(
      UtilsTestFixtures.CHANNEL.getName(),
      AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED,
      NOW_MINUS_TEN_MINUTES,
      NOW_MINUS_FIVE_MINUTES,
      true);

  public static final AcquiredChannelEnvironmentIssueBoolean ACQUIRED_CHANNEL_SOH_BOOLEAN_TWO =
    AcquiredChannelEnvironmentIssueBoolean.from(
      UtilsTestFixtures.CHANNEL_TWO.getName(),
      AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED,
      NOW_MINUS_TEN_MINUTES,
      NOW_MINUS_FIVE_MINUTES,
      true);

  public static final List<AcquiredChannelEnvironmentIssueBoolean>
    ACQUIRED_CHANNEL_SOH_BOOLEAN_ISSUES = List
    .of(ACQUIRED_CHANNEL_SOH_BOOLEAN, ACQUIRED_CHANNEL_SOH_BOOLEAN_TWO);

  public static final AceiUpdates ACQUIRED_CHANNEL_ENVIRONMENT_ISSUES = AceiUpdates.builder()
    .setBooleanInserts(singleton(ACQUIRED_CHANNEL_SOH_BOOLEAN))
    .setAnalogInserts(singleton(ACQUIRED_CHANNEL_SOH_ANALOG))
    .build();

  public static final AceiUpdates ACQUIRED_CHANNEL_ENVIRONMENT_ISSUES_BATCH_TEST = AceiUpdates
    .builder()
    .setBooleanInserts(Set.of(
      AcquiredChannelEnvironmentIssueBoolean.from(
        UtilsTestFixtures.CHANNEL.getName(),
        AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED,
        Instant.EPOCH,
        Instant.EPOCH.plus(1, ChronoUnit.SECONDS),
        true),
      AcquiredChannelEnvironmentIssueBoolean.from(
        UtilsTestFixtures.CHANNEL.getName(),
        AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED,
        Instant.EPOCH.plus(3, ChronoUnit.SECONDS),
        Instant.EPOCH.plus(4, ChronoUnit.SECONDS),
        true),
      AcquiredChannelEnvironmentIssueBoolean.from(
        UtilsTestFixtures.CHANNEL.getName(),
        AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED,
        Instant.EPOCH.plus(6, ChronoUnit.SECONDS),
        Instant.EPOCH.plus(7, ChronoUnit.SECONDS),
        true)
    ))
    .build();


  private static RawStationDataFrameMetadata metadata = RawStationDataFrameMetadata.builder()
    .setStationName(UtilsTestFixtures.CHANNEL.getStation())
    .setChannelNames(List.of(UtilsTestFixtures.CHANNEL.getName()))
    .setPayloadFormat(RawStationDataFramePayloadFormat.CD11)
    .setReceptionTime(Instant.EPOCH)
    .setWaveformSummaries(Map.of("ac1", waveformSummary))
    .setPayloadStartTime(Instant.EPOCH)
    .setPayloadEndTime(Instant.EPOCH)
    .setAuthenticationStatus(AuthenticationStatus.NOT_YET_AUTHENTICATED)
    .build();
  static AcquiredStationSohExtract acquiredStationSohExtract = AcquiredStationSohExtract
    .create(List.of(metadata), List.of(ACQUIRED_CHANNEL_SOH_ANALOG, ACQUIRED_CHANNEL_SOH_BOOLEAN));

}