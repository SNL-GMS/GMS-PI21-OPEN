package gms.shared.frameworks.osd.coi.channel;

import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.coi.provenance.InformationSource;
import gms.shared.frameworks.osd.coi.stationreference.RelativePosition;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static gms.shared.frameworks.osd.coi.util.RandomUtility.randomDouble;
import static gms.shared.frameworks.osd.coi.util.RandomUtility.randomEnum;
import static gms.shared.frameworks.osd.coi.util.RandomUtility.randomInt;
import static gms.shared.frameworks.osd.coi.util.RandomUtility.randomUpperCase;

public class ReferenceChannelTestFixtures {

  private static final String LOCATION_CODE = "locationCode";
  private static final String REFERENCE_CHANNEL_COMMENT = "reference channel comment";

  private ReferenceChannelTestFixtures() {
  }

  private static final ReferenceChannel asarAs01Bhz = ReferenceChannel.builder()
    .setName("AS01/BHZ")
    .setDataType(ChannelDataType.SEISMIC)
    .setBandType(ChannelBandType.BROADBAND)
    .setInstrumentType(ChannelInstrumentType.HIGH_GAIN_SEISMOMETER)
    .setOrientationType(ChannelOrientationType.VERTICAL)
    .setOrientationCode(ChannelOrientationType.VERTICAL.getCode())
    .setLocationCode(LOCATION_CODE)
    .setLatitude(7.7)
    .setLongitude(11.11)
    .setElevation(3.3)
    .setDepth(0.04)
    .setHorizontalAngle(0.0)
    .setVerticalAngle(0.0)
    .setUnits(Units.COUNTS_PER_NANOMETER)
    .setNominalSampleRate(40.0)
    .setActualTime(Instant.EPOCH)
    .setSystemTime(Instant.EPOCH)
    .setActive(true)
    .setInformationSource(InformationSource.from("IDC",
      Instant.now(), "IDC"))
    .setComment(REFERENCE_CHANNEL_COMMENT)
    .setPosition(RelativePosition.from(0.0, 0.0, 0.0))
    .setAliases(Collections.emptyList())
    .build();

  private static final ReferenceChannel pdarPd01Shz = ReferenceChannel.builder()
    .setName("PD01/SHZ")
    .setDataType(ChannelDataType.SEISMIC)
    .setBandType(ChannelBandType.SHORT_PERIOD)
    .setInstrumentType(ChannelInstrumentType.HIGH_GAIN_SEISMOMETER)
    .setOrientationType(ChannelOrientationType.VERTICAL)
    .setOrientationCode(ChannelOrientationType.VERTICAL.getCode())
    .setLocationCode(LOCATION_CODE)
    .setLatitude(42.7765)
    .setLongitude(-109.58314)
    .setElevation(2.192)
    .setDepth(0.0381)
    .setHorizontalAngle(-1)
    .setVerticalAngle(0.0)
    .setUnits(Units.COUNTS_PER_NANOMETER)
    .setNominalSampleRate(20.0)
    .setActualTime(Instant.EPOCH)
    .setSystemTime(Instant.EPOCH)
    .setActive(true)
    .setInformationSource(InformationSource.from("IDC",
      Instant.now(), "IDC"))
    .setComment(REFERENCE_CHANNEL_COMMENT)
    .setPosition(RelativePosition.from(0.0, 0.0, 0.0))
    .setAliases(Collections.emptyList())
    .build();

  private static final ReferenceChannel txarTx01Shz = ReferenceChannel.builder()
    .setName("TX01/SHZ")
    .setDataType(ChannelDataType.SEISMIC)
    .setBandType(ChannelBandType.SHORT_PERIOD)
    .setInstrumentType(ChannelInstrumentType.HIGH_GAIN_SEISMOMETER)
    .setOrientationType(ChannelOrientationType.VERTICAL)
    .setOrientationCode(ChannelOrientationType.VERTICAL.getCode())
    .setLocationCode(LOCATION_CODE)
    .setLatitude(29.33397)
    .setLongitude(-103.66769)
    .setElevation(0.997)
    .setDepth(0.0061)
    .setHorizontalAngle(-1)
    .setVerticalAngle(0.0)
    .setUnits(Units.COUNTS_PER_NANOMETER)
    .setNominalSampleRate(40.0)
    .setActualTime(Instant.EPOCH)
    .setSystemTime(Instant.EPOCH)
    .setActive(true)
    .setInformationSource(InformationSource.from("IDC",
      Instant.now(), "IDC"))
    .setComment(REFERENCE_CHANNEL_COMMENT)
    .setPosition(RelativePosition.from(0.0, 0.0, 0.0))
    .setAliases(Collections.emptyList())
    .build();

  public static ReferenceChannel randomChannel() {
    return randomChannelBuilder().build();
  }

  public static ReferenceChannel.Builder randomChannelBuilder() {
    ChannelOrientationType orientationType = randomEnum(ChannelOrientationType.class);
    Instant activeTime = Instant.EPOCH;

    return ReferenceChannel.builder()
      .setName(randomUpperCase(3))
      .setDataType(randomEnum(ChannelDataType.class))
      .setBandType(randomEnum(ChannelBandType.class))
      .setInstrumentType(randomEnum(ChannelInstrumentType.class))
      .setOrientationType(orientationType)
      .setOrientationCode(orientationType.getCode())
      .setLocationCode("")
      .setLatitude(randomDouble(180.0) - 90.0)
      .setLongitude(randomDouble(360.0) - 180.0)
      .setElevation(randomDouble(8.850))
      .setDepth(randomDouble(0.1))
      .setVerticalAngle(randomDouble(90.0))
      .setHorizontalAngle(randomDouble(360.0))
      .setUnits(Units.COUNTS_PER_NANOMETER)
      .setNominalSampleRate((double) (randomInt(800) / (double) 10))
      .setActualTime(activeTime)
      .setSystemTime(activeTime)
      .setActive(true)
      .setInformationSource(randomInformationSource())
      .setComment(randomUpperCase(10))
      .setPosition(RelativePosition.from(
        randomDouble(0.5),
        randomDouble(0.5),
        randomDouble(0.1)
      ))
      .setAliases(List.of());
  }

  private static InformationSource randomInformationSource() {
    return InformationSource.from(
      randomUpperCase(3),
      Instant.EPOCH,
      randomUpperCase(10)
    );
  }

  public static ReferenceChannel asarAs01Bhz() {
    return asarAs01Bhz;
  }

  public static ReferenceChannel pdarPd01Shz() {
    return pdarPd01Shz;
  }

  public static ReferenceChannel txarTx01Shz() {
    return txarTx01Shz;
  }

}
