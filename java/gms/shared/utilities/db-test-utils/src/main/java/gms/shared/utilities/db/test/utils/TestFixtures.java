package gms.shared.utilities.db.test.utils;

import gms.shared.frameworks.osd.coi.DoubleValue;
import gms.shared.frameworks.osd.coi.InstantValue;
import gms.shared.frameworks.osd.coi.PhaseType;
import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.channel.ChannelBandType;
import gms.shared.frameworks.osd.coi.channel.ChannelDataType;
import gms.shared.frameworks.osd.coi.channel.ChannelGroup;
import gms.shared.frameworks.osd.coi.channel.ChannelGroup.Type;
import gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType;
import gms.shared.frameworks.osd.coi.channel.ChannelOrientationType;
import gms.shared.frameworks.osd.coi.channel.ChannelProcessingMetadataType;
import gms.shared.frameworks.osd.coi.channel.ChannelSegment;
import gms.shared.frameworks.osd.coi.channel.Orientation;
import gms.shared.frameworks.osd.coi.channel.ReferenceChannel;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueAnalog;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import gms.shared.frameworks.osd.coi.event.DepthRestraintType;
import gms.shared.frameworks.osd.coi.event.Ellipse;
import gms.shared.frameworks.osd.coi.event.Ellipsoid;
import gms.shared.frameworks.osd.coi.event.Event;
import gms.shared.frameworks.osd.coi.event.EventHypothesis;
import gms.shared.frameworks.osd.coi.event.EventLocation;
import gms.shared.frameworks.osd.coi.event.FeaturePrediction;
import gms.shared.frameworks.osd.coi.event.FeaturePredictionComponent;
import gms.shared.frameworks.osd.coi.event.FeaturePredictionCorrectionType;
import gms.shared.frameworks.osd.coi.event.FinalEventHypothesis;
import gms.shared.frameworks.osd.coi.event.LocationBehavior;
import gms.shared.frameworks.osd.coi.event.LocationRestraint;
import gms.shared.frameworks.osd.coi.event.LocationSolution;
import gms.shared.frameworks.osd.coi.event.LocationUncertainty;
import gms.shared.frameworks.osd.coi.event.MagnitudeModel;
import gms.shared.frameworks.osd.coi.event.MagnitudeType;
import gms.shared.frameworks.osd.coi.event.NetworkMagnitudeBehavior;
import gms.shared.frameworks.osd.coi.event.NetworkMagnitudeSolution;
import gms.shared.frameworks.osd.coi.event.PreferredEventHypothesis;
import gms.shared.frameworks.osd.coi.event.PreferredLocationSolution;
import gms.shared.frameworks.osd.coi.event.RestraintType;
import gms.shared.frameworks.osd.coi.event.ScalingFactorType;
import gms.shared.frameworks.osd.coi.event.StationMagnitudeSolution;
import gms.shared.frameworks.osd.coi.provenance.InformationSource;
import gms.shared.frameworks.osd.coi.signaldetection.AmplitudeMeasurementValue;
import gms.shared.frameworks.osd.coi.signaldetection.Calibration;
import gms.shared.frameworks.osd.coi.signaldetection.EnumeratedMeasurementValue;
import gms.shared.frameworks.osd.coi.signaldetection.FeatureMeasurement;
import gms.shared.frameworks.osd.coi.signaldetection.FeatureMeasurementTypes;
import gms.shared.frameworks.osd.coi.signaldetection.FrequencyAmplitudePhase;
import gms.shared.frameworks.osd.coi.signaldetection.Location;
import gms.shared.frameworks.osd.coi.signaldetection.MeasuredChannelSegmentDescriptor;
import gms.shared.frameworks.osd.coi.signaldetection.NumericMeasurementValue;
import gms.shared.frameworks.osd.coi.signaldetection.QcMask;
import gms.shared.frameworks.osd.coi.signaldetection.QcMaskCategory;
import gms.shared.frameworks.osd.coi.signaldetection.QcMaskType;
import gms.shared.frameworks.osd.coi.signaldetection.QcMaskVersion;
import gms.shared.frameworks.osd.coi.signaldetection.QcMaskVersionDescriptor;
import gms.shared.frameworks.osd.coi.signaldetection.SignalDetection;
import gms.shared.frameworks.osd.coi.signaldetection.SignalDetectionHypothesis;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.stationreference.NetworkOrganization;
import gms.shared.frameworks.osd.coi.stationreference.NetworkRegion;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceCalibration;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceNetwork;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceNetworkMembership;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceResponse;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSite;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSiteMembership;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSourceResponse;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceStation;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceStationMembership;
import gms.shared.frameworks.osd.coi.stationreference.RelativePosition;
import gms.shared.frameworks.osd.coi.stationreference.ResponseTypes;
import gms.shared.frameworks.osd.coi.stationreference.StationType;
import gms.shared.frameworks.osd.coi.stationreference.StatusType;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import gms.shared.frameworks.osd.coi.waveforms.FkAttributes;
import gms.shared.frameworks.osd.coi.waveforms.FkSpectra;
import gms.shared.frameworks.osd.coi.waveforms.FkSpectrum;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame.AuthenticationStatus;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrameMetadata;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFramePayloadFormat;
import gms.shared.frameworks.osd.coi.waveforms.Waveform;
import gms.shared.frameworks.osd.coi.waveforms.WaveformSummary;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptyList;

public class TestFixtures {

  private TestFixtures() {
  }

  private static final String CHANNEL_NAME_1 = "testChannelOne";
  private static final String CHANNEL_NAME_2 = "testChannelTwo";
  private static final String CHANNEL_DESCRIPTION = "This is a description of the channel";
  private static final String CHANNEL_GROUP_NAME = "channelGroupOne";
  private static final String STATION_NAME_1 = "stationOne";
  private static final String STATION_NAME_2 = "stationTwo";

