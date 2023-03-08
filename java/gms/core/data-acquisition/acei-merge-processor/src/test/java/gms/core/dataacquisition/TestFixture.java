package gms.core.dataacquisition;

import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueAnalog;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import gms.shared.frameworks.osd.dao.channelsoh.AcquiredChannelEnvironmentIssueAnalogDao;
import gms.shared.frameworks.osd.dao.channelsoh.AcquiredChannelEnvironmentIssueBooleanDao;

import java.time.Instant;

public class TestFixture {

  public static final Instant EPOCH = Instant.EPOCH;


  public static class AcquiredChannelEnvironmentalIssuesSets {

    public static final AcquiredChannelEnvironmentIssueBoolean AARDVARK_CLOCKLOCK_0_2 = AcquiredChannelEnvironmentIssueBoolean
      .from(
        "aardvark",
        AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED,
        EPOCH,
        EPOCH.plusSeconds(2).minusMillis(25),
        true
      );
    public static final AcquiredChannelEnvironmentIssueBoolean AARDVARK_CLOCKLOCK_0_4 = AcquiredChannelEnvironmentIssueBoolean
      .from(
        "aardvark",
        AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED,
        EPOCH,
        EPOCH.plusSeconds(4).minusMillis(25),
        true
      );
    public static final AcquiredChannelEnvironmentIssueBoolean AARDVARK_CLOCKLOCK_0_6 = AcquiredChannelEnvironmentIssueBoolean
      .from(
        "aardvark",
        AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED,
        EPOCH,
        EPOCH.plusSeconds(6).minusMillis(25),
        true
      );
    public static final AcquiredChannelEnvironmentIssueBoolean AARDVARK_CLOCKLOCK_1_2 = AcquiredChannelEnvironmentIssueBoolean
      .from(
        "aardvark",
        AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED,
        EPOCH.plusSeconds(1),
        EPOCH.plusSeconds(2).minusMillis(25),
        true
      );
    public static final AcquiredChannelEnvironmentIssueBoolean AARDVARK_CLOCKLOCK_2_4 = AcquiredChannelEnvironmentIssueBoolean
      .from(
        "aardvark",
        AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED,
        EPOCH.plusSeconds(2),
        EPOCH.plusSeconds(4).minusMillis(25),
        true
      );
    public static final AcquiredChannelEnvironmentIssueBoolean AARDVARK_CLOCKLOCK_3_4 = AcquiredChannelEnvironmentIssueBoolean
      .from(
        "aardvark",
        AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED,
        EPOCH.plusSeconds(3),
        EPOCH.plusSeconds(4).minusMillis(25),
        true
      );
    public static final AcquiredChannelEnvironmentIssueBoolean AARDVARK_CLOCKLOCK_2_6 = AcquiredChannelEnvironmentIssueBoolean
      .from(
        "aardvark",
        AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED,
        EPOCH.plusSeconds(2),
        EPOCH.plusSeconds(6).minusMillis(25),
        true
      );
    public static final AcquiredChannelEnvironmentIssueBoolean AARDVARK_CLOCKLOCK_4_6 = AcquiredChannelEnvironmentIssueBoolean
      .from(
        "aardvark",
        AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED,
        EPOCH.plusSeconds(4),
        EPOCH.plusSeconds(6).minusMillis(25),
        true
      );
    public static final AcquiredChannelEnvironmentIssueBoolean AARDVARK_CLOCKLOCK_4_6_F = AcquiredChannelEnvironmentIssueBoolean
      .from(
        "aardvark",
        AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED,
        EPOCH.plusSeconds(4),
        EPOCH.plusSeconds(6).minusMillis(25),
        false
      );

    public static final AcquiredChannelEnvironmentIssueBoolean AARDVARK_CLOCKLOCK_5_6 = AcquiredChannelEnvironmentIssueBoolean
      .from(
        "aardvark",
        AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED,
        EPOCH.plusSeconds(5),
        EPOCH.plusSeconds(6).minusMillis(25),
        true
      );

    public static final AcquiredChannelEnvironmentIssueBoolean BADGER_CLOCKLOCK_2_4 = AcquiredChannelEnvironmentIssueBoolean
      .from(
        "badger",
        AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED,
        EPOCH.plusSeconds(2),
        EPOCH.plusSeconds(4).minusMillis(25),
        true
      );

