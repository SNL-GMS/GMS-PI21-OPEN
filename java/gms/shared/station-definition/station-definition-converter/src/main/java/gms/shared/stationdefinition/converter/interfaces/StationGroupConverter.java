package gms.shared.stationdefinition.converter.interfaces;

import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.coi.station.StationGroup;
import gms.shared.stationdefinition.converter.DaoStationConverter;
import gms.shared.stationdefinition.dao.css.AffiliationDao;
import gms.shared.stationdefinition.dao.css.NetworkDao;
import gms.shared.stationdefinition.dao.css.SiteDao;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface StationGroupConverter {

  /**
   * Converts networkKey map, networkDescription, and List of Stations into COI {@link StationGroup} objects
   *
   * @param networkKey - pair containing network name and effective at time
   * @param networkDescription - map of network names to network description
   * @param stationList - list of {@link Station} for {@link StationGroup.Data}
   * @return coi StationGroup object
   */
  StationGroup convert(Pair<String, Instant> networkKey, String networkDescription, List<Station> stationList);


  /**
   * Converts {@link NetworkDao} and list of {@link SiteDao}s to a populated
   * {@link StationGroup} with list of {@link Station} version references
   *
   * @param network - {@link NetworkDao}
   * @param affiliations - list of {@link AffiliationDao}
   * @param sites - list of {@link SiteDao}
   * @param stationFunction - Function that applies {@link DaoStationConverter}
   * @return {@link StationGroup}
   */
  StationGroup convert(Instant versionTime, Optional<Instant> effectiveUntil, NetworkDao network,
    List<Station> stations);

}
