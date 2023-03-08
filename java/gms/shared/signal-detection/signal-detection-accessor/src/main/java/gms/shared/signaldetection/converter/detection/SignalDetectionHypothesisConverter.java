package gms.shared.signaldetection.converter.detection;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import gms.shared.signaldetection.coi.detection.FeatureMeasurement;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesis;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesisConverterId;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesisId;
import gms.shared.signaldetection.coi.types.FeatureMeasurementType;
import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.signaldetection.dao.css.AmplitudeDao;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.signaldetection.dao.css.AssocDao;
import gms.shared.signaldetection.dao.css.enums.AmplitudeType;
import gms.shared.signaldetection.repository.utils.SignalDetectionIdUtility;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.waveform.coi.ChannelSegment;
import gms.shared.waveform.coi.Timeseries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class SignalDetectionHypothesisConverter implements SignalDetectionHypothesisConverterInterface {

  private final FeatureMeasurementConverterInterface featureMeasurementConverter;
  private final SignalDetectionIdUtility signalDetectionIdUtility;

  /**
   * Create {@link SignalDetectionHypothesisConverter} instance using {@link FeatureMeasurementConverterInterface}
   * to create the set of {@link FeatureMeasurement}s needed to create a {@link SignalDetectionHypothesis}
   *
   * @param featureMeasurementConverter {@link FeatureMeasurementConverterInterface} instance
   * @return {@link SignalDetectionHypothesisConverter}
   */
  @Autowired
  public SignalDetectionHypothesisConverter(
    FeatureMeasurementConverterInterface featureMeasurementConverter,
    SignalDetectionIdUtility signalDetectionIdUtility) {
    this.featureMeasurementConverter = featureMeasurementConverter;
    this.signalDetectionIdUtility = signalDetectionIdUtility;
  }

  @Override
  public Optional<SignalDetectionHypothesis> convert(SignalDetectionHypothesisConverterId converterId,
    ArrivalDao arrivalDao,
    Optional<AssocDao> assocDao,
    Collection<AmplitudeDao> amplitudeDaos,
    String monitoringOrganization,
    Station station,
    Channel channel,
    ChannelSegment<? extends Timeseries> channelSegment) {

    Objects.requireNonNull(converterId);
    Objects.requireNonNull(arrivalDao);
    Objects.requireNonNull(assocDao);
    Objects.requireNonNull(amplitudeDaos);
    Objects.requireNonNull(station);
    Objects.requireNonNull(channel);
    Objects.requireNonNull(channelSegment);

    // check that the channels are all from the same station
    if (channel.isPresent()) {
      Preconditions.checkState(channel.getStation().getName().equals(station.getName()),
        "Channel must be from the provided station");
    }

    if (assocDao.isPresent()) {
      Preconditions.checkState(assocDao.get().getId().getArrivalId() == arrivalDao.getId(),
        "Assoc and Arrival arid must be equal to create signal detection hypothesis.");
    }

    Preconditions.checkState(channelSegment.getId().getChannel().getName().equals(channel.getName()),
      "Channel segment must be from provided channel");

    // create the SignalDetectionHypothesisId object
    UUID hypothesisId;
    if (assocDao.isPresent()) {
      var assocDaoVal = assocDao.get();
      hypothesisId = signalDetectionIdUtility.getOrCreateSignalDetectionHypothesisIdFromAridOridAndStageId(
        assocDaoVal.getId().getArrivalId(), assocDaoVal.getId().getOriginId(), converterId.getLegacyDatabaseAccountId());
    } else {
      hypothesisId = signalDetectionIdUtility.getOrCreateSignalDetectionHypothesisIdFromAridAndStageId(arrivalDao.getId(),
        converterId.getLegacyDatabaseAccountId());
    }

    List<FeatureMeasurement<?>> featureMeasurements = createFeatureMeasurements(arrivalDao, assocDao, amplitudeDaos,
      channel, channelSegment);

    Optional<SignalDetectionHypothesis.Data> signalDetectionHypothesisData;

    signalDetectionHypothesisData = Optional.of(
      SignalDetectionHypothesis.Data.builder()
        .setMonitoringOrganization(monitoringOrganization)
        .setStation(station)
        .setRejected(false)
        .setParentSignalDetectionHypothesis(
          converterId.getParentId()
            .map(parentId -> SignalDetectionHypothesis.createEntityReference(converterId.getDetectionId(), parentId)))
        .setFeatureMeasurements(ImmutableSet.copyOf(featureMeasurements))
        .build());

    return Optional.ofNullable(SignalDetectionHypothesis.from(
      SignalDetectionHypothesisId.from(converterId.getDetectionId(), hypothesisId),
      signalDetectionHypothesisData));
  }

  @Override
  public Optional<SignalDetectionHypothesis> convertToEntityReference(String legacyDatabaseAccountId, UUID detectionId,
    ArrivalDao arrivalDao, Optional<AssocDao> assocDao) {

    Objects.requireNonNull(legacyDatabaseAccountId);
    Objects.requireNonNull(detectionId);
    Objects.requireNonNull(arrivalDao);
    Objects.requireNonNull(assocDao);

    if (assocDao.isPresent()) {
      Preconditions.checkState(assocDao.get().getId().getArrivalId() == arrivalDao.getId(),
        "Assoc and Arrival arid must be equal to create signal detection hypothesis.");
    }

    UUID hypothesisId;

    // create the SignalDetectionHypothesisId object
    if (assocDao.isPresent()) {
      var assocDaoVal = assocDao.get();
      hypothesisId = signalDetectionIdUtility.getOrCreateSignalDetectionHypothesisIdFromAridOridAndStageId(
        assocDaoVal.getId().getArrivalId(), assocDaoVal.getId().getOriginId(), legacyDatabaseAccountId);
    } else {
      hypothesisId = signalDetectionIdUtility.getOrCreateSignalDetectionHypothesisIdFromAridAndStageId(arrivalDao.getId(),
        legacyDatabaseAccountId);
    }

    return Optional.ofNullable(SignalDetectionHypothesis.createEntityReference(detectionId, hypothesisId));
  }

  /**
   * Create list of {@link FeatureMeasurement}s using the {@link ArrivalDao}, {@link Channel},
   * the {@link ChannelSegment}, and finally the {@link FeatureMeasurementTypes}
   *
   * @param arrivalDao input {@link ArrivalDao}
   * @param channel {@link Channel} for the channel segment
   * @param channelSegment {@link ChannelSegment} on which the feature measurements were made
   * @return list of {@link FeatureMeasurement}s
   */
  private List<FeatureMeasurement<?>> createFeatureMeasurements(ArrivalDao arrivalDao,
    Optional<AssocDao> assocDao,
    Collection<AmplitudeDao> amplitudeDaos,
    Channel channel,
    ChannelSegment<? extends Timeseries> channelSegment) {

    // List of FM enums that we need for building the FMs
    List<FeatureMeasurementType<?>> fmTypes = List.of(
      FeatureMeasurementTypes.ARRIVAL_TIME,
      FeatureMeasurementTypes.PHASE,
      FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH,
      FeatureMeasurementTypes.SLOWNESS,
      FeatureMeasurementTypes.EMERGENCE_ANGLE,
      FeatureMeasurementTypes.RECTILINEARITY,
      FeatureMeasurementTypes.SHORT_PERIOD_FIRST_MOTION,
      FeatureMeasurementTypes.LONG_PERIOD_FIRST_MOTION,
      FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2
    );

    Optional<AmplitudeDao> amplitudeDaoOptional = amplitudeDaos.stream()
      .filter(amplitudeDao -> amplitudeDao.getAmplitudeType().equals(AmplitudeType.AMPLITUDE_A5_OVER_2.getName()))
      .findFirst();

    // create feature measurements from the channels and channel segments
    return fmTypes.stream()
      .flatMap(fmType -> featureMeasurementConverter.createMeasurementValueSpec(fmType, arrivalDao,
        assocDao, amplitudeDaoOptional))
      .map(spec -> {
        var snr = Optional
          .of(DoubleValue
            .from(arrivalDao.getSnr(), Optional.empty(), Units.DECIBELS));
        return featureMeasurementConverter.convert(spec, channel, channelSegment,
          spec.getFeatureMeasurementType().equals(FeatureMeasurementTypes.ARRIVAL_TIME)
            ? snr : Optional.empty());
      })
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toList());
  }
}