    public static final AcquiredChannelEnvironmentIssueBoolean BADGER_CLOCKLOCK_2_6 = AcquiredChannelEnvironmentIssueBoolean
      .from(
        "badger",
        AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED,
        EPOCH.plusSeconds(2),
        EPOCH.plusSeconds(6).minusMillis(25),
        true
      );

    public static final AcquiredChannelEnvironmentIssueBoolean BADGER_CLOCKLOCK_4_6 = AcquiredChannelEnvironmentIssueBoolean
      .from(
        "badger",
        AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED,
        EPOCH.plusSeconds(4),
        EPOCH.plusSeconds(6).minusMillis(25),
        true
      );

    public static final AcquiredChannelEnvironmentIssueBoolean AARDVARK_CLIPPED_0_2 = AcquiredChannelEnvironmentIssueBoolean
      .from(
        "aardvark",
        AcquiredChannelEnvironmentIssueType.CLIPPED,
        EPOCH,
        EPOCH.plusSeconds(2).minusMillis(25),
        true
      );

    public static final AcquiredChannelEnvironmentIssueBoolean AARDVARK_CLIPPED_0_4 = AcquiredChannelEnvironmentIssueBoolean
      .from(
        "aardvark",
        AcquiredChannelEnvironmentIssueType.CLIPPED,
        EPOCH,
        EPOCH.plusSeconds(4).minusMillis(25),
        true
      );

    public static final AcquiredChannelEnvironmentIssueBoolean AARDVARK_CLIPPED_0_6 = AcquiredChannelEnvironmentIssueBoolean
      .from(
        "aardvark",
        AcquiredChannelEnvironmentIssueType.CLIPPED,
        EPOCH,
        EPOCH.plusSeconds(6).minusMillis(25),
        true
      );

    public static final AcquiredChannelEnvironmentIssueBoolean AARDVARK_CLIPPED_2_4 = AcquiredChannelEnvironmentIssueBoolean
      .from(
        "aardvark",
        AcquiredChannelEnvironmentIssueType.CLIPPED,
        EPOCH.plusSeconds(2),
        EPOCH.plusSeconds(4).minusMillis(25),
        true
      );

    public static final AcquiredChannelEnvironmentIssueBoolean AARDVARK_CLIPPED_2_6 = AcquiredChannelEnvironmentIssueBoolean
      .from(
        "aardvark",
        AcquiredChannelEnvironmentIssueType.CLIPPED,
        EPOCH.plusSeconds(2),
        EPOCH.plusSeconds(6).minusMillis(25),
        true
      );

    public static final AcquiredChannelEnvironmentIssueBoolean AARDVARK_CLIPPED_4_6 = AcquiredChannelEnvironmentIssueBoolean
      .from(
        "aardvark",
        AcquiredChannelEnvironmentIssueType.CLIPPED,
        EPOCH.plusSeconds(4),
        EPOCH.plusSeconds(6).minusMillis(25),
        true
      );

    public static final AcquiredChannelEnvironmentIssueAnalog AARDVARK_DURATION_OUTAGE_0_4 = AcquiredChannelEnvironmentIssueAnalog
      .from(
        "aardvark",
        AcquiredChannelEnvironmentIssueType.DURATION_OUTAGE,
        EPOCH,
        EPOCH.plusSeconds(4).minusMillis(25),
        0
      );

    public static final AcquiredChannelEnvironmentIssueAnalog AARDVARK_DURATION_OUTAGE_4_6 = AcquiredChannelEnvironmentIssueAnalog
      .from(
        "aardvark",
        AcquiredChannelEnvironmentIssueType.DURATION_OUTAGE,
        EPOCH.plusSeconds(4),
        EPOCH.plusSeconds(6).minusMillis(25),
        1
      );
  }

  public static class AceiDaoSets {
    public static AcquiredChannelEnvironmentIssueBooleanDao aardvarkClocklock02() {
      var dao = new AcquiredChannelEnvironmentIssueBooleanDao();
      dao.setChannelName("aardvark");
      dao.setType(AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED);
      dao.setStartTime(EPOCH);
      dao.setEndTime(EPOCH.plusSeconds(2).minusMillis(25));
      dao.setStatus(true);

      return dao;
    }

    public static AcquiredChannelEnvironmentIssueBooleanDao aardvarkClocklock04() {
      var dao = new AcquiredChannelEnvironmentIssueBooleanDao();
      dao.setChannelName("aardvark");
      dao.setType(AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED);
      dao.setStartTime(EPOCH);
      dao.setEndTime(EPOCH.plusSeconds(4).minusMillis(25));
      dao.setStatus(true);

      return dao;
    }

