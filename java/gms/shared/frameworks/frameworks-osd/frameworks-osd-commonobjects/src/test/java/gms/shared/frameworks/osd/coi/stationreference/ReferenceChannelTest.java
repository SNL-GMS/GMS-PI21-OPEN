package gms.shared.frameworks.osd.coi.stationreference;

import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.coi.channel.ChannelBandType;
import gms.shared.frameworks.osd.coi.channel.ChannelDataType;
import gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType;
import gms.shared.frameworks.osd.coi.channel.ChannelOrientationType;
import gms.shared.frameworks.osd.coi.channel.ReferenceChannel;
import gms.shared.frameworks.osd.coi.provenance.InformationSource;
import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReferenceChannelTest {

  private static final String name = "BHZ";
  private static final UUID id = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_16LE));
  private static final ChannelDataType dataType = ChannelDataType.SEISMIC;
  private static final ChannelBandType bandType = ChannelBandType.HIGH_BROADBAND;
  private static final ChannelInstrumentType instrumentType = ChannelInstrumentType.HIGH_GAIN_SEISMOMETER;
  private static final ChannelOrientationType orientationType = ChannelOrientationType.EAST_WEST;
  private static final char orientationCode = orientationType.getCode();
  private static final Units units = Units.HERTZ;
  private static final String locationCode = "1";
  private static final double latitude = -13.56789;
  private static final double longitude = 89.04123;
  private static final double elevation = 376.43;
  private static final double depth = 123.456;
  private static final double verticalAngle = 12.34;
  private static final double horizontalAngle = 43.21;
  private static final double nominalSampleRate = 40;
  private static final Instant actualTime = Instant.now().minusSeconds(50);
  private static final Instant systemTime = Instant.now();
  private static final String comment = "It must be true.";
  private static final UUID versionId = UUID.nameUUIDFromBytes(
    (name + dataType + bandType + instrumentType + orientationType
      + orientationCode + locationCode
      + latitude + longitude + elevation
      + depth + verticalAngle + horizontalAngle + units
      + nominalSampleRate + actualTime)
      .getBytes(StandardCharsets.UTF_16LE));
  private static final RelativePosition position = RelativePosition.from(1.1,
    2.2, 3.3);

  private static final InformationSource informationSource = InformationSource.from("IDC",
    Instant.now(), "IDC");

  private static List<ReferenceAlias> aliases = new ArrayList<>();

  private static final double precision = 0.00001;

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(StationReferenceTestFixtures.REFERENCE_CHANNEL, ReferenceChannel.class);
  }

  @Test
  void testReferenceChannelBuilder() {
    ReferenceChannel channel = ReferenceChannel.builder()
      .setName(name)
      .setDataType(dataType)
      .setBandType(bandType)
      .setInstrumentType(instrumentType)
      .setOrientationType(orientationType)
      .setOrientationCode(orientationCode)
      .setUnits(units)
      .setLocationCode(locationCode)
      .setLatitude(latitude)
      .setLongitude(longitude)
      .setElevation(elevation)
      .setDepth(depth)
      .setVerticalAngle(verticalAngle)
      .setHorizontalAngle(horizontalAngle)
      .setNominalSampleRate(nominalSampleRate)
      .setActualTime(actualTime)
      .setSystemTime(systemTime)
      .setActive(true)
      .setInformationSource(informationSource)
      .setComment(comment)
      .setPosition(position)
      .setAliases(aliases)
      .build();
    assertEquals(id, channel.getEntityId());
    assertEquals(versionId, channel.getVersionId());
    assertEquals(name, channel.getName());
    assertEquals(dataType, channel.getDataType());
    assertEquals(locationCode, channel.getLocationCode());
    assertEquals(latitude, channel.getLatitude(), precision);
    assertEquals(longitude, channel.getLongitude(), precision);
    assertEquals(elevation, channel.getElevation(), precision);
    assertEquals(depth, channel.getDepth(), precision);
    assertEquals(verticalAngle, channel.getVerticalAngle(), precision);
    assertEquals(horizontalAngle, channel.getHorizontalAngle(), precision);
    assertEquals(nominalSampleRate, channel.getNominalSampleRate(), precision);
    assertEquals(actualTime, channel.getActualTime());
    assertEquals(systemTime, channel.getSystemTime());
    assertEquals(informationSource, channel.getInformationSource());
    assertEquals(comment, channel.getComment());
    assertEquals(position, channel.getPosition());
    assertEquals(aliases, channel.getAliases());
  }
}
