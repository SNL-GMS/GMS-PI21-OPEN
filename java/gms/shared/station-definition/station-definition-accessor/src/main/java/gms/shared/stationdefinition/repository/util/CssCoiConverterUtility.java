package gms.shared.stationdefinition.repository.util;

import gms.shared.stationdefinition.dao.css.SiteChanKey;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CSS to COI Converter utility for building COIs from DAOs
 */
public class CssCoiConverterUtility {

  private CssCoiConverterUtility() {
  }

  /**
   * Get list of {@link SiteChanKey} from channelNames
   *
   * @param channelNames - list of channel names
   * @return list of site chan keys
   */
  public static List<SiteChanKey> getSiteChanKeysFromChannelNames(Collection<String> channelNames) {
    return channelNames.stream()
      .distinct()
      .map(StationDefinitionIdUtility::getCssKeyFromName)
      .collect(Collectors.toList());
  }

  /**
   * Get station codes from {@link SiteChanKey}s
   *
   * @param siteChanKeys - list of site chan keys
   * @return set of station codes
   */
  public static Set<String> getStationCodesFromSiteChanKeys(List<SiteChanKey> siteChanKeys) {
    return siteChanKeys.stream()
      .map(SiteChanKey::getStationCode)
      .collect(Collectors.toSet());
  }
}