    public static AcquiredChannelEnvironmentIssueBooleanDao aardvarkClocklock06() {
      var dao = new AcquiredChannelEnvironmentIssueBooleanDao();
      dao.setChannelName("aardvark");
      dao.setType(AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED);
      dao.setStartTime(EPOCH);
      dao.setEndTime(EPOCH.plusSeconds(6).minusMillis(25));
      dao.setStatus(true);

      return dao;
    }

    public static AcquiredChannelEnvironmentIssueBooleanDao aardvarkClocklock12() {
      var dao = new AcquiredChannelEnvironmentIssueBooleanDao();
      dao.setChannelName("aardvark");
      dao.setType(AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED);
      dao.setStartTime(EPOCH.plusSeconds(1));
      dao.setEndTime(EPOCH.plusSeconds(2).minusMillis(25));
      dao.setStatus(true);

      return dao;
    }

    public static AcquiredChannelEnvironmentIssueBooleanDao aardvarkClocklock24() {
      var dao = new AcquiredChannelEnvironmentIssueBooleanDao();
      dao.setChannelName("aardvark");
      dao.setType(AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED);
      dao.setStartTime(EPOCH.plusSeconds(2));
      dao.setEndTime(EPOCH.plusSeconds(4).minusMillis(25));
      dao.setStatus(true);

      return dao;
    }

    public static AcquiredChannelEnvironmentIssueBooleanDao aardvarkClocklock34() {
      var dao = new AcquiredChannelEnvironmentIssueBooleanDao();
      dao.setChannelName("aardvark");
      dao.setType(AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED);
      dao.setStartTime(EPOCH.plusSeconds(3));
      dao.setEndTime(EPOCH.plusSeconds(4).minusMillis(25));
      dao.setStatus(true);

      return dao;
    }

    public static AcquiredChannelEnvironmentIssueBooleanDao aardvarkClocklock26() {
      var dao = new AcquiredChannelEnvironmentIssueBooleanDao();
      dao.setChannelName("aardvark");
      dao.setType(AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED);
      dao.setStartTime(EPOCH.plusSeconds(2));
      dao.setEndTime(EPOCH.plusSeconds(6).minusMillis(25));
      dao.setStatus(true);

      return dao;
    }

    public static AcquiredChannelEnvironmentIssueBooleanDao aardvarkClocklock46() {
      var dao = new AcquiredChannelEnvironmentIssueBooleanDao();
      dao.setChannelName("aardvark");
      dao.setType(AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED);
      dao.setStartTime(EPOCH.plusSeconds(4));
      dao.setEndTime(EPOCH.plusSeconds(6).minusMillis(25));
      dao.setStatus(true);

      return dao;
    }

    public static AcquiredChannelEnvironmentIssueBooleanDao aardvarkClocklock46F() {
      var dao = new AcquiredChannelEnvironmentIssueBooleanDao();
      dao.setChannelName("aardvark");
      dao.setType(AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED);
      dao.setStartTime(EPOCH.plusSeconds(4));
      dao.setEndTime(EPOCH.plusSeconds(6).minusMillis(25));
      dao.setStatus(false);

      return dao;
    }

    public static AcquiredChannelEnvironmentIssueBooleanDao aardvarkClocklock56() {
      var dao = new AcquiredChannelEnvironmentIssueBooleanDao();
      dao.setChannelName("aardvark");
      dao.setType(AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED);
      dao.setStartTime(EPOCH.plusSeconds(5));
      dao.setEndTime(EPOCH.plusSeconds(6).minusMillis(25));
      dao.setStatus(true);

      return dao;
    }

    public static AcquiredChannelEnvironmentIssueBooleanDao badgerClocklock24() {
      var dao = new AcquiredChannelEnvironmentIssueBooleanDao();
      dao.setChannelName("badger");
      dao.setType(AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED);
      dao.setStartTime(EPOCH.plusSeconds(2));
      dao.setEndTime(EPOCH.plusSeconds(4).minusMillis(25));
      dao.setStatus(true);

      return dao;
    }

    public static AcquiredChannelEnvironmentIssueBooleanDao badgerClocklock26() {
      var dao = new AcquiredChannelEnvironmentIssueBooleanDao();
      dao.setChannelName("badger");
      dao.setType(AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED);
      dao.setStartTime(EPOCH.plusSeconds(2));
      dao.setEndTime(EPOCH.plusSeconds(6).minusMillis(25));
      dao.setStatus(true);

      return dao;
    }

