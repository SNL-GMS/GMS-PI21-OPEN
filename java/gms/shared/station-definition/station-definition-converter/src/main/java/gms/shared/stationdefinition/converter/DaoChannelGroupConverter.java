package gms.shared.stationdefinition.converter;

import com.google.common.base.Preconditions;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelGroup;
import gms.shared.stationdefinition.coi.channel.Location;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.converter.interfaces.ChannelGroupConverter;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteDao;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Component
public class DaoChannelGroupConverter implements ChannelGroupConverter {

  public static final String SITE_DAO_NULL = "SiteDao must not be null";

  public static Location getLocation(SiteDao siteDao, Collection<SiteChanDao> siteChanDaos) {
    OptionalDouble possibleChannelGroupAverageDepth = siteChanDaos
      .stream()
      .mapToDouble(SiteChanDao::getEmplacementDepth)
      .average();

    double channelGroupAverageDepth = possibleChannelGroupAverageDepth.orElse(0);
    var roundedChannelGroupAverageDepth = BigDecimal.valueOf(channelGroupAverageDepth)
      .setScale(10, RoundingMode.HALF_UP)
      .setScale(4, RoundingMode.HALF_UP)
      .doubleValue();

    return Location.from(siteDao.getLatitude(), siteDao.getLongitude(),
      roundedChannelGroupAverageDepth, siteDao.getElevation());
  }

  /**
   * Converts a {@link SiteDao}, and a passed list of {@link SiteChanDao} into a {@link ChannelGroup}.
   * {@link Channel}s in the {@link ChannelGroup} will be version references
   * NOTE: The depth is currently ignored and set to "0" as a hardcoded value.
   *
   * @param siteDao The Site for the ChannelGroup
   * @param siteChanDaos The channels in the channel group, as specified by ChannelConverter
   * @return The {@link ChannelGroup} representing the
   * provided CSS data
   */
  @Override
  public ChannelGroup convert(SiteDao siteDao, Collection<SiteChanDao> siteChanDaos,
    UnaryOperator<Channel> channelFunction,
    Instant effectiveAt, Instant effectiveUntil, Collection<Channel> channels) {

    Objects.requireNonNull(siteDao, SITE_DAO_NULL);
    Objects.requireNonNull(siteChanDaos, "List of SiteChanDaos must not be null");
    Objects.requireNonNull(channelFunction, "Channel Function must not be null");
    Objects.requireNonNull(effectiveAt, "Effective at cannot be null");

    Preconditions.checkState(effectiveAt.isBefore(effectiveUntil), "Effective at must be before effective until");
    Preconditions.checkState(!siteChanDaos.isEmpty(), "SiteChanDaos cannot be empty");

    return getChannelGroup(siteDao, siteChanDaos, channels, effectiveAt, effectiveUntil, channelFunction);
  }

  private ChannelGroup getChannelGroup(SiteDao siteDao, Collection<SiteChanDao> siteChanDaos,
    Collection<Channel> channels, Instant effectiveAt, Instant effectiveUntil, UnaryOperator<Channel> channelFunction) {
    Instant newEndDate = effectiveUntil;
    //this should be done in the jpa converter, but there are side effects of setting it to null, needs to be set to Optional.emtpy()
    if (newEndDate.equals(Instant.MAX)) {
      newEndDate = null;
    }
    Pair<Boolean, Boolean> updatedByResponse = ConverterUtils.getUpdatedByResponse(channels, effectiveAt, effectiveUntil);

    if (channelFunction != null) {
      channels = channels.stream().map(channelFunction::apply).collect(Collectors.toList());
    } else {
      channels = channels.stream()
        .map(channel -> channel.toBuilder().setData(Optional.empty()).build())
        .collect(Collectors.toList());
    }

    final ChannelGroup.Data newGroupData = ChannelGroup.Data.builder()
      .setDescription(siteDao.getStationName())
      .setLocation(getLocation(siteDao, siteChanDaos))
      .setStation(Station.createVersionReference(siteDao.getReferenceStation(), siteDao.getId().getOnDate()))
      .setEffectiveUntil(newEndDate)
      .setType(ChannelGroup.ChannelGroupType.PHYSICAL_SITE)
      .setChannels(channels)
      .setEffectiveAtUpdatedByResponse(Optional.of(updatedByResponse.getLeft()))
      .setEffectiveUntilUpdatedByResponse(Optional.of(updatedByResponse.getRight()))
      .build();

    return ChannelGroup.builder()
      .setName(siteDao.getId().getStationCode())
      .setEffectiveAt(effectiveAt)
      .setData(newGroupData)
      .build();
  }

  /**
   * Converts a {@link SiteDao}, and a passed list of {@link SiteChanDao} into a {@link ChannelGroup}.
   * {@link Channel}s in the {@link ChannelGroup} will be version references
   * NOTE: The depth is currently ignored and set to "0" as a hardcoded value.
   *
   * @param siteDao The Site for the ChannelGroup
   * @param siteChanDaos The channels in the channel group, as specified by ChannelConverter
   * @return The {@link ChannelGroup} representing the
   * provided CSS data
   */
  @Override
  public ChannelGroup convert(SiteDao siteDao, Collection<SiteChanDao> siteChanDaos,
    Instant effectiveAt, Instant effectiveUntil, Collection<Channel> channels) {
    Objects.requireNonNull(siteDao, SITE_DAO_NULL);
    Objects.requireNonNull(siteChanDaos, "List of SiteChanDaos must not be null");
    Objects.requireNonNull(effectiveAt, "Effective at cannot be null");

    Preconditions.checkState(effectiveAt.isBefore(effectiveUntil), "Effective at must be before effective until");
    Preconditions.checkState(!siteChanDaos.isEmpty(), "SiteChanDaos cannot be empty");

    return getChannelGroup(siteDao, siteChanDaos, channels, effectiveAt, effectiveUntil, null);
  }

  /**
   * Converts a Site into a {@link ChannelGroup} version reference.
   * NOTE: The depth is currently ignored and set to "0" as a hardcoded value.
   *
   * @param siteDao The Site for the ChannelGroup
   * @return The {@link ChannelGroup} representing the provided CSS data as a version reference
   */
  @Override
  public ChannelGroup convertToVersionReference(SiteDao siteDao) {

    Objects.requireNonNull(siteDao, SITE_DAO_NULL);

    return ChannelGroup
      .createVersionReference(siteDao.getId().getStationCode(), siteDao.getId().getOnDate());
  }
}
