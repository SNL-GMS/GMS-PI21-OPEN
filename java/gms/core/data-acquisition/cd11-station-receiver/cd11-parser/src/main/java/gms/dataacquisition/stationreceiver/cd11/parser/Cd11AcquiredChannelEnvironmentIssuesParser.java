package gms.dataacquisition.stationreceiver.cd11.parser;

import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueAnalog;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Cd11AcquiredChannelEnvironmentIssuesParser {

  private static final Logger logger = LoggerFactory.getLogger(
    Cd11AcquiredChannelEnvironmentIssuesParser.class);

  private Cd11AcquiredChannelEnvironmentIssuesParser() {
  }

  /**
   * @param fields CD1.1 subframe fields
   * @param channelName Channel name of the subframe
   * @param startTime start of subframe's time range these soh values are valid for
   * @param endTime end of subframe's time range these soh values are valid for
   * @return List of {@link AcquiredChannelEnvironmentIssue} retrieved from the subframe's fields
   * @throws IllegalArgumentException if any of the fields are malformed per CD1.1 format
   */
  public static List<AcquiredChannelEnvironmentIssue<?>> parseAcquiredChannelSoh(
    byte[] fields,
    String channelName,
    Instant startTime, Instant endTime) {
    logger.debug("Parsing subframe for channel: {}, time range: {} - {}", channelName, startTime,
      endTime);

    List<AcquiredChannelEnvironmentIssue<?>> stateOfHealthSet = new ArrayList<>();
    var idx = 0;

    if (fields.length != 32) {
      logger.warn(
        "Unexpected channel status format for channel named {}. "
          + "Expected CD1.1 status fields with 32 bytes, but found {}. "
          + "State of health not generated",
        channelName, fields.length);
    } else {

      // If the first byte is equal to one, then the CD1.1 status format is expected.
      // TODO: For now, we allow parsing of SoH despite status marker
      if (fields[idx] != 1) {
        logger.warn(
          "Unexpected channel status format for channel named {}. "
            + "Expected CD1.1 status marker, value of 1, but found {}",
          channelName, fields[idx]);
      }

      // Data status byte
      idx = 1;
      stateOfHealthSet.addAll(parseDataStatusByte(fields[idx], channelName, startTime, endTime));

      // Channel security byte
      idx = 2;
      stateOfHealthSet.addAll(parseChannelSecurityByte(fields[idx], channelName, startTime, endTime));

      // Miscellaneous status byte
      idx = 3;
      stateOfHealthSet.addAll(parseMiscStatusByte(fields[idx], channelName, startTime, endTime));

      // Voltage indicator byte
      idx = 4;
      stateOfHealthSet.addAll(parseVoltageIndicatorByte(fields[idx], channelName, startTime, endTime));

      // Clock differential in microseconds.
      idx = 28;
      var byteBuffer = ByteBuffer.wrap(fields, idx, 4);
      stateOfHealthSet.add(AcquiredChannelEnvironmentIssueAnalog.from(channelName,
        AcquiredChannelEnvironmentIssueType.CLOCK_DIFFERENTIAL_IN_MICROSECONDS,
        startTime, endTime, byteBuffer.getInt()));
    }

    return stateOfHealthSet;
  }

  private static List<AcquiredChannelEnvironmentIssueBoolean> parseDataStatusByte(byte field,
    String chanName, Instant startTime, Instant endTime) {
    List<AcquiredChannelEnvironmentIssueBoolean> parsedSohSet = new ArrayList<>();
    parsedSohSet.add(AcquiredChannelEnvironmentIssueBoolean.from(chanName,
      AcquiredChannelEnvironmentIssueType.DEAD_SENSOR_CHANNEL,
      startTime, endTime, isSet(field, 0)));

    parsedSohSet.add(AcquiredChannelEnvironmentIssueBoolean.from(chanName,
      AcquiredChannelEnvironmentIssueType.ZEROED_DATA,
      startTime, endTime, isSet(field, 1)));

    parsedSohSet.add(AcquiredChannelEnvironmentIssueBoolean.from(chanName,
      AcquiredChannelEnvironmentIssueType.CLIPPED,
      startTime, endTime, isSet(field, 2)));

    parsedSohSet.add(AcquiredChannelEnvironmentIssueBoolean.from(chanName,
      AcquiredChannelEnvironmentIssueType.CALIBRATION_UNDERWAY,
      startTime, endTime, isSet(field, 3)));

    return parsedSohSet;
  }

  private static List<AcquiredChannelEnvironmentIssueBoolean> parseChannelSecurityByte(byte field,
    String chanName, Instant startTime, Instant endTime) {
    List<AcquiredChannelEnvironmentIssueBoolean> parsedSohSet = new ArrayList<>();
    parsedSohSet.add(AcquiredChannelEnvironmentIssueBoolean.from(chanName,
      AcquiredChannelEnvironmentIssueType.EQUIPMENT_HOUSING_OPEN,
      startTime, endTime, isSet(field, 0)));

    parsedSohSet.add(AcquiredChannelEnvironmentIssueBoolean.from(chanName,
      AcquiredChannelEnvironmentIssueType.DIGITIZING_EQUIPMENT_OPEN,
      startTime, endTime, isSet(field, 1)));

    parsedSohSet.add(AcquiredChannelEnvironmentIssueBoolean.from(chanName,
      AcquiredChannelEnvironmentIssueType.VAULT_DOOR_OPENED,
      startTime, endTime, isSet(field, 2)));

    parsedSohSet.add(AcquiredChannelEnvironmentIssueBoolean.from(chanName,
      AcquiredChannelEnvironmentIssueType.AUTHENTICATION_SEAL_BROKEN,
      startTime, endTime, isSet(field, 3)));

    parsedSohSet.add(AcquiredChannelEnvironmentIssueBoolean.from(chanName,
      AcquiredChannelEnvironmentIssueType.EQUIPMENT_MOVED,
      startTime, endTime, isSet(field, 4)));

    return parsedSohSet;
  }

  private static List<AcquiredChannelEnvironmentIssueBoolean> parseMiscStatusByte(byte field,
    String chanName, Instant startTime,
    Instant endTime) {
    List<AcquiredChannelEnvironmentIssueBoolean> parsedSohSet = new ArrayList<>();
    parsedSohSet.add(AcquiredChannelEnvironmentIssueBoolean.from(chanName,
      AcquiredChannelEnvironmentIssueType.CLOCK_DIFFERENTIAL_TOO_LARGE,
      startTime, endTime, isSet(field, 0)));

    parsedSohSet.add(AcquiredChannelEnvironmentIssueBoolean.from(chanName,
      AcquiredChannelEnvironmentIssueType.GPS_RECEIVER_OFF,
      startTime, endTime, isSet(field, 1)));

    parsedSohSet.add(AcquiredChannelEnvironmentIssueBoolean.from(chanName,
      AcquiredChannelEnvironmentIssueType.GPS_RECEIVER_UNLOCKED,
      startTime, endTime, isSet(field, 2)));

    parsedSohSet.add(AcquiredChannelEnvironmentIssueBoolean.from(chanName,
      AcquiredChannelEnvironmentIssueType.DIGITIZER_ANALOG_INPUT_SHORTED,
      startTime, endTime, isSet(field, 3)));

    parsedSohSet.add(AcquiredChannelEnvironmentIssueBoolean.from(chanName,
      AcquiredChannelEnvironmentIssueType.DIGITIZER_CALIBRATION_LOOP_BACK,
      startTime, endTime, isSet(field, 4)));

    return parsedSohSet;
  }

  private static List<AcquiredChannelEnvironmentIssueBoolean> parseVoltageIndicatorByte(byte field,
    String chanName, Instant startTime,
    Instant endTime) {
    List<AcquiredChannelEnvironmentIssueBoolean> parsedSohSet = new ArrayList<>();
    parsedSohSet.add(AcquiredChannelEnvironmentIssueBoolean.from(chanName,
      AcquiredChannelEnvironmentIssueType.MAIN_POWER_FAILURE,
      startTime, endTime, isSet(field, 0)));

    parsedSohSet.add(AcquiredChannelEnvironmentIssueBoolean.from(chanName,
      AcquiredChannelEnvironmentIssueType.BACKUP_POWER_UNSTABLE,
      startTime, endTime, isSet(field, 1)));

    return parsedSohSet;
  }

  /**
   * Check whether a bit is set in the given byte.  The index starts at zero for the first bit.
   *
   * @return boolean
   */
  private static boolean isSet(byte field, int idx) {
    if (idx > 7) {
      return false;
    }
    return ((field >>> idx) & 0x01) == 0x01;
  }

}