  public static final Channel channelWithNonExistentStation = Channel.from(
    CHANNEL_NAME_1,
    "Test Channel One",
    CHANNEL_DESCRIPTION,
    "stationDoesNotExist",
    ChannelDataType.DIAGNOSTIC_SOH,
    ChannelBandType.BROADBAND,
    ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
    ChannelOrientationType.EAST_WEST,
    'E',
    Units.HERTZ,
    50.0,
    Location.from(100.0, 10.0, 50.0, 100),
    Orientation.from(10.0, 35.0),
    List.of(),
    Map.of(),
    Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, CHANNEL_GROUP_NAME)
  );
  public static final Channel channel1 = Channel.from(
    CHANNEL_NAME_1,
    "Test Channel One",
    CHANNEL_DESCRIPTION,
    STATION_NAME_1,
    ChannelDataType.DIAGNOSTIC_SOH,
    ChannelBandType.BROADBAND,
    ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
    ChannelOrientationType.EAST_WEST,
    'E',
    Units.HERTZ,
    50.0,
    Location.from(100.0, 10.0, 50.0, 100),
    Orientation.from(10.0, 35.0),
    List.of(),
    Map.of(),
    Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, CHANNEL_GROUP_NAME)
  );
  public static final Channel channel2 = Channel.from(
    CHANNEL_NAME_2,
    "Test Channel Two",
    CHANNEL_DESCRIPTION,
    STATION_NAME_1,
    ChannelDataType.DIAGNOSTIC_SOH,
    ChannelBandType.BROADBAND,
    ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
    ChannelOrientationType.EAST_WEST,
    'E',
    Units.HERTZ,
    50.0,
    Location.from(100.0, 10.0, 50.0, 100),
    Orientation.from(10.0, 35.0),
    List.of(),
    Map.of(),
    Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, CHANNEL_GROUP_NAME)
  );
  public static final Channel channel3 = Channel.from(
    "testChannelThree",
    "Test Channel Three",
    CHANNEL_DESCRIPTION,
    STATION_NAME_1,
    ChannelDataType.DIAGNOSTIC_SOH,
    ChannelBandType.BROADBAND,
    ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
    ChannelOrientationType.EAST_WEST,
    'E',
    Units.HERTZ,
    50.0,
    Location.from(100.0, 10.0, 50.0, 100),
    Orientation.from(10.0, 35.0),
    List.of(),
    Map.of(),
    Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, CHANNEL_GROUP_NAME)
  );
  public static final Channel channel4 = Channel.from(
    "testChannelFour",
    "Test Channel Four",
    CHANNEL_DESCRIPTION,
    STATION_NAME_1,
    ChannelDataType.DIAGNOSTIC_SOH,
    ChannelBandType.BROADBAND,
    ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
    ChannelOrientationType.EAST_WEST,
    'E',
    Units.HERTZ,
    50.0,
    Location.from(100.0, 10.0, 50.0, 100),
    Orientation.from(10.0, 35.0),
    List.of(),
    Map.of(),
    Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, CHANNEL_GROUP_NAME)
  );
  public static final Channel channel5 = Channel.from(
    "testChannelFive",
    "Test Channel Five",
    CHANNEL_DESCRIPTION,
    STATION_NAME_1,
    ChannelDataType.DIAGNOSTIC_SOH,
    ChannelBandType.BROADBAND,
    ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
    ChannelOrientationType.EAST_WEST,
    'E',
    Units.HERTZ,
    50.0,
    Location.from(100.0, 10.0, 50.0, 100),
    Orientation.from(10.0, 35.0),
    List.of(),
    Map.of(),
    Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, CHANNEL_GROUP_NAME)
  );
  public static final Channel channel6 = Channel.from(
    "testChannelSix",
    "Test Channel Six",
    CHANNEL_DESCRIPTION,
    STATION_NAME_1,
    ChannelDataType.DIAGNOSTIC_SOH,
    ChannelBandType.BROADBAND,
    ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
    ChannelOrientationType.EAST_WEST,
    'E',
    Units.HERTZ,
    50.0,
    Location.from(100.0, 10.0, 50.0, 100),
    Orientation.from(10.0, 35.0),
    List.of(),
    Map.of(),
    Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, CHANNEL_GROUP_NAME)
  );
  public static final Channel derivedChannelOne = Channel.from(
    "derivedChannelOne",
    "Derived Channel One",
    CHANNEL_DESCRIPTION,
    STATION_NAME_1,
    ChannelDataType.DIAGNOSTIC_SOH,
    ChannelBandType.BROADBAND,
    ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
    ChannelOrientationType.EAST_WEST,
    'E',
    Units.HERTZ,
    50.0,
    Location.from(100.0, 10.0, 50.0, 100),
    Orientation.from(10.0, 35.0),
    List.of(channel1.getName(), channel6.getName()),
    Map.of(),
    Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, CHANNEL_GROUP_NAME)
  );

  public static final Channel channel7 = Channel.from(
    "testChannelSeven",
    "Test Channel Seven",
    CHANNEL_DESCRIPTION,
    STATION_NAME_2,
    ChannelDataType.DIAGNOSTIC_SOH,
    ChannelBandType.BROADBAND,
    ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
    ChannelOrientationType.EAST_WEST,
    'E',
    Units.HERTZ,
    50.0,
    Location.from(100.0, 10.0, 50.0, 100),
    Orientation.from(10.0, 35.0),
    List.of(),
    Map.of(),
    Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, "")
  );

  public static final Channel channel8 = Channel.from(
    "testChannelEight",
    "Test Channel Eight",
    CHANNEL_DESCRIPTION,
    STATION_NAME_2,
    ChannelDataType.DIAGNOSTIC_SOH,
    ChannelBandType.BROADBAND,
    ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
    ChannelOrientationType.EAST_WEST,
    'E',
    Units.HERTZ,
    50.0,
    Location.from(100.0, 10.0, 50.0, 100),
    Orientation.from(10.0, 35.0),
    List.of(),
    Map.of(),
    Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, "")
  );

  public static final Channel derivedChannelTwo = Channel.from(
    "derivedChannelTwo",
    "Derived from Test Channel Seven",
    CHANNEL_DESCRIPTION,
    STATION_NAME_2,
    ChannelDataType.DIAGNOSTIC_SOH,
    ChannelBandType.BROADBAND,
    ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
    ChannelOrientationType.EAST_WEST,
    'E',
    Units.HERTZ,
    50.0,
    Location.from(100.0, 10.0, 50.0, 100),
    Orientation.from(10.0, 35.0),
    List.of(TestFixtures.channel7.getName()),
    Map.of(),
    Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, "")
  );

  public static final ChannelGroup channelGroupOne = ChannelGroup.from(
    CHANNEL_GROUP_NAME,
    "Sample channel group containing all test suite channels",
    Location.from(100.0, 10.0, 50.0, 100.0),
    Type.SITE_GROUP,
    List.of(channel1, channel2, channel3, channel4, channel5, channel6));

  public static final ChannelGroup channelGroupTwo = ChannelGroup.from(
    "channelGroupTwo",
    "Sample channel group containing all test suite channels",
    Location.from(100.0, 10.0, 50.0, 100.0),
    Type.SITE_GROUP,
    List.of(channel7));

  public static final Station station = Station.from(
    STATION_NAME_1,
    StationType.SEISMIC_ARRAY,
    "Test station",
    Map.of(
      CHANNEL_NAME_1, RelativePosition.from(50.0, 55.0, 60.0),
      CHANNEL_NAME_2, RelativePosition.from(40.0, 35.0, 60.0),
      "testChannelThree", RelativePosition.from(30.0, 15.0, 60.0),
      "testChannelFour", RelativePosition.from(20.0, 40.0, 60.0),
      "testChannelFive", RelativePosition.from(32.5, 16.0, 60.0),
      "testChannelSix", RelativePosition.from(22.5, 27.0, 60.0)),
    Location.from(135.75, 65.75, 50.0, 0.0),
    List.of(channelGroupOne),
    List.of(channel1, channel2, channel3, channel4, channel5, channel6));

  public static final Station stationTwo = Station.from(
    STATION_NAME_2,
    StationType.SEISMIC_ARRAY,
    "Test station",
    Map.of(
      "testChannelSeven", RelativePosition.from(50.0, 55.0, 64.0)
    ),
    Location.from(135.75, 65.75, 50.0, 0.0),
    List.of(channelGroupTwo),
    List.of(channel7));

  public static final StationGroup STATION_GROUP = StationGroup.from(
    "testStationGroup",
    "This is an example of a station group",
    List.of(station)
  );
  public static final String UNKNOWN_NAME = "someFakeName";
  public static final UUID UNKNOWN_ID = UUID.fromString("e2a78dbc-97d6-466b-9dd4-4e3fdf6dd95b");

  // ReferenceChannels
  private static final Instant CHANGE_TIME_1 = Instant.ofEpochSecond(797731200);
  private static final Instant CHANGE_TIME_2 = Instant.ofEpochSecond(1195430400);
  private static final Instant CHANGE_TIME_3 = Instant.ofEpochSecond(1232496000);
  private static final ChannelBandType BROADBAND = ChannelBandType.BROADBAND;
  private static final ChannelDataType SEISMIC = ChannelDataType.SEISMIC;
  private static final ChannelInstrumentType HIGH_GAIN_SEISMOMETER =
    ChannelInstrumentType.HIGH_GAIN_SEISMOMETER;
  private static final ChannelOrientationType EAST_WEST = ChannelOrientationType.EAST_WEST;
  private static final ChannelOrientationType NORTH_SOUTH = ChannelOrientationType.NORTH_SOUTH;
  private static final InformationSource infoSource = InformationSource.from("IDC",
    Instant.now(), "IDC");

  private static final RelativePosition ZERO_POSITION = RelativePosition.from(0.0, 0.0, 0.0);
  private static final Orientation ORIENTATION_90_90 = Orientation.from(90.0, 90.0);
  private static final Orientation ORIENTATION_90_0 = Orientation.from(90.0, 0.0);
  private static final Orientation ORIENTATION_0_NEG1 = Orientation.from(0.0, -1.0);
  private static final Orientation NA_ORIENTATION = Orientation.from(-1, -1);
  private static final double NA_VALUE = -999.0;
  private static final Location NA_VALUE_LOCATION = Location.from(
    NA_VALUE, NA_VALUE, NA_VALUE, NA_VALUE);

  // Channel BHE (3 versions)
  private static final Location LOCATION_CHAN_JNU_BHE_V1 = Location
    .from(33.1217, 130.8783, 1, 0.54);
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
    .setInformationSource(infoSource)
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
    .setInformationSource(infoSource)
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
    .setInformationSource(infoSource)
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
    .setActualTime(CHANGE_TIME_1)
    .setSystemTime(CHANGE_TIME_1)
    .setActive(true)
    .setInformationSource(infoSource)
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
    .setInformationSource(infoSource)
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
    .setInformationSource(infoSource)
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
    .setInformationSource(infoSource)
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
    .setActualTime(CHANGE_TIME_2)
    .setSystemTime(CHANGE_TIME_2)
    .setActive(true)
    .setInformationSource(infoSource)
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
    .setInformationSource(infoSource)
    .setComment("decommissioned CHAN_JNU_BHZ_V3 ReferenceChannel")
    .setPosition(ZERO_POSITION)
    .setAliases(Collections.emptyList())
    .build();

  public static final List<ReferenceChannel> allReferenceChannels = List.of(
    CHAN_JNU_BHE_V1, CHAN_JNU_BHE_V2, CHAN_JNU_BHE_V3, CHAN_JNU_BHN_V1, CHAN_JNU_BHN_V2,
    CHAN_JNU_BHN_V3, CHAN_JNU_BHZ_V1, CHAN_JNU_BHZ_V2, CHAN_JNU_BHZ_V3);

  // ReferenceSourceResponses
  private static final ReferenceSourceResponse REFERENCE_SOURCE_RESPONSE = ReferenceSourceResponse
    .builder()
    .setSourceResponseData("test".getBytes())
    .setSourceResponseUnits(Units.COUNTS_PER_NANOMETER)
    .setSourceResponseTypes(ResponseTypes.FAP)
    .setInformationSources(List.of(infoSource))
    .build();

  // ReferenceCalibration
  private static final Duration TEN_SECOND_DURATION = Duration.ofSeconds(10);
  private static final DoubleValue CAL_FACTOR = DoubleValue
    .from(1.992, 10.3, Units.NANOMETERS_PER_COUNT);

  private static final Calibration CALIBRATION_BHE_V1 =
    Calibration.from(1, TEN_SECOND_DURATION, CAL_FACTOR);
  private static final Calibration CALIBRATION_BHE_V2 =
    Calibration.from(2, TEN_SECOND_DURATION, CAL_FACTOR);
  private static final Calibration CALIBRATION_BHE_V3 =
    Calibration.from(3, TEN_SECOND_DURATION, CAL_FACTOR);

  private static final ReferenceCalibration REF_CALIBRATION_BHE_V1 =
    ReferenceCalibration.from(TEN_SECOND_DURATION, CALIBRATION_BHE_V1);
  private static final ReferenceCalibration REF_CALIBRATION_BHE_V2 =
    ReferenceCalibration.from(TEN_SECOND_DURATION, CALIBRATION_BHE_V2);
  private static final ReferenceCalibration REF_CALIBRATION_BHE_V3 =
    ReferenceCalibration.from(TEN_SECOND_DURATION, CALIBRATION_BHE_V3);

  // FrequencyAmplitudePhase
  private static final Optional<FrequencyAmplitudePhase> fap = Optional.of(
    FrequencyAmplitudePhase.builder()
      .setFrequencies(new double[]{8.9})
      .setAmplitudeResponseUnits(Units.HERTZ)
      .setAmplitudeResponse(new double[]{0.1})
      .setAmplitudeResponseStdDev(new double[]{2.3})
      .setPhaseResponseUnits(Units.HERTZ)
      .setPhaseResponse(new double[]{4.5})
      .setPhaseResponseStdDev(new double[]{6.7})
      .build());

  // ReferenceResponses
  // bhe responses
  private static final ReferenceResponse RESPONSE_BHE_V1 = ReferenceResponse.builder()
    .setChannelName("RESPONSE_BHE_V1")
    .setActualTime(CHANGE_TIME_1)
    .setSystemTime(CHANGE_TIME_1)
    .setComment("nm/c")
    .setSourceResponse(REFERENCE_SOURCE_RESPONSE)
    .setReferenceCalibration(REF_CALIBRATION_BHE_V1)
    .setFapResponse(fap)
    .build();

  private static final ReferenceResponse RESPONSE_BHE_V2 =
    ReferenceResponse.builder()
      .setChannelName("RESPONSE_BHE_V2")
      .setActualTime(CHANGE_TIME_2)
      .setSystemTime(CHANGE_TIME_2)
      .setComment("nm/c")
      .setSourceResponse(REFERENCE_SOURCE_RESPONSE)
      .setReferenceCalibration(REF_CALIBRATION_BHE_V2)
      .setFapResponse(fap)
      .build();

  private static final ReferenceResponse RESPONSE_BHE_V3 = ReferenceResponse.builder()
    .setChannelName("RESPONSE_BHE_V3")
    .setActualTime(CHANGE_TIME_3)
    .setSystemTime(CHANGE_TIME_3)
    .setComment("nm/c")
    .setSourceResponse(REFERENCE_SOURCE_RESPONSE)
    .setReferenceCalibration(REF_CALIBRATION_BHE_V3)
    .setFapResponse(fap)
    .build();

  public static final List<ReferenceResponse> ALL_REFERENCE_RESPONSES = List.of(RESPONSE_BHE_V1,
    RESPONSE_BHE_V2, RESPONSE_BHE_V3);

  // Waveform Summaries
  public static final Map<String, WaveformSummary> waveformSummaries = Map.of(
    channel1.getName(), WaveformSummary.from(channel1.getName(),
      Instant.now(), Instant.now().plusSeconds(20L)));

  // Waveforms / ChannelSegments
  private static final String SEGMENT_START_DATE_STRING = "1970-01-02T03:04:05.123Z";
  private static final double SAMPLE_RATE1 = 2.0;
  private static final double SAMPLE_RATE2 = 5.0;
  private static final double SAMPLE_RATE3 = 6.0;
  private static final double[] WAVEFORM_POINTS1 = new double[]{1.1, 2.2, 3.3, 4.4, 5.5};
  private static final double[] WAVEFORM_POINTS2 = new double[]{6, 7, 8, 9, 10};
  private static final double[] WAVEFORM_POINTS3 = new double[]{11, 12, 13, 14, 15, 16, 17};
  public static final Instant SEGMENT_START = Instant.parse(SEGMENT_START_DATE_STRING);

  private static final UUID CHANNEL_SEGMENT1_ID = UUID
    .fromString("57015315-f7b2-4487-b3e7-8780fbcfb413");
  public static final Waveform waveform1 = Waveform.from(
    SEGMENT_START, SAMPLE_RATE1, WAVEFORM_POINTS1);
  private static final List<Waveform> waveforms1 = List.of(waveform1);
  public static final ChannelSegment<Waveform> channelSegment1 = ChannelSegment.from(
    CHANNEL_SEGMENT1_ID, channel1, channel1.getName(),
    ChannelSegment.Type.RAW, waveforms1);
  public static final Instant SEGMENT1_END = channelSegment1.getEndTime();

  private static final UUID CHANNEL_SEGMENT2_ID = UUID
    .fromString("67015315-f7b2-4487-b3e7-8780fbcfb413");
  public static final Instant SEGMENT_START2 = waveform1.getEndTime().plusSeconds(1);
  public static final Waveform waveform2 = Waveform.from(
    SEGMENT_START2, SAMPLE_RATE2, WAVEFORM_POINTS2);
  private static final List<Waveform> waveforms2 = List.of(waveform2);
  public static final ChannelSegment<Waveform> channelSegment2 = ChannelSegment.from(
    CHANNEL_SEGMENT2_ID, channel2, channel2.getName(),
    ChannelSegment.Type.RAW, waveforms2);
  public static final Instant SEGMENT_END2 = channelSegment2.getEndTime();

  private static final UUID CHANNEL_SEGMENT3_ID = UUID
    .fromString("77015315-f7b2-4487-b3e7-8780fbcfb413");
  public static final Instant SEGMENT_START3 = waveform2.getEndTime().plusSeconds(1);
  public static final Waveform waveform3 = Waveform.from(
    SEGMENT_START3, SAMPLE_RATE3, WAVEFORM_POINTS3);
  private static final List<Waveform> waveforms3 = List.of(waveform3);
  public static final ChannelSegment<Waveform> channelSegment3 = ChannelSegment.from(
    CHANNEL_SEGMENT3_ID, channel3, channel3.getName(),
    ChannelSegment.Type.RAW, waveforms3);

  // RawStationDataFrames
  private static final String FRAME1_STATION_NAME = "TEST STATION";

  private static final String FRAME2_STATION_NAME = "TEST STATION 2";

  public static final RawStationDataFrame frame1 = RawStationDataFrame.builder()
    .setId(UUID.randomUUID())
    .setMetadata(RawStationDataFrameMetadata.builder()
      .setStationName(FRAME1_STATION_NAME)
      .setChannelNames(List.of(channel1.getName(), channel2.getName()))
      .setPayloadFormat(RawStationDataFramePayloadFormat.CD11)
      .setPayloadStartTime(SEGMENT_START)
      .setPayloadEndTime(SEGMENT1_END)
      .setReceptionTime(SEGMENT1_END.plusSeconds(10))
      .setAuthenticationStatus(AuthenticationStatus.AUTHENTICATION_SUCCEEDED)
      .setWaveformSummaries(waveformSummaries)
      .build())
    .setRawPayload(new byte[50])
    .build();

  public static final RawStationDataFrame frame2 = RawStationDataFrame.builder()
    .setId(UUID.randomUUID())
    .setMetadata(RawStationDataFrameMetadata.builder()
      .setStationName(FRAME2_STATION_NAME)
      .setChannelNames(List.of(channel2.getName()))
      .setPayloadFormat(RawStationDataFramePayloadFormat.CD11)
      .setPayloadStartTime(SEGMENT_START2)
      .setPayloadEndTime(SEGMENT_END2)
      .setReceptionTime(SEGMENT_END2.plusSeconds(10))
      .setAuthenticationStatus(AuthenticationStatus.AUTHENTICATION_FAILED)
      .setWaveformSummaries(Map.of(channel2.getName(), WaveformSummary.from(channel2.getName(),
        Instant.now(), Instant.now().plusSeconds(20L))))
      .build())
    .setRawPayload(new byte[50])
    .build();

  public static final List<RawStationDataFrame> allFrames = List.of(frame1, frame2);

  public static final QcMask createQcMask(Instant startTime, Instant endTime, int numVersions,
    Channel channel) {
    var qcMaskId = UUID.randomUUID();
    var channelSegmentId = UUID.randomUUID();
    var parentQcMask = QcMaskVersionDescriptor.from(qcMaskId, 1);
    List<QcMaskVersion> qcMaskVersions = new ArrayList<>();
    for (var i = 0; i < numVersions; i++) {
      qcMaskVersions.add(QcMaskVersion.builder()
        .setVersion(i + 1L)
        .setParentQcMasks(List.of(parentQcMask))
        .setChannelSegmentIds(List.of(channelSegmentId))
        .setType(QcMaskType.LONG_GAP)
        .setCategory(QcMaskCategory.WAVEFORM_QUALITY)
        .setRationale("Testing")
        .setStartTime(startTime)
        .setEndTime(endTime)
        .build());
    }

    return QcMask.from(qcMaskId, channel.getName(), qcMaskVersions);
  }

  public static ChannelSegment<FkSpectra> buildFkSpectraChannelSegment() {
    return ChannelSegment.create(channel1, channel1.getName(), ChannelSegment.Type.FK_SPECTRA,
      List.of(buildFkSpectra()));
  }

  public static Waveform buildLongWaveform(Instant startTime, int durationSeconds,
    double sampleRate) {
    var values = new double[(int) Math.floor(durationSeconds * sampleRate)];
    var random = new SecureRandom();
    for (var i = 0; i < values.length; i++) {
      values[i] = random.nextFloat();
    }

    return Waveform.from(startTime, sampleRate, values);
  }

  public static FkSpectra buildFkSpectra() {

    var fkPower1 = new double[25][25];
    var fkPower2 = new double[25][25];
    var fkFstat1 = new double[25][25];
    var fkFstat2 = new double[25][25];

    for (var i = 0; i < 25; i++) {
      for (var j = 0; j < 25; j++) {
        fkPower1[i][j] = (j == 0 ? Double.NaN : (i * 10.0) + (j + 1));
        fkPower2[i][j] = (j == 1 ? Double.NaN : (i * 15.0) + (j + 1));
        fkFstat1[i][j] = (j == 0 ? Double.NaN : (i * 20.0) + (j + 1));
        fkFstat2[i][j] = ((j == 1) ? Double.NaN : (i * 25.0) + (j + 1));
      }
    }

    var fkAttributes1 = FkAttributes.from(
      1.23,
      4.56,
      7.89,
      0.12,
      3.45
    );

    var fkAttributes2 = FkAttributes.from(
      0.98,
      7.65,
      4.32,
      1.09,
      8.76
    );

    var metadata = FkSpectra.Metadata.builder()
      .setPhaseType(PhaseType.P)
      .setSlowStartX(5)
      .setSlowDeltaX(10)
      .setSlowStartY(5)
      .setSlowDeltaY(10)
      .build();

    return FkSpectra.builder()
      .setStartTime(Instant.EPOCH)
      .setSampleRate(1 / 60.0)
      .withValues(List.of(
        FkSpectrum.from(
          fkPower1, fkFstat1, 1, List.of(fkAttributes1)),
        FkSpectrum.from(
          fkPower2, fkFstat2, 1, List.of(fkAttributes2))))
      .setMetadata(metadata)
      .build();
  }

  // SOH
  public static final String FAKE_CHANNEL_NAME = "FAKE CHANNEL NAME";

  public static final AcquiredChannelEnvironmentIssueAnalog channelSohAnalog =
    AcquiredChannelEnvironmentIssueAnalog
      .from(
        channel1.getName(),
        AcquiredChannelEnvironmentIssueType.STATION_POWER_VOLTAGE,
        SEGMENT_START, SEGMENT1_END, 1.5);

  public static final AcquiredChannelEnvironmentIssueBoolean channelSohBool =
    AcquiredChannelEnvironmentIssueBoolean
      .from(
        channel1.getName(),
        AcquiredChannelEnvironmentIssueType.DEAD_SENSOR_CHANNEL,
        SEGMENT_START, SEGMENT1_END, true);

  private static final double LAT = 23.9;
  private static final double LON = -89.0;
  private static final double DEPTH = 0.06;
  private static final double ZERO_DEPTH = 0.0;
  public static final Instant ARRIVAL_TIME = Instant.EPOCH.plusSeconds(60);

  private static final double RESIDUAL = 2.1;
  private static final double WEIGHT = 0.87;
  private static final boolean DEFINING = false;

  // We use the UtilsTestFixtures channel so we can match up on stations later.
  private static final MeasuredChannelSegmentDescriptor DESCRIPTOR =
    MeasuredChannelSegmentDescriptor.builder()
      .setChannelName(UtilsTestFixtures.CHANNEL.getName())
      .setMeasuredChannelSegmentStartTime(Instant.EPOCH)
      .setMeasuredChannelSegmentEndTime(Instant.EPOCH.plusSeconds(300))
      .setMeasuredChannelSegmentCreationTime(Instant.EPOCH.plusSeconds(360))
      .build();

  public static final FeatureMeasurement<InstantValue> ARRIVAL_TIME_MEASUREMENT = FeatureMeasurement
    .from(UtilsTestFixtures.CHANNEL,
      DESCRIPTOR,
      FeatureMeasurementTypes.ARRIVAL_TIME,
      InstantValue.from(ARRIVAL_TIME, Duration.ofMillis(5)));

  public static final FeatureMeasurement<EnumeratedMeasurementValue.PhaseTypeMeasurementValue> PHASE_MEASUREMENT = FeatureMeasurement
    .from(UtilsTestFixtures.CHANNEL,
      DESCRIPTOR,
      FeatureMeasurementTypes.PHASE,
      EnumeratedMeasurementValue.PhaseTypeMeasurementValue.from(PhaseType.P, 0.5));

  private static final UUID SIGNAL_DETECTION_ID = UUID.randomUUID();
  private static final String MONITORING_ORG = "Test Org";
  public static final SignalDetectionHypothesis SIGNAL_DETECTION_HYPOTHESIS =
    SignalDetectionHypothesis.from(UUID.randomUUID(),
      SIGNAL_DETECTION_ID,
      MONITORING_ORG,
      UtilsTestFixtures.STATION.getName(),
      null,
      false,
      List.of(ARRIVAL_TIME_MEASUREMENT, PHASE_MEASUREMENT));

  public static final SignalDetection SIGNAL_DETECTION = SignalDetection.from(SIGNAL_DETECTION_ID,
    MONITORING_ORG,
    UtilsTestFixtures.STATION.getName(),
    List.of(SIGNAL_DETECTION_HYPOTHESIS));

  //Create an Ellipse.
  private static final ScalingFactorType SCALING_FACTOR_TYPE_1 = ScalingFactorType.CONFIDENCE;
  private static final double K_WEIGHT = 0.0;
  private static final double CONFIDENCE_LEVEL = 0.5;
  private static final double MAJOR_AXIS_LENGTH = 0.0;
  private static final double MAJOR_AXIS_TREND = 0.0;
  private static final double MAJOR_AXIS_PLUNGE = 0.0;
  private static final double INTERMEDIATE_AXIS_LENGTH = 0.0;
  private static final double INTERMEDIATE_AXIS_TREND = 0.0;
  private static final double INTERMEDIATE_AXIS_PLUNGE = 0.0;
  private static final double MINOR_AXIS_LENGTH = 0.0;
  private static final double MINOR_AXIS_TREND = 0.0;
  private static final double DEPTH_UNCERTAINTY = 0.0;
  private static final Duration TIME_UNCERTAINTY = Duration.ofSeconds(5);

  private static final Ellipse ELLIPSE = Ellipse
    .from(SCALING_FACTOR_TYPE_1, K_WEIGHT, CONFIDENCE_LEVEL, MAJOR_AXIS_LENGTH, MAJOR_AXIS_TREND,
      MINOR_AXIS_LENGTH, MINOR_AXIS_TREND, DEPTH_UNCERTAINTY, TIME_UNCERTAINTY);
  private static final Ellipsoid ELLIPSOID = Ellipsoid
    .from(SCALING_FACTOR_TYPE_1, K_WEIGHT, CONFIDENCE_LEVEL,
      MAJOR_AXIS_LENGTH, MAJOR_AXIS_TREND, MAJOR_AXIS_PLUNGE,
      INTERMEDIATE_AXIS_LENGTH, INTERMEDIATE_AXIS_TREND,
      INTERMEDIATE_AXIS_PLUNGE, MINOR_AXIS_LENGTH, INTERMEDIATE_AXIS_TREND,
      INTERMEDIATE_AXIS_PLUNGE, TIME_UNCERTAINTY);

  private static final LocationUncertainty LOCATION_UNCERTAINTY = LocationUncertainty
    .from(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
      0.0, 0.0, 0.0, Set.of(ELLIPSE), Set.of(ELLIPSOID));

  private static final LocationRestraint LOCATION_RESTRAINT = LocationRestraint.from(
    RestraintType.FIXED,
    LAT,
    RestraintType.FIXED,
    LON,
    DepthRestraintType.FIXED_AT_SURFACE,
    ZERO_DEPTH,
    RestraintType.FIXED,
    ARRIVAL_TIME);

  public static final EventLocation LOCATION = EventLocation.from(
    1, 1, DEPTH, ARRIVAL_TIME);
  public static final EventLocation LOCATION_2 = EventLocation.from(
    50, 50, DEPTH, ARRIVAL_TIME.plusSeconds(60));
  private static final EventLocation LOCATION_3 = EventLocation.from(
    25, 25, DEPTH, ARRIVAL_TIME.plusSeconds(160));

  public static final FeatureMeasurement<AmplitudeMeasurementValue> AMPLITUDE_FEATURE_MEASUREMENT =
    FeatureMeasurement.from(UtilsTestFixtures.CHANNEL,
      DESCRIPTOR,
      FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2,
      AmplitudeMeasurementValue.from(
        Instant.EPOCH, Duration.ofSeconds(1), DoubleValue.from(0.0, 0.0, Units.DEGREES)));

  public static final FeatureMeasurement<AmplitudeMeasurementValue> AMPLITUDE_FEATURE_MEASUREMENT_1 =
    FeatureMeasurement.from(UtilsTestFixtures.CHANNEL,
      DESCRIPTOR,
      FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2,
      AmplitudeMeasurementValue.from(
        Instant.EPOCH, Duration.ofSeconds(1), DoubleValue.from(1.0, 1.0, Units.DEGREES)));

  public static final FeatureMeasurement<AmplitudeMeasurementValue> AMPLITUDE_FEATURE_MEASUREMENT_2 =
    FeatureMeasurement.from(UtilsTestFixtures.CHANNEL,
      DESCRIPTOR,
      FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2,
      AmplitudeMeasurementValue.from(
        Instant.EPOCH, Duration.ofSeconds(1), DoubleValue.from(1.1, 1.0, Units.DEGREES)));

  public static final StationMagnitudeSolution STATION_MAGNITUDE_SOLUTION =
    StationMagnitudeSolution.builder()
      .setMagnitude(3.5)
      .setMagnitudeUncertainty(2.0)
      .setStationCorrection(2.0)
      .setStationName(UtilsTestFixtures.STATION.getName())
      .setMeasurement(AMPLITUDE_FEATURE_MEASUREMENT)
      .setModel(MagnitudeModel.VEITH_CLAWSON)
      .setModelCorrection(1.0)
      .setPhase(PhaseType.P)
      .setType(MagnitudeType.MB)
      .build();

  public static final StationMagnitudeSolution STATION_MAGNITUDE_SOLUTION_2 =
    StationMagnitudeSolution.builder()
      .setMagnitude(3.5)
      .setMagnitudeUncertainty(0.1)
      .setStationCorrection(0.0)
      .setStationName(UtilsTestFixtures.STATION.getName())
      .setMeasurement(AMPLITUDE_FEATURE_MEASUREMENT_2)
      .setModel(MagnitudeModel.VEITH_CLAWSON)
      .setModelCorrection(0.0)
      .setPhase(PhaseType.P)
      .setType(MagnitudeType.MB)
      .build();

  public static final NetworkMagnitudeBehavior NETWORK_MAGNITUDE_BEHAVIOR =
    NetworkMagnitudeBehavior.builder()
      .setDefining(false)
      .setResidual(0.0)
      .setWeight(0.0)
      .setStationMagnitudeSolution(STATION_MAGNITUDE_SOLUTION)
      .build();

  public static final NetworkMagnitudeSolution NETWORK_MAGNITUDE_SOLUTION =
    NetworkMagnitudeSolution.builder()
      .setMagnitudeType(MagnitudeType.MB)
      .setMagnitude(3.5)
      .setUncertainty(0.0)
      .setNetworkMagnitudeBehaviors(List.of(NETWORK_MAGNITUDE_BEHAVIOR))
      .build();

  public static final NetworkMagnitudeBehavior NETWORK_MAGNITUDE_BEHAVIOR_2 =
    NetworkMagnitudeBehavior.builder()
      .setDefining(false)
      .setResidual(0.0)
      .setWeight(0.0)
      .setStationMagnitudeSolution(STATION_MAGNITUDE_SOLUTION_2)
      .build();

  public static final NetworkMagnitudeSolution NETWORK_MAGNITUDE_SOLUTION_2 =
    NetworkMagnitudeSolution.builder()
      .setMagnitudeType(MagnitudeType.MB)
      .setMagnitude(3.5)
      .setUncertainty(0.0)
      .setNetworkMagnitudeBehaviors(List.of(NETWORK_MAGNITUDE_BEHAVIOR_2))
      .build();

  private static final List<NetworkMagnitudeSolution> NETWORK_MAGNITUDE_SOLUTIONS =
    List.of(NETWORK_MAGNITUDE_SOLUTION);

  private static final List<NetworkMagnitudeSolution> NETWORK_MAGNITUDE_SOLUTIONS_2 =
    List.of(NETWORK_MAGNITUDE_SOLUTION_2);

  // Create FeaturePrediction objects.
  private static final FeaturePrediction<NumericMeasurementValue> FEATURE_PREDICTION_1 =
    FeaturePrediction.<NumericMeasurementValue>builder()
      .setPhase(PhaseType.P)
      .setPredictedValue(NumericMeasurementValue.from(Instant.EPOCH,
        DoubleValue.from(1.0, 0.1, Units.SECONDS)))
      .setFeaturePredictionComponents(Set.of(
        FeaturePredictionComponent.from(
          DoubleValue.from(1.0, 0.1, Units.SECONDS),
          false,
          FeaturePredictionCorrectionType.BASELINE_PREDICTION),
        FeaturePredictionComponent.from(
          DoubleValue.from(11.0, 0.11, Units.SECONDS),
          false,
          FeaturePredictionCorrectionType.BASELINE_PREDICTION)))
      .setExtrapolated(false)
      .setPredictionType(FeatureMeasurementTypes.SOURCE_TO_RECEIVER_DISTANCE)
      .setSourceLocation(EventLocation.from(1.0, 1.0, 1.0, Instant.now()))
      .setReceiverLocation(Location.from(1.0, 1.0, 1.0, 1.0))
      .setChannelName(UtilsTestFixtures.CHANNEL.getName())
      .build();

  private static final FeaturePrediction<NumericMeasurementValue> FEATURE_PREDICTION_2 =
    FeaturePrediction.<NumericMeasurementValue>builder()
      .setPhase(PhaseType.P)
      .setPredictedValue(NumericMeasurementValue
        .from(Instant.EPOCH, DoubleValue.from(2.0, 0.2, Units.SECONDS)))
      .setFeaturePredictionComponents(Set.of(
        FeaturePredictionComponent.from(
          DoubleValue.from(2.0, 0.2, Units.SECONDS),
          false,
          FeaturePredictionCorrectionType.BASELINE_PREDICTION),
        FeaturePredictionComponent.from(
          DoubleValue.from(22.0, 0.22, Units.SECONDS),
          false,
          FeaturePredictionCorrectionType.BASELINE_PREDICTION)))
      .setExtrapolated(false)
      .setPredictionType(FeatureMeasurementTypes.SOURCE_TO_RECEIVER_DISTANCE)
      .setSourceLocation(EventLocation.from(2.0, 2.0, 2.0, Instant.now()))
      .setReceiverLocation(Location.from(2.0, 2.0, 2.0, 2.0))
      .setChannelName(UtilsTestFixtures.CHANNEL.getName())
      .build();

  private static final FeaturePrediction<InstantValue> FEATURE_PREDICTION_3 =
    FeaturePrediction.<InstantValue>builder()
      .setPhase(PhaseType.P)
      .setPredictedValue(InstantValue.from(Instant.EPOCH, Duration.ZERO))
      .setFeaturePredictionComponents(Set.of(
        FeaturePredictionComponent.from(
          DoubleValue.from(3.0, 0.3, Units.SECONDS),
          false,
          FeaturePredictionCorrectionType.BASELINE_PREDICTION),
        FeaturePredictionComponent.from(
          DoubleValue.from(33.0, 0.33, Units.SECONDS),
          false,
          FeaturePredictionCorrectionType.BASELINE_PREDICTION)))
      .setExtrapolated(false)
      .setPredictionType(FeatureMeasurementTypes.ARRIVAL_TIME)
      .setSourceLocation(EventLocation.from(3.0, 2.0, 2.0, Instant.now()))
      .setReceiverLocation(Location.from(3.0, 3.0, 3.0, 3.0))
      .setChannelName(UtilsTestFixtures.CHANNEL.getName())
      .build();

  private static final FeaturePrediction<NumericMeasurementValue> featurePredictionNoInstantValue =
    FeaturePrediction.<NumericMeasurementValue>builder()
      .setPhase(PhaseType.P)
      .setPredictedValue(Optional.empty())
      .setFeaturePredictionComponents(Set.of())
      .setExtrapolated(false)
      .setPredictionType(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH)
      .setSourceLocation(EventLocation.from(3.0, 2.0, 2.0, Instant.now()))
      .setReceiverLocation(Location.from(3.0, 3.0, 3.0, 3.0))
      .setChannelName(UtilsTestFixtures.CHANNEL.getName())
      .build();

  private static final LocationBehavior LOCATION_BEHAVIOR = LocationBehavior
    .from(RESIDUAL, WEIGHT, DEFINING, FEATURE_PREDICTION_1, ARRIVAL_TIME_MEASUREMENT);

  private static final Set<LocationBehavior> LOCATION_BEHAVIORS = Set.of(LOCATION_BEHAVIOR);

  private static final Set<FeaturePrediction<?>> FEATURE_PREDICTIONS_EMPTY = Set.of();

  // Create a LocationSolution
  private static final LocationSolution LOCATION_SOLUTION = LocationSolution.builder()
    .generateId()
    .setLocation(LOCATION)
    .setLocationRestraint(LOCATION_RESTRAINT)
    .setLocationUncertainty(Optional.of(LOCATION_UNCERTAINTY))
    .setLocationBehaviors(LOCATION_BEHAVIORS)
    .setFeaturePredictions(FEATURE_PREDICTIONS_EMPTY)
    .setNetworkMagnitudeSolutions(NETWORK_MAGNITUDE_SOLUTIONS)
    .build();
  private static final LocationSolution LOCATION_SOLUTION_2 = LocationSolution.builder()
    .generateId()
    .setLocation(LOCATION_2)
    .setLocationRestraint(LOCATION_RESTRAINT)
    .setLocationUncertainty(Optional.of(LOCATION_UNCERTAINTY))
    .setLocationBehaviors(LOCATION_BEHAVIORS)
    .setFeaturePredictions(FEATURE_PREDICTIONS_EMPTY)
    .setNetworkMagnitudeSolutions(emptyList())
    .build();
  private static final LocationSolution LOCATION_SOLUTION_3 = LocationSolution.builder()
    .generateId()
    .setLocation(LOCATION_3)
    .setLocationRestraint(LOCATION_RESTRAINT)
    .setLocationUncertainty(Optional.of(LOCATION_UNCERTAINTY))
    .setLocationBehaviors(LOCATION_BEHAVIORS)
    .setFeaturePredictions(FEATURE_PREDICTIONS_EMPTY)
    .setNetworkMagnitudeSolutions(NETWORK_MAGNITUDE_SOLUTIONS_2)
    .build();
  private static final LocationSolution LOCATION_SOLUTION_4 = LocationSolution.builder()
    .generateId()
    .setLocation(LOCATION_3)
    .setLocationRestraint(LOCATION_RESTRAINT)
    .setLocationUncertainty(Optional.of(LOCATION_UNCERTAINTY))
    .setLocationBehaviors(LOCATION_BEHAVIORS)
    .setFeaturePredictions(FEATURE_PREDICTIONS_EMPTY)
    .setNetworkMagnitudeSolutions(emptyList())
    .build();
  private static final LocationSolution LOCATION_SOLUTION_5 = LocationSolution.builder()
    .generateId()
    .setLocation(LOCATION_3)
    .setLocationRestraint(LOCATION_RESTRAINT)
    .setLocationUncertainty(Optional.of(LOCATION_UNCERTAINTY))
    .setLocationBehaviors(LOCATION_BEHAVIORS)
    .setFeaturePredictions(FEATURE_PREDICTIONS_EMPTY)
    .setNetworkMagnitudeSolutions(emptyList())
    .build();
  private static final LocationSolution LOCATION_SOLUTION_6 = LocationSolution.builder()
    .generateId()
    .setLocation(LOCATION_3)
    .setLocationRestraint(LOCATION_RESTRAINT)
    .setLocationUncertainty(Optional.of(LOCATION_UNCERTAINTY))
    .setLocationBehaviors(LOCATION_BEHAVIORS)
    .setFeaturePredictions(FEATURE_PREDICTIONS_EMPTY)
    .setNetworkMagnitudeSolutions(emptyList())
    .build();
  public static final LocationSolution LOCATION_SOLUTION_7 = LocationSolution.builder()
    .generateId()
    .setLocation(LOCATION_3)
    .setLocationRestraint(LOCATION_RESTRAINT)
    .setLocationUncertainty(Optional.of(LOCATION_UNCERTAINTY))
    .setLocationBehaviors(LOCATION_BEHAVIORS)
    .setFeaturePredictions(FEATURE_PREDICTIONS_EMPTY)
    .setNetworkMagnitudeSolutions(emptyList())
    .build();

  public static final LocationSolution LOCATION_SOLUTION_WITH_MAGNITUDE = LocationSolution.builder()
    .generateId()
    .setLocation(LOCATION_3)
    .setLocationRestraint(LOCATION_RESTRAINT)
    .setLocationUncertainty(Optional.of(LOCATION_UNCERTAINTY))
    .setLocationBehaviors(LOCATION_BEHAVIORS)
    .setFeaturePredictions(FEATURE_PREDICTIONS_EMPTY)
    .setNetworkMagnitudeSolutions(NETWORK_MAGNITUDE_SOLUTIONS)
    .build();

  // Create some Event's
  public static final Event EVENT = Event.create(
    Set.of(UUID.randomUUID()), Set.of(SIGNAL_DETECTION_HYPOTHESIS.getId()),
    Set.of(LOCATION_SOLUTION),
    PreferredLocationSolution.from(LOCATION_SOLUTION),
    "monitoringOrg", UUID.randomUUID());

  public static final Event EVENT_2 = Event.create(
    Set.of(UUID.randomUUID()), Set.of(SIGNAL_DETECTION_HYPOTHESIS.getId()),
    Set.of(LOCATION_SOLUTION_2),
    PreferredLocationSolution.from(LOCATION_SOLUTION_2),
    "monitoringOrg 2", UUID.randomUUID());

  public static final Event EVENT_3 = Event.create(
    Set.of(UUID.randomUUID()), Set.of(SIGNAL_DETECTION_HYPOTHESIS.getId()),
    Set.of(LOCATION_SOLUTION_3),
    PreferredLocationSolution.from(LOCATION_SOLUTION_3),
    "monitoringOrg 3", UUID.randomUUID());

  public static final Event EVENT_4 = Event.create(
    Set.of(UUID.randomUUID()), Set.of(SIGNAL_DETECTION_HYPOTHESIS.getId()),
    Set.of(LOCATION_SOLUTION_4),
    PreferredLocationSolution.from(LOCATION_SOLUTION_4),
    "monitoringOrg 4", UUID.randomUUID());

  public static final Event EVENT_5 = Event.create(
    Set.of(UUID.randomUUID()), Set.of(SIGNAL_DETECTION_HYPOTHESIS.getId()),
    Set.of(LOCATION_SOLUTION_5),
    PreferredLocationSolution.from(LOCATION_SOLUTION_5),
    "monitoringOrg 5", UUID.randomUUID());

  public static final Event EVENT_6 = Event.create(
    Set.of(UUID.randomUUID()), Set.of(SIGNAL_DETECTION_HYPOTHESIS.getId()),
    Set.of(LOCATION_SOLUTION_6),
    PreferredLocationSolution.from(LOCATION_SOLUTION_6),
    "monitoringOrg 6", UUID.randomUUID());

  public static final Event EVENT_WITH_MAGNITUDE = Event.create(
    Set.of(UUID.randomUUID()), Set.of(SIGNAL_DETECTION_HYPOTHESIS.getId()),
    Set.of(LOCATION_SOLUTION_WITH_MAGNITUDE),
    PreferredLocationSolution.from(LOCATION_SOLUTION_WITH_MAGNITUDE),
    "monitoringOrg with magnitude", UUID.randomUUID());

  public static final Event EVENT_6_A = Event.from(
    EVENT_6.getId(),
    Set.of(),
    "monitoringOrg 6oops",
    EVENT_6.getHypotheses(),
    List.of(),
    List.of(PreferredEventHypothesis
      .from(UUID.randomUUID(), EVENT_6.getHypotheses().iterator().next())));

  public static final Event UNSTORED_EVENT = Event.create(
    Set.of(UUID.randomUUID()), Set.of(SIGNAL_DETECTION_HYPOTHESIS.getId()),
    Set.of(LOCATION_SOLUTION_3),
    PreferredLocationSolution.from(LOCATION_SOLUTION_3),
    "monitoringOrg unstored", UUID.randomUUID());

  private static UUID eventId = UUID.randomUUID();
  private static UUID eventNoInstantValueId = UUID.randomUUID();
  private static UUID processingStageId = UUID.randomUUID();
  private static String monitoringOrg = "monitoringOrg 7";
  private static UUID ehId = UUID.randomUUID();
  private static UUID parentEhId = UUID.randomUUID();
  private static UUID parentEhNoInstantValueId = UUID.randomUUID();

  private static final LocationSolution locationSolutionWithFp1 = LocationSolution.builder()
    .generateId()
    .setLocation(LOCATION_3)
    .setLocationRestraint(LOCATION_RESTRAINT)
    .setLocationUncertainty(Optional.of(LOCATION_UNCERTAINTY))
    .setLocationBehaviors(LOCATION_BEHAVIORS)
    .setFeaturePredictions(Set.of(FEATURE_PREDICTION_1))
    .setNetworkMagnitudeSolutions(
      //networkMagnitudeSolutions1
      emptyList()
    )
    .build();

  private static final LocationSolution locationSolutionWithFp1Modified = locationSolutionWithFp1
    .toBuilder()
    .generateId()
    .generateId()
    .setFeaturePredictions(Set.of(FEATURE_PREDICTION_1, FEATURE_PREDICTION_2))
    .build();
  private static final LocationSolution locationSolutionWithFp2 = locationSolutionWithFp1
    .toBuilder()
    .generateId()
    .setFeaturePredictions(Set.of(FEATURE_PREDICTION_2))
    .build();
  private static final LocationSolution locationSolutionWithFp3 = locationSolutionWithFp1
    .toBuilder()
    .generateId()
    .setFeaturePredictions(Set.of(FEATURE_PREDICTION_3))
    .build();
  private static final LocationSolution locationSolutionNoInsantValue = locationSolutionWithFp1
    .toBuilder()
    .generateId()
    .setFeaturePredictions(Set.of(featurePredictionNoInstantValue))
    .build();

  private static EventHypothesis eh1 = EventHypothesis.from(
    ehId, eventId, Set.of(parentEhId), false,
    Set.of(locationSolutionWithFp1),
    PreferredLocationSolution.from(locationSolutionWithFp1),
    Set.of());
  private static EventHypothesis eh1Modified = EventHypothesis.from(
    ehId, eventId, Set.of(parentEhId), false,
    Set.of(locationSolutionWithFp1Modified, locationSolutionWithFp2),
    PreferredLocationSolution.from(locationSolutionWithFp1Modified),
    Set.of());
  private static EventHypothesis eh2 = EventHypothesis.from(
    UUID.randomUUID(), eventId, Set.of(parentEhId), false,
    Set.of(locationSolutionWithFp2),
    PreferredLocationSolution.from(locationSolutionWithFp2),
    Set.of());
  private static EventHypothesis eh3 = EventHypothesis.from(
    UUID.randomUUID(), eventId, Set.of(parentEhId), false,
    Set.of(locationSolutionWithFp3),
    PreferredLocationSolution.from(locationSolutionWithFp3),
    Set.of());

  private static EventHypothesis ehNoInstantValue = EventHypothesis.from(
    UUID.randomUUID(), eventNoInstantValueId, Set.of(parentEhNoInstantValueId), false,
    Set.of(locationSolutionNoInsantValue),
    PreferredLocationSolution.from(locationSolutionNoInsantValue),
    Set.of());

  static final Event eventWithFeaturePredictions = Event.from(
    eventId,
    Set.of(),
    monitoringOrg,
    Set.of(eh1),
    List.of(FinalEventHypothesis.from(eh1)),
    List.of(PreferredEventHypothesis.from(processingStageId, eh1)));
  static final Event eventWithFeaturePredictionsModified = Event.from(
    eventId,
    Set.of(),
    monitoringOrg,
    Set.of(eh1Modified, eh2),
    List.of(FinalEventHypothesis.from(eh1Modified)),
    List.of(PreferredEventHypothesis.from(processingStageId, eh1Modified)));
  static final Event eventWithFeaturePredictions3 = Event.from(
    eventId,
    Set.of(),
    monitoringOrg,
    Set.of(eh3),
    List.of(FinalEventHypothesis.from(eh3)),
    List.of(PreferredEventHypothesis.from(processingStageId, eh3)));

  static final Event eventNoInstantValue = Event.from(
    eventNoInstantValueId,
    Set.of(),
    monitoringOrg,
    Set.of(ehNoInstantValue),
    List.of(FinalEventHypothesis.from(ehNoInstantValue)),
    List.of(PreferredEventHypothesis.from(processingStageId, ehNoInstantValue)));

  public static final Instant changeTime1 = Instant.ofEpochSecond(797731200);
  public static final Instant changeTime2 = Instant.ofEpochSecond(1195430400);
  public static final Instant changeTime3 = Instant.ofEpochSecond(1232496000);

  public static final ReferenceStation JNU_V1 = ReferenceStation.builder()
    .setName("JNU")
    .setDescription("Ohita, Japan")
    .setStationType(StationType.SEISMIC_3_COMPONENT)
    .setSource(infoSource)
    .setComment("")
    .setLatitude(33.1217)
    .setLongitude(130.8783)
    .setElevation(0.54)
    .setActualChangeTime(changeTime1)
    .setSystemChangeTime(changeTime1)
    .setActive(true)
    .setAliases(new ArrayList<>())
    .build();

  public static final ReferenceStation JNU_V2 = ReferenceStation.builder()
    .setName("JNU")
    .setDescription("")
    .setStationType(StationType.SEISMIC_3_COMPONENT)
    .setSource(infoSource)
    .setComment("upgrade for IMS")
    .setLatitude(NA_VALUE)
    .setLongitude(NA_VALUE)
    .setElevation(NA_VALUE)
    .setActualChangeTime(changeTime2)
    .setSystemChangeTime(changeTime2)
    .setActive(true)
    .setAliases(new ArrayList<>())
    .build();

  public static final ReferenceStation JNU_V3 = ReferenceStation.builder()
    .setName("JNU")
    .setDescription("Oita Nakatsue, Japan Meterological Agency Seismic Network")
    .setStationType(StationType.SEISMIC_3_COMPONENT)
    .setSource(infoSource)
    .setComment("")
    .setLatitude(33.121667)
    .setLongitude(130.87833)
    .setElevation(0.573)
    .setActualChangeTime(changeTime3)
    .setSystemChangeTime(changeTime3)
    .setActive(true)
    .setAliases(new ArrayList<>())
    .build();

  public static final List<ReferenceStation> jnuVersions = List.of(TestFixtures.JNU_V1,
    TestFixtures.JNU_V2, TestFixtures.JNU_V3);

  public static final ReferenceNetwork NET_IMS_AUX;
  public static final ReferenceNetwork NET_IDC_DA;
  public static final Set<ReferenceNetworkMembership> NETWORK_MEMBERSHIPS;
  public static final ReferenceSite JNU_SITE_V1 = ReferenceSite.builder()
    .setName("JNU")
    .setDescription("Ohita, Japan")
    .setSource(infoSource)
    .setComment("")
    .setLatitude(33.1217)
    .setLongitude(130.8783)
    .setElevation(0.54)
    .setActualChangeTime(changeTime1)
    .setSystemChangeTime(changeTime1)
    .setActive(true)
    .setPosition(ZERO_POSITION)
    .setAliases(new ArrayList<>())
    .build();

  public static final ReferenceSite JNU_SITE_V2 = ReferenceSite.builder()
    .setName("JNU")
    .setDescription("")
    .setSource(infoSource)
    .setComment("upgrade for IMS")
    .setLatitude(NA_VALUE)
    .setLongitude(NA_VALUE)
    .setElevation(NA_VALUE)
    .setActualChangeTime(changeTime2)
    .setSystemChangeTime(changeTime2)
    .setActive(true)
    .setPosition(ZERO_POSITION)
    .setAliases(new ArrayList<>())
    .build();

  public static final ReferenceSite JNU_SITE_V3 = ReferenceSite.builder()
    .setName("JNU")
    .setDescription("Oita Nakatsue, Japan Meterological Agency Seismic Network")
    .setSource(infoSource)
    .setComment("")
    .setLatitude(33.121667)
    .setLongitude(130.87833)
    .setElevation(0.573)
    .setActualChangeTime(changeTime3)
    .setSystemChangeTime(changeTime3)
    .setActive(true)
    .setPosition(ZERO_POSITION)
    .setAliases(new ArrayList<>())
    .build();

  public static final List<ReferenceSite> JNU_SITE_VERSIONS = List
    .of(JNU_SITE_V1, JNU_SITE_V2, JNU_SITE_V3);

  public static final Set<ReferenceSiteMembership> SITE_MEMBERSHIPS;
  public static final Set<ReferenceStationMembership> STATION_MEMBERSHIPS;

  static {
    // Define networks
    Instant netImxAuxChangeTime = Instant.ofEpochSecond(604713600);
    NET_IMS_AUX = ReferenceNetwork.builder()
      .setName("IMS_AUX")
      .setDescription("All IMS auxiliary seismic stations")
      .setOrganization(NetworkOrganization.CTBTO)
      .setRegion(NetworkRegion.GLOBAL)
      .setSource(infoSource)
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
      .setSource(infoSource)
      .setComment("")
      .setActualChangeTime(idcDaChangeTime)
      .setSystemChangeTime(idcDaChangeTime)
      .setActive(true)
      .build();

    NETWORK_MEMBERSHIPS = associateStationsAndNetworks();
    STATION_MEMBERSHIPS = associateSitesAndStations();
    SITE_MEMBERSHIPS = associateChannelAndSites();
  }

  private static Set<ReferenceNetworkMembership> associateStationsAndNetworks() {
    // declare memberships
    UUID jnuId = JNU_V1.getEntityId();
    var imsMember1 = ReferenceNetworkMembership.from(
      UUID.randomUUID(), "", changeTime1, changeTime1,
      NET_IMS_AUX.getEntityId(), jnuId, StatusType.ACTIVE);
    var imsMember2 = ReferenceNetworkMembership.from(
      UUID.randomUUID(), "", changeTime2, changeTime2,
      NET_IMS_AUX.getEntityId(), jnuId, StatusType.INACTIVE);
    var imsMember3 = ReferenceNetworkMembership.from(
      UUID.randomUUID(), "", changeTime3, changeTime3,
      NET_IMS_AUX.getEntityId(), jnuId, StatusType.ACTIVE);
    var idcMember1 = ReferenceNetworkMembership.from(
      UUID.randomUUID(), "", changeTime1, changeTime1,
      NET_IDC_DA.getEntityId(), jnuId, StatusType.ACTIVE);
    var idcMember2 = ReferenceNetworkMembership.from(
      UUID.randomUUID(), "", changeTime2, changeTime2,
      NET_IDC_DA.getEntityId(), jnuId, StatusType.INACTIVE);
    var idcMember3 = ReferenceNetworkMembership.from(
      UUID.randomUUID(), "", changeTime3, changeTime3,
      NET_IDC_DA.getEntityId(), jnuId, StatusType.ACTIVE);
    // set reference to all memberships
    return Set.of(imsMember1, imsMember2, imsMember3,
      idcMember1, idcMember2, idcMember3);
  }

  private static Set<ReferenceStationMembership> associateSitesAndStations() {
    UUID jnuId = JNU_V1.getEntityId();
    UUID jnuSiteId = JNU_SITE_V1.getEntityId();
    var member1 = ReferenceStationMembership.from(
      UUID.randomUUID(), "", changeTime1, changeTime1,
      jnuId, jnuSiteId, StatusType.ACTIVE);
    var member2 = ReferenceStationMembership.from(
      UUID.randomUUID(), "", changeTime2, changeTime2,
      jnuId, jnuSiteId, StatusType.INACTIVE);
    var member3 = ReferenceStationMembership.from(
      UUID.randomUUID(), "", changeTime3, changeTime3,
      jnuId, jnuSiteId, StatusType.ACTIVE);
    return Set.of(member1, member2, member3);
  }

  private static Set<ReferenceSiteMembership> associateChannelAndSites() {
    UUID jnuSiteId = TestFixtures.JNU_SITE_V1.getEntityId();
    String channelName = TestFixtures.CHAN_JNU_BHE_V1.getName();
    var member1 = ReferenceSiteMembership.from(
      UUID.randomUUID(), "", changeTime1, changeTime1,
      jnuSiteId, channelName, StatusType.ACTIVE);
    var member2 = ReferenceSiteMembership.from(
      UUID.randomUUID(), "", changeTime2, changeTime2,
      jnuSiteId, channelName, StatusType.INACTIVE);
    var member3 = ReferenceSiteMembership.from(
      UUID.randomUUID(), "", changeTime3, changeTime3,
      jnuSiteId, channelName, StatusType.ACTIVE);
    return Set.of(member1, member2, member3);
  }
}
