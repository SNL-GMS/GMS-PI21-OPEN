package gms.shared.stationdefinition.converter.util;

import com.google.common.base.Functions;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteChanKey;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SiteChanMerger {

  private SiteChanMerger() {
  }

  /*
   Merges siteChans that have the same start and end time.
    */
  private static UnaryOperator<List<SiteChanDao>> doMerge = siteChanDaos -> {
    Set<SiteChanDao> mergedSiteChanDaos = new LinkedHashSet<>();

    if (siteChanDaos.size() < 2) {
      return siteChanDaos;
    }
    var merged = false;
    var mergedLastSiteChanDao = false;
    SiteChanDao curSiteChanDao = siteChanDaos.get(0);
    //merge version times
    for (var i = 0; i < siteChanDaos.size() - 1; i++) {
      if (!merged) {
        curSiteChanDao = siteChanDaos.get(i);
      }
      var nextSiteChanDao = siteChanDaos.get(i + 1);

      if (curSiteChanDao.getOffDate().equals(nextSiteChanDao.getId().getOnDate())) {
        mergedSiteChanDaos.remove(curSiteChanDao);
        curSiteChanDao.setOffDate(nextSiteChanDao.getOffDate());
        mergedSiteChanDaos.add(curSiteChanDao);
        merged = true;
        //we are merging in the last SiteChanDao, we don't need to account for it after the loop is done
        if (i == siteChanDaos.size() - 2) {
          mergedLastSiteChanDao = true;
        }
      } else if (!merged) {
        mergedSiteChanDaos.add(curSiteChanDao);
      } else {
        merged = false;
      }
    }
    //we did not merge the last SiteChanDao...add it to the list
    if (!mergedLastSiteChanDao) {
      mergedSiteChanDaos.add(siteChanDaos.get(siteChanDaos.size() - 1));
    }
    return new ArrayList<>(mergedSiteChanDaos);
  };

  public static List<SiteChanDao> mergeSiteChans(
    Map<String, List<SiteChanDao>> siteChansForVersionByChannel) {
    //perform merge on each list in siteChansForVersionByChannel
    //combine them into a list at the end
    //create new versions for each time from the current station or from the raw data
    //need to update DaoStationConverter to accept start/end Time
    return siteChansForVersionByChannel.values().stream()
      .flatMap(siteChanDaos -> doMerge.apply(siteChanDaos).stream())
      .collect(Collectors.toList());
  }

  //gets possibleVersionTimes for Channels
  public static List<Instant> getPossibleVersionTimes(List<SiteChanDao> mergedSiteChanDaos){
    if(mergedSiteChanDaos == null){
      return List.of();
    }

    return Stream.concat(
        mergedSiteChanDaos.stream().map(Functions.compose(SiteChanKey::getOnDate, SiteChanDao::getId)),
        mergedSiteChanDaos.stream().map(SiteChanDao::getOffDate))
      .distinct()
      .sorted()
      .collect(Collectors.toList());
  }
}
