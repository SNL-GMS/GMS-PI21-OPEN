package gms.shared.stationdefinition.converter;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelNameUtilities;
import gms.shared.stationdefinition.coi.channel.ChannelProcessingMetadataType;
import gms.shared.stationdefinition.coi.channel.ChannelTypes;
import gms.shared.stationdefinition.coi.channel.ChannelTypesParser;
import gms.shared.stationdefinition.coi.channel.Location;
import gms.shared.stationdefinition.coi.channel.Orientation;
import gms.shared.stationdefinition.coi.channel.Response;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.stationdefinition.converter.interfaces.ChannelConverter;
import gms.shared.stationdefinition.converter.interfaces.ResponseConverterTransform;
import gms.shared.stationdefinition.dao.css.BeamDao;
import gms.shared.stationdefinition.dao.css.InstrumentDao;
import gms.shared.stationdefinition.dao.css.SensorDao;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteDao;
import gms.shared.stationdefinition.dao.css.WfdiscDao;
import gms.shared.utilities.bridge.database.converter.InstantToDoubleConverterPositiveNa;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static gms.shared.stationdefinition.coi.channel.ChannelProcessingMetadataType.BEAM_COHERENT;
import static gms.shared.stationdefinition.coi.channel.ChannelProcessingMetadataType.CHANNEL_GROUP;
import static gms.shared.stationdefinition.coi.channel.ChannelProcessingMetadataType.STEERING_AZIMUTH;
import static gms.shared.stationdefinition.coi.channel.ChannelProcessingMetadataType.STEERING_SLOWNESS;

@Component
public class DaoChannelConverter implements ChannelConverter {

  public static final String SITE_CHAN_MUST_NOT_BE_NULL = "Site chan must not be null.";
  public static final String SITE_MUST_NOT_BE_NULL = "Site must not be null.";
  public static final String COULD_NOT_PARSE_CHANNEL_TYPES_FOR_SITE_CHAN_DAO = "Could not parse ChannelTypes for site"
    + " chan dao";
  private final DaoCalibrationConverter calibrationConverter;
  private final FileFrequencyAmplitudePhaseConverter fapConverter;

  @Autowired
  public DaoChannelConverter(
    DaoCalibrationConverter calibrationConverter,
    FileFrequencyAmplitudePhaseConverter fapConverter) {
    this.calibrationConverter = calibrationConverter;
    this.fapConverter = fapConverter;
  }

  @Override
  public Channel convert(SiteChanDao siteChanDao,
    SiteDao siteDao,
    SensorDao sensorDao,
    InstrumentDao instrumentDao,
    WfdiscDao wfdiscDao,
    Range<Instant> versionRange,
    ResponseConverterTransform responseConverterTransform) {

    Objects.requireNonNull(responseConverterTransform);
    var response = getResponse(sensorDao, wfdiscDao, instrumentDao, responseConverterTransform);
    return this.convert(siteChanDao, siteDao, sensorDao, instrumentDao,
      wfdiscDao, versionRange, Optional.ofNullable(response));
  }

