package gms.dataacquisition.stationreceiver.cd11.datamanipulator;

import org.kohsuke.args4j.Option;

public class DataManipulatorArgs {


  @Option(name = "-fromCustom", usage = "use this if going from custom to original format")
  private boolean fromCustom;

  @Option(name = "-fileLocation", usage = "Path to JSON file to read")
  private String fileLocation;

  @Option(name = "-folderLocation", usage = "Path to folder where JSON files to read in are")
  private String folderLocation;

  @Option(name = "-outputFile", usage = "Name of the file to write to")
  private String outputFile;

  @Option(name = "-outputFolder", usage = "Folder where to put json files created from process. Names will be derived from their original names")
  private String outputFolder;

  public boolean isFromCustom() {
    return fromCustom;
  }

  public String getFileLocation() {
    return fileLocation;
  }

  public String getFolderLocation() {
    return folderLocation;
  }

  public String getOutputFile() {
    return outputFile;
  }

  public String getOutputFolder() {
    return outputFolder;
  }
}
