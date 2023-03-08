package gms.dataacquisition.stationreceiver.cd11.common.frames;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;

import java.nio.ByteBuffer;
import java.time.Instant;

import static com.google.common.base.Preconditions.checkState;

@AutoValue
public abstract class Cd11CommandResponse implements Cd11Payload {

  public abstract String getResponderStation();

  public abstract String getSite();

  public abstract String getChannel();

  public abstract String getLocName();

  public abstract Instant getTimestamp();

  public abstract String getCommandRequestMessage();

  public abstract String getResponseMessage();

  /**
   * Returns this connection request frame as bytes.
   *
   * @return byte[], representing the frame in wire format
   */
  @Override
  @Memoized
  public byte[] toBytes() {
    var frameSizeInt = 8 + 5 + 3 + 2 + 2 + 20 +
      Integer.BYTES + getCommandRequestMessage().length() +
      Integer.BYTES + getResponseMessage().length();

    var outputByteBuffer = ByteBuffer.allocate(frameSizeInt);
    outputByteBuffer.put(FrameUtilities.padToLength(getResponderStation(), 8).getBytes());
    outputByteBuffer.put(FrameUtilities.padToLength(getSite(), 5).getBytes());
    outputByteBuffer.put(FrameUtilities.padToLength(getChannel(), 3).getBytes());
    outputByteBuffer.put(FrameUtilities.padToLength(getLocName(), 2).getBytes());
    outputByteBuffer.put((byte) 0); // Null byte.
    outputByteBuffer.put((byte) 0); // Null byte.
    outputByteBuffer.put(FrameUtilities.instantToJd(getTimestamp()).getBytes());
    outputByteBuffer.putInt(getCommandRequestMessage().length());
    outputByteBuffer.put(getCommandRequestMessage().getBytes());
    outputByteBuffer.putInt(getResponseMessage().length());
    outputByteBuffer.put(getResponseMessage().getBytes());

    return outputByteBuffer.array();
  }

  public static Builder builder() {
    return new AutoValue_Cd11CommandResponse.Builder();
  }

  @AutoValue.Builder
  public interface Builder {
    Builder setResponderStation(String responderStation);

    Builder setSite(String site);

    Builder setChannel(String channel);

    Builder setLocName(String locName);

    Builder setTimestamp(Instant timestamp);

    Builder setCommandRequestMessage(String commandRequestMessage);

    Builder setResponseMessage(String responseMessage);

    Cd11CommandResponse autoBuild();

    default Cd11CommandResponse build() {
      Cd11CommandResponse commandResponseFrame = autoBuild();
      validate(commandResponseFrame);
      return commandResponseFrame;
    }

    private static void validate(Cd11CommandResponse commandResponseFrame) {
      checkState(commandResponseFrame.getResponderStation().length() <= 8);
      checkState(commandResponseFrame.getSite().length() <= 5);
      checkState(commandResponseFrame.getChannel().length() <= 3);
      checkState(commandResponseFrame.getLocName().length() <= 2);
      checkState(!commandResponseFrame.getCommandRequestMessage().isBlank());
      checkState(!commandResponseFrame.getResponseMessage().isBlank());
    }

  }

}