  @Override
  public Channel convert(SiteChanDao siteChanDao,
    SiteDao siteDao,
    SensorDao sensorDao,
    InstrumentDao instrumentDao,
    WfdiscDao wfdiscDao,
    Range<Instant> versionRange,
    Optional<Response> response) {
    Objects.requireNonNull(siteChanDao, SITE_CHAN_MUST_NOT_BE_NULL);
    Objects.requireNonNull(siteDao, SITE_MUST_NOT_BE_NULL);
    Objects.requireNonNull(versionRange);
    Preconditions
      .checkState(instrumentDao != null || wfdiscDao != null, "Either instrument or wfdisc must be non-null");

    Optional<ChannelTypes> channelTypesOptional = ChannelTypesParser
      .parseChannelTypes(siteChanDao.getId().getChannelCode());

    String stationName = siteDao.getReferenceStation();
    String siteName = siteChanDao.getId().getStationCode();

    Preconditions.checkState(channelTypesOptional.isPresent(),
      COULD_NOT_PARSE_CHANNEL_TYPES_FOR_SITE_CHAN_DAO);

    var channelTypes = channelTypesOptional.get();

    //could make instrumentDao optional
    double sampleRate = instrumentDao != null ? instrumentDao.getSampleRate() : wfdiscDao.getSampRate();

    var location = Location.from(siteDao.getLatitude(), siteDao.getLongitude(),
      siteChanDao.getEmplacementDepth(), siteDao.getElevation());

    var orientation = Orientation.from(siteChanDao.getHorizontalAngle(),
      siteChanDao.getVerticalAngle());

    String name = ChannelNameUtilities.createShortName(siteDao.getReferenceStation(),
      siteDao.getId().getStationCode(),
      siteChanDao.getId().getChannelCode());

    Instant effectiveUntil = versionRange.upperEndpoint();
    if (effectiveUntil.equals(InstantToDoubleConverterPositiveNa.NA_TIME)) {
      effectiveUntil = null;
    }
    EnumMap<ChannelProcessingMetadataType, Object> channelProcessingMetadataMap = new EnumMap<>(ChannelProcessingMetadataType.class);
    channelProcessingMetadataMap.put(ChannelProcessingMetadataType.CHANNEL_GROUP, siteName);
    HashMap<String, Object> processingDefinitionMap = new HashMap<>();
    var channelData = Channel.Data.builder()
      .setCanonicalName(name)
      .setEffectiveUntil(effectiveUntil)
      .setDescription(siteChanDao.getChannelDescription())
      .setStation(Station.createVersionReference(stationName, siteDao.getId().getOnDate()))
      .setChannelDataType(channelTypes.getDataType())
      .setChannelBandType(channelTypes.getBandType())
      .setChannelInstrumentType(channelTypes.getInstrumentType())
      .setChannelOrientationType(channelTypes.getOrientationType())
      .setChannelOrientationCode(channelTypes.getOrientationCode())
      .setUnits(Units.determineUnits(channelTypes.getDataType()))
      .setNominalSampleRateHz(sampleRate)
      .setLocation(location)
      .setOrientationAngles(orientation)
      .setConfiguredInputs(List.of())
      .setProcessingDefinition(processingDefinitionMap)
      .setProcessingMetadata(channelProcessingMetadataMap)
      .setResponse(response)
      .build();

    Channel unnamed = Channel.builder().setName("[PLACEHOLDER]")
      .setEffectiveAt(versionRange.lowerEndpoint())
      .setData(channelData)
      .build();


    return unnamed.toBuilder().setName(name)
      .setData(channelData.toBuilder()
        .setCanonicalName(name)
        .build())
      .build();
  }

  private Optional<Channel> buildChannelData(Map<ChannelProcessingMetadataType, Object> processingMetadataMap,
    HashMap<String, Object> processingDefinitionMap,
    SiteChanDao siteChanDao,
    SiteDao siteDao,
    Optional<Response> response,
    String description,
    double sampleRate) {

    Optional<ChannelTypes> channelTypesOptional = ChannelTypesParser
      .parseChannelTypes(siteChanDao.getId().getChannelCode());

    String stationName = siteDao.getReferenceStation();

    Preconditions.checkState(channelTypesOptional.isPresent(),
      COULD_NOT_PARSE_CHANNEL_TYPES_FOR_SITE_CHAN_DAO);

    var channelTypes = channelTypesOptional.get();

    var location = Location.from(siteDao.getLatitude(), siteDao.getLongitude(),
      siteChanDao.getEmplacementDepth(), siteDao.getElevation());

    var orientation = Orientation.from(siteChanDao.getHorizontalAngle(),
      siteChanDao.getVerticalAngle());

    String name = ChannelNameUtilities.createShortName(siteDao.getReferenceStation(),
      siteDao.getId().getStationCode(),
      siteChanDao.getId().getChannelCode());
    Instant effectiveUntil = siteChanDao.getOffDate();
    if (effectiveUntil.equals(InstantToDoubleConverterPositiveNa.NA_TIME)) {
      effectiveUntil = null;
    }
    var channelData = Channel.Data.builder()
      .setCanonicalName(name)
      .setEffectiveUntil(effectiveUntil)
      .setDescription(description)
      .setStation(Station.createVersionReference(stationName, siteDao.getId().getOnDate()))
      .setChannelDataType(channelTypes.getDataType())
      .setChannelBandType(channelTypes.getBandType())
      .setChannelInstrumentType(channelTypes.getInstrumentType())
      .setChannelOrientationType(channelTypes.getOrientationType())
      .setChannelOrientationCode(channelTypes.getOrientationCode())
      .setUnits(Units.determineUnits(channelTypes.getDataType()))
      .setNominalSampleRateHz(sampleRate)
      .setLocation(location)
      .setOrientationAngles(orientation)
      .setConfiguredInputs(List.of())
      .setProcessingDefinition(processingDefinitionMap)
      .setProcessingMetadata(processingMetadataMap)
      .setResponse(response)
      .build();

    Channel unnamed = Channel.builder().setName("[PLACEHOLDER]")
      .setEffectiveAt(siteChanDao.getId().getOnDate())
      .setData(channelData)
      .build();

    return Optional.of(unnamed.toBuilder().setName(name)
      .setData(channelData.toBuilder()
        .setCanonicalName(name)
        .build())
      .build());
  }

