package gms.dataacquisition.css.stationrefconverter.commandline;

import org.kohsuke.args4j.Option;

public class StationRefConverterCommandLineArgs {

  @Option(name = "-network", required = true, usage = "Path to CSS network file")
  private String networkFile;

  @Option(name = "-outputDir", usage = "Output directory for data files from the converter")
  private String outputDir;

  @Option(name = "-data", required = true, usage = "Path to directory containing stations directories with necessary CSS files")
  private String dataDir;

  public String getNetworkFile() {
    return networkFile;
  }

  public String getOutputDir() {
    return outputDir;
  }

  public String getDataDir() {
    return dataDir;
  }
}
