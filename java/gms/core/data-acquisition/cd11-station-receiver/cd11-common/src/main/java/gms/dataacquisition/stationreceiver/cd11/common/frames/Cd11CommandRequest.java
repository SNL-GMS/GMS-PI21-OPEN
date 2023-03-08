package gms.dataacquisition.stationreceiver.cd11.common.frames;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;

import java.nio.ByteBuffer;
import java.time.Instant;

import static com.google.common.base.Preconditions.checkState;
import static gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities.instantToJd;

@AutoValue
public abstract class Cd11CommandRequest implements Cd11Payload {

  public abstract String getStationName();

  public abstract String getSite();

  public abstract String getChannel();

  public abstract String getLocName();

  public abstract Instant getTimestamp();

  public abstract String getCommandMessage();


  /**
   * Returns this command request frame as bytes.
   *
   * @return byte[], representing the frame in wire format
   */
  @Override
  @Memoized
  public byte[] toBytes() {
    var frameSizeInt = 8 + 5 + 3 + 2 + 2 + 20 + Integer.BYTES + getCommandMessage().length();

    var outputByteBuffer = ByteBuffer.allocate(frameSizeInt);
    outputByteBuffer.put(FrameUtilities.padToLength(getStationName(), 8).getBytes());
    outputByteBuffer.put(FrameUtilities.padToLength(getSite(), 5).getBytes());
    outputByteBuffer.put(FrameUtilities.padToLength(getChannel(), 3).getBytes());
    outputByteBuffer.put(FrameUtilities.padToLength(getLocName(), 2).getBytes());
    outputByteBuffer.put((byte) 0); // Null byte.
    outputByteBuffer.put((byte) 0); // Null byte.
    outputByteBuffer.put(instantToJd(getTimestamp()).getBytes());
    outputByteBuffer.putInt(getCommandMessage().length());
    outputByteBuffer.put(getCommandMessage().getBytes());

    return outputByteBuffer.array();
  }

  public static Builder builder() {
    return new AutoValue_Cd11CommandRequest.Builder();
  }

  @AutoValue.Builder
  public interface Builder {
    Builder setStationName(String stationName);

    Builder setSite(String site);

    Builder setChannel(String channel);

    Builder setLocName(String locName);

    Builder setTimestamp(Instant timestamp);

    Builder setCommandMessage(String commandMessage);

    Cd11CommandRequest autoBuild();

    default Cd11CommandRequest build() {
      Cd11CommandRequest commandRequestFrame = autoBuild();
      validate(commandRequestFrame);
      return commandRequestFrame;
    }

    private static void validate(Cd11CommandRequest commandRequestFrame) {
      checkState(commandRequestFrame.getStationName().length() <= 8);
      checkState(commandRequestFrame.getSite().length() <= 5);
      checkState(commandRequestFrame.getChannel().length() <= 3);
      checkState(commandRequestFrame.getLocName().length() <= 2);
      checkState(!commandRequestFrame.getCommandMessage().isBlank());
    }
  }

}