  private Channel convertToDerived(Map<ChannelProcessingMetadataType, Object> processingMetadataMap,
    HashMap<String, Object> processingDefinitionMap,
    SiteChanDao siteChanDao,
    SiteDao siteDao,
    Pair<Instant, Instant> channelEffectiveTimes,
    String description,
    WfdiscDao wfdiscDao) {

    return buildChannelData(processingMetadataMap,
      processingDefinitionMap,
      siteChanDao,
      siteDao,
      Optional.empty(),
      description,
      wfdiscDao.getSampRate())
      .map(channel -> {
        var channelData = channel.getData().orElseThrow();
        return channel.toBuilder()
          .setEffectiveAt(channelEffectiveTimes.getLeft())
          .setData(channelData.toBuilder()
            .setEffectiveUntil(channelEffectiveTimes.getRight())
            .build())
          .build();
      }).orElseThrow();
  }

  @Override
  public Channel convertToBeamDerived(SiteDao siteDao,
    SiteChanDao siteChanDao,
    WfdiscDao wfdiscDao,
    Instant channelEffectiveTime,
    Instant channelEndTime,
    Optional<BeamDao> beamDao,
    Map<ChannelProcessingMetadataType, Object> processingMetadataMap) {

    Objects.requireNonNull(siteChanDao, SITE_CHAN_MUST_NOT_BE_NULL);
    Objects.requireNonNull(siteDao, SITE_MUST_NOT_BE_NULL);
    Objects.requireNonNull(channelEffectiveTime, "Channel effective time must not be null.");
    Objects.requireNonNull(channelEndTime, "Channel effective until time must not be null.");
    Objects.requireNonNull(wfdiscDao, "Wfdisc cannot be null");
    Objects.requireNonNull(beamDao, "Beam cannot be null");

    Preconditions.checkState(channelEffectiveTime.isBefore(channelEndTime),
      "Channel effective time must be before channel end time.");

    HashMap<String, Object> processingDefinitionMap = new HashMap<>();

    beamDao.ifPresent(beam -> {
      processingMetadataMap.put(BEAM_COHERENT, siteChanDao.getChannelType());
      processingMetadataMap.put(STEERING_AZIMUTH, beam.getAzimuth());
      processingMetadataMap.put(STEERING_SLOWNESS, beam.getSlowness());
      processingMetadataMap.put(CHANNEL_GROUP, "beam");
      processingDefinitionMap.put(BEAM_COHERENT.name(), siteChanDao.getChannelType());
      processingDefinitionMap.put(STEERING_AZIMUTH.name(), beam.getAzimuth());
      processingDefinitionMap.put(STEERING_SLOWNESS.name(), beam.getSlowness());
    });

    var channel = convertToDerived(processingMetadataMap,
      processingDefinitionMap,
      siteChanDao,
      siteDao,
      Pair.of(channelEffectiveTime, channelEndTime),
      beamDao.map(BeamDao::getDescription).orElseGet(siteChanDao::getChannelDescription),
      wfdiscDao);

    String name = ChannelNameUtilities.createName(channel);
    var data = channel.getData().orElseThrow();
    return channel.toBuilder()
      .setName(name)
      .setData(data.toBuilder()
        .setCanonicalName(name)
        .build())
      .build();
  }

  /**
   * Converts the SiteChan and Site objects to a {@link Channel} coi version reference
   *
   * @param siteDao the corresponding site of the channel
   * @param siteChanDao the SiteChan that represents the channels characteristics
   * @return Channel coi version reference object
   */
  @Override
  public Channel convertToVersionReference(SiteDao siteDao, SiteChanDao siteChanDao) {

    Objects.requireNonNull(siteChanDao, SITE_CHAN_MUST_NOT_BE_NULL);
    Objects.requireNonNull(siteDao, SITE_MUST_NOT_BE_NULL);

    final String channelName = ChannelNameUtilities.createShortName(siteDao.getReferenceStation(),
      siteDao.getId().getStationCode(),
      siteChanDao.getId().getChannelCode());
    return Channel.createVersionReference(channelName, siteChanDao.getId().getOnDate());
  }

  @Override
  public Channel convertToEntityReference(SiteDao siteDao, SiteChanDao siteChanDao) {
    return Channel.createEntityReference(ChannelNameUtilities.createShortName(siteDao.getReferenceStation(),
      siteDao.getId().getStationCode(),
      siteChanDao.getId().getChannelCode()));
  }

  private Response getResponse(SensorDao sensorDao, WfdiscDao wfdiscDao,
    InstrumentDao instrumentDao, ResponseConverterTransform responseConverterTransform) {
    if (instrumentDao == null || wfdiscDao == null || sensorDao == null) {
      return null;
    } else {
      var calibration = calibrationConverter.convert(wfdiscDao, sensorDao);
      var frequencyAmplitudePhase = fapConverter.convertToEntityReference(
        //File path is used here solely for unique UUID generation (and never accessed)
        instrumentDao.getDirectory() + instrumentDao.getDataFile());
      return responseConverterTransform.getResponse(wfdiscDao, sensorDao, calibration, frequencyAmplitudePhase);
    }
  }
}
