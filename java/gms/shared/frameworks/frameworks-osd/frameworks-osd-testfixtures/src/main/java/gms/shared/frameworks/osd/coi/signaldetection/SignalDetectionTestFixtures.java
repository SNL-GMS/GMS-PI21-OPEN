package gms.shared.frameworks.osd.coi.signaldetection;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.DoubleValue;
import gms.shared.frameworks.osd.coi.InstantValue;
import gms.shared.frameworks.osd.coi.PhaseType;
import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.channel.ChannelDataType;
import gms.shared.frameworks.osd.coi.channel.ChannelSegment;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.signaldetection.EnumeratedMeasurementValue.FirstMotionMeasurementValue;
import gms.shared.frameworks.osd.coi.signaldetection.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.frameworks.osd.coi.stationreference.RelativePosition;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import gms.shared.frameworks.osd.coi.waveforms.Waveform;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Defines objects used in testing
 */
public class SignalDetectionTestFixtures {

  /*
  Explicit constructor
   */
  private SignalDetectionTestFixtures() {
  }

  public static final ObjectMapper objMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  // QcMask Related Test Fixtures

  public static final QcMask qcMask;
  public static final QcMaskVersion qcMaskVersion;
  public static final QcMaskVersionDescriptor QC_MASK_VERSION_DESCRIPTOR;

  static {
    qcMask = QcMask.create("Test Channel",
      List.of(QcMaskVersionDescriptor.from(UUID.randomUUID(), 3),
        QcMaskVersionDescriptor.from(UUID.randomUUID(), 1)),
      List.of(UUID.randomUUID(), UUID.randomUUID()), QcMaskCategory.WAVEFORM_QUALITY,
      QcMaskType.LONG_GAP, "Rationale", Instant.now(), Instant.now().plusSeconds(2));

    qcMask.addQcMaskVersion(List.of(UUID.randomUUID(), UUID.randomUUID()),
      QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.SPIKE, "Rationale SPIKE",
      Instant.now().plusSeconds(3), Instant.now().plusSeconds(4));

    qcMaskVersion = qcMask.getCurrentQcMaskVersion();

    QC_MASK_VERSION_DESCRIPTOR = qcMaskVersion.getParentQcMasks().iterator().next();
  }

  // Processing Station Reference Test Fixtures

  public static final double LAT = 67.00459;
  public static final double LON = -103.00459;
  public static final double ELEV = 13.05;
  public static final double DEPTH = 6.899;
  public static final double VERTICAL_ANGLE = 3.4;
  public static final double HORIZONTAL_ANGLE = 5.7;
  public static final String DESCRIPTION = "";

  // Create a Calibration
  public static final double FACTOR = 1.2;
  public static final double FACTOR_ERROR = 0.112;
  public static final double PERIOD = 14.5;
  public static final long TIME_SHIFT = (long) 2.24;
  public static final DoubleValue CAL_FACTOR = DoubleValue.from(FACTOR, FACTOR_ERROR, Units.SECONDS);
  public static final Duration calTimeShift = Duration.ofSeconds(TIME_SHIFT);

  public static final Calibration calibration = Calibration.from(
    PERIOD,
    calTimeShift,
    CAL_FACTOR);

  // Create a Response

  // create an AmplitudePhaseResponse -> amplitudePhaseResponse
  public static final DoubleValue amplitude = DoubleValue.from(0.000014254, 0.0,
    Units.NANOMETERS_PER_COUNT);
  public static final DoubleValue phase = DoubleValue.from(350.140599, 0.0, Units.DEGREES);

  public static final AmplitudePhaseResponse amplitudePhaseResponse = AmplitudePhaseResponse
    .from(amplitude, phase);

  // create a FrequencyAmplitudePhase (fapResponse) using amplitudePhaseResponse created above
  public static final double FREQUENCY = 0.001000;
  public static final FrequencyAmplitudePhase FAP_RESPONSE =
    FrequencyAmplitudePhase.builder()
      .setFrequencies(new double[]{FREQUENCY})
      .setAmplitudeResponseUnits(Units.NANOMETERS_PER_COUNT)
      .setAmplitudeResponse(new double[]{0.000014254})
      .setAmplitudeResponseStdDev(new double[]{0.0})
      .setPhaseResponseUnits(Units.DEGREES)
      .setPhaseResponse(new double[]{350.140599})
      .setPhaseResponseStdDev(new double[]{0.0})
      .build();

