package gms.dataacquisition.css.stationrefconverter;

import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.channel.ChannelBandType;
import gms.shared.frameworks.osd.coi.channel.ChannelDataType;
import gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType;
import gms.shared.frameworks.osd.coi.channel.ChannelOrientationType;

@AutoValue
public abstract class ChannelTypes {

  public abstract ChannelBandType getBandType();

  public abstract ChannelInstrumentType getInstrumentType();

  public abstract ChannelOrientationType getOrientationType();

  public abstract char getOrientationCode();

  public abstract ChannelDataType getDataType();

  public abstract Builder toBuilder();

  static Builder builder() {
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
