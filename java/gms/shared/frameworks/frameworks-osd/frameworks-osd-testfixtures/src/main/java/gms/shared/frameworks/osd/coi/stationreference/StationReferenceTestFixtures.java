package gms.shared.frameworks.osd.coi.stationreference;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.DoubleValue;
import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.coi.channel.ChannelBandType;
import gms.shared.frameworks.osd.coi.channel.ChannelDataType;
import gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType;
import gms.shared.frameworks.osd.coi.channel.ChannelOrientationType;
import gms.shared.frameworks.osd.coi.channel.Orientation;
import gms.shared.frameworks.osd.coi.channel.ReferenceChannel;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.provenance.InformationSource;
import gms.shared.frameworks.osd.coi.signaldetection.Calibration;
import gms.shared.frameworks.osd.coi.signaldetection.FrequencyAmplitudePhase;
import gms.shared.frameworks.osd.coi.signaldetection.Location;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class StationReferenceTestFixtures {

  private StationReferenceTestFixtures() {

  }

  public static final String UNKNOWN_NAME = "someFakeName";
  public static final UUID UNKNOWN_ID = UUID.fromString("e2a78dbc-97d6-466b-9dd4-4e3fdf6dd95b");

  public static final String COMMENT = "This is a comment.";
  public static final String DESCRIPTION = "This is a description.";
  public static final Instant ACTUAL_TIME = Instant.parse("1980-01-02T03:04:05.123Z");
  public static final Instant SYSTEM_TIME = Instant.parse("2010-11-07T06:05:04.321Z");
  public static final StatusType STATUS = StatusType.ACTIVE;

  public static final String NETWORK_NAME = "NET01"; // when stored it should be uppercase
  public static final NetworkOrganization NETWORK_ORGANIZATION = NetworkOrganization.CTBTO;
  public static final NetworkRegion NETWORK_REGION = NetworkRegion.REGIONAL;
  public static final UUID NETWORK_ID = UUID
    .nameUUIDFromBytes(NETWORK_NAME.getBytes(StandardCharsets.UTF_16LE));
  public static final UUID NETWORK_VERSION_ID = UUID.nameUUIDFromBytes((
    NETWORK_NAME + NETWORK_ORGANIZATION + NETWORK_REGION + ACTUAL_TIME)
    .getBytes(StandardCharsets.UTF_16LE));
  public static final UUID DIGITIZER_ID = UUID.fromString("3712f9de-ff83-4f3d-a832-a82a04067001");
  public static final UUID CALIBRATION_ID = UUID.fromString("aaa0198e-ff83-4f3d-a832-a82a04022000");
  public static final String CHANNEL_NAME = "channel.name";
  public static final UUID SENSOR_ID = UUID.fromString("1230198e-ff83-4f3d-a832-a82a04022321");

  public static final ReferenceAlias STATION_ALIAS = ReferenceAlias.create(
    "StationAlias", StatusType.ACTIVE, COMMENT, ACTUAL_TIME, SYSTEM_TIME);
  public static final ReferenceAlias SITE_ALIAS = ReferenceAlias.create(
    "SiteAlias", StatusType.ACTIVE, COMMENT, ACTUAL_TIME, SYSTEM_TIME);
  public static final ReferenceAlias CHANNEL_ALIAS = ReferenceAlias.create(
    "ChannelAlias", StatusType.ACTIVE, COMMENT, ACTUAL_TIME, SYSTEM_TIME);
  public static final List<ReferenceAlias> STATION_ALIASES = List.of(STATION_ALIAS);
  public static final List<ReferenceAlias> SITE_ALIASES = List.of(SITE_ALIAS);

  public static final InformationSource INFORMATION_SOURCE = InformationSource.from("Internet",
    ACTUAL_TIME, COMMENT);

  public static final double LATITUDE = -13.56789;
  public static final double LONGITUDE = 89.04123;
  public static final double ELEVATION = 376.43;

  public static final double PRECISION = 0.00001;

  //////////////////////////////////////////////////////////

  // Create a ReferenceNetwork
  public static final ReferenceNetwork REFERENCE_NETWORK = ReferenceNetwork.builder()
    .setName(NETWORK_NAME)
    .setDescription(DESCRIPTION)
    .setOrganization(NETWORK_ORGANIZATION)
    .setRegion(NETWORK_REGION)
    .setSource(INFORMATION_SOURCE)
    .setComment(COMMENT)
    .setActualChangeTime(ACTUAL_TIME)
    .setSystemChangeTime(SYSTEM_TIME)
    .setActive(true)
    .build();

  // Create a ReferenceStation
  public static final String STATION_NAME = "STATION01"; // when stored it should be uppercase
  public static final StationType STATION_TYPE = StationType.HYDROACOUSTIC;
  public static final ReferenceStation REFERENCE_STATION = ReferenceStation.builder()
    .setName(STATION_NAME)
    .setDescription(DESCRIPTION)
    .setStationType(STATION_TYPE)
    .setSource(INFORMATION_SOURCE)
    .setComment(COMMENT)
    .setLatitude(LATITUDE)
    .setLongitude(LONGITUDE)
    .setElevation(ELEVATION)
    .setActualChangeTime(ACTUAL_TIME)
    .setSystemChangeTime(SYSTEM_TIME)
    .setActive(true)
    .setAliases(STATION_ALIASES)
    .build();

  // Create a RelativePosition
  public static final double DISPLACEMENT_NORTH = 2.01;
  public static final double DISPLACEMENT_EAST = 2.95;
  public static final double DISPLACEMENT_VERTICAL = 0.56;
  public static final RelativePosition POSITION = RelativePosition.from(DISPLACEMENT_NORTH,
    DISPLACEMENT_EAST, DISPLACEMENT_VERTICAL);

  // Create a ReferenceSite
  public static final String SITE_NAME = "SITE01"; // when stored it should be uppercase
  public static final UUID siteId = UUID
    .nameUUIDFromBytes(SITE_NAME.getBytes(StandardCharsets.UTF_16LE));
  public static final ReferenceSite site = ReferenceSite.builder()
    .setName(SITE_NAME)
    .setDescription(DESCRIPTION)
    .setSource(INFORMATION_SOURCE)
    .setComment(COMMENT)
    .setLatitude(LATITUDE)
    .setLongitude(LONGITUDE)
    .setElevation(ELEVATION)
    .setActive(true)
    .setActualChangeTime(ACTUAL_TIME)
    .setSystemChangeTime(SYSTEM_TIME)
    .setPosition(POSITION)
    .setAliases(SITE_ALIASES)
    .build();

  // Create a ReferenceDigitizer
  public static final String DIGITIZER_NAME = "digitizer name";
  public static final String DIGIT_MANUFACTURER = DigitizerManufacturers.TRIMBLE;
  public static final String DIGIT_MODEL = DigitizerModels.REFTEK;
  public static final String DIGIT_SERIAL = "124590B";
  public static final String DIGITIZER_COMMENT = "Digitizer comment";
  public static final ReferenceDigitizer DIGITIZER = ReferenceDigitizer.builder()
    .setEntityId(UUID.randomUUID())
    .setVersionId(UUID.randomUUID())
    .setName(DIGITIZER_NAME)
    .setManufacturer(DIGIT_MANUFACTURER)
    .setModel(DIGIT_MODEL)
    .setSerialNumber(DIGIT_SERIAL)
    .setActualChangeTime(ACTUAL_TIME)
    .setSystemChangeTime(SYSTEM_TIME)
    .setInformationSource(INFORMATION_SOURCE)
    .setComment(DIGITIZER_COMMENT)
    .setDescription(DESCRIPTION)
    .build();

  // Create a ReferenceChannel
  private static final ChannelBandType BROADBAND = ChannelBandType.BROADBAND;
  private static final ChannelDataType SEISMIC = ChannelDataType.SEISMIC;
  private static final ChannelInstrumentType HIGH_GAIN_SEISMOMETER =
    ChannelInstrumentType.HIGH_GAIN_SEISMOMETER;
  private static final ChannelOrientationType EAST_WEST = ChannelOrientationType.EAST_WEST;
  private static final ChannelOrientationType NORTH_SOUTH = ChannelOrientationType.NORTH_SOUTH;
  public static final ChannelDataType CHANNEL_DATA_TYPE = SEISMIC;
  public static final ChannelBandType CHANNEL_BAND_TYPE = BROADBAND;
  public static final ChannelInstrumentType CHANNEL_INSTRUMENT_TYPE = HIGH_GAIN_SEISMOMETER;
  public static final ChannelOrientationType CHANNEL_ORIENTATION_TYPE = NORTH_SOUTH;
  public static final char CHANNEL_ORIENTATION_CODE = CHANNEL_ORIENTATION_TYPE.getCode();
  public static final Units UNITS = Units.HERTZ;
  public static final String LOCATION_CODE = "23";
  public static final double DEPTH = 12.943;
  public static final double VERTICAL_ANGLE = 1.005;
  public static final double HORIZONTAL_ANGLE = 3.66;
  public static final double NOMINAL_SAMPLE_RATE = 40.0;
  protected static final List<ReferenceCalibration> calibrations = new ArrayList<>();
  protected static final List<ReferenceResponse> responses = new ArrayList<>();
  protected static final List<ReferenceAlias> ALIASES = new ArrayList<>();

  public static final ReferenceChannel REFERENCE_CHANNEL = ReferenceChannel.builder()
    .setName(CHANNEL_NAME)
    .setDataType(CHANNEL_DATA_TYPE)
    .setBandType(CHANNEL_BAND_TYPE)
    .setInstrumentType(CHANNEL_INSTRUMENT_TYPE)
    .setOrientationType(CHANNEL_ORIENTATION_TYPE)
    .setOrientationCode(CHANNEL_ORIENTATION_CODE)
    .setUnits(UNITS)
    .setLocationCode(LOCATION_CODE)
    .setLatitude(LATITUDE)
    .setLongitude(LONGITUDE)
    .setElevation(ELEVATION)
    .setDepth(DEPTH)
    .setVerticalAngle(VERTICAL_ANGLE)
    .setHorizontalAngle(HORIZONTAL_ANGLE)
    .setNominalSampleRate(NOMINAL_SAMPLE_RATE)
    .setActualTime(ACTUAL_TIME)
    .setSystemTime(SYSTEM_TIME)
    .setActive(true)
    .setInformationSource(INFORMATION_SOURCE)
    .setComment(COMMENT)
    .setPosition(POSITION)
    .setAliases(ALIASES)
    .build();

  // Create a Channel ReferenceSensor
  public static final String INSTRUMENT_MANUFACTURER =
    InstrumentManufacturers.GEOTECH_INSTRUMENTS_LLC;
  public static final String INSTRUMENT_MODEL = InstrumentModels.GS_13;
  public static final String SERIAL_NUMBER = "S1234-00";
  public static final int NUMBER_OF_COMPONENTS = 2;
  public static final double CORNER_PERIOD = 3.0;
  public static final double LOW_PASSBAND = 1.0;
  public static final double HIGH_PASSBAND = 5.0;
  public static final ReferenceSensor REFERENCE_SENSOR = ReferenceSensor.builder()
    .setChannelName(REFERENCE_CHANNEL.getName())
    .setInstrumentManufacturer(INSTRUMENT_MANUFACTURER)
    .setInstrumentModel(INSTRUMENT_MODEL)
    .setSerialNumber(SERIAL_NUMBER)
    .setNumberOfComponents(NUMBER_OF_COMPONENTS)
    .setCornerPeriod(CORNER_PERIOD)
    .setLowPassband(LOW_PASSBAND)
    .setHighPassband(HIGH_PASSBAND)
    .setActualTime(ACTUAL_TIME)
    .setSystemTime(SYSTEM_TIME)
    .setInformationSource(INFORMATION_SOURCE)
    .setComment(COMMENT)
    .build();

  // Create a Channel ReferenceResponse
  public static final ResponseTypes RESPONSE_TYPE = ResponseTypes.PAZFIR;
  protected static final byte[] RESPONSE_DATA = "kt0naPqwrtoij2541akAx"
    .getBytes(StandardCharsets.UTF_16LE);
  protected static final String RESPONSE_UNITS = "millimeters";
  // Create a Channel ReferenceCalibration
  public static final double CALIBRATION_INTERVAL = 3.0;
  public static final double CALIBRATION_PERIOD = 1.0;
  public static final double CALIBRATION_TIME_SHIFT = 0.0;
  public static final Duration CALIBRATION_INTERVAL_DURATION = Duration.ofSeconds(
    (long) 0.0);
  public static final DoubleValue CAL_FACTOR = DoubleValue
    .from(2.5, 0.9876, Units.NANOMETERS_PER_COUNT);
  public static final Duration CAL_TIME_SHIFT_DURATION = Duration.ofSeconds(
    (long) CALIBRATION_TIME_SHIFT);

  public static final Calibration CALIBRATION = Calibration.from(
    CALIBRATION_PERIOD, CALIBRATION_INTERVAL_DURATION, CAL_FACTOR);

  public static final ReferenceCalibration REFERENCE_CALIBRATION = ReferenceCalibration.from(
    CALIBRATION_INTERVAL_DURATION, CALIBRATION);

  public static final ReferenceCalibration REF_CALIBRATION_BHE_V_1 = ReferenceCalibration
    .from(CALIBRATION_INTERVAL_DURATION, CALIBRATION);

  public static final InformationSource INFO_SOURCE = InformationSource.from("TEST",
    Instant.EPOCH, "TEST");

  public static final ReferenceSourceResponse REFERENCE_SOURCE_RESPONSE =
    ReferenceSourceResponse.builder()
      .setSourceResponseData("test".getBytes())
      .setSourceResponseUnits(Units.COUNTS_PER_NANOMETER)
      .setSourceResponseTypes(ResponseTypes.FAP)
      .setInformationSources(List.of(INFO_SOURCE))
      .build();

  public static final Optional<FrequencyAmplitudePhase> FAP = Optional.of(
    FrequencyAmplitudePhase.builder()
      .setFrequencies(new double[]{8.9})
      .setAmplitudeResponseUnits(Units.HERTZ)
      .setAmplitudeResponse(new double[]{0.1})
      .setAmplitudeResponseStdDev(new double[]{2.3})
      .setPhaseResponseUnits(Units.HERTZ)
      .setPhaseResponse(new double[]{4.5})
      .setPhaseResponseStdDev(new double[]{6.7})
      .build());

  public static final ReferenceResponse REFERENCE_RESPONSE = ReferenceResponse.builder()
    .setChannelName(CHANNEL_NAME)
    .setActualTime(ACTUAL_TIME)
    .setSystemTime(SYSTEM_TIME)
    .setComment(COMMENT)
    .setSourceResponse(REFERENCE_SOURCE_RESPONSE)
    .setReferenceCalibration(REF_CALIBRATION_BHE_V_1)
    .setFapResponse(FAP)
    .build();

  public static final String TESTING = "Testing";
  public static final ReferenceNetworkMembership REFERENCE_NETWORK_MEMBERSHIP =
    ReferenceNetworkMembership
      .create(TESTING,
        ACTUAL_TIME, SYSTEM_TIME, REFERENCE_NETWORK.getEntityId(),
        REFERENCE_STATION.getEntityId(), STATUS);

  public static final ReferenceStationMembership REFERENCE_STATION_MEMBERSHIP =
    ReferenceStationMembership
      .create(TESTING,
        ACTUAL_TIME, SYSTEM_TIME, REFERENCE_STATION.getEntityId(), site.getEntityId(),
        STATUS);

  public static final ReferenceSiteMembership REFERENCE_SITE_MEMBERSHIP =
    ReferenceSiteMembership.create(
      TESTING, ACTUAL_TIME, SYSTEM_TIME, site.getEntityId(), REFERENCE_CHANNEL.getName(),
      STATUS);

  public static final ReferenceDigitizerMembership REFERENCE_DIGITIZER_MEMBERSHIP =
    ReferenceDigitizerMembership
      .create(TESTING,
        ACTUAL_TIME, SYSTEM_TIME, DIGITIZER_ID, REFERENCE_CHANNEL.getEntityId(), STATUS);

  public static final ObjectMapper JSON_OBJECT_MAPPER =
    CoiObjectMapperFactory.getJsonObjectMapper();

  private static final double NA_VALUE = -999.0;
  private static final Location NA_VALUE_LOCATION = Location.from(
    NA_VALUE, NA_VALUE, NA_VALUE, NA_VALUE);

  private static final Instant CHANGE_TIME_1 = Instant.ofEpochSecond(797731200);
  private static final Instant CHANGE_TIME_2 = Instant.ofEpochSecond(1195430400);
  private static final Instant CHANGE_TIME_3 = Instant.ofEpochSecond(1232496000);

  public static final ReferenceStation JNU_V1 = ReferenceStation.builder()
    .setName("JNU")
    .setDescription("Ohita, Japan")
    .setStationType(StationType.SEISMIC_3_COMPONENT)
    .setSource(INFO_SOURCE)
    .setComment("")
    .setLatitude(33.1217)
    .setLongitude(130.8783)
    .setElevation(0.54)
    .setActualChangeTime(CHANGE_TIME_1)
    .setSystemChangeTime(CHANGE_TIME_1)
    .setActive(true)
    .setAliases(new ArrayList<>())
    .build();

  public static final ReferenceStation JNU_V2 = ReferenceStation.builder()
    .setName("JNU")
    .setDescription("")
    .setStationType(StationType.SEISMIC_3_COMPONENT)
    .setSource(INFO_SOURCE)
    .setComment("upgrade for IMS")
    .setLatitude(NA_VALUE)
    .setLongitude(NA_VALUE)
    .setElevation(NA_VALUE)
    .setActualChangeTime(CHANGE_TIME_2)
    .setSystemChangeTime(CHANGE_TIME_2)
    .setActive(true)
    .setAliases(new ArrayList<>())
    .build();

  public static final ReferenceStation JNU_V3 = ReferenceStation.builder()
    .setName("JNU")
    .setDescription("Oita Nakatsue, Japan Meterological Agency Seismic Network")
    .setStationType(StationType.SEISMIC_3_COMPONENT)
    .setSource(INFO_SOURCE)
    .setComment("")
    .setLatitude(33.121667)
    .setLongitude(130.87833)
    .setElevation(0.573)
    .setActualChangeTime(CHANGE_TIME_3)
    .setSystemChangeTime(CHANGE_TIME_3)
    .setActive(true)
    .setAliases(new ArrayList<>())
    .build();

  public static final List<ReferenceStation> JNU_VERSIONS = List.of(JNU_V1,
    JNU_V2, JNU_V3);
  private static final RelativePosition ZERO_POSITION = RelativePosition.from(0.0, 0.0, 0.0);

  public static final ReferenceNetwork NET_IMS_AUX;
  public static final ReferenceNetwork NET_IDC_DA;
  public static List<ReferenceNetworkMembership> referenceNetworkMemberships;
  public static final ReferenceSite JNU_SITE_V1 = ReferenceSite.builder()
    .setName("JNU")
    .setDescription("Ohita, Japan")
    .setSource(INFO_SOURCE)
    .setComment("")
    .setLatitude(33.1217)
    .setLongitude(130.8783)
    .setElevation(0.54)
    .setActualChangeTime(CHANGE_TIME_1)
    .setSystemChangeTime(CHANGE_TIME_1)
    .setActive(true)
    .setPosition(ZERO_POSITION)
    .setAliases(new ArrayList<>())
    .build();

  public static final ReferenceSite JNU_SITE_V2 = ReferenceSite.builder()
    .setName("JNU")
    .setDescription("")
    .setSource(INFO_SOURCE)
    .setComment("upgrade for IMS")
    .setLatitude(NA_VALUE)
    .setLongitude(NA_VALUE)
    .setElevation(NA_VALUE)
    .setActualChangeTime(CHANGE_TIME_2)
    .setSystemChangeTime(CHANGE_TIME_2)
    .setActive(true)
    .setPosition(ZERO_POSITION)
    .setAliases(new ArrayList<>())
    .build();


  public static final ReferenceSite JNU_SITE_V3 = ReferenceSite.builder()
    .setName("JNU")
    .setDescription("Oita Nakatsue, Japan Meterological Agency Seismic Network")
    .setSource(INFO_SOURCE)
    .setComment("")
    .setLatitude(33.121667)
    .setLongitude(130.87833)
    .setElevation(0.573)
    .setActualChangeTime(CHANGE_TIME_3)
    .setSystemChangeTime(CHANGE_TIME_3)
    .setActive(true)
    .setPosition(ZERO_POSITION)
    .setAliases(new ArrayList<>())
    .build();

  public static final List<ReferenceSite> JNU_SITE_VERSIONS = List.of(JNU_SITE_V1, JNU_SITE_V2,
    JNU_SITE_V3);

  // ReferenceChannels

  private static final Orientation ORIENTATION_90_90 = Orientation.from(90.0, 90.0);
  private static final Orientation ORIENTATION_90_0 = Orientation.from(90.0, 0.0);
  private static final Orientation ORIENTATION_0_NEG1 = Orientation.from(0.0, -1.0);
  private static final Orientation NA_ORIENTATION = Orientation.from(-1, -1);

  // Channel BHE (3 versions)
  private static final Location LOCATION_CHAN_JNU_BHE_V1 = Location.from(33.1217, 130.8783, 1,
    0.54);
  public static final ReferenceChannel CHAN_JNU_BHE_V1 = ReferenceChannel.builder()
    .setName("CHAN_JNU_BHE_V1")
    .setDataType(SEISMIC)
    .setBandType(BROADBAND)
    .setInstrumentType(HIGH_GAIN_SEISMOMETER)
    .setOrientationType(EAST_WEST)
    .setOrientationCode(EAST_WEST.getCode())
    .setLocationCode("0")
    .setLatitude(LOCATION_CHAN_JNU_BHE_V1.getLatitudeDegrees())
    .setLongitude(LOCATION_CHAN_JNU_BHE_V1.getLongitudeDegrees())
    .setElevation(LOCATION_CHAN_JNU_BHE_V1.getElevationKm())
    .setDepth(LOCATION_CHAN_JNU_BHE_V1.getDepthKm())
    .setVerticalAngle(ORIENTATION_90_90.getVerticalAngleDeg())
    .setHorizontalAngle(ORIENTATION_90_90.getHorizontalAngleDeg())
    .setUnits(Units.COUNTS_PER_NANOMETER)
    .setNominalSampleRate(20)
    .setActualTime(CHANGE_TIME_1)
    .setSystemTime(CHANGE_TIME_1)
    .setActive(true)
    .setInformationSource(INFO_SOURCE)
    .setComment("CHAN_JNU_BHE_V1 ReferenceChannel")
    .setPosition(ZERO_POSITION)
    .setAliases(Collections.emptyList())
    .build();

  private static final ReferenceChannel CHAN_JNU_BHE_V2 = ReferenceChannel.builder()
    .setName("CHAN_JNU_BHE_V2")
    .setDataType(SEISMIC)
    .setBandType(BROADBAND)
    .setInstrumentType(HIGH_GAIN_SEISMOMETER)
    .setOrientationType(EAST_WEST)
    .setOrientationCode(EAST_WEST.getCode())
    .setLocationCode("0")
    .setLatitude(NA_VALUE_LOCATION.getLatitudeDegrees())
    .setLongitude(NA_VALUE_LOCATION.getLongitudeDegrees())
    .setElevation(NA_VALUE_LOCATION.getElevationKm())
    .setDepth(NA_VALUE_LOCATION.getDepthKm())
    .setVerticalAngle(NA_ORIENTATION.getVerticalAngleDeg())
    .setHorizontalAngle(NA_ORIENTATION.getHorizontalAngleDeg())
    .setUnits(Units.COUNTS_PER_NANOMETER)
    .setNominalSampleRate(20)
    .setActualTime(CHANGE_TIME_2)
    .setSystemTime(CHANGE_TIME_2)
    .setActive(true)
    .setInformationSource(INFO_SOURCE)
    .setComment("decommissioned CHAN_JNU_BHE_V2 ReferenceChannel")
    .setPosition(ZERO_POSITION)
    .setAliases(Collections.emptyList())
    .build();

  private static final Location LOCATION_CHAN_JNU_BHE_V3 = Location.from(
    33.121667, 130.87833, 0, 0.573);
  private static final ReferenceChannel CHAN_JNU_BHE_V3 = ReferenceChannel.builder()
    .setName("CHAN_JNU_BHE_V3")
    .setDataType(SEISMIC)
    .setBandType(BROADBAND)
    .setInstrumentType(HIGH_GAIN_SEISMOMETER)
    .setOrientationType(EAST_WEST)
    .setOrientationCode(EAST_WEST.getCode())
    .setLocationCode("0")
    .setLatitude(LOCATION_CHAN_JNU_BHE_V3.getLatitudeDegrees())
    .setLongitude(LOCATION_CHAN_JNU_BHE_V3.getLongitudeDegrees())
    .setElevation(LOCATION_CHAN_JNU_BHE_V3.getElevationKm())
    .setDepth(LOCATION_CHAN_JNU_BHE_V3.getDepthKm())
    .setVerticalAngle(ORIENTATION_90_90.getVerticalAngleDeg())
    .setHorizontalAngle(ORIENTATION_90_90.getHorizontalAngleDeg())
    .setUnits(Units.COUNTS_PER_NANOMETER)
    .setNominalSampleRate(40)
    .setActualTime(CHANGE_TIME_3)
    .setSystemTime(CHANGE_TIME_3)
    .setActive(true)
    .setInformationSource(INFO_SOURCE)
    .setComment("decommissioned CHAN_JNU_BHE_V3 ReferenceChannel")
    .setPosition(ZERO_POSITION)
    .setAliases(Collections.emptyList())
    .build();

  private static final Location LOCATION_CHAN_JNU_BHN_V1 = Location.from(
    33.1217, 130.8783, 1, 0.54);
  private static final ReferenceChannel CHAN_JNU_BHN_V1 = ReferenceChannel.builder()
    .setName("CHAN_JNU_BHN_V1")
    .setDataType(SEISMIC)
    .setBandType(BROADBAND)
    .setInstrumentType(HIGH_GAIN_SEISMOMETER)
    .setOrientationType(NORTH_SOUTH)
    .setOrientationCode(NORTH_SOUTH.getCode())
    .setLocationCode("0")
    .setLatitude(LOCATION_CHAN_JNU_BHN_V1.getLatitudeDegrees())
    .setLongitude(LOCATION_CHAN_JNU_BHN_V1.getLongitudeDegrees())
    .setElevation(LOCATION_CHAN_JNU_BHN_V1.getElevationKm())
    .setDepth(LOCATION_CHAN_JNU_BHN_V1.getDepthKm())
    .setVerticalAngle(ORIENTATION_90_0.getVerticalAngleDeg())
    .setHorizontalAngle(ORIENTATION_90_0.getHorizontalAngleDeg())
    .setUnits(Units.COUNTS_PER_NANOMETER)
    .setNominalSampleRate(20)
    .setActive(true)
    .setActualTime(CHANGE_TIME_1)
    .setSystemTime(CHANGE_TIME_1)
    .setInformationSource(INFO_SOURCE)
    .setComment("CHAN_JNU_BHN_V1 ReferenceChannel")
    .setPosition(ZERO_POSITION)
    .setAliases(Collections.emptyList())
    .build();

  private static final ReferenceChannel CHAN_JNU_BHN_V2 = ReferenceChannel.builder()
    .setName("CHAN_JNU_BHN_V2")
    .setDataType(SEISMIC)
    .setBandType(BROADBAND)
    .setInstrumentType(HIGH_GAIN_SEISMOMETER)
    .setOrientationType(NORTH_SOUTH)
    .setOrientationCode(NORTH_SOUTH.getCode())
    .setLocationCode("0")
    .setLatitude(NA_VALUE_LOCATION.getLatitudeDegrees())
    .setLongitude(NA_VALUE_LOCATION.getLongitudeDegrees())
    .setElevation(NA_VALUE_LOCATION.getElevationKm())
    .setDepth(NA_VALUE_LOCATION.getDepthKm())
    .setVerticalAngle(NA_ORIENTATION.getVerticalAngleDeg())
    .setHorizontalAngle(NA_ORIENTATION.getHorizontalAngleDeg())
    .setUnits(Units.COUNTS_PER_NANOMETER)
    .setNominalSampleRate(20)
    .setActualTime(CHANGE_TIME_2)
    .setSystemTime(CHANGE_TIME_2)
    .setActive(true)
    .setInformationSource(INFO_SOURCE)
    .setComment("decommissioned CHAN_JNU_BHN_V2 ReferenceChannel")
    .setPosition(ZERO_POSITION)
    .setAliases(Collections.emptyList())
    .build();

  private static final Location LOCATION_CHAN_JNU_BHN_V3 = Location.from(
    33.121667, 130.87833, 0, 0.573);
  private static final ReferenceChannel CHAN_JNU_BHN_V3 = ReferenceChannel.builder()
    .setName("CHAN_JNU_BHN_V3")
    .setDataType(SEISMIC)
    .setBandType(BROADBAND)
    .setInstrumentType(HIGH_GAIN_SEISMOMETER)
    .setOrientationType(NORTH_SOUTH)
    .setOrientationCode(NORTH_SOUTH.getCode())
    .setLocationCode("0")
    .setLatitude(LOCATION_CHAN_JNU_BHN_V3.getLatitudeDegrees())
    .setLongitude(LOCATION_CHAN_JNU_BHN_V3.getLongitudeDegrees())
    .setElevation(LOCATION_CHAN_JNU_BHN_V3.getElevationKm())
    .setDepth(LOCATION_CHAN_JNU_BHN_V3.getDepthKm())
    .setVerticalAngle(ORIENTATION_90_0.getVerticalAngleDeg())
    .setHorizontalAngle(ORIENTATION_90_0.getHorizontalAngleDeg())
    .setUnits(Units.COUNTS_PER_NANOMETER)
    .setNominalSampleRate(40)
    .setActualTime(CHANGE_TIME_3)
    .setSystemTime(CHANGE_TIME_3)
    .setActive(true)
    .setInformationSource(INFO_SOURCE)
    .setComment("decommissioned CHAN_JNU_BHN_V3 ReferenceChannel")
    .setPosition(ZERO_POSITION)
    .setAliases(Collections.emptyList())
    .build();

  private static final Location LOCATION_CHAN_JNU_BHZ_V1 = Location.from(
    33.1217, 130.8783, 1, 0.54);
  private static final ReferenceChannel CHAN_JNU_BHZ_V1 = ReferenceChannel.builder()
    .setName("CHAN_JNU_BHZ_V1")
    .setDataType(SEISMIC)
    .setBandType(BROADBAND)
    .setInstrumentType(HIGH_GAIN_SEISMOMETER)
    .setOrientationType(NORTH_SOUTH)
    .setOrientationCode(NORTH_SOUTH.getCode())
    .setLocationCode("0")
    .setLatitude(LOCATION_CHAN_JNU_BHZ_V1.getLatitudeDegrees())
    .setLongitude(LOCATION_CHAN_JNU_BHZ_V1.getLongitudeDegrees())
    .setElevation(LOCATION_CHAN_JNU_BHZ_V1.getElevationKm())
    .setDepth(LOCATION_CHAN_JNU_BHZ_V1.getDepthKm())
    .setVerticalAngle(ORIENTATION_0_NEG1.getVerticalAngleDeg())
    .setHorizontalAngle(ORIENTATION_0_NEG1.getHorizontalAngleDeg())
    .setUnits(Units.COUNTS_PER_NANOMETER)
    .setNominalSampleRate(20)
    .setActualTime(CHANGE_TIME_1)
    .setSystemTime(CHANGE_TIME_1)
    .setActive(true)
    .setInformationSource(INFO_SOURCE)
    .setComment("CHAN_JNU_BHZ_V1 ReferenceChannel")
    .setPosition(ZERO_POSITION)
    .setAliases(Collections.emptyList())
    .build();

  private static final ReferenceChannel CHAN_JNU_BHZ_V2 = ReferenceChannel.builder()
    .setName("CHAN_JNU_BHZ_V2")
    .setDataType(SEISMIC)
    .setBandType(BROADBAND)
    .setInstrumentType(HIGH_GAIN_SEISMOMETER)
    .setOrientationType(NORTH_SOUTH)
    .setOrientationCode(NORTH_SOUTH.getCode())
    .setLocationCode("0")
    .setLatitude(NA_VALUE_LOCATION.getLatitudeDegrees())
    .setLongitude(NA_VALUE_LOCATION.getLongitudeDegrees())
    .setElevation(NA_VALUE_LOCATION.getElevationKm())
    .setDepth(NA_VALUE_LOCATION.getDepthKm())
    .setVerticalAngle(ORIENTATION_0_NEG1.getVerticalAngleDeg())
    .setHorizontalAngle(ORIENTATION_0_NEG1.getHorizontalAngleDeg())
    .setUnits(Units.COUNTS_PER_NANOMETER)
    .setNominalSampleRate(20)
    .setActive(true)
    .setActualTime(CHANGE_TIME_2)
    .setSystemTime(CHANGE_TIME_2)
    .setInformationSource(INFO_SOURCE)
    .setComment("decommissioned CHAN_JNU_BHZ_V2 ReferenceChannel")
    .setPosition(ZERO_POSITION)
    .setAliases(Collections.emptyList())
    .build();

  private static final Location LOCATION_CHAN_JNU_BHZ_V3 = Location.from(
    33.121667, 130.87833, 0, 0.573);
  private static final ReferenceChannel CHAN_JNU_BHZ_V3 = ReferenceChannel.builder()
    .setName("CHAN_JNU_BHZ_V3")
    .setDataType(SEISMIC)
    .setBandType(BROADBAND)
    .setInstrumentType(HIGH_GAIN_SEISMOMETER)
    .setOrientationType(NORTH_SOUTH)
    .setOrientationCode(NORTH_SOUTH.getCode())
    .setLocationCode("0")
    .setLatitude(LOCATION_CHAN_JNU_BHZ_V3.getLatitudeDegrees())
    .setLongitude(LOCATION_CHAN_JNU_BHZ_V3.getLongitudeDegrees())
    .setElevation(LOCATION_CHAN_JNU_BHZ_V3.getElevationKm())
    .setDepth(LOCATION_CHAN_JNU_BHZ_V3.getDepthKm())
    .setVerticalAngle(ORIENTATION_0_NEG1.getVerticalAngleDeg())
    .setHorizontalAngle(ORIENTATION_0_NEG1.getHorizontalAngleDeg())
    .setUnits(Units.COUNTS_PER_NANOMETER)
    .setNominalSampleRate(40)
    .setActualTime(CHANGE_TIME_3)
    .setSystemTime(CHANGE_TIME_3)
    .setActive(true)
    .setInformationSource(INFO_SOURCE)
    .setComment("decommissioned CHAN_JNU_BHZ_V3 ReferenceChannel")
    .setPosition(ZERO_POSITION)
    .setAliases(Collections.emptyList())
    .build();

  public static final List<ReferenceChannel> ALL_REFERENCE_CHANNELS = List.of(
    CHAN_JNU_BHE_V1, CHAN_JNU_BHE_V2, CHAN_JNU_BHE_V3, CHAN_JNU_BHN_V1, CHAN_JNU_BHN_V2,
    CHAN_JNU_BHN_V3, CHAN_JNU_BHZ_V1, CHAN_JNU_BHZ_V2, CHAN_JNU_BHZ_V3);

  protected static Set<ReferenceSiteMembership> siteMemberships;
  protected static Set<ReferenceStationMembership> stationMemberships;

  static {
    // Define networks
    Instant netImxAuxChangeTime = Instant.ofEpochSecond(604713600);
    NET_IMS_AUX = ReferenceNetwork.builder()
      .setName("IMS_AUX")
      .setDescription("All IMS auxiliary seismic stations")
      .setOrganization(NetworkOrganization.CTBTO)
      .setRegion(NetworkRegion.GLOBAL)
      .setSource(INFO_SOURCE)
      .setComment("")
      .setActualChangeTime(netImxAuxChangeTime)
      .setSystemChangeTime(netImxAuxChangeTime)
      .setActive(true)
      .build();

    Instant idcDaChangeTime = Instant.ofEpochSecond(228700800);
    NET_IDC_DA = ReferenceNetwork.builder()
      .setName("IDC_DA")
      .setDescription("All acquired stations - used by update interval")
      .setOrganization(NetworkOrganization.UNKNOWN)
      .setRegion(NetworkRegion.GLOBAL)
      .setSource(INFO_SOURCE)
      .setComment("")
      .setActualChangeTime(idcDaChangeTime)
      .setSystemChangeTime(idcDaChangeTime)
      .setActive(true)
      .build();

    associateStationsAndNetworks();
    associateSitesAndStations();
    associateChannelAndSites();
  }

  private static void associateStationsAndNetworks() {
    // declare memberships
    UUID jnuId = JNU_V1.getEntityId();
    var imsMember1 = ReferenceNetworkMembership.from(
      UUID.randomUUID(), "", CHANGE_TIME_1, CHANGE_TIME_1,
      NET_IMS_AUX.getEntityId(), jnuId, StatusType.ACTIVE);
    var imsMember2 = ReferenceNetworkMembership.from(
      UUID.randomUUID(), "", CHANGE_TIME_2, CHANGE_TIME_2,
      NET_IMS_AUX.getEntityId(), jnuId, StatusType.INACTIVE);
    var imsMember3 = ReferenceNetworkMembership.from(
      UUID.randomUUID(), "", CHANGE_TIME_3, CHANGE_TIME_3,
      NET_IMS_AUX.getEntityId(), jnuId, StatusType.ACTIVE);
    var idcMember1 = ReferenceNetworkMembership.from(
      UUID.randomUUID(), "", CHANGE_TIME_1, CHANGE_TIME_1,
      NET_IDC_DA.getEntityId(), jnuId, StatusType.ACTIVE);
    var idcMember2 = ReferenceNetworkMembership.from(
      UUID.randomUUID(), "", CHANGE_TIME_2, CHANGE_TIME_2,
      NET_IDC_DA.getEntityId(), jnuId, StatusType.INACTIVE);
    var idcMember3 = ReferenceNetworkMembership.from(
      UUID.randomUUID(), "", CHANGE_TIME_3, CHANGE_TIME_3,
      NET_IDC_DA.getEntityId(), jnuId, StatusType.ACTIVE);
    // set reference to all memberships
    referenceNetworkMemberships = List.of(imsMember1, imsMember2, imsMember3,
      idcMember1, idcMember2, idcMember3);
  }

  private static void associateSitesAndStations() {
    final UUID jnuId = JNU_V1.getEntityId();
    final UUID jnuSiteId = JNU_SITE_V1.getEntityId();
    var member1 = ReferenceStationMembership.from(
      UUID.randomUUID(), "", CHANGE_TIME_1, CHANGE_TIME_1,
      jnuId, jnuSiteId, StatusType.ACTIVE);
    var member2 = ReferenceStationMembership.from(
      UUID.randomUUID(), "", CHANGE_TIME_2, CHANGE_TIME_2,
      jnuId, jnuSiteId, StatusType.INACTIVE);
    var member3 = ReferenceStationMembership.from(
      UUID.randomUUID(), "", CHANGE_TIME_3, CHANGE_TIME_3,
      jnuId, jnuSiteId, StatusType.ACTIVE);
    stationMemberships = Set.of(member1, member2, member3);
  }

  private static void associateChannelAndSites() {
    UUID jnuSiteId = JNU_SITE_V1.getEntityId();
    String channelName = CHAN_JNU_BHE_V1.getName();
    var member1 = ReferenceSiteMembership.from(
      UUID.randomUUID(), "", CHANGE_TIME_1, CHANGE_TIME_1,
      jnuSiteId, channelName, StatusType.ACTIVE);
    var member2 = ReferenceSiteMembership.from(
      UUID.randomUUID(), "", CHANGE_TIME_2, CHANGE_TIME_2,
      jnuSiteId, channelName, StatusType.INACTIVE);
    var member3 = ReferenceSiteMembership.from(
      UUID.randomUUID(), "", CHANGE_TIME_3, CHANGE_TIME_3,
      jnuSiteId, channelName, StatusType.ACTIVE);
    siteMemberships = Set.of(member1, member2, member3);
  }

  static {
    calibrations.add(REF_CALIBRATION_BHE_V_1);
    responses.add(REFERENCE_RESPONSE);
  }
}
