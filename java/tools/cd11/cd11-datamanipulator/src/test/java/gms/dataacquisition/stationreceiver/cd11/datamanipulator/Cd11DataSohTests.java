package gms.dataacquisition.stationreceiver.cd11.datamanipulator;

import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueAnalog;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Cd11DataSohTests {

  Cd11DataFrameSoh cd11DataFrameSoh = new Cd11DataFrameSoh(null, null, null, null);

  @Test
  void acquiredSohListToBytes_testEmptySoh() {
    List<AcquiredChannelEnvironmentIssue<?>> sohList = new ArrayList<>();
    var oldBytesArray = new byte[]{(byte) 0xC0, 0x00, 0x00, 0x00};
    var newSohBytesArray = cd11DataFrameSoh.acquiredSohListToBytes(sohList, oldBytesArray);
    assertArrayEquals(oldBytesArray, newSohBytesArray);
  }

  @Test
  void acquiredSohListToBytes_testNonEmptySoh() {
    List<AcquiredChannelEnvironmentIssue<?>> sohList = new ArrayList<>();
    var stationString = "SHZ";
    var startTimeInstant = Instant.parse("2020-02-28T17:44:40Z");
    var endTimeInstant = Instant.parse("2020-02-28T17:44:49.975Z");
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.DEAD_SENSOR_CHANNEL, startTimeInstant, endTimeInstant, false));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.ZEROED_DATA, startTimeInstant, endTimeInstant, false));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.CLIPPED, startTimeInstant, endTimeInstant, false));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.CALIBRATION_UNDERWAY, startTimeInstant, endTimeInstant, false));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.EQUIPMENT_HOUSING_OPEN, startTimeInstant, endTimeInstant, false));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.DIGITIZING_EQUIPMENT_OPEN, startTimeInstant, endTimeInstant, false));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.VAULT_DOOR_OPENED, startTimeInstant, endTimeInstant, false));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.AUTHENTICATION_SEAL_BROKEN, startTimeInstant, endTimeInstant, false));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.EQUIPMENT_MOVED, startTimeInstant, endTimeInstant, false));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.CLOCK_DIFFERENTIAL_TOO_LARGE, startTimeInstant, endTimeInstant, false));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.GPS_RECEIVER_OFF, startTimeInstant, endTimeInstant, false));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.GPS_RECEIVER_UNLOCKED, startTimeInstant, endTimeInstant, false));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.DIGITIZER_ANALOG_INPUT_SHORTED, startTimeInstant, endTimeInstant, false));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.DIGITIZER_CALIBRATION_LOOP_BACK, startTimeInstant, endTimeInstant, false));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.MAIN_POWER_FAILURE, startTimeInstant, endTimeInstant, false));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.BACKUP_POWER_UNSTABLE, startTimeInstant, endTimeInstant, false));
    sohList.add(AcquiredChannelEnvironmentIssueAnalog.from(stationString, AcquiredChannelEnvironmentIssueType.CLOCK_DIFFERENTIAL_IN_MICROSECONDS, startTimeInstant, endTimeInstant, 0.0));
    byte[] oldBytes = new byte[]{
      0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
      0x00, 0x00};
    byte[] newSohBytes = cd11DataFrameSoh.acquiredSohListToBytes(sohList, oldBytes);
    assertArrayEquals(oldBytes, newSohBytes);
  }

  @Test
  void acquiredSohListToBytes_testNonEmptySohWithChanges() {
    List<AcquiredChannelEnvironmentIssue<?>> sohList = new ArrayList<>();
    var stationString = "SHZ";
    var startTimeInstant = Instant.parse("2020-02-28T17:44:40Z");
    var endTimeInstant = Instant.parse("2020-02-28T17:44:49.975Z");
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.DEAD_SENSOR_CHANNEL, startTimeInstant, endTimeInstant, true));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.ZEROED_DATA, startTimeInstant, endTimeInstant, false));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.CLIPPED, startTimeInstant, endTimeInstant, true));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.CALIBRATION_UNDERWAY, startTimeInstant, endTimeInstant, false));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.EQUIPMENT_HOUSING_OPEN, startTimeInstant, endTimeInstant, true));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.DIGITIZING_EQUIPMENT_OPEN, startTimeInstant, endTimeInstant, false));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.VAULT_DOOR_OPENED, startTimeInstant, endTimeInstant, true));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.AUTHENTICATION_SEAL_BROKEN, startTimeInstant, endTimeInstant, false));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.EQUIPMENT_MOVED, startTimeInstant, endTimeInstant, true));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.CLOCK_DIFFERENTIAL_TOO_LARGE, startTimeInstant, endTimeInstant, false));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.GPS_RECEIVER_OFF, startTimeInstant, endTimeInstant, true));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.GPS_RECEIVER_UNLOCKED, startTimeInstant, endTimeInstant, false));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.DIGITIZER_ANALOG_INPUT_SHORTED, startTimeInstant, endTimeInstant, true));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.DIGITIZER_CALIBRATION_LOOP_BACK, startTimeInstant, endTimeInstant, false));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.MAIN_POWER_FAILURE, startTimeInstant, endTimeInstant, true));
    sohList.add(AcquiredChannelEnvironmentIssueBoolean.from(stationString, AcquiredChannelEnvironmentIssueType.BACKUP_POWER_UNSTABLE, startTimeInstant, endTimeInstant, false));
    sohList.add(AcquiredChannelEnvironmentIssueAnalog.from(stationString, AcquiredChannelEnvironmentIssueType.CLOCK_DIFFERENTIAL_IN_MICROSECONDS, startTimeInstant, endTimeInstant, 5.0));
    var oldBytesArray = new byte[]{
      0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
      0x00, 0x00};
    var newSohBytesArray = cd11DataFrameSoh.acquiredSohListToBytes(sohList, oldBytesArray);
    assertEquals(0b00000001, newSohBytesArray[0]);
    assertEquals(0b00000101, newSohBytesArray[1]);
    assertEquals(0b00010101, newSohBytesArray[2]);
    assertEquals(0b00001010, newSohBytesArray[3]);
    assertEquals(0b00000001, newSohBytesArray[4]);
    IntStream.range(5, 31).forEach(i -> assertEquals(0x00, newSohBytesArray[i]));
    assertEquals(0x05, newSohBytesArray[31]);
  }

}