  //create a FrequencyAmplitudePhase (fapResponse using TWO amplitudePhaseResponses created above...
  public static final double FREQUENCY_2 = 0.001010;
  public static final FrequencyAmplitudePhase RESPONSE_BY_FREQUENCY_2 =
    FrequencyAmplitudePhase.builder()
      .setFrequencies(new double[]{FREQUENCY, FREQUENCY_2})
      .setAmplitudeResponseUnits(Units.NANOMETERS_PER_COUNT)
      .setAmplitudeResponse(new double[]{0.000014254, 0.000014685})
      .setAmplitudeResponseStdDev(new double[]{0.0, 0.0})
      .setPhaseResponseUnits(Units.DEGREES)
      .setPhaseResponse(new double[]{350.140599, 350.068990})
      .setPhaseResponseStdDev(new double[]{0.0, 0.0})
      .build();

  public static final UUID ID = UUID.fromString("cccaa77a-b6a4-478f-b3cd-5c934ee6b999");

  // Create a Channel
  public static final UUID CHANNEL_ID = UUID.fromString("d07aa77a-b6a4-478f-b3cd-5c934ee6b812");
  public static final String CHANNEL_NAME = "CHAN01";
  public static final ChannelDataType CHANNEL_DATA_TYPE = ChannelDataType.SEISMIC;
  public static final Channel CHANNEL = UtilsTestFixtures.CHANNEL;

  // create the response using fapResponse created above
  public static final Response RESPONSE = Response.from(
    CHANNEL_NAME,
    calibration,
    FAP_RESPONSE);

  // Create a Channel Segment
  public static final UUID PROCESSING_CHANNEL_1_ID = UUID
    .fromString("46947cc2-8c86-4fa1-a764-c9b9944614b7");
  public static final Instant SEGMENT_START = Instant.parse("1970-01-02T03:04:05.123Z");
  public static final Instant SEGMENT_END = SEGMENT_START.plusMillis(2000);
  public static final double SAMPLE_RATE = 2.0;
  protected static final double[] WAVEFORM_POINTS = new double[]{1.1, 2.2, 3.3, 4.4, 5.5};
  public static final Waveform WAVEFORM_1 = Waveform.from(SEGMENT_START, SAMPLE_RATE,
    WAVEFORM_POINTS);
  public static final Collection<Waveform> waveforms = Collections.singleton(WAVEFORM_1);
  public static final UUID CHANNEL_SEGMENT_ID = UUID
    .fromString("57015315-f7b2-4487-b3e7-8780fbcfb413");
  public static final ChannelSegment<Waveform> CHANNEL_SEGMENT = ChannelSegment
    .from(CHANNEL_SEGMENT_ID,
      UtilsTestFixtures.CHANNEL, "segmentName",
      ChannelSegment.Type.RAW, waveforms);

  // Create a Location
  public static final Location LOCATION = Location.from(1.2, 3.4, 7.8, 5.6);

  // Create a RelativePosition
  public static final RelativePosition RELATIVE_POSITION = RelativePosition
    .from(1.2, 3.4, 5.6);

  // Create an FkSpectraDefinition
  private static final Duration WINDOW_LEAD = Duration.ofMinutes(3);
  private static final Duration WINDOW_LENGTH = Duration.ofMinutes(2);
  private static final double FK_SAMPLE_RATE = 1 / 60.0;

  private static final double LOW_FREQUENCY = 4.5;
  private static final double HIGH_FREQUENCY = 6.0;

  private static final boolean USE_CHANNEL_VERTICAL_OFFSETS = false;
  private static final boolean NORMALIZE_WAVEFORMS = false;
  private static final PhaseType PHASE_TYPE = PhaseType.P;

  private static final double EAST_SLOW_START = 5;
  private static final double EAST_SLOW_DELTA = 10;
  private static final int EAST_SLOW_COUNT = 25;
  private static final double NORTH_SLOW_START = 5;
  private static final double NORTH_SLOW_DELTA = 10;
  private static final int NORTH_SLOW_COUNT = 25;

  private static final double WAVEFORM_SAMPLE_RATE_HZ = 10.0;
  private static final double WAVEFORM_SAMPLE_RATE_TOLERANCE_HZ = 11.0;

  // Beam Definition Test Fixtures
  private static final double AZIMUTH = 37.5;
  private static final double SLOWNESS = 17.2;
  private static final double NOMINAL_SAMPLE_RATE = 40.0;
  private static final double SAMPLE_RATE_TOLERANCE = 2.0;

  private static boolean coherent = true;
  private static boolean snappedSampling = true;
  private static boolean twoDimensional = true;

  public static final BeamDefinition BEAM_DEFINITION = BeamDefinition
    .builder()
    .setPhaseType(PHASE_TYPE)
    .setAzimuth(AZIMUTH)
    .setSlowness(SLOWNESS)
    .setCoherent(coherent)
    .setSnappedSampling(snappedSampling)
    .setTwoDimensional(twoDimensional)
    .setNominalWaveformSampleRate(NOMINAL_SAMPLE_RATE)
    .setWaveformSampleRateTolerance(SAMPLE_RATE_TOLERANCE)
    .setMinimumWaveformsForBeam(1)
    .build();

