package gms.dataacquisition.stationreceiver.cd11.common.frames;

import com.google.auto.value.AutoValue;


/**
 * This custom frame signals to the Data Consumer that a "reset" has occurred, and that the Data
 * Consumer needs to clear its gap list, shutdown, and listen for a new Data Provider connection.
 * <p>
 * NOTE: This frame is **NOT** described in the CD 1.1 protocol.
 */
@AutoValue
public abstract class CustomReset implements Cd11Payload {

  public abstract byte[] getBytes();

  @Override
  public byte[] toBytes() {
    return getBytes();
  }

  public static CustomReset create(byte[] bytes) {
    return new AutoValue_CustomReset(bytes);
  }

}
