package gms.shared.stationdefinition.coi.channel;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.auto.value.AutoValue;

@AutoValue
@JsonPropertyOrder(alphabetic = true)
public abstract class ChannelTypes {

  public abstract ChannelBandType getBandType();

  public abstract ChannelInstrumentType getInstrumentType();

  public abstract ChannelOrientationType getOrientationType();

  public abstract char getOrientationCode();

  public abstract ChannelDataType getDataType();

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_ChannelTypes.Builder()
      .setBandType(ChannelBandType.UNKNOWN)
      .setInstrumentType(ChannelInstrumentType.UNKNOWN)
      .setOrientationType(ChannelOrientationType.UNKNOWN);
  }

  @AutoValue.Builder
  public interface Builder {

    Builder setBandType(ChannelBandType bandType);

    Builder setInstrumentType(ChannelInstrumentType instrumentType);

    Builder setOrientationType(ChannelOrientationType orientationType);

    Builder setOrientationCode(char orientationCode);

    Builder setDataType(ChannelDataType dataType);

    ChannelTypes build();
  }
}