  public static final FkSpectraDefinition FK_SPECTRA_DEFINITION = FkSpectraDefinition.builder()
    .setWindowLead(WINDOW_LEAD)
    .setWindowLength(WINDOW_LENGTH)
    .setSampleRateHz(FK_SAMPLE_RATE)
    .setLowFrequencyHz(LOW_FREQUENCY)
    .setHighFrequencyHz(HIGH_FREQUENCY)
    .setUseChannelVerticalOffsets(USE_CHANNEL_VERTICAL_OFFSETS)
    .setNormalizeWaveforms(NORMALIZE_WAVEFORMS)
    .setPhaseType(PHASE_TYPE)
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

  // ------- Event -------

  // ------- EventHypothesis -------

  // ------- SignalDetectionEventAssociation -------

  public static final DoubleValue standardDoubleValue = DoubleValue.from(5, 1, Units.SECONDS);
  public static final InstantValue ARRIVAL_TIME_MEASUREMENT = InstantValue.from(
    Instant.EPOCH, Duration.ofMillis(1));
  public static final PhaseTypeMeasurementValue phaseMeasurement = PhaseTypeMeasurementValue.from(
    PhaseType.P, 0.5);
  public static final FirstMotionMeasurementValue firstMotionMeasurement =
    FirstMotionMeasurementValue
      .from(
        FirstMotionType.UP, 0.5);
  public static final AmplitudeMeasurementValue amplitudeMeasurement = AmplitudeMeasurementValue
    .from(
      Instant.EPOCH, Duration.ofMillis(1), standardDoubleValue);
  public static final InstantValue instantMeasurement = InstantValue.from(
    Instant.EPOCH, Duration.ofMillis(1));

  public static final MeasuredChannelSegmentDescriptor DESCRIPTOR =
    MeasuredChannelSegmentDescriptor.builder()
      .setChannelName(UtilsTestFixtures.CHANNEL.getName())
      .setMeasuredChannelSegmentStartTime(Instant.EPOCH)
      .setMeasuredChannelSegmentEndTime(Instant.EPOCH.plusSeconds(5))
      .setMeasuredChannelSegmentCreationTime(Instant.EPOCH.plusSeconds(6))
      .build();

  public static final FeatureMeasurement<InstantValue> ARRIVAL_TIME_FEATURE_MEASUREMENT
    = FeatureMeasurement.from(UtilsTestFixtures.CHANNEL, DESCRIPTOR,
    FeatureMeasurementTypes.ARRIVAL_TIME, ARRIVAL_TIME_MEASUREMENT);
  public static final FeatureMeasurement<PhaseTypeMeasurementValue> PHASE_FEATURE_MEASUREMENT
    = FeatureMeasurement.from(UtilsTestFixtures.CHANNEL, DESCRIPTOR,
    FeatureMeasurementTypes.PHASE, phaseMeasurement);
  public static final FeatureMeasurement<FirstMotionMeasurementValue> FIRST_MOTION_FEATURE_MEASUREMENT
    = FeatureMeasurement.from(UtilsTestFixtures.CHANNEL, DESCRIPTOR,
    FeatureMeasurementTypes.FIRST_MOTION, firstMotionMeasurement);
  public static final FeatureMeasurement<AmplitudeMeasurementValue> AMPLITUDE_FEATURE_MEASUREMENT
    = FeatureMeasurement.from(UtilsTestFixtures.CHANNEL, DESCRIPTOR,
    FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2, amplitudeMeasurement);
  public static final FeatureMeasurement<InstantValue> INSTANT_FEATURE_MEASUREMENT
    = FeatureMeasurement.from(UtilsTestFixtures.CHANNEL, DESCRIPTOR,
    FeatureMeasurementTypes.ARRIVAL_TIME, instantMeasurement);

  private static final UUID SIGNAL_DETECTION_ID = UUID.randomUUID();
  private static final String MONITORING_ORG = "Test Monitoring Org";
  public static final UUID SIGNAL_DETECTION_HYPOTHESIS_ID = UUID.randomUUID();
  public static final SignalDetectionHypothesis SIGNAL_DETECTION_HYPOTHESIS =
    SignalDetectionHypothesis.builder(SIGNAL_DETECTION_HYPOTHESIS_ID, SIGNAL_DETECTION_ID,
        MONITORING_ORG, UtilsTestFixtures.STATION.getName(), null, false)
      .addMeasurement(ARRIVAL_TIME_FEATURE_MEASUREMENT)
      .addMeasurement(PHASE_FEATURE_MEASUREMENT)
      .build();

  public static final SignalDetection SIGNAL_DETECTION = SignalDetection.from(SIGNAL_DETECTION_ID,
    MONITORING_ORG,
    UtilsTestFixtures.STATION.getName(),
    List.of(SIGNAL_DETECTION_HYPOTHESIS));
}
