package gms.dataacquisition.stationreceiver.cd11.common.frames;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;

import java.nio.ByteBuffer;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Alert Frames are sent by either the data provider or consumer to notify the other party that the
 * connection is going to be terminated. Because of their very nature an Alert Frame will not be
 * addressed by an Acknack Frame. For example, if an Alert Frame were to be re-sent, the receiving
 * protocol peer would terminate the connection.
 */
@AutoValue
public abstract class Cd11Alert implements Cd11Payload {

  /**
   * The minimum byte array length of an alert frame. This value does not include the size the
   * message which is dynamic.
   */
  public static final int MINIMUM_FRAME_LENGTH = Integer.BYTES;

  /**
   * Get the size of the alert message
   *
   * @return Size of the alert message
   */
  public abstract int getSize();

  /**
   * Get the alert message
   *
   * @return Alert message
   */
  public abstract String getMessage();

  /**
   * Returns this alert frame as bytes.
   *
   * @return byte[], representing the frame in wire format
   */
  @Override
  @Memoized
  public byte[] toBytes() {
    var paddingSizeInt = FrameUtilities.calculatePaddedLength(getSize(), Integer.BYTES);

    var outputByteBuffer = ByteBuffer.allocate(Cd11Alert.MINIMUM_FRAME_LENGTH + paddingSizeInt);
    outputByteBuffer.putInt(getSize());
    outputByteBuffer.put(FrameUtilities.padToLength(getMessage(), paddingSizeInt).getBytes());

    return outputByteBuffer.array();
  }

  public static Cd11Alert create(String message) {
    return create(message.length(), message);
  }

  public static Cd11Alert create(int size, String message) {
    checkArgument(size == message.length(), "Size should equal length of message");

    return new AutoValue_Cd11Alert(size, message);
  }

}
