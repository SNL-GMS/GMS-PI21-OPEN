package gms.dataacquisition.stationreceiver.cd11.common.enums;

import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Acknack;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Alert;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11CommandRequest;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11CommandResponse;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ConnectionExchange;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Data;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11OptionExchange;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Payload;
import gms.dataacquisition.stationreceiver.cd11.common.frames.CustomReset;

/**
 * Enumeration for each CD 1.1 frame type.
 */
public enum FrameType {

  CONNECTION_REQUEST(1, Cd11ConnectionExchange.class),
  CONNECTION_RESPONSE(2, Cd11ConnectionExchange.class),
  OPTION_REQUEST(3, Cd11OptionExchange.class),
  OPTION_RESPONSE(4, Cd11OptionExchange.class),
  DATA(5, Cd11Data.class),
  ACKNACK(6, Cd11Acknack.class),
  ALERT(7, Cd11Alert.class),
  COMMAND_REQUEST(8, Cd11CommandRequest.class),
  COMMAND_RESPONSE(9, Cd11CommandResponse.class),
  CD_ONE_ENCAPSULATION(13, Cd11Data.class),
  CUSTOM_RESET_FRAME(26, CustomReset.class);

  private final int value;
  private final Class<? extends Cd11Payload> className;

  FrameType(final int newValue, final Class<? extends Cd11Payload> newClassName) {
    value = newValue;
    className = newClassName;
  }

  public int getValue() {
    return value;
  }

  public Class<? extends Cd11Payload> getClassName() {
    return className;
  }

  public static FrameType fromInt(int value) {
    for (FrameType ft : FrameType.values()) {
      if (ft.value == value) {
        return ft;
      }
    }
    throw new IllegalArgumentException(String.format(
      "Integer value %1$d does not map to a Cd11FrameType enumeration.", value));

  }

  public static FrameType fromClass(Class<? extends Cd11Frame> clazz) {
    for (FrameType ft : FrameType.values()) {
      if (ft.className.isInstance(clazz)) {
        return ft;
      }
    }
    return null;
  }

  /**
   * Returns a string containing a comma separated list of valid enumeration values.
   *
   * @return Comma separated list of valid values.
   */
  public static String validValues() {
    var validValues = new StringBuilder();
    for (FrameType fti : FrameType.values()) {
      if (validValues.length() > 0) {
        validValues.append(", ");
      }
      validValues.append(fti.toString());
    }
    return validValues.toString();
  }

  /**
   * Returns a string containing a comma separated list of valid integers.
   *
   * @return Comma separated list of valid integers.
   */
  public static String validIntValues() {
    var validValues = new StringBuilder();
    for (FrameType fti : FrameType.values()) {
      if (validValues.length() > 0) {
        validValues.append(", ");
      }
      validValues.append(fti.getValue());
    }
    return validValues.toString();
  }


}
