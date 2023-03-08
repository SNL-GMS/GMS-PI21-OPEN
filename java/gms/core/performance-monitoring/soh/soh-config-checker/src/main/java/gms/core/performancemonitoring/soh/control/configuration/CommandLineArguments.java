package gms.core.performancemonitoring.soh.control.configuration;

import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.BooleanOptionHandler;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

import java.util.List;
import java.util.Optional;

/**
 * Class that represents command line arguments for args4j
 */
public class CommandLineArguments {

  @Option(
    name = "-o",
    aliases = {"--osdHostName"},
    required = true,
    usage = "Hostname of the OSD repository"
  )
  private String osdHostName;

  @Option(
    name = "-d",
    aliases = {"--configDir"},
    required = true,
    usage = "Directory containing configuration files"
  )
  private String configurationDirectory;

  @Option(
    name = "-g",
    aliases = {"--stationGroups"},
    required = false,
    handler = StringArrayOptionHandler.class,
    usage = "Use only the listed station groups"
  )
  private List<String> stationGroups;

  @Option(
    name = "-s",
    aliases = {"--stations"},
    required = false,
    handler = StringArrayOptionHandler.class,
    usage = "Use only the listed stations. All station groups will only have these stations. Stations groups without the stations are filtered out."
  )
  private List<String> stations;

  @Option(
    name = "-x",
    required = false,
    handler = BooleanOptionHandler.class,
    usage = "Suppress config info output"
  )
  private boolean suppressInfo;

  @Option(
    name = "-j",
    aliases = {"--json"},
    required = false,
    handler = BooleanOptionHandler.class,
    usage = "Print serialized JSON of StationSohMonitoringDefinition"
  )
  private boolean printJson;

  @Option(
    name = "-jx",
    aliases = {"-xj", "--jsonDump"},
    required = false,
    handler = BooleanOptionHandler.class,
    usage = "Combine -x and -j (only print json)"
  )
  private boolean printJsonButNotInfo;

  @Option(
    name = "-t",
    aliases = {"--timing"},
    required = false,
    handler = BooleanOptionHandler.class,
    usage = "Print timing information"
  )
  private boolean printTimingInfo;

  public String getOsdHostName() {
    return osdHostName;
  }

  public String getConfigurationDirectory() {
    return configurationDirectory;
  }

  public List<String> getStationGroups() {
    return Optional.ofNullable(stationGroups).orElse(List.of());
  }

  public List<String> getStations() {
    return Optional.ofNullable(stations).orElse(List.of());
  }

  public boolean suppressInfo() {
    return suppressInfo || printJsonButNotInfo;
  }

  public boolean printJson() {
    return printJson || printJsonButNotInfo;
  }

  public boolean printTimingInfo() {
    return printTimingInfo && !printJsonButNotInfo;
  }
}