    public static AcquiredChannelEnvironmentIssueBooleanDao badgerClocklock46() {
      var dao = new AcquiredChannelEnvironmentIssueBooleanDao();
      dao.setChannelName("badger");
      dao.setType(AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED);
      dao.setStartTime(EPOCH.plusSeconds(4));
      dao.setEndTime(EPOCH.plusSeconds(6).minusMillis(25));
      dao.setStatus(true);

      return dao;
    }

    public static AcquiredChannelEnvironmentIssueBooleanDao aardvarkClipped02() {
      var dao = new AcquiredChannelEnvironmentIssueBooleanDao();
      dao.setChannelName("aardvark");
      dao.setType(AcquiredChannelEnvironmentIssueType.CLIPPED);
      dao.setStartTime(EPOCH);
      dao.setEndTime(EPOCH.plusSeconds(2).minusMillis(25));
      dao.setStatus(true);

      return dao;
    }

    public static AcquiredChannelEnvironmentIssueBooleanDao aardvarkClipped04() {
      var dao = new AcquiredChannelEnvironmentIssueBooleanDao();
      dao.setChannelName("aardvark");
      dao.setType(AcquiredChannelEnvironmentIssueType.CLIPPED);
      dao.setStartTime(EPOCH);
      dao.setEndTime(EPOCH.plusSeconds(4).minusMillis(25));
      dao.setStatus(true);

      return dao;
    }

    public static AcquiredChannelEnvironmentIssueBooleanDao aardvarkClipped06() {
      var dao = new AcquiredChannelEnvironmentIssueBooleanDao();
      dao.setChannelName("aardvark");
      dao.setType(AcquiredChannelEnvironmentIssueType.CLIPPED);
      dao.setStartTime(EPOCH);
      dao.setEndTime(EPOCH.plusSeconds(6).minusMillis(25));
      dao.setStatus(true);

      return dao;
    }

    public static AcquiredChannelEnvironmentIssueBooleanDao aardvarkClipped24() {
      var dao = new AcquiredChannelEnvironmentIssueBooleanDao();
      dao.setChannelName("aardvark");
      dao.setType(AcquiredChannelEnvironmentIssueType.CLIPPED);
      dao.setStartTime(EPOCH.plusSeconds(2));
      dao.setEndTime(EPOCH.plusSeconds(4).minusMillis(25));
      dao.setStatus(true);

      return dao;
    }

    public static AcquiredChannelEnvironmentIssueBooleanDao aardvarkClipped26() {
      var dao = new AcquiredChannelEnvironmentIssueBooleanDao();
      dao.setChannelName("aardvark");
      dao.setType(AcquiredChannelEnvironmentIssueType.CLIPPED);
      dao.setStartTime(EPOCH.plusSeconds(2));
      dao.setEndTime(EPOCH.plusSeconds(6).minusMillis(25));
      dao.setStatus(true);

      return dao;
    }

    public static AcquiredChannelEnvironmentIssueBooleanDao aardvarkClipped46() {
      var dao = new AcquiredChannelEnvironmentIssueBooleanDao();
      dao.setChannelName("aardvark");
      dao.setType(AcquiredChannelEnvironmentIssueType.CLIPPED);
      dao.setStartTime(EPOCH.plusSeconds(4));
      dao.setEndTime(EPOCH.plusSeconds(6).minusMillis(25));
      dao.setStatus(true);

      return dao;
    }

    public static AcquiredChannelEnvironmentIssueAnalogDao aardvarkDurationOutage04() {
      var dao = new AcquiredChannelEnvironmentIssueAnalogDao();
      dao.setChannelName("aardvark");
      dao.setType(AcquiredChannelEnvironmentIssueType.DURATION_OUTAGE);
      dao.setStartTime(EPOCH);
      dao.setEndTime(EPOCH.plusSeconds(4).minusMillis(25));
      dao.setStatus(0);

      return dao;
    }

    public static AcquiredChannelEnvironmentIssueAnalogDao aardvarkDurationOutage46() {
      var dao = new AcquiredChannelEnvironmentIssueAnalogDao();
      dao.setChannelName("aardvark");
      dao.setType(AcquiredChannelEnvironmentIssueType.DURATION_OUTAGE);
      dao.setStartTime(EPOCH.plusSeconds(4));
      dao.setEndTime(EPOCH.plusSeconds(6).minusMillis(25));
      dao.setStatus(0);

      return dao;
    }
  }
}
