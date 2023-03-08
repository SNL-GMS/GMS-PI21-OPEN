package gms.shared.frameworks.osd.coi.test.utils;

import gms.shared.frameworks.osd.coi.PhaseType;
import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.channel.ChannelBandType;
import gms.shared.frameworks.osd.coi.channel.ChannelDataType;
import gms.shared.frameworks.osd.coi.channel.ChannelFactory;
import gms.shared.frameworks.osd.coi.channel.ChannelGroup;
import gms.shared.frameworks.osd.coi.channel.ChannelGroup.Type;
import gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType;
import gms.shared.frameworks.osd.coi.channel.ChannelOrientationType;
import gms.shared.frameworks.osd.coi.channel.ChannelProcessingMetadataType;
import gms.shared.frameworks.osd.coi.channel.Orientation;
import gms.shared.frameworks.osd.coi.channel.ReferenceChannel;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueAnalog;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import gms.shared.frameworks.osd.coi.provenance.InformationSource;
import gms.shared.frameworks.osd.coi.signaldetection.BeamDefinition;
import gms.shared.frameworks.osd.coi.signaldetection.FilterCausality;
import gms.shared.frameworks.osd.coi.signaldetection.FilterDefinition;
import gms.shared.frameworks.osd.coi.signaldetection.FilterPassBandType;
import gms.shared.frameworks.osd.coi.signaldetection.FilterSource;
import gms.shared.frameworks.osd.coi.signaldetection.FilterType;
import gms.shared.frameworks.osd.coi.signaldetection.FkSpectraDefinition;
import gms.shared.frameworks.osd.coi.signaldetection.Location;
import gms.shared.frameworks.osd.coi.signaldetection.MeasuredChannelSegmentDescriptor;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.stationreference.RelativePosition;
import gms.shared.frameworks.osd.coi.stationreference.StationType;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UtilsTestFixtures {

  public static final String STATION_NAME = "STA";
  public static final String GROUP_NAME = "GROUP";

  public static final UUID CHANNEL_SEGMENT_ID = UUID
    .fromString("57015315-f7b2-4487-b3e7-8780fbcfb413");

  private static final Instant SEGMENT_START = Instant.parse("1970-01-02T03:04:05.123Z");
  private static final Instant SEGMENT_END = SEGMENT_START.plusMillis(2000);

  // AcquiredChannelSohAnalog
  public static final UUID SOH_ANALOG_ID = UUID.fromString("b12c0b3a-4681-4ee3-82fc-4fcc292aa59f");
  public static final String PROCESSING_CHANNEL_2_NAME = "PROCESSING_CHANNEL_2_NAME";
  public static final AcquiredChannelEnvironmentIssueAnalog channelSohAnalog = AcquiredChannelEnvironmentIssueAnalog
    .from(
      PROCESSING_CHANNEL_2_NAME,
      AcquiredChannelEnvironmentIssueType.STATION_POWER_VOLTAGE, SEGMENT_START, SEGMENT_END,
      1.5);

  // AcquiredChannelSohBoolean
  public static final String PROCESSING_CHANNEL_1_NAME = "PROCESSING_CHANNEL_1_NAME";
  public static final AcquiredChannelEnvironmentIssueBoolean channelSohBoolean = AcquiredChannelEnvironmentIssueBoolean
    .from(
      PROCESSING_CHANNEL_1_NAME,
      AcquiredChannelEnvironmentIssueType.DEAD_SENSOR_CHANNEL, SEGMENT_START, SEGMENT_END,
      true);

  public static final String EXAMPLE_STATION = "Example Station";
  public static final Channel CHANNEL = Channel.from(
    "Real Channel Name", "Canonical Name",
    "Example description",
    EXAMPLE_STATION,
    ChannelDataType.DIAGNOSTIC_SOH,
    ChannelBandType.BROADBAND,
    ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
    ChannelOrientationType.VERTICAL,
    'Z',
    Units.COUNTS_PER_NANOMETER,
    65.0,
    Location.from(35.0,
      -125.0,
      100.0,
      5500.0),
    Orientation.from(
      65.0,
      135.0
    ),
    List.of(),
    Map.of(),
    Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, GROUP_NAME));

  public static final Channel CHANNEL_TWO = Channel.from(
    "Another Real Channel Name", "Another Canonical Name",
    "Example description",
    EXAMPLE_STATION,
    ChannelDataType.DIAGNOSTIC_SOH,
    ChannelBandType.BROADBAND,
    ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
    ChannelOrientationType.VERTICAL,
    'Z',
    Units.COUNTS_PER_NANOMETER,
    65.0,
    Location.from(35.0,
      -125.0,
      100.0,
      5500.0),
    Orientation.from(
      65.0,
      135.0
    ),
    List.of(),
    Map.of(),
    Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, GROUP_NAME));

  public static final Station STATION = Station.from(
    EXAMPLE_STATION,
    StationType.HYDROACOUSTIC,
    "This is a test station",
    Map.of(CHANNEL.getName(), RelativePosition.from(50.0, 5.0, 10.0),
      CHANNEL_TWO.getName(), RelativePosition.from(50.0, 5.0, 10.0)),
    Location.from(35.647, 100.0, 50.0, 10.0),
    List.of(ChannelGroup.from(
      "test group",
      "This is a test",
      Location.from(45.678, 120.0, 50.0, 55.0),
      Type.PROCESSING_GROUP,
      List.of(CHANNEL, CHANNEL_TWO)
    )),
    List.of(CHANNEL, CHANNEL_TWO)
  );

  public static final ChannelGroup channelGroup = ChannelGroup.from(
    "Test Channel Group",
    "Some description",
    Location.from(35.0,
      -125.0,
      100.0,
      5500.0),
    Type.PROCESSING_GROUP,
    List.of(CHANNEL)
  );

  public static final StationGroup STATION_GROUP = StationGroup.from(GROUP_NAME,
    "Test Station Group",
    List.of(STATION));

  // TODO: use a single ChannelDataType implementation
  private static final ChannelDataType dataType = ChannelDataType.SEISMIC;

  // Reference Channel definition
  public static final Location location = Location.from(7.7, 11.11, 0.04, 3.3);
  public static final Orientation orientationAngles = Orientation.from(0.0, 0.0);
  public static final double NOMINAL_SAMPLE_RATE_HZ = 40.0;
  public static final ChannelBandType bandType = ChannelBandType.BROADBAND;
  public static final ChannelInstrumentType instrumentType = ChannelInstrumentType.HIGH_GAIN_SEISMOMETER;
  public static final ChannelOrientationType orientationType = ChannelOrientationType.VERTICAL;
  public static final String RAW_CHANNEL_NAME = new String(new char[]{
    bandType.getCode(), instrumentType.getCode(), orientationType.getCode()});
  private static final InformationSource infoSource = InformationSource.from("IDC",
    Instant.now(), "IDC");

  public static final ReferenceChannel referenceChannel = ReferenceChannel.builder()
    .setName(RAW_CHANNEL_NAME)
    .setDataType(dataType)
    .setBandType(bandType)
    .setInstrumentType(instrumentType)
    .setOrientationType(orientationType)
    .setOrientationCode(orientationType.getCode())
    .setLocationCode("locationCode")
    .setLatitude(location.getLatitudeDegrees())
    .setLongitude(location.getLongitudeDegrees())
    .setElevation(location.getElevationKm())
    .setDepth(location.getDepthKm())
    .setVerticalAngle(orientationAngles.getVerticalAngleDeg())
    .setHorizontalAngle(orientationAngles.getHorizontalAngleDeg())
    .setUnits(Units.COUNTS_PER_NANOMETER)
    .setNominalSampleRate(NOMINAL_SAMPLE_RATE_HZ)
    .setActualTime(Instant.EPOCH)
    .setSystemTime(Instant.EPOCH)
    .setInformationSource(infoSource)
    .setComment("station reference comment")
    .setPosition(RelativePosition.from(0.0, 0.0, 0.0))
    .setAliases(Collections.emptyList())
    .setActive(true)
    .setInformationSource(InformationSource.from("originating org", Instant.EPOCH, "reference"))
    .build();

  public static final MeasuredChannelSegmentDescriptor DESCRIPTOR =
    MeasuredChannelSegmentDescriptor.builder()
      .setChannelName(CHANNEL.getName())
      .setMeasuredChannelSegmentStartTime(Instant.EPOCH)
      .setMeasuredChannelSegmentEndTime(Instant.EPOCH.plusSeconds(5))
      .setMeasuredChannelSegmentCreationTime(Instant.EPOCH.plusSeconds(6))
      .build();

  // Raw
  public static final Channel raw = ChannelFactory
    .rawFromReferenceChannel(referenceChannel, STATION_NAME, GROUP_NAME);

  // Derived Channel: FilterDefinition
  public static final FilterType type = FilterType.FIR_HAMMING;
  public static final FilterPassBandType passBandType = FilterPassBandType.LOW_PASS;
  public static final double LOW = 0.0;
  public static final double HIGH = 5.0;
  public static final FilterCausality causality = FilterCausality.CAUSAL;
  public static final double GROUP_DELAY = 1.5;

  public static final FilterDefinition filterDefinition = FilterDefinition.builder()
    .setName("Detection filter")
    .setDescription("Detection low pass filter")
    .setFilterType(type)
    .setFilterPassBandType(passBandType)
    .setLowFrequencyHz(LOW)
    .setHighFrequencyHz(HIGH)
    .setOrder(1)
    .setFilterSource(FilterSource.SYSTEM)
    .setFilterCausality(causality)
    .setZeroPhase(true)
    .setSampleRate(40.0)
    .setSampleRateTolerance(3.14)
    .setACoefficients(new double[]{6.7, 7.8})
    .setBCoefficients(new double[]{3.4, 4.5})
    .setGroupDelaySecs(GROUP_DELAY)
    .build();

  public static final Channel filtered = ChannelFactory.filtered(raw, filterDefinition);

  // Derived Channel: BeamDefinition
  public static final double BEAM_AZIMUTH_DEG = 67.3;
  public static final double BEAM_SLOWNESS_SEC_PER_DEG = 4.32;
  public static final boolean BEAM_COHERENT = true;

  public static final BeamDefinition beamDefinition = BeamDefinition.builder()
    .setPhaseType(PhaseType.Lg)
    .setAzimuth(BEAM_AZIMUTH_DEG)
    .setSlowness(BEAM_SLOWNESS_SEC_PER_DEG)
    .setCoherent(BEAM_COHERENT)
    .setSnappedSampling(true)
    .setTwoDimensional(false)
    .setNominalWaveformSampleRate(NOMINAL_SAMPLE_RATE_HZ + 2.5)
    .setWaveformSampleRateTolerance(1.2)
    .setMinimumWaveformsForBeam(2)
    .build();

  public static final Channel beamed = ChannelFactory.beamed(STATION, List.of(raw), beamDefinition);

  // Derived Channel: FKDefinition

  // Create an FkSpectraDefinition
  private static final Duration windowLead = Duration.ofMinutes(3);
  private static final Duration windowLength = Duration.ofMinutes(2);
  private static final double FK_SAMPLE_RATE = 1 / 60.0;

  private static final double LOW_FREQUENCY = 4.5;
  private static final double HIGH_FREQUENCY = 6.0;

  private static final boolean USE_CHANNEL_VERTICAL_OFFSETS = false;
  private static final boolean NORMALIZE_WAVEFORMS = false;
  private static final PhaseType phaseType = PhaseType.P;

  private static final double EAST_SLOW_START = 5;
  private static final double EAST_SLOW_DELTA = 10;
  private static final int EAST_SLOW_COUNT = 25;
  private static final double NORTH_SLOW_START = 5;
  private static final double NORTH_SLOW_DELTA = 10;
  private static final int NORTH_SLOW_COUNT = 25;

  private static final double WAVEFORM_SAMPLE_RATE_HZ = 10.0;
  private static final double WAVEFORM_SAMPLE_RATE_TOLERANCE_HZ = 11.0;

  public static final FkSpectraDefinition FK_SPECTRA_DEFINITION = FkSpectraDefinition.builder()
    .setWindowLead(windowLead)
    .setWindowLength(windowLength)
    .setSampleRateHz(FK_SAMPLE_RATE)
    .setLowFrequencyHz(LOW_FREQUENCY)
    .setHighFrequencyHz(HIGH_FREQUENCY)
    .setUseChannelVerticalOffsets(USE_CHANNEL_VERTICAL_OFFSETS)
    .setNormalizeWaveforms(NORMALIZE_WAVEFORMS)
    .setPhaseType(phaseType)
    .setSlowStartXSecPerKm(EAST_SLOW_START)
    .setSlowDeltaXSecPerKm(EAST_SLOW_DELTA)
    .setSlowCountX(EAST_SLOW_COUNT)
    .setSlowStartYSecPerKm(NORTH_SLOW_START)
    .setSlowDeltaYSecPerKm(NORTH_SLOW_DELTA)
    .setSlowCountY(NORTH_SLOW_COUNT)
    .setWaveformSampleRateHz(WAVEFORM_SAMPLE_RATE_HZ)
    .setWaveformSampleRateToleranceHz(WAVEFORM_SAMPLE_RATE_TOLERANCE_HZ)
    .setMinimumWaveformsForSpectra(2)
    .build();

  public static final Channel fked = ChannelFactory.fk(STATION, List.of(raw), FK_SPECTRA_DEFINITION);

  // ACEIAnalogLatestData
  public static final List<AcquiredChannelEnvironmentIssueAnalog> latestAnalogs = List.of(
    AcquiredChannelEnvironmentIssueAnalog.from(
      CHANNEL.getName(),
      AcquiredChannelEnvironmentIssueType.CLIPPED,
      SEGMENT_START.plusMillis(300),
      SEGMENT_START.plusMillis(500),
      0.9),
    AcquiredChannelEnvironmentIssueAnalog.from(
      CHANNEL_TWO.getName(),
      AcquiredChannelEnvironmentIssueType.CLIPPED,
      SEGMENT_START.plusMillis(300),
      SEGMENT_START.plusMillis(500),
      0.9));

  public static final List<AcquiredChannelEnvironmentIssueAnalog> earlierAnalogs = List.of(
    AcquiredChannelEnvironmentIssueAnalog.from(
      CHANNEL.getName(),
      AcquiredChannelEnvironmentIssueType.CLIPPED,
      SEGMENT_START,
      SEGMENT_START.plusMillis(100),
      0.9),
    AcquiredChannelEnvironmentIssueAnalog.from(
      CHANNEL.getName(),
      AcquiredChannelEnvironmentIssueType.CLIPPED,
      SEGMENT_START.plusMillis(100),
      SEGMENT_START.plusMillis(200),
      0.9),
    AcquiredChannelEnvironmentIssueAnalog.from(
      CHANNEL_TWO.getName(),
      AcquiredChannelEnvironmentIssueType.CLIPPED,
      SEGMENT_START,
      SEGMENT_START.plusMillis(100),
      0.9),
    AcquiredChannelEnvironmentIssueAnalog.from(
      CHANNEL_TWO.getName(),
      AcquiredChannelEnvironmentIssueType.CLIPPED,
      SEGMENT_START.plusMillis(100),
      SEGMENT_START.plusMillis(200),
      0.9));

  // ACEIBooleanLatestData
  public static final List<AcquiredChannelEnvironmentIssueBoolean> latestBooleans = List.of(
    AcquiredChannelEnvironmentIssueBoolean.from(
      CHANNEL.getName(),
      AcquiredChannelEnvironmentIssueType.DEAD_SENSOR_CHANNEL,
      SEGMENT_START.plusMillis(300),
      SEGMENT_START.plusMillis(500),
      true),
    AcquiredChannelEnvironmentIssueBoolean.from(
      CHANNEL_TWO.getName(),
      AcquiredChannelEnvironmentIssueType.DEAD_SENSOR_CHANNEL,
      SEGMENT_START.plusMillis(300),
      SEGMENT_START.plusMillis(500),
      true));

  public static final List<AcquiredChannelEnvironmentIssueBoolean> earlierBoolean = List.of(
    AcquiredChannelEnvironmentIssueBoolean.from(
      CHANNEL.getName(),
      AcquiredChannelEnvironmentIssueType.DEAD_SENSOR_CHANNEL,
      SEGMENT_START,
      SEGMENT_START.plusMillis(100),
      true),
    AcquiredChannelEnvironmentIssueBoolean.from(
      CHANNEL.getName(),
      AcquiredChannelEnvironmentIssueType.DEAD_SENSOR_CHANNEL,
      SEGMENT_START.plusMillis(100),
      SEGMENT_START.plusMillis(200),
      true),
    AcquiredChannelEnvironmentIssueBoolean.from(
      CHANNEL_TWO.getName(),
      AcquiredChannelEnvironmentIssueType.DEAD_SENSOR_CHANNEL,
      SEGMENT_START,
      SEGMENT_START.plusMillis(100),
      true),
    AcquiredChannelEnvironmentIssueBoolean.from(
      CHANNEL_TWO.getName(),
      AcquiredChannelEnvironmentIssueType.DEAD_SENSOR_CHANNEL,
      SEGMENT_START.plusMillis(100),
      SEGMENT_START.plusMillis(200),
      true));

  public static final List<AcquiredChannelEnvironmentIssueBoolean> latestBooleansMultipleTypes = List.of(
    AcquiredChannelEnvironmentIssueBoolean.from(
      CHANNEL.getName(),
      AcquiredChannelEnvironmentIssueType.BEGINNING_DATE_OUTAGE,
      SEGMENT_START.plusMillis(300),
      SEGMENT_START.plusMillis(500),
      true),
    AcquiredChannelEnvironmentIssueBoolean.from(
      CHANNEL.getName(),
      AcquiredChannelEnvironmentIssueType.DATA_AVAILABILITY_GEOPHYSICAL_CHANNELS,
      SEGMENT_START.plusMillis(300),
      SEGMENT_START.plusMillis(500),
      true), AcquiredChannelEnvironmentIssueBoolean.from(
      CHANNEL.getName(),
      AcquiredChannelEnvironmentIssueType.CLOCK_DIFFERENTIAL_TOO_LARGE,
      SEGMENT_START.plusMillis(300),
      SEGMENT_START.plusMillis(500),
      true), AcquiredChannelEnvironmentIssueBoolean.from(
      CHANNEL.getName(),
      AcquiredChannelEnvironmentIssueType.AUTHENTICATION_SEAL_BROKEN,
      SEGMENT_START.plusMillis(300),
      SEGMENT_START.plusMillis(500),
      true), AcquiredChannelEnvironmentIssueBoolean.from(
      CHANNEL.getName(),
      AcquiredChannelEnvironmentIssueType.AMPLIFIER_SATURATION_DETECTED,
      SEGMENT_START.plusMillis(300),
      SEGMENT_START.plusMillis(500),
      true),
    AcquiredChannelEnvironmentIssueBoolean.from(
      CHANNEL_TWO.getName(),
      AcquiredChannelEnvironmentIssueType.ZEROED_DATA,
      SEGMENT_START.plusMillis(300),
      SEGMENT_START.plusMillis(500),
      true));


  private UtilsTestFixtures() {

  }
}
