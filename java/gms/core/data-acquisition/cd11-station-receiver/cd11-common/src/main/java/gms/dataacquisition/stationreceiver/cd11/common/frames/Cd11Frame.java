package gms.dataacquisition.stationreceiver.cd11.common.frames;

import com.google.auto.value.AutoValue;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;

import java.nio.ByteBuffer;

/**
 * Base class for all CD 1.1 Frame classes.
 */
@AutoValue
public abstract class Cd11Frame {

  public abstract Cd11Header getHeader();

  public abstract Cd11Payload getPayload();

  public abstract Cd11Trailer getTrailer();

  public FrameType getType() {
    return getHeader().getFrameType();
  }

  /**
   * Returns a byte array representing the entire CD 1.1 frame (header, body, and trailer). NOTE:
   * This method can only be called when a fully constructed frame trailer has been set.
   *
   * @return Byte array representing the full CD 1.1 frame.
   * @throws IllegalStateException Thrown when the frame header or trailer have not been set.
   */
  public byte[] toBytes() {
    var headerBytesArray = getHeader().toBytes();
    var frameBodyBytesArray = getPayload().toBytes();
    var trailerBytesArray = getTrailer().toBytes();

    var frameByteBuffer = ByteBuffer.allocate(
      headerBytesArray.length + frameBodyBytesArray.length + trailerBytesArray.length);
    frameByteBuffer.put(headerBytesArray);
    frameByteBuffer.put(frameBodyBytesArray);
    frameByteBuffer.put(trailerBytesArray);

    return frameByteBuffer.array();
  }

  public static Builder builder() {
    return new AutoValue_Cd11Frame.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  public interface Builder {

    Builder setHeader(Cd11Header header);

    Builder setPayload(Cd11Payload payload);

    Builder setTrailer(Cd11Trailer trailer);

    Cd11Frame build();
  }

}
