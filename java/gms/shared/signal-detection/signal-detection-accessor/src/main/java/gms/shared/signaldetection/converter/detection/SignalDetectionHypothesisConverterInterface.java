package gms.shared.signaldetection.converter.detection;

import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesis;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesisConverterId;
import gms.shared.signaldetection.dao.css.AmplitudeDao;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.signaldetection.dao.css.AssocDao;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.waveform.coi.ChannelSegment;
import gms.shared.waveform.coi.Timeseries;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface SignalDetectionHypothesisConverterInterface {

  /**
   * Convert method definition for converting legacy DB to COI objects for SignalDetection.
   *
   * @param converterId the identifier combining stage, detection and hypothesis ids
   * @param arrivalDao the {@link ArrivalDao} containing the data for the {@link SignalDetectionHypothesis}
   * @param assocDao the optional {@link AssocDao} containing the data for the {@link SignalDetectionHypothesis}
   * @param amplitudeDao the optional {@link AmplitudeDao} containing the data for the {@link SignalDetectionHypothesis}
   * @param monitoringOrganization the monitoring organization from which the  {@link SignalDetectionHypothesis} was measured
   * @param station the {@link Station} on which the {@link SignalDetectionHypothesis} was measured
   * @param channel the {@link Channel} on which the {@link SignalDetectionHypothesis} was measured
   * @param channelSegment the {@link ChannelSegment} providing the data used to measure the
   * {@link SignalDetectionHypothesis}
   * @return a SignalDetectionHypothesis object
   */
  Optional<SignalDetectionHypothesis> convert(SignalDetectionHypothesisConverterId converterId,
    ArrivalDao arrivalDao,
    Optional<AssocDao> assocDao,
    Collection<AmplitudeDao> amplitudeDao,
    String monitoringOrganization,
    Station station,
    Channel channel,
    ChannelSegment<? extends Timeseries> channelSegment);

  /**
   * Convert the provided detection id and {@link ArrivalDao}
   *
   * @param legacyDatabaseAccountId the string legacy database account id
   * @param detectionId the {@link UUID} of the signal detection containing the {@link SignalDetectionHypothesis}
   * to create
   * @param arrivalDao the {@link ArrivalDao} containing the data for the {@link SignalDetectionHypothesis)}
   * @param assocDao the {@link Optional<AssocDao>} that contains data
   * @return an entity reference {@link SignalDetectionHypothesis}
   */
  Optional<SignalDetectionHypothesis> convertToEntityReference(String legacyDatabaseAccountId, UUID detectionId,
    ArrivalDao arrivalDao, Optional<AssocDao> assocDao);
}
