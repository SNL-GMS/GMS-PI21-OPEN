package gms.shared.stationdefinition.converter.interfaces;

import com.google.common.collect.Range;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelProcessingMetadataType;
import gms.shared.stationdefinition.coi.channel.Response;
import gms.shared.stationdefinition.dao.css.BeamDao;
import gms.shared.stationdefinition.dao.css.InstrumentDao;
import gms.shared.stationdefinition.dao.css.SensorDao;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteDao;
import gms.shared.stationdefinition.dao.css.WfdiscDao;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public interface ChannelConverter {

  /**
   * Converts the SiteChan, Sensor, and Instrument objects, combined with a station name, to a {@link Channel}
   *
   * @param siteChanDao the SiteChan that represents the channels characteristics
   * @param siteDao the corresponding site of the channel
   * @param sensor the Sensor for the channel
   * @param instrument the instrument for the channel
   * @param wfdiscDao the response for the channel
   * @param responseConverterTransform creates response for channel
   * @return {@link Channel}
   */
  Channel convert(SiteChanDao siteChanDao,
    SiteDao siteDao,
    SensorDao sensor,
    InstrumentDao instrument,
    WfdiscDao wfdiscDao,
    Range<Instant> versionRange,
    ResponseConverterTransform responseConverterTransform);

  /**
   * Converts the SiteChan, Sensor, and Instrument objects, combined with a station name, to a {@link Channel}
   *
   * @param siteChanDao the SiteChan that represents the channels characteristics
   * @param siteDao the corresponding site of the channel
   * @param sensor the Sensor for the channel
   * @param instrument the instrument for the channel
   * @param wfdiscDao the response for the channel
   * @param response contains channel response
   * @return {@link Channel}
   */
  Channel convert(SiteChanDao siteChanDao,
    SiteDao siteDao,
    SensorDao sensor,
    InstrumentDao instrument,
    WfdiscDao wfdiscDao,
    Range<Instant> versionRange,
    Optional<Response> response);

  /**
   * Converts the provided {@link SiteDao} and {@link SiteChanDao} into a version reference (name and effective time)
   *
   * @param siteDao The site dao providing the station portion of the name
   * @param siteChanDao The site chan for defining the remaining parts of the name
   * @return a {@link Channel} entity reference
   */
  Channel convertToVersionReference(SiteDao siteDao, SiteChanDao siteChanDao);

  /**
   * Converts the provided {@link SiteDao} and {@link SiteChanDao} into an entity reference (name only) {@link Channel}.
   * {@link Channel}
   *
   * @param siteDao The site dao providing the station portion of the name
   * @param siteChanDao The site chan for defining the remaining parts of the name
   * @return a {@link Channel} version reference
   */
  Channel convertToEntityReference(SiteDao siteDao, SiteChanDao siteChanDao);

  /**
   * Augments a generic derived {@link Channel} with beam information
   *
   * @param siteDao The {@link SiteDao} corresponding to the site chan
   * @param siteChanDao The corresponding site chan record
   * @param wfdiscDao the {@link WfdiscDao} for the channel
   * @param channelEffectiveTime the effective time for the channel
   * @param beamDao A {@link BeamDao} that were found by the initially provided {@link WfdiscDao} ID
   * @return an augmented derived {@link Channel}
   */
  Channel convertToBeamDerived(SiteDao siteDao,
    SiteChanDao siteChanDao,
    WfdiscDao wfdiscDao,
    Instant channelEffectiveTime,
    Instant channelEndTime,
    Optional<BeamDao> beamDao,
    Map<ChannelProcessingMetadataType, Object> processingMetadataMap);
}
