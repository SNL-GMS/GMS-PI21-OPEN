package gms.testtools.simulators.bridgeddatasourcesimulator.api;


import gms.testtools.simulators.bridgeddatasourcesimulator.api.coi.Site;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.coi.SiteChan;

import java.util.Collection;

public interface BridgedDataSourceSimulatorAugmentation {

  /**
   * Stores new versions of channels.
   *
   * @param channels - A list of new SiteChan versions to insert.
   */
  void storeNewChannelVersions(Collection<SiteChan> channels);

  /**
   * Stores new versions of sites.
   *
   * @param sites - A list of new Site versions to insert.
   */
  void storeNewSiteVersions(Collection<Site> sites);
}
