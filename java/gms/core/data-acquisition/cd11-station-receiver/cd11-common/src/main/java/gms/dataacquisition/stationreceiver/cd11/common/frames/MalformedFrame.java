package gms.dataacquisition.stationreceiver.cd11.common.frames;

import com.google.auto.value.AutoValue;

import java.util.Optional;

@AutoValue
public abstract class MalformedFrame {
  public abstract PartialFrame getPartialFrame();

  public abstract Throwable getCause();

  public abstract byte[] getBytes();

  public abstract int getReadPosition();

  public abstract Optional<String> getStation();

  public static Builder builder() {
    return new AutoValue_MalformedFrame.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  public interface Builder {
    Builder setPartialFrame(PartialFrame partialFrame);

    Builder setCause(Throwable cause);

    Builder setBytes(byte[] bytes);

    Builder setReadPosition(int readPosition);

    Builder setStation(String station);

    MalformedFrame build();
  }
}
