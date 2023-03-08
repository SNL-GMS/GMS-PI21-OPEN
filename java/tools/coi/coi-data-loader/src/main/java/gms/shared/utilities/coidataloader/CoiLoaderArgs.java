package gms.shared.utilities.coidataloader;

import org.kohsuke.args4j.Option;

/**
 * Command-line arguments for use by {@link CoiLoaderApplication} in calling the {@link CoiLoader}.
 */
public class CoiLoaderArgs {

  @Option(name = "-useService", usage = "Store data to OSD service instead of directly to database")
  private boolean useService;

  @Option(name = "-refNetworks", usage = "Path to ReferenceNetwork[] (JSON)")
  private String refNetworks;

  @Option(name = "-refStations", usage = "Path to ReferenceStation[] (JSON)")
  private String refStations;

  @Option(name = "-refSites", usage = "Path to ReferenceSite[] (JSON)")
  private String refSites;

  @Option(name = "-refChans", usage = "Path to ReferenceChannel[] (JSON)")
  private String refChans;

  @Option(name = "-refSensors", usage = "Path to ReferenceSensor[] (JSON)")
  private String refSensors;

  @Option(name = "-refResponseDir", usage = "Path to directory containing JSON files each with one ReferenceResponse[]")
  private String refResponseDir;

  @Option(name = "-refNetMemberships", usage = "Path to ReferenceNetworkMembership[] (JSON)")
  private String refNetMemberships;

  @Option(name = "-refStaMemberships", usage = "Path to ReferenceStationMembership[] (JSON)")
  private String refStaMemberships;

  @Option(name = "-refSiteMemberships", usage = "Path to ReferenceSiteMembership[] (JSON)")
  private String refSiteMemberships;

  @Option(name = "-stationGroups", usage = "Path to StationGroup[] (JSON)")
  private String stationGroups;

  //  @Option(name = "-processingResponses", usage = "Path to Response[] (JSON)")
  private String processingResponses;

  //  @Option(name = "-events", usage = "Path to Event[] (JSON)")
  private String events;

  //  @Option(name = "-sigDets", usage = "Path to SignalDetection[] (JSON)")
  private String sigDets;

  //  @Option(name = "-masks", usage = "Path to QcMask[] (JSON)")
  private String masks;

  //  @Option(name = "-waveformClaimCheck", usage = "Path to SegmentClaimCheck[] (JSON)")
  private String waveformClaimCheck;

  //  @Option(name = "-wfDir", usage = "Path to directory containing all .w files")
  private String wfDir;

  //  @Option(name = "-fkDir", usage = "Path to directory containing files each with a ChannelSegment<FkSpectra> (JSON")
  private String fkDir;

  public boolean getUseService() {
    return useService;
  }

  public String getRefNetworks() {
    return refNetworks;
  }

  public String getRefStations() {
    return refStations;
  }

  public String getRefSites() {
    return refSites;
  }

  public String getRefChans() {
    return refChans;
  }

  public String getRefSensors() {
    return refSensors;
  }

  public String getRefResponseDir() {
    return refResponseDir;
  }

  public String getRefNetMemberships() {
    return refNetMemberships;
  }

  public String getRefStaMemberships() {
    return refStaMemberships;
  }

  public String getRefSiteMemberships() {
    return refSiteMemberships;
  }

  public String getStationGroups() {
    return stationGroups;
  }

  public String getProcessingResponses() {
    return processingResponses;
  }

  public String getEvents() {
    return events;
  }

  public String getSigDets() {
    return sigDets;
  }

  public String getMasks() {
    return masks;
  }

  public String getWaveformClaimCheck() {
    return waveformClaimCheck;
  }

  public String getWfDir() {
    return wfDir;
  }

  public String getFkDir() {
    return fkDir;
  }
}
